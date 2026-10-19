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
     * 【认证安全】OWASP A07 身份识别和认证失效防护（非可执行代码）
     * =========================================================================
     *
     * 一、密码安全生命周期
     *   1) 存储：BCrypt（cost>=10），绝不明文/MD5/SHA1
     *   2) 传输：HTTPS 加密传输，登录页强制 HTTPS
     *   3) 重置：Token 一次性 + 短 TTL（15分钟），Token 哈希存储
     *   4) 修改：需验证旧密码
     *   5) 强度：>=8位，含大小写/数字/特殊字符，检查弱密码字典（top10000）
     *   6) 过期：90天强制修改（视业务，有争议，NIST 建议不强制定期换）
     *
     * 二、会话/Token 安全
     *   Session 方案：
     *   [ ] Session ID 用 SecureRandom 生成，足够长（>=128 bit）
     *   [ ] Cookie 标志：HttpOnly（防 XSS 窃取）+ Secure（仅 HTTPS）+ SameSite=Lax/Strict（防 CSRF）
     *   [ ] Session 超时：30分钟无操作失效
     *   [ ] 登录后重新生成 Session ID（防 Session Fixation）
     *   [ ] 登出时销毁 Session（invalidate()）
     *   JWT 方案：
     *   [ ] 密钥强度：>=256 bit，密钥轮转（kid 头标识版本）
     *   [ ] 算法：HS256 或 RS256，禁用 none 算法
     *   [ ] Payload 最小化：不放敏感数据（密码/手机号），JWT 可被解码
     *   [ ] TTL 短：Access Token 15分钟 + Refresh Token 7天
     *   [ ] Refresh Token 一次性使用，用后轮转
     *   [ ] 黑名单：登出/改密后 Token 加入 Redis 黑名单
     *
     * 三、暴力破解防护
     *   1) 失败计数：5 次失败锁定 15 分钟（账号维度 + IP 维度）
     *   2) 验证码：3 次失败后要求图形验证码/滑块
     *   3) 限流：登录接口限流（10 req/min/IP）
     *   4) 告警：连续失败触发安全告警
     *   5) 延迟响应：失败后 sleep(1s)，增加暴力破解成本
     *
     * 四、多因素认证（MFA）
     *   1) 短信验证码：二次验证（SMS OTP）
     *   2) TOTP：Google Authenticator（时间基一次性密码）
     *   3) WebAuthn：硬件密钥/指纹（最强）
     *   4) 适用场景：管理员账户/敏感操作/金融场景
     * =========================================================================
     */

    /*
     * =========================================================================
     * 【安全补充】CSRF/SSRF/点击劫持防护（非可执行代码）
     * =========================================================================
     *
     * 一、CSRF（跨站请求伪造）- OWASP A01
     *   原理：用户已登录 A 站，访问恶意 B 站，B 站自动向 A 站发请求（带 Cookie）
     *   防护：
     *   1) Token 方案：服务端生成 CSRF Token，前端表单携带，服务端校验
     *   2) SameSite Cookie：SameSite=Lax/Strict，跨站请求不带 Cookie
     *   3) 验证 Origin/Referer：检查请求来源是否本站
     *   4) 本项目禁用 CSRF 的理由：走 Token 认证（非 Cookie），CSRF 不适用
     *      若恢复 Cookie 认证，必须重新启用 CSRF
     *
     * 二、SSRF（服务端请求伪造）
     *   原理：攻击者让服务端向内网发起请求（如请求元数据服务 169.254.169.254）
     *   场景：用户输入 URL，服务端去请求（如"图片代理"/"URL 预览"功能）
     *   防护：
     *   1) URL 白名单：只允许 http/https，只允许特定域名
     *   2) 禁止内网：解析 DNS 后校验 IP 不在私有网段（10.x/172.16.x/192.168.x/127.x）
     *   3) 禁止重定向：跟随重定向后再次校验目标 IP
     *   4) 云元数据：禁止 169.254.169.254（AWS/GCP 元数据接口）
     *
     * 三、点击劫持（Clickjacking）
     *   原理：恶意网站用 iframe 嵌入目标网站，用户点击被劫持
     *   防护：
     *   1) X-Frame-Options: DENY（禁止 iframe 嵌入）
     *      Spring Security: .headers().frameOptions().deny()
     *   2) CSP frame-ancestors: 'none'（更现代的替代方案）
     *   3) JavaScript 防护：if (window.top !== window.self) window.top.location = window.self.location
     *
     * 四、安全响应头完整清单
     *   X-Content-Type-Options: nosniff（禁止 MIME 嗅探）
     *   X-Frame-Options: DENY（防点击劫持）
     *   X-XSS-Protection: 1; mode=block（旧版浏览器 XSS 过滤）
     *   Content-Security-Policy: default-src 'self'（CSP，最强 XSS 防护）
     *   Strict-Transport-Security: max-age=31536000; includeSubDomains（强制 HTTPS）
     *   Referrer-Policy: strict-origin-when-cross-origin（控制 Referrer 泄露）
     *   Permissions-Policy: camera=(), microphone=(), geolocation=()（禁用敏感 API）
     *   Spring Security 配置：
     *   .headers().contentTypeOptions().frameOptions().xssProtection()
     *              .httpStrictTransportSecurity().referrerPolicy()
     *              .permissionsPolicy()
     * =========================================================================
     */
}