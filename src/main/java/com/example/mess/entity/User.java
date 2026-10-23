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
     * 【Java 并发】实体可变状态的线程安全与 JMM 可见性（非可执行代码）
     * =========================================================================
     *
     * 一、本实体在运行时的线程分布
     *   1) 本实体是"事务内临时对象"的典型：加载数据后委托给当前请求线程，
     *      事务结束随持久化上下文清理，通常不跨线程共享
     *   2) 一旦脱离事务（如推送消息、异步任务、缓存存对象引用），
     *      实体对象会被多个线程同时看到，setUsername 与 getUsername 可能并发：
     *      写线程 set 后，读线程下一行可能仍读到旧值（可见性问题的实体版）
     *   3) 实体字段为 String/Long/LocalDateTime，单个引用赋值是原子的，
     *      但"用户名变更 + 邮箱变更"的复合写并非原子，读方可能看到
     *      新用户名 + 旧邮箱的组合 -> 业务语义的中间态
     *
     * 二、synchronized 与 Lock 的适用场景对比
     *   1) synchronized 方法/块：JVM 内建，获取与释放由编译器保证，
     *      异常时自动释放，简单够用；不可中断、不可超时、非公平
     *   2) ReentrantLock：可中断（lockInterruptibly）、可超时（tryLock）、
     *      可公平（FairSync）、可与 Condition 配合精细唤醒；必须 finally unlock
     *   3) 本实体相关的场景：若要在内存中维护"用户变更榜单"这种共享结构，
     *      高竞争时 ReentrantLock 性能更平稳；一般竞争 synchronized 即可
     *   4) 误区：给 setter 加 synchronized 并不能解决可见性问题以外的
     *      语义原子性 -> 外部调用方仍是"先 set 再 get"，需要把复合操作
     *      封装到一个同步方法里（单一同步点），才真正解决问题
     *
     * 三、volatile 与实体的对齐
     *   1) volatile 保证可见性与单变量读写顺序，不保证复合原子性：
     *      private volatile boolean dirty; 适合做"标记位"（停机、脏标记）
     *   2) 若本实体作为共享的缓存对象（如"用户在线状态表"），
     *      字段加 volatile 可避免读线程缓存旧值，但仍有复合中间态问题
     *   3) 更稳的做法：把可变实体按业务场景改造为不可变 DTO 再共享
     *      （copy-on-write），写入方换整个对象引用，读取方拿到一致的快照
     *   4) 实体与 DTO 的分界在此点体现价值：实体留在事务线程内（可变），
     *      DTO 泛滥并发场景（不可变），见 UserDto 注释块
     *
     * 四、Long、String、时间类型的并发友好性
     *   1) String：不可变，多线程只读安全；唯一的坑是 interned/substring
     *      内容大时内存放大（老 JDK），现代 JDK substring 拷贝很安全
     *   2) Long：对象引用赋值原子（32 位 JVM 上 long/double 例外），
     *      一般无需 volatile；但 Long 的 hashCode 缓存是惰性写入的可变状态，
     *      重复计算安全（幂等），不构成风险
     *   3) LocalDateTime：不可变，内部状态固定，天然线程安全；
     *      老代码常见 Date 是可变且非线程安全，跨线程传递 Date 必须防御拷贝
     *   4) 集合字段是最大隐患：List/Set/Map 在实体里做值对象时，
     *      应构造时定死并返回不可变视图（List.copyOf / unmodifiableList）
     *
     * 五、对象发布与逸出的最小规则
     *   1) 不要在构造器中启动线程并传 this（this 逸出，半成品对象被读到）
     *   2) 不要在构造器中注册回调并共享 this（同上，改为 init 方法或工厂后注册）
     *   3) 单例 Bean 持有实体集合：集合本身用并发容器，条目仍可能被并发 set
     *      -> 选型：要么不可变条目 + 并发容器，要么整体加同步
     *   4) 日志打印实体时：对象被其他线程改动会出现"打印时的状态不是
     *      逻辑时刻的状态"，测试报告里偶现的诡异值往往源于此
     * =========================================================================
     */
}