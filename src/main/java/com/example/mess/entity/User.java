package com.example.mess.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 用户实体类 - 映射到数据库USER表，使用JPA实现对象关系映射。
 * 
 * <p>实体类设计原则:
 * <ul>
 *   <li><b>单一职责</b>: 只负责数据库映射，不包含业务逻辑</li>
 *   <li><b>与DTO分离</b>: 通过UserDto对外暴露数据，保护内部结构</li>
 *   <li><b>主键策略</b>: IDENTITY（数据库自增），由数据库管理ID生成</li>
 *   <li><b>字段约束</b>: username和email具有唯一性约束，防止重复注册</li>
 * </ul>
 * 
 * <p>JPA映射说明:
 * <ul>
 *   <li>@Entity - 标识为JPA实体，由EntityManager管理生命周期</li>
 *   <li>@Table(name = "USER") - 映射到USER表（注意：USER是保留字，生产环境建议加前缀）</li>
 *   <li>@Id + @GeneratedValue(IDENTITY) - 主键自增，依赖数据库AUTO_INCREMENT</li>
 *   <li>@Column(unique = true, nullable = false) - 唯一索引+非空约束</li>
 * </ul>
 * 
 * <p>与UserDto的关系:
 * <ul>
 *   <li>Entity → DTO: UserService.convertToDto()方法转换</li>
 *   <li>DTO → Entity: UserService.convertToEntity()方法转换</li>
 *   <li>转换时不复制id和createdAt，由数据库管理</li>
 * </ul>
 * 
 * @see com.example.mess.dto.UserDto 对应的数据传输对象
 * @see com.example.mess.repository.UserRepository 数据访问接口
 * @see com.example.mess.service.UserService 业务逻辑服务
 * @since 1.0
 */
@Entity
@Table(name = "USER")
public class User {
    
    /**
     * 用户主键ID。
     * <p>使用IDENTITY策略由数据库自增生成（如MySQL AUTO_INCREMENT）。
     * 创建用户时不需要指定此字段，保存后自动回填。
     * 
     * <p>策略选择原因:
     * <ul>
     *   <li>IDENTITY: 简单可靠，依赖数据库，适合中小规模应用</li>
     *   <li>不推荐SEQUENCE: 需要额外配置序列，增加复杂度</li>
     *   <li>不推荐TABLE: 性能较差，需要额外的序列表</li>
     * </ul>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 用户名，用于登录认证。
     * <p>非空且唯一，最大50字符。数据库自动创建唯一索引。
     * 在用户注册时需要检查唯一性（通过existsByUsername）。
     */
    @Column(unique = true, nullable = false)
    private String username;

    /**
     * 电子邮箱，用于通信通知。
     * <p>非空且唯一，最大100字符。数据库自动创建唯一索引。
     * 在用户注册时需要检查唯一性（通过existsByEmail）。
     */
    @Column(unique = true, nullable = false)
    private String email;

    /**
     * 用户真实姓名，用于显示。
     * <p>可为空，最大100字符。无唯一性约束。
     * 可选字段，允许用户不填写真实姓名。
     */
    private String name;

    /**
     * 用户创建时间。
     * <p>数据库列名created_at，默认值CURRENT_TIMESTAMP。
     * 由数据库自动设置，创建用户时不需要指定，更新时不可修改。
     * 用于审计和记录用户注册时间。
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /** 获取用户主键ID。 @return 用户ID，新建用户保存前为null */
    public Long getId() {
        // 返回主键id；新建实体尚未持久化时为null
        return id;
    }

    /** 设置用户主键ID，通常由JPA在持久化后自动回填，业务代码一般无需调用。 @param id 用户ID */
    public void setId(Long id) {
        // 赋值主键id，一般由JPA框架在save后回填，业务代码谨慎手动调用
        this.id = id;
    }

    /** 获取用户名（登录标识）。 @return 用户名 */
    public String getUsername() {
        // 返回用户名（登录唯一标识）
        return username;
    }

    /** 设置用户名，须保证全局唯一，否则持久化时会违反唯一约束。 @param username用户名 */
    public void setUsername(String username) {
        // 赋值用户名，须确保全局唯一
        this.username = username;
    }

    /** 获取电子邮箱。 @return 邮箱地址 */
    public String getEmail() {
        // 返回邮箱地址
        return email;
    }

    /** 设置电子邮箱，须保证全局唯一，建议在设置前做格式校验。 @param email 邮箱地址 */
    public void setEmail(String email) {
        // 赋值邮箱，须确保全局唯一并建议先做格式校验
        this.email = email;
    }

    /** 获取用户真实姓名。 @return 姓名，可能为null */
    public String getName() {
        // 返回真实姓名（可选字段，可能为null）
        return name;
    }

