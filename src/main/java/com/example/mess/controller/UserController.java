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
     * 【性能优化】接口性能优化清单（非可执行代码）
     * =========================================================================
     *
     * 一、响应速度优化
     *   1) 分页：列表接口必须分页，Pageable + Page<T>
     *   2) 字段裁剪：只返回前端需要的字段，用投影或 DTO
     *   3) 压缩：server.compression.enabled=true，Gzip 压缩 JSON
     *   4) HTTP 缓存：Cache-Control + ETag，静态数据 304 免传输
     *   5) CDN：静态资源走 CDN，API 走源站
     *
     * 二、并发处理优化
     *   1) 线程池：server.tomcat.threads.max=200（默认 200），IO 密集型可调高
     *   2) 异步接口：@Async + CompletableFuture 返回，耗时操作不阻塞
     *   3) 响应式：WebFlux 替代 MVC，背压控制，高并发场景吞吐更高
     *   4) 限流：Bucket4j 按 IP/用户限流，防止恶意刷接口
     *
     * 三、请求处理优化
     *   1) 参数校验前置：@Valid 在 Controller 层拦截，避免无效请求进 Service
     *   2) 幂等设计：POST 接口加幂等键，防止重复提交
     *   3) 批量接口：/api/users/batch 一次处理多条，减少 HTTP 往返
     *   4) 请求合并：Hystrix Collapser 或自研合并器，短时间多次请求合并
     *
     * 四、超时与熔断
     *   1) 连接超时：server.connection-timeout=5s
     *   2) 读超时：下游调用设置 read-timeout，避免线程阻塞
     *   3) 熔断：Resilience4j CircuitBreaker，错误率 >50% 熔断 10s
     *   4) 降级：熔断时返回兜底数据，保证核心功能可用
     *
     * =========================================================================
     * 【监控可观测性】接口层关键指标
     * =========================================================================
     *   - QPS：http.server.requests{uri=/api/users} 的 count/second
     *   - P99 延迟：http.server.requests 的 percentile，>1s 告警
     *   - 错误率：5xx/total，>1% 告警；4xx/total 关注但不告警
     *   - Tomcat 线程：server.tomcat.threads.busy/current，busy 接近 max 告警
     *   - 请求队列：tomcat.threads.busy 持续高位说明线程不够或下游慢
     *   接入：Micrometer + Prometheus + Grafana，Alertmanager 告警
     * =========================================================================
     */

    /*
     * =========================================================================
     * 【性能优化补充】Tomcat 与线程池调优（非可执行代码）
     * =========================================================================
     *
     * 一、Tomcat 线程模型
     *   每个请求分配一个线程（BIO/NIO），线程从线程池获取：
     *   - server.tomcat.threads.min-spare=10（最小空闲线程）
     *   - server.tomcat.threads.max=200（最大线程数，IO 密集型可调高）
     *   - server.tomcat.accept-count=100（等待队列长度）
     *   - server.tomcat.max-connections=8191（最大连接数）
     *
     * 二、线程池调优策略
     *   1) CPU 密集型：threads.max = CPU 核数 + 1
     *   2) IO 密集型：threads.max = CPU 核数 * 2 ~ 4（视 IO 等待比例）
     *   3) 混合型：拆分线程池，CPU 任务和 IO 任务分开
     *   4) 线程数不是越大越好：线程多 -> 上下文切换开销大 -> 吞吐反而下降
     *
     * 三、连接超时配置
     *   - server.connection-timeout=5s：建立连接超时
     *   - server.keep-alive-timeout=60s：Keep-Alive 空闲超时
     *   - server.max-keep-alive-requests=100：单连接最大请求数
     *   - spring.mvc.async.request-timeout=30s：异步请求超时
     *
     * 四、背压与限流
     *   1) 接受队列满时：Tomcat 拒绝新连接（Connection refused）
     *   2) 自定义限流：Bucket4j 按 IP/用户/接口限流
     *      - 每 IP 100 req/s，每用户 10 req/s，每接口 1000 req/s
     *   3) 网关限流：Spring Cloud Gateway + Redis 限流过滤器
     *   4) 自适应限流：根据系统负载动态调整限流阈值（如 Sentinel）
     *
     * 五、压测与容量规划
     *   1) 压测工具：JMeter / wrk / Gatling
     *   2) 关键指标：QPS / P99 延迟 / 错误率 / CPU / 内存
     *   3) 容量公式：单实例 QPS * 实例数 = 总容量（留 30% 余量）
     *   4) 压测环境与生产环境配置一致，否则数据无意义
     * =========================================================================
     */
}