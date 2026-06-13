#!/bin/bash
# 海员培训平台 maritime 模块代码批量生成脚本
# 使用方式：bash script/codegen/maritime-codegen.sh
# 生成的 zip 包保存在 script/codegen/output/ 目录

set -e

BASE_URL="http://localhost:48080"
TENANT_ID="1"
USERNAME="admin"
PASSWORD="admin123"
FRONT_TYPE=20       # Vue3 Element Plus
SCENE=1             # 管理后台
AUTHOR="Gene Ye"
MODULE_NAME="maritime"
PACKAGE_PATH="cn.iocoder.yudao.module.maritime"
DATA_SOURCE_ID=0
PARENT_MENU_ID=5986  # 海员培训 父菜单（通过 API 创建）
OUTPUT_DIR="$(dirname "$0")/output"

mkdir -p "$OUTPUT_DIR"

echo "=== [1/4] 登录获取 Token ==="
TOKEN=$(curl -s -X POST "$BASE_URL/admin-api/system/auth/login" \
  -H "Content-Type: application/json" \
  -H "tenant-id: $TENANT_ID" \
  -d "{\"username\":\"$USERNAME\",\"password\":\"$PASSWORD\",\"captchaVerification\":\"\"}" \
  | python3 -c "import sys,json; r=json.load(sys.stdin); print(r['data']['accessToken'])")

if [ -z "$TOKEN" ]; then
  echo "❌ 登录失败，请确认后端已启动且账号密码正确"
  exit 1
fi
echo "✅ Token 获取成功"

AUTH="-H \"Authorization: Bearer $TOKEN\" -H \"tenant-id: $TENANT_ID\""

# 表配置：表名|业务名|类名|中文描述
TABLES=(
  "maritime_course_session|session|CourseSession|班期管理"
  "maritime_session_fee_config|sessionFeeConfig|SessionFeeConfig|班期费用配置"
  "maritime_enrollment|enrollment|Enrollment|报名管理"
  "maritime_enrollment_order|enrollmentOrder|EnrollmentOrder|报名订单"
  "maritime_refund_apply|refundApply|RefundApply|退费申请"
  "maritime_announcement|announcement|Announcement|最新动态"
  "maritime_user_message|userMessage|UserMessage|站内消息"
  "maritime_groupon_record|grouponRecord|GrouponRecord|拼团记录"
  "maritime_groupon_member|grouponMember|GrouponMember|拼团成员"
  "maritime_referral_relation|referralRelation|ReferralRelation|推荐关系"
  "maritime_commission_record|commissionRecord|CommissionRecord|佣金记录"
  "maritime_commission_account|commissionAccount|CommissionAccount|佣金账户"
  "maritime_risk_blacklist|blacklist|RiskBlacklist|风控黑名单"
)

# 提取表名列表
TABLE_NAMES=()
for entry in "${TABLES[@]}"; do
  TABLE_NAMES+=("$(echo "$entry" | cut -d'|' -f1)")
done

echo ""
echo "=== [2/4] 批量导入 ${#TABLES[@]} 张表 ==="

# 构建 JSON 数组
NAMES_JSON=$(printf '"%s",' "${TABLE_NAMES[@]}")
NAMES_JSON="[${NAMES_JSON%,}]"

IMPORT_RESP=$(curl -s -X POST "$BASE_URL/admin-api/infra/codegen/create-list" \
  -H "Authorization: Bearer $TOKEN" \
  -H "tenant-id: $TENANT_ID" \
  -H "Content-Type: application/json" \
  -d "{\"dataSourceConfigId\":$DATA_SOURCE_ID,\"tableNames\":$NAMES_JSON}")

echo "导入结果: $(echo "$IMPORT_RESP" | python3 -c "import sys,json; r=json.load(sys.stdin); print('成功，IDs:'+str(r.get('data',[]))) if r.get('code')==0 else print('失败:'+r.get('msg',''))")"

echo ""
echo "=== [3/4] 逐表配置生成参数 ==="

