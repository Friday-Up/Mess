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
     * 【HTTP/REST】HTTP 协议基础与内嵌服务器的端口模型（非可执行代码）
     * =========================================================================
     *
     * 一、从 TCP 到 HTTP：一次 localhost:8080 调用的网络路径
     *   1) 客户端 socket.connect(127.0.0.1, 8080)：三次握手建连接
     *   2) 应用层写入 "GET /hello HTTP/1.1\r\nHost: localhost\r\n\r\n"：
     *      请求行（方法/URI/版本）+ 请求头（键值对）+ 空行 + 可选实体
     *   3) Tomcat Acceptor 收到连接 -> Poller 监听就绪 -> 工作线程解析
     *      请求头并 wrap 为 RequestFacade
     *   4) Servlet 容器把字节流解析成结构化（method/uri/header/params），
     *      上层 Spring 无需手写字节解析——协议层的抽象红利
     *   5) HTTP/1.1 默认 Keep-Alive：一个 TCP 连接可串行跑多个请求，
     *      这是浏览器把静态资源放同一 origin 的原因（少握手开销）
     *
     * 二、版本演进与本项目现状
     *   1) HTTP/1.1：本项目默认，串行请求（含 Keep-Alive 复用），
     *      队头阻塞（HOL Blocking）是主要痛点
     *   2) HTTP/2：多路复用（一个连接并行跑流），需要 TLS/ALPN 协商，
     *      反向代理层（Nginx/网关）启用即可，应用代码零改动
     *   3) HTTP/3：QUIC/UDP 底座，弱网切换更稳，主流云厂商支持中
     *   4) 项目启示：代码不感知协议版本，升级的是基础设施；
     *      但 API 设计上的"每个请求自包含元数据"原则（见 Controller 块）
     *      是与任何版本兼容的前提
     *
     * 三、端口的运维语义
     *   1) 8080 是 HTTP 默认替代端口（80 被系统服务占用），
     *      生产环境常用 80/443（前置负载均衡/网关）
     *   2) 端口监听 = bind 系统调用：OS 的 backlog（accept 队列）
     *      与应用层 accept 速率的平衡——队列满会丢包
     *   3) server.port=0 表示"让 OS 随机分配端口"，测试场景常用，
     *      集成测试获取实际端口的姿势是 @LocalServerPort
     *   4) 一端口一进程：同机 8080 冲突会抛 "Address already in use"，
     *      排查口诀：lsof -i :8080 / netstat -ano
     *
     * 四、请求/响应元数据清单（设计 API 时必须想清楚的字段）
     *   1) 请求侧：Method（动作）/ Path（资源）/ Query（筛选条件）/
     *      Header（鉴权、内容协商、追踪 ID）/ Body（负荷）
     *   2) 响应侧：Status Code（结果语义）/ Header（缓存、内容类型、
     *      CORS 放行、Location 重定向）/ Body（数据或错误详情）
     *   3) 本项目 ApiResponse 把 Status 与 Body 语义做了部分重叠（见块），
     *      优势是前端统一解析，代价是放弃了 HTTP 状态码的部分表达力——
     *      工程权衡的结果
     *   4) 元数据可观测性：把 trace_id 放入 X-Trace-Id 响应头，
     *      是排障"客户端能自证我发送了哪个请求"的最小可靠配置
     *   5) HTTPS：TLS 握手后才是 HTTP 语义；Header 中的 Authorization
     *      / Cookie 在 TLS 内加密，但不代表可以放明文密码——
     *      协议层加密与应用层安全是两道门，见 SecurityConfig 块
     * =========================================================================
     */
}