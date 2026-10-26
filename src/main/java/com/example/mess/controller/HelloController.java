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
     * 【HTTP/REST】HTTP 方法语义与幂等性、安全性（非可执行代码）
     * =========================================================================
     *
     * 一、RFC 9110 定义的方法属性三轴
     *   1) Safe（安全）：GET/HEAD/OPTIONS/TRACE——只读、不产生副作用，
     *      客户端可"放心"重发（爬虫/预取/缓存代理都依赖此约定）
     *   2) Idempotent（幂等）：同一方法+URI+Body 重复执行，效果与一次相同
     *      GET/PUT/DELETE 幂等；POST 通常不幂等；PATCH 语义上可能不幂等
     *   3) Cacheable（可缓存）：GET/HEAD 显式可缓存，POST 在特定条件下可缓存
     *   4) 本类 helloGet 是 safe+idempotent+cacheable；helloPost 不 safe 不幂等

     * 二、本类 GET/POST 的语义正确性体检
     *   1) GET /hello?name=x：参数在 query，语义"查询问候"，
     *      完全符合 safe——不写库、不改状态、响应可缓存
     *   2) GET /hello/advanced：带版本查询参数，幂等且可缓存，
     *      每次响应同一输入产出同一输出（前提：service 不依赖时间变动）
     *   3) POST /hello：仅作为"演示"，实际语义是"创建问候记录"会更好；
     *      当前只是返回变形字符串——可以说语义偏弱，但最小样例可接受
     *   4) REST 纪律：不要用 POST 干 GET 的事（规避 query 长度限制时除外），
     *      语义偏移会破坏缓存层、CDN、监控、安全过滤器的行为预期

     * 三、幂等性的工程价值（不只是文档名词）
     *   1) 客户端重试安全：网络超时后，GET/PUT/DELETE 可直接重试；
     *      POST 必须用幂等键（Idempotency-Key 请求头）做服务端去重
     *   2) service mesh/网关层重试：默认只重试幂等方法，避免把一次下单
     *      放大成两次扣款（生产事故高发场景）
     *   3) 幂等键实现路径（本类/项目演进方向）：
     *      a) 客户端生成 UUID 放入 Idempotency-Key 头
     *      b) 服务端 Redis SETNX(key, responseSnapshot, TTL=24h)
     *      c) 命中直接返回快照，未命中执行业务并存快照
     *   4) 判定幂等的责任在谁：协议契约由服务端声明（"我的 POST 是幂等的
     *      当且仅当你带 key"），客户端不能默认 POST 可重试

     * 四、状态码与方法的对应禁忌
     *   1) 本类所有成功路径返 200——对 GET 是正解
     *   2) POST 创建成功应为 201 Created + Location 头（指向新资源），
     *      本项目 UserController 的 createUser 用 ApiResponse 包 200，
     *      是"保持解析统一"的实用主义妥协（详见 ApiResponse 块）
     *   3) 客户端不应把 200 包体里的 success=false 视为 HTTP 失败：监控
     *      仪表盘的"错误率"若仅按 status>=500 计，会漏掉业务错误——
     *      上报时必须同时看 HTTP 状态与包体 success 字段
     * =========================================================================
     */
}