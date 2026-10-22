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
     * 【Spring 内幕】MockMvc 内幕：MockServletEnvironment 与请求模拟（非可执行代码）
     * =========================================================================
     *
     * 一、MockMvc.build() 做了什么
     *   1) 自动配置模式下，MockMvcAutoConfiguration 组装出 MockMvcBuilder：
     *      内部构造 TestDispatcherServlet（DispatcherServlet 的测试子类）
     *   2) 注册 HandlerMapping + HandlerAdapter + ExceptionResolver +
     *      HttpMessageConverter -> 与生产装配尽量一致
     *   3) MockMvc 本质是把 HTTP 请求模拟成对象（MockHttpServletRequest），
     *      跳过 Tomcat 网络/线程层，直接在调用线程里走 DispatcherServlet
     *      -> 快（毫秒级）且不需要起 Web 服务器
     *
     * 二、perform() 内部的调用链
     *   1) MockMvc.perform(get("/hello?name=Spring"))
     *      -> RequestBuilder 构造 MockHttpServletRequest（含 URI/参数/Header）
     *   2) MockMvc -> MockFilterChain 串联已注册 Filter（含 Security 代理）
     *      -> 最后进入 TestDispatcherServlet.doDispatch
     *   3) DispatcherServlet 走完整 Handler + Resolver 链路（见 UserController 注释块）
     *   4) MockHttpServletResponse 收集写出结果
     *   5) ResultMatcher 校验状态/体/头：andExpect(status().isOk())
     *      jsonPath 解析 JSON 断言
     *
     * 三、MockMvc vs @SpringBootTest(webEnvironment=RANDOM_PORT) 的差异
     *   - MockMvc：不起真实端口，最快，但绕开部分网络层（Servlet 实际编码、
     *     异步请求处理场景的真实超时）
     *   - RANDOM_PORT：起真实内嵌 Tomcat，TestRestTemplate 真发 HTTP，
     *     可以验证序列化/反序列化的端到端正确性，但启动慢约 1-2 秒/次
     *   - 本测试采用 MockMvc 自动配置模式，侧重点在接口行为
     *
     * 四、打桩与真实容器的边界原则
     *   - when(service.xxx()).thenReturn(...) 显式 mock，隔离 Service 层
     *   - 但要警惕过度 mock：把全部层都 mock 后，
     *     最后只测了 @RequestMapping 注册是否正确，价值大减
     *   - 测试金字塔建议：controller 层 mock service（本类）；
     *     service 层 mock repository；repository 层上 @DataJpaTest 真实测
     *
     * 五、MockMvc 诊断输出与调试技巧
     *   1) .andDo(print()) / .andDo(MockMvcResultHandlers.log())：
     *      输出完整请求/响应（头/体/处理耗时），是排查 BUG 的第一工具
     *   2) Content-Type 不匹配诊断：UnsupportedMediaTypeStatusException 会列出
     *      supported，常见原因是缺请求头或未注册对应 Converter
     *   3) HttpMessageNotReadableException 表示 JSON 解析失败，先查字段名
     *      拼写/嵌套结构/枚举值，Jackson 反序列化器会直接抛出定位信息
     *   4) Security 过滤器拦截（401/403）时先确认过滤器链顺序与
     *      SecurityContext 是否被注入（print() 可见 SecurityContext 属性）
     *   5) 静态导入 get()/status()/jsonPath() 让链式调用可读性最佳
     *
     * 六、参数注入与默认值的 MockMvc 行为
     *   1) @RequestParam(defaultValue = World) 在请求无 name 参数时生效，
     *      MockMvc 不传 .param(name) 即可验证默认分支
     *   2) 传空字符串 .param(name, ) 不会触发默认值，而是得到空串
     *   3) 中文参数需 .param(name, 张三) + UTF-8 编码断言，
     *      或使用 .contentType(TEXT_PLAIN) + UTF-8 避免乱码
     *   4) 断言响应体优先用 content().string 而非 jsonPath（字符串响应更直接）
     * =========================================================================
     */
}