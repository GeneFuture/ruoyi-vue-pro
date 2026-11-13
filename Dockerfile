# syntax=docker/dockerfile:1
#
# 海员培训平台 —— 管理后台 + Spring Boot 单镜像
#
# 构建要求：Docker BuildKit，建议在 amd64 构建机执行；
# Apple Silicon 本机构建完整镜像请加 --platform linux/amd64（会走模拟，较慢）。
#
# 更快的本机构建方式见 deploy/runtime/Dockerfile + deploy/prepare-runtime.sh。
#
# 依赖缓存：下面的 RUN --mount=type=cache 需要 BuildKit（Docker 23+ 默认开启）。
# 若微信云托管构建日志报 "unknown flag: --mount"，说明其构建器不支持 BuildKit，
# 删掉对应行的 --mount=type=cache 参数即可，其余不受影响。

##############################
# 1. 构建管理后台前端
##############################
# 如 Alpine 下个别前端原生依赖安装失败，可换成 node:22-bookworm-slim
FROM node:22-alpine AS web-builder
WORKDIR /build/web

# 国内镜像 + 固定 pnpm 版本（缓存 npm 全局安装包）
RUN --mount=type=cache,target=/root/.npm \
    npm config set registry https://registry.npmmirror.com \
 && npm i -g pnpm@10.33.1

# 先装依赖，利用 Docker 层缓存；缓存 pnpm store 避免每次重新下载依赖
COPY yudao-ui/yudao-ui-admin-vue3/package.json yudao-ui/yudao-ui-admin-vue3/pnpm-lock.yaml ./
RUN --mount=type=cache,target=/root/.local/share/pnpm/store \
    pnpm install --frozen-lockfile

COPY yudao-ui/yudao-ui-admin-vue3/ ./
# .env.prod 中 VITE_BASE_URL 必须为空（同源部署），产物目录为 dist-prod
RUN pnpm build:prod

##############################
# 2. 构建后端
##############################
FROM maven:3.9-eclipse-temurin-25 AS server-builder
WORKDIR /build

ENV MAVEN_OPTS="-Xmx2g"
COPY .mvn/ .mvn/
COPY lombok.config ./
COPY pom.xml ./
COPY yudao-dependencies/ yudao-dependencies/
COPY yudao-framework/ yudao-framework/
COPY yudao-server/ yudao-server/
COPY yudao-module-system/ yudao-module-system/
COPY yudao-module-infra/ yudao-module-infra/
COPY yudao-module-member/ yudao-module-member/
COPY yudao-module-mp/ yudao-module-mp/
COPY yudao-module-pay/ yudao-module-pay/
COPY yudao-module-maritime/ yudao-module-maritime/
COPY deploy/settings.xml /build/settings.xml

# -pl yudao-server -am：只构建 server 及其依赖模块
# 产物：/build/yudao-server/target/yudao-server.jar
# 缓存 /root/.m2：依赖下载是首次构建最慢的部分，缓存后重建只需要编译
RUN --mount=type=cache,target=/root/.m2 \
    mvn -s /build/settings.xml -T 4 -Dmaven.test.skip=true clean package -pl yudao-server -am

##############################
# 3. 运行时：nginx + JRE 25
##############################
FROM eclipse-temurin:25-jre-alpine AS runtime

# nginx 用于托管前端静态文件 + 反向代理后端；curl 用于容器健康检查
RUN apk add --no-cache nginx tzdata curl \
 && cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
 && echo "Asia/Shanghai" > /etc/timezone \
 && mkdir -p /run/nginx /app
WORKDIR /app

COPY --from=web-builder /build/web/dist-prod/ /usr/share/nginx/html/
COPY --from=server-builder /build/yudao-server/target/yudao-server.jar /app/app.jar
COPY deploy/nginx.conf /etc/nginx/http.d/default.conf
COPY deploy/entrypoint.sh /app/entrypoint.sh
RUN chmod +x /app/entrypoint.sh

ENV TZ=Asia/Shanghai \
    JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+UseG1GC --enable-native-access=ALL-UNNAMED --sun-misc-unsafe-memory-access=allow"

# 微信云托管「服务设置 - 端口」必须填 80
EXPOSE 80

HEALTHCHECK --interval=30s --timeout=5s --start-period=120s --retries=3 \
  CMD curl -fsS http://127.0.0.1:48080/actuator/health || exit 1

ENTRYPOINT ["/app/entrypoint.sh"]
