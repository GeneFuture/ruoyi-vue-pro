# 运行时镜像（推荐在 Apple Silicon 本机使用）
#
# 前置：先在本机完成原生构建：
#   1) cd yudao-ui/yudao-ui-admin-vue3 && pnpm build:prod      -> dist-prod/
#   2) mvn -T 4 -DskipTests clean package -pl yudao-server -am -> yudao-server/target/yudao-server.jar
#   3) bash deploy/prepare-runtime.sh
#
# 构建（必须指定 amd64，腾讯云为 x86_64）：
#   docker build --platform linux/amd64 -t maritime-app:<tag> deploy/runtime
#
# 构建上下文就是 deploy/runtime/，不包含源码，构建只需要几秒。

FROM eclipse-temurin:25-jre-alpine

RUN apk add --no-cache nginx tzdata curl \
 && cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
 && echo "Asia/Shanghai" > /etc/timezone \
 && mkdir -p /run/nginx /app
WORKDIR /app

COPY web/ /usr/share/nginx/html/
COPY app.jar /app/app.jar
COPY nginx.conf /etc/nginx/http.d/default.conf
COPY entrypoint.sh /app/entrypoint.sh
RUN chmod +x /app/entrypoint.sh

ENV TZ=Asia/Shanghai \
    JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+UseG1GC --enable-native-access=ALL-UNNAMED --sun-misc-unsafe-memory-access=allow"

EXPOSE 80

HEALTHCHECK --interval=30s --timeout=5s --start-period=120s --retries=3 \
  CMD curl -fsS http://127.0.0.1:48080/actuator/health || exit 1

ENTRYPOINT ["/app/entrypoint.sh"]
