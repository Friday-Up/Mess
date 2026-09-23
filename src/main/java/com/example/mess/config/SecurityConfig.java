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
     * 【技术债务】TD-009 安全配置
     * =========================================================================
     *
     * TD-009-1: 使用内存用户存储
     *   现状：InMemoryUserDetailsManager，用户数据硬编码
     *   影响：应用重启用户丢失，无法动态增删用户
     *   优先级：P1（生产不可用）
     *   修复方案：实现 UserDetailsService 查数据库
     *   预估工时：2d
     *
     * TD-009-2: 无 JWT/Token 认证
     *   现状：基于 Session 的认证，不适合前后端分离
     *   影响：跨域场景认证困难，水平扩展需 Session 共享
     *   优先级：P1
     *   修复方案：加 JWT Filter + sessionCreationPolicy(STATELESS)
     *   预估工时：2d
     *
     * TD-009-3: CORS 未配置
     *   现状：未显式配置 CORS
     *   影响：前端跨域请求可能被拒绝
     *   优先级：P2
     *   修复方案：加 .cors(Customizer.withDefaults()) + CorsConfigurationSource Bean
     *   预估工时：0.5d
     *
     * TD-009-4: 缺少安全响应头
     *   现状：无 X-Content-Type-Options/X-Frame-Options/CSP/HSTS
     *   影响：易受点击劫持/MIME 嗅探等攻击
     *   优先级：P2
     *   修复方案：加 headers().contentTypeOptions().frameOptions().xssProtection()
     *   预估工时：0.5d
     *
     * TD-009-5: 缺少登录/登出端点
     *   现状：无显式登录接口
     *   影响：前端无法获取 Token/Session
     *   优先级：P1
     *   修复方案：加 /api/auth/login + /api/auth/logout
     *   预估工时：1d
     *
     * =========================================================================
     * 【重构路线图】安全配置演进方向
     * =========================================================================
     * Phase 1（当前）：内存用户 + Session 认证 + CSRF 禁用
     * Phase 2：数据库用户 + JWT Token + CORS + 安全响应头
     * Phase 3：OAuth2/SSO + RBAC 权限模型 + 审计日志 + 限流
     * =========================================================================
     */

    /*
     * =========================================================================
     * 【技术债务】TD-009-S 安全配置补充
     * =========================================================================
     *
     * TD-009-6: 缺少密码强度策略
     *   现状：无密码复杂度要求
     *   影响：弱密码易被暴力破解
     *   优先级：P2
     *   修复方案：加 PasswordValidator（长度/大小写/数字/特殊字符）
     *   预估工时：0.5d
     *
     * TD-009-7: 缺少登录失败锁定
     *   现状：无连续失败锁定机制
     *   影响：可无限次尝试暴力破解
     *   优先级：P2
     *   修复方案：加失败计数 + 临时锁定（如 5 次失败锁定 15 分钟）
     *   预估工时：1d
     *
     * TD-009-8: 缺少审计日志
     *   现状：无登录成功/失败/权限拒绝的审计记录
     *   影响：安全事件无法追溯
     *   优先级：P2
     *   修复方案：加 AuthenticationSuccessHandler/FailureHandler 记录日志
     *   预估工时：1d
     *
     * TD-009-9: 密钥/凭证硬编码
     *   现状：用户名密码硬编码在 Java 代码中
     *   影响：代码泄露即凭证泄露
     *   优先级：P1
     *   修复方案：迁移到环境变量或密钥管理服务
     *   预估工时：0.5d
     * =========================================================================
     */
}