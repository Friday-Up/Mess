package com.example.mess;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * 应用主类
 * Spring Boot应用入口点
 * 
 * @SpringBootApplication 组合注解，包含@Configuration、@EnableAutoConfiguration、@ComponentScan
 * @EnableCaching 启用Spring缓存
 * 
 * 作者: zhangyaolong.5
 * 创建时间: 2026-05-26
 */
@SpringBootApplication
@EnableCaching
public class MessApplication {

    /**
     * 主方法，应用入口点
     * 
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(MessApplication.class, args);
    }
}