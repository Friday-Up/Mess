package com.example.mess.dto;

import java.time.LocalDateTime;

/**
 * 用户数据传输对象（Data Transfer Object）
 * 
 * 用于在不同层（Controller、Service、Repository）之间传输用户数据，
 * 避免直接暴露实体类（Entity）的内部结构，实现数据隔离和安全控制。
 * 
 * 设计原则:
 * - DTO模式：将实体类与外部接口解耦，保护领域模型
 * - 最小化暴露：只包含必要的字段，不暴露敏感信息（如密码）
 * - 单向传输：从Service层向Controller层传递数据
 * 
 * 与User实体的区别:
 * - User实体：映射数据库表，包含所有字段（包括内部字段）
 * - UserDto：只包含需要对外暴露的字段
 * - 转换过程在UserService中完成（convertToDto/convertToEntity方法）
 * 
 * 字段说明:
 * - id: 用户唯一标识，由数据库自动生成
 * - username: 用户名，用于登录和标识，具有唯一性约束
 * - email: 电子邮箱，用于通信和通知，具有唯一性约束
 * - name: 用户真实姓名，用于显示和个性化
 * - createdAt: 用户创建时间，由数据库自动设置
 * 
 * 扩展建议:
 * - 如需分页信息，可继承PageDto基类
 * - 如需验证，可添加JSR-303验证注解（@NotBlank, @Email等）
 * - 如需版本控制，可添加version字段
 * 
 * 作者: zhangyaolong.5
 * 创建时间: 2026-05-26
 * 
 * @see com.example.mess.entity.User 对应的实体类
 * @see com.example.mess.service.UserService 包含DTO转换逻辑
 */
public class UserDto {
    
    /**
     * 用户唯一标识
     * 
     * 由数据库在插入记录时自动生成（自增主键）
     * 在创建新用户时为null，由数据库分配
     * 在更新和查询时必须有值
     * 
     * 使用场景:
     * - 作为API路径参数（如 /api/users/{id}）
     * - 作为查询条件
     * - 作为缓存键的一部分
     */
    private Long id;
    
    /**
     * 用户名
     * 
     * 用于登录认证和用户标识
     * 具有唯一性约束，不能与已有用户名重复
     * 长度限制：1-50个字符
     * 
     * 使用场景:
     * - 用户登录
     * - 用户搜索
     * - 用户名唯一性校验
     */
    private String username;
    
    /**
     * 电子邮箱
     * 
     * 用于通信、通知和密码找回
     * 具有唯一性约束，不能与已有邮箱重复
     * 格式要求：符合标准邮箱格式（如 user@example.com）
     * 长度限制：1-100个字符
     * 
     * 使用场景:
     * - 邮件通知
     * - 密码找回
     * - 邮箱唯一性校验
     */
    private String email;
    
    /**
     * 用户真实姓名
     * 
     * 用于显示和个性化设置
     * 无唯一性约束，不同用户可以有相同的姓名
     * 长度限制：1-100个字符
     * 
     * 使用场景:
     * - 用户界面显示
     * - 个性化问候
     * - 用户信息展示
     */
    private String name;
    
    /**
     * 用户创建时间
     * 
     * 记录用户账号的创建时间
     * 由数据库在插入记录时自动设置（CURRENT_TIMESTAMP）
     * 不可修改，只读字段
     * 
     * 使用场景:
     * - 用户注册时间统计
     * - 用户活跃度分析
     * - 数据排序和筛选
     */
    private LocalDateTime createdAt;

    /**
     * 获取用户ID
     * 
     * @return Long 用户唯一标识，新建用户时可能为null
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
     * @return LocalDateTime 创建时间，新建用户时可能为null
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