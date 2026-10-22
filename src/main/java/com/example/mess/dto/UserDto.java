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
     * 【Spring 内幕】Jackson 序列化内幕：泛型、反射与序列化器缓存（非可执行代码）
     * =========================================================================
     *
     * 一、为什么 DTO 要与 Entity 分离（内存与框架层理由）
     *   - Jackson 序列化 DTO 时基于反射读取 getter，默认只认字段顺序
     *   - Entity 如果直接输出，会触发关联关系加载（@OneToMany 懒加载代理
     *     也可能被序列化 -> 诱发 N+1 查询和 LazyInitializationException）
     *   - DTO 拷贝（本类字段均为简单类型）让序列化边界清晰可控，
     *     且能自由调整字段顺序或隐藏内部字段
     *
     * 二、ObjectMapper 写出一个 UserDto 的完整过程
     *   1) writeValueAsString(dto)：查找 BeanSerializer
     *   2) BeanSerializer 通过 SerializerCache 查 UserDto 类 -> 自定义或默认
     *   3) JacksonIntrospector 扫描 public getter/getterField -> BeanPropertyDefinition
     *      - @JsonProperty 改变字段名；@JsonIgnore 排除字段
     *      - 本类未加注解 -> 默认所有 getter 逐一输出
     *   4) 对 Long id：LongSerializer（Jackson 内置）；对 null 字段：NullSerializer
     *   5) 合成 JSON 字符串，写入 Writer
     *   关键点：序列化器是按 Class 缓存（ConcurrentHashMap），首次反射慢，
     *   之后复用 -> 高频接口性能靠类稳定，避免每次动态生成
     *
     * 三、反序列化的对称流程（POST /api/users 的 @RequestBody）
     *   1) readValue(json, UserDto.class)：JsonParser 流式读取
     *   2) 按类的默认构造函数实例化（要求有无参构造，Jackson 的隐含契约）
     *   3) 逐字段 setter 或字段直写（默认字段可见性规则）
     *   4) 失败行为：JsonProcessingException -> 由 HttpMessageConverter 包装，
     *      最终被 GlobalExceptionHandler 转为 400
     *   进阶：JSON 转换默认不校验业务规则 -> DTO 上需要显式 @Valid 注解
     *
     * 四、泛型与 TypeReference：为什么 List<UserDto> 需要特殊处理
     *   Jackson 处理 List.class 时无法推断元素类型（泛型擦除），
     *   所以必须传 TypeReference<List<UserDto>> 或 JavaType；
     *   Spring 的 MethodParameter 在方法签名里保留了泛型信息，
     *   这就是 ObjectMapper 能正确序列化 Page<UserDto> 的根本原因
     *
     * 五、Java 17+ record 的演进对比
     *   1) record 是不可变数据载体：编译器生成全参构造/getter/equals/hashCode
     *   2) 传统 DTO（本类）vs record 的本质差异：DTO 可变、Jackson 需无参构造；
     *      record 不可变、反序列化走 canonical 构造（Jackson 2.12+ 原生支持）
     *   3) 性能上 record 字段直接访问，少了反射 setter 的开销
     *   4) 可读性上 record 更接近纯数据表达，适合值对象语义
     *   5) 兼容性：record 隐式 final 不能被继承，与 @JsonProperty 等注解可共存，
     *      但有动态属性（如 @ConfigurationProperties）时不适合 record
     *   6) 本项目保持传统 DTO：兼容 ModelMapper 等工具，迁移需评估全部依赖点
     *
     * 六、Bean Validation 与 @Valid 的框架层机制
     *   1) 参数前加 @Valid -> RequestResponseBodyMethodProcessor 触发 Validator
     *   2) 校验失败抛 MethodArgumentNotValidException -> 400（需 Advice 捕获）
     *   3) 常用注解：@NotNull/@NotBlank/@Size(max=50)/@Email/@Pattern
     *   4) 分组校验：@Validated(Create.class) 区分新增与更新的校验规则
     *   5) 本类当前未加校验注解，UserService 手动检查用户名/邮箱唯一，
     *      引入 @Valid 后可把唯一性外的格式校验下沉到 DTO 层
     * =========================================================================
     */
}