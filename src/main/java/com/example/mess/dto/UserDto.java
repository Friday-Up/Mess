package com.example.mess.dto;

import java.time.LocalDateTime;

/**
 * 用户数据传输对象（DTO） - 用于各层之间安全传输用户数据。
 * 
 * <p>DTO模式的核心价值在于解耦内部实体与外部接口：
 * <ul>
 *   <li>避免直接暴露实体类内部结构（如JPA注解、数据库映射细节）</li>
 *   <li>只包含对外暴露的必要字段，不含密码等敏感信息</li>
 *   <li>支持API版本演进，实体变更不影响外部接口契约</li>
 *   <li>转换逻辑集中在UserService中，便于维护和测试</li>
 * </ul>
 * 
 * <p>字段说明:
 * <ul>
 *   <li><b>id</b> - 用户唯一标识，由数据库自动生成</li>
 *   <li><b>username</b> - 用户名，用于登录认证，具有唯一性约束，1-50字符</li>
 *   <li><b>email</b> - 电子邮箱，用于通信通知，具有唯一性约束，1-100字符</li>
 *   <li><b>name</b> - 用户真实姓名，用于显示，无唯一性约束，1-100字符</li>
 *   <li><b>createdAt</b> - 用户创建时间，由数据库自动设置，只读字段</li>
 * </ul>
 * 
 * <p>与实体类的区别:
 * <ul>
 *   <li>不包含JPA注解（@Entity, @Table, @Column等）</li>
 *   <li>不包含数据库字段映射细节</li>
 *   <li>不包含密码等敏感字段</li>
 *   <li>可用于Jackson序列化，直接返回给前端</li>
 * </ul>
 * 
 * @see com.example.mess.entity.User 对应的实体类
 * @see com.example.mess.service.UserService 包含DTO转换逻辑
 * @since 1.0
 */
public class UserDto {
    
    /**
     * 用户唯一标识。
     * <p>由数据库自动生成（IDENTITY策略），创建时不需要指定。
     * 作为主键用于所有查询、更新、删除操作。
     */
    private Long id;

    /**
     * 用户名，用于登录认证。
     * <p>具有唯一性约束，长度1-50字符。
     * 在用户注册时需要检查唯一性（通过existsByUsername）。
     */
    private String username;

    /**
     * 电子邮箱，用于通信通知。
     * <p>具有唯一性约束，长度1-100字符。
     * 在用户注册时需要检查唯一性（通过existsByEmail）。
     */
    private String email;

    /**
     * 用户真实姓名，用于显示。
     * <p>无唯一性约束，长度1-100字符。
     * 可选字段，允许为空。
     */
    private String name;

    /**
     * 用户创建时间。
     * <p>由数据库自动设置为CURRENT_TIMESTAMP，只读字段。
     * 创建用户时不需要指定，更新时不可修改。
     */
    private LocalDateTime createdAt;

    /** 获取用户ID。 @return 用户唯一标识，新建用户序列化输入时可为null */
    public Long getId() {
        return id;
    }

    /** 设置用户ID，创建请求中通常留空,由服务端在响应中回填。 @param id 用户唯一标识 */
    public void setId(Long id) {
        this.id = id;
    }

    /** 获取用户名。 @return 登录用用户名 */
    public String getUsername() {
        return username;
    }

    /** 设置用户名,须唯一且长度控制在1-50字符,创建/更新时为必填项。 @param username 用户名 */
    public void setUsername(String username) {
        this.username = username;
    }

    /** 获取电子邮箱。 @return 邮箱地址 */
    public String getEmail() {
        return email;
    }

    /** 设置电子邮箱,须唯一且符合邮箱格式,长度1-100字符。 @param email 邮箱地址 */
    public void setEmail(String email) {
        this.email = email;
    }

    /** 获取用户真实姓名。 @return 姓名，可能为null */
    public String getName() {
        return name;
    }

    /** 设置用户真实姓名,为可选展示字段,允许为空。 @param name 姓名 */
    public void setName(String name) {
        this.name = name;
    }

