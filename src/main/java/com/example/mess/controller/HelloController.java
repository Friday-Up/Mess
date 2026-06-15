package com.example.mess.controller;

import com.example.mess.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

/**
 * 问候控制器 - 提供简单的问候API接口
 * 
 * 用于演示Spring Boot的基本功能，包括：
 * - RESTful API设计
 * - 请求参数处理
 * - 统一响应格式
 * - Swagger文档集成
 * - 日志记录
 * 
 * 核心注解说明:
 * 
 * @RestController:
 *   组合注解，等同于@Controller + @ResponseBody
 *   - @Controller: 标识为Spring MVC控制器
 *   - @ResponseBody: 返回值直接序列化为JSON（跳过视图解析）
 *   适用于RESTful API，所有方法返回JSON数据
 * 
 * @RequestMapping("/hello"):
 *   定义控制器的基础请求路径
 *   所有方法的路径都以此为前缀
 *   例如: /hello, /hello/advanced
 * 
 * @Tag(name = "问候服务", description = "..."):
 *   Swagger/OpenAPI文档分组标签
 *   在Swagger UI中将此控制器的API归为"问候服务"组
 *   便于API文档的分组和导航
 * 
 * API端点概览:
 * - GET  /hello          - 简单问候，返回"Hello, {name}!"
 * - GET  /hello/advanced - 高级问候，支持版本号参数
 * - POST /hello          - POST方式问候
 * 
 * 权限配置:
 * - 所有问候API均为公开访问（无需认证）
 * - 在SecurityConfig中配置: /hello/** -> permitAll()
 * 
 * 日志策略:
 * - 每个请求都记录INFO级别日志
 * - 包含请求参数信息，便于调试和审计
 * 
 * 作者: zhangyaolong.5
 * 创建时间: 2026-05-26
 * 
 * @see com.example.mess.dto.ApiResponse 统一响应格式
 * @see com.example.mess.config.SecurityConfig 安全配置
 */
@RestController
@RequestMapping("/hello")
@Tag(name = "问候服务", description = "简单的问候API，用于测试和演示")
public class HelloController {

    /**
     * SLF4J日志记录器
     * 用于记录请求信息和调试日志
     * 
     * 日志级别:
     * - ERROR: 严重错误，需要立即处理
     * - WARN: 警告信息，可能存在问题
     * - INFO: 一般信息，记录关键操作（当前使用）
     * - DEBUG: 调试信息，详细的执行流程
     * - TRACE: 追踪信息，最详细的日志
     */
    private static final Logger log = LoggerFactory.getLogger(HelloController.class);

    /**
     * 简单的问候接口
     * 
     * 根据传入的名称返回个性化的问候语。
     * 如果不传name参数，默认问候"World"。
     * 
     * 请求示例:
     * - GET /hello              -> "Hello, World!"
     * - GET /hello?name=Spring  -> "Hello, Spring!"
     * - GET /hello?name=张三    -> "Hello, 张三!"
     * 
     * 响应格式:
     * {
     *   "success": true,
     *   "message": "Success",
     *   "data": "Hello, World!",
     *   "timestamp": "2026-05-26T10:30:00.000000"
     * }
     * 
     * @param name 问候对象的名称，默认为"World"
     * @return ApiResponse<String> 包含问候语的统一响应对象
     * 
     * HTTP方法: GET
     * 访问路径: /hello
     * 权限要求: 公开访问（无需认证）
     */
    @GetMapping
    @Operation(
        summary = "简单问候", 
        description = "根据名称返回问候语，name参数可选，默认为'World'"
    )
    public ApiResponse<String> hello(@RequestParam(defaultValue = "World") String name) {
        // 构建并返回个性化的问候语
        String greeting = String.format("Hello, %s!", name);
        
        // 记录请求日志，包含问候对象名称
        log.info("收到问候请求，问候对象: {}", name);
        
        // 使用ApiResponse包装返回结果，确保统一的响应格式
        return ApiResponse.success(greeting);
    }

    /**
     * 高级问候接口
     * 
     * 根据名称和API版本号返回不同格式的问候语。
     * 版本1.0返回标准格式，版本2.0返回增强格式。
     * 
     * 版本差异:
     * - v1.0: "Hello, {name}! (API v1.0)" - 标准格式
     * - v2.0: "Hello, {name}! Welcome to API v2.0" - 增强格式
     * - 其他: 与v1.0相同
     * 
     * 请求示例:
     * - GET /hello/advanced                          -> "Hello, World! (API v1.0)"
     * - GET /hello/advanced?name=Spring              -> "Hello, Spring! (API v1.0)"
     * - GET /hello/advanced?name=Spring&version=2.0  -> "Hello, Spring! Welcome to API v2.0"
     * 
     * API版本设计:
     * - 通过查询参数传递版本号（非URL路径版本）
     * - 适用于小范围的功能变更
     * - 大范围变更建议使用URL路径版本（如/v2/hello）
     * 
     * @param name 问候对象的名称，默认为"World"
     * @param version API版本号，默认为"1.0"
     * @return ApiResponse<String> 包含高级问候语的统一响应对象
     * 
     * HTTP方法: GET
     * 访问路径: /hello/advanced
     * 权限要求: 公开访问（无需认证）
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
            // 版本2.0使用增强格式
            greeting = String.format("Hello, %s! Welcome to API v%s", name, version);
        } else {
            // 其他版本使用标准格式
            greeting = String.format("Hello, %s! (API v%s)", name, version);
        }
        
        // 记录请求日志，包含问候对象名称和版本号
        log.info("收到高级问候请求，问候对象: {}, 版本: {}", name, version);
        
        return ApiResponse.success(greeting);
    }

    /**
     * POST方式的问候接口
     * 
     * 通过POST方法发送问候请求，与GET接口功能类似但使用不同的HTTP方法。
     * 主要用于演示POST请求的处理方式。
     * 
     * GET vs POST选择:
     * - GET: 幂等操作，不修改服务器状态，可缓存
     * - POST: 非幂等操作，可能修改服务器状态，不可缓存
     * - 问候操作本身是幂等的，使用GET更符合RESTful规范
     * - 此POST接口仅用于演示目的
     * 
     * 请求示例:
     * - POST /hello              -> "Hello, World! (via POST)"
     * - POST /hello?name=SpringBoot -> "Hello, SpringBoot! (via POST)"
     * 
     * @param name 问候对象的名称，默认为"World"
     * @return ApiResponse<String> 包含问候语的统一响应对象（标注POST来源）
     * 
     * HTTP方法: POST
     * 访问路径: /hello
     * 权限要求: 公开访问（无需认证）
     */
    @PostMapping
    @Operation(
        summary = "POST问候", 
        description = "通过POST请求发送问候"
    )
    public ApiResponse<String> helloPost(@RequestParam(defaultValue = "World") String name) {
        // 构建问候语，标注为POST方式发送
        String greeting = String.format("Hello, %s! (via POST)", name);
        
        // 记录POST请求日志
        log.info("收到POST问候请求，问候对象: {}", name);
        
        return ApiResponse.success(greeting);
    }
}