for entry in "${TABLES[@]}"; do
  TABLE_NAME=$(echo "$entry" | cut -d'|' -f1)
  BUSINESS_NAME=$(echo "$entry" | cut -d'|' -f2)
  CLASS_NAME=$(echo "$entry" | cut -d'|' -f3)
  CLASS_COMMENT=$(echo "$entry" | cut -d'|' -f4)

  # 查询该表在 codegen 中的 ID
  TABLE_ID=$(curl -s "$BASE_URL/admin-api/infra/codegen/table/list?dataSourceConfigId=$DATA_SOURCE_ID" \
    -H "Authorization: Bearer $TOKEN" \
    -H "tenant-id: $TENANT_ID" \
    | python3 -c "
import sys,json
r=json.load(sys.stdin)
tables=r.get('data',[])
for t in tables:
    if t['tableName']=='$TABLE_NAME':
        print(t['id'])
        break
")

  if [ -z "$TABLE_ID" ]; then
    echo "⚠️  $TABLE_NAME 未找到（可能已存在或导入失败），跳过"
    continue
  fi

  # 获取完整 detail（包含 columns）
  DETAIL=$(curl -s "$BASE_URL/admin-api/infra/codegen/detail?tableId=$TABLE_ID" \
    -H "Authorization: Bearer $TOKEN" \
    -H "tenant-id: $TENANT_ID")

  # 更新表配置
  UPDATE_BODY=$(echo "$DETAIL" | python3 -c "
import sys,json
d=json.load(sys.stdin)['data']
table=d['table']
table['moduleName']='$MODULE_NAME'
table['businessName']='$BUSINESS_NAME'
table['className']='$CLASS_NAME'
table['classComment']='$CLASS_COMMENT'
table['author']='$AUTHOR'
table['frontType']=$FRONT_TYPE
table['scene']=$SCENE
table['parentMenuId']=$PARENT_MENU_ID
print(json.dumps({'table':table,'columns':d['columns']}))
")

  UPDATE_RESP=$(curl -s -X PUT "$BASE_URL/admin-api/infra/codegen/update" \
    -H "Authorization: Bearer $TOKEN" \
    -H "tenant-id: $TENANT_ID" \
    -H "Content-Type: application/json" \
    -d "$UPDATE_BODY")

  STATUS=$(echo "$UPDATE_RESP" | python3 -c "import sys,json; r=json.load(sys.stdin); print('✅' if r.get('code')==0 else '❌ '+r.get('msg',''))")
  echo "$STATUS $TABLE_NAME → businessName=$BUSINESS_NAME className=$CLASS_NAME"
done

echo ""
echo "=== [4/4] 下载生成代码 ==="

for entry in "${TABLES[@]}"; do
  TABLE_NAME=$(echo "$entry" | cut -d'|' -f1)

  TABLE_ID=$(curl -s "$BASE_URL/admin-api/infra/codegen/table/list?dataSourceConfigId=$DATA_SOURCE_ID" \
    -H "Authorization: Bearer $TOKEN" \
    -H "tenant-id: $TENANT_ID" \
    | python3 -c "
import sys,json
r=json.load(sys.stdin)
for t in r.get('data',[]):
    if t['tableName']=='$TABLE_NAME':
        print(t['id'])
        break
")

  if [ -z "$TABLE_ID" ]; then continue; fi

  OUTPUT_FILE="$OUTPUT_DIR/${TABLE_NAME}.zip"
  HTTP_CODE=$(curl -s -w "%{http_code}" -o "$OUTPUT_FILE" \
    "$BASE_URL/admin-api/infra/codegen/download?tableId=$TABLE_ID" \
    -H "Authorization: Bearer $TOKEN" \
    -H "tenant-id: $TENANT_ID")

  if [ "$HTTP_CODE" = "200" ]; then
    SIZE=$(wc -c < "$OUTPUT_FILE")
    echo "✅ $TABLE_NAME.zip (${SIZE} bytes)"
  else
    echo "❌ $TABLE_NAME 下载失败 HTTP=$HTTP_CODE"
  fi
done

echo ""
echo "=== 完成 ==="
echo "生成的 zip 包在：$OUTPUT_DIR"
echo "解压每个 zip 后将文件复制到对应模块目录，并执行其中的 sql.sql 导入菜单"
