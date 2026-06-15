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
 * 用户控制器 - 提供用户管理相关的RESTful API接口
 * 
 * 实现用户的CRUD（创建、读取、更新、删除）操作，
 * 遵循RESTful API设计规范，使用统一的响应格式。
 * 
 * 核心注解说明:
 * 
 * @RestController:
 *   标识为RESTful控制器，方法返回值自动序列化为JSON
 * 
 * @RequestMapping("/api/users"):
 *   定义基础路径为/api/users
 *   所有方法的路径都以此为前缀
 *   符合RESTful资源命名规范：
 *   - 使用复数名词（users）
 *   - 嵌套在/api路径下，区分API和页面请求
 * 
 * @Tag(name = "用户管理", description = "..."):
 *   Swagger/OpenAPI文档分组标签
 *   在Swagger UI中将此控制器的API归为"用户管理"组
 * 
 * RESTful API设计规范:
 * - GET    /api/users      - 获取用户列表（幂等）
 * - GET    /api/users/{id} - 获取单个用户（幂等）
 * - POST   /api/users      - 创建新用户（非幂等）
 * - PUT    /api/users/{id} - 更新用户（幂等）
 * - DELETE /api/users/{id} - 删除用户（幂等）
 * 
 * 权限配置:
 * - 所有用户管理API需要认证后访问
 * - 在SecurityConfig中配置: anyRequest().authenticated()
 * - 认证方式: 表单登录（user/password 或 admin/admin）
 * 
 * 分页支持:
 * - 使用Spring Data的Pageable参数自动解析分页请求
 * - 请求示例: GET /api/users?page=0&size=10&sort=name,asc
 * - page: 页码（从0开始）
 * - size: 每页大小
 * - sort: 排序字段和方向
 * 
 * 异常处理:
 * - 资源不存在时抛出ResourceNotFoundException
 * - 由GlobalExceptionHandler统一处理，返回404状态码
 * 
 * 作者: zhangyaolong.5
 * 创建时间: 2026-05-26
 * 
 * @see com.example.mess.service.UserService 用户服务层
 * @see com.example.mess.dto.UserDto 用户数据传输对象
 * @see com.example.mess.dto.ApiResponse 统一响应格式
 * @see com.example.mess.config.SecurityConfig 安全配置
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "用户管理", description = "用户相关的API接口")
public class UserController {

    /**
     * 用户服务层依赖
     * 
     * 通过@Autowired自动注入，提供用户相关的业务逻辑处理。
     * Controller层只负责请求转发和响应封装，
     * 业务逻辑由Service层处理。
     * 
     * 依赖注入方式:
     * - @Autowired字段注入（当前使用，简化演示）
     * - 推荐使用构造器注入（更好的可测试性和不可变性）
     */
    @Autowired
    private UserService userService;

    /**
     * 获取所有用户列表（分页）
     * 
     * 返回分页的用户数据，支持通过请求参数控制分页和排序。
     * 
     * 请求示例:
     * - GET /api/users                          -> 默认分页（第0页，20条/页）
     * - GET /api/users?page=0&size=10           -> 第0页，每页10条
     * - GET /api/users?page=1&size=5&sort=name  -> 第1页，每页5条，按name排序
     * 
     * 响应格式:
     * {
     *   "success": true,
     *   "data": {
     *     "content": [...],       // 当前页数据
     *     "totalElements": 3,     // 总记录数
     *     "totalPages": 1,        // 总页数
     *     "number": 0,            // 当前页码
     *     "size": 20,             // 每页大小
     *     "first": true,          // 是否首页
     *     "last": true            // 是否末页
     *   }
     * }
     * 
     * @param pageable 分页参数，Spring自动从请求参数解析
     *                 包含page（页码）、size（大小）、sort（排序）
     * @return ApiResponse<Page<UserDto>> 包含分页用户数据的统一响应
     * 
     * HTTP方法: GET
     * 访问路径: /api/users
     * 权限要求: 需要认证
     */
    @GetMapping
    @Operation(summary = "获取用户列表", description = "获取所有用户的分页列表")
    public ApiResponse<Page<UserDto>> getAllUsers(Pageable pageable) {
        // 调用Service层获取分页数据
        Page<UserDto> users = userService.getAllUsers(pageable);
        // 使用ApiResponse包装返回结果
        return ApiResponse.success(users);
    }

