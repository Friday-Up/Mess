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
     * 【面试问答】关于 DTO 的常见面试题（非可执行代码）
     * =========================================================================
     *
     * Q1: 为什么需要 DTO？直接返回实体不行吗？
     * A1: DTO 解耦接口与表结构、裁剪敏感字段、承载校验注解、组合多源数据。
     *     直接返回实体会泄露内部字段、触发懒加载异常、接口与数据库强耦合。
     *
     * Q2: DTO 和 VO 有什么区别？
     * A2: 严格区分时：DTO 侧重传输（可入可出），VO 侧重展示（只读）。
     *     小项目通常混用不细分，大项目按场景拆：CreateDto/UpdateDto/QueryVo。
     *
     * Q3: @Valid 和 @Validated 的区别？
     * A3: @Valid 是 JSR 标准，支持嵌套校验但不支持分组；
     *     @Validated 是 Spring 扩展，支持分组校验（groups）。
     *     方法参数上用 @Valid 触发校验，需要分组时改用 @Validated(Group.class)。
     *
     * Q4: Jackson 把 isXxx 序列化成了 xxx，怎么办？
     * A4: Jackson 默认去掉 Boolean 的 is 前缀。用 @JsonProperty("isXxx")
     *     固定 JSON 字段名，或全局配置 Jackson 命名策略。
     *
     * Q5: 为什么时间字段前端拿到的是数组而不是字符串？
     * A5: LocalDateTime 默认序列化成数组 [2024,1,15,10,30,0]。
     *     加 @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
     *     或全局配置 spring.jackson.date-format + time-zone。
     *
     * Q6: MapStruct 比 BeanUtils.copyProperties 好在哪？
     * A6: MapStruct 编译期生成映射代码，字段名拼错编译报错，无反射开销；
     *     BeanUtils 运行时反射，字段名拼错静默失败（不报错但不映射）。
     * =========================================================================
     */
}