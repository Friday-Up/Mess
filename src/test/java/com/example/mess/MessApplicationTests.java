package com.example.mess;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * 应用上下文加载测试 - 验证Spring Boot能否正常启动
 */
@SpringBootTest
@ActiveProfiles("test")
class MessApplicationTests {

    /** 验证应用上下文成功加载（失败时自动抛出异常） */
    @Test
    void contextLoads() {
    }

    /** 应用健康检查占位测试 */
    @Test
    void applicationHealthCheck() {
    }
}