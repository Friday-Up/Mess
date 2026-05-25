package com.example.mess.controller;

import com.example.mess.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "基础接口", description = "基础问候接口")
public class HelloController {

    @GetMapping("/")
    @Operation(summary = "首页", description = "返回欢迎信息")
    public ApiResponse<String> index() {
        return ApiResponse.success("Hello, Spring Boot!");
    }

    @GetMapping("/hello")
    @Operation(summary = "问候接口", description = "根据名称返回问候语")
    public ApiResponse<String> hello(@RequestParam(defaultValue = "World") String name) {
        return ApiResponse.success("Hello, " + name + "!");
    }
}