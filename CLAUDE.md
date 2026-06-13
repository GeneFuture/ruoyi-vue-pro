# CLAUDE.md — 海员培训平台开发指南

本文件描述项目的代码风格、模块约定和开发规范，供 AI 编程助手参考。**所有新代码必须严格遵循以下约定。**

---

## 项目概述

**海员培训平台**：基于 ruoyi-vue-pro 框架的课程报名、拼团、推荐佣金系统。

- **后端框架**：Spring Boot 3.5.9 + MyBatis Plus 3.5.15
- **Java 版本**：JDK 25
- **数据库**：MySQL 8.0 + Redis 8
- **自定义模块包路径**：`cn.iocoder.yudao.module.maritime`
- **自定义模块目录**：`yudao-module-maritime/`

---

## 模块结构

### 现有模块（根 pom.xml）

```
yudao-dependencies/   # 依赖版本管理 BOM
yudao-framework/      # 框架封装（starter 集合）
yudao-server/         # 启动入口
yudao-module-system/  # 系统模块（用户、权限、字典）
yudao-module-infra/   # 基础设施（定时任务、文件、代码生成）
yudao-module-member/  # 会员模块（微信登录、用户信息）
yudao-module-pay/     # 支付模块（微信支付、退款、企业付款）
yudao-module-mall/    # 商城模块（trade、product、promotion）
yudao-module-ai/      # AI 模块
```

### 新建 maritime 模块结构（单模块，不拆分 api/biz）

```
yudao-module-maritime/
├── pom.xml
└── src/
    ├── main/
    │   ├── java/cn/iocoder/yudao/module/maritime/
    │   │   ├── controller/
    │   │   │   ├── admin/          # 管理端 Controller（/admin-api/maritime/...）
    │   │   │   └── app/            # 小程序端 Controller（/app-api/maritime/...）
    │   │   ├── service/            # Service 接口 + 实现类（同包）
    │   │   ├── dal/
    │   │   │   ├── dataobject/     # DO 数据对象
    │   │   │   └── mysql/          # Mapper 接口
    │   │   ├── convert/            # MapStruct 转换接口
    │   │   ├── enums/              # 枚举、错误码常量
    │   │   ├── job/                # 定时任务
    │   │   └── mq/                 # 消息队列（生产者/消费者）
    │   └── resources/
    │       └── mapper/maritime/    # XML Mapper（复杂查询用）
    └── test/
        └── resources/sql/
            ├── create_tables.sql
            └── clean.sql
```

---

## Controller 规范

### 注解模板

```java
// 小程序端
@Tag(name = "用户 APP - 课程管理")
@RestController
@RequestMapping("/maritime/course")
@Validated
public class AppCourseController {

    @Resource
    private CourseService courseService;

    @GetMapping("/list")
    @Operation(summary = "获取课程列表")
    @PermitAll  // 无需登录
    public CommonResult<PageResult<AppCourseRespVO>> getCourseList(
            @Valid AppCoursePageReqVO pageReqVO) {
        PageResult<CourseDO> pageResult = courseService.getCoursePageForApp(pageReqVO);
        return success(CourseConvert.INSTANCE.convertPage(pageResult));
    }
}

// 管理端
@Tag(name = "管理后台 - 课程管理")
@RestController
@RequestMapping("/maritime/course")
@Validated
public class AdminCourseController {

    @Resource
    private CourseService courseService;

    @PostMapping("/create")
    @Operation(summary = "创建课程")
    @PreAuthorize("@ss.hasPermission('maritime:course:create')")
    public CommonResult<Long> createCourse(@Valid @RequestBody CourseCreateReqVO createReqVO) {
        return success(courseService.createCourse(createReqVO));
    }

    @GetMapping("/page")
    @Operation(summary = "获得课程分页")
    @PreAuthorize("@ss.hasPermission('maritime:course:query')")
    public CommonResult<PageResult<CourseRespVO>> getCoursePage(
            @Valid CoursePageReqVO pageReqVO) {
        PageResult<CourseDO> pageResult = courseService.getCoursePage(pageReqVO);
        return success(CourseConvert.INSTANCE.convertPage(pageResult));
    }
}
```

### 关键规则

- **依赖注入**：必须用 `@Resource`，**禁止用 `@Autowired`**
- **返回类型**：始终返回 `CommonResult<T>`，使用静态导入的 `success()` 方法
- **分页返回**：`CommonResult<PageResult<T>>`
- **权限标识**：格式 `模块:业务:操作`，如 `maritime:course:create`
- **路径**：
  - 小程序端：`/app-api/maritime/xxx`（框架自动加前缀）
  - 管理端：`/admin-api/maritime/xxx`（框架自动加前缀）
- **Controller 不写业务逻辑**，只做参数校验 + 调用 Service + VO 转换

---

## Service 规范

### 接口 + 实现（同包）

```java
// CourseService.java — 接口
public interface CourseService {

    /**
     * 创建课程
     *
     * @param createReqVO 创建信息
     * @return 课程编号
     */
    Long createCourse(CourseCreateReqVO createReqVO);

    /**
     * 获得课程分页（小程序端）
     */
    PageResult<CourseDO> getCoursePageForApp(AppCoursePageReqVO pageReqVO);

    /**
     * 获得课程，不存在则抛出异常
     */
    CourseDO getCourse(Long id);
}

// CourseServiceImpl.java — 实现
@Service
@Validated
@Slf4j
public class CourseServiceImpl implements CourseService {

    @Resource
    private CourseMapper courseMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createCourse(CourseCreateReqVO createReqVO) {
        CourseDO course = CourseConvert.INSTANCE.convert(createReqVO);
        courseMapper.insert(course);
        return course.getId();
    }

    @Override
    public CourseDO getCourse(Long id) {
        CourseDO course = courseMapper.selectById(id);
        if (course == null) {
            throw ServiceExceptionUtil.exception(COURSE_NOT_EXISTS);
        }
        return course;
    }
}
```

