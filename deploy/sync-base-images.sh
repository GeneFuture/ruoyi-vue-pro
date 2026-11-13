#!/bin/bash
# 把构建所需的基础镜像同步到自有镜像仓库（CCR/TCR），
# 用于云端构建拉不到 Docker Hub 的场景（429 限流、镜像站证书过期等）。
#
# 用法：
#   bash deploy/sync-base-images.sh <目标仓库前缀> [源镜像站]
#
# 示例：
#   docker login ccr.ccs.tencentyun.com
#   bash deploy/sync-base-images.sh ccr.ccs.tencentyun.com/your-namespace
#
#   # 源镜像站默认 docker.m.daocloud.io，也可用 docker.io（可能被限流）
#   bash deploy/sync-base-images.sh ccr.ccs.tencentyun.com/your-namespace docker.io
set -euo pipefail

TARGET="${1:-}"
SRC="${2:-docker.m.daocloud.io}"
PLATFORM="${DOCKER_PLATFORM:-linux/amd64}"

if [ -z "$TARGET" ]; then
  echo "用法: bash deploy/sync-base-images.sh <目标仓库前缀> [源镜像站]"
  echo "示例: bash deploy/sync-base-images.sh ccr.ccs.tencentyun.com/your-namespace"
  exit 1
fi
TARGET="${TARGET%/}"

command -v docker >/dev/null 2>&1 || { echo "[sync] 缺少命令：docker"; exit 1; }
docker info >/dev/null 2>&1 || { echo "[sync] Docker 未运行，请先启动 Docker / OrbStack"; exit 1; }

IMAGES=(
  "node:22-alpine"
  "maven:3.9-eclipse-temurin-25"
  "eclipse-temurin:25-jre-alpine"
)

for img in "${IMAGES[@]}"; do
  src="$SRC/$img"
  dst="$TARGET/$img"
  echo "==> $src -> $dst"
  docker pull --platform "$PLATFORM" "$src"
  docker tag "$src" "$dst"
  docker push "$dst"
done

echo ""
echo "同步完成：$TARGET"
echo ""
echo "1) 本地构建 root Dockerfile："
echo "   docker build --platform $PLATFORM \\"
echo "     --build-arg NODE_IMAGE=$TARGET/node:22-alpine \\"
echo "     --build-arg MAVEN_IMAGE=$TARGET/maven:3.9-eclipse-temurin-25 \\"
echo "     --build-arg JRE_IMAGE=$TARGET/eclipse-temurin:25-jre-alpine \\"
echo "     -t <镜像>:<tag> ."
echo ""
echo "2) 云端源码构建（控制台无法传 build-arg，修改根 Dockerfile 顶部三个 ARG 默认值）："
echo "   ARG NODE_IMAGE=$TARGET/node:22-alpine"
echo "   ARG MAVEN_IMAGE=$TARGET/maven:3.9-eclipse-temurin-25"
echo "   ARG JRE_IMAGE=$TARGET/eclipse-temurin:25-jre-alpine"
echo ""
echo "3) 本地运行时镜像（deploy/runtime）："
echo "   JRE_IMAGE=$TARGET/eclipse-temurin:25-jre-alpine PUSH=1 bash deploy/build.sh <镜像仓库地址> <tag>"
