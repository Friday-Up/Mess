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
     * 【ADR-002】实体与DTO分离策略
     * =========================================================================
     * 上下文：JPA 实体直接暴露给前端会泄露内部字段、触发懒加载异常、
     *         导致接口与表结构强耦合。
     * 决策：实体只做持久化映射，对外传输一律转 DTO。Service 层负责转换。
     * 替代方案：
     *   A) 实体直接返回前端 —— 简单但泄露 password 等字段，序列化懒加载报错。
     *   B) 用 @JsonIgnore 隐藏敏感字段 —— 每加一个字段都要记得加注解，易遗漏。
     *   C) 用投影接口（Projection）—— 适合只读查询，不适合创建/更新场景。
     * 后果：多一层转换代码（可用 MapStruct 自动生成），但接口契约独立于表结构，
     *       前后端可独立演进。新增实体字段不会自动暴露给前端。
     *
     * =========================================================================
     * 【代码审查要点】JPA 实体
     * =========================================================================
     * [ ] 实体不直接出现在 Controller 参数或返回值中
     * [ ] 敏感字段（password/token）不在 DTO 中出现
     * [ ] 类非 final（Hibernate CGLIB 代理需要）
     * [ ] 有无参构造器（JPA 反射需要，编译器默认提供，加有参构造后须显式补）
     * [ ] @Id 有 @GeneratedValue 策略
     * [ ] 唯一业务字段在数据库层有唯一索引（不依赖应用层 existsBy 检查）
     * [ ] equals/hashCode 基于稳定字段（id 或业务唯一键，不用可变字段）
     * [ ] 关联关系默认 LAZY（避免无谓 JOIN 和 N+1）
     * [ ] toString 不包含懒加载关联（防止序列化时触发额外查询）
     * [ ] ddl-auto 在生产环境设为 none 或 validate
     * =========================================================================
     */

    /*
     * =========================================================================
     * 【ADR-002-S】主键生成与乐观锁策略（补充）
     * =========================================================================
     * 上下文：主键生成策略影响插入性能和批量操作；并发更新需要乐观锁防止丢失更新。
     * 决策：主键用 IDENTITY（MySQL 自增），简单可靠；暂不加 @Version 乐观锁，
     *       当前并发更新场景少，后续按需添加。
     * 替代方案：
     *   A) SEQUENCE（Oracle/PG 序列）—— 可预分配，批量快，MySQL 不支持原生序列。
     *   B) UUID —— 全局唯一，无数据库依赖，但索引性能差（无序导致页分裂）。
     *   C) 雪花算法 —— 有序分布式 ID，需额外配置 Worker ID。
     * 后果：IDENTITY 简单但不能预分配，批量插入需逐条获取 ID；
     *       后续如需批量优化可切换到 SEQUENCE（换数据库时）或雪花算法。
     *
     * 乐观锁补充说明：
     *   @Version 字段（Integer/Long）在 UPDATE 时自动 WHERE version = ?，
     *   版本不匹配则抛 OptimisticLockException。
     *   适用场景：并发更新概率低、冲突时让用户重试。
     *   不适用场景：高并发写（大量冲突导致重试风暴）。
     *   当前项目用户更新频率低，暂不加乐观锁；后续如需可加 @Version 字段。
     *
     * JPA 实体四种状态速查：
     *   New（新建）—— 未 persist，不受 EntityManager 管理
     *   Managed（受管）—— 在持久化上下文中，字段变更自动同步（脏检查）
     *   Detached（游离）—— 脱离上下文，变更不自动落库
     *   Removed（待删除）—— 被标记删除，flush 后从数据库移除
     * =========================================================================
     */
}