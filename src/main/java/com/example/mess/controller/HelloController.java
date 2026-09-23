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
     * 【ADR-008】演示端点保留策略
     * =========================================================================
     * 上下文：项目中有 /hello 端点用于验证框架集成和联调，生产环境是否保留？
     * 决策：保留但限制访问。/hello 作为健康探针和联调验证有价值，
     *       生产环境通过 Security 配置限制访问权限（如只允许 ADMIN）。
     * 替代方案：
     *   A) 生产环境删除 —— 失去快速验证能力，每次部署后无法确认框架正常。
     *   B) 完全开放 —— 可能被滥用为 DDoS 目标或信息泄露。
     *   C) 用 Actuator /actuator/health 替代 —— 框架级探针，不走业务逻辑，
     *      适合运维探活但不适合验证业务框架集成。
     * 后果：保留 /hello 可快速验证"请求 -> 认证 -> 序列化 -> 响应"全链路；
     *       需确保 Security 配置正确限制访问。
     *
     * =========================================================================
     * 【代码审查要点】演示端点
     * =========================================================================
     * [ ] 不含业务逻辑，只验证"框架能通"
     * [ ] 日志用占位符 {} 拼接，不用字符串 +
     * [ ] @Value 有兜底值（${app.xxx:default}），无配置也能启动
     * [ ] 不依赖数据库/缓存，独立可验证
     * [ ] Security 配置限制访问权限（生产环境不 permitAll）
     * [ ] 不暴露内部实现细节（版本号/路径/配置值）
     * [ ] 与 /actuator/health 职责区分：hello 走业务链路，actuator 走框架链路
     * =========================================================================
     */

    /*
     * =========================================================================
     * 【ADR-008-S】日志与配置注入策略（补充）
     * =========================================================================
     * 上下文：日志是排障的核心工具，配置注入是外部化的基础。
     * 决策：日志用 SLF4J + Logback，占位符 {} 拼接；配置用 @Value 注入，
     *       必须提供兜底值（${key:default}）。
     * 替代方案：
     *   A) System.out.println —— 无级别、无格式、无上下文，不可接受。
     *   B) java.util.logging —— 不统一，与 SLF4J 生态不兼容。
     *   C) 不提供兜底值 —— 配置缺失时启动报错，开发体验差。
     * 后果：SLF4J 是门面，运行时绑定 Logback；占位符在日志级别不满足时不拼接；
     *       兜底值保证无配置也能启动。
     *
     * 日志级别速查：
     *   ERROR -> 系统故障，需立即处理（5xx 异常）
     *   WARN  -> 潜在问题，需关注（4xx 异常、降级）
     *   INFO  -> 关键业务事件（启动、关停、重要操作）
     *   DEBUG -> 调试信息（方法出入参、中间状态）
     *   TRACE -> 最细粒度（框架内部流程），生产一般不用
     *
     * 配置热更新：
     *   @Value 默认启动时注入一次，配置中心变更不生效
     *   加 @RefreshScope（Spring Cloud）后，/actuator/refresh 触发重新注入
     *   @ConfigurationProperties Bean 整体可被刷新，比 @Value 更适合批量绑定
     * =========================================================================
     */
}