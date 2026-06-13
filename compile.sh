#!/bin/bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

# ── 颜色 ─────────────────────────────────────────────────────────────────────
GREEN='\033[0;32m'; YELLOW='\033[1;33m'; RED='\033[0;31m'; NC='\033[0m'
info()    { echo -e "${GREEN}[compile]${NC} $*"; }
warn()    { echo -e "${YELLOW}[compile]${NC} $*"; }
error()   { echo -e "${RED}[compile]${NC} $*"; }

# ── 参数解析 ──────────────────────────────────────────────────────────────────
MODE="all"          # all | maritime | server | quick
SKIP_TESTS=true
CLEAN=false

usage() {
  cat <<EOF
用法: ./compile.sh [选项]

选项:
  -m, --module <name>   只编译指定模块（maritime / server）
  -c, --clean           编译前执行 mvn clean
  -t, --with-tests      不跳过单元测试（默认跳过）
  -h, --help            显示帮助

示例:
  ./compile.sh                    # 编译整个后端
  ./compile.sh -m maritime        # 只编译 maritime 模块
  ./compile.sh -m server          # 只编译 yudao-server（打包）
  ./compile.sh -c                 # clean 后全量编译
  ./compile.sh -m maritime -c     # clean 后只编译 maritime
EOF
  exit 0
}

while [[ $# -gt 0 ]]; do
  case $1 in
    -m|--module)  MODE="$2"; shift 2 ;;
    -c|--clean)   CLEAN=true;  shift ;;
    -t|--with-tests) SKIP_TESTS=false; shift ;;
    -h|--help)    usage ;;
    *) error "未知参数: $1"; usage ;;
  esac
done

# ── Maven 参数 ────────────────────────────────────────────────────────────────
MVN_OPTS="-T 4"                              # 4线程并行
SKIP_FLAG=""
if $SKIP_TESTS; then
  SKIP_FLAG="-DskipTests"
fi

CLEAN_CMD=""
if $CLEAN; then
  CLEAN_CMD="clean"
fi

START=$(date +%s)

# ── 执行编译 ──────────────────────────────────────────────────────────────────
case "$MODE" in
  all)
    info "编译整个后端项目..."
    mvn $MVN_OPTS $CLEAN_CMD compile $SKIP_FLAG
    ;;

  maritime)
    info "编译 yudao-module-maritime（含依赖模块）..."
    mvn $MVN_OPTS $CLEAN_CMD compile $SKIP_FLAG \
      -pl yudao-module-maritime -am
    ;;

  server)
    info "编译并打包 yudao-server..."
    mvn $MVN_OPTS $CLEAN_CMD package $SKIP_FLAG \
      -pl yudao-server -am
    ;;

  *)
    # 任意 Maven 模块名
    info "编译模块: $MODE"
    mvn $MVN_OPTS $CLEAN_CMD compile $SKIP_FLAG \
      -pl "$MODE" -am
    ;;
esac

END=$(date +%s)
ELAPSED=$((END - START))

echo ""
info "✓ 编译成功  耗时 ${ELAPSED}s"