### 关键规则

- 接口方法写 JavaDoc 注释
- 实现类注解：`@Service`, `@Validated`, `@Slf4j`
- 依赖注入：`@Resource`
- 业务异常：`throw ServiceExceptionUtil.exception(ErrorCode)` 或带参数的重载
- 事务：`@Transactional(rollbackFor = Exception.class)`
- Service 返回 DO，**不返回 VO**（转换在 Controller 层做）

---

## DO（数据对象）规范

```java
@TableName(value = "maritime_course", autoResultMap = true)
@KeySequence("maritime_course_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseDO extends BaseDO {

    @TableId
    private Long id;

    private String name;

    private String certificateType;

    private String coverImage;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, String> tuitionDescription;  // JSON 字段用 JacksonTypeHandler

    private Integer status;

    private Integer sortOrder;

    private Long tenantId;
}
```

### BaseDO 已包含的字段（**不要重复定义**）

```
createTime    LocalDateTime   创建时间
updateTime    LocalDateTime   更新时间
creator       String          创建人
updater       String          更新人
deleted       Boolean         逻辑删除（MyBatis Plus @TableLogic）
```

### 关键规则

- 所有 DO 必须继承 `BaseDO`
- 所有表必须有 `tenant_id` 字段（多租户支持）
- JSON 字段：加 `@TableField(typeHandler = JacksonTypeHandler.class)`，同时 `@TableName` 加 `autoResultMap = true`
- 金额字段：`BigDecimal`，单位为**元**（调用 ruoyi Pay 模块时 ×100 转换为分）
- 枚举字段在 DO 中存 String/Integer 原始值，不存枚举对象

---

## Mapper 规范

```java
@Mapper
public interface CourseMapper extends BaseMapperX<CourseDO> {

    default CourseDO selectByName(String name) {
        return selectOne(CourseDO::getName, name);
    }

    default PageResult<CourseDO> selectPage(AppCoursePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<CourseDO>()
                .eqIfPresent(CourseDO::getCertificateType, reqVO.getCertificateType())
                .likeIfPresent(CourseDO::getLocation, reqVO.getLocation())
                .eq(CourseDO::getStatus, 1)  // 只查上架
                .orderByDesc(CourseDO::getSortOrder)
                .orderByDesc(CourseDO::getId));
    }

    // CAS 更新（防并发）
    default int incrementEnrolledCount(Long sessionId, Integer maxStudents) {
        return update(null, new LambdaUpdateWrapper<CourseSessionDO>()
                .setSql("enrolled_count = enrolled_count + 1")
                .eq(CourseSessionDO::getId, sessionId)
                .lt(CourseSessionDO::getEnrolledCount, maxStudents)
                .eq(CourseSessionDO::getDeleted, false));
    }
}
```

### 关键规则

- 继承 `BaseMapperX<DO类>`（框架扩展，支持分页等）
- 查询方法用 **default 方法**（不写 XML，除非查询极复杂）
- 动态条件用 `LambdaQueryWrapperX` + `xxxIfPresent` 方法（自动跳过 null 值）
- 用 Lambda 方法引用（`CourseDO::getName`），**禁止字符串列名**
- **官方强烈建议避免数据库 JOIN**，改用多次单表查询 + Java 内存拼接
- 复杂查询需要 XML 时，放在 `resources/mapper/maritime/` 目录

---

## VO 规范

### ReqVO（请求参数）

```java
@Schema(description = "管理后台 - 课程创建 Request VO")
@Data
public class CourseCreateReqVO {

    @Schema(description = "课程名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "STCW基本安全培训")
    @NotBlank(message = "课程名称不能为空")
    @Size(max = 100)
    private String name;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull
    @InEnum(CommonStatusEnum.class)
    private Integer status;
}
```

### PageReqVO（分页请求）

```java
@Schema(description = "管理后台 - 课程分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CoursePageReqVO extends PageParam {

    @Schema(description = "证书类型", example = "STCW")
    private String certificateType;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;
}
```

### RespVO（响应数据）

```java
@Schema(description = "管理后台 - 课程信息 Response VO")
@Data
public class CourseRespVO {

    @Schema(description = "课程ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "课程名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "STCW基本安全培训")
    private String name;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;
}
```

### 关键规则

- **@Schema** 每个字段都要写，必填字段加 `requiredMode = Schema.RequiredMode.REQUIRED`
- ReqVO 字段加校验注解（`@NotNull`, `@NotBlank`, `@Size`, `@Min`, `@Max`）
- PageReqVO 继承 `PageParam`，加 `@EqualsAndHashCode(callSuper = true)` 和 `@ToString(callSuper = true)`
- CreateReqVO / UpdateReqVO / SaveReqVO / PageReqVO / RespVO 分别建文件

---

## Convert（对象转换）规范

```java
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CourseConvert {

    CourseConvert INSTANCE = Mappers.getMapper(CourseConvert.class);

    CourseDO convert(CourseCreateReqVO reqVO);

    CourseRespVO convert(CourseDO course);

    AppCourseRespVO convertForApp(CourseDO course);

    default PageResult<CourseRespVO> convertPage(PageResult<CourseDO> page) {
        return new PageResult<>(
                page.getList().stream().map(this::convert).collect(Collectors.toList()),
                page.getTotal()
        );
    }
}
```

