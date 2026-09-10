package com.example.mess;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Mess应用入口类 - Spring Boot应用的启动点和全局配置中心。
 *
 * <p>核心注解说明:
 * <ul>
 *   <li>@SpringBootApplication: 组合注解，包含以下三个注解的功能:
 *     <ul>
 *       <li>@SpringBootConfiguration - 标识为配置类，等价于@Configuration</li>
 *       <li>@EnableAutoConfiguration - 根据classpath自动配置Spring组件（数据源、缓存等）</li>
 *       <li>@ComponentScan - 自动扫描当前包及子包下的@Component/@Service/@Repository等</li>
 *     </ul>
 *   </li>
 *   <li>@EnableCaching: 启用Spring Cache抽象层，使@Cacheable/@CacheEvict等注解生效。
 *     缓存实现由spring.cache.type配置决定（simple为内存缓存，redis为Redis缓存）</li>
 * </ul>
 *
 * <p>启动流程: main() → SpringApplication.run() → 创建ApplicationContext →
 * 自动配置Bean → 启动内嵌Tomcat(8080) → 应用就绪
 *
 * @since 1.0
 */
@SpringBootApplication
@EnableCaching
public class MessApplication {

    /**
     * 应用入口方法。SpringApplication.run()负责引导整个Spring Boot应用启动，
     * 包括创建ApplicationContext、自动配置、启动内嵌Servlet容器等。
     *
     * @param args 命令行参数，可覆盖application.yml中的配置项
     */
    public static void main(String[] args) {
        SpringApplication.run(MessApplication.class, args);
    }

    /*
     * ============================================================================
     * 【设计文档】MessApplication 启动类与工程结构说明（补充文档，非可执行代码）
     * ============================================================================
     *
     * 一、启动类的作用
     * ----------------------------------------------------------------------------
     * MessApplication 是整个 Spring Boot 应用的引导入口，@SpringBootApplication
     * 触发自动配置与组件扫描，SpringApplication.run 完成上下文构建、Bean 装配、
     * 内嵌容器启动等一系列引导动作，是"约定优于配置"理念的集中体现。
     *
     * 二、包结构约定（组件扫描的隐含契约）
     * ----------------------------------------------------------------------------
     *   com.example.mess            —— 启动类所在根包，扫描起点;
     *   com.example.mess.controller —— 表现层（Controller）;
     *   com.example.mess.service    —— 业务层（Service）;
     *   com.example.mess.repository —— 数据访问层（Repository）;
     *   com.example.mess.entity     —— 持久化实体（Entity）;
     *   com.example.mess.dto        —— 数据传输对象与统一响应（DTO/ApiResponse）;
     *   com.example.mess.config     —— 配置类（SecurityConfig 等）;
     *   com.example.mess.exception  —— 异常与全局处理器。
     * 由于 @ComponentScan 默认扫描启动类所在包及子包，务必保证业务类位于该包树下。
     *
     * 三、关键注解决策
     * ----------------------------------------------------------------------------
     * 决策 1：@EnableCaching 置于启动类
     *   理由：缓存是全局横切能力，在入口开启使 @Cacheable 等注解全局生效;
     *        具体缓存实现由 spring.cache.type 决定（simple 内存 / redis 分布式）。
     * 决策 2：启动类保持"薄"，不写业务
     *   理由：入口只负责引导，业务应分散到各分层组件，利于测试与维护。
     *
     * 四、启动流程速览
     * ----------------------------------------------------------------------------
     *   main → SpringApplication.run → 创建 ApplicationContext
     *        → 执行自动配置（数据源/JPA/缓存/安全等）
     *        → 扫描并注册 Bean → 启动内嵌 Tomcat(8080) → 应用就绪。
     *
     * 五、运行与配置提示
     * ----------------------------------------------------------------------------
     *   - 命令行参数可覆盖 application.yml 配置（如 --server.port=9090）;
     *   - 多环境通过 spring.profiles.active 切换（如 test/dev/prod）;
     *   - 测试用 @SpringBootTest 加载完整上下文，@ActiveProfiles 指定测试环境。
     * ============================================================================
     */
}