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
     * 【运维手册】启动类相关排障与配置调优（非可执行代码）
     * =========================================================================
     *
     * 1. 启动失败：端口被占用
     *    现象：Web server failed to start. Port 8080 was already in use.
     *    排查：lsof -i :8080 找到占用进程；或改 server.port=8081 临时绕开。
     *
     * 2. 启动失败：数据源未配置
     *    现象：Failed to determine a suitable driver class。
     *    原因：classpath 有 JPA 依赖但未配 spring.datasource.url。
     *    解决：提供数据源配置，或排除 DataSourceAutoConfiguration。
     *
     * 3. 启动成功但接口 404
     *    原因：启动类不在根包，组件扫描未覆盖 Controller。
     *    解决：把启动类移到最外层包；或用 @ComponentScan 指定 basePackages。
     *
     * 4. 启动慢
     *    可能原因：自动配置扫描范围大、JPA 初始化建表、DevTools 远程重启。
     *    调优：用 spring-boot-startup-report 插件分析各 Bean 初始化耗时。
     *
     * 5. @EnableCaching 去掉会怎样
     *    所有 @Cacheable/@CacheEvict 静默失效，每次请求都走数据库。
     *    不会报错，但性能退化为无缓存状态，排查时容易被忽略。
     *
     * 6. 常用启动参数
     *    --server.port=9090            指定端口
     *    --spring.profiles.active=prod 激活生产配置
     *    --debug                       打印自动配置报告（哪些生效/哪些被排除）
     *    --spring.jmx.enabled=false    关闭 JMX 减少启动开销
     * =========================================================================
     */

    /*
     * =========================================================================
     * 【补充手册】配置优先级与 Profile 速查（非可执行代码）
     * =========================================================================
     *
     * 一、配置加载优先级（高 -> 低）
     *   1. 命令行参数 --key=value（最高）
     *   2. 系统属性 -Dkey=value
     *   3. OS 环境变量
     *   4. application-{profile}.yml/properties
     *   5. application.yml/properties（最低）
     *   后加载的不会覆盖先加载的（高优先级生效）
     *
     * 二、Profile 机制
     *   spring.profiles.active=dev,prod
     *   application-dev.yml   —— 开发环境配置
     *   application-prod.yml  —— 生产环境配置
     *   application-test.yml  —— 测试环境配置
     *   @Profile("dev") 标注的 Bean 只在 dev profile 下激活
     *
     * 三、常见配置项速查
     *   server.port=8080                        端口
     *   spring.datasource.url=jdbc:mysql://..   数据源
     *   spring.jpa.hibernate.ddl-auto=none      DDL 策略
     *   spring.jpa.show-sql=true                打印 SQL
     *   logging.level.com.example=DEBUG         日志级别
     *   management.endpoints.web.exposure=*     Actuator 端点暴露
     *
     * 四、配置热更新
     *   @Value 默认启动时注入一次，配置中心变更不生效
     *   加 @RefreshScope（Spring Cloud）后，/actuator/refresh 触发重新注入
     *   @ConfigurationProperties Bean 整体可被刷新，比 @Value 更适合批量绑定
     * =========================================================================
     */
}