### 关键规则

- 使用 **MapStruct**（`@Mapper` 来自 `org.mapstruct`）
- 设置 `unmappedTargetPolicy = ReportingPolicy.IGNORE`
- 提供静态 `INSTANCE` 单例
- 多个同类型转换方法命名：`convert`、`convert01`、`convertForApp` 等

---

## 枚举规范

```java
@Getter
@AllArgsConstructor
public enum EnrollmentStatusEnum {

    PENDING_DEPOSIT("PENDING_DEPOSIT", "待支付定金"),
    DEPOSITED("DEPOSITED", "已付定金"),
    PENDING_BALANCE("PENDING_BALANCE", "待付尾款"),
    IN_PROGRESS("IN_PROGRESS", "上课中"),
    COMPLETED("COMPLETED", "已完成"),
    CANCELLED("CANCELLED", "已取消"),
    REFUNDING("REFUNDING", "退款中");

    private final String status;
    private final String name;

    public static EnrollmentStatusEnum getByStatus(String status) {
        return EnumUtil.getBy(EnrollmentStatusEnum.class,
                e -> Objects.equals(status, e.getStatus()));
    }
}
```

### 关键规则

- `@Getter` + `@AllArgsConstructor`（Lombok）
- 提供 `getByXxx` 静态查找方法，使用 Hutool `EnumUtil.getBy()`
- 枚举常量名用大写下划线，`status`/`type` 字段存数据库的实际值

---

## 错误码规范

```java
// enums/ErrorCodeConstants.java
public interface ErrorCodeConstants {

    // ========== 课程模块 1-009-001-000 ==========
    ErrorCode COURSE_NOT_EXISTS = new ErrorCode(1_009_001_000, "课程不存在");
    ErrorCode COURSE_NAME_DUPLICATE = new ErrorCode(1_009_001_001, "课程名称已存在");

    // ========== 班期模块 1-009-002-000 ==========
    ErrorCode SESSION_NOT_EXISTS = new ErrorCode(1_009_002_000, "班期不存在");
    ErrorCode SESSION_NOT_OPEN = new ErrorCode(1_009_002_001, "班期不在招生状态");
    ErrorCode SESSION_FULL = new ErrorCode(1_009_002_002, "班期名额已满");

    // ========== 报名模块 1-009-003-000 ==========
    ErrorCode ENROLLMENT_NOT_EXISTS = new ErrorCode(1_009_003_000, "报名记录不存在");
    ErrorCode ENROLLMENT_DUPLICATE = new ErrorCode(1_009_003_001, "同一班期不能重复报名");

    // ========== 拼团模块 1-009-004-000 ==========
    ErrorCode GROUPON_NOT_EXISTS = new ErrorCode(1_009_004_000, "拼团不存在");
    ErrorCode GROUPON_EXPIRED = new ErrorCode(1_009_004_001, "拼团已超时");

    // ========== 推荐/佣金模块 1-009-005-000 ==========
    ErrorCode REFERRAL_CODE_NOT_EXISTS = new ErrorCode(1_009_005_000, "推荐码不存在");
    ErrorCode COMMISSION_NOT_EXISTS = new ErrorCode(1_009_005_001, "佣金记录不存在");
}
```

### 关键规则

- `maritime` 模块错误码段：**1-009-xxx-xxx**（1_009_000_000 起）
- 子模块分段（每段预留 1000 个）：
  - 课程：1_009_001_000
  - 班期：1_009_002_000
  - 报名：1_009_003_000
  - 拼团：1_009_004_000
  - 推荐/佣金：1_009_005_000
- 异常抛出：`throw ServiceExceptionUtil.exception(COURSE_NOT_EXISTS);`
- 带参数：`throw ServiceExceptionUtil.exception(USER_MOBILE_USED, mobile);`（消息用 `{}` 占位符）

---

## 数据库 SQL 规范

### 建表约定

```sql
CREATE TABLE maritime_course (
  id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '课程ID',
  name            VARCHAR(100) NOT NULL                COMMENT '课程名称',
  status          TINYINT(1)   NOT NULL DEFAULT 1      COMMENT '状态（0下架 1上架）',
  sort_order      INT          NOT NULL DEFAULT 0       COMMENT '排序权重（越大越靠前）',
  -- ⚠️ 以下4字段为框架必填字段（BaseDO）
  creator         VARCHAR(64)  NOT NULL DEFAULT ''      COMMENT '创建者',
  updater         VARCHAR(64)  NOT NULL DEFAULT ''      COMMENT '更新者',
  created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted         TINYINT(1)   NOT NULL DEFAULT 0       COMMENT '逻辑删除（0正常 1删除）',
  tenant_id       BIGINT       NOT NULL DEFAULT 0       COMMENT '租户ID',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程模板表';
```

### 关键规则

- 所有表名前缀：`maritime_`
- 所有表必须有 **BaseDO 对应字段**：`creator`, `updater`, `created_at`, `updated_at`, `deleted`, `tenant_id`
- 金额字段用 `DECIMAL(10,2)`，单位为**元**
- 状态字段用 `VARCHAR(20)` 或 `TINYINT(1)`（根据是否有多状态）
- 所有字符串字段用 `utf8mb4`（支持 emoji）
- 项目**不使用 Flyway**，SQL 文件手动执行

