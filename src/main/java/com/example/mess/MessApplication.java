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
     * 【设计模式】单例与静态工厂：SpringApplication.run 的模式密码（非可执行代码）
     * =========================================================================
     *
     * 一、单例模式（Singleton）在 Spring 中的"借壳还魂"
     *   1) 经典 GoF 单例：构造私有、持有静态实例、全局唯一访问点，
     *      靠类自身保证唯一
     *   2) Spring 的修正：移除类的自我约束，把唯一性交给容器：
     *      每个单例 Bean 由 BeanDefinition 声明 scope=singleton，
     *      容器的 DefaultSingletonBeanRegistry（singletonObjects Map）
     *      保证一个 beanName 只创建一份实例
     *   3) 优势：可配置（@Scope 切换）、可注入、可代理（AOP 织入）、
     *      可销毁（生命周期回调）；经典单例几乎没有这些能力
     *   4) 本类自身不是单例（无私有构造、无静态实例），
     *      它的"唯一性"来自被容器扫描注册为单例 BeanDefinition

     * 二、静态工厂方法模式（Static Factory Method）
     *   1) 本类 main() 调用的 SpringApplication.run(Class, args) 即典型
     *      静态工厂：调用方无需 new SpringApplication，由静态方法决定
     *      返回什么（ConfigurableApplicationContext 的具体实现）
     *   2) 比 new 的四大优势（Effective Java Item 1）在源码中兑现：
     *      - 有名字：run / from / existing 表达意图
     *      - 不必每次新建：run 可能返回已有的 context（existing 场景）
     *      - 可返回子类型：实际返回 AnnotationConfigServletWebServerApplicationContext
     *      - 参数化：webApplicationType 推断后构造不同子类
     *   3) 对比 AbstractFactory：run 不是抽象工厂，仍属静态工厂范畴，
     *      区别在于"无实例多态"，只有一个入口
     *
     * 三、建造者模式的影子：SpringApplicationBuilder
     *   1) new SpringApplicationBuilder(MessApplication.class)
     *      .profiles("prod").web(WebApplicationType.SERVLET).run();
     *   2) 与主流 Builder（逐步设置后 build()）一致，是同一引导能力的
     *      另一种表达：静态工厂适合一行搞定，Builder 适合复杂装配
     *   3) Project Lombok 的 @Builder 同出一辙：在 DTO/Entity 上加注解
     *      即获得 Builder，本项目 UserDto 可借此减少构造函数爆炸
     * =========================================================================
     */
}