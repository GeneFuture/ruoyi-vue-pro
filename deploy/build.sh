#!/bin/bash
# 一键构建前端 + 后端 + 运行时镜像（默认不推送）
#
# 用法：
#   bash deploy/build.sh <镜像仓库地址> [tag]
#   PUSH=1 bash deploy/build.sh <镜像仓库地址> [tag]        # 构建完成后自动 docker push
#   SKIP_WEB=1 bash deploy/build.sh <镜像仓库地址> [tag]    # 只改后端时跳过前端构建
#   SKIP_SERVER=1 bash deploy/build.sh <镜像仓库地址> [tag] # 只改前端时跳过后端构建
#
# 示例：
#   bash deploy/build.sh ccr.ccs.tencentyun.com/your-namespace/maritime-app 20261109
#
# 说明：
#   本机执行可利用 pnpm / Maven 本地缓存，比微信云托管源码构建快很多；
#   deploy/runtime/Dockerfile 只做 COPY，docker build 通常只需几秒。
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
IMAGE_REPO="${1:-}"
TAG="${2:-$(date +%Y%m%d-%H%M)}"
PLATFORM="${DOCKER_PLATFORM:-linux/amd64}"
PUSH="${PUSH:-0}"
SKIP_WEB="${SKIP_WEB:-0}"
SKIP_SERVER="${SKIP_SERVER:-0}"

if [ -z "$IMAGE_REPO" ]; then
  echo "用法: bash deploy/build.sh <镜像仓库地址> [tag]"
  echo "示例: bash deploy/build.sh ccr.ccs.tencentyun.com/your-namespace/maritime-app 20261109"
  exit 1
fi

for cmd in pnpm mvn docker; do
  command -v "$cmd" >/dev/null 2>&1 || { echo "[build] 缺少命令：$cmd"; exit 1; }
done

if [ -n "$(git -C "$ROOT" status --porcelain)" ]; then
  echo "[build] 提示：工作区存在未提交改动，本次构建会包含这些改动"
fi

if [ "$SKIP_WEB" = "1" ]; then
  echo "==> 1/4 跳过前端构建（复用 dist-prod）"
  [ -f "$ROOT/yudao-ui/yudao-ui-admin-vue3/dist-prod/index.html" ] \
    || { echo "[build] dist-prod/index.html 不存在，无法跳过前端构建"; exit 1; }
else
  echo "==> 1/4 构建管理后台前端"
  cd "$ROOT/yudao-ui/yudao-ui-admin-vue3"
  pnpm install --frozen-lockfile
  pnpm build:prod
fi

if [ "$SKIP_SERVER" = "1" ]; then
  echo "==> 2/4 跳过后端构建（复用 target/yudao-server.jar）"
  [ -f "$ROOT/yudao-server/target/yudao-server.jar" ] \
    || { echo "[build] yudao-server/target/yudao-server.jar 不存在，无法跳过后端构建"; exit 1; }
else
  echo "==> 2/4 构建后端（含 maritime 模块）"
  cd "$ROOT"
  mvn -T 4 -Dmaven.test.skip=true clean package -pl yudao-server -am
fi

echo "==> 3/4 装配运行时上下文"
bash "$ROOT/deploy/prepare-runtime.sh"

echo "==> 4/4 校验产物并构建镜像（$PLATFORM）"
jar_has() {
  if command -v unzip >/dev/null 2>&1; then
    unzip -l "$1" | grep -q "$2"
  else
    jar tf "$1" | grep -q "$2"
  fi
}
jar_has "$ROOT/deploy/runtime/app.jar" "BOOT-INF/lib/yudao-module-maritime" \
  || { echo "[build] app.jar 中未找到 maritime 模块，请检查后端构建"; exit 1; }
[ -f "$ROOT/deploy/runtime/web/index.html" ] \
  || { echo "[build] 缺少前端产物 index.html"; exit 1; }

docker build --platform "$PLATFORM" -t "$IMAGE_REPO:$TAG" "$ROOT/deploy/runtime"

if [ "$PUSH" = "1" ]; then
  echo "==> 推送镜像"
  docker push "$IMAGE_REPO:$TAG"
fi

echo ""
echo "构建完成：$IMAGE_REPO:$TAG"
if [ "$PUSH" = "1" ]; then
  echo "镜像已推送"
fi
echo "下一步（微信云托管控制台）："
echo "  部署发布 → 发布 → 选择「镜像」→ 填入 $IMAGE_REPO:$TAG"
echo "  不要选「官方模板」；容器端口必须填 80（镜像内 nginx），健康检查 /actuator/health"
