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
     * 【阅读笔记】Spring MVC 常用注解词典（非可执行代码）
     * ============================================================================
     *
     * 一、路由与绑定
     * ----------------------------------------------------------------------------
     *   @RestController = @Controller + @ResponseBody
     *       —— 组合体，所有返回值直接序列化为响应体;
     *   @RequestMapping(path, method)
     *       —— 类级限定前缀 + 方法级映射具体动作;
     *   @GetMapping / @PostMapping / @PutMapping / @DeleteMapping
     *       —— 对应 HTTP 动词的语义化写法，推荐;
     *   @PathVariable / @RequestParam / @RequestBody
     *       —— 从路径 / 查询串 / 请求体取参数;
     *   @RequestHeader / @CookieValue
     *       —— 定取 Header 或 Cookie 的值。
     *
     * 二、参数校验
     * ----------------------------------------------------------------------------
     *   @Valid 加在 @RequestBody 参数上才会触发 Hibernate Validator;
     *   @Validated 来自 Spring，支持分组校验，比 @Valid 更灵活;
     *   校验失败默认抛 MethodArgumentNotValidException，须由全局异常处理转 400。
     *
     * 三、响应定义
     * ----------------------------------------------------------------------------
     *   @ResponseStatus(HttpStatus.CREATED) —— 给方法显式指定默认状态码;
     *   ResponseEntity<T>               —— 需要自定义 Header / 状态码时携带体;
     *   HttpHeaders / MultiValueMap     —— 需要返回多个同名 Header 时使用。
     *
     * 四、RESTful 风格自检
     * ----------------------------------------------------------------------------
     *   [ ] 资源用复数名词（/users 而非 /user）
     *   [ ] 语义明确：错误时有错误体，且携带 path/timestamp
     *   [ ] 状态码语义正确（创建 201，查询 200，校验失败 400，冲突 409）
     *   [ ] 幂等性：GET/PUT/DELETE 应可安全重试，POST 需考虑主键或唯一约束
     *
     * 五、当前接口的应用示例
     * ----------------------------------------------------------------------------
     *   GET    /api/users        -> 列表查询
     *   GET    /api/users/{id}   -> 资源定位查询
     *   POST   /api/users        -> 创建请求，返回成功时 data 填充新对象
     *   DELETE /api/users/{id}   -> 删除成功，返回 data=null 的成功响应
     * ============================================================================
     */

    /*
     * ============================================================================
     * 【补充阅读】API 版本管理与接口演进（非可执行代码）
     * ============================================================================
     *
     * 一、四种常见版本方案
     * ----------------------------------------------------------------------------
     *   1) URL 路径：/api/v1/users —— 最直观，网关路由友好，推荐首选;
     *   2) 查询参数：/api/users?version=1 —— 侵入小，但缓存/路由不友好;
     *   3) Header：Accept: application/vnd.demo.v1+json —— REST 味道足，调试麻烦;
     *   4) 自定义 Header：X-Api-Version: 1 —— 简单，但跨工具兼容性一般。
     *
     * 二、演进原则
     * ----------------------------------------------------------------------------
     *   - 只加不改：新版本只增字段/增接口，不删除不改语义;
     *   - 旧版本设维护期，到期下线并提前公告;
     *   - Swagger 分组展示各版本，前端明确锁定版本号;
     *   - 破坏性变更（删字段/改类型/改语义）必须开新版本。
     *
     * 三、兼容性评审清单
     * ----------------------------------------------------------------------------
     *   [ ] 新增字段对旧客户端是否无害（可为 null、有默认值）
     *   [ ] 删除字段是否已确认无消费方
     *   [ ] 枚举新增取值旧客户端能否容错
     *   [ ] 错误码结构是否保持一致
     *   [ ] 分页格式、排序规则是否向后兼容
     * ============================================================================
     */
}