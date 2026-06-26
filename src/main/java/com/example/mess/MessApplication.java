package com.example.mess;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Mess应用入口 - @SpringBootApplication(自动配置+组件扫描) + @EnableCaching(启用缓存)
 */
@SpringBootApplication
@EnableCaching
public class MessApplication {

    public static void main(String[] args) {
        SpringApplication.run(MessApplication.class, args);
    }
}