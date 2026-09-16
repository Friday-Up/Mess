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
     * 【检查清单】安全配置自检表（非可执行代码）
     * =========================================================================
     *
     * [ ] 生产环境不用 InMemoryUserDetailsManager，改数据库 UserDetailsService
     * [ ] 密码用 BCryptPasswordEncoder 或更强算法，不明文存储
     * [ ] 授权规则先精确后宽泛（anyRequest 放最后）
     * [ ] CORS 显式配置允许来源，不用 *
     * [ ] 敏感接口有权限要求（不是 permitAll）
     * [ ] 认证失败返回 401，权限不足返回 403（不是统一 500）
     *
     * 认证排障速查
     *   现象：所有接口都 403
     *     -> 检查 anyRequest().authenticated() 是否把公开接口也拦了
     *     -> 检查 permitAll() 规则的顺序是否在 anyRequest 之前
     *
     *   现象：带了正确用户名密码还是 401
     *     -> 检查密码编码器是否匹配（存的是 BCrypt 但用 NoOp 验证）
     *     -> 检查 UserDetails 的密码字段是否正确加载
     *
     *   现象：登录成功但接口仍 403
     *     -> 角色配置：hasRole("ADMIN") 对应的 authority 是 "ROLE_ADMIN"
     *     -> 检查用户是否真的被分配了该角色
     *
     *   现象：CSRF 报 403
     *     -> 本项目已禁用 CSRF；若恢复，前端须在请求头带 CSRF Token
     *     -> 前端无法获取 Token 时检查 Cookie 是否被 SameSite 策略拦截
     * =========================================================================
     */

    /*
     * =========================================================================
     * 【补充手册】密码存储演进与生产迁移清单（非可执行代码）
     * =========================================================================
     *
     * 一、密码存储的演进
     *   第一代：明文存储 —— 不可接受，数据库泄露即全部裸奔
     *   第二代：MD5/SHA1 哈希 —— 不可逆但可彩虹表破解，已不安全
     *   第三代：加盐哈希（salt+SHA256）—— 比上一代好，但 GPU 暴力破解仍可行
     *   第四代：BCrypt/Argon2/PBKDF2 —— 内置盐+可调计算代价，抗暴力破解
     *   推荐：BCryptPasswordEncoder（Spring 内建支持，代价因子 >= 10）
     *
     * 二、生产环境迁移清单
     *   [ ] InMemoryUserDetailsManager -> 自定义 UserDetailsService（查数据库）
     *   [ ] 密码编码器确认 BCryptPasswordEncoder
     *   [ ] 认证从 Session+Cookie 改为 JWT 或 OAuth2（无状态）
     *   [ ] sessionCreationPolicy(STATELESS) 关闭 Session
     *   [ ] CORS 显式配置允许来源（不用 *）
     *   [ ] 安全响应头：X-Content-Type-Options, X-Frame-Options,
     *       Content-Security-Policy, Strict-Transport-Security
     *   [ ] 登录失败锁定策略（连续 N 次失败临时锁定 IP/账号）
     *   [ ] 审计日志：记录登录成功/失败、敏感操作
     *   [ ] 密钥/凭证不放代码中，用环境变量或密钥管理服务
     * =========================================================================
     */
}