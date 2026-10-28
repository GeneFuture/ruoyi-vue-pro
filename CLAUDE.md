# CLAUDE.md — 海员培训平台开发指南

本文件描述项目的代码风格、模块约定和开发规范，供 AI 编程助手参考。**所有新代码必须严格遵循以下约定。**

---

## 项目概述

**海员培训平台**：基于 ruoyi-vue-pro 框架的课程报名、拼团、支付与消息通知系统。

- **后端框架**：Spring Boot 3.5.9 + MyBatis Plus 3.5.15
- **Java 版本**：JDK 25
- **数据库**：MySQL 8.0 + Redis 8
- **自定义模块包路径**：`cn.iocoder.yudao.module.maritime`
- **自定义模块目录**：`yudao-module-maritime/`

单模块 `yudao-module-maritime/`，**不拆分 api/biz**；模块清单见根 `pom.xml`，目录树 `ls` 即得，不再赘述。

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
    PAID_OFF("PAID_OFF", "已付清/待开班"),
    MANUAL_PROCESSING("MANUAL_PROCESSING", "人工处理"),
    COMPLETED("COMPLETED", "已完成"),
    CANCELLED("CANCELLED", "已取消");

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
}
```

### 关键规则

- `maritime` 模块错误码段：**1-009-xxx-xxx**（1_009_000_000 起）
- 子模块分段（每段预留 1000 个）：
  - 课程：1_009_001_000
  - 班期：1_009_002_000
  - 报名：1_009_003_000
  - 拼团：1_009_004_000
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
├── T07-尾款支付与退费.md
├── T09-消息通知系统.md
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

## 支付模块接入

见 `yudao-module-maritime/CLAUDE.md`（动到该模块文件时自动加载）：支付单 / 退款 / 微信企业转账的调用方式，以及「元 → 分 ×100」的金额约定。

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

## 前端开发规范

- 小程序端（uni-app）：见 `yudao-ui/yudao-mall-uniapp/CLAUDE.md`
- 管理后台（Vue3）：见 `yudao-ui/yudao-ui-admin-vue3/CLAUDE.md`
