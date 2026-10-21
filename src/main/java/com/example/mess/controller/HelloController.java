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
     * 【微服务模式】服务发现与健康检查（非可执行代码）
     * =========================================================================
     *
     * 一、服务发现（Service Discovery）
     *   单体：所有模块在同一进程，直接方法调用
     *   微服务：服务实例动态伸缩（K8s Pod 扩缩容），IP 不固定
     *   服务发现问题：订单服务如何找到用户服务的可用实例？
     *   方案一：客户端发现（Client-Side Discovery）
     *   - 服务注册：用户服务启动时向注册中心（Nacos/Eureka）注册 IP:Port
     *   - 服务发现：订单服务从注册中心拉取用户服务实例列表
     *   - 负载均衡：订单服务客户端负载均衡（Ribbon/LoadBalancer）选实例
     *   方案二：服务端发现（Server-Side Discovery）
     *   - 路由：请求先到负载均衡器（K8s Service/网关）
     *   - 均衡：负载均衡器查询注册中心，选实例转发
     *   - 客户端无感知，只需访问虚拟 IP/域名
     *   K8s 原生方案：K8s Service + CoreDNS = 服务端发现
     *   - 用户服务：Service user-service -> Pod 列表
     *   - 订单服务：访问 http://user-service:8080 -> CoreDNS 解析 -> Service 转发
     *   - 无需额外注册中心（K8s 自带）
     *   Spring Cloud 方案：Nacos/Eureka + LoadBalancer
     *   - @LoadBalanced RestTemplate/WebClient 自动服务发现 + 负载均衡
     *   - Feign/OpenFeign 声明式调用：@FeignClient("user-service")
     *
     * 二、健康检查与服务注册的关系
     *   注册中心需要知道实例是否健康：
     *   1) 心跳机制：实例定时向注册中心发心跳（Nacos 5s/次）
     *   2) 健康检查端点：注册中心定时调用 /actuator/health
     *   3) K8s 探针：livenessProbe 失败 -> 重启 Pod；readinessProbe 失败 -> 摘除流量
     *   4) 优雅上下线：
     *      上线：健康检查通过后注册中心才推送实例（避免流量打到未就绪实例）
     *      下线：先从注册中心注销 -> 等待流量排空 -> 再关闭进程
     *   本项目 /hello 可作为简易健康检查端点：
     *   - 验证"请求 -> 认证 -> 序列化 -> 响应"全链路
     *   - 生产环境建议用 /actuator/health（框架级，更全面）
     *
     * 三、弹性模式（Resilience Patterns）
     *   微服务调用链中任一环节故障都可能雪崩：
     *   1) 超时（Timeout）：下游调用设置超时（2s），避免线程阻塞
     *   2) 重试（Retry）：可重试错误（网络抖动）重试 3 次，需幂等
     *   3) 熔断（Circuit Breaker）：错误率 >50% 时熔断 10s，
     *      熔断期间快速失败（不调下游），10s 后半开试探
     *      实现：Resilience4j @CircuitBreaker
     *   4) 降级（Fallback）：熔断/超时时返回兜底数据
     *      如：用户服务降级 -> 返回缓存数据或默认值
     *   5) 舱壁（Bulkhead）：隔离资源，每个下游调用独立线程池/信号量
     *      如：用户服务调用线程池 10 个，订单服务 10 个，互不影响
     *   6) 限流（Rate Limiting）：限制自身处理速率，保护下游
     *   Spring Cloud 全家桶：Resilience4j（熔断/限流/舱壁）+ LoadBalancer（负载均衡）
     * =========================================================================
     */
}