    /**
     * 根据ID获取用户详情
     * 
     * 根据用户ID查询单个用户的详细信息。
     * 如果用户不存在，Service层会抛出ResourceNotFoundException，
     * 由GlobalExceptionHandler处理并返回404状态码。
     * 
     * 请求示例:
     * - GET /api/users/1   -> 获取ID为1的用户
     * - GET /api/users/999 -> 用户不存在，返回404
     * 
     * 响应格式（成功）:
     * {
     *   "success": true,
     *   "data": {
     *     "id": 1,
     *     "username": "admin",
     *     "email": "admin@example.com",
     *     "name": "Administrator",
     *     "createdAt": "2026-05-26T10:30:00"
     *   }
     * }
     * 
     * 响应格式（失败）:
     * {
     *   "success": false,
     *   "message": "用户不存在"
     * }
     * 
     * @param id 用户ID，从URL路径中提取
     * @return ApiResponse<UserDto> 包含用户详情的统一响应
     * 
     * HTTP方法: GET
     * 访问路径: /api/users/{id}
     * 权限要求: 需要认证
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取用户详情", description = "根据用户ID获取用户详细信息")
    public ApiResponse<UserDto> getUserById(@PathVariable Long id) {
        // 调用Service层根据ID查询用户
        UserDto user = userService.getUserById(id);
        // 使用ApiResponse包装返回结果
        return ApiResponse.success(user);
    }

    /**
     * 创建新用户
     * 
     * 接收JSON格式的用户数据，创建新的用户记录。
     * 请求体中的用户数据通过@RequestBody注解自动反序列化为UserDto。
     * 
     * 请求示例:
     * POST /api/users
     * Content-Type: application/json
     * {
     *   "username": "newuser",
     *   "email": "newuser@example.com",
     *   "name": "New User"
     * }
     * 
     * 注意事项:
     * - username和email必须唯一，重复会返回500错误
     * - id和createdAt由系统自动生成，无需传入
     * - 当前未添加参数验证，建议添加@Valid注解
     * 
     * @param userDto 用户信息，从请求体JSON自动反序列化
     * @return ApiResponse<UserDto> 包含创建后用户数据的统一响应（含自动生成的ID）
     * 
     * HTTP方法: POST
     * 访问路径: /api/users
     * 权限要求: 需要认证
     */
    @PostMapping
    @Operation(summary = "创建用户", description = "创建新的用户")
    public ApiResponse<UserDto> createUser(@RequestBody UserDto userDto) {
        // 调用Service层创建用户
        UserDto createdUser = userService.createUser(userDto);
        // 返回创建后的用户数据（包含自动生成的ID和createdAt）
        return ApiResponse.success(createdUser);
    }

    /**
     * 更新用户信息
     * 
     * 根据用户ID更新用户信息，只更新请求体中包含的字段。
     * 如果用户不存在，Service层会抛出ResourceNotFoundException。
     * 
     * 请求示例:
     * PUT /api/users/1
     * Content-Type: application/json
     * {
     *   "username": "updateduser",
     *   "email": "updated@example.com",
     *   "name": "Updated User"
     * }
     * 
     * 注意事项:
     * - PUT是全量更新，所有字段都会被覆盖
     * - 如需部分更新，建议使用PATCH方法
     * - id和createdAt不可通过此接口修改
     * 
     * @param id 用户ID，从URL路径中提取
     * @param userDto 更新的用户信息，从请求体JSON反序列化
     * @return ApiResponse<UserDto> 包含更新后用户数据的统一响应
     * 
     * HTTP方法: PUT
     * 访问路径: /api/users/{id}
     * 权限要求: 需要认证
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新用户", description = "根据用户ID更新用户信息")
    public ApiResponse<UserDto> updateUser(@PathVariable Long id, @RequestBody UserDto userDto) {
        // 调用Service层更新用户信息
        UserDto updatedUser = userService.updateUser(id, userDto);
        // 返回更新后的用户数据
        return ApiResponse.success(updatedUser);
    }

    /**
     * 删除用户
     * 
     * 根据用户ID删除用户记录，此操作不可逆。
     * 如果用户不存在，Service层会抛出ResourceNotFoundException。
     * 
     * 请求示例:
     * DELETE /api/users/1   -> 删除ID为1的用户
     * DELETE /api/users/999 -> 用户不存在，返回404
     * 
     * 注意事项:
     * - 物理删除：直接从数据库中删除记录
     * - 不可恢复：删除后数据无法恢复
     * - 生产环境建议使用软删除（逻辑删除）
     * - 删除操作会清除该用户的缓存
     * 
     * @param id 用户ID，从URL路径中提取
     * @return ApiResponse<Void> 成功时data为null的统一响应
     * 
     * HTTP方法: DELETE
     * 访问路径: /api/users/{id}
     * 权限要求: 需要认证
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户", description = "根据用户ID删除用户")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        // 调用Service层删除用户
        userService.deleteUser(id);
        // 返回成功响应，data为null
        return ApiResponse.success(null);
    }
}