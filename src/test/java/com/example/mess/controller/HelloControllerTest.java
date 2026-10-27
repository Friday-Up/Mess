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
         * 【可观测性与生产运维】单测验证响应契约与观测字段不退化（非可执行代码）
         * =========================================================================
         *
         * 一、响应体里的观测字段需要测试锁死
         *   1) 断言 ApiResponse 含 traceId / errorCode / success 三字段的结构：
         *      契约测试防"有人重构响应体时删掉 traceId"——
         *      字段一旦被删，前端报障定位能力静默归零，没人会及时发现
         *   2) 本类对 hello 的断言即响应格式回归保护：路由、状态码、
         *      序列化结构不被改崩——观测依赖的"响应可解析"由此保底
         *   3) 错误场景测试同理：mock service 抛异常后断言响应里
         *      errorCode 出现且 traceId 非空——错误闭环的测试是
         *      "用户报障能被定位"这条链路的存在性证明
         *
         * 二、测试自身的可观测边界
         *   1) 测试失败输出要"人能直接看懂"：断言消息写清
         *      "期望字段 X 存在但实际缺失"，而不是只给 assert true/false——
         *      测试输出是 CI 第一观测界面，可读性直接影响修复速度
         *   2) 别在单测里写日志断言（mock Logback Appender 数日志行数）：
         *      日志实现细节高频变动，这类测试脆弱——日志契约用
         *      专门的集成测试 + 结构化字段断言，而不是字符串匹配
         *   3) 测试名的可观测价值：should_returnTraceId_whenServiceFails——
         *      测试名即文档，失败时看名字就知道破坏了哪条行为
         *
         * 三、基准契约测试在 CI 中的价值
         *   1) 本测试是"最小可用性回归"：任何让 hello 坏掉的改动
         *      一定也威胁其他接口——它失败时优先怀疑框架级/配置级破坏
         *   2) CI 失败也要可观测：失败率、平均修复时间入指标，
         *      CI 不稳定（flaky）会消磨团队对测试的信任，
         *      信任崩塌比个别测试缺失危害更大
         *   3) 本测试的响应断言同时是"监控探针的样本"：
         *      探针脚本按同样的断言逻辑实现，测试与探针保持一致，
         *      防"测试绿但探针误报"的尴尬
         =========================================================================
         */
}