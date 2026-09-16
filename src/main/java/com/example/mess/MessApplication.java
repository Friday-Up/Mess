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
     * 【面试问答】关于启动类的常见面试题（非可执行代码）
     * =========================================================================
     *
     * Q1: @SpringBootApplication 包含哪些注解？各自做什么？
     * A1: 三个核心注解的组合：
     *     - @SpringBootConfiguration: 等价 @Configuration，标识配置类，可定义 @Bean
     *     - @EnableAutoConfiguration: 根据 classpath 依赖自动装配 Bean
     *     - @ComponentScan: 扫描启动类所在包及子包下的组件
     *
     * Q2: 为什么启动类要放在最外层包？
     * A2: @ComponentScan 默认以启动类所在包为根，向子包递归扫描。
     *     若启动类在深层子包，兄弟包中的 Controller/Service 不会被扫描到，
     *     导致"明明加了注解却注入不进来"。
     *
     * Q3: SpringApplication.run() 内部做了什么？
     * A3: 简化版七步：
     *     1) 推断应用类型（Servlet/Reactive/None）
     *     2) 加载 spring.factories 中的 Initializer 和 Listener
     *     3) 准备 Environment（配置来源合并）
     *     4) 创建 ApplicationContext
     *     5) refreshContext：注册 BeanDefinition -> 实例化 -> 注入 -> 初始化
     *     6) 启动内嵌 Tomcat
     *     7) 发布 ApplicationReadyEvent，执行 Runner
     *
     * Q4: @EnableCaching 去掉会怎样？
     * A4: @Cacheable/@CacheEvict 全部静默失效，不会报错但每次都查库。
     *     这是"缓存没生效"最常被忽略的原因。
     *
     * Q5: 如何在启动后执行初始化逻辑？
     * A5: 三种方式（按执行时机排序）：
     *     - @PostConstruct: Bean 初始化阶段执行
     *     - CommandLineRunner / ApplicationRunner: 上下文刷新后执行
     *     - ApplicationListener<ApplicationReadyEvent>: 完全就绪后执行
     * =========================================================================
     */

    /*
     * =========================================================================
     * 【源码走读】自动配置加载机制（非可执行代码）
     * =========================================================================
     *
     * 一、自动配置的入口
     *    @EnableAutoConfiguration 通过 @Import(AutoConfigurationImportSelector)
     *    加载 META-INF/spring/org.springframework.boot.autoconfigure.
     *    AutoConfiguration.imports 文件中列出的配置类。
     *    （Spring Boot 3.x 用 AutoConfiguration.imports，
     *     旧版用 spring.factories 中的 EnableAutoConfiguration 键）
     *
     * 二、条件装配注解
     *    @ConditionalOnClass(DataSource.class)  —— classpath 有该类才装配
     *    @ConditionalOnMissingBean              —— 容器中没有该 Bean 才装配
     *    @ConditionalOnProperty(prefix, name)   —— 配置项满足条件才装配
     *    @ConditionalOnWebApplication           —— 是 Web 应用才装配
     *    这些注解决定了"有依赖就自动配，没依赖就跳过"。
     *
     * 三、配置优先级（高 -> 低）
     *    1. 命令行参数 --key=value
     *    2. JVM 系统属性 -Dkey=value
     *    3. OS 环境变量
     *    4. application-{profile}.yml
     *    5. application.yml
     *    高优先级覆盖低优先级，后者只补前者没有的。
     *
     * 四、排障工具
     *    --debug 启动时打印自动配置报告：
     *      Positive matches: 哪些配置类生效了
     *      Negative matches: 哪些被排除及原因
     *      Unconditional classes: 无条件加载的
     *    这是"为什么我的 Bean 没有被自动装配"的排查利器。
     *
     * 五、手动排除不需要的自动配置
     *    @SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
     *    或配置 spring.autoconfigure.exclude=xxx
     *    减少启动时间和内存占用。
     * =========================================================================
     */
}