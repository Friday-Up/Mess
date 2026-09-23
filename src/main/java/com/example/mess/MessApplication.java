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
     * =========================================================================
     * 【技术债务】TD-001 启动类相关
     * =========================================================================
     *
     * TD-001-1: 无健康检查端点
     *   现状：启动后无法快速确认应用是否就绪
     *   影响：部署后需人工访问接口验证，CI/CD 流水线无法自动判断健康
     *   优先级：P2
     *   修复方案：引入 spring-boot-starter-actuator，暴露 /actuator/health
     *   预估工时：0.5d
     *
     * TD-001-2: 无优雅停机
     *   现状：kill 进程时正在处理的请求会被中断
     *   影响：滚动更新时可能出现 5xx
     *   优先级：P3
     *   修复方案：server.shutdown=graceful + lifecycle timeout
     *   预估工时：0.5d
     *
     * TD-001-3: 配置硬编码风险
     *   现状：部分配置值可能硬编码在代码中
     *   影响：换环境需改代码重新部署
     *   优先级：P2
     *   修复方案：所有配置外部化到 application-{profile}.yml 或环境变量
     *   预估工时：1d
     *
     * =========================================================================
     * 【重构路线图】启动类演进方向
     * =========================================================================
     * Phase 1（当前）：单体启动，@EnableCaching，基本配置
     * Phase 2：引入 Actuator 健康检查 + 优雅停机 + 配置外部化
     * Phase 3：如需微服务化，拆分启动类 + 引入配置中心（Nacos/Apollo）
     * =========================================================================
     */
}