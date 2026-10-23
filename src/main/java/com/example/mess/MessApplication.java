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
     * 【Java 并发】应用启动的线程模型与 @Async 线程池配置（非可执行代码）
     * =========================================================================
     *
     * 一、main 线程在启动阶段忙什么
     *   1) 应用入口的 main 线程依次执行：环境准备 -> 容器 refresh ->
     *      Bean 实例化 -> 内嵌 Tomcat 启动 -> Runner 回调，全程单线程串行
     *   2) refresh() 期间只有少数后台线程被拉起：
     *      - Tomcat 的 Acceptor 与 Poller 线程（onRefresh 阶段）
     *      - HikariPool 的 houseKeeper 与添加连接线程
     *      - 心跳/Nacos 注册等（若引入注册中心）
     *   3) 推论：启动慢通常不是线程争用，而是 Bean 过多或数据源初始化
     *      阻塞（如远程元数据库连不上、Schema 初始化 SQL 过重）
     *   4) main 线程在发布 ApplicationReadyEvent 后返回方法，JVM 不退出，
     *      因为 Tomcat 的非守护线程仍在工作（见了 HelloController 注释块）
     *
     * 二、Spring 的任务执行器（TaskExecutor）体系
     *   1) 启用 @EnableAsync 后，Spring 查找唯一的 TaskExecutor Bean：
     *      - 优先：名为 taskExecutor 且类型为 TaskExecutor 的 Bean
     *      - 次选：类型为 Executor 的 Bean
     *      - 兜底：SimpleAsyncTaskExecutor（每次 new Thread，生产勿用）
     *   2) 未显式配置时的默认：Spring Boot 自动装配 ThreadPoolTaskExecutor
     *      核心线程 8、最大 Integer.MAX_VALUE、队列 Integer.MAX_VALUE，
     *      看似慷慨，实则队列无上限 -> 任务积压会吃满内存，必须复写
     *   3) 生产推荐配置示例（ApplicationRunner 初始化阶段创建）：
     *      核心线程 = 2 * CPU 核；最大 = 核心线程 * 2；队列容量 = 200；
     *      拒绝策略 = CallerRunsPolicy（把压力回灌给调用方，自然限流）
     *   4) 线程命名：设置 ThreadNamePrefix=async- 以便枯日志排查时
     *      一眼区分 Tomcat 工作线程 pool-N 与异步线程 async-N
     *
     * 三、优雅停机与线程的排空
     *   1) server.shutdown=graceful 令 Spring 在收到 SIGTERM 后：
     *      - 停止接受新请求（连接不再进 Acceptor）
     *      - 等待正在执行的请求完成（最多 spring.lifecycle.timeout-per-shutdown-phase=30s）
     *   2) 停机也取决于线程类型：
     *      - 守护线程（Daemon=true）：JVM 不等它退出就直接结束
     *      - 非守护线程：JVM 会等其结束，未处理的长时间任务会拖住停机
     *   3) 本类的 SpringApplication.run() 已注册 shutdown hook（Runtime.addShutdownHook），
     *      SIGTERM 触发 hook -> 容器 close -> 各 Bean 销毁回调按依赖逆序执行
     *   4) 实务：K8s 的 terminationGracePeriodSeconds 要 >= 应用停机超时，
     *      否则 Pod 被强杀，在途请求丢响应、缓存写一半
     *
     * 四、Bean 实例化的线程安全约束
     *   1) 容器保证单例 Bean 在 refresh 期间由主线程逐个实例化，
     *      同一 Bean 不会并发创建 -> 构造器里无需同步
     *   2) 但构造器中启动子线程并引用半成品对象（this 逸出）会破坏安全：
     *      - 子线程读取尚未注入完的 @Autowired 字段 -> 看到 null
     *      - 正确姿势：用 @PostConstruct 或 ApplicationRunner 再启动
     *   3) 单例中持有可变状态必须考虑线程安全（无状态 Bean 最优，
     *      详见 UserService 注释块）；多例 Bean 每次创建新对象无共享风险
     *   4) @DependsOn 的语义：声明初始化顺序，与线程无关，
     *      只保证被依赖 Bean 先实例化与先销毁，不可替代真正的同步
     * =========================================================================
     */
}