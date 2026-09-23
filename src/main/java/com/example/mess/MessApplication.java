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
     * 【ADR-001】启动类位置与组件扫描策略
     * =========================================================================
     * 上下文：Spring Boot 应用需要自动发现并注册所有组件（Controller/Service/
     *         Repository/Config），组件扫描的根由启动类所在包决定。
     * 决策：将启动类放在最外层包 com.example.mess，让 @ComponentScan 递归覆盖
     *       所有子包（controller/service/repository/dto/config/exception）。
     * 替代方案：
     *   A) 启动类放子包 + @ComponentScan(basePackages="com.example.mess")
     *      —— 可行但冗余，默认行为已满足。
     *   B) 启动类放子包不加配置 —— 组件漏扫，Bean 注入失败。
     * 后果：所有子包组件自动注册，无需额外配置；新增子包无需修改启动类。
     *       但需注意：第三方 jar 中的组件需 @ComponentScan 显式指定包路径。
     *
     * =========================================================================
     * 【代码审查要点】启动类相关
     * =========================================================================
     * [ ] 启动类在最外层包，不在子包中
     * [ ] @SpringBootApplication 未被拆分为多余的单注解（无必要）
     * [ ] @EnableCaching 存在且未被误删（删掉后缓存静默失效，不报错）
     * [ ] main 方法只有 SpringApplication.run，不含业务初始化逻辑
     * [ ] 初始化逻辑放在 CommandLineRunner / ApplicationRunner 中
     * [ ] 无硬编码配置值（用 @Value 或 @ConfigurationProperties）
     * [ ] 新增子包后启动类无需修改（自动扫描覆盖）
     * =========================================================================
     */

    /*
     * =========================================================================
     * 【ADR-001-S】自动配置与排障策略（补充）
     * =========================================================================
     * 上下文：Spring Boot 自动配置"约定优于配置"，但"不生效"时排查困难。
     * 决策：使用 --debug 启动参数查看自动配置报告，定位"为什么 Bean 没装配"。
     * 替代方案：
     *   A) 不排障，直接手动 @Bean —— 绕过自动配置，失去约定优势。
     *   B) 看源码猜 —— 效率低，容易猜错。
     * 后果：--debug 输出 Positive/Negative/Unconditional 三类配置，
     *       精确定位哪个自动配置类生效/被排除及原因。
     *
     * 常见启动报错与排查：
     *   端口被占用：lsof -i :8080 找占用进程，或改 server.port
     *   数据源未配：缺 spring.datasource.url，或排除 DataSourceAutoConfiguration
     *   Bean 冲突：同类型多个 Bean，用 @Primary 或 @Qualifier 消歧
     *   循环依赖：A 依赖 B、B 依赖 A，用 @Lazy 打破或重构
     *   组件漏扫：启动类不在最外层包，或 @ComponentScan 范围不够
     *
     * 排除不需要的自动配置：
     *   @SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
     *   或 spring.autoconfigure.exclude=xxx
     *   减少启动时间和内存占用。
     *
     * 配置优先级（高 -> 低）：
     *   1. 命令行参数 --key=value
     *   2. JVM 系统属性 -Dkey=value
     *   3. OS 环境变量
     *   4. application-{profile}.yml
     *   5. application.yml
     * =========================================================================
     */
}