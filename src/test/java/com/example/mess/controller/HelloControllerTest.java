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
     * 【契约测试】序列化契约与版本兼容性（非可执行代码）
     * =========================================================================
     *
     * 一、序列化契约的定义
     *   序列化契约 = API 响应的 JSON 结构约定
     *   包含：字段名、字段类型、字段是否可选、嵌套结构、格式（日期/枚举）
     *   示例（ApiResponse<String> 的契约）：
     *   {
     *     "code": integer,         // 必填，HTTP 状态码
     *     "message": string,       // 必填，用户可读信息
     *     "data": string | null,   // 可选，泛型数据
     *     "path": string           // 必填，请求路径
     *   }
     *   契约一旦发布，就是与所有消费方的约定，不能随意破坏
     *
     * 二、版本兼容性规则
     *   向后兼容（旧客户端读新响应）：
     *   [x] 新增字段（旧客户端忽略未知字段）- 安全
     *   [x] 新增可选字段（值为 null 时旧客户端无感知）- 安全
     *   [ ] 删除字段 - 破坏兼容
     *   [ ] 改字段名 - 破坏兼容
     *   [ ] 改字段类型（integer -> string）- 破坏兼容
     *   [ ] 改字段语义（code 从 HTTP 状态改为业务码）- 破坏兼容
     *   向前兼容（新客户端读旧响应）：
     *   [x] 新客户端对新增字段提供默认值 - 需设计
     *   [x] 新客户端容忍字段缺失 - 需设计
     *   建议：新增字段用 @JsonInclude(NON_NULL)，旧响应无此字段，新客户端给默认值
     *
     * 三、契约测试实践
     *   1) JSON Schema 校验：
     *      定义 Schema（JSON Schema Draft 07）：
     *      {"type":"object","properties":{
     *        "code":{"type":"integer"},"message":{"type":"string"},
     *        "data":{"type":["string","null"]},"path":{"type":"string"}},
     *       "required":["code","message","path"]}
     *      测试：用 json-schema-validator 校验响应符合 Schema
     *      好处：一次定义，多处校验（测试/网关/前端）
     *   2) 快照测试（Snapshot Testing）：
     *      首次运行记录响应 JSON 快照，后续运行比对
     *      响应变化时测试失败，人工确认是否预期变更
     *      工具：jest（JS）/ 自定义 JSON 比对
     *   3) 消费者驱动契约（Pact）：
     *      消费方定义期望的响应结构 -> 提供方验证满足
     *      微服务场景最有效（详见 MessApplicationTests 中的 CDC 说明）
     *
     * 四、泛型擦除的契约影响
     *   ApiResponse<String> 和 ApiResponse<UserDto> 运行时类型相同（擦除）
     *   对契约的影响：
     *   1) 序列化不受影响：Jackson 运行时能拿到实际对象的类
     *   2) 反序列化受影响：不知道 data 该转成什么类型（变 LinkedHashMap）
     *   3) 契约文档需显式声明 data 的类型：
     *      OpenAPI: ApiResponse<String> -> data: { type: string }
     *      OpenAPI: ApiResponse<UserDto> -> data: { $ref: UserDto }
     *   4) TypeScript 前端：ApiResponse<UserDto> 映射为
     *      interface ApiResponse<T> { code: number; data: T; ... }
     *      泛型参数 T 在 TS 侧保留（编译期类型检查）
     *   5) 本测试用 constructParametricType 显式指定泛型，
     *      验证反序列化契约的完整性（data 正确还原为 String 而非 LinkedHashMap）
     * =========================================================================
     */
}