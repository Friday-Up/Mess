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
     * ============================================================================
     * 【设计文档】User 持久化实体设计说明（补充文档，非可执行代码）
     * ============================================================================
     *
     * 一、职责定位
     * ----------------------------------------------------------------------------
     * User 是与数据库 USER 表一一对应的 JPA 持久化实体，处于「数据模型层」，
     * 承载领域数据的存储结构与映射规则。它只描述"数据长什么样、如何落库"，
     * 不承担业务逻辑（业务在 UserService，对外传输用 UserDto）。
     *
     * 二、字段与列映射
     * ----------------------------------------------------------------------------
     *   id        —— @Id + @GeneratedValue(IDENTITY)，数据库自增主键;
     *   username  —— @Column(unique=true)，唯一约束，自动建立唯一索引;
     *   email     —— @Column(unique=true)，唯一约束，自动建立唯一索引;
     *   name      —— 普通列，允许为空，无唯一约束;
     *   createdAt —— 由数据库默认值 CURRENT_TIMESTAMP 填充，语义上只读。
     *
     * 三、关键设计决策
     * ----------------------------------------------------------------------------
     * 决策 1：主键采用 IDENTITY 自增策略
     *   理由：实现简单、单表自增性能好；如需分布式唯一可换 SEQUENCE 或雪花算法。
     * 决策 2：username/email 施加数据库级唯一约束
     *   理由：即使 Service 层校验被绕过，数据库仍是唯一性的最后防线。
     * 决策 3：createdAt 由数据库填充而非应用层 new LocalDateTime
     *   理由：以数据库时钟为准，避免多实例应用时钟漂移导致的时间不一致。
     * 决策 4：实体不含密码等敏感字段的对外暴露
     *   理由：对外一律走 UserDto，实体即便新增敏感字段也不会泄漏到 API。
     *
     * 四、与 UserDto 的映射关系
     * ----------------------------------------------------------------------------
     *   Entity → DTO：UserService#convertToDto，剔除敏感/内部字段;
     *   DTO → Entity：UserService#convertToEntity，刻意忽略 id 与 createdAt;
     *   映射的单一职责集中在 Service，实体本身保持"纯数据"。
     *
     * 五、生命周期与状态
     * ----------------------------------------------------------------------------
     *   瞬态(Transient)：new User() 后未保存，id 为 null;
     *   持久(Persistent)：save 后纳入持久化上下文，字段变更自动脏检查同步;
     *   游离(Detached)：事务结束后脱离上下文，需重新 merge 才能继续跟踪。
     *
     * 六、扩展指南
     * ----------------------------------------------------------------------------
     *   - 新增审计字段：可引入 @CreatedDate/@LastModifiedDate + JPA Auditing;
     *   - 引入乐观锁：添加 @Version 字段防止并发覆盖更新;
     *   - 软删除：新增 deleted 标记并配合 @Where 过滤;
     *   - 建立关联：如"用户-角色"多对多，使用 @ManyToMany 并注意懒加载策略。
     *
     * 七、常见坑位
     * ----------------------------------------------------------------------------
     *   ! 直接把实体返回给前端会暴露持久化细节，务必转 DTO;
     *   ! toString/equals 若纳入懒加载关联字段，可能触发 N+1 或延迟加载异常;
     *   ! 在事务外修改持久实体不会自动落库，需注意事务边界。
     * ============================================================================
     */

    /*
     * ============================================================================
     * 【补充文档】User 字段级说明与索引建议（非可执行代码）
     * ============================================================================
     *
     * 一、字段语义
     * ----------------------------------------------------------------------------
     *   id       ：主键，IDENTITY 策略由数据库自增生成，插入前为 null;
     *   username ：登录名/展示名，业务上要求唯一，建议加唯一索引;
     *   email    ：邮箱，业务上要求唯一，建议加唯一索引，格式校验在 DTO 层做;
     *   （如后续新增 createdAt/updatedAt 可用 @CreationTimestamp/@UpdateTimestamp
     *     或 JPA Auditing 自动维护。）
     *
     * 二、索引与约束建议
     * ----------------------------------------------------------------------------
     *   - username、email 加唯一索引，既保证业务唯一性又加速等值查询;
     *   - 高频查询字段（如状态、创建时间）按查询模式补普通索引;
     *   - 唯一约束是并发唯一性的最终保障，应用层预检查只作优化不作依赖。
     *
     * 三、映射与演进注意
     * ----------------------------------------------------------------------------
     *   - 新增字段应评估是否需要数据库迁移脚本（Flyway/Liquibase）而非仅改实体;
     *   - 关联关系（如 @OneToMany/@ManyToOne）需谨慎选择加载策略，默认避免 EAGER;
     *   - 大文本字段用 @Lob，避免污染常规查询的行大小与缓存。
     *
     * 四、FAQ
     * ----------------------------------------------------------------------------
     *   Q: 为什么实体不放校验注解而放在 DTO？
     *   A: 实体聚焦持久化映射；输入校验属于接口层职责，放 DTO 更内聚且可复用。
     *
     *   Q: equals/hashCode 应基于哪个字段？
     *   A: 推荐基于业务唯一键（如 username）或数据库生成后的 id，
     *      避免使用可变字段导致集合行为异常。
     * ============================================================================
     */
}