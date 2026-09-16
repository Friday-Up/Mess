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
     * 【面试问答】关于日志与配置注入的常见面试题（非可执行代码）
     * =========================================================================
     *
     * Q1: 为什么用 SLF4J 的占位符 {} 而不用字符串拼接？
     * A1: 占位符在日志级别不满足时不产生拼接开销；字符串拼接总会执行。
     *     log.info("user: " + user) 即使日志级别是 WARN 也拼接了字符串。
     *
     * Q2: @Value("${key:default}") 的 default 是什么？
     * A2: 冒号后面是兜底值，配置项不存在时用它，避免启动报
     *     "Could not resolve placeholder"。配置存在时忽略兜底值。
     *
     * Q3: @Value 改了配置为什么不生效？
     * A3: @Value 在 Bean 初始化时注入一次，运行时配置变更不会自动刷新。
     *     需要 @RefreshScope（Spring Cloud Config）+ /actuator/refresh。
     *
     * Q4: 日志不输出怎么办？
     * A4: 检查链路：
     *     1) logback.xml 中 logger level >= 当前日志级别（INFO 日志需要 >= INFO）
     *     2) appender ref 绑定到该 logger
     *     3) 用的是 SLF4J 接口（别混用 java.util.logging）
     *
     * Q5: 日志没有 traceId 怎么排查？
     * A5: 在过滤器中用 MDC.put("traceId", uuid) 注入，
     *     logback pattern 加 %X{traceId} 输出。
     *     跨服务用 Sleuth/OpenTelemetry 自动传递。
     *
     * Q6: @Value 和 @ConfigurationProperties 怎么选？
     * A6: 单个值用 @Value；批量绑定一组配置用 @ConfigurationProperties，
     *     后者支持松散绑定、JSR303 校验、整体刷新。
     * =========================================================================
     */
}