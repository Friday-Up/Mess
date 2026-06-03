package com.example.mess.controller;

import com.example.mess.dto.ApiResponse;
import com.example.mess.dto.UserDto;
import com.example.mess.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 * 提供用户管理相关的RESTful API接口
 * 
 * @RestController 组合注解，包含@Controller和@ResponseBody
 * @RequestMapping 定义基础路径为/api/users
 * @Tag Swagger文档标签，用于API分组
 * 
 * 作者: zhangyaolong.5
 * 创建时间: 2026-05-26
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "用户管理", description = "用户相关的API接口")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 获取所有用户列表
     * 
     * @param pageable 分页参数
     * @return ApiResponse<Page<UserDto>> 用户分页数据
     * 
     * HTTP方法: GET
     * 访问路径: /api/users
     * 权限要求: 需要认证
     */
    @GetMapping
    @Operation(summary = "获取用户列表", description = "获取所有用户的分页列表")
    public ApiResponse<Page<UserDto>> getAllUsers(Pageable pageable) {
        Page<UserDto> users = userService.getAllUsers(pageable);
        return ApiResponse.success(users);
    }

    /**
     * 根据ID获取用户详情
     * 
     * @param id 用户ID
     * @return ApiResponse<UserDto> 用户详情
     * 
     * HTTP方法: GET
     * 访问路径: /api/users/{id}
     * 权限要求: 需要认证
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取用户详情", description = "根据用户ID获取用户详细信息")
    public ApiResponse<UserDto> getUserById(@PathVariable Long id) {
        UserDto user = userService.getUserById(id);
        return ApiResponse.success(user);
    }

    /**
     * 创建新用户
     * 
     * @param userDto 用户信息
     * @return ApiResponse<UserDto> 创建的用户信息
     * 
     * HTTP方法: POST
     * 访问路径: /api/users
     * 权限要求: 需要认证
     */
    @PostMapping
    @Operation(summary = "创建用户", description = "创建新的用户")
    public ApiResponse<UserDto> createUser(@RequestBody UserDto userDto) {
        UserDto createdUser = userService.createUser(userDto);
        return ApiResponse.success(createdUser);
    }

    /**
     * 更新用户信息
     * 
     * @param id 用户ID
     * @param userDto 更新的用户信息
     * @return ApiResponse<UserDto> 更新后的用户信息
     * 
     * HTTP方法: PUT
     * 访问路径: /api/users/{id}
     * 权限要求: 需要认证
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新用户", description = "根据用户ID更新用户信息")
    public ApiResponse<UserDto> updateUser(@PathVariable Long id, @RequestBody UserDto userDto) {
        UserDto updatedUser = userService.updateUser(id, userDto);
        return ApiResponse.success(updatedUser);
    }

    /**
     * 删除用户
     * 
     * @param id 用户ID
     * @return ApiResponse<Void> 删除结果
     * 
     * HTTP方法: DELETE
     * 访问路径: /api/users/{id}
     * 权限要求: 需要认证
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户", description = "根据用户ID删除用户")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ApiResponse.success(null);
    }
}