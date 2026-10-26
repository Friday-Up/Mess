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
     * 【HTTP/REST】实体作为 REST 资源的字段契约与暴露控制（非可执行代码）
     * =========================================================================
     *
     * 一、实体 ≠ API 资源：暴露控制的红线
     *   1) 本实体字段：id / username / email / name / createdAt
     *   2) 直接暴露实体的风险（本项目已规避）：
     *      a) 框架元数据泄漏：JPA 实体带代理状态、版本字段，序列化会带上
     *      b) 关联关系放大：@OneToMany 懒加载触发 N+1 与 LazyInitializationException
     *      c) 内部字段暴露：password/salt 这类不该出 API 的字段被误序列化
     *      d) 修改 DB 结构直接影响 API 契约——耦合面过大
     *   3) UserDto 充当"契约守门员"，本项目这一层设计是正解
     *   4) 反模式警示：用 @JsonIgnore 挡字段是"在实体上打补丁"，
     *      长期看会积累一堆"这字段给这个接口看，那个接口不看"的注解债

     * 二、字段命名的 REST 规范
     *   1) JSON 字段 camelCase（id / username / createdAt）——本项目合规
     *   2) 别 snake_case：与 Java 字段映射需 @JsonProperty("created_at")，
     *      增加映射层复杂度，除非有强外部规范要求
     *   3) 时间格式：ISO 8601（2026-10-23T14:30:00 或带时区 Z）
     *      Java 侧 LocalDateTime + Jackson 默认即输出 ISO——本项目天然合规
     *   4) 布尔命名：is+Adj（isAdmin）而非 active 标志位的"裸 bool"——
     *      "是不是"关系比"是否激活"更直接表达 REST 语义
     *   5) 字段名稳定性：发布后不可改名，只可加新字段；旧字段废弃用
     *      文档 + 两个版本并行过渡（可加 @Deprecated 提示）

     * 三、id 作为 URI 标识符的 REST 语义
     *   1) GET /api/users/{id} 的 id 是"资源身份"，不是自增数字的泄密：
     *      本项目暴露自增 id 的风险清单：
     *      a) 业务量泄露：竞品可用 id 差值推算注册量（增长速度情报）
     *      b) 枚举攻击：可通过遍历 id 拉取所有用户（本项目由 Security 保护）
     *      c) 业务关联性：id 出现在日志/链路追踪里可能成为 PII 关联点
     *   2) 实务解法：
     *      小规模：保留自增 + 权限校验（本项目当前）
     *      高安全：换 UUID 作为对外 id，内部 Long 关联
     *      双 id 策略：数据库自增（内部）+ 公开 slug（外部可读 URL）
     *   3) 路径参数 vs query：标识符（id）走 path，筛选条件（name=x）走 query
     *   4) 软删除：DELETE 后真删 vs 字段 deletedAt——后者保留可追溯性，
     *      REST 上看：删除后再 GET 同 id 应返回 404（需在查询过滤）

     * 四、createdAt 字段的契约语义
     *   1) 只读字段：客户端发的 createdAt 应被忽略（本 DTO 已有此约束）
     *   2) 时区语义：DB 存 UTC，API 输出带时区或 UTC——跨区域部署必修
     *   3) Last-Modified 联动：该字段可作为 HTTP Last-Modified 头来源，
     *      支持 If-Modified-Since 条件请求（304 减量传输）
     *   4) 与 updatedAt 区别：当前只有 createdAt；当业务需要"变更审计"
     *      时 add updatedAt + @PreUpdate 自动更新——REST 契约新增字段即可

     * 五、批量操作的 REST 设计（未来扩展参考）
     *   1) 批量读：GET /api/users?ids=1,2,3 一次性返回多个资源
     *   2) 批量创建：POST /api/users/batch body 为数组——事务边界争议大
     *      （部分成功如何报错？推荐"全成功或全失败"+ 207 Multi-Status 表达）
     *   3) 批量更新：PATCH /api/users body 为 [{id, patch}] 数组
     *   4) 批量删除：DELETE /api/users?ids=1,2,3 —— 幂等可重试
     *   5) 大批量异步：返回 202 Accepted + Location 指向任务资源
     =========================================================================
     */
}