---

## pom.xml 依赖规范

```xml
<!-- yudao-module-maritime/pom.xml -->
<dependencies>
    <!-- Web + Security -->
    <dependency>
        <groupId>cn.iocoder.boot</groupId>
        <artifactId>yudao-spring-boot-starter-security</artifactId>
    </dependency>

    <!-- MyBatis Plus -->
    <dependency>
        <groupId>cn.iocoder.boot</groupId>
        <artifactId>yudao-spring-boot-starter-mybatis</artifactId>
    </dependency>

    <!-- 多租户 -->
    <dependency>
        <groupId>cn.iocoder.boot</groupId>
        <artifactId>yudao-spring-boot-starter-biz-tenant</artifactId>
    </dependency>

    <!-- 跨模块调用 pay 模块 -->
    <dependency>
        <groupId>cn.iocoder.boot</groupId>
        <artifactId>yudao-module-pay</artifactId>
        <version>${revision}</version>
    </dependency>

    <!-- 跨模块调用 member 模块 -->
    <dependency>
        <groupId>cn.iocoder.boot</groupId>
        <artifactId>yudao-module-member</artifactId>
        <version>${revision}</version>
    </dependency>

    <!-- Test -->
    <dependency>
        <groupId>cn.iocoder.boot</groupId>
        <artifactId>yudao-spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### 关键规则

- 版本统一在 `yudao-dependencies` BOM 中管理，**不要写版本号**（框架 starter 用 BOM 管理）
- 跨模块依赖用 `${revision}` 占位符
- 依赖注意区分 `main`（生产依赖）和 `test` scope

---

## 常用框架类速查

| 类名 | 包路径 | 用途 |
|------|--------|------|
| `BaseDO` | `cn.iocoder.yudao.framework.mybatis.core.dataobject` | DO 基类 |
| `BaseMapperX` | `cn.iocoder.yudao.framework.mybatis.core.mapper` | Mapper 基类 |
| `LambdaQueryWrapperX` | `cn.iocoder.yudao.framework.mybatis.core.query` | 动态查询 |
| `PageParam` | `cn.iocoder.yudao.framework.common.pojo` | 分页请求基类 |
| `PageResult` | `cn.iocoder.yudao.framework.common.pojo` | 分页结果 |
| `CommonResult` | `cn.iocoder.yudao.framework.common.pojo` | 统一响应 |
| `ServiceExceptionUtil` | `cn.iocoder.yudao.framework.common.exception.util` | 业务异常工具 |
| `ErrorCode` | `cn.iocoder.yudao.framework.common.exception` | 错误码类 |
| `SecurityFrameworkUtils` | `cn.iocoder.yudao.framework.security.core.util` | 获取登录用户 |

### 获取当前登录用户

```java
// 获取 userId
Long userId = SecurityFrameworkUtils.getLoginUserId();

// 获取完整登录信息
LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();
```

---

## 定时任务规范

```java
@Slf4j
@Component
public class OrderExpireJob implements JobHandler {

    @Resource
    private EnrollmentService enrollmentService;

