package com.example.mess.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 用户实体类
 * 
 * 映射到数据库中的USER表，是系统核心领域模型之一。
 * 使用JPA（Java Persistence API）注解实现对象关系映射（ORM）。
 * 
 * 设计原则:
 * - 实体类与DTO分离：通过UserDto对外暴露数据，保护内部结构
 * - 字段约束：使用JPA注解定义数据库约束，确保数据完整性
 * - 自增主键：使用IDENTITY策略，由数据库自动分配ID
 * 
 * JPA注解说明:
 * - @Entity: 标识这是一个JPA实体类，映射到数据库表
 * - @Table: 指定映射的数据库表名和约束
 * - @Id: 标识主键字段
 * - @GeneratedValue: 指定主键生成策略
 * - @Column: 指定字段的数据库映射属性
 * 
 * 数据库表结构:
 * USER (
 *   ID BIGINT AUTO_INCREMENT PRIMARY KEY,
 *   USERNAME VARCHAR(50) NOT NULL UNIQUE,
 *   EMAIL VARCHAR(100) NOT NULL UNIQUE,
 *   NAME VARCHAR(100),
 *   CREATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP
 * )
 * 
 * 主键生成策略:
 * - GenerationType.IDENTITY: 使用数据库自增主键
 * - 适用于MySQL、PostgreSQL、H2等支持自增的数据库
 * - 不适用于Oracle（需使用SEQUENCE策略）
 * 
 * 字段约束:
 * - username: 非空且唯一，用于登录认证
 * - email: 非空且唯一，用于通信和通知
 * - name: 可为空，用于显示真实姓名
 * - createdAt: 自动设置，记录创建时间
 * 
 * 扩展建议:
 * - 添加密码字段（需加密存储）
 * - 添加角色字段（实现RBAC权限控制）
 * - 添加状态字段（启用/禁用）
 * - 添加更新时间字段（记录最后修改时间）
 * - 添加软删除标记（逻辑删除）
 * 
 * 作者: zhangyaolong.5
 * 创建时间: 2026-05-26
 * 
 * @see com.example.mess.dto.UserDto 对应的数据传输对象
 * @see com.example.mess.repository.UserRepository 数据访问接口
 */
@Entity
@Table(name = "USER")
public class User {
    
    /**
     * 用户唯一标识（主键）
     * 
     * 使用IDENTITY策略自动生成，由数据库在INSERT时分配
     * 新创建的User对象id为null，持久化后由数据库填充
     * 
     * 主键特性:
     * - 非空：数据库自动保证
     * - 唯一：数据库自动保证
     * - 不可变：一旦分配不可修改
     * 
     * 注意事项:
     * - 不要手动设置id值，应让数据库自动生成
     * - 在事务提交后才能获取生成的id
     * - 删除后重新插入会分配新的id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 用户名
     * 
     * 用于登录认证和用户唯一标识
     * 数据库约束：非空（NOT NULL）且唯一（UNIQUE）
     * 最大长度：50个字符
     * 
     * 业务规则:
     * - 注册时必须提供
     * - 不可与已有用户名重复
     * - 区分大小写（取决于数据库排序规则）
     * - 建议只允许字母、数字和下划线
     * 
     * 安全考虑:
     * - 不应包含特殊字符（防止SQL注入）
     * - 建议最小长度限制（如3个字符）
     * - 应进行唯一性校验后再保存
     */
    @Column(unique = true, nullable = false)
    private String username;
    
    /**
     * 电子邮箱
     * 
     * 用于通信、通知和密码找回
     * 数据库约束：非空（NOT NULL）且唯一（UNIQUE）
     * 最大长度：100个字符
     * 
     * 业务规则:
     * - 注册时必须提供
     * - 不可与已有邮箱重复
     * - 应符合标准邮箱格式
     * - 不区分大小写（建议存储前转小写）
     * 
     * 安全考虑:
     * - 应验证邮箱格式
     * - 应发送验证邮件确认
     * - 可用于密码找回流程
     */
    @Column(unique = true, nullable = false)
    private String email;
    
    /**
     * 用户真实姓名
     * 
     * 用于显示和个性化设置
     * 数据库约束：可为空（NULL），无唯一性约束
     * 最大长度：100个字符
     * 
     * 业务规则:
     * - 注册时可选提供
     * - 可与已有姓名重复
     * - 可随时修改
     * - 可包含中文、空格等字符
     */
    private String name;
    
    /**
     * 用户创建时间
     * 
     * 记录用户账号的创建时间
     * 数据库列名：created_at
     * 默认值：CURRENT_TIMESTAMP（当前时间戳）
     * 
     * 特性:
     * - 由数据库自动设置，应用层通常不修改
     * - 精度到秒级别
     * - 用于审计和统计
     * 
     * 使用场景:
     * - 用户注册时间统计
     * - 按注册时间排序
     * - 用户活跃度分析
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * 获取用户ID
     * 
     * @return Long 用户唯一标识，新创建的对象可能返回null
     */
    public Long getId() {
        return id;
    }

    /**
     * 设置用户ID
     * 
     * 注意：通常由数据库自动生成，不建议手动设置
     * 仅在数据迁移或特殊场景下使用
     * 
     * @param id 用户唯一标识
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 获取用户名
     * 
     * @return String 用户名，不会返回null
     */
    public String getUsername() {
        return username;
    }

    /**
     * 设置用户名
     * 
     * @param username 用户名，不能为null或空字符串
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * 获取电子邮箱
     * 
     * @return String 电子邮箱，不会返回null
     */
    public String getEmail() {
        return email;
    }

    /**
     * 设置电子邮箱
     * 
     * @param email 电子邮箱，应符合标准邮箱格式
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * 获取用户真实姓名
     * 
     * @return String 用户真实姓名，可能为null
     */
    public String getName() {
        return name;
    }

    /**
     * 设置用户真实姓名
     * 
     * @param name 用户真实姓名
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取用户创建时间
     * 
     * @return LocalDateTime 创建时间，新创建的对象可能返回null
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * 设置用户创建时间
     * 
     * 注意：通常由数据库自动设置，不建议手动修改
     * 仅在数据迁移或特殊场景下使用
     * 
     * @param createdAt 创建时间
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}