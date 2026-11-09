#!/bin/sh
# 单容器入口：同时启动 Spring Boot 与 nginx
# 任一进程退出则终止容器，交由云托管重启，避免 Java 挂了但 nginx 还在返回页面
set -eu

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
