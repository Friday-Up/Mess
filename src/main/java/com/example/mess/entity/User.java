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
     * 【数据安全】敏感数据保护与脱敏（非可执行代码）
     * =========================================================================
     *
     * 一、OWASP A02:2021 - 加密机制失效
     *   本类涉及的用户数据需分级保护：
     *   - 公开数据：username（可展示）
     *   - 个人数据：email（需脱敏展示，如 a***@x.com）
     *   - 敏感数据：password（必须加密存储，绝不明文）
     *   - 高敏数据：身份证/手机号（需加密存储 + 脱敏展示）
     *
     * 二、数据脱敏策略
     *   1) 展示脱敏：DTO 层做，实体层保持原值
     *      email -> a***@x.com（保留首字符和域名）
     *      phone -> 138****5678（保留前3后4）
     *   2) 日志脱敏：@Slf4j + 自定义 Logback MessageConverter
     *      日志中不输出完整敏感字段
     *   3) 数据库加密：
     *      应用层加密：JPA AttributeConverter（AES 加解密）
     *      数据库层加密：MySQL 透明数据加密（TDE）
     *      传输层加密：TLS 连接数据库（jdbc:mysql://...?useSSL=true）
     *
     * 三、字段级权限（行级/列级安全）
     *   1) 列级权限：不同角色看到不同字段（如普通用户看不到 createdAt）
     *      实现：DTO 按角色裁剪字段，或 Jackson @JsonView 分组视图
     *   2) 行级权限：用户只能看到自己的数据
     *      实现：Repository 查询自动加 WHERE user_id = currentUser
     *      框架：Spring Security + Hibernate Filter
     *
     * 四、数据留存与销毁
     *   1) 软删除：deleted 标记，数据可恢复，满足审计要求
     *   2) 硬删除：GDPR"被遗忘权"要求彻底删除用户数据
     *   3) 留存期限：日志保留 6 个月，用户数据按法规保留
     *   4) 匿名化：统计场景用匿名数据，不可逆推原始用户
     * =========================================================================
     */

    /*
     * =========================================================================
     * 【安全补充】数据合规与隐私保护（非可执行代码）
     * =========================================================================
     *
     * 一、GDPR（欧盟通用数据保护条例）
     *   1) 合法性基础：处理个人数据需有合法依据（同意/合同/合法利益）
     *   2) 数据主体权利：
     *      知情权：隐私政策告知数据用途
     *      访问权：用户可查看自己的数据（GET /api/users/me）
     *      更正权：用户可修改自己的数据（PUT /api/users/me）
     *      删除权（被遗忘权）：用户可要求彻底删除数据（DELETE /api/users/me）
     *      可携带权：用户可导出自己的数据（GET /api/users/me/export）
     *   3) 数据泄露通报：72 小时内通报监管机构
     *   4) 罚款：最高 2000 万欧元或全球营业额 4%
     *
     * 二、个人信息保护法（PIPL，中国）
     *   1) 单独同意：处理敏感个人信息（生物识别/医疗/金融/行踪）需单独同意
     *   2) 最小必要：只收集实现目的所需的最少数据
     *   3) 数据本地化：关键信息基础设施运营者收集的个人信息在境内存储
     *   4) 跨境传输：需安全评估/认证/标准合同
     *
     * 三、工程实践
     *   1) 数据分类分级：公开/内部/敏感/高敏，不同级别不同防护
     *   2) 数据最小化：DTO 只包含必要字段，不收集/不返回多余数据
     *   3) 目的限制：数据只用于收集时声明的目的
     *   4) 存储限制：超过留存期限的数据自动删除（定时任务清理）
     *   5) 隐私设计（Privacy by Design）：架构设计阶段考虑隐私，而非事后补救
     * =========================================================================
     */
}