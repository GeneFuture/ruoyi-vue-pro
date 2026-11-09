#!/bin/bash
# 一键构建前端 + 后端 + 运行时镜像（不自动推送）
#
# 用法：
#   bash deploy/build.sh <镜像仓库地址> [tag]
# 示例：
#   bash deploy/build.sh ccr.ccs.tencentyun.com/your-namespace/maritime-app 20261109
#
# 推送：
#   docker push <镜像仓库地址>:<tag>
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
IMAGE_REPO="${1:-}"
TAG="${2:-$(date +%Y%m%d-%H%M)}"
PLATFORM="${DOCKER_PLATFORM:-linux/amd64}"

if [ -z "$IMAGE_REPO" ]; then
  echo "用法: bash deploy/build.sh <镜像仓库地址> [tag]"
  echo "示例: bash deploy/build.sh ccr.ccs.tencentyun.com/your-namespace/maritime-app 20261109"
  exit 1
fi

echo "==> 1/4 构建管理后台前端"
cd "$ROOT/yudao-ui/yudao-ui-admin-vue3"
pnpm install --frozen-lockfile
pnpm build:prod

echo "==> 2/4 构建后端（含 maritime 模块）"
cd "$ROOT"
mvn -T 4 -DskipTests clean package -pl yudao-server -am

echo "==> 3/4 装配运行时上下文"
bash "$ROOT/deploy/prepare-runtime.sh"

echo "==> 4/4 构建镜像（$PLATFORM）"
docker build --platform "$PLATFORM" -t "$IMAGE_REPO:$TAG" "$ROOT/deploy/runtime"

echo ""
echo "构建完成：$IMAGE_REPO:$TAG"
echo "下一步："
echo "  docker push $IMAGE_REPO:$TAG"