    /** 获取创建时间。 @return 用户创建时间 */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /** 设置创建时间,该字段为只读,客户端传入的值会被服务端忽略。 @param createdAt 创建时间 */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /*
     * =========================================================================
     * 【HTTP/REST】DTO 作为 API 契约的字段规范与演进策略（非可执行代码）
     * =========================================================================
     *
     * 一、DTO 是契约的"法律文本"
     *   1) 字段 = 条款：客户端按字段编程，服务端按字段保证语义
     *   2) 类型 = 解释：Long id 类型即承诺"数字、可能大、不是字符串"
     *   3) Null 语义：每个字段需明确"可空吗？空时表示什么？"
     *      缺乏 null 语义文档是客户端 NPE 的高发源头
     *   4) 顺序无关：JSON 字段顺序不应被客户端依赖（规范未保证），
     *      测试断言要用字段名访问而非位置索引

     * 二、字段层面的八条 REST 规范（本类体检）
     *   1) 命名 camelCase ✓（id/username/email/name/createdAt）
     *   2) 只读字段服务端回填，忽略客户端输入 ✓（createdAt 已在 setter 中防御）
     *   3) 不暴露内部主键代替代业务键？本项目直接暴露 id 可接受（见 User 块）
     *   4) 枚举字段值用字符串（"ACTIVE"）而非数字 1/2——契约可读性
     *   5) 时间 ISO 8601 ✓（LocalDateTime + Jackson 默认输出）
     *   6) 不嵌套过多层：两层已是上限（DTO 内含 AddressDto 可以）
     *   7) 拒绝冗余字段：能从已有字段计算出的不重复暴露（age 可由 birthday 算）
     *   8) 拒绝动词字段：DTO 字段应全是"数据"，不含"动作指令"语义

     * 三、字段演进的三条安全规则（向兼容）
     *   1) 加字段：安全——旧客户端忽略未知字段（Jackson 默认行为）
     *   2) 改字段类型：不安全——旧客户端反序列化失败，必须新版本接口
     *   3) 删字段：破坏性——先标 @Deprecated + 文档，至少一个版本周期
     *      再下线；硬删会让旧客户端启动崩溃
     *   4) 必填变可选 / 可选变必填：必填变可选安全；可选变必填破坏性
     *   5) 实践：DTO 加 jsonSchema 描述或 Swagger @Schema(description=...)
     *      把契约文档化，OpenAPI 文档从代码生成（见 Swagger 配置）

     * 四、请求 DTO 与响应 DTO 的分界（项目演进参考）
     *   1) 本类"一身两职"（创建请求 + 查询响应），小项目可接受
     *   2) 中等规模后拆分：
     *      UserCreateRequest（POST body：username/email/password——不含 id/createdAt）
     *      UserUpdateRequest（PUT body：可改字段）
     *      UserResponse（GET body：id/username/email/name/createdAt——不含 password）
     *   3) 拆分收益：
     *      a) 校验注解按请求场景分别配（Create 加 @NotNull id 预填校验）
     *      b) 防止"响应里把创建才有的字段也带出"
     *      c) 文档与客户端生成代码更准
     *   4) 拆分代价：类数量 +2，转换方法 +2——规模小不值，规模大必须

     * 五、序列化与反序列化的契约细节
     *   1) Jackson 默认按 getter 序列化、setter 反序列化——字段命名跟 Java
     *   2) 未知字段：默认反序列化抛错，配置 FAIL_ON_UNKNOWN_PROPERTIES=false
     *      让客户端可发送"含未知字段"的请求（未来兼容）——本项目 application.yml 已配
     *   3) 日期序列化：默认 timestamps 数字（毫秒），建议配置
     *      spring.jackson.serialization.write-dates-as-timestamps=false
     *      输出 ISO 字符串（人类可读、跨语言解析一致）
     *   4) 数字精度：Long 大于 2^53 时 JS 精度丢失（id 到 9007199254740993 后）
     *      解法：序列化为字符串（@JsonSerialize(using=ToStringSerializer.class)）
     *      在雪花 ID 场景必修——自增 id 暂不涉及但必须提前评估
     *   5) 编码：UTF-8 全链路（Content-Type: application/json;charset=UTF-8），
     *      防乱码——本项目默认配置已是
     * =========================================================================
     */
}