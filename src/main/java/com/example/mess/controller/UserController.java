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
     * =========================================================================
     * 【面试问答】关于 Controller 与 RESTful 的常见面试题（非可执行代码）
     * =========================================================================
     *
     * Q1: @Controller 和 @RestController 的区别？
     * A1: @RestController = @Controller + @ResponseBody。
     *     前者返回视图名（HTML 页面），后者返回值直接序列化为 JSON。
     *
     * Q2: @PathVariable 和 @RequestParam 的区别？
     * A2: @PathVariable 从 URL 路径中取值（/users/{id} -> id）；
     *     @RequestParam 从查询串中取值（/users?name=alice -> name）。
     *
     * Q3: @Valid 加在哪里？不生效怎么办？
     * A3: 加在 @RequestBody 参数前。不生效检查：
     *     1) 参数前确实有 @Valid 注解；
     *     2) DTO 字段上有校验注解（@NotBlank 等）；
     *     3) 全局异常处理器捕获了 MethodArgumentNotValidException。
     *
     * Q4: RESTful 怎么设计 URL？
     * A4: 资源用名词复数（/api/users），动作用 HTTP 方法表达。
     *     GET=/api/users（列表）GET=/api/users/{id}（详情）
     *     POST=/api/users（创建）PUT=/api/users/{id}（全量更新）
     *     DELETE=/api/users/{id}（删除）
     *
     * Q5: POST 和 PUT 的幂等性区别？
     * A5: PUT 幂等（多次调用结果一致），POST 不幂等（重复提交创建多条）。
     *     防重复提交：前端按钮禁用 + 后端幂等键/唯一约束。
     *
     * Q6: 404 和 403 怎么选？
     * A6: 资源不存在 -> 404；存在但无权访问 -> 403。
     *     安全注意：敏感资源不应对无权用户暴露"存在"（403 泄露存在性），
     *     可考虑统一返回 404。
     * =========================================================================
     */

    /*
     * =========================================================================
     * 【源码走读】Spring MVC 请求处理流程（非可执行代码）
     * =========================================================================
     *
     * 一、一次 HTTP 请求的旅程
     *    1) Tomcat 接收请求，交给 DispatcherServlet
     *    2) DispatcherServlet 查 HandlerMapping 找到匹配的 Controller 方法
     *    3) HandlerAdapter 调用 Controller 方法：
     *       a) 参数解析：@RequestBody -> HttpMessageConverter 反序列化
     *       b) 校验：@Valid -> Hibernate Validator
     *       c) 执行方法
     *       d) 返回值处理：@ResponseBody -> 序列化为 JSON
     *    4) 如果有异常，交给 HandlerExceptionResolver（@ExceptionHandler）
     *    5) Response 返回客户端
     *
     * 二、@ResponseBody 的作用
     *    标注后，返回值不走视图解析器（ViewResolver），
     *    而是交给 HttpMessageConverter（如 MappingJackson2HttpMessageConverter）
     *    序列化为 JSON 写入响应体。@RestController 已隐含 @ResponseBody。
     *
     * 三、@Valid 的触发时机
     *    在参数解析阶段，HttpMessageConverter 反序列化完请求体后，
     *    RequestResponseBodyMethodProcessor 检查参数上是否有 @Valid，
     *    有则调用 validator.validate()，失败抛 MethodArgumentNotValidException。
     *
     * 四、全局异常处理器的接入点
     *    ExceptionHandlerExceptionResolver 遍历所有 @ControllerAdvice 类，
     *    找到匹配异常类型的 @ExceptionHandler 方法执行。
     *    匹配逻辑：异常类型继承距离最近的优先。
     *
     * 五、拦截器 vs 过滤器
     *    Filter（过滤器）：Servlet 容器层，在 DispatcherServlet 前后执行，
     *      可改请求/响应对象，Spring MVC 之外也生效。
     *    Interceptor（拦截器）：Spring MVC 层，在 Handler 执行前后执行，
     *      可访问 HandlerMethod，但拿不到原始请求体（已被读取）。
     *    Security 过滤器链是 Filter 层，@ControllerAdvice 是 MVC 层。
     * =========================================================================
     */
}