    @Override
    public String execute(String param) throws Exception {
        log.info("[OrderExpireJob] 开始执行订单过期检查");
        int count = enrollmentService.closeExpiredOrders();
        log.info("[OrderExpireJob] 完成，关闭订单数：{}", count);
        return "处理订单数：" + count;
    }
}
```

- 框架使用 **Quartz + MySQL** 实现分布式任务（不是 XXL-JOB）
- 实现 `JobHandler` 接口，Spring Bean 名即为控制台配置的 Handler 名
- 多租户场景加 `@TenantJob`，会并行遍历每个租户执行
- 方法内捕获并记录每条记录的异常，**不让单条失败影响整批**
- 在后台「基础设施 → 定时任务」界面配置 Cron 表达式，无需硬编码

---

## 命名约定速查

| 类型 | 命名规则 | 示例 |
|------|---------|------|
| 数据表 | `maritime_` 前缀 + 下划线 | `maritime_course_session` |
| DO 类 | 驼峰 + `DO` 后缀 | `CourseSessionDO` |
| Mapper | 驼峰 + `Mapper` 后缀 | `CourseSessionMapper` |
| Service 接口 | 驼峰 + `Service` 后缀 | `CourseSessionService` |
| Service 实现 | 驼峰 + `ServiceImpl` 后缀 | `CourseSessionServiceImpl` |
| 创建 ReqVO | 驼峰 + `CreateReqVO` | `CourseCreateReqVO` |
| 更新 ReqVO | 驼峰 + `UpdateReqVO` | `CourseUpdateReqVO` |
| 分页 ReqVO | 驼峰 + `PageReqVO` | `CoursePageReqVO` |
| 响应 VO | 驼峰 + `RespVO` | `CourseRespVO` |
| 小程序 VO | `App` 前缀 + 驼峰 + `RespVO` | `AppCourseRespVO` |
| Convert | 驼峰 + `Convert` | `CourseConvert` |
| Admin Controller | `Admin` 前缀 + 驼峰 + `Controller` | `AdminCourseController` |
| App Controller | `App` 前缀 + 驼峰 + `Controller` | `AppCourseController` |
| 枚举 | 驼峰 + `Enum` 后缀 | `EnrollmentStatusEnum` |
| 错误码 | 全大写下划线 | `COURSE_NOT_EXISTS` |

---

## 业务金额约定

- **DO / VO / Service 层**：`BigDecimal`，单位**元**
- **调用 ruoyi Pay 模块**（创建支付单、退款单、企业付款）：必须 `×100` 转为 `Integer`（分）
- 转换示例：`order.getAmount().multiply(BigDecimal.valueOf(100)).intValue()`

---

## 开发任务文档位置

```
docs/开发任务/
├── README.md           # 任务总览和依赖关系图
├── T00-环境搭建.md
├── T01-数据库与模块骨架.md
├── T02-微信登录与课程展示.md
├── T03-报名流程.md
├── T04-定金支付集成.md
├── T05-拼团功能.md
├── T06-推荐系统与佣金触发.md
├── T07-尾款支付与退费.md
├── T08-佣金结算与发放.md
├── T09-消息通知系统.md
├── T10-风控增强.md
├── T11-数据统计与报表.md
└── T12-测试与上线.md
```

**开发前必读对应任务文档**，文档中包含完整的接口定义、SQL、Service 逻辑伪代码。

---

## 新建模块步骤（来自官方文档）

1. 创建 `yudao-module-maritime/pom.xml`，parent 设为 `yudao`
2. 在**根 `pom.xml`** 的 `<modules>` 中添加 `<module>yudao-module-maritime</module>`
3. 在 **`yudao-server/pom.xml`** 中添加 maritime 模块依赖
4. 创建包结构 `cn.iocoder.yudao.module.maritime`（遵循框架包扫描约定）
5. 启动项目，在控制台确认 Controller 已注册，访问 `/doc.html` 验证接口可见

> Controller 路径约定：admin 端加 `/admin-api` 前缀，app 端加 `/app-api` 前缀，框架自动处理，无需手写。

---

## VO 命名约定（官方推荐）

官方代码生成器生成的 VO 有两种风格，项目统一用以下方式：

| 场景 | 类名 | 说明 |
|------|------|------|
| 新建请求 | `XxxSaveReqVO` 或 `XxxCreateReqVO` | 不含 `createTime`、`creator` 等系统字段 |
| 更新请求 | `XxxSaveReqVO` 或 `XxxUpdateReqVO` | 同上，含 `id` 字段 |
| 分页查询 | `XxxPageReqVO` | 继承 `PageParam` |
| 响应数据 | `XxxRespVO` | 含所有字段含 `createTime` |

> 官方说明：新建/修改请求不传 `createTime`、`creator`，而响应需要返回，因此拆开是必要的。

---

## 参数校验规范（来自官方文档）

### 基本用法

```java
// Controller 类加 @Validated，方法参数加 @Valid
@PostMapping("/create")
public CommonResult<Long> create(@RequestBody @Valid CourseCreateReqVO reqVO) { ... }

// 路径参数/Query 参数直接加校验注解
@GetMapping("/get")
public CommonResult<CourseRespVO> get(
        @RequestParam @NotNull(message = "编号不能为空") Long id) { ... }
```

### 常用校验注解

```java
@NotNull        // 不能为 null（所有类型）
@NotBlank       // 不能为空字符串（String，trim 后判断）
@NotEmpty       // 集合/数组不能为空
@Size(max=100)  // 字符串长度限制
@Min(1)         // 最小值
@Max(200)       // 最大值
@Length(min=4, max=16)      // 字符串长度范围
@Pattern(regexp="^[0-9]+$") // 正则校验
@Email          // 邮箱格式
@Mobile         // 手机号格式（框架自定义注解）
@InEnum(XxxEnum.class)      // 枚举值校验（框架自定义注解）
```

### 校验失败响应

校验失败自动被全局异常处理器捕获，返回：
```json
{"code": 400, "msg": "请求参数不正确: 课程名称不能为空"}
```

---

## 支付模块接入（来自官方文档）

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

### 微信企业转账（佣金发放）

```java
@Resource
private PayTransferApi payTransferApi;

// 发起转账（金额转为分）
PayTransferCreateReqDTO transferReq = new PayTransferCreateReqDTO()
    .setAppKey("maritime")
    .setChannelCode(PayChannelEnum.WX_LITE.getCode())
    .setMerchantTransferId(commission.getTransferNo())
    .setType(PayTransferTypeEnum.WX_BALANCE.getType())
    .setUserAccount(memberUser.getOpenId())          // 收款方 openid
    .setUserName(memberUser.getRealName())
    .setPrice(commission.getCommissionAmount()
        .multiply(BigDecimal.valueOf(100)).intValue())  // ⚠️ 元 → 分
    .setSubject("培训推荐佣金");

Long payTransferId = payTransferApi.createTransfer(transferReq);
```

> ⚠️ 微信转账需用户在小程序中确认收款（`requestMerchantTransfer()`），比支付宝多一步。

### 支付配置（application-local.yaml）

```yaml
yudao:
  pay:
    order-notify-url: http://your-domain/app-api/pay/order/notify
    refund-notify-url: http://your-domain/app-api/pay/refund/notify
    transfer-notify-url: http://your-domain/app-api/pay/transfer/notify
```

---

## 异步事件（Spring Event）

轻量级异步处理（通知、日志等不影响主流程的操作）使用 Spring Event，**不引入 MQ 中间件**。

### 发布事件

```java
@Component
@RequiredArgsConstructor
public class EnrollmentEventProducer {

    private final ApplicationContext applicationContext;

    public void sendDepositPaidEvent(Long enrollmentId) {
        applicationContext.publishEvent(
            new DepositPaidEvent(this, enrollmentId));
    }
}

