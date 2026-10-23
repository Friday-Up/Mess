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
     * 【Java 并发】不可变对象与安全发布：DTO 为何天然线程安全（非可执行代码）
     * =========================================================================
     *
     * 一、不可变性的形式定义（JLS 视角）
     *   1) 对象创建后状态不能被修改（所有字段不再被赋值）
     *   2) 所有字段都是 final（这是可见性保证的必要条件，不可省）
     *   3) 对象被正确构造（this 未逸出，无构造期共享）
     *   三条全成立，该对象才称得上不可变（Immutable），可被任意多线程
     *   无锁共享；缺一条都要按"可变对象"的规则同步
     *
     * 二、本 DTO 的不可变性体检
     *   1) 字段：id/username/email/name/createdAt 均为 String/Long/LocalDateTime，
     *      成员类型自身不可变（见 User 注释块第四节）
     *   2) but：字段本身未声明 final，setter 公开 -> 严格意义上是"可变 POJO"
     *      并发场景里仍可能被 setter 改动（读写竞争）
     *   3) 升级方案（按成本递增）：
     *      a) 去除 setter、字段加 final、只保留全参构造（毫无序列化兼容问题
     *         用 Jackson @JsonCreator 即可）
     *      b) 改为 Java 17 record（编译器自动 final + equals/hashCode），
     *         Jackson 2.12+ 原生支持 canonical 构造
     *   4) 现实折中：本类主要用于"一次性请求响应对象"，生命周期短，
     *      风险面小；但任何被放入缓存或被异步任务持有的 DTO 必须升级
     *
     * 三、安全发布（Safe Publication）的四种渠道
     *   1) 基于 volatile 字段引用：写线程把构造好的 DTO 赋给 volatile 变量，
     *      读线程看到引用时也必然看到全部字段初值（happens-before 传递）
     *   2) 基于静态初始化器：static final UserDto DEFAULT = new UserDto(...)
     *      类加载机制保证构造完成前任何线程无法访问
     *   3) 基于 final 字段语义：在构造函数结束时，final 字段的值对全部
     *      线程可见（JMM 的 final 语义，无需额外同步）—— record 天然符合
     *   4) 基于并发容器：放入 ConcurrentHashMap 后，任何 get 到引用的线程
     *      都能看到构造时的完整状态
     *   反例：直接把 DTO 赋给普通字段并跨线程读，会发生"看到 null 字段"
     *   或"字段半初始化"的灵异现象
     *
     * 四、DTO 复制与对象逃逸
     *   1) convertToDto 的拷贝发生在当前请求线程内，随后立即响应给客户端，
     *      这是"最安全的逃逸路径"：随请求写入 HTTP 响应后即被 GC
     *   2) 危险逃逸：把 DTO 塞进 List 缓存，再被另一个请求线程读过期条目
     *      -> 即便本类升级为不可变，List 本身仍需并发容器保护
     *   3) 修改已有 DTO 集合：流式 map().collect(Collectors.toList()) 产生
     *      的 List 是普通 ArrayList，若被跨线程迭代需 unmodifiableList 包装
     *   4) 排查经验：偶发 ConcurrentModificationException 多来自"一端迭代、
     *      另一端 add/remove"，根治方式是让集合固化（冻结发布）或换并发容器
     * =========================================================================
     */
}