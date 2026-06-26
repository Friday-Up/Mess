package com.example.mess.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 用户实体类 - 映射到数据库USER表
 * 
 * 使用JPA注解实现对象关系映射。与UserDto分离，通过DTO对外暴露数据，保护内部结构。
 * 主键策略：IDENTITY（数据库自增）。
 * 
 * @see com.example.mess.dto.UserDto 对应的数据传输对象
 * @see com.example.mess.repository.UserRepository 数据访问接口
 */
@Entity
@Table(name = "USER")
public class User {
    
    /** 主键，使用IDENTITY策略由数据库自增生成 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /** 用户名，非空且唯一，用于登录认证，最大50字符 */
    @Column(unique = true, nullable = false)
    private String username;
    
    /** 电子邮箱，非空且唯一，用于通信通知，最大100字符 */
    @Column(unique = true, nullable = false)
    private String email;
    
    /** 用户真实姓名，可为空，最大100字符 */
    private String name;
    
    /** 创建时间，数据库列名created_at，默认CURRENT_TIMESTAMP */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}