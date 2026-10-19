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
     * 【性能优化】实体与数据库交互性能（非可执行代码）
     * =========================================================================
     *
     * 一、查询性能
     *   1) 只查需要的字段：用投影接口或 DTO 构造表达式，避免 SELECT * 拉大字段
     *   2) 索引策略：username/email 加唯一索引，高频查询字段加普通索引
     *   3) 分页：用 Pageable 避免全表加载，深分页用游标（WHERE id < lastId）
     *   4) N+1 检测：开 spring.jpa.show-sql=true，查询数异常时用 JOIN FETCH
     *
     * 二、写入性能
     *   1) 批量插入：hibernate.jdbc.batch_size=50 + saveAll()，减少往返
     *   2) IDENTITY 策略会禁用批量插入的 JDBC batch（需逐条获取 ID），
     *      批量场景考虑 SEQUENCE 或雪花算法
     *   3) 只读事务：@Transactional(readOnly=true) 让 MySQL 不加行锁
     *   4) 避免大事务：事务越长锁持有越久，并发吞吐越低
     *
     * 三、连接池调优（HikariCP）
     *   - maximum-pool-size：CPU 密集型 = 核数+1，IO 密集型 = 2*核数
     *   - minimum-idle：与 maximum 相同避免动态创建
     *   - connection-timeout：30000ms（获取连接超时）
     *   - max-lifetime：1800000ms（连接最大存活，需小于数据库 wait_timeout）
     *
     * =========================================================================
     * 【监控可观测性】数据库层关键指标
     * =========================================================================
     *   - 连接池：hikaricp.connections.active/idle/pending
     *   - 查询耗时：hibernate.query.execution.time（需开启统计）
     *   - 慢查询：MySQL slow_query_log，long_query_time=1s
     *   - 事务耗时：spring.transaction.active（长事务告警）
     *   - 死锁：MySQL SHOW ENGINE INNODB STATUS 中的 LATEST DETECTED DEADLOCK
     * =========================================================================
     */

    /*
     * =========================================================================
     * 【性能优化补充】JPA 二级缓存与批量处理（非可执行代码）
     * =========================================================================
     *
     * 一、JPA 二级缓存（跨 Session 共享）
     *   一级缓存：Session 级别（事务内），自动启用
     *   二级缓存：SessionFactory 级别（跨事务），需显式配置：
     *   1) 依赖：hibernate-ehcache 或 hibernate-jcache
     *   2) 配置：spring.jpa.properties.hibernate.cache.use_second_level_cache=true
     *   3) 实体标注：@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
     *   4) 适用场景：读多写少的字典数据、配置数据
     *   5) 不适用：频繁变更的业务数据（缓存失效成本高）
     *   6) 分布式缓存：多实例部署时 EhCache 需复制，
     *      或改用 Spring Cache + Redis（更通用）
     *
     * 二、批量处理性能
     *   1) 批量插入：saveAll() + hibernate.jdbc.batch_size=50
     *      IDENTITY 策略禁用 JDBC batch（需逐条获取 ID）
     *      批量场景用 SEQUENCE（Oracle/PG）或 APPLICATION（雪花算法）
     *   2) 批量更新：@Modifying @Query("UPDATE User u SET u.status = :status")
     *      一条 SQL 更新多行，比循环逐条快 10-100 倍
     *   3) 批量删除：@Modifying @Query("DELETE FROM User u WHERE u.status = :status")
     *      注意：JPQL 批量操作不触发 JPA 生命周期回调（@PreRemove 等）
     *   4) 大批量数据迁移：分批提交（每 1000 条 flush + clear），
     *      避免持久化上下文膨胀导致 OOM
     *
     * 三、懒加载性能陷阱
     *   1) 序列化时触发：实体转 JSON 时懒加载字段被访问，触发 N+1
     *      解决：DTO 只包含需要的字段，或 @JsonIgnore 忽略关联
     *   2) 事务外访问：事务结束后访问懒加载字段，抛 LazyInitializationException
     *      解决：事务内完成所有数据访问，或 OSIV（Open Session In View）
     *   3) OSIV 陷阱：spring.jpa.open-in-view=true（默认）延长 Session 到响应结束，
     *      简化开发但掩盖 N+1 问题，生产环境建议关闭
     * =========================================================================
     */
}