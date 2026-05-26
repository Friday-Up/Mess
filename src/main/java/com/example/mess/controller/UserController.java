package com.example.mess.controller;

import com.example.mess.dto.ApiResponse;
import com.example.mess.dto.UserDto;
import com.example.mess.entity.User;
import com.example.mess.exception.ResourceNotFoundException;
import com.example.mess.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * 用户管理控制器
 * 提供完整的用户CRUD操作API接口
 * 
 * @RestController 组合注解，包含@Controller和@ResponseBody
 * @RequestMapping 定义基础路径为/api/users
 * @Tag Swagger文档标签，用于API分组
 * 
 * 作者: zhangyaolong.5
 * 创建时间: 2026-05-26
 * 
 * 功能说明:
 * - 获取用户列表（支持分页）
 * - 根据ID获取用户详情
 * - 根据用户名获取用户
 * - 创建新用户
 * - 更新用户信息
 * - 删除用户
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "用户管理", description = "用户管理相关API，提供完整的用户CRUD操作")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    /**
     * 用户服务层依赖
     * 用于处理用户相关的业务逻辑
     */
    private final UserService userService;

    /**
     * 构造函数注入
     * 使用构造器注入而不是字段注入，提高可测试性和不可变性
     * 
     * @param userService 用户服务实例
     */
    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 获取所有用户列表（支持分页）
     * 
     * @param page 页码，从0开始，默认为0
     * @param size 每页大小，默认为10
     * @param sort 排序字段，默认为"id"
     * @param direction 排序方向，默认为"asc"
     * @return ApiResponse<Page<User>> 分页的用户列表响应
     * 
     * HTTP方法: GET
     * 访问路径: /api/users
     * 权限要求: 需要认证（通过Spring Security配置）
     * 
     * 示例调用:
     * GET /api/users
     * GET /api/users?page=0&size=5
     * GET /api/users?page=1&size=10&sort=name&direction=desc
     * 
     * 示例响应:
     * {
     *   "success": true,
     *   "message": "获取用户列表成功",
     *   "data": {
     *     "content": [...],
     *     "totalElements": 3,
     *     "totalPages": 1,
     *     "size": 10,
     *     "number": 0
     *   }
     * }
     * 
     * 缓存说明: 此接口不使用缓存，因为用户列表可能频繁变化
     */
    @GetMapping
    @Operation(
        summary = "获取所有用户", 
        description = "返回所有用户的列表，支持分页查询"
    )
    public ApiResponse<Page<User>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction) {
        
        log.info("获取用户列表，页码: {}, 每页大小: {}, 排序字段: {}, 排序方向: {}", 
                page, size, sort, direction);
        
        // 创建分页对象，这里简化处理，实际项目中可能需要更复杂的分页逻辑
        Pageable pageable = Pageable.ofSize(size).withPage(page);
        Page<User> users = userService.getAllUsers(pageable);
        
        log.info("成功获取用户列表，总用户数: {}", users.getTotalElements());
        return ApiResponse.success("获取用户列表成功", users);
    }

    /**
     * 根据用户ID获取用户详情
     * 
     * @param id 用户ID，从路径变量中获取
     * @return ApiResponse<User> 用户详情响应
     * @throws ResourceNotFoundException 当用户不存在时抛出此异常
     * 
     * HTTP方法: GET
     * 访问路径: /api/users/{id}
     * 权限要求: 需要认证
     * 
     * 示例调用:
     * GET /api/users/1
     * 
     * 示例响应:
     * {
     *   "success": true,
     *   "message": "获取用户成功",
     *   "data": {
     *     "id": 1,
     *     "username": "admin",
     *     "email": "admin@example.com",
     *     "name": "Administrator",
     *     "createdAt": "2026-05-26T10:30:00"
     *   }
     * }
     * 
     * 缓存说明: 此接口使用Redis缓存，用户数据会被缓存以提高性能
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "根据ID获取用户", 
        description = "根据用户ID获取用户信息，使用缓存提高性能"
    )
    public ApiResponse<User> getUserById(@PathVariable Long id) {
        
        log.info("获取用户详情，用户ID: {}", id);
        
        Optional<User> user = userService.getUserById(id);
        
        // 如果用户不存在，抛出ResourceNotFoundException异常
        // 该异常会被GlobalExceptionHandler统一处理
        return user.map(u -> {
                    log.info("成功获取用户详情，用户名: {}", u.getUsername());
                    return ApiResponse.success("获取用户成功", u);
                })
                .orElseThrow(() -> {
                    log.warn("用户不存在，ID: {}", id);
                    return new ResourceNotFoundException("用户不存在，ID: " + id);
                });
    }

    /**
     * 根据用户名获取用户详情
     * 
     * @param username 用户名，从路径变量中获取
     * @return ApiResponse<User> 用户详情响应
     * @throws ResourceNotFoundException 当用户不存在时抛出此异常
     * 
     * HTTP方法: GET
     * 访问路径: /api/users/username/{username}
     * 权限要求: 需要认证
     * 
     * 示例调用:
     * GET /api/users/username/admin
     * 
     * 示例响应:
     * {
     *   "success": true,
     *   "message": "获取用户成功",
     *   "data": {
     *     "id": 1,
     *     "username": "admin",
     *     "email": "admin@example.com",
     *     "name": "Administrator",
     *     "createdAt": "2026-05-26T10:30:00"
     *   }
     * }
     * 
     * 注意: 用户名应该唯一，但此约束在数据库层面强制执行
     */
    @GetMapping("/username/{username}")
    @Operation(
        summary = "根据用户名获取用户", 
        description = "根据用户名获取用户信息"
    )
    public ApiResponse<User> getUserByUsername(@PathVariable String username) {
        
        log.info("根据用户名获取用户，用户名: {}", username);
        
        Optional<User> user = userService.getUserByUsername(username);
        
        return user.map(u -> {
                    log.info("成功获取用户，用户ID: {}", u.getId());
                    return ApiResponse.success("获取用户成功", u);
                })
                .orElseThrow(() -> {
                    log.warn("用户不存在，用户名: {}", username);
                    return new ResourceNotFoundException("用户不存在，用户名: " + username);
                });
    }

    /**
     * 创建新用户
     * 
     * @param userDto 用户数据传输对象，包含创建用户所需的信息
     * @return ApiResponse<User> 创建成功的用户响应
     * 
     * HTTP方法: POST
     * 访问路径: /api/users
     * 权限要求: 需要认证
     * 
     * 请求体示例:
     * {
     *   "username": "newuser",
     *   "email": "newuser@example.com",
     *   "name": "New User"
     * }
     * 
     * 示例响应:
     * {
     *   "success": true,
     *   "message": "创建用户成功",
     *   "data": {
     *     "id": 4,
     *     "username": "newuser",
     *     "email": "newuser@example.com",
     *     "name": "New User",
     *     "createdAt": "2026-05-26T10:35:00"
     *   }
     * }
     * 
     * 验证规则:
     * - username: 不能为空
     * - email: 不能为空，必须符合邮箱格式
     * - name: 不能为空
     * 
     * 缓存说明: 创建用户后，新用户信息会被添加到缓存中
     */
    @PostMapping
    @Operation(
        summary = "创建新用户", 
        description = "创建一个新的用户，需要验证输入数据"
    )
    public ApiResponse<User> createUser(@Valid @RequestBody UserDto userDto) {
        
        log.info("创建新用户，用户名: {}, 邮箱: {}", userDto.getUsername(), userDto.getEmail());
        
        // 从DTO创建用户实体
        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        user.setName(userDto.getName());
        
        // 调用服务层保存用户
        User createdUser = userService.createUser(user);
        
        log.info("创建新用户成功，用户ID: {}, 用户名: {}", createdUser.getId(), createdUser.getUsername());
        return ApiResponse.success("创建用户成功", createdUser);
    }

    /**
     * 更新用户信息
     * 
     * @param id 要更新的用户ID
     * @param userDto 包含更新信息的用户数据传输对象
     * @return ApiResponse<User> 更新后的用户响应
     * @throws ResourceNotFoundException 当用户不存在时抛出此异常
     * 
     * HTTP方法: PUT
     * 访问路径: /api/users/{id}
     * 权限要求: 需要认证
     * 
     * 请求体示例:
     * {
     *   "username": "updateduser",
     *   "email": "updated@example.com",
     *   "name": "Updated User"
     * }
     * 
     * 示例响应:
     * {
     *   "success": true,
     *   "message": "更新用户成功",
     *   "data": {
     *     "id": 1,
     *     "username": "updateduser",
     *     "email": "updated@example.com",
     *     "name": "Updated User",
     *     "createdAt": "2026-05-26T10:30:00"
     *   }
     * }
     * 
     * 缓存说明: 更新用户后，缓存中的用户信息会被更新
     */
    @PutMapping("/{id}")
    @Operation(
        summary = "更新用户信息", 
        description = "根据用户ID更新用户信息，支持部分更新"
    )
    public ApiResponse<User> updateUser(@PathVariable Long id, @Valid @RequestBody UserDto userDto) {
        
        log.info("更新用户信息，用户ID: {}", id);
        
        // 创建包含更新信息的用户实体
        User userDetails = new User();
        userDetails.setUsername(userDto.getUsername());
        userDetails.setEmail(userDto.getEmail());
        userDetails.setName(userDto.getName());
        
        // 调用服务层更新用户
        User updatedUser = userService.updateUser(id, userDetails);
        
        log.info("更新用户成功，用户ID: {}, 用户名: {}", updatedUser.getId(), updatedUser.getUsername());
        return ApiResponse.success("更新用户成功", updatedUser);
    }

    /**
     * 删除用户
     * 
     * @param id 要删除的用户ID
     * @return ApiResponse<Void> 删除成功的响应
     * @throws ResourceNotFoundException 当用户不存在时抛出此异常
     * 
     * HTTP方法: DELETE
     * 访问路径: /api/users/{id}
     * 权限要求: 需要认证
     * 
     * 示例调用:
     * DELETE /api/users/1
     * 
     * 示例响应:
     * {
     *   "success": true,
     *   "message": "删除用户成功",
     *   "data": null
     * }
     * 
     * 缓存说明: 删除用户后，缓存中的用户信息会被清除
     */
    @DeleteMapping("/{id}")
    @Operation(
        summary = "删除用户", 
        description = "根据用户ID删除用户，删除后无法恢复"
    )
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        
        log.info("删除用户，用户ID: {}", id);
        
        // 调用服务层删除用户
        userService.deleteUser(id);
        
        log.info("删除用户成功，用户ID: {}", id);
        return ApiResponse.<Void>success("删除用户成功", null);
    }
}