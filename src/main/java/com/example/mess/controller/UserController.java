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
     * 【Spring 内幕】DispatcherServlet 请求处理流水线与 HandlerAdapter 机制（非可执行代码）
     * =========================================================================
     *
     * 一、一个 GET /api/users/1 请求在 DispatcherServlet.doDispatch 的流转
     *   Tomcat 接收连接 -> 构造 RequestFacade -> 匹配 Filter 链（见 SecurityConfig）
     *   -> 进入 DispatcherServlet（本质是一个 Servlet）：
     *   1) getHandler(mappedRequest)：遍历已注册的 HandlerMapping 列表
     *      - 已注册（Boot Servlet 项目共约 8 个）：
     *        RequestMappingHandlerMapping（处理 @RequestMapping）
     *        BeanNameUrlHandlerMapping、RouterFunctionMapping 等
     *      - 本类所有 @GetMapping 等注解在启动时由
     *        RequestMappingHandlerMapping.afterPropertiesSet() 扫描注册进
     *        urlLookup 表（key = /api/users/{id}, value = HandlerMethod）
     *   2) getHandlerAdapter(handler)：返回 RequestMappingHandlerAdapter
     *   3) handler.handle()：反射调用本类方法，内部先执行拦截器链 preHandle
     *   4) 参数绑定：HandlerMethodArgumentResolverComposite 决定每个参数的来源
     *      本类典型解析器：
     *      - @PathVariable Long id -> PathVariableMethodArgumentResolver
     *      - @RequestBody UserDto dto -> RequestResponseBodyMethodProcessor
     *      - Pageable pageable -> PageableHandlerMethodArgumentResolver（Spring Data）
     *      - @RequestParam String name -> RequestParamMethodArgumentResolver
     *   5) 返回值处理：HandlerMethodReturnValueHandlerComposite
     *      返回 ApiResponse<UserDto> 且有 @ResponseBody
     *      -> RequestResponseBodyMethodProcessor 接管
     *      -> MessageConverter 协商写出响应体（详见 ApiResponse 注释块）
     *   6) 异常分支：任何阶段抛异常 -> DispatcherServlet.processDispatchResult
     *      -> HandlerExceptionResolver 链（详见 GlobalExceptionHandler 注释块）
     *   7) finally：afterCompletion 回调（事务提交后、响应已写出）
     *
     * 二、HandlerMethod 与执行框架：为什么方法能被描述出来
     *   启动时 @RequestMapping 被封装成 HandlerMethod 对象：
     *   - bean（String）：bean 名称（userController）
     *   - method：java.lang.reflect.Method 引用
     *   - parameters：MethodParameter 数组，记录参数索引/嵌套层级/参数注解
     *   执行时真正调用的是 InvocableHandlerMethod.invokeForRequest()，
     *   用反射 Method.invoke(bean, argsComplete) 完成调用
     *   性能备注：Spring 对反射调用做了两层优化：
     *   - MethodParameter 缓存 ArgumentResolver 绑定结果，避免每次匹配
     *   - 可选的参数预解析（部分版本），减少 map 查找
     *
     * 三、为什么 Controller 不应该写业务逻辑（框架视角）
     *   HandlerAdapter 是一次性入口：请求线程万一阻塞在业务代码中的
     *   远程调用或锁等待，会占满 Tomcat 线程池（默认 200 线程），
     *   导致健康检查也超时（/hello 放行路径同样无响应）
     *   因此最佳实践是 Controller 只做协议转换与参数校验，
     *   同步耗时逻辑下沉到 @Async 或消息驱动处理，保持线程快速回笼
     *
     * 四、异步请求处理与线程模型
     *   1) DeferredResult<ResponseEntity>：容器线程立即释放，业务线程完成后
     *      setResult，由 DispatcherServlet 回调写出响应，适合远程调用场景
     *   2) CompletableFuture：组合多个远程调用非阻塞合并，@Async 自动桥接
     *   3) SseEmitter：一个连接多次写出，适合服务端实时推送
     *   4) WebFlux 的 Mono/Flux 需要整体切换响应式容器，与本项目 Servlet 栈不兼容
     *   5) DeferredResult.onTimeout 务必注册兜底响应，否则容器会响应空体或 503
     *   6) 同步与异步谨慎混用：同一应用内并存会让事务边界更难推理
     *
     * 五、HandlerInterceptor 的注册与执行点（MVC 拦截器原理）
     *   1) 与 Filter 的区别：Interceptor 在 DispatcherServlet 内部、
     *      只对进入 HandlerMapping 的请求生效；Filter 在 Servlet 之前、
     *      对所有请求（含静态资源）生效
     *   2) 三个回调点：
     *      - preHandle：Handler 执行前（事务开始前），可短路返回 false
     *      - postHandle：Handler 返回后、视图渲染前（异常时走 afterCompletion 而非此）
     *      - afterCompletion：视图渲染后（finally 语义），适合清理 ThreadLocal
     *   3) 注册方式：实现 WebMvcConfigurer.addInterceptors，
     *      通过 InterceptorRegistry 指定 order 与 pathPatterns
     *   4) 典型用途：登录态校验、请求日志埋点、接口耗时统计、
     *      操作审计（写审计表）、防重复提交标记
     *   5) 陷阱：preHandle 里抛异常会被全局异常处理器接到，
     *      但 postHandle 里的异常不会（已过异常解析点），
     *      故业务校验别放在 postHandle
     * =========================================================================
     */
}