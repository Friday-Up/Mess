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
         * 【JVM内存管理与GC调优】启动期类加载与 JVM 启动参数（非可执行代码）
         * =========================================================================
         *
         * 一、Spring Boot 启动在 JVM 层做了什么
         *   1) main 开始后：引导类加载器加载 rt.jar 基础类 ->
         *      AppClassLoader 加载应用类 -> spring.factories 驱动大量类加载
         *   2) 启动慢的常见元凶：类加载量大（Spring 全家桶数万类）+
         *      Bean 初始化顺序深 + @ComponentScan 范围过宽
         *   3) 本类 @SpringBootApplication 默认扫描本包及子包：
         *      包目录越深、类越多，类加载与 Bean 扫描越慢——启动优化第一步是收敛扫描范围
         *
         * 二、启动优化相关的 JVM 参数与工具
         *   1) -XX:+TieredCompilation + -XX:TieredStopAtLevel=1：
         *      只用 C1 编译器，启动快 20-40%（牺牲峰值吞吐，适合短任务/CLI）
         *   2) -Xverify:none / -noverify：跳过字节码校验（省启动时间，
         *      但有安全风险，仅开发环境慎用）
         *   3) Spring Boot 3+ 的 CDS（Class Data Sharing）：
         *      -XX:SharedArchiveFile=app.jsa 归档已加载类元数据，二次启动跳过解析
         *   4) 检查你的启动瓶颈：-Xlog:class+load 对比类加载量；
         *      JFR 记录 Startup 事件定位耗时 Bean
         * 三、main 线程与启动期 Full GC
         *   1) main 线程完成初始化后把控制权交给 Tomcat 工作线程：
         *      之后 GC 事件多在工作线程触发——jstack 看 Web 线程而非 main
         *   2) 启动期 Full GC 是危险信号：Metaspace 类加载爆炸 / Bean 初始化
         *      过早触发大对象晋升——启动 GC 日志必看，它是配置错误的最早信号
         *
         =========================================================================
         */
}