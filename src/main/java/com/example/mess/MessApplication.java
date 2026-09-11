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
     * 【阅读笔记】启动类注解拆解与启动流程（非可执行代码）
     * ============================================================================
     *
     * 一、@SpringBootApplication 是三个注解的组合
     * ----------------------------------------------------------------------------
     *   @SpringBootConfiguration
     *       —— 本质是 @Configuration，声明这是配置类，可定义 @Bean;
     *   @EnableAutoConfiguration
     *       —— 启用自动配置：依据 classpath 依赖与已有 Bean，
     *          从 spring.factories / AutoConfiguration.imports 中筛选并装配配置类;
     *   @ComponentScan
     *       —— 扫描当前包及其子包下的 @Component/@Service/@Repository/@Controller。
     *
     *   关键推论：启动类所在包 = 组件扫描的根。若把启动类放到深层子包，
     *   会漏扫兄弟包中的 Bean，导致"明明写了注解却注入不进来"。
     *
     * 二、启动流程（简化）
     * ----------------------------------------------------------------------------
     *   1) 创建 SpringApplication，推断应用类型（Servlet / Reactive / None）;
     *   2) 加载 ApplicationContextInitializer 与 ApplicationListener（SPI 机制）;
     *   3) 准备 Environment：读取命令行参数 > 系统属性 > 环境变量 >
     *      application-{profile}.yml > application.yml 等，后者优先级更低;
     *   4) 创建并刷新 ApplicationContext：
     *      BeanDefinition 注册 -> 实例化 -> 依赖注入 -> 初始化 -> 后置处理器;
     *   5) 启动内嵌 Web 容器（默认 Tomcat），注册 DispatcherServlet;
     *   6) 发布 ApplicationReadyEvent，此时 @PostConstruct / CommandLineRunner 已执行。
     *
     * 三、@EnableCaching 的作用
     * ----------------------------------------------------------------------------
     *   开启基于注解的缓存基础设施，为 @Cacheable/@CacheEvict 提供代理支持。
     *   若去掉该注解，缓存注解会静默失效（不报错但每次都查库），
     *   这是排查"缓存没生效"时最常被忽略的一环。
     *
     * 四、自定义启动行为的三种入口
     * ----------------------------------------------------------------------------
     *   - ApplicationRunner / CommandLineRunner：启动后执行业务初始化;
     *   - ApplicationContextInitializer：上下文 refresh 前调整配置;
     *   - EnvironmentPostProcessor：比 Initializer 更早介入配置加载。
     *
     * 五、常见启动报错定位思路
     * ----------------------------------------------------------------------------
     *   - BeanCreationException：顺着 caused by 找第一个业务类;
     *   - Port already in use：端口被占用，改 server.port 或结束占用进程;
     *   - Failed to configure a DataSource：缺数据库依赖或配置。
     * ============================================================================
     */
}