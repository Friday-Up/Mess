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
     * 【ADR-015】泛型反序列化测试策略
     * =========================================================================
     * 上下文：ApiResponse<T> 的泛型 T 在运行时被擦除，直接反序列化会得到
     *         LinkedHashMap 而非目标类型。测试中需要验证完整序列化/反序列化链路。
     * 决策：使用 ObjectMapper.getTypeFactory().constructParametricType() 显式指定
     *      泛型类型，确保 data 字段被正确反序列化为 String。
     * 替代方案：
     *   A) 直接强转 (String) data —— 运行时 data 是 LinkedHashMap，ClassCastException。
     *   B) 用 TypeReference<ApiResponse<String>>{} —— 也可行，但本测试选择
     *      constructParametricType 展示更通用的方案（适合动态类型场景）。
     *   C) 只验证 JSON 字符串不反序列化 —— 无法验证反序列化链路。
     * 后果：测试验证了完整的序列化+反序列化链路；泛型擦除问题在测试阶段暴露；
     *       前端/客户端集成时也需同样处理（用 TypeReference 或 ParameterizedType）。
     *
     * =========================================================================
     * 【代码审查要点】序列化测试
     * =========================================================================
     * [ ] 泛型反序列化用 constructParametricType 或 TypeReference
     * [ ] 断言信息可读（assertEquals(expected, actual) 而非 assertTrue）
     * [ ] 时间断言考虑时区（CI 可能用 UTC）
     * [ ] 共享 ObjectMapper（不要每个测试 new 一个）
     * [ ] 不用 Thread.sleep 做异步测试（用 Awaitility 或 CountDownLatch）
     * [ ] Boolean 字段名不以 is 开头（Jackson 去掉 is 前缀导致字段名不一致）
     * [ ] null 字段加 @JsonInclude(NON_NULL) 或全局配置
     * =========================================================================
     */
}