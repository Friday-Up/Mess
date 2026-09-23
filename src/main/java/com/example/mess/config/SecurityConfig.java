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
     * 【ADR-009】认证方案选择策略
     * =========================================================================
     * 上下文：项目需要认证机制保护接口，可选 Session+Cookie、JWT Token、
     *         OAuth2 等方案。当前是开发/演示阶段，先用内存用户快速验证。
     * 决策：开发阶段用 InMemoryUserDetailsManager + BCryptPasswordEncoder，
     *       禁用 CSRF（因为走 Token 认证路线），生产阶段迁移到数据库用户 + JWT。
     * 替代方案：
     *   A) Session+Cookie —— 有状态，不适合微服务和前后端分离。
     *   B) 直接上 JWT —— 开发阶段配置复杂，拖慢初期进度。
     *   C) 不做认证 —— 接口裸奔，安全风险极高。
     * 后果：开发阶段快速可用；生产迁移需：1) 实现 UserDetailsService 查数据库；
     *       2) 加 JWT Filter；3) sessionCreationPolicy(STATELESS)；
     *       4) CORS 显式配置；5) 安全响应头加固。
     *
     * =========================================================================
     * 【代码审查要点】安全配置
     * =========================================================================
     * [ ] 生产环境不用 InMemoryUserDetailsManager，改数据库 UserDetailsService
     * [ ] 密码用 BCryptPasswordEncoder 或更强算法，不明文存储
     * [ ] 授权规则先精确后宽泛（anyRequest 放最后）
     * [ ] CORS 显式配置允许来源，不用 *
     * [ ] 敏感接口有权限要求（不是 permitAll）
     * [ ] 认证失败返回 401，权限不足返回 403
     * [ ] CSRF 策略明确：Token 认证可禁用，Cookie 认证需启用
     * [ ] 用户/密钥不放代码中，用环境变量或密钥管理服务
     * [ ] Security 的 401/403 需单独配置 AuthenticationEntryPoint/AccessDeniedHandler
     * =========================================================================
     */

    /*
     * =========================================================================
     * 【ADR-009-S】密码存储演进策略（补充）
     * =========================================================================
     * 上下文：密码存储方案直接影响用户数据安全，需选择抗破解的算法。
     * 决策：使用 BCryptPasswordEncoder，内置随机盐 + 可调计算代价（cost factor），
     *       抗暴力破解和彩虹表攻击。
     * 替代方案：
     *   A) 明文存储 —— 数据库泄露即全部裸奔，不可接受。
     *   B) MD5/SHA1 哈希 —— 可被彩虹表秒破，已不安全。
     *   C) 加盐 SHA256 —— 比上一代好，但 GPU 暴力破解仍可行。
     *   D) Argon2 —— 比 BCrypt 更新更强，但 Spring 内建支持不如 BCrypt 完善。
     * 后果：BCrypt 是 Spring Security 默认推荐，生态支持好；
     *       cost factor >= 10 可调，硬件升级后可提高代价保持安全性。
     *
     * 生产迁移清单：
     *   [ ] InMemoryUserDetailsManager -> 自定义 UserDetailsService（查数据库）
     *   [ ] 密码编码器确认 BCryptPasswordEncoder
     *   [ ] 认证从 Session+Cookie 改为 JWT 或 OAuth2（无状态）
     *   [ ] sessionCreationPolicy(STATELESS) 关闭 Session
     *   [ ] CORS 显式配置允许来源（不用 *）
     *   [ ] 安全响应头：X-Content-Type-Options, X-Frame-Options,
     *       Content-Security-Policy, Strict-Transport-Security
     *   [ ] 登录失败锁定策略（连续 N 次失败临时锁定）
     *   [ ] 审计日志：记录登录成功/失败、敏感操作
     * =========================================================================
     */
}