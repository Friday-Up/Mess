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
     * 【性能优化】安全过滤器链性能（非可执行代码）
     * =========================================================================
     *
     * 一、过滤器链开销分析
     *   每个请求穿过 SecurityFilterChain，开销约 0.1-0.5ms（无认证时）。
     *   认证（查数据库/验证 Token）是主要开销，可达 5-20ms。
     *
     * 二、认证性能优化
     *   1) JWT 无状态认证：Token 自包含用户信息，免查数据库验证
     *   2) Token 缓存：解析后的 Authentication 缓存到 SecurityContext
     *   3) 密码编码：BCrypt cost factor=10 约 100ms，只在登录时执行一次
     *      后续请求用 Token/Session 免重复编码
     *   4) 批量登录：限制登录接口 QPS，防暴力破解消耗 CPU（BCrypt 是 CPU 密集型）
     *
     * 三、授权性能优化
     *   1) 规则顺序：高频放行的规则放前面（如 /api/public/** permitAll）
     *   2) 规则精确：用 requestMatchers 精确匹配，避免 anyRequest 兜底过多
     *   3) 角色缓存：用户角色信息缓存，避免每次请求查数据库
     *   4) 方法级安全：@PreAuthorize 有 AOP 开销，高频接口考虑 URL 级授权
     *
     * 四、Session 性能
     *   1) 无状态：sessionCreationPolicy(STATELESS)，不创建 Session
     *   2) Session 共享：多实例部署用 Spring Session + Redis
     *   3) Session 超时：server.servlet.session.timeout=30m，避免内存泄漏
     *
     * =========================================================================
     * 【监控可观测性】安全层关键指标
     * =========================================================================
     *   - 认证失败率：401/total，>10% 可能被攻击
     *   - 授权拒绝率：403/total，突增可能权限配置错误
     *   - 登录 QPS：/api/auth/login 的 count/second，突增可能是撞库
     *   - BCrypt 耗时：登录接口 P99 > 500ms 正常（BCrypt 本身慢）
     *   - Session 存活数：spring.session.active（内存/Redis 中）
     *   安全告警：连续 5 次登录失败锁定 IP，发送告警通知
     * =========================================================================
     */

    /*
     * =========================================================================
     * 【性能优化补充】分布式安全架构性能（非可执行代码）
     * =========================================================================
     *
     * 一、多实例部署的安全性能
     *   1) Session 共享：Spring Session + Redis，避免 Session 粘滞
     *      开销：每次请求读写 Redis，约 1-3ms
     *   2) JWT 无状态：Token 自包含，免服务端存储，约 0.5ms（签名验证）
     *   3) Token 黑名单：JWT 本身无法主动失效，需 Redis 黑名单
     *      开销：每次请求查 Redis，约 1ms
     *   4) 建议：高并发场景用 JWT + 短 TTL + Refresh Token，
     *      避免 Session 共享的 Redis 瓶颈
     *
     * 二、网关层安全性能
     *   1) 认证前置：网关统一认证，后端服务免认证开销
     *   2) 限流前置：网关限流，保护后端不被打挂
     *   3) WAF：Web 应用防火墙，拦截 SQL 注入/XSS/CC 攻击
     *   4) SSL 卸载：网关终止 TLS，后端 HTTP，减少后端 CPU 开销
     *      TLS 握手约 10-50ms（RSA 2048），卸载后后端免开销
     *
     * 三、密码相关性能
     *   1) BCrypt cost factor：10 约 100ms，12 约 400ms
     *      登录是低频操作，100ms 可接受；注册同理
     *   2) 暴力破解防护：登录限流（5次/分钟/IP），
     *      失败锁定（5次失败锁15分钟），验证码（3次失败后）
     *   3) 密码重置：Token 一次性 + 短 TTL（15分钟），防重放
     * =========================================================================
     */
}