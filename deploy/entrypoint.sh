#!/bin/sh
# 单容器入口：同时启动 Spring Boot 与 nginx
# 任一进程退出则终止容器，交由云托管重启，避免 Java 挂了但 nginx 还在返回页面
set -eu

# 微信云托管会对出站 HTTPS（如 api.weixin.qq.com）做 TLS 中间代理，并在容器内
# 注入平台自签 CA（常见路径 /usr/local/share/ca-certificates/certificate.crt）。
# curl/openssl 会信任系统证书，但 JVM 只读 $JAVA_HOME/lib/security/cacerts，
# 因此启动时把平台 CA 导入 JVM 信任库；否则 JustAuth 等 java.net.http 调用会报
# PKIX path building failed（WxJava 自带 trust-all，反而不会暴露该问题）。
import_cloudrun_ca() {
  java_home="${JAVA_HOME:-/opt/java/openjdk}"
  cacerts="$java_home/lib/security/cacerts"
  keytool_bin="$java_home/bin/keytool"
  [ -f "$cacerts" ] || return 0
  [ -x "$keytool_bin" ] || keytool_bin=keytool
  command -v "$keytool_bin" >/dev/null 2>&1 || return 0

  found=0
  i=0
  import_ca_file() {
    i=$((i + 1))
    alias="cloudrun-ca-$i"
    # 平台证书可能轮换：每次启动先删旧别名再导入
    "$keytool_bin" -delete -alias "$alias" -keystore "$cacerts" -storepass changeit >/dev/null 2>&1 || true
    if "$keytool_bin" -importcert -noprompt -trustcacerts -alias "$alias" -file "$1" \
         -keystore "$cacerts" -storepass changeit >/dev/null 2>&1; then
      echo "[entrypoint] imported platform CA: $1"
    else
      echo "[entrypoint] WARN: failed to import CA: $1"
    fi
  }

  for f in /usr/local/share/ca-certificates/*.crt /usr/local/share/ca-certificates/*.pem \
           /app/cert/*.crt /app/cert/*.pem /certificates/*.crt /certificates/*.pem; do
    [ -f "$f" ] || continue
    found=1
    import_ca_file "$f"
  done

  # 兜底：平台可能只把证书合并进系统 bundle，没有保留单独文件
  if [ "$found" = "0" ] && [ -f /etc/ssl/certs/ca-certificates.crt ]; then
    tmpdir="$(mktemp -d)"
    awk -v dir="$tmpdir" '/BEGIN CERTIFICATE/{n++} {print > (dir "/ca-" n ".crt")}' \
      /etc/ssl/certs/ca-certificates.crt
    for f in "$tmpdir"/ca-*.crt; do
      [ -f "$f" ] || continue
      # 只导入微信 API 的代理证书，避免把整个系统 CA 重复塞进 JVM
      if "$keytool_bin" -printcert -file "$f" 2>/dev/null | grep -q "api.weixin.qq.com"; then
        found=1
        import_ca_file "$f"
      fi
    done
    rm -rf "$tmpdir"
  fi

  if [ "$found" = "0" ]; then
    echo "[entrypoint] no platform CA found; candidates:"
    ls -l /usr/local/share/ca-certificates /app/cert /certificates 2>/dev/null || true
  fi
}
import_cloudrun_ca

JAVA_OPTS="${JAVA_OPTS:--XX:MaxRAMPercentage=75 -XX:+UseG1GC --enable-native-access=ALL-UNNAMED --sun-misc-unsafe-memory-access=allow}"

echo "[entrypoint] starting backend ..."
# shellcheck disable=SC2086
java $JAVA_OPTS -jar /app/app.jar ${ARGS:-} &
JAVA_PID=$!

echo "[entrypoint] starting nginx ..."
nginx -g 'daemon off;' &
NGINX_PID=$!

stop() {
  echo "[entrypoint] stopping ..."
  kill -TERM "$NGINX_PID" "$JAVA_PID" 2>/dev/null || true
}
trap stop TERM INT

# busybox sh 没有 wait -n，轮询任一子进程
while kill -0 "$JAVA_PID" 2>/dev/null && kill -0 "$NGINX_PID" 2>/dev/null; do
  sleep 1
done

echo "[entrypoint] a child process exited, stopping container ..."
stop

# 给 Spring Boot 最多 30 秒优雅停机
i=0
while [ "$i" -lt 30 ] && { kill -0 "$JAVA_PID" 2>/dev/null || kill -0 "$NGINX_PID" 2>/dev/null; }; do
  sleep 1
  i=$((i + 1))
done
kill -KILL "$JAVA_PID" "$NGINX_PID" 2>/dev/null || true

wait 2>/dev/null || true
exit 1