// 事件类
public class DepositPaidEvent extends ApplicationEvent {
    private final Long enrollmentId;
    public DepositPaidEvent(Object source, Long enrollmentId) {
        super(source);
        this.enrollmentId = enrollmentId;
    }
}
```

### 监听事件

```java
@Component
@Slf4j
public class DepositPaidEventListener {

    @Resource
    private MessageService messageService;

    @EventListener
    @Async   // ⚠️ 必须加，默认同步执行会阻塞主线程
    public void onDepositPaid(DepositPaidEvent event) {
        log.info("[onDepositPaid] 处理定金支付事件, enrollmentId={}", event.getEnrollmentId());
        messageService.sendDepositSuccessMessage(event.getEnrollmentId());
    }
}
```

> `@Async` 是必须的，Spring Event 默认同步，不加会阻塞支付回调主流程。

---

## 幂等性（防重复提交）

```java
// 方法级别防重：@Idempotent
@PostMapping("/create")
@Idempotent(
    timeout = 10,
    timeUnit = TimeUnit.SECONDS,
    message = "正在处理，请勿重复提交",
    keyResolver = UserIdempotentKeyResolver.class  // 按用户隔离
)
public CommonResult<Long> createEnrollment(@Valid @RequestBody AppEnrollmentCreateReqVO reqVO) {
    ...
}
```

底层用 Redis 存储 MD5(方法+参数) 作为 key，TTL 内重复请求直接拦截。

---

## 代码生成器使用步骤

1. 建好数据库表
2. 后台「基础设施 → 代码生成 → 基于 DB 导入」选择 `maritime_*` 表
3. 编辑配置：设置模块名 `maritime`、包路径 `cn.iocoder.yudao.module.maritime`、业务名
4. 预览代码确认无误，下载压缩包
5. 将生成的文件复制到对应目录（DO/Mapper/Service/Controller/VO/Convert）
6. 执行生成的 `sql/sql.sql` 初始化后台菜单
7. 直接在生成代码基础上修改业务逻辑，**不要重复生成**（后续加字段直接改代码）

> 代码生成器节省约 80% 工作量，剩余 20% 是自定义业务逻辑。

---

## 获取当前登录用户（修正版）

```java
// App 端（小程序）— 获取会员 userId
Long memberId = getLoginUserId();  // 静态导入 SecurityFrameworkUtils

// Admin 端（管理后台）— 获取系统用户 userId
Long adminUserId = getLoginUserId();

// 获取完整信息
LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();
Integer userType = loginUser.getUserType();  // 区分会员/管理员

---

## 小程序端（uni-app）开发规范

**项目路径**：`yudao-ui/yudao-mall-uniapp/`

### 目录结构

```
yudao-mall-uniapp/
├── pages/                  # 页面（按功能分目录）
│   ├── index/              # 首页、分类、登录、搜索
│   ├── goods/              # 商品详情
│   ├── order/              # 订单列表、详情
│   ├── pay/                # 支付页面
│   ├── user/               # 用户中心
│   ├── activity/           # 活动页（拼团、秒杀等）
│   ├── commission/         # 分销佣金
│   └── public/             # 公共页（登录授权等）
├── sheep/                  # 框架核心库
│   ├── api/                # API 请求（按业务域）
│   ├── components/         # 公共组件（前缀 s-xxx）
│   ├── hooks/              # 组合式函数
│   ├── platform/           # 平台适配（微信/支付宝/H5）
│   ├── request/            # HTTP 封装（luch-request）
│   ├── store/              # Pinia 状态管理
│   ├── ui/                 # UI 基础组件（前缀 su-xxx）
│   └── scss/               # 全局样式
└── pages.json              # 路由配置
```

### API 调用规范

```js
// sheep/api/maritime/course.js
import request from '@/sheep/request';

const CourseApi = {
  // 获取课程列表（分页）
  getCoursePage: (params) => request({ url: '/app-api/maritime/course/page', method: 'GET', data: params }),

  // 获取课程详情
  getCourse: (id) => request({ url: '/app-api/maritime/course/get', method: 'GET', data: { id } }),

  // 报名登记
  createEnrollment: (data) => request({
    url: '/app-api/maritime/enrollment/create',
    method: 'POST',
    data,
    custom: { showSuccess: true, successMsg: '报名成功', loadingMsg: '提交中' }
  }),
};
export default CourseApi;
```

- 文件名：`camelCase.js`，导出对象名：`XxxApi`
- URL 前缀：App 端接口用 `/app-api/`（框架自动带 `baseUrl`，无需写完整域名）
- `custom` 字段控制 loading/成功提示/错误提示

### Pinia Store 规范

```js
// sheep/store/maritime.js
import { defineStore } from 'pinia';
import CourseApi from '@/sheep/api/maritime/course';

const maritime = defineStore('maritime', {
  state: () => ({
    courseList: [],
    currentCourse: null,
  }),
  actions: {
    async getCourseList(params) {
      const { code, data } = await CourseApi.getCoursePage(params);
      if (code !== 0) return;
      this.courseList = data.list;
    },
  },
});
export default maritime;
```

- Store 文件放 `sheep/store/`，用 `defineStore` Options API 风格
- 页面通过 `sheep.$store('maritime')` 访问
- 响应码 `0` 为成功（框架约定）

### 页面组件规范

