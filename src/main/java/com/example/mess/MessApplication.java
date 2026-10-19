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
     * 【云原生部署】容器化与 K8s 部署指南（非可执行代码）
     * =========================================================================
     *
     * 一、Docker 化要点
     *   1) 基础镜像：eclipse-temurin:17-jre-alpine（小体积，~80MB）
     *      避免 full JDK 镜像（~400MB），生产只需 JRE
     *   2) 非 root 运行：USER 1000:1000，容器内不用 root，降低逃逸风险
     *   3) 健康检查：HEALTHCHECK CMD curl -f http://localhost:8080/actuator/health
     *   4) 资源限制：docker run --memory=512m --cpus=1，防止单容器吃光主机资源
     *
     * 二、K8s 部署配置
     *   1) 健康探针：
     *      livenessProbe: /actuator/health/liveness（进程活着？失败重启 Pod）
     *      readinessProbe: /actuator/health/readiness（能接流量？失败摘除流量）
     *      startupProbe: 失败阈值 30，间隔 10s（给慢启动应用时间）
     *   2) 资源配额：
     *      requests: cpu=250m, memory=512Mi（调度依据）
     *      limits: cpu=1000m, memory=1Gi（硬上限，超限 OOMKill）
     *   3) 优雅停机：
     *      server.shutdown=graceful（接收 SIGTERM 后停止接新请求）
     *      spring.lifecycle.timeout-per-shutdown-phase=30s
     *      terminationGracePeriodSeconds: 30（K8s 等 30s 再强杀）
     *
     * 三、配置外部化（12-Factor App）
     *   1) 环境变量：spring.datasource.url 从 env 注入，不打进镜像
     *   2) ConfigMap：非敏感配置（日志级别/线程池大小）
     *   3) Secret：敏感配置（数据库密码/JWT 密钥），base64 编码非加密
     *   4) 配置中心：Nacos/Apollo 动态配置，免重新部署
     *
     * 四、镜像安全
     *   1) 镜像扫描：Trivy/Grype 扫描 CVE 漏洞
     *   2) 最小化镜像：distroless 或 scratch，无 shell 无包管理器
     *   3) 镜像签名：cosign 签名，K8s 准入控制器验证签名
     *   4) 私有仓库：Harbor，带漏洞扫描和镜像策略
     * =========================================================================
     */
}