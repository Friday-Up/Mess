package com.example.mess.dto;

import java.time.LocalDateTime;

/**
 * 用户数据传输对象
 * 用于在不同层之间传输用户数据
 * 
 * 作者: zhangyaolong.5
 * 创建时间: 2026-05-26
 */
public class UserDto {
    
    private Long id;
    private String username;
    private String email;
    private String name;
    private LocalDateTime createdAt;

    // Getter和Setter方法
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