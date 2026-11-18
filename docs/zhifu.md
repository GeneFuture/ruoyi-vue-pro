# 微信小程序支付配置手册

> 适用环境：微信云托管 `prod` / 服务 `springboot-csjr`
>
> 小程序 AppID：`wxd895d9ad76ac250f`
>
> 商户号：`1003767637`（武汉海舟信息科技有限公司）
>
> 云托管域名：`https://springboot-csjr-344073-6-1528079030.sh.run.tcloudbase.com`

---

## 0. 前置条件

- 小程序必须是**已认证的企业 / 个体工商户主体**（个人主体不能开通微信支付）
- 已申请微信支付商户号，且商户主体与小程序主体一致（或已完成关联授权）
- 商户平台可登录（商户管理员）

---

## 1. 微信支付商户平台配置

登录 [微信支付商户平台](https://pay.weixin.qq.com)。

### 1.1 商户信息

账户中心 → 商户信息，记录：

```
商户号：1003767637
商户名称：武汉海舟信息科技有限公司
```

### 1.2 关联小程序 AppID（关键）

产品中心 → **AppID账号管理**：

1. 点击「关联 AppID」/「新增授权」
2. 填写小程序 AppID：`wxd895d9ad76ac250f`
3. 如提示主体不一致，还需填写 AppID 认证主体，并上传《微信支付联合营运承诺函》
4. 提交后，到 **微信公众平台 → 微信支付 → 商户号管理 → 待关联商户号** 确认
   （或通过「公众平台安全助手」的消息确认）
5. 关联成功后，状态应显示「已关联」

> 常见报错 `appid和mch_id不匹配`、`商户号该产品权限未开通`，基本都是这一步没完成。

### 1.3 开通 JSAPI 支付

产品中心 → **我的产品**，确认 **JSAPI支付** 已开通。

> 小程序支付与 JSAPI 支付共用权限和下单接口，但调起方式不同。

### 1.4 产品中心 → 开发配置（按截图）

| 配置项 | 小程序支付是否需要 | 说明 |
|---|---|---|
| JSAPI支付 → 支付授权目录 | **不需要** | 这是网页 JSAPI 支付校验页面 URL 用的；小程序 `wx.requestPayment` 不做该校验 |
| Native支付回调链接 | 不需要 | Native 是扫码支付，本项目不用 |
| 支付方式配置 → 云闪付 | 可选 | 不影响小程序支付，不用可以关闭 |

> **不要**把云托管域名填到「支付授权目录」里，填了也不会影响小程序支付。

如果以后部署 H5 版本，并在微信浏览器里用 JSAPI 支付，才需要添加：

```
https://<H5 域名>/
```

要求：HTTPS、以 `/` 结尾、不能带参数、不支持 IP。

### 1.5 账户中心 → API 安全（准备 3 组凭证）

| 位置 | 需要拿到 | 用途 |
|---|---|---|
| API 安全 → API v3 密钥 | 32 位密钥 | 回调报文解密、接口签名 |
| API 安全 → API 证书 | 下载 `apiclient_key.pem`，记录**证书序列号** | 商户请求签名 |
| API 安全 → 微信支付公钥 | 下载 `pub_key.pem`，记录**公钥 ID** | 验证微信返回/回调签名 |

> yudao 当前强制使用「微信支付公钥」模式，必须配置 `pub_key.pem` + 公钥 ID，不能只配平台证书。
>
> 以上密钥/证书不要提交到 Git，也不要贴在公开渠道。

---

## 2. 云托管环境变量（支付回调地址）

微信云托管 → 服务 `springboot-csjr` → 服务设置 → 环境变量：

```dotenv
PAY_ORDER_NOTIFY_URL=https://springboot-csjr-344073-6-1528079030.sh.run.tcloudbase.com/admin-api/pay/notify/order
PAY_REFUND_NOTIFY_URL=https://springboot-csjr-344073-6-1528079030.sh.run.tcloudbase.com/admin-api/pay/notify/refund
PAY_TRANSFER_NOTIFY_URL=https://springboot-csjr-344073-6-1528079030.sh.run.tcloudbase.com/admin-api/pay/notify/transfer
```

保存后重新发布服务。

说明：

- 这三个是**微信 → pay 模块**的回调地址；代码会自动在后面拼接 `/{channelId}`
- 云托管默认域名对微信支付服务端回调是可用的（“仅测试”限制只针对小程序服务器域名配置）
- 正式环境建议换成已备案自定义域名

---

## 3. yudao 后台：创建支付应用

后台 → **支付管理 → 支付应用 → 新增**：

| 字段 | 填写 |
|---|---|
| 应用名 | 海员培训 |
| 应用标识（appKey） | **`maritime`**（必须与代码 `PAY_APP_KEY` 一致） |
| 开启状态 | 开启 |
| 支付结果回调地址 | `https://springboot-csjr-344073-6-1528079030.sh.run.tcloudbase.com/app-api/maritime/order/notify` |
| 退款结果回调地址 | 见下方说明 |
| 转账结果回调地址 | 可留空 |

> **两个回调地址不要混淆：**
>
> - 环境变量 `PAY_ORDER_NOTIFY_URL` = 微信 → pay 模块的回调
> - 支付应用里的「支付结果回调地址」 = pay 模块 → 海员业务模块的回调

**退款回调地址说明：**

支付应用表单里退款回调是必填，但海员模块目前没有退款回调接口。可以：

1. 临时填 `https://springboot-csjr-344073-6-1528079030.sh.run.tcloudbase.com/app-api/maritime/order/notify` 先把应用建起来（退款通知会失败重试，不影响支付）
2. 或者补一个 `/app-api/maritime/order/refund-notify` 接口，返回成功并按 `merchantRefundId` 更新退款申请状态

---

## 4. yudao 后台：配置 wx_lite 渠道

支付管理 → 支付应用列表 → **微信配置 → 小程序 → 配置**：

| 字段 | 填写 |
|---|---|
| 渠道费率 | 实际费率，如 0.6 |
| 微信 APPID | `wxd895d9ad76ac250f` |
| 商户号 | `1003767637` |
| 渠道状态 | 开启 |
| API 版本 | **v3** |
| API V3 密钥 | 32 位 API v3 密钥 |
| apiclient_key.pem | 上传商户 API 证书私钥 |
| 证书序列号 | 商户 API 证书序列号 |
| public_key.pem | 上传微信支付公钥 |
| 公钥 ID | 微信支付公钥 ID |

保存后确认渠道状态为「开启」。

---

## 5. 开启「支付通知 Job」（重要）

后台 → **基础设施 → 定时任务** → 找到 **「支付通知 Job」**（handler `payNotifyJob`）。

默认是**暂停**状态，改成**开启**。

- 支付成功后的第一次业务回调是立即执行的
- 该 Job 负责失败重试；不开启的话，回调失败后报名状态会一直停在待支付

---

## 6. 支付链路说明

```
小程序
  │ 1. POST /app-api/maritime/order/pay-deposit
  │    → 海员模块调用 pay 模块创建支付单，返回 payOrderId
  │
  │ 2. POST /app-api/pay/order/submit
  │    → channelCode=wx_lite，channelExtras={openid}
  │    → pay 模块调用微信统一下单，返回小程序支付参数
  │
  │ 3. uni.requestPayment(...)
  ▼
微信支付
  │ 4. 异步回调 PAY_ORDER_NOTIFY_URL + /{channelId}
  ▼
pay 模块
  │ 5. 更新 pay_order 为已支付
  │ 6. 调用支付应用的「支付结果回调地址」
  ▼
海员模块 /app-api/maritime/order/notify
  │ 7. 校验支付单、更新报名订单/报名状态
  ▼
小程序轮询 /app-api/maritime/order/status 看到已支付
```

关键点：

- 支付应用 appKey 必须是 `maritime`
- 小程序 openid 必须来自 AppID `wxd895d9ad76ac250f`
- 支付应用的回调地址必须公网可达（云托管默认域名即可）
- `payNotifyJob` 负责失败重试

---

## 7. 测试流程

1. 后台创建一个测试班期，定金设为 `0.01` 元
2. 用**体验版 / 正式版**小程序（真机）走完整流程：
   - 登录 → 报名 → 支付定金 → 微信支付
3. 检查：
   - **支付管理 → 支付订单**：状态「已支付」
   - **支付管理 → 支付通知**：`.../app-api/maritime/order/notify` 任务状态「成功」
   - **报名订单**：状态 PAID
   - **报名记录**：状态 DEPOSITED
4. 如果微信扣款成功但报名一直待支付：
   - 看「支付通知」任务里的错误信息
   - 检查支付应用的「支付结果回调地址」
   - 检查 `payNotifyJob` 是否开启
   - 检查云托管日志中的 `/app-api/maritime/order/notify`

---

## 8. 常见报错对照

| 报错 | 原因 / 处理 |
|---|---|
| 支付应用不存在 | 支付应用 appKey 不是 `maritime`，或应用未开启 |
| 支付渠道不存在 | `wx_lite` 渠道没配置 / 没开启 |
| `appid和mch_id不匹配` | 小程序 AppID 未在商户平台关联，见 1.2 |
| `商户号该产品权限未开通` | JSAPI 支付产品未开通，见 1.3 |
| `签名错误` / `API v3 key` | v3 密钥、私钥、证书序列号、公钥 ID 某一项不对 |
| `无效的openid` / `下单账号与支付账号不一致` | 小程序 AppID 与支付渠道 APPID 不一致，openid 必须来自 `wxd895d9ad76ac250f` |
| `当前页面的URL未注册` | 这是 H5 JSAPI 支付才会出现的错误；小程序支付不需要配置支付授权目录 |
| 支付成功但报名未更新 | 支付应用回调地址错误，或 `payNotifyJob` 未开启 |
| 支付回调地址不可访问 | `PAY_*_NOTIFY_URL` 或支付应用的 `orderNotifyUrl` 填错 |

---

## 9. 退款说明

- 海员模块目前没有实现退款回调接口，退款可在 pay 模块后台手动处理
- 如需自动更新报名/退款申请状态，需要补一个 `/app-api/maritime/order/refund-notify` 接口，并把它配置到支付应用的「退款结果回调地址」

---

## 10. 上线前检查清单

- [ ] 小程序主体已认证（企业 / 个体工商户）
- [ ] 商户号 `1003767637` 已关联 AppID `wxd895d9ad76ac250f`，状态「已关联」
- [ ] JSAPI 支付产品已开通
- [ ] API v3 密钥已生成并记录
- [ ] `apiclient_key.pem` 已下载，证书序列号已记录
- [ ] `pub_key.pem` 已下载，公钥 ID 已记录
- [ ] 云托管 `PAY_ORDER_NOTIFY_URL` / `PAY_REFUND_NOTIFY_URL` / `PAY_TRANSFER_NOTIFY_URL` 已配置并发布
- [ ] 支付应用 `maritime` 已创建并开启
- [ ] 支付应用「支付结果回调地址」= `/app-api/maritime/order/notify`
- [ ] `wx_lite` 渠道已配置并开启
- [ ] 「支付通知 Job」已开启
- [ ] 用 0.01 元完成真实支付验证
- [ ] 支付后报名订单 / 报名状态正确更新
- [ ] 敏感凭证未提交到 Git、未出现在公开日志中
