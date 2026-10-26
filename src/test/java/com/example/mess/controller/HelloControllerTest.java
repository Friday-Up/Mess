package com.example.mess.controller;

import com.example.mess.dto.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * HelloController测试 - 使用@WebMvcTest只加载Web层，MockMvc模拟HTTP请求。
 * 
 * <p>测试策略:
 * <ul>
 *   <li>@WebMvcTest: 只加载HelloController和相关Spring MVC组件，不加载完整应用上下文</li>
 *   <li>MockMvc: 模拟HTTP请求，不启动真实服务器，测试速度快</li>
 *   <li>覆盖所有端点: 简单问候、高级问候、POST问候</li>
 *   <li>验证响应格式: 检查ApiResponse结构和状态码</li>
 * </ul>
 * 
 * <p>测试覆盖:
 * <ul>
 *   <li>helloWithDefaultName: GET /hello 默认参数</li>
 *   <li>helloWithCustomName: GET /hello?name=Spring 自定义参数</li>
 *   <li>advancedHelloWithVersionOne: GET /hello/advanced v1.0</li>
 *   <li>advancedHelloWithVersionTwo: GET /hello/advanced v2.0</li>
 *   <li>advancedHelloWithDefaultParameters: GET /hello/advanced 默认参数</li>
 *   <li>helloPost: POST /hello 自定义参数</li>
 * </ul>
 * 
 * <p>注意事项:
 * <ul>
 *   <li>需要配置安全测试上下文（当前使用@WebMvcTest，Security自动配置）</li>
 *   <li>POST请求需要CSRF Token或禁用CSRF（SecurityConfig中已禁用）</li>
 * </ul>
 * 
 * @since 1.0
 */
