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
     * 【技术债务】TD-002 实体层
     * =========================================================================
     *
     * TD-002-1: 缺少 updatedAt 字段
     *   现状：只有 createdAt，无法追踪最后修改时间
     *   影响：审计和排障时无法知道记录何时被修改
     *   优先级：P2
     *   修复方案：加 updatedAt 字段 + @PreUpdate 回调自动维护
     *   预估工时：0.5d
     *
     * TD-002-2: 缺少乐观锁
     *   现状：并发更新时后写覆盖先写，无检测
     *   影响：管理后台同时编辑同一用户时数据丢失
     *   优先级：P3（当前并发量低）
     *   修复方案：加 @Version 字段，更新时 WHERE version = ?
     *   预估工时：0.5d
     *
     * TD-002-3: 缺少软删除
     *   现状：删除是物理删除（DELETE），数据不可恢复
     *   影响：误删无法恢复，审计缺失
     *   优先级：P3
     *   修复方案：加 deleted 字段 + @Where(clause="deleted=false") + Repository 方法调整
     *   预估工时：1d
     *
     * TD-002-4: 无 JPA Auditing
     *   现状：createdAt 手动赋值，updatedAt 缺失
     *   影响：每个创建逻辑都要手动设时间，易遗漏
     *   优先级：P2
     *   修复方案：启用 @CreatedDate/@LastModifiedDate + @EntityListeners(AuditingEntityListener)
     *   预估工时：0.5d
     *
     * =========================================================================
     * 【重构路线图】实体层演进方向
     * =========================================================================
     * Phase 1（当前）：基本字段 + 手动时间赋值 + 物理删除
     * Phase 2：加 updatedAt + JPA Auditing + 乐观锁
     * Phase 3：软删除 + 审计字段（createdBy/updatedBy）+ 领域事件
     * =========================================================================
     */

    /*
     * =========================================================================
     * 【技术债务】TD-002-S 实体层补充
     * =========================================================================
     *
     * TD-002-5: 缺少状态字段
     *   现状：无用户状态（启用/禁用）
     *   影响：无法禁用用户而不删除数据
     *   优先级：P2
     *   修复方案：加 status 字段（ENABLED/DISABLED），查询时过滤
     *   预估工时：0.5d
     *
     * TD-002-6: 缺少角色/权限字段
     *   现状：无用户角色，Security 配置硬编码
     *   影响：无法动态分配权限
     *   优先级：P2
     *   修复方案：加 roles 字段（多对多关系或 JSON 数组），Security 动态鉴权
     *   预估工时：2d
     *
     * TD-002-7: 密码字段缺失
     *   现状：User 实体无 password 字段
     *   影响：无法存储认证凭证
     *   优先级：P1
     *   修复方案：加 password 字段 + BCrypt 加密存储，DTO 中排除
     *   预估工时：1d
     * =========================================================================
     */
}