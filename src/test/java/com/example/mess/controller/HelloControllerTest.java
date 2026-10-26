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
         * 【JVM内存管理与GC调优】单测中的 JIT 缺席与微基准陷阱（非可执行代码）
         * =========================================================================
         *
         * 一、为什么单测"快"但不代表生产快
         *   1) 本类跑完一次约几百毫秒：每个 @Test 仅执行几十次方法调用，
         *      远低于 C1/C2 编译阈值（1500/10000）——全程解释执行
         *   2) 生产环境热路径是"已 JIT 高度优化的代码"，
         *      单测中方法性能特征与稳态不同——别用单测感知"性能"
         *   3) 但单测的"快"是另一种价值：快速反馈 + 高频运行 + 测试金字塔地基
         *      ——质量工程要快测快返，性能要交给专门的微基准与压测
         *
         * 二、别在单测里做性能断言
         *   1) 反例：assert executionTime < 100——CI 机器负载波动、JIT 没起来、
         *      GC 停顿随机——这种断言是"血肉横飞的 flaky"的源头
         *   2) 正解：性能验证走 JMH（强制 warmup、多 fork 隔离、统计置信区间），
         *      单测锁行为，JMH 锁性能——两个层次不要混
         *   3) Mockito mock 的性能特征：mock 调用走字节码生成代理，
         *      比真实对象慢 2-10 倍——测试中的"调用耗时"对生产毫无参考价值
         *
         * 三、测试 JVM 配置的取舍
         *   1) -XX:TieredStopAtLevel=1 加速测试 JVM 起跑；
         *      -Xshare:on 开启 CDS 加速启动——CI 提效的实用参数
         *   2) test-compile 是 javac 的纯编译（无 JIT 介入）：
         *      编译错误不是运行性能问题——这两层反馈要分清
         * 四、测试运行时的 JIT 参数实践
         *   1) surefire argLine 优化：-XX:TieredStopAtLevel=1 -Xshare:on
         *      让测试 JVM 起跑快 30%+——CI 提效的稳定手段
         *   2) -Xint（纯解释执行）让某些测试反而变慢——
         *      少数字节码增强框架（如 Lombok 生成的复杂结构）受 JIT 影响大，
         *      CI 慢时确认 argLine 没被意外设置成 -Xint
         *
         =========================================================================
         */
}