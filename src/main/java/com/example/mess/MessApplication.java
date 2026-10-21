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
     * 【架构演进】从单体到微服务的演进路线（非可执行代码）
     * =========================================================================
     *
     * 一、单体架构（当前状态）
     *   特征：所有模块（用户/订单/支付）打包在一个 JAR/WAR 中
     *   优点：开发简单、部署简单、调试方便、事务一致性容易
     *   缺点：代码膨胀后构建慢、模块耦合、技术栈单一、扩展只能整体扩展
     *   适用：初创期、团队 <10 人、业务快速试错阶段
     *
     * 二、模块化单体（Modular Monolith，推荐的第一步演进）
     *   特征：仍是单体部署，但代码按业务域严格分模块
     *   规则：
     *   1) 每个模块有明确的边界（包结构：com.example.mess.user / order / payment）
     *   2) 模块间只通过公开接口通信，不直接访问其他模块的内部类
     *   3) 每个模块有独立的数据库表，不跨模块 JOIN
     *   4) 模块间事件通信：ApplicationEventPublisher + @EventListener
     *   优点：保留单体部署简单性，同时获得模块边界的可拆分性
     *   演进成本：低，只需重构包结构和依赖关系
     *
     * 三、微服务架构（业务规模足够大时）
     *   特征：每个业务域独立部署、独立数据库、独立技术栈
     *   拆分依据：DDD 限界上下文（Bounded Context）
     *   通信方式：
     *   1) 同步：REST/gRPC（服务间直接调用，简单但耦合）
     *   2) 异步：消息队列（Kafka/RocketMQ，解耦但最终一致）
     *   3) 事件驱动：领域事件 + 事件溯源（最解耦，最复杂）
     *   基础设施需求：
     *   - 服务注册发现：Nacos/Eureka/Consul
     *   - 配置中心：Nacos/Apollo
     *   - API 网关：Spring Cloud Gateway
     *   - 链路追踪：SkyWalking/Zipkin
     *   - 分布式事务：Seata/消息事务
     *   - 容器编排：Kubernetes
     *
     * 四、演进原则（Martin Fowler 的 Strangler Fig 模式）
     *   1) 不要一次性重写：绞杀者模式，逐步用新服务替换旧模块
     *   2) 先模块化再微服务化：单体内部先划清边界，再按边界拆出
     *   3) 数据先行：先拆数据库，再拆服务（数据耦合是最难拆的）
     *   4) 双写过渡：新旧并行运行，流量逐步切换，验证后下线旧模块
     *   5) 拆分收益 > 拆分成本时才拆：微服务不是银弹，分布式复杂性代价高
     * =========================================================================
     */
}