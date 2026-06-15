package com.example.mess;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Mess应用主类 - Spring Boot应用入口点
 * 
 * 这是整个Mess应用的启动类，包含main方法作为程序入口。
 * Spring Boot通过此类自动配置和启动应用。
 * 
 * 核心注解说明:
 * 
 * @SpringBootApplication:
 *   Spring Boot的核心组合注解，等同于以下三个注解的组合:
 *   - @Configuration: 标识此类为配置类，可以定义Bean
 *   - @EnableAutoConfiguration: 启用Spring Boot自动配置机制
 *     根据classpath中的依赖自动配置Spring应用
 *     例如: 检测到spring-boot-starter-web则自动配置嵌入式Tomcat
 *   - @ComponentScan: 启用组件扫描，自动发现和注册Bean
 *     扫描当前包及子包下所有@Component、@Service、@Repository等注解
 * 
 * @EnableCaching:
 *   启用Spring缓存抽象，激活@Cacheable、@CacheEvict等注解
 *   配合spring.cache.type配置使用:
 *   - simple: 使用ConcurrentMapCacheManager（开发环境）
 *   - redis: 使用RedisCacheManager（生产环境）
 * 
 * 启动流程:
 * 1. SpringApplication.run()启动Spring容器
 * 2. 执行自动配置，加载所有必要的Bean
 * 3. 扫描组件，注册@Controller、@Service、@Repository等
 * 4. 启动嵌入式Web服务器（默认Tomcat，端口8080）
 * 5. 初始化数据库连接和缓存
 * 6. 应用就绪，开始接收请求
 * 
 * 配置加载顺序:
 * 1. application.yml - 主配置文件
 * 2. application-{profile}.yml - 环境特定配置
 * 3. 命令行参数 - 最高优先级
 * 
 * 自定义启动配置:
 * - 可通过SpringApplication.Builder自定义启动行为
 * - 可添加ApplicationListener监听启动事件
 * - 可实现CommandLineRunner在启动后执行初始化逻辑
 * 
 * 作者: zhangyaolong.5
 * 创建时间: 2026-05-26
 * 
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.cache.annotation.EnableCaching
 */
@SpringBootApplication
@EnableCaching
public class MessApplication {

    /**
     * 应用主方法 - 程序入口点
     * 
     * SpringApplication.run()方法执行以下操作:
     * 1. 创建Spring应用上下文（ApplicationContext）
     * 2. 注册命令行参数为Spring属性
     * 3. 刷新应用上下文，加载所有Bean定义
     * 4. 触发自动配置，根据依赖配置Bean
     * 5. 启动嵌入式Web服务器
     * 6. 返回应用上下文对象
     * 
     * 启动参数示例:
     * --server.port=9090          修改服务端口
     * --spring.profiles.active=prod  激活生产环境配置
     * --debug                     启用调试模式
     * 
     * 常见启动问题:
     * - 端口被占用: 修改server.port或关闭占用进程
     * - Bean创建失败: 检查依赖注入和自动配置
     * - 数据库连接失败: 检查数据源配置
     * 
     * @param args 命令行参数，可用于覆盖配置属性
     */
    public static void main(String[] args) {
        SpringApplication.run(MessApplication.class, args);
    }
}