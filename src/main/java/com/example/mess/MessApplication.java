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
         * 【可观测性与生产运维】应用启动的可观测性与发布健康检查（非可执行代码）
         * =========================================================================
         *
         * 一、启动过程本身就是第一类可观测目标
         *   1) 生产发布时最关心三件事：起没起来、多久起来、为什么没起来——
         *      启动日志必须能回答这三问，否则回滚决策全靠猜
         *   2) Spring Boot 启动日志要点：Started MessApplication in X seconds + 
         *      端口 + profile——运维脚本用 grep 这行判活，别用进程存在判活
         *   3) 启动慢的信号：Bean 初始化耗时分布可用 
         *      spring.main.log-startup-info + 启动事件监听记录每个 Bean 的 init 时间
         *
         * 二、健康检查与就绪/存活探针
         *   1) actuator/health 是存活基础：probe=liveness 走进程内自检，
         *      probe=readiness 加 DB/Redis 依赖检查——K8s 两探针分开配置
         *   2) readiness 未就绪时流量不进：滚动发布零断流的底线，
         *      别把 readiness 检查配成"只查进程"，那样发布会打到未启动实例
         *   3) 健康指标分级：UP/DEGRADED/DOWN——依赖抖动时 DOWN 会把实例踢出，
         *      误配会导致全集群雪崩，敏感依赖用 DEGRADED + 告警而非 DOWN
         *
         * 三、启动失败的排障路径
         *   1) 启动失败第一现场是 stdout/stderr 与退出码：
         *      K8s 用 restartPolicy + describe pod 看退出原因，比事后翻日志快
         *   2) 启动期 Full GC / OOM 用 -Xlog:gc 与 heap dump on exit 抓首案——
         *      没有现场的事后分析往往归因错误
         =========================================================================
         */
}