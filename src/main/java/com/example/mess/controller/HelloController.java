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
         * 【可观测性与生产运维】零 DB 接口的监控基线与黄金指标（非可执行代码）
         * =========================================================================
         *
         * 一、这个接口是全链路监控的基线
         *   1) 黄金四指标：延迟(P99)、流量(QPS)、错误率(5xx 比例)、饱和度(线程/连接)——
         *      hello 是延迟与流量的无干扰基线：总 RT 减它即下行依赖耗时
         *   2) 延迟要分位数不要平均值：P99 才暴露尾延迟，平均数会把 1% 的慢请求
         *      平均成"还行"——SLA 一律用分位数表述（P99 < 100ms）
         *   3) RED 方法（Rate/Error/Duration）：每个端点独立计数，
         *      Grafana 按 endpoint 标签拆看，别把所有接口聚合成一条线
         *
         * 二、轻量接口的指标埋点方式
         *   1) Micrometer + Actuator/Prometheus：@Timed 注解或 
         *      TimedAspect 在 Controller 切面自动出 P50/P95/P99——无需手写
         *   2) 自定义业务指标要克制：指标数过万会让 Prometheus 查询变慢，
         *      高基数标签（userId）禁止做指标标签，只能进日志/链路
         *   3) 本接口虽零 DB，但也要进监控：它的异常率上涨常是框架级问题
         *      （线程池打满/序列化故障），比业务接口更早暴露系统性风险
         *
         * 三、基准接口在 SRE 实践中的位置
         *   1) 可用性 SLO 的最便宜探针：用 hello 周期探测（每 10s），
         *      成功率作 SLO 燃烧率计算——比全链路探测更早发现"实例不健康"
         *   2) 不要往 hello 里加任何依赖：一旦它查缓存/调服务，
         *      就失去了"基线"身份——基线的价值在于它只反映框架与机器本身
         *   3) 多区域部署时 hello 是区域健康哨兵：每区独立 SLO，
         *      区域级故障先在 hello 的可用率上显现，再传导到业务接口
         *   4) 错误预算实践：hello 的 SLO 燃烧率 >14x 时冻结发布——
         *      这是最小成本的发布熔断，不需要复杂的业务指标体系支撑
         =========================================================================
         */
}