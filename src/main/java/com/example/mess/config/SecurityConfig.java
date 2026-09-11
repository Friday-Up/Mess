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
     * ============================================================================
     * 【阅读笔记】Spring Security 核心机制速览（非可执行代码）
     * ============================================================================
     *
     * 一、过滤器链的心智模型
     * ----------------------------------------------------------------------------
     *   SecurityFilterChain 是一条责任链，每个过滤器负责一件事：
     *     认证 -> 授权 -> CSRF -> Session -> 异常转换;
     *   一次请求会穿过整条链，任一环节对不起都可能被拦截返回 401/403。
     *   本配置用 SecurityFilterChain Bean 定义规则，是 Spring Security 6.x 的
     *   标准写法（旧的 WebSecurityConfigurerAdapter 已废弃）。
     *
     * 二、授权规则的常见写法
     * ----------------------------------------------------------------------------
     *   requestMatchers("/api/public/**").permitAll()
     *   requestMatchers(HttpMethod.GET, "/api/users/**").hasRole("USER")
     *   anyRequest().authenticated()          —— 其余都要登录
     *   规则顺序敏感：前面的规则先匹配，必须"先精确后宽泛"。
     *
     * 三、密码存储的正确姿势
     * ----------------------------------------------------------------------------
     *   错误：明文、"MD5"、普通哈希（皆不可逆且无盐）;
     *   推荐：BCrypt / Argon2 / PBKDF2——内置盐与计算代价参数。
     *   Spring 提供 DelegationPasswordEncoder，用 {id} 前缀区分编码器，
     *   便于灰度升级加密算法而不清空存量数据。
     *
     * 四、CSRF 决策要点
     * ----------------------------------------------------------------------------
     *   - CSRF 只针对"浏览器基于 Cookie 的认证"有效；
     *   - 若项目走 Token / Header 认证（无 Cookie），可以安全禁用;
     *   - 若同时支持浏览器认证，建议在服务端激活 CSRF 并在前端读 Token 提交。
     *
     * 五、常见误区
     * ----------------------------------------------------------------------------
     *   - 只禁用了 CSRF 就以为安全了——XSS 偷走 Token 更危险;
     *   - 把管理员接口只藏在前端菜单里——必须服务端强制鉴权;
     *   - 密码哈希后不做参数规范——如不加盐、不参数化 SQL 等会留下漏洞。
     * ============================================================================
     */

    /*
     * ============================================================================
     * 【补充阅读】生产环境迁移检查清单（非可执行代码）
     * ============================================================================
     *
     *   [ ] 用户存储：InMemoryUserDetailsManager -> 数据库 UserDetailsService
     *       并实现自定义 UserDetails（含部门/租户等业务字段）
     *   [ ] 密码编码器：确认全站使用 BCryptPasswordEncoder 或更强算法
     *   [ ] 认证方式：Session+Cookie -> JWT/OAuth2，明确无状态策略
     *   [ ] 会话管理：sessionManagement().sessionCreationPolicy(STATELESS)
     *   [ ] 安全配置外置：不要把用户/密钥硬编码在 Java 中，改放环境变量或密钥管理
     *   [ ] CORS：显式声明允许的源与方法，避免全放开
     *   [ ] CSRF：若回退到 Cookie 认证，重新启用并前端配合
     *   [ ] Header 加固：X-Content-Type-Options、X-Frame-Options、
     *       Content-Security-Policy、Strict-Transport-Security
     *   [ ] 登出/Token 失效策略：黑名单或短过期 + Refresh Token
     *   [ ] 审计日志：记录登录成功/失败、权限拒绝等安全事件
     *
     *   说明：当前配置是开发/演示形态，上生产前请按以上清单逐项确认。
     * ============================================================================
     */
}