@WebMvcTest(HelloController.class)
@ActiveProfiles("test")
class HelloControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    /** GET /hello 默认参数 → "Hello, World!" */
    @Test
    void helloWithDefaultName() throws Exception {
        // Act: 不带 name 参数发起 GET，触发 Controller 的默认值逻辑
        MvcResult result = mockMvc.perform(get("/hello")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())                       // 先断言 HTTP 200
                .andReturn();                                     // 取出完整响应以便反序列化

        // 借助 TypeFactory 构造 ApiResponse<String> 的泛型类型，避免泛型擦除导致反序列化失败
        ApiResponse<String> response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                objectMapper.getTypeFactory().constructParametricType(ApiResponse.class, String.class));

        // Assert: 业务成功标志为 true，且默认问候语为 "Hello, World!"
        assertTrue(response.isSuccess());
        assertEquals("Hello, World!", response.getData());
    }

    /** GET /hello?name=Spring → "Hello, Spring!" */
    @Test
    void helloWithCustomName() throws Exception {
        // Act: 带 name=Spring 参数发起 GET，验证参数绑定生效
        MvcResult result = mockMvc.perform(get("/hello")
                .param("name", "Spring")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // 反序列化响应体为 ApiResponse<String>
        ApiResponse<String> response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                objectMapper.getTypeFactory().constructParametricType(ApiResponse.class, String.class));

        // Assert: 问候语应含传入的名字
        assertTrue(response.isSuccess());
        assertEquals("Hello, Spring!", response.getData());
    }

    /** GET /hello/advanced?name=Spring&version=1.0 → "Hello, Spring! (API v1.0)" */
    @Test
    void advancedHelloWithVersionOne() throws Exception {
        // Act: 传入 version=1.0，验证 Controller 对 1.0 分支的格式化输出
        MvcResult result = mockMvc.perform(get("/hello/advanced")
                .param("name", "Spring")
                .param("version", "1.0")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // 反序列化响应体为 ApiResponse<String>
        ApiResponse<String> response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                objectMapper.getTypeFactory().constructParametricType(ApiResponse.class, String.class));

        // Assert: v1.0 分支应返回带 "(API v1.0)" 后缀的问候语
        assertTrue(response.isSuccess());
        assertEquals("Hello, Spring! (API v1.0)", response.getData());
    }

    /** GET /hello/advanced?name=Spring&version=2.0 → "Hello, Spring! Welcome to API v2.0" */
    @Test
    void advancedHelloWithVersionTwo() throws Exception {
        // Act: 传入 version=2.0，验证 Controller 对 2.0 分支的差异化文案
        MvcResult result = mockMvc.perform(get("/hello/advanced")
                .param("name", "Spring")
                .param("version", "2.0")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // 反序列化响应体为 ApiResponse<String>
        ApiResponse<String> response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                objectMapper.getTypeFactory().constructParametricType(ApiResponse.class, String.class));

        // Assert: v2.0 分支应返回 "Welcome to API v2.0" 文案
        assertTrue(response.isSuccess());
        assertEquals("Hello, Spring! Welcome to API v2.0", response.getData());
    }

    /** GET /hello/advanced 默认参数 → "Hello, World! (API v1.0)" */
    @Test
    void advancedHelloWithDefaultParameters() throws Exception {
        // Act: 不传任何参数，验证 name 与 version 的默认值同时生效
        MvcResult result = mockMvc.perform(get("/hello/advanced")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // 反序列化响应体为 ApiResponse<String>
        ApiResponse<String> response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                objectMapper.getTypeFactory().constructParametricType(ApiResponse.class, String.class));

        // Assert: 默认 name=World、version=1.0 组合的问候语
        assertTrue(response.isSuccess());
        assertEquals("Hello, World! (API v1.0)", response.getData());
    }

    /** POST /hello?name=SpringBoot → "Hello, SpringBoot! (via POST)" */
    @Test
    void helloPost() throws Exception {
        // Act: 用 POST 方法访问 /hello，验证 POST 端点及 CSRF 已在 SecurityConfig 中禁用
        MvcResult result = mockMvc.perform(post("/hello")
                .param("name", "SpringBoot")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // 反序列化响应体为 ApiResponse<String>
        ApiResponse<String> response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                objectMapper.getTypeFactory().constructParametricType(ApiResponse.class, String.class));

        // Assert: POST 端点应返回带 "(via POST)" 标识的问候语
        assertTrue(response.isSuccess());
        assertEquals("Hello, SpringBoot! (via POST)", response.getData());
    }

    /*
     * =========================================================================
     * 【HTTP/REST】HTTP 方法测试矩阵与安全方法验证（非可执行代码）
     * =========================================================================
     *
     * 一、本类覆盖的 HTTP 方法测试维度
     *   1) GET /hello（默认参数）-> 200 + 包体 data="Hello, World!"
     *   2) GET /hello?name=Spring -> 200 + 含请求参数字符串
     *   3) GET /hello/advanced -> 高版本问候端点（多参数组合）
     *   4) POST /hello -> 200 + "(via POST)" 标识
     *   5) 缺失维度建议补强（REST 视角）：
     *      GET 不支持请求体（应 400 或忽略）
     *      HEAD /hello（同 GET 但无包体）
     *      OPTIONS /hello（返 Allow 头）
     *      不存在的路由 -> 404
     *      方法不匹配 -> 405 + Allow 头

     * 二、安全方法（Safe Method）的契约验证
     *   1) GET 必须是只读：测试断言 "GET 后 Repository 状态不变"
     *      （verify(repo, never()).save(any())）——防止把写操作混入 GET
     *   2) GET 必须可缓存：断言响应带 Cache-Control 头（项目可配置）
     *   3) GET 必须可重复：相同请求两次响应相同字符串——
     *      除非显式声明"内容随时间变"，否则违反 = 契约破损
     *   4) GET 请求体语义：框架默认忽略 GET body（规范不鼓励），

     * 三、方法与状态码的交叉矩阵（防止误返回）
     *   1) GET 成功：200（带包体）或 204（明确空体）
     *   2) GET 失败：4xx（不返 5xx 给客户端参数问题）
     *   3) POST 创建成功：201 + Location 头（项目当前 200 可接受，演进改 201）
     *   4) POST 业务失败：4xx + 包体 errorCode
     *   5) PUT 成功：200（返更新后资源）或 204（空体）
     *   6) DELETE 成功：204（推荐）或 200（带 message）
     *   7) 未预期的 5xx：测试不应覆盖具体异常，只测兜底路径返回包体格式

     * 四、HTTP 头的契约测试用例清单
     *   1) Content-Type 断言：application/json（编码不指定时默认 UTF-8）
     *   2) Cache-Control：GET /hello 返 max-age=60（若启用）
     *   3) Vary：Accept-Encoding（若启用 gzip）
     *   4) CORS 头：跨源请求 OPTIONS 预检验证 Allow-Origin 等头
     *   5) X-Request-Id：每个响应必带，与请求头传入的回显一致
     *   6) Security 头：X-Content-Type-Options: nosniff（Spring Security 默认开）
     *   7) 断言写法示例：
     *      .andExpect(header().string("Content-Type", "application/json"))
     *      .andExpect(header().exists("X-Request-Id"))

     * 五、MockMvc 与真实 HTTP 的差异（决策使用场景）
     *   1) MockMvc 走的"对象模拟"路径，不发真 TCP，不解析真 HTTP 报文
     *   2) 测不到的：Content-Length 准确性 / chunked encoding / gzip 透明 / 
     *      真实连接超时 / TLS 协商
     *   3) 能测到的：路由 / 状态码 / 包体 / 头 / Filter 链 / 序列化
     *   4) 何时换 TestRestTemplate + RANDOM_PORT：
     *      需要验证 HTTP 协议级行为时（如 CORS 预检真 OPTIONS）
     *      或部署前需要真容器冒烟时
     *   5) 本类日常用 MockMvc 毫秒级，CI 烟测加 1-2 个真实端点用例即可
     * =========================================================================
     */
}