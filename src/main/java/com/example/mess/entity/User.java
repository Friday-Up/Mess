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
         * 【可观测性与生产运维】实体与数据资产的可观测边界（非可执行代码）
         * =========================================================================
         *
         * 一、实体是可观测数据的"契约载体"
         *   1) 字段即数据契约：加字段/改可空性的变更要同步更新 
         *      监控、日志、文档三处——光改代码不改契约，下游观测全部失明
         *   2) 敏感字段清单：password/salt/手机号/邮箱属于 PII，
         *      日志打印 User 实体时必须走脱敏器（toString 隐藏敏感字段）——
         *      Lombok @Data 生成的 toString 会全字段输出，是隐私泄漏高发点
         *   3) createdAt/updatedAt 是审计基础：任何表的变更追溯依赖时间字段，
         *      别为省两列丢掉"数据何时被改"这一最基础观测维度
         *
         * 二、数据量级与业务健康
         *   1) 用户表行数增长率是北极星指标的底层事实：
         *      监控 user 表每日增量，与注册指标对账——不一致即埋点有 bug 或有脏数据
         *   2) 大字段的水位监控：biography 类 TEXT 字段的平均长度/最大值
         *      要监控——"一个用户写了 100MB 简介把表撑爆"是真实事故模式
         *   3) 数据一致性巡检：定期跑"孤儿记录/缺失关联"的 SQL 巡检任务，
         *      结果入指标——脏数据的修复成本随时间指数增长，巡检是最便宜的保险
         *
         * 三、实体的变更追踪与可回溯
         *   1) 关键实体的变更历史：重要字段（状态/权限/余额）的变更
         *      落审计表（旧值/新值/操作人/时间/traceId）——可回溯是
         *      线上事故定责的最基本证据链
         *   2) 软删除优于物理删除：deleted_flag 让"误删"可恢复，
         *      也让"数据消失"类故障可观测——物理删除让事故无现场
         *   3) @Version 乐观锁的冲突计数入指标：冲突率突增说明
         *      并发写竞争加剧，是容量规划的前瞻信号，而不是仅当普通错误处理
         =========================================================================
         */
}