```vue
<template>
  <s-layout title="课程列表">
    <view class="ss-p-20">
      <view
        v-for="course in state.courses"
        :key="course.id"
        class="ss-m-b-20"
        @tap="onCourseDetail(course.id)"
      >
        <s-goods-item :title="course.name" :img="course.picUrl" :price="course.price" />
      </view>
    </view>
  </s-layout>
</template>

<script setup>
  import { reactive } from 'vue';
  import { onLoad } from '@dcloudio/uni-app';
  import sheep from '@/sheep';
  import CourseApi from '@/sheep/api/maritime/course';

  const state = reactive({ courses: [] });

  onLoad(async () => {
    const { code, data } = await CourseApi.getCoursePage({});
    if (code === 0) state.courses = data.list;
  });

  const onCourseDetail = (id) => {
    sheep.$router.go('/pages/maritime/course-detail', { id });
  };
</script>
```

**关键约定：**
- 使用 `<script setup>` + Composition API
- 生命周期钩子从 `@dcloudio/uni-app` 导入（`onLoad`、`onShow`、`onPullDownRefresh`）
- 布局用 `<s-layout>`，内置导航栏和 tabbar 支持
- 框架内置组件前缀 `s-`（如 `s-goods-item`、`s-empty`、`s-layout`）
- UI 基础组件前缀 `su-`（如 `su-tabs`、`su-sticky`）
- 路由跳转用 `sheep.$router.go(path, params)`
- 全局状态用 `sheep.$store('storeName')`
- CSS 类用框架内置工具类（`ss-p-20`=padding 20rpx，`ss-m-b-20`=margin-bottom 20rpx，`ss-flex`=display flex 等）

### 微信登录流程

```js
// 平台适配层自动处理，页面层调用：
import AuthUtil from '@/sheep/api/member/auth';

// 微信小程序登录
const res = await AuthUtil.weixinMiniAppLogin(code, encryptedData, iv);
// 登录成功后 token 自动存 uni.setStorageSync('token', ...)
// 用户信息存 sheep.$store('user').userInfo
```

- 微信登录由 `sheep/platform/provider/wechat/` 处理平台差异（`#ifdef MP-WEIXIN`）
- 需要登录的接口在 `custom.auth = true` 时自动弹出授权弹窗

### 支付流程（小程序端）

```js
import PayApi from '@/sheep/api/pay/order';

// 1. 调后端创建支付单
const { code, data } = await PayApi.createPayOrder({ bizOrderId: orderId, channelCode: 'wx_lite' });

// 2. 发起微信支付
uni.requestPayment({
  provider: 'wxpay',
  ...data.channelExtras,  // timeStamp, nonceStr, package, signType, paySign
  success: () => { /* 支付成功跳转 */ },
  fail: () => { /* 处理失败 */ },
});
```

### 海员培训平台新增页面位置

```
pages/maritime/
├── course-list.vue      # T02: 课程列表
├── course-detail.vue    # T02: 课程详情
├── session-list.vue     # T02: 期次列表（某课程下的开课场次）
├── enrollment.vue       # T03: 报名表单（填姓名、身份证等）
├── enrollment-pay.vue   # T04: 定金支付确认
└── my-enrollments.vue   # 我的报名记录
```

在 `pages.json` 中注册：
```json
{
  "pages": [
    { "path": "pages/maritime/course-list", "style": { "navigationBarTitleText": "课程列表" } },
    { "path": "pages/maritime/course-detail", "style": { "navigationBarTitleText": "课程详情" } }
  ]
}
```

---

## 管理后台（Vue3）开发规范

**项目路径**：`yudao-ui/yudao-ui-admin-vue3/src/`

### 目录结构

```
src/
├── api/                  # API 请求（按业务域，TS 类型）
│   └── maritime/
│       ├── course.ts
│       └── enrollment.ts
├── views/                # 页面视图（按业务域）
│   └── maritime/
│       ├── course/
│       │   ├── index.vue       # 列表页
│       │   └── CourseForm.vue  # 新增/编辑弹窗
│       └── enrollment/
│           └── index.vue
└── router/               # 路由（通常动态加载，菜单权限控制）
```

### API 文件规范（TypeScript）

```ts
// src/api/maritime/course.ts
import request from '@/config/axios'

export interface CourseVO {
  id: number
  name: string
  certificateType: number
  price: number       // 单位：分
  status: number
  createTime: Date
}

export interface CoursePageReqVO extends PageParam {
  name?: string
  certificateType?: number
  status?: number
  createTime?: Date[]
}

// 查询课程分页
export const getCoursePage = (params: CoursePageReqVO) => {
  return request.get({ url: '/admin-api/maritime/course/page', params })
}

// 查询课程详情
export const getCourse = (id: number) => {
  return request.get({ url: '/admin-api/maritime/course/get?id=' + id })
}

// 新增课程
export const createCourse = (data: CourseVO) => {
  return request.post({ url: '/admin-api/maritime/course/create', data })
}

// 修改课程
export const updateCourse = (data: CourseVO) => {
  return request.put({ url: '/admin-api/maritime/course/update', data })
}

// 删除课程
export const deleteCourse = (id: number) => {
  return request.delete({ url: '/admin-api/maritime/course/delete?id=' + id })
}
```

**关键约定：**
- 接口 URL 前缀：Admin 端用 `/admin-api/`
- 每个接口函数单独导出（named export），不用默认导出对象
- VO 类型声明在同一文件顶部
- 分页参数继承 `PageParam`（框架全局类型）

### 列表页（index.vue）规范

