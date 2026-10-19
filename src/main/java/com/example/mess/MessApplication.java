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
     * 【性能优化】启动性能与资源占用（非可执行代码）
     * =========================================================================
     *
     * 一、启动耗时优化
     *   1) 排除无用自动配置：
     *      @SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
     *      减少 Bean 扫描和初始化开销
     *   2) 懒加载：spring.main.lazy-initialization=true
     *      Bean 用到时才初始化，启动快但首次请求慢，适合开发环境
     *   3) 关闭 JMX：spring.jmx.enabled=false，减少 MBean 注册开销
     *   4) 分析启动瓶颈：--debug 查看自动配置报告，
     *      或引入 spring-boot-startup-report 插件量化各 Bean 初始化耗时
     *
     * 二、内存占用优化
     *   1) JVM 参数：-Xms256m -Xmx512m 限制堆大小，避免容器 OOM
     *   2) -XX:+UseG1GC：大堆场景下 G1 比 CMS 更稳定
     *   3) 线程池：server.tomcat.threads.max=200 控制线程数
     *   4) 连接池：spring.datasource.hikari.maximum-pool-size=10 控制连接数
     *
     * 三、容器化部署建议
     *   - JVM 堆大小设为容器内存的 50%-70%
     *   - 使用 -XX:MaxRAMPercentage=70.0 替代硬编码 -Xmx，自动适配容器内存
     *   - Dockerfile 用多阶段构建减小镜像体积
     *   - 健康探针：liveness=/actuator/health/liveness, readiness=/actuator/health/readiness
     *
     * =========================================================================
     * 【监控可观测性】启动阶段的关键指标
     * =========================================================================
     *   - 启动耗时：application.started.time（Micrometer 指标）
     *   - 就绪耗时：application.ready.time
     *   - JVM 内存：jvm.memory.used/committed/max
     *   - 线程数：jvm.threads.live/daemon/peak
     *   - GC 次数/耗时：jvm.gc.pause（P99 耗时需关注）
     *   接入 Micrometer + Prometheus + Grafana 可形成完整监控面板
     * =========================================================================
     */
}