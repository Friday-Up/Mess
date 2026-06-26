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
 * 用户控制器 - 提供用户管理RESTful API
 * 
 * 基础路径: /api/users，所有接口需要认证。
 * 
 * RESTful端点:
 * - GET    /api/users      → 分页获取用户列表
 * - GET    /api/users/{id} → 获取用户详情
 * - POST   /api/users      → 创建用户
 * - PUT    /api/users/{id} → 更新用户
 * - DELETE /api/users/{id} → 删除用户
 * 
 * 分页参数: page(页码从0开始), size(每页大小), sort(排序)
 * 
 * @see com.example.mess.service.UserService
 * @see com.example.mess.dto.UserDto
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "用户管理", description = "用户相关的API接口")
public class UserController {

    @Autowired
    private UserService userService;

    /** 获取用户列表（分页），支持page、size、sort参数 */
    @GetMapping
    @Operation(summary = "获取用户列表", description = "获取所有用户的分页列表")
    public ApiResponse<Page<UserDto>> getAllUsers(Pageable pageable) {
        return ApiResponse.success(userService.getAllUsers(pageable));
    }

    /** 根据ID获取用户详情，不存在时返回404 */
    @GetMapping("/{id}")
    @Operation(summary = "获取用户详情", description = "根据用户ID获取用户详细信息")
    public ApiResponse<UserDto> getUserById(@PathVariable Long id) {
        return ApiResponse.success(userService.getUserById(id));
    }

    /** 创建新用户，username和email需唯一，id和createdAt由系统生成 */
    @PostMapping
    @Operation(summary = "创建用户", description = "创建新的用户")
    public ApiResponse<UserDto> createUser(@RequestBody UserDto userDto) {
        return ApiResponse.success(userService.createUser(userDto));
    }

    /** 更新用户信息（全量更新），id和createdAt不可修改 */
    @PutMapping("/{id}")
    @Operation(summary = "更新用户", description = "根据用户ID更新用户信息")
    public ApiResponse<UserDto> updateUser(@PathVariable Long id, @RequestBody UserDto userDto) {
        return ApiResponse.success(userService.updateUser(id, userDto));
    }

    /** 删除用户（物理删除，不可恢复），不存在时返回404 */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户", description = "根据用户ID删除用户")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ApiResponse.success(null);
    }
}