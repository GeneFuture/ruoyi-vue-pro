# 升级计划：JDK 25 + Spring Boot 4

## 背景与现状

| 项目 | 分支 | revision | Spring Boot | Java |
|------|------|----------|-------------|------|
| 当前仓库 ruoyi-vue-pro | feat/haizhou | 2026.03-SNAPSHOT | 3.5.9 | 25 |
| 参考仓库 yv-ruoyi-vue-pro | master-jdk25 | 2026.07-jdk25-SNAPSHOT | 4.1.0 | 25 |

- 两仓库共享 git 历史（同一个上游 YunaiV/ruoyi-vue-pro）
- merge-base = `a695eb5d88`（v2026_03 发布）
- feat/haizhou 领先 9 个提交（maritime 模块 + mall-uniapp + member 定制 + 模块裁剪）
- master-jdk25 领先 556 个提交（官方 SB4 升级全量改动）
- 本地工具链：JDK 25.0.1 (GraalVM) + Maven 3.9.9，就绪

## feat/haizhou 分支的定制内容（必须保留）

1. **模块裁剪**：删除 bpm、crm、erp、iot、mes、report 模块（2467 文件）
2. **maritime 模块**：海员培训业务（248 文件，依赖 pay/member）
3. **member 定制**：referralCode 推荐码字段、微信登录增强（约 19 文件）
4. **Convert 批量修改**：@Mapper 增加 `unmappedTargetPolicy = ReportingPolicy.IGNORE`（与上游做法大部分重合，可自动合并）
5. **AiWorkflowServiceImpl**：fastjson → Jackson 迁移（上游迁到 fastjson2，保留我们的 Jackson 版）
6. **yudao-ui**：新增 maritime 管理端页面 + yudao-mall-uniapp 小程序
7. **构建辅助**：compile.sh / clean.sh / run.sh / .mvn/jvm.config（JDK25 native-access 参数）

## merge-tree 预演结果（-X no-renames）

- **7 个内容冲突**：
  1. `pom.xml`（根）→ 取上游版本号 + 保留我们的模块清单（含 maritime）
  2. `yudao-dependencies/pom.xml` → 取上游（SB4 BOM 全量升级）
  3. `sql/tools/convertor.py`、`sql/tools/docker-compose.yaml` → 取上游
  4. `AiWorkflowServiceImpl.java` → 保留我方 Jackson 实现
  5. `MemberUserMapper.java` → 上游为基础 + 合入我方 selectByReferralCode
  6. `PayChannelConvert.java` → 上游 convertList + 我方 @Mapper 策略
- **177 个 modify/delete 冲突**：全部在已裁剪模块（iot 78 / mes 35 / bpm 34 / crm 27 / report 2 / erp 1）→ 一律 `git rm` 保持删除
- yudao-ui 无冲突；未跟踪文件无覆盖风险（已验证）
- rename 检测会把已删模块 pom 误配到 maritime/pom.xml → 必须使用 `-X no-renames`

## 执行步骤

### Phase 0：准备
1. 从 feat/haizhou 创建升级分支 `feat/haizhou-jdk25`（保留原分支不动）
2. stash 工作区未提交的已跟踪文件改动（.DS_Store、mes/wm UI 残留）

### Phase 1：合并
3. `git merge -X no-renames jdk25-ref/master-jdk25`（jdk25-ref 已作为本地 remote fetch）
4. 批量解决 modify/delete：`git rm` 六个已裁剪模块的全部冲突文件
5. 手工解决 7 个内容冲突（按上述策略）
6. 提交 merge commit

### Phase 2：编译修复（核心工作量）
7. `mvn clean install -DskipTests -T 1C` 全量构建
8. 预期需要修复的点：
   - maritime 模块对 SB4/Spring 7/Security 7 API 变更的适配
   - member/pay/mall/mp 中我方定制代码与新版本的兼容
   - MyBatis Plus、MapStruct、Hutool 等依赖版本升级引发的 API 变化
9. 迭代修复直至全部模块编译通过

### Phase 3：验证
10. `mvn package` 产物验证
11. 条件允许时启动 yudao-server 冒烟（依赖本地 MySQL/Redis）
12. 恢复 stash 的工作区改动

### Phase 4：评审
13. 提交 code review（codex），通过后交付

## 风险与对策

| 风险 | 对策 |
|------|------|
| maritime 大量代码不兼容 SB4 | 参考上游对 pay/member 的 SB4 适配模式逐处修改 |
| 本地数据库不可用无法冒烟 | 退化为编译 + package 验证 + 单元测试 |
| merge 后遗漏定制 | 已建立定制清单（上文 7 项），merge 后逐项核对 |
| 构建失败定位困难 | 分模块编译（compile.sh -m）缩小范围 |

## 回滚方案

- feat/haizhou 分支保持不动，任何时刻可 `git checkout feat/haizhou` 回退
- 升级分支上的每一步均有独立 commit，可细粒度回滚
