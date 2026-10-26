package com.example.mess.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security安全配置 - 使用Spring Security 6.x Lambda DSL风格。
 * 
 * <p>配置类职责:
 * <ul>
 *   <li>定义安全过滤器链（SecurityFilterChain）</li>
 *   <li>配置授权规则（哪些URL需要认证）</li>
 *   <li>配置认证方式（表单登录）</li>
 *   <li>管理内存用户存储（仅用于演示）</li>
 * </ul>
 * 
 * <p>授权规则详解:
 * <table border="1">
 *   <tr><th>URL模式</th><th>权限</th><th>说明</th></tr>
 *   <tr><td>/hello/**</td><td>公开访问</td><td>问候API，用于演示和测试</td></tr>
 *   <tr><td>/swagger-ui/**</td><td>公开访问</td><td>Swagger UI文档页面</td></tr>
 *   <tr><td>/v3/api-docs/**</td><td>公开访问</td><td>OpenAPI规范JSON</td></tr>
 *   <tr><td>/login</td><td>公开访问</td><td>登录页面</td></tr>
 *   <tr><td>其他</td><td>需要认证</td><td>所有业务API（如/api/users/**）</td></tr>
 * </table>
 * 
 * <p>认证方式:
 * <ul>
 *   <li>表单登录: 默认登录页面/login，登录成功后重定向到原始请求</li>
 *   <li>内存用户: user/password(ROLE_USER), admin/admin(ROLE_ADMIN)</li>
 * </ul>
 * 
 * <p>安全特性:
 * <ul>
 *   <li>CSRF禁用: REST API场景不需要CSRF保护（无状态请求）</li>
 *   <li>登出支持: 访问/logout即可登出，清除会话</li>
 *   <li>默认密码编码: 使用withDefaultPasswordEncoder()（仅演示用）</li>
 * </ul>
 * 
 * <p><b>生产环境注意事项:</b>
 * <ul>
 *   <li>替换为数据库用户存储（JdbcUserDetailsManager或自定义UserDetailsService）</li>
 *   <li>使用BCryptPasswordEncoder加密密码</li>
 *   <li>启用HTTPS防止中间人攻击</li>
 *   <li>配置CORS跨域策略</li>
 *   <li>添加JWT令牌认证替代表单登录</li>
 *   <li>配置会话管理（超时、并发控制）</li>
 * </ul>
 * 
 * @see org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
 * @since 1.0
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * 配置安全过滤器链。
     * <p>定义HTTP安全策略，包括：
     * <ul>
     *   <li>授权规则: 哪些URL公开访问，哪些需要认证</li>
     *   <li>表单登录: 启用默认表单登录页面</li>
     *   <li>登出支持: 允许用户登出并清除会话</li>
     *   <li>CSRF禁用: REST API场景不需要CSRF保护</li>
     * </ul>
     * 
     * <p>Lambda DSL风格是Spring Security 6.x推荐的配置方式，
     * 替代了旧的链式调用风格，更加类型安全和可读。
     * 
     * @param http HttpSecurity配置对象，由Spring自动注入
     * @return 配置好的安全过滤器链
     * @throws Exception 配置异常时抛出
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 配置授权规则：定义哪些请求需要认证，哪些可匿名访问
            .authorizeHttpRequests(auth -> auth
                // /hello/** 路径全部放行（问候API，用于演示和健康检查）
                .requestMatchers("/hello/**").permitAll()
                // Swagger文档相关路径放行，方便开发调试查看API文档
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                // 除上述放行路径外，其余所有请求都必须通过认证
                .anyRequest().authenticated()
            )
            // 启用表单登录，指定自定义登录页路径为/login，并放行登录页本身
            .formLogin(form -> form
                .loginPage("/login")
                .permitAll()
            )
            // 启用登出功能，放行登出请求（默认/logout）
            .logout(logout -> logout.permitAll())
            // 禁用CSRF保护：REST API为无状态请求，通常使用Token认证而非Cookie会话
            .csrf(csrf -> csrf.disable());
        
        // 构建并返回配置完成的安全过滤器链
        return http.build();
    }

    /**
     * 配置内存用户存储（仅用于演示）。
     * <p>创建两个演示用户：
     * <ul>
     *   <li>user/password - ROLE_USER角色，拥有普通用户权限</li>
     *   <li>admin/admin - ROLE_ADMIN角色，拥有管理员权限</li>
     * </ul>
     * 
     * <p>使用InMemoryUserDetailsManager存储用户信息在内存中，
     * 应用重启后用户数据丢失。生产环境必须替换为持久化存储。
     * 
     * <p>密码使用withDefaultPasswordEncoder()，明文存储，
     * 仅用于开发演示。生产环境必须使用BCryptPasswordEncoder。
     * 
     * @return 内存用户详情管理器，包含预定义的演示用户
     */
    @Bean
    public InMemoryUserDetailsManager userDetailsService() {
        // 构建普通用户：用户名user，密码password，角色USER
        // withDefaultPasswordEncoder使用明文密码（内部标记为{noop}），仅限演示使用
        UserDetails user = User.withDefaultPasswordEncoder()
            .username("user")
            .password("password")
            .roles("USER")
            .build();
        
        // 构建管理员用户：用户名admin，密码admin，角色ADMIN
        // 生产环境必须改用BCryptPasswordEncoder对密码加密存储
        UserDetails admin = User.withDefaultPasswordEncoder()
            .username("admin")
            .password("admin")
            .roles("ADMIN")
            .build();
        
        // 将两个用户放入内存管理器；数据仅存于内存，应用重启后丢失
        return new InMemoryUserDetailsManager(user, admin);
    }

    /*
     * =========================================================================
     * 【HTTP/REST】HTTP 认证协议与跨域策略（非可执行代码）
     * =========================================================================
     *
     * 一、HTTP 无状态性是安全设计的起点
     *   1) HTTP/1.1 本身无请求间记忆，认证态靠附加机制：
     *      a) Cookie + Session：服务端存状态（内存/Redis），客户端持 Cookie
     *      b) Token（Bearer）：服务端无状态，客户端持 Token
     *      c) Basic：每次请求带 base64(user:pwd)——仅 HTTPS 可用
     *      d) mTLS：双向证书认证，服务间通信（K8s 内网常用）
     *   2) 本项目用表单登录 + Session（formLogin）：默认 Spring Session
     *      存在内存 Map（开发用）；生产换 Redis/JDBC（spring-session-data-redis）
     *   3) REST 派视角：表单登录偏 Web，纯 API 项目推荐 Token/Bearer
     *      —— 但本项目 Hello/User 混用，表单是合理基础

     * 二、Cookie 与 Session 的协议细节
     *   1) Cookie 头格式：Set-Cookie: SESSIONID=abc; Path=/; HttpOnly; Secure; SameSite=Lax
     *   2) 关键属性清单：
     *      HttpOnly：JS 读不到，挡 XSS 偷 Cookie
     *      Secure：仅 HTTPS 发送
     *      SameSite=Lax/Strict：挡 CSRF（跨站请求不带 Cookie）
     *      Path=/api：作用域限制
     *   3) CSRF 与 Cookie 的因果关系：浏览器自动带 Cookie 是 CSRF 根源；
     *      Token（localStorage）不受此攻击面 —— 这是 Token 派的核心论据
     *   4) 本项目 disable CSRF：因为 REST API 无状态语义 + Token 计划；
     *      若保留表单登录且要恢复 Session 验证，必须打开 CSRF
     *   5) SameSite 现代浏览器默认 Lax，已显著降 CSRF 风险——
     *      但服务端仍应显式配置，不依赖浏览器差异

     * 三、CORS（Cross-Origin Resource Sharing）的协议交互
     *   1) 同源策略：协议+域名+端口 三者一致才同源；
     *      跨源请求默认被浏览器拦截（不是服务器拒绝）
     *   2) 预检请求（Preflight）：复杂请求（带自定义头/PUT/DELETE/JSON Content-Type）
     *      浏览器先发 OPTIONS 探路：
     *      OPTIONS /api/users
     *      Origin: https://app.example.com
     *      Access-Control-Request-Method: POST
     *      Access-Control-Request-Headers: Content-Type, Authorization
     *   3) 服务端响应：
     *      Access-Control-Allow-Origin: https://app.example.com（或 * 公共 API）
     *      Access-Control-Allow-Methods: GET, POST, PUT, DELETE
     *      Access-Control-Allow-Headers: Content-Type, Authorization
     *      Access-Control-Allow-Credentials: true（与 * 互斥）
     *      Access-Control-Max-Age: 3600（预检缓存）
     *   4) 本项目 CORS 现状：未显式 .cors(...)——
     *      同域开发下无碍；SPA（不同 origin）就需要：
     *      .cors(cors -> cors.configurationSource(source)) 配置 Bean
     *   5) 简单请求（GET/POST + 简单头）无预检，直接发请求 +
     *      Response 带 CORS 头——前端拿不到响应头会被浏览器拦在 JS 层

     * 四、跨域的替代方案与陷阱
     *   1) 反向代理同源：Nginx 把前端与 API 放同一域名下（/api 转发后端），
     *      彻底避免 CORS——生产环境最稳
     *   2) 陷阱 1：Access-Control-Allow-Origin: * 与 credentials=true 不能并存
     *      浏览器直接拒绝带 Cookie 的跨源请求
     *   3) 陷阱 2：预检失败被网关吞掉——返回 405/404 而非正确的 CORS 头，
     *      前端误以为权限问题，排查要从 OPTIONS 响应头开始
     *   4) 陷阱 3：把 Allow-Headers 写死白名单，客户端加新自定义头
     *      （如 X-Trace-Id）立即触发预检失败——设个通用策略

     * 五、REST 安全清单（按协议层）
     *   1) HTTPS 强制：HSTS 头（Strict-Transport-Security）防降级
     *   2) 鉴权头校验：Authorization: Bearer <jwt> 在过滤器层鉴权
     *   3) 限流：按 IP/API Key 限流（429 Too Many Requests），见网关或
     *      Resilience4j RateLimiter
     *   4) 请求体大小限制：server.max-http-header-size / max-swallow-size
     *      防 DoS（超大包耗内存）
     *   5) 输出脱敏：日志与响应里 password/token/手机号掩码（响应不外泄）
     *   6) 安全头清单（Spring Security 默认开）：
     *      X-Content-Type-Options: nosniff
     *      X-Frame-Options: DENY（防点击劫持）
     *      X-XSS-Protection（旧浏览器）
     *      Content-Security-Policy（前端强约束）
     * =========================================================================
     */
}