```vue
<template>
  <!-- 搜索栏 -->
  <ContentWrap>
    <el-form ref="queryFormRef" :inline="true" :model="queryParams" class="-mb-15px" label-width="68px">
      <el-form-item label="课程名称" prop="name">
        <el-input v-model="queryParams.name" clearable placeholder="请输入课程名称" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon class="mr-5px" icon="ep:search" />搜索</el-button>
        <el-button @click="resetQuery"><Icon class="mr-5px" icon="ep:refresh" />重置</el-button>
        <el-button v-hasPermi="['maritime:course:create']" type="primary" @click="openForm(undefined)">
          <Icon class="mr-5px" icon="ep:plus" />新增
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table v-loading="loading" :data="list">
      <el-table-column label="课程名称" prop="name" />
      <el-table-column label="价格" prop="price">
        <template #default="{ row }">¥ {{ fenToYuan(row.price) }}</template>
      </el-table-column>
      <el-table-column label="状态" prop="status">
        <template #default="{ row }">
          <dict-tag :type="DICT_TYPE.COMMON_STATUS" :value="row.status" />
        </template>
      </el-table-column>
      <el-table-column label="操作">
        <template #default="{ row }">
          <el-button v-hasPermi="['maritime:course:update']" link type="primary" @click="openForm(row.id)">编辑</el-button>
          <el-button v-hasPermi="['maritime:course:delete']" link type="danger" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <Pagination :total="total" v-model:page="queryParams.pageNo" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </ContentWrap>

  <!-- 新增/编辑弹窗 -->
  <CourseForm ref="formRef" @success="getList" />
</template>

<script setup lang="ts">
  import { ref, reactive, onMounted } from 'vue'
  import { getCoursePage, deleteCourse } from '@/api/maritime/course'
  import { DICT_TYPE } from '@/utils/dict'
  import { fenToYuan } from '@/utils'
  import CourseForm from './CourseForm.vue'
  import { ElMessage, ElMessageBox } from 'element-plus'

  const loading = ref(false)
  const list = ref([])
  const total = ref(0)
  const queryParams = reactive({ pageNo: 1, pageSize: 10, name: '' })
  const queryFormRef = ref()
  const formRef = ref()

  const getList = async () => {
    loading.value = true
    try {
      const data = await getCoursePage(queryParams)
      list.value = data.list
      total.value = data.total
    } finally {
      loading.value = false
    }
  }

  const handleQuery = () => { queryParams.pageNo = 1; getList() }
  const resetQuery = () => { queryFormRef.value.resetFields(); handleQuery() }
  const openForm = (id?: number) => formRef.value.open(id)

  const handleDelete = async (id: number) => {
    await ElMessageBox.confirm('确认删除？', '提示', { type: 'warning' })
    await deleteCourse(id)
    ElMessage.success('删除成功')
    await getList()
  }

  onMounted(() => getList())
</script>
```

### 弹窗表单（XxxForm.vue）规范

```vue
<template>
  <Dialog v-model="dialogVisible" :title="dialogTitle">
    <el-form ref="formRef" v-loading="formLoading" :model="formData" :rules="formRules" label-width="80px">
      <el-form-item label="课程名称" prop="name">
        <el-input v-model="formData.name" placeholder="请输入课程名称" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="formData.status">
          <el-radio v-for="dict in getIntDictOptions(DICT_TYPE.COMMON_STATUS)" :key="dict.value" :value="dict.value">
            {{ dict.label }}
          </el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button :disabled="formLoading" type="primary" @click="submitForm">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
  import { ref, reactive } from 'vue'
  import { createCourse, updateCourse, getCourse } from '@/api/maritime/course'
  import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
  import { ElMessage } from 'element-plus'

  const emit = defineEmits(['success'])
  const dialogVisible = ref(false)
  const dialogTitle = ref('新增课程')
  const formLoading = ref(false)
  const formData = reactive({ id: undefined, name: '', status: 0 })
  const formRules = { name: [{ required: true, message: '请输入课程名称', trigger: 'blur' }] }
  const formRef = ref()

  const open = async (id?: number) => {
    dialogVisible.value = true
    dialogTitle.value = id ? '修改课程' : '新增课程'
    if (id) {
      formLoading.value = true
      try {
        Object.assign(formData, await getCourse(id))
      } finally {
        formLoading.value = false
      }
    }
  }
  defineExpose({ open })

  const submitForm = async () => {
    await formRef.value.validate()
    formLoading.value = true
    try {
      formData.id ? await updateCourse(formData) : await createCourse(formData)
      ElMessage.success(formData.id ? '修改成功' : '新增成功')
      dialogVisible.value = false
      emit('success')
    } finally {
      formLoading.value = false
    }
  }
</script>
```

**关键约定：**
- 弹窗用框架封装的 `<Dialog>` 组件（不是 el-dialog）
- 通过 `defineExpose({ open })` 暴露给父组件调用
- 权限控制用 `v-hasPermi="['maritime:course:create']"` 指令
- 字典渲染用 `<dict-tag>` 或 `getIntDictOptions(DICT_TYPE.xxx)`
- 价格显示用工具函数 `fenToYuan()`（分转元）
- 表格分页用 `<Pagination>` 组件（框架封装）

### 海员培训平台管理后台新增视图位置

```
src/views/maritime/
├── course/
│   ├── index.vue          # T02: 课程管理列表
│   └── CourseForm.vue     # T02: 课程新增/编辑
├── session/
│   ├── index.vue          # T02: 期次管理列表
│   └── SessionForm.vue
├── enrollment/
│   └── index.vue          # T03: 报名记录（只读，审核）
└── commission/
    └── index.vue          # T07: 佣金记录

src/api/maritime/
├── course.ts
├── session.ts
├── enrollment.ts
└── commission.ts
```
```
