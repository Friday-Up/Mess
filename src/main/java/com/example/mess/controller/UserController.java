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
     * 【Java 并发】控制层并发画像：从请求线程到响应线程的全链路（非可执行代码）
     * =========================================================================
     *
     * 一、一次 GET /api/users/1 牵涉到哪些线程
     *   1) Tomcat 工作线程（Thread-1）：全程贯穿
     *      HandlerMapping 匹配 -> HandlerAdapter 调用本类方法 -> Service ->
     *      Repository -> JdbcTemplate/Hibernate -> DB IO（阻塞同步等待）
     *   2) HikariCP 连接池的借用线程：工作线程从池里 acquire 连接，
     *      借用期间工作线程阻塞（不是 Hikari 的线程在执行业务 SQL），
     *      连接池线程只负责建连接与清理空闲连接
     *   3) MySQL 驱动的 IO 线程：请求发出后工作线程在 socket 上 read 阻塞，
     *      直到驱动收完结果集才被唤醒
     *   4) 结论：串行链路把 200 个工作线程按"下游延迟"成倍放大：
     *      DB 平均 200ms，每线程 5 QPS，200 线程理论上限 1000 QPS
     *      （任何一层 RT 抖动都会让工作线程成批阻塞，崩点是超时无响应）
     *
     * 二、控制层共享状态的边界
     *   1) 本类只装配了 Service 与 DTO 转换的入参，全部无状态 -> 安全
     *   2) 常见越界行为与后果：
     *      - 在方法外 new SimpleDateFmt 作为字段：SimpleDateFmt 非线程安全，
     *        格式化结果错乱甚至抛异常；改用 DateTimeFormatter（不可变）
     *      - 用 ArrayList 做入参收集并缓存到字段：可见性与覆盖问题
     *   3) 请求级缓存可用 @Scope(WebApplicationContext.SCOPE_REQUEST) 的 Bean，
     *      Spring 为每个请求新建实例，结束即销毁，无thread 竞争
     *   4) 会话级数据用 @Scope(SCOPE_SESSION)；跨请求共享才落到单例 +
     *      并发容器或显式同步
     *
     * 三、ThreadLocal 在请求链路中的真实落点
     *   1) Spring 的事务管理：TransactionSynchronizationManager 用多个
     *      ThreadLocal（connectionHolder/isReadOnly/name）绑定数据库连接
     *   2) Security 上下文：SecurityContextHolderStrategy 的 ThreadLocal
     *      携带 Authentication（见 SecurityConfig 注释块）
     *   3) MDC 链路追踪：Logback 的 MDC Map 是 ThreadLocal，基于
     *      traceId 串联日志（本类 logにも自动注入？需配置 layout）
     *   4) 三者都可能因线程复用泄漏：
     *      - 事务：TransactionInterceptor 在 finally 里清理，安全
     *      - Security：SecurityContextHolderFilter 请求结束清理，安全
     *      - MDC：需要自己在 Filter/Interceptor 里 clear，任何一处漏掉
     *        就会在压测中看到"上一次请求的 traceId 跑到下次"，经典脏数据
     *
     * 四、控制层压测与并发的自检清单
     *   1) 并发线程数应 <= maxThreads+acceptCount，否则压测先压 Tomcat 排队
     *      （排队带来的 RT 上升会掩盖业务瓶颈）
     *   2) 观察工作线程池水位：jvisualvm 或 /actuator/metrics/tomcat.threads.busy
     *      busy 接近 max 时先看下游（DB/第三方）耗时，而不是加线程
     *   3) 切换点：CPU 飙高但 DB 慢查询少，通常是序列化/正则/大对象拷贝，
     *      异步化并不能救，先做 CPU profile（async-profiler 生成火焰图）
     *   4) 接口幂等性：并发重试会触发两次创建 -> 详见 UserService 唯一性
     *      检查与数据库唯一约束的双保险设计
     *   5) 超时兜底：Tomcat 的 connectionTimeout 与下游客户端超时应
     *      层层递减（Tomcat > Service > Remote Client），形成漏斗
     * =========================================================================
     */
}