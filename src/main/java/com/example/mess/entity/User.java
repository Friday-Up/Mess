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
     * 【Spring 内幕】JPA 实体生命周期与持久化上下文的脏检查机制（非可执行代码）
     * =========================================================================
     *
     * 一、实体四种状态与转换触发点（Hibernate 6.x）
     *   1) new/transient：new User() 后，尚未与 EntityManager 关联
     *   2) managed：save()/persist() 或 findById() 后，处于持久化上下文
     *   3) detached：上下文关闭/clear/evict 后，丢失关联但保留数据
     *   4) removed：调用 remove() 后，等待 flush 时执行 DELETE
     *   本项目的 save() 实现来自 SimpleJpaRepository：
     *   - 仅当实体主键为 null 时调用 entityManager.persist（新增）
     *   - 已有主键则调用 merge -> 产生复制到托管副本的效果
     *   这就是写代码时 save 既能新增也能更新的容器层原因
     *
     * 二、脏检查与最小变更 UPDATE：不用写 UPDATE 也能更新字段
     *   事务内：
     *   1) 加载：findById -> User 进入持久化上下文，快照副本同时保存
     *   2) 业务修改：setter 赋值（本类 setEmail/setName 等）
     *   3) flush（事务提交或手动 flush 时）：
     *      Hibernate 比较当前实体与快照 -> email 变了
     *      -> 生成 UPDATE USER SET email=? WHERE id=?
     *      仅 changed 字段参与 SET（@DynamicUpdate 时，否则全字段）
     *   4) 事务提交 -> DB 生效
     *   注意：事务外的 setter 修改不会触发 update（实体已 detached）
     *
     * 三、一级缓存（PersistenceContext）与重复查询的省钱机制
     *   同一事务/上下文中：
     *   1) findById(1L) -> 查库 -> 实体与快照入上下文
     *   2) 再次 findById(1L) -> 直接命中上下文缓存，不查库
     *   3) findByUsername 不同路径（JPQL/原生 SQL）默认绕开一级缓存，
     *      会查库并按主键返回上下文中已有托管对象（保证引用同一）
     *   一级缓存生命周期 = EntityManager 生命周期 = 一个事务（通常勿跨事务共享）
     *
     * 四、flush 的隐式触发时机（最容易被忽略的边界）
     *   - 事务提交前 commit -> flush
     *   - 执行同类或带查询方法的 JPQL/SQL 查询前（auto flush）
     *   - 手动调用 EntityManager.flush()
     *   - saveAndFlush() 常见用途：立即写入数据库，便于立刻验证唯一约束
     *
     * 五、N+1 问题与本项目的预防点
     *   - 如果 User 有 @OneToMany 关联（当前没有），使用 findAll() 再遍历关联，
     *     会触发 N 条额外 SQL（即 N+1 问题）
     *   - 当前设计：本实体无关联关系（单表实体）+ DTO 只暴露需要的字段，
     *     天然规避 N+1；增加关联时记得 @Fetch(JOIN) 或 EntityGraph 优化
     *
     * 六、乐观锁与并发更新机制
     *   1) 脏检查的并发边界：两个事务同时更新同字段会丢失更新（lost update）
     *   2) @Version 乐观锁实现原理：加载时快照记录 version，更新时执行
     *      UPDATE USER SET version=version+1 WHERE id=? AND version=?，
     *      命中行数为 0 时抛 ObjectOptimisticLockingFailureException
     *   3) 悲观锁 SELECT ... FOR UPDATE 适合热点行，但持锁久有死锁风险
     *   4) 跨实例的分布式锁（Redisson）弥补乐观锁无法覆盖的场景，
     *      注意锁粒度与超时租约，避免锁泄漏卡死系统
     *   5) 项目决策点：当前 User 写入低频，最小可行解是不加锁；
     *      未来出现同时改邮箱等需求时，引入 @Version 成本最低
     *
     * 七、JPA 缓存层级与 Spring Cache 的关系
     *   1) 一级缓存：PersistenceContext，事务级，默认开启
     *   2) 二级缓存：SessionFactory 级，跨事务共享，需显式开启
     *      并标注 @org.hibernate.annotations.Cache(usage = READ_WRITE)
     *   3) Spring Cache（本项目的 users 缓存）：应用级缓存，
     *      在 Service 层生效，对 Repository 层透明，两层可叠加
     *   4) 叠加原则：一级缓存防重复 SQL、Spring Cache 防重复业务方法、
     *      二级缓存只在高并发读且实体更新少的场景启用
     * =========================================================================
     */
}