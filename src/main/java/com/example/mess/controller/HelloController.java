package com.example.mess.controller;

import com.example.mess.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

/**
 * 问候控制器 - 提供简单的问候API，用于演示和测试Spring Boot基本功能。
 * 
 * <p>控制器职责:
 * <ul>
 *   <li>提供简单的问候接口，验证Spring Boot应用正常运行</li>
 *   <li>演示GET/POST请求处理、参数绑定、响应封装</li>
 *   <li>作为健康检查端点，监控应用可用性</li>
 * </ul>
 * 
 * <p>安全策略:
 * <ul>
 *   <li>所有接口公开访问（SecurityConfig中 /hello/** → permitAll）</li>
 *   <li>不需要认证，适合作为健康检查和监控端点</li>
 * </ul>
 * 
 * <p>API端点:
 * <table border="1">
 *   <tr><th>方法</th><th>路径</th><th>参数</th><th>说明</th></tr>
 *   <tr><td>GET</td><td>/hello</td><td>name(可选,默认World)</td><td>简单问候</td></tr>
 *   <tr><td>GET</td><td>/hello/advanced</td><td>name, version</td><td>高级问候（支持版本号）</td></tr>
 *   <tr><td>POST</td><td>/hello</td><td>name(可选,默认World)</td><td>POST方式问候</td></tr>
 * </table>
 * 
 * <p>使用示例:
 * <pre>{@code
 *   // 简单问候
 *   curl http://localhost:8080/hello?name=Spring
 *   // 高级问候
 *   curl http://localhost:8080/hello/advanced?name=Spring&version=2.0
 *   // POST问候
 *   curl -X POST http://localhost:8080/hello?name=SpringBoot
 * }</pre>
 * 
 * @see com.example.mess.config.SecurityConfig 安全配置中定义了访问规则
 * @since 1.0
 */
@RestController
@RequestMapping("/hello")
@Tag(name = "问候服务", description = "简单的问候API，用于测试和演示")
public class HelloController {

    private static final Logger log = LoggerFactory.getLogger(HelloController.class);

    /**
     * 简单问候接口。
     * <p>根据传入的name参数返回问候语，name参数可选，默认为"World"。
     * 这是最基础的GET请求示例，演示了@RequestParam和默认值的使用。
     * 
     * <p>请求示例: GET /hello?name=Spring → 返回 "Hello, Spring!"
     * 
     * @param name 问候对象名称，默认值为"World"
     * @return 包含问候语的ApiResponse，success=true
     */
    @GetMapping
    @Operation(summary = "简单问候", description = "根据名称返回问候语，name参数可选，默认为'World'")
    public ApiResponse<String> hello(@RequestParam(defaultValue = "World") String name) {
        // 使用String.format拼装问候语，name未传时默认为"World"
        String greeting = String.format("Hello, %s!", name);
        // 记录INFO级日志，便于追踪接口调用情况（占位符{}避免字符串拼接开销）
        log.info("收到问候请求，问候对象: {}", name);
        // 用统一响应对象包装问候语返回
        return ApiResponse.success(greeting);
    }

    /**
     * 高级问候接口。
     * <p>支持name和version两个参数，根据版本号返回不同格式的问候语。
     * v2.0返回增强格式（包含欢迎信息），其他版本返回标准格式。
     * 演示了多参数绑定和条件逻辑处理。
     * 
     * <p>请求示例:
     * <ul>
     *   <li>GET /hello/advanced?name=Spring&version=1.0 → "Hello, Spring! (API v1.0)"</li>
     *   <li>GET /hello/advanced?name=Spring&version=2.0 → "Hello, Spring! Welcome to API v2.0"</li>
     * </ul>
     * 
     * @param name 问候对象名称，默认值为"World"
     * @param version API版本号，默认值为"1.0"，v2.0返回增强格式
     * @return 包含高级问候语的ApiResponse
     */
    @GetMapping("/advanced")
    @Operation(summary = "高级问候", description = "根据名称和版本返回高级问候语")
    public ApiResponse<String> advancedHello(
            @RequestParam(defaultValue = "World") String name,
            @RequestParam(defaultValue = "1.0") String version) {
        String greeting;
        if ("2.0".equals(version)) {
            // 版本2.0返回增强格式问候语（含Welcome欢迎信息）
            // 用常量"2.0"在前调用equals，可避免version为null时的空指针异常
            greeting = String.format("Hello, %s! Welcome to API v%s", name, version);
        } else {
            // 其他版本返回标准格式问候语（附带版本号标识）
            greeting = String.format("Hello, %s! (API v%s)", name, version);
        }
        // 记录问候对象与版本号，便于统计各版本调用分布
        log.info("收到高级问候请求，问候对象: {}, 版本: {}", name, version);
        return ApiResponse.success(greeting);
    }