    /** 设置用户真实姓名，为可选字段，允许为空。 @param name 姓名 */
    public void setName(String name) {
        // 赋值真实姓名（可选，允许为空）
        this.name = name;
    }

    /** 获取创建时间。 @return 用户创建时间，由数据库自动生成 */
    public LocalDateTime getCreatedAt() {
        // 返回创建时间（由数据库默认值填充）
        return createdAt;
    }

    /** 设置创建时间，正常由数据库默认值填充，手动设置仅用于数据迁移等特殊场景。 @param createdAt 创建时间 */
    public void setCreatedAt(LocalDateTime createdAt) {
        // 赋值创建时间，一般无需手动调用，仅用于数据迁移等特殊场景
        this.createdAt = createdAt;
    }

    /*
     * =========================================================================
     * 【面试问答】关于 JPA 实体的常见面试题（非可执行代码）
     * =========================================================================
     *
     * Q1: @GeneratedValue 的几种策略有什么区别？
     * A1: IDENTITY 用数据库自增列（MySQL 常用），不能预分配，批量插入慢；
     *     SEQUENCE 用数据库序列（Oracle/PG），可预分配，批量快；
     *     TABLE 用表模拟序列，跨库但性能差；AUTO 由方言自动选。
     *
     * Q2: 实体为什么不能是 final 类？
     * A2: Hibernate 用 CGLIB 生成代理子类实现懒加载，final 类无法被继承。
     *
     * Q3: 实体的四种状态是什么？
     * A3: New（新建，未持久化）、Managed（受管，脏检查自动同步）、
     *     Detached（游离，脱离上下文）、Removed（待删除）。
     *     persist 让 New -> Managed；merge 让 Detached -> Managed（返回新实例）。
     *
     * Q4: 为什么不建议把实体直接返回给前端？
     * A4: 1) 泄露内部字段（如 password、内部标识）；
     *     2) 懒加载关联在序列化时触发 N+1 或 LazyInitializationException；
     *     3) 接口契约与表结构耦合，表改动直接影响前端。
     *     正确做法：转 DTO 隔离。
     *
     * Q5: ddl-auto 设什么值？
     * A5: 开发用 update（自动加列），测试用 create（每次重建），
     *     生产用 none 或 validate，DDL 由 Flyway/Liquibase 管理。
     *
     * Q6: equals/hashCode 应基于哪个字段？
     * A6: 推荐基于业务唯一键（如 username）或 id。不用可变字段，
     *     否则对象放入 HashSet 后修改字段会导致找不到。
     * =========================================================================
     */

    /*
     * =========================================================================
     * 【源码走读】Hibernate 脏检查与一级缓存（非可执行代码）
     * =========================================================================
     *
     * 一、脏检查（Dirty Checking）
     *    当实体处于 Managed 状态时，Hibernate 在事务提交前对比
     *    "当前快照"与"数据库读入时的快照"，发现差异自动生成 UPDATE。
     *    开发者不需要显式调用 save()，修改字段 + 提交事务即可落库。
     *
     *    注意：
     *    - 只在持久化上下文（Session/EntityManager）内有效
     *    - 事务外修改 Detached 实体不会触发脏检查（不会落库）
     *    - @Transactional 方法返回后，实体可能仍为 Managed，
     *      序列化时访问懒加载关联会触发额外查询
     *
     * 二、一级缓存（Persistence Context）
     *    每个 Session（事务）维护一个一级缓存（Map<id, entity>）。
     *    同一事务内多次 findById(id) 只查一次数据库，后续从缓存取。
     *    事务结束（Session 关闭）后缓存清空，实体变 Detached。
     *
     *    这意味着：
     *    - 同一事务内修改实体后再查，拿到的是修改后的内存值（不是数据库值）
     *    - 跨事务不共享一级缓存
     *    - 大事务中加载大量实体会导致一级缓存膨胀，内存压力大
     *
     * 三、二级缓存（可选）
     *    跨 Session 共享的缓存，需显式配置（如 Ehcache/Redis）。
     *    @Cache(usage=READ_WRITE) 标注实体或集合。
     *    适合读多写少的字典数据，不适合频繁变更的业务数据。
     *
     * 四、@Version 乐观锁
     *    实体加 @Version 字段，UPDATE 时自动 WHERE version = ?，
     *    版本不匹配则抛 OptimisticLockException。
     *    适合"并发更新概率低、冲突时让用户重试"的场景。
     *    对比悲观锁（SELECT ... FOR UPDATE）：乐观锁不锁行，性能好，
     *    但冲突时需要业务层处理重试。
     * =========================================================================
     */
}