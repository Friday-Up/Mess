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
         * 【数据库与SQL优化】应用启动期的数据源初始化与建表策略（非可执行代码）
         * =========================================================================
         *
         * 一、启动期数据库做了什么（spring.sql.init 模式）
         *   1) 本项目 schema.sql + data.sql 由 spring.sql.init.mode=always 触发：
         *      DataSource 初始化后 -> 执行 schema.sql（DDL）
         *      -> 执行 data.sql（DML）-> 完成后才进入 ApplicationContext refresh 完成
         *   2) H2/MySQL 方言差异：schema.sql 直接手写 DDL，
         *      不同数据库之间需按 profile 切换（schema-h2.sql / schema-mysql.sql）
         *   3) JPA 的另一种建表方式（Hibernateddl-auto）：
         *      create-drop：每次启动删表重建，仅测试用法
         *      update：启动时 diff 实体与表结构自动 ALTER，开发用
         *      validate：只校验不修改，生产推荐（配合 Flyway/Liquibase 管版本）
         *      none：完全不管，生产环境由 DBA/迁移工具负责
         *   4) 本项目选择 sql.init + 手写 DDL 是"显式可控"路线：
         *      优点：脚本即文档，变更可审阅；
         *      规模化后演进：把 schema.sql 数据迁移到 Flyway（版本化迁移）
         =========================================================================
         */
}