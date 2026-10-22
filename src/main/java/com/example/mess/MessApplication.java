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
     * 【Spring 内幕】SpringApplication.run() 引导流程与 IoC 容器创建（非可执行代码）
     * =========================================================================
     *
     * 一、run() 方法背后发生了什么（Spring Boot 3.x 源码顺序）
     *   本类 main() 只有一行 SpringApplication.run()，但它执行了十二个关键阶段：
     *   1) new SpringApplication(...)：构造推断
     *      - 推断应用类型：类路径有 DispatcherServlet/Servlet 标记 -> SERVLET，
     *        有 WebFlux 标记 -> REACTIVE，否则 NONE
     *      - 从 spring.factories 加载 7 个 ApplicationContextInitializer
     *      - 加载 9 个 ApplicationListener（环境后置、日志、配置文件监听）
     *   2) SpringFactoriesLoader 机制：读取 META-INF/spring.factories 中的
     *      org.springframework.context.ApplicationContextInitializer=xxx
     *      按全限定类名实例化，这就是 Spring Boot 的 SPI 加载器
     *   3) run(args) 开始引导：
     *      a. 创建 StopWatch 记录启动耗时（结束前打印 Started in x.xxx seconds）
     *      b. 获取 SpringApplicationRunListeners 并发布 ApplicationStartingEvent
     *         （LogStartupInfoListener 打印 banner、StartingInfoListener 输出 PID）
     *      c. 准备 Environment：读取 application.yml -> application-profile.yml ->
     *         命令行参数 -> 系统属性 -> OS 环境变量（ConfigFileApplicationListener 处理）
     *      d. 创建 ApplicationContext：SERVLET 类型创建
     *         AnnotationConfigServletWebServerApplicationContext
     *      e. prepareContext：BeanDefinitionLoader 加载 MessApplication 本身为
     *         BeanDefinition，发布 ApplicationPreparedEvent
     *      f. refreshContext：核心，见下方第二节
     *      g. afterRefresh：钩子（默认空实现）
     *      h. StopWatch.stop() 并发布 ApplicationStartedEvent
     *      i. 调用所有 ApplicationRunner / CommandLineRunner（有序执行）
     *      j. 发布 ApplicationReadyEvent -> 应用正式对外提供服务
     *
     * 二、IoC 容器 refresh() 的十三步（AbstractApplicationContext.refresh）
     *   第 5 步 invokeBeanFactoryPostProcessors 是自动装配的发动机：
     *   - ConfigurationClassPostProcessor 处理 @SpringBootApplication
     *   - @EnableAutoConfiguration 的 @Import(AutoConfigurationImportSelector)
     *     读取 META-INF/spring/org.springframework.boot.autoconfigure.
     *     AutoConfiguration.imports（Boot 2.7+ 用新文件名，旧名已废弃）
     *   - 按 @ConditionalOnClass / @ConditionalOnMissingBean 等条件过滤，
     *     本项目被装配的典型自动配置：
     *     * DataSourceAutoConfiguration -> H2 内存数据源（dev profile）
     *     * JpaRepositoriesAutoConfiguration -> 激活 UserRepository 为 BeanDefinition
     *     * SecurityAutoConfiguration -> 生成 SecurityFilterChain
     *     * CacheAutoConfiguration -> 使 @EnableCaching 生效（simple 并发缓存）
     *   第 6 步 registerBeanPostProcessors：注册后置处理器到 BeanFactory
     *   第 9 步 onRefresh：ServletWebServerApplicationContext 创建 Tomcat，
     *     绑定 8080 端口，注册 DispatcherServlet 到 ServletContext
     *   第 11 步 finishBeanFactoryInitialization：实例化所有非懒加载单例
     *     （Bean 生命周期见 UserService 文件的注释块）
     *   refresh() 完成后 IoC 容器就绪，Tomcat 已监听端口
     *
     * 三、为什么不建议在静态 main 中直接 new ApplicationContext()
     *   - SpringApplication 封装了：banner、环境准备、事件发布、Runner 调用、
     *     异常诊断（FailureAnalyzers 把堆栈翻译成人话）、关闭钩子注册
     *   - 手动 new 会丢失以上能力，且无法优雅停机（server.shutdown=graceful 失效）
     *
     * 四、启动失败的诊断层：FailureAnalyzer
     *   - 启动过程中抛出的异常先被 SpringApplication 交给 FailureAnalyzers（从
     *     spring.factories 加载，如 NoUniqueBeanFailureAnalyzer），把诸如
     *     注解同名 bean 冲突翻译成四段式人话：
     *     Description / Action / 报告类名，比裸堆栈更易排查
     *   - 生产 CI 里保留启动日志剖面，可对比两次启动的 BeanDefinition 注册
     *     列表与 Condition 评估报告，尽早发现依赖升级带来的自动装配漂移
     *   - 开启 debug=true 可输出 ConditionEvaluationReport，逐条列出
     *     自动配置类的匹配/不匹配原因（OnClassCondition / OnBeanCondition）
     *   - 关闭诊断会牺牲可观测性，建议永远保留 FailureAnalyzers 注册表
     *
     * 六、Spring Boot 3.x 的 AOT 与 GraalVM 原生镜像（启动流程的进化方向）
     *   1) 传统运行时：反射 + 动态代理 + spring.factories 扫描，启动慢但灵活
     *   2) AOT（Ahead-of-Time）处理：构建期把 BeanGraph 的反射元数据、
     *      代理类、资源配置全部生成为 Java 源码或注册文件，
     *      启动时无需扫描 -> 容器起步从秒级降到毫秒级
     *   3) 开启方式：spring-boot-maven-plugin 的 process-aot goal，
     *      或 native-maven-plugin 的 native:compile 产出原生可执行文件
     *   4) 代价：反射受限（需注册反射元数据）、关闭动态代理（CGLIB 受限）、
     *      不支持运行时改配置拼接类 -> 适合云原生短生命周期服务
     *   5) 本项目的兼容性评估：
     *      - @SpringBootApplication / @EnableCaching 都是官方支持注解
     *      - CGLIB 代理（SecurityConfig Full 模式）已有 AOT 替代
     *      - H2/Spring Data JPA 需确认 Hibernate 对原生镜像的支持
     *      结论：本项目结构简单，改造成本低，可作为 AOT 试点
     * =========================================================================
     */
}