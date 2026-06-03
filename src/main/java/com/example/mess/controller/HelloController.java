package com.example.mess.controller;

import com.example.mess.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

/**
 * 问候控制器
 * 提供简单的问候API接口，用于演示基本的Spring Boot功能
 * 
 * @RestController 组合注解，包含@Controller和@ResponseBody
 * @RequestMapping 定义基础路径为/hello
 * @Tag Swagger文档标签，用于API分组
 * 
 * 作者: zhangyaolong.5
 * 创建时间: 2026-05-26
 */
@RestController
@RequestMapping("/hello")
@Tag(name = "问候服务", description = "简单的问候API，用于测试和演示")
public class HelloController {

    private static final Logger log = LoggerFactory.getLogger(HelloController.class);

    /**
     * 简单的问候接口
     * 
     * @param name 问候对象的名称，默认为"World"
     * @return ApiResponse<String> 包含问候语的响应
     * 
     * HTTP方法: GET
     * 访问路径: /hello
     * 权限要求: 公开访问
     */
    @GetMapping
    @Operation(
        summary = "简单问候", 
        description = "根据名称返回问候语，name参数可选，默认为'World'"
    )
    public ApiResponse<String> hello(@RequestParam(defaultValue = "World") String name) {
        // 构建并返回个性化的问候语
        String greeting = String.format("Hello, %s!", name);
        
        log.info("收到问候请求，问候对象: {}", name);
        
        return ApiResponse.success(greeting);
    }

    /**
     * 高级问候接口
     * 
     * @param name 问候对象的名称
     * @param version API版本号
     * @return ApiResponse<String> 包含高级问候语的响应
     * 
     * HTTP方法: GET
     * 访问路径: /hello/advanced
     * 权限要求: 公开访问
     */
    @GetMapping("/advanced")
    @Operation(
        summary = "高级问候", 
        description = "根据名称和版本返回高级问候语"
    )
    public ApiResponse<String> advancedHello(
            @RequestParam(defaultValue = "World") String name,
            @RequestParam(defaultValue = "1.0") String version) {
        
        // 根据版本号构建不同格式的问候语
        String greeting;
        if ("2.0".equals(version)) {
            greeting = String.format("Hello, %s! Welcome to API v%s", name, version);
        } else {
            greeting = String.format("Hello, %s! (API v%s)", name, version);
        }
        
        log.info("收到高级问候请求，问候对象: {}, 版本: {}", name, version);
        
        return ApiResponse.success(greeting);
    }

    /**
     * POST方式的问候接口
     * 
     * @param name 问候对象的名称
     * @return ApiResponse<String> 包含问候语的响应
     * 
     * HTTP方法: POST
     * 访问路径: /hello
     * 权限要求: 公开访问
     */
    @PostMapping
    @Operation(
        summary = "POST问候", 
        description = "通过POST请求发送问候"
    )
    public ApiResponse<String> helloPost(@RequestParam(defaultValue = "World") String name) {
        // 构建问候语
        String greeting = String.format("Hello, %s! (via POST)", name);
        
        log.info("收到POST问候请求，问候对象: {}", name);
        
        return ApiResponse.success(greeting);
    }
}