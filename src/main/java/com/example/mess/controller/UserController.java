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
 * 用户控制器 - 提供用户管理RESTful API，遵循REST架构风格。
 * 
 * <p>控制器职责:
 * <ul>
 *   <li>接收HTTP请求，参数校验和绑定</li>
 *   <li>调用Service层业务逻辑</li>
 *   <li>返回标准化的ApiResponse响应</li>
 *   <li>不包含业务逻辑，保持控制器薄层</li>
 * </ul>
 * 
 * <p>RESTful端点设计:
 * <table border="1">
 *   <tr><th>HTTP方法</th><th>路径</th><th>说明</th><th>请求体</th><th>响应体</th></tr>
 *   <tr><td>GET</td><td>/api/users</td><td>分页获取用户列表</td><td>-</td><td>Page&lt;UserDto&gt;</td></tr>
 *   <tr><td>GET</td><td>/api/users/{id}</td><td>获取用户详情</td><td>-</td><td>UserDto</td></tr>
 *   <tr><td>POST</td><td>/api/users</td><td>创建新用户</td><td>UserDto</td><td>UserDto</td></tr>
 *   <tr><td>PUT</td><td>/api/users/{id}</td><td>全量更新用户</td><td>UserDto</td><td>UserDto</td></tr>
 *   <tr><td>DELETE</td><td>/api/users/{id}</td><td>删除用户</td><td>-</td><td>Void</td></tr>
 * </table>
 * 
 * <p>分页参数说明:
 * <ul>
 *   <li><b>page</b> - 页码从0开始，默认0（第一页）</li>
 *   <li><b>size</b> - 每页大小，默认20</li>
 *   <li><b>sort</b> - 排序字段，格式: property,asc|desc（如: username,asc）</li>
 * </ul>
 * 
 * <p>安全策略:
 * <ul>
 *   <li>所有接口需要认证（SecurityConfig中配置）</li>
 *   <li>基础路径: /api/users</li>
 *   <li>使用@RestController确保所有响应为JSON格式</li>
 * </ul>
 * 
 * <p>异常处理:
 * <ul>
 *   <li>资源不存在 → 404 Not Found（由GlobalExceptionHandler处理）</li>
 *   <li>参数校验失败 → 400 Bad Request（Spring自动处理）</li>
 *   <li>服务器错误 → 500 Internal Server Error</li>
 * </ul>
 * 
 * @see com.example.mess.service.UserService 用户业务逻辑服务
 * @see com.example.mess.dto.UserDto 用户数据传输对象
 * @see com.example.mess.config.SecurityConfig 安全配置
 * @since 1.0
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "用户管理", description = "用户相关的API接口")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 获取用户列表（分页）。
     * <p>支持page、size、sort参数，返回分页的用户列表。
     * 使用Spring Data的Pageable自动解析分页参数。
     * 
     * <p>请求示例: GET /api/users?page=0&size=10&sort=username,asc
     * 
     * @param pageable 分页参数，由Spring自动从请求参数构建
     * @return 包含分页用户列表的ApiResponse
     */
    @GetMapping
    @Operation(summary = "获取用户列表", description = "获取所有用户的分页列表")
    public ApiResponse<Page<UserDto>> getAllUsers(Pageable pageable) {
        // 调用Service层获取分页用户数据，Pageable由Spring从请求参数(page/size/sort)自动构建
        // 使用ApiResponse.success包装结果，统一返回格式便于前端解析
        return ApiResponse.success(userService.getAllUsers(pageable));
    }

    /**
     * 根据ID获取用户详情。
     * <p>通过路径变量获取用户ID，查询数据库返回用户详情。
     * 用户不存在时由Service层抛出ResourceNotFoundException，返回404。
     * 
     * <p>请求示例: GET /api/users/1
     * 
     * @param id 用户ID，从URL路径中提取
     * @return 包含用户详情的ApiResponse
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取用户详情", description = "根据用户ID获取用户详细信息")
    public ApiResponse<UserDto> getUserById(@PathVariable Long id) {
        // @PathVariable从URL路径(/api/users/{id})中提取id
        // 委托Service查询用户详情，若不存在Service会抛出异常由全局处理器返回404
        return ApiResponse.success(userService.getUserById(id));
    }

    /**
     * 创建新用户。
     * <p>接收UserDto JSON请求体，创建新用户记录。
     * username和email需唯一（由数据库唯一约束保证），id和createdAt由系统自动生成。
     * 
     * <p>请求示例: POST /api/users
     * <pre>{@code
     * {
     *   "username": "newuser",
     *   "email": "newuser@example.com",
     *   "name": "New User"
     * }
     * }</pre>
     * 
     * @param userDto 用户数据，从请求体JSON反序列化
     * @return 包含创建成功的用户信息的ApiResponse
     */
    @PostMapping
    @Operation(summary = "创建用户", description = "创建新的用户")
    public ApiResponse<UserDto> createUser(@RequestBody UserDto userDto) {
        // @RequestBody将请求体JSON反序列化为UserDto对象
        // 委托Service完成持久化，返回包含自动生成id和createdAt的用户信息
        return ApiResponse.success(userService.createUser(userDto));
    }

    /**
     * 更新用户信息（全量更新）。
     * <p>根据ID更新用户所有可修改字段。id和createdAt不可修改（由系统管理）。
     * 用户不存在时返回404。使用PUT方法符合REST全量更新语义。
     * 
     * <p>请求示例: PUT /api/users/1
     * <pre>{@code
     * {
     *   "username": "updateduser",
     *   "email": "updated@example.com",
     *   "name": "Updated User"
     * }
     * }</pre>
     * 
     * @param id 用户ID，从URL路径中提取
     * @param userDto 用户更新数据，从请求体JSON反序列化
     * @return 包含更新后用户信息的ApiResponse
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新用户", description = "根据用户ID更新用户信息")
    public ApiResponse<UserDto> updateUser(@PathVariable Long id, @RequestBody UserDto userDto) {
        // 同时接收路径变量id（定位待更新用户）和请求体userDto（新的字段值）
        // 委托Service执行全量更新，返回更新后的用户信息
        return ApiResponse.success(userService.updateUser(id, userDto));
    }

    /**
     * 删除用户（物理删除，不可恢复）。
     * <p>根据ID从数据库中永久删除用户记录。用户不存在时返回404。
     * 注意：这是物理删除，删除后数据不可恢复。生产环境建议使用软删除（逻辑删除）。
     * 
     * <p>请求示例: DELETE /api/users/1
     * 
     * @param id 用户ID，从URL路径中提取
     * @return 包含空数据的成功响应（Void类型）
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户", description = "根据用户ID删除用户")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        // 委托Service删除指定用户，无返回数据
        userService.deleteUser(id);
        // 删除成功返回不含数据的成功响应（data为null），符合REST删除操作语义
        return ApiResponse.success(null);
    }

    /*
     * ============================================================================
     * 【设计文档】UserController RESTful 接口契约与设计说明（补充文档，非可执行代码）
     * ============================================================================
     *
     * 一、层次定位与职责
     * ----------------------------------------------------------------------------
     * UserController 属于「表现层 / Web 层」，是 HTTP 世界与业务世界的边界适配器。
     * 其职责被严格限定为：
     *   1) 协议适配：解析 HTTP请求（路径变量、查询参数、请求体），产出 HTTP 响应；
     *   2) 参数绑定与基础校验：借助 @Valid 触发 Bean Validation；
     *   3) 结果包装：统一用 ApiResponse<T> 包裹返回体，保证响应结构一致；
     *   4) 委派业务：将真正的业务处理下沉到 UserService，自身不含业务规则。
     * 反之，事务、唯一性校验、对象转换等均不应出现在本类。
     *
     * 二、RESTful 路由契约
     * ----------------------------------------------------------------------------
     *   HTTP 方法   路径                 语义              成功状态   典型失败
     *   GET        /api/users           分页查询用户列表    200        —
     *   GET        /api/users/{id}      查询单个用户        200        404(不存在)
     *   POST       /api/users           创建用户           200/201    400(重复/校验)
     *   DELETE     /api/users/{id}      删除用户           200        404(不存在)
     *
     * 三、统一响应结构
     * ----------------------------------------------------------------------------
     *   所有接口返回 ApiResponse<T>，形如：
     *     { "code": 200, "message": "success", "data": {...} }
     *   删除等无返回体的操作 data 置为 null，仍保留统一外壳，
     *   便于前端以固定结构解析，无需为每个接口定制反序列化逻辑。
     *
     * 四、关键设计决策
     * ----------------------------------------------------------------------------
     * 决策 1：Controller 只返回 ApiResponse，不直接返回实体或 ResponseEntity
     *   理由：统一响应契约降低前端心智负担；异常场景交由 GlobalExceptionHandler
     *        统一兜底，Controller 内无需散落 try-catch。
     *
     * 决策 2：分页参数通过 Pageable 自动绑定
     *   理由：复用 Spring Data 的 page/size/sort 约定，避免手写分页参数解析。
     *
     * 决策 3：DELETE 成功返回 200 而非 204
     *   理由：本项目约定所有响应统一走 ApiResponse 外壳，204 无响应体与该约定冲突，
     *        故选择 200 + data:null 的折中方案。
     *
     * 五、典型调用示例（curl）
     * ----------------------------------------------------------------------------
     *   # 分页查询
     *   curl 'http://localhost:8080/api/users?page=0&size=10'
     *   # 查询单个
     *   curl 'http://localhost:8080/api/users/1'
     *   # 创建
     *   curl -X POST 'http://localhost:8080/api/users' \
     *        -H 'Content-Type: application/json' \
     *        -d '{"username":"alice","email":"a@x.com","name":"Alice"}'
     *   # 删除
     *   curl -X DELETE 'http://localhost:8080/api/users/1'
     *
     * 六、安全与跨域说明
     * ----------------------------------------------------------------------------
     *   - CSRF：SecurityConfig 中已禁用（无状态 REST API + Token 场景无需 CSRF）；
     *   - 认证：受 SecurityConfig 的过滤链保护，具体放行规则见该类文档；
     *   - 跨域：如需前端跨域访问，应在 SecurityConfig 或 WebMvcConfigurer 统一配置 CORS。
     *
     * 七、扩展指南
     * ----------------------------------------------------------------------------
     *   - 新增「更新用户」：补 PUT /api/users/{id}，委派 userService.updateUser；
     *   - 需要字段级校验：在 UserDto 字段上添加 @NotBlank/@Email 等注解并配合 @Valid；
     *   - 需要接口文档：引入 springdoc-openapi，注解自动生成 Swagger UI。
     * ============================================================================
     */

    /*
     * ============================================================================
     * 【补充文档】UserController 错误码对照与联调 FAQ（非可执行代码）
     * ============================================================================
     *
     * 一、HTTP 状态码对照
     * ----------------------------------------------------------------------------
     *   200 OK               ：查询/操作成功，data 携带结果;
     *   201 Created          ：创建成功（如需严格 REST 语义可返回 201 + Location）;
     *   400 Bad Request      ：参数校验失败（@Valid 不通过、格式错误）;
     *   401 Unauthorized     ：未认证或凭证无效;
     *   403 Forbidden        ：已认证但无权限;
     *   404 Not Found        ：资源不存在（ResourceNotFoundException）;
     *   409 Conflict         ：唯一约束冲突（用户名/邮箱重复）;
     *   500 Internal Error   ：服务端未预期异常，不透出堆栈。
     *
     * 二、联调常见问题
     * ----------------------------------------------------------------------------
     *   Q: POST 请求返回 403 但已带 Token？
     *   A: 若启用了 CSRF，需携带 CSRF Token；本项目安全配置已禁用 CSRF，
     *      若仍 403 请检查权限规则与请求头 Content-Type 是否为 application/json。
     *
     *   Q: 请求体字段名对不上导致值为 null？
     *   A: 确认 JSON 字段名与 DTO 属性一致（或用 @JsonProperty 映射），
     *      并确保请求头 Content-Type: application/json。
     *
     *   Q: 中文乱码？
     *   A: 统一使用 UTF-8；确认客户端与服务端编码一致，必要时配置
     *      HttpMessageConverter 的默认字符集。
     *
     * 三、curl 快速自测
     * ----------------------------------------------------------------------------
     *   查询列表：curl -s http://localhost:8080/api/users
     *   查询单个：curl -s http://localhost:8080/api/users/1
     *   创建用户：curl -s -X POST http://localhost:8080/api/users \
     *              -H "Content-Type: application/json" \
     *              -d '{"username":"alice","email":"a@x.com"}'
     * ============================================================================
     */
}