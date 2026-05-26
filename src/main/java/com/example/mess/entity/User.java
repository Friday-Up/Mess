package com.example.mess.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;

/**
 * 用户实体类
 * 用于映射数据库中的用户表
 * 
 * @Entity 表示这是一个JPA实体类
 * @Id 表示这是主键字段
 * @GeneratedValue 指定主键生成策略为自增
 * 
 * 作者: zhangyaolong.5
 * 创建时间: 2026-05-26
 */
@Entity
public class User {

    /**
     * 用户ID - 主键
     * 使用IDENTITY策略，由数据库自动生成
     * 类型: Long
     * 是否可为空: 否
     * 示例值: 1, 2, 3...
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 用户名 - 用于登录和显示
     * 应该是唯一的，但在实体层不做约束，在业务层验证
     * 类型: String
     * 最大长度: 255字符
     * 是否可为空: 否
     * 示例值: "admin", "john_doe", "jane_smith"
     */
    private String username;

    /**
     * 邮箱地址 - 用户的联系邮箱
     * 用于通知和密码找回等功能
     * 类型: String
     * 最大长度: 255字符
     * 是否可为空: 否
     * 示例值: "user@example.com"
     */
    private String email;

    /**
     * 真实姓名 - 用户的显示名称
     * 用于友好的用户界面显示
     * 类型: String
     * 最大长度: 255字符
     * 是否可为空: 否
     * 示例值: "张三", "John Doe", "Jane Smith"
     */
    private String name;

    /**
     * 创建时间 - 记录用户创建的时间戳
     * 默认值为当前时间
     * 类型: LocalDateTime
     * 是否可为空: 否
     * 示例值: "2026-05-26T10:30:00"
     */
    private LocalDateTime createdAt;

    /**
     * 默认构造函数
     * 创建新用户时自动设置创建时间为当前时间
     * 
     * 使用场景:
     * - 通过JPA创建新用户时
     * - 通过API创建用户时
     * - 单元测试中创建测试数据时
     */
    public User() {
        this.createdAt = LocalDateTime.now();
    }

    /**
     * 带参数的构造函数
     * 用于快速创建用户对象
     * 
     * @param username 用户名，不能为null或空
     * @param email 邮箱地址，必须符合邮箱格式
     * @param name 真实姓名，不能为null或空
     * 
     * 使用场景:
     * - 在Service层创建新用户时
     * - 在Controller层处理用户创建请求时
     * - 在测试类中创建测试数据时
     * 
     * 示例:
     * User user = new User("john_doe", "john@example.com", "John Doe");
     */
    public User(String username, String email, String name) {
        this.username = username;
        this.email = email;
        this.name = name;
        this.createdAt = LocalDateTime.now();
    }

    // ==================== Getter和Setter方法 ====================

    /**
     * 获取用户ID
     * @return 用户ID，可能为null（对于新创建的对象）
     */
    public Long getId() {
        return id;
    }

    /**
     * 设置用户ID
     * @param id 用户ID，通常由JPA在持久化时设置
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 获取用户名
     * @return 用户名，不能为空
     */
    public String getUsername() {
        return username;
    }

    /**
     * 设置用户名
     * @param username 用户名，不能为空或null
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * 获取邮箱地址
     * @return 邮箱地址，不能为空
     */
    public String getEmail() {
        return email;
    }

    /**
     * 设置邮箱地址
     * @param email 邮箱地址，必须符合邮箱格式
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * 获取真实姓名
     * @return 真实姓名，不能为空
     */
    public String getName() {
        return name;
    }

    /**
     * 设置真实姓名
     * @param name 真实姓名，不能为空或null
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取创建时间
     * @return 创建时间，永远不会为null
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * 设置创建时间
     * @param createdAt 创建时间，通常不需要手动设置
     * 
     * 注意: 这个方法通常只在特殊情况下使用，
     * 比如数据迁移或测试时设置特定的时间
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * 重写toString方法，用于日志和调试
     * @return 用户对象的字符串表示
     */
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}