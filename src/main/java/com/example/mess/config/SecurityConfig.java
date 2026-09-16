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
     * 【面试问答】关于 Spring Security 的常见面试题（非可执行代码）
     * =========================================================================
     *
     * Q1: Spring Security 的核心是什么？
     * A1: SecurityFilterChain —— 一条过滤器链，每个过滤器负责一个环节：
     *     认证 -> 授权 -> CSRF -> Session -> 异常转换。请求穿过整条链。
     *
     * Q2: CSRF 什么时候可以禁用？
     * A2: CSRF 只对"浏览器 Cookie 认证"有效。若用 Token/Header 认证
     *     （无 Cookie），可以安全禁用。若有浏览器 Cookie 认证则需启用，
     *     前端从 Cookie 读 CSRF Token 放入请求头。
     *
     * Q3: 为什么密码要用 BCrypt 而不用 MD5？
     * A3: MD5 可被彩虹表秒破。BCrypt 内置随机盐 + 可调计算代价（cost factor），
     *     每次哈希结果不同，抗暴力破解。Spring 用 BCryptPasswordEncoder。
     *
     * Q4: hasRole("ADMIN") 和 hasAuthority("ADMIN") 的区别？
     * A4: hasRole 自动加 ROLE_ 前缀，实际匹配 ROLE_ADMIN；
     *     hasAuthority 精确匹配，不加前缀。
     *     UserDetails 中角色需以 "ROLE_" 开头才能用 hasRole。
     *
     * Q5: Security 的 401/403 不经过 @RestControllerAdvice 怎么处理？
     * A5: Security 过滤器链中的异常不走 Controller 层的全局处理器。
     *     需单独配置 AuthenticationEntryPoint（401）和
     *     AccessDeniedHandler（403），返回统一 ApiResponse 格式。
     *
     * Q6: 生产环境怎么从内存用户迁移到数据库？
     * A6: 实现 UserDetailsService.loadUserByUsername()，
     *     查数据库返回 UserDetails。替换 InMemoryUserDetailsManager。
     *     密码字段用 BCrypt 加密存储。
     * =========================================================================
     */

    /*
     * =========================================================================
     * 【源码走读】Security 过滤器链与认证流程（非可执行代码）
     * =========================================================================
     *
     * 一、过滤器链的顺序（简化版）
     *    SecurityContextPersistenceFilter  —— 从 Session 恢复 SecurityContext
     *    UsernamePasswordAuthenticationFilter —— 处理表单登录认证
     *    BasicAuthenticationFilter        —— 处理 HTTP Basic 认证
     *    ExceptionTranslationFilter       —— 翻译认证/授权异常为 401/403
     *    FilterSecurityInterceptor        —— 最终授权决策
     *    请求穿过整条链，任一环节拦截即返回，不进入 Controller。
     *
     * 二、认证流程（表单登录为例）
     *    1) 请求到达 UsernamePasswordAuthenticationFilter
     *    2) 封装为 UsernamePasswordAuthenticationToken（未认证）
     *    3) 交给 AuthenticationManager -> AuthenticationProvider
     *    4) Provider 调 UserDetailsService.loadUserByUsername() 查用户
     *    5) PasswordEncoder.matches() 校验密码
     *    6) 成功 -> 创建已认证 Token 存入 SecurityContext
     *    7) 失败 -> 抛 AuthenticationException -> 401
     *
     * 三、授权流程
     *    FilterSecurityInterceptor 在 Controller 执行前做最终检查：
     *    1) 读取 SecurityContext 中的 Authentication
     *    2) 根据 @PreAuthorize 或 hasRole/hasAuthority 规则判断
     *    3) 通过 -> 放行；不通过 -> 抛 AccessDeniedException -> 403
     *
     * 四、Session vs Token
     *    Session：认证后存 Session，后续请求靠 Cookie 关联。有状态。
     *    Token（JWT）：认证后返回 Token，后续请求 Header 携带。无状态。
     *    无状态用 sessionCreationPolicy(STATELESS) + JWT Filter 替换表单登录。
     *
     * 五、Security 的 401/403 为什么不经过 @RestControllerAdvice
     *    Security 异常在 Filter 层抛出，此时还没进入 DispatcherServlet，
     *    @ControllerAdvice 是 MVC 层机制，拦截不到。
     *    需要 AuthenticationEntryPoint（401）和 AccessDeniedHandler（403）
     *    在 Security 配置中手动指定，返回统一 ApiResponse 格式。
     * =========================================================================
     */
}