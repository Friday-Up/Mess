package com.example.mess.dto;

import java.time.LocalDateTime;

/**
 * 用户数据传输对象（DTO）
 * 
 * 用于在Controller、Service、Repository之间传输用户数据，
 * 避免直接暴露实体类内部结构。只包含对外暴露的字段，不含密码等敏感信息。
 * 转换逻辑在UserService的convertToDto/convertToEntity方法中完成。
 * 
 * @see com.example.mess.entity.User 对应的实体类
 * @see com.example.mess.service.UserService 包含DTO转换逻辑
 */
public class UserDto {
    
    /** 用户唯一标识，由数据库自动生成 */
    private Long id;
    
    /** 用户名，用于登录认证，具有唯一性约束，1-50字符 */
    private String username;
    
    /** 电子邮箱，用于通信通知，具有唯一性约束，1-100字符 */
    private String email;
    
    /** 用户真实姓名，用于显示，无唯一性约束，1-100字符 */
    private String name;
    
    /** 用户创建时间，由数据库自动设置，只读字段 */
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