# 支付模块接入（来自官方文档）

> 本文件只在 Claude 处理 `yudao-module-maritime/` 下的文件时加载。
> 核心约定：金额在本模块内一律 `BigDecimal` 单位**元**，调用 pay 模块时**必须 ×100 转为分**。

### 依赖配置

```xml
<dependency>
    <groupId>cn.iocoder.boot</groupId>
    <artifactId>yudao-module-pay</artifactId>
    <version>${revision}</version>
</dependency>
```

### 支付流程（微信小程序）

```
业务模块                    pay 模块                   微信
  │── createPayOrder() ──►  创建 pay_order 记录
  │── submitPayOrder() ──►  调用微信 API ──────────►  获取 prepayId
  │◄─────── prepayInfo ─────────────────────────────
  │（前端调用 wx.requestPayment）
  │                         ◄── 微信异步回调
  │                         更新 pay_order 状态
  │◄── 回调业务接口（PayOrderHandler）
```

**Step 1：创建支付单**

```java
// 注入 pay 模块 API
@Resource
private PayOrderApi payOrderApi;

// 创建支付单（金额必须转为分）
PayOrderCreateReqDTO payReq = new PayOrderCreateReqDTO()
    .setAppKey("maritime")                       // pay_app 中配置的 key
    .setUserIp(getClientIP())
    .setMerchantOrderId(order.getOrderNo())      // 我们自己的订单号
    .setSubject("海员培训定金 - " + sessionName)
    .setBody(sessionCode + " 定金")
    .setPrice(order.getAmount()
        .multiply(BigDecimal.valueOf(100)).intValue())  // ⚠️ 元 → 分
    .setExpireTime(order.getExpireTime());

Long payOrderId = payOrderApi.createOrder(payReq);
```

**Step 2：提交获取 prepayId**

```java
@Resource
private PayOrderApi payOrderApi;

PayOrderSubmitRespDTO submitResp = payOrderApi.submitOrder(
    new PayOrderSubmitReqDTO()
        .setId(payOrderId)
        .setChannelCode(PayChannelEnum.WX_LITE.getCode())
        .setChannelExtras(MapUtil.of("openid", memberUser.getOpenId()))
);
// submitResp.getDisplayContent() 包含 prepayId、timeStamp、nonceStr、paySign
```

**Step 3：实现支付回调 Handler**

```java
@Component
public class DepositPayOrderHandler implements PayOrderHandler {

    @Override
    public String getMerchantOrderIdPrefix() {
        return "DEPOSIT-";   // 与订单号前缀匹配
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleOrder(PayOrderDO payOrder) {
        // 1. 幂等：CAS 更新订单状态
        int updated = enrollmentOrderMapper.updateStatusIfPending(
            order.getId(), OrderStatusEnum.PAID);
        if (updated == 0) return;   // 已处理，跳过

        // 2. 校验金额（防篡改）
        BigDecimal paidAmount = BigDecimal.valueOf(payOrder.getPrice())
            .divide(BigDecimal.valueOf(100));  // 分 → 元
        if (paidAmount.compareTo(order.getAmount()) != 0) {
            log.error("[handleOrder] 金额不匹配: {}", payOrder.getMerchantOrderId());
            return;
        }

        // 3. 执行业务逻辑
        enrollmentService.handleDepositPaid(order.getEnrollmentId());

        // 4. 发布异步事件（通知等）
        applicationContext.publishEvent(new DepositPaidEvent(order.getEnrollmentId()));
    }
}
```

### 退款接入

```java
@Resource
private PayRefundApi payRefundApi;

// 发起退款（金额转为分）
PayRefundCreateReqDTO refundReq = new PayRefundCreateReqDTO()
    .setAppKey("maritime")
    .setPayOrderId(order.getPayOrderId())
    .setMerchantRefundId(refundApply.getRefundNo())
    .setReason(refundApply.getApplyReason())
    .setPrice(refundApply.getRefundAmount()
        .multiply(BigDecimal.valueOf(100)).intValue());  // ⚠️ 元 → 分

Long payRefundId = payRefundApi.createRefund(refundReq);
```

退款回调实现 `PayRefundHandler`，逻辑同 `PayOrderHandler`。

### 支付配置（application-local.yaml）

```yaml
yudao:
  pay:
    order-notify-url: http://your-domain/app-api/pay/order/notify
    refund-notify-url: http://your-domain/app-api/pay/refund/notify
```