    /**
     * POST方式问候接口。
     * <p>通过POST请求发送问候，演示POST请求处理和参数绑定。
     * 返回格式与GET不同，添加"(via POST)"标识。
     * 
     * <p>请求示例: POST /hello?name=SpringBoot → "Hello, SpringBoot! (via POST)"
     * 
     * @param name 问候对象名称，默认值为"World"
     * @return 包含POST问候语的ApiResponse
     */
    @PostMapping
    @Operation(summary = "POST问候", description = "通过POST请求发送问候")
    public ApiResponse<String> helloPost(@RequestParam(defaultValue = "World") String name) {
        // POST方式的问候语，附加"(via POST)"以区别于GET请求
        String greeting = String.format("Hello, %s! (via POST)", name);
        // 记录POST问候日志
        log.info("收到POST问候请求，问候对象: {}", name);
        return ApiResponse.success(greeting);
    }

    /*
     * =========================================================================
     * 【Spring 内幕】从 Tomcat NIO 到 Handler：监听、分发与过滤器链（非可执行代码）
     * =========================================================================
     *
     * 一、一次 /hello?name=Spring 请求的网络与线程模型（内嵌 Tomcat 10）
     *   1) Acceptor 线程（1 个）：ServerSocket.accept() 接收 TCP 连接
     *   2) Poller 线程（默认 1-2 个）：基于 NIO Selector 监听 OP_READ 就绪事件，
     *      就绪后包装为 NioChannel 投递到 Executor 线程池
     *   3) Executor 线程池（默认 maxThreads=200，minSpareThreads=10）：
     *      负责实际解析 HTTP 头、读取 Request 体并执行 Servlet 调用链
     *   4) 本类 hello() 方法运行在 Executor 线程中，返回字符串后等响应
     *      写出完成线程才回池
     *   可见一个简单 Hello 也要经过：TCP -> NIO -> HTTP 解析 -> Filter 链
     *   -> DispatcherServlet -> HandlerAdapter -> 方法，共 6 层
     *
     * 二、Filter 过滤器链的执行顺序与 DelegatingFilterProxy 桥接
     *   Spring Security 注册的是一个 FilterChainProxy（由 springFilterChain 提供）：
     *   - 它本质是 jakarta.servlet.Filter，注册到 ServletContext 时通过
     *     DelegatingFilterProxy 包装 -> 代理 BeanFactory 中的 springSecurityFilterChain
     *   - 请求进入 FilterChainProxy 后按 SecurityFilterChain 列表匹配：
     *     本项目只注册了一条链，链上默认约 12-16 个 Filter（顺序敏感）：
     *     DisableEncodeUrlFilter -> ForceEagerSessionCreationFilter ->
     *     ChannelProcessingFilter -> WebAsyncManagerIntegrationFilter ->
     *     SecurityContextHolderFilter -> HeaderWriterFilter -> CsrfFilter ->
     *     LogoutFilter -> UsernamePasswordAuthenticationFilter ->
     *     RequestCacheAwareFilter -> SecurityContextHolderAwareRequestFilter ->
     *     AnonymousAuthenticationFilter -> SessionManagementFilter ->
     *     ExceptionTranslationFilter -> AuthorizationFilter
     *   - 本类 /hello/** 被 permitAll()，授权过滤器直接放行
     *   - 若访问 /api/users/** 则 AuthorizationManager.decide() 检查当前
     *     SecurityContext 中的 Authentication 是否已认证（表单登录）
     *
     * 三、SecurityContext 的线程绑定与跨线程传播风险
     *   - SecurityContextHolderStrategy（默认 ThreadLocal 模式）：用户身份
     *     存储在当前线程的 ThreadLocal，过滤器读取后放入 Request
     *   - 若在本类方法内开启 @Async 子线程，子线程默认看不到安全上下文，
     *     需配置 DelegatingSecurityContextRunnable（WebAsyncManagerIntegrationFilter）
     *   - WebFlux/Reactor 场景则使用 ReactorContext 模式替代 ThreadLocal
     *
     * 四、健康检查的框架层实现与最佳实践
     *   1) Spring Boot Actuator 的 /actuator/health 是 framework-level 实现：
     *      内置 HealthIndicator（db/diskSpace/redis/discoveryClient）自动探测，
     *      StatusAggregator 聚合为 UP/DOWN/UNKNOWN
     *   2) 与 K8s 探针对接（ManagementProbesHealthContributorHandlerMapping）：
     *      liveness 只看应用自身，失败重启 Pod；readiness 额外检查外部依赖，
     *      失败摘除流量（management.endpoint.health.probes.enabled=true）
     *   3) 本类 /hello 作为最小健康检查的适用场景：验证路由+序列化+响应
     *      全链路且无重外部依赖；大流量场景注意限流，避免与业务 API 竞争线程
     * =========================================================================
     */
}