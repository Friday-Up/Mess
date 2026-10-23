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
     * 【Java 并发】Tomcat 线程模型、Servlet 线程安全与 ThreadLocal 清理（非可执行代码）
     * =========================================================================
     *
     * 一、内嵌 Tomcat 的线程分工（请求一进一出谁在干活）
     *   1) Acceptor 线程（1 个，ServerSocket.accept 阻塞循环）：
     *      只负责收 TCP 连接，把 NioChannel 注册给 Poller，绝不做业务
     *      经验：连接洪水攻击时此线程最先排队，但业务 CPU 不高
     *   2) Poller 线程（默认 1-2 个，基于 NIO Selector）：
     *      监听 OP_READ 就绪，把就绪通道包装成任务交给 Executor
     *   3) Executor 工作线程（默认池 maxThreads=200，minSpare=10，namePrefix=pool-N）：
     *      解析 HTTP 报文 -> 走 Filter 链 -> 进 DispatcherServlet -> 执行本类
     *      hello() 方法 -> 写响应 -> 线程回池
     *   4) 线程回池的含义：池化线程体（PooledThreadRunnable）被复用，
     *      但 ThreadLocal 不会被容器清除 -> 这是脏数据的主要来源
     *
     * 二、Controller 的线程安全基线
     *   1) 本类只持有一个 final 依赖（UserService 注入）与无状态方法：
     *      天然线程安全，多个 Tomcat 线程可同时进入 hello() 互不干扰
     *   2) 何时会出问题？向本类加了共享可变字段：
     *      private int hitCount;  // 两个请求线程同时 hitCount++ 会丢更新
     *   3) 线程安全的修法按场景：
     *      - 计数：AtomicInteger 或 LongAdder（高并发更优）
     *      - 收集结果：ConcurrentHashMap / CopyOnWriteArrayList
     *      - 复合判断：synchronized 块或 ReentrantLock（见 UserService 块）
     *   4) 方法内的局部变量：每个线程有独立栈帧，局部引用绝无共享，
     *      写业务逻辑应尽量把状态压到方法局部，缩小共享面
     *
     * 三、ThreadLocal 的使用与污染排查
     *   1) 常见用途：MDC 日志链路 ID、用户上下文、DB 连接绑定（JDBC），
     *      Spring 事务管理器正是用 ThreadLocal 把连接绑到当前线程
     *   2) Tomcat 线程池复用线程：上一次请求设置的 ThreadLocal 值
     *      若不 remove，会被下一次请求读到 -> 脏数据、权限串号
     *   3) 正确模式（finally 清理不可省）：
     *      try { CONTEXT.set(user); doWork(); } finally { CONTEXT.remove(); }
     *   4) 排查手段：压测压出"偶现串号"问题，先翻 ThreadLocal 使用点，
     *      再用 jstack 看工作线程栈里残留的 Filter/Interceptor；线上用
     *      arthas thread -b 或 ThreadLocal 网关埋点做脏数据拦截
     *
     * 四、同步与异步 Servlet 的界限
     *   1) 传统 Servlet（本类）：一个请求占一个工作线程直到响应写出
     *   2) 异步 Servlet（startAsync + DeferredResult）：Release 工作线程，
     *      业务线程完成后回调写响应，适合长耗时外部调用
     *   3) Spring MVC 的 @Async 返回值机制（DeferredResult/Callable/SseEmitter）
     *      底层即异步 Servlet，但并不自动换线程池，需自定义 TaskExecutor
     *   4) 判断准则：CPU 密集任务别异步（线程切换反而更慢）；
     *      IO 密集任务（远程调用、写文件）才值得释放 Tomcat 线程
     *   5) /hello 这类零依赖接口应保持同步，异步化反而增加状态机复杂度
     * =========================================================================
     */
}