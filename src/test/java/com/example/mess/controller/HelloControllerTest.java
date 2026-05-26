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
 * 问候控制器测试类
 * 测试HelloController的所有API接口
 * 
 * @WebMvcTest 注解用于测试Web层，只加载控制器相关的组件
 * @ActiveProfiles 注解用于指定使用测试环境的配置
 * 
 * 作者: zhangyaolong.5
 * 创建时间: 2026-05-26
 * 
 * 功能说明:
 * - 测试所有问候API接口
 * - 验证响应格式和状态码
 * - 测试参数处理和默认值
 * 
 * 使用场景:
 * - 控制器层的单元测试
 * - API接口的功能验证
 * - 回归测试
 * 
 * 测试范围:
 * - GET /hello - 简单问候接口
 * - GET /hello/advanced - 高级问候接口
 * - POST /hello - POST问候接口
 */
@WebMvcTest(HelloController.class)
@ActiveProfiles("test")
class HelloControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 测试简单问候接口 - 默认参数
     * 
     * 测试场景: 不带参数调用/hello接口
     * 预期结果: 返回"Hello, World!"
     * 
     * HTTP方法: GET
     * 访问路径: /hello
     * 权限要求: 公开访问
     * 
     * 测试步骤:
     * 1. 发送GET请求到/hello
     * 2. 验证状态码为200
     * 3. 验证响应内容为"Hello, World!"
     * 4. 验证响应格式为ApiResponse
     * 
     * 边界条件:
     * - 无参数调用应使用默认值"World"
     * - 响应应为JSON格式
     * - 状态码应为200 OK
     */
    @Test
    void helloWithDefaultName() throws Exception {
        MvcResult result = mockMvc.perform(get("/hello")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        ApiResponse<String> response = objectMapper.readValue(responseBody, 
                objectMapper.getTypeFactory().constructParametricType(ApiResponse.class, String.class));

        assertTrue(response.isSuccess());
        assertEquals("Hello, World!", response.getData());
    }

    /**
     * 测试简单问候接口 - 自定义参数
     * 
     * 测试场景: 带name参数调用/hello接口
     * 预期结果: 返回"Hello, {name}!"
     * 
     * HTTP方法: GET
     * 访问路径: /hello?name=Spring
     * 权限要求: 公开访问
     * 
     * 测试步骤:
     * 1. 发送GET请求到/hello?name=Spring
     * 2. 验证状态码为200
     * 3. 验证响应内容为"Hello, Spring!"
     * 4. 验证响应格式为ApiResponse
     * 
     * 边界条件:
     * - 参数应正确传递和处理
     * - 特殊字符应正确处理
     * - 空参数应使用默认值
     */
    @Test
    void helloWithCustomName() throws Exception {
        MvcResult result = mockMvc.perform(get("/hello")
                .param("name", "Spring")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        ApiResponse<String> response = objectMapper.readValue(responseBody, 
                objectMapper.getTypeFactory().constructParametricType(ApiResponse.class, String.class));

        assertTrue(response.isSuccess());
        assertEquals("Hello, Spring!", response.getData());
    }

    /**
     * 测试高级问候接口 - 版本1.0
     * 
     * 测试场景: 调用/hello/advanced接口，使用默认版本
     * 预期结果: 返回"Hello, {name}! (API v1.0)"
     * 
     * HTTP方法: GET
     * 访问路径: /hello/advanced?name=Spring&version=1.0
     * 权限要求: 公开访问
     * 
     * 测试步骤:
     * 1. 发送GET请求到/hello/advanced?name=Spring&version=1.0
     * 2. 验证状态码为200
     * 3. 验证响应内容格式正确
     * 4. 验证响应格式为ApiResponse
     * 
     * 边界条件:
     * - 版本参数应正确处理
     * - 默认版本应为1.0
     * - 响应格式应包含版本信息
     */
    @Test
    void advancedHelloWithVersionOne() throws Exception {
        MvcResult result = mockMvc.perform(get("/hello/advanced")
                .param("name", "Spring")
                .param("version", "1.0")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        ApiResponse<String> response = objectMapper.readValue(responseBody, 
                objectMapper.getTypeFactory().constructParametricType(ApiResponse.class, String.class));

        assertTrue(response.isSuccess());
        assertEquals("Hello, Spring! (API v1.0)", response.getData());
    }

    /**
     * 测试高级问候接口 - 版本2.0
     * 
     * 测试场景: 调用/hello/advanced接口，使用版本2.0
     * 预期结果: 返回"Hello, {name}! Welcome to API v2.0"
     * 
     * HTTP方法: GET
     * 访问路径: /hello/advanced?name=Spring&version=2.0
     * 权限要求: 公开访问
     * 
     * 测试步骤:
     * 1. 发送GET请求到/hello/advanced?name=Spring&version=2.0
     * 2. 验证状态码为200
     * 3. 验证响应内容格式正确
     * 4. 验证响应格式为ApiResponse
     * 
     * 边界条件:
     * - 版本2.0应使用特殊格式
     * - 版本参数区分大小写
     * - 无效版本应回退到默认格式
     */
    @Test
    void advancedHelloWithVersionTwo() throws Exception {
        MvcResult result = mockMvc.perform(get("/hello/advanced")
                .param("name", "Spring")
                .param("version", "2.0")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        ApiResponse<String> response = objectMapper.readValue(responseBody, 
                objectMapper.getTypeFactory().constructParametricType(ApiResponse.class, String.class));

        assertTrue(response.isSuccess());
        assertEquals("Hello, Spring! Welcome to API v2.0", response.getData());
    }

    /**
     * 测试高级问候接口 - 默认参数
     * 
     * 测试场景: 调用/hello/advanced接口，不带参数
     * 预期结果: 返回"Hello, World! (API v1.0)"
     * 
     * HTTP方法: GET
     * 访问路径: /hello/advanced
     * 权限要求: 公开访问
     * 
     * 测试步骤:
     * 1. 发送GET请求到/hello/advanced
     * 2. 验证状态码为200
     * 3. 验证响应内容使用默认值
     * 4. 验证响应格式为ApiResponse
     * 
     * 边界条件:
     * - 无参数时应使用默认值
     * - name默认值为"World"
     * - version默认值为"1.0"
     */
    @Test
    void advancedHelloWithDefaultParameters() throws Exception {
        MvcResult result = mockMvc.perform(get("/hello/advanced")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        ApiResponse<String> response = objectMapper.readValue(responseBody, 
                objectMapper.getTypeFactory().constructParametricType(ApiResponse.class, String.class));

        assertTrue(response.isSuccess());
        assertEquals("Hello, World! (API v1.0)", response.getData());
    }

    /**
     * 测试POST问候接口
     * 
     * 测试场景: 通过POST方式调用/hello接口
     * 预期结果: 返回"Hello, {name}! (via POST)"
     * 
     * HTTP方法: POST
     * 访问路径: /hello?name=SpringBoot
     * 权限要求: 公开访问
     * 
     * 测试步骤:
     * 1. 发送POST请求到/hello?name=SpringBoot
     * 2. 验证状态码为200
     * 3. 验证响应内容包含POST标识
     * 4. 验证响应格式为ApiResponse
     * 
     * 边界条件:
     * - POST方法应正确处理
     * - 参数应通过查询字符串传递
     * - 响应应区别于GET方法
     */
    @Test
    void helloPost() throws Exception {
        MvcResult result = mockMvc.perform(post("/hello")
                .param("name", "SpringBoot")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        ApiResponse<String> response = objectMapper.readValue(responseBody, 
                objectMapper.getTypeFactory().constructParametricType(ApiResponse.class, String.class));

        assertTrue(response.isSuccess());
        assertEquals("Hello, SpringBoot! (via POST)", response.getData());
    }
}