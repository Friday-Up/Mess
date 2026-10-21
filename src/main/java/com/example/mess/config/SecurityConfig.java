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
     * 【分布式安全】微服务认证授权架构（非可执行代码）
     * =========================================================================
     *
     * 一、微服务认证架构演进
     *   单体：Session 存在应用内存，Cookie 关联 Session
     *   集群：Session 共享（Spring Session + Redis）
     *   微服务：无状态 Token（JWT），每个服务独立验证
     *
     * 二、JWT 在微服务中的流转
     *   1) 登录：用户 -> 网关 -> 认证服务 -> 验证凭证 -> 签发 JWT
     *   2) 携带：前端将 JWT 存 localStorage，每次请求放 Authorization: Bearer <jwt>
     *   3) 网关验证：网关统一验证 JWT 签名/过期，无效直接 401
     *   4) 传递：网关将用户信息放 Header（X-User-Id/X-User-Roles）转发给后端
     *   5) 后端信任：后端从 Header 读用户信息，不再验 JWT（信任网关）
     *      注意：后端需确保请求来自网关（内网隔离/mTLS/网关 IP 白名单）
     *   6) 服务间调用：服务 A 调服务 B 时，透传原始 JWT 或用服务 Token
     *
     * 三、OAuth2 / OIDC（开放授权 / 开放身份连接）
     *   场景：第三方登录（微信/GitHub/Google）、SSO（单点登录）
     *   角色：
     *   - 资源所有者：用户
     *   - 客户端：第三方应用
     *   - 授权服务器：签发 Token（Keycloak/Auth0/自建）
     *   - 资源服务器：业务服务（验证 Token）
     *   流程（授权码模式）：
     *   1) 用户点击"微信登录" -> 跳转微信授权页
     *   2) 用户授权 -> 微信返回授权码 -> 客户端用授权码换 Token
     *   3) 客户端带 Token 请求资源服务器 -> 资源服务器验证 Token
     *   Spring Security OAuth2：
     *   - @EnableWebSecurity + oauth2ResourceServer().jwt() -> 资源服务器
     *   - Spring Authorization Server -> 授权服务器
     *
     * 四、服务间认证（mTLS / Service Token）
     *   网关到后端：信任网关 Header（内网隔离）
     *   服务到服务：
     *   1) mTLS（双向 TLS）：服务 A 和服务 B 互验证书，Istio 自动注入
     *   2) Service Token：服务 A 用自身凭证（API Key/服务 JWT）调服务 B
     *   3) 服务账号：K8s ServiceAccount + RBAC
     *   零信任网络：不信任内网，所有调用都需认证（mTLS + 权限）
     *
     * 五、权限模型
     *   1) RBAC（基于角色）：用户 -> 角色 -> 权限
     *      如：admin 角色 -> user:delete 权限
     *      实现：Spring Security hasRole/hasAuthority
     *   2) ABAC（基于属性）：根据用户/资源/环境属性动态判断
     *      如：用户是部门 A 的，只能看部门 A 的数据
     *      实现：自定义 PermissionEvaluator
     *   3) ACL（访问控制列表）：每个资源配置可访问的用户列表
     *      如：文档 D1 -> [user1 可读, user2 可写]
     *   建议：简单场景 RBAC，复杂场景 RBAC + 数据权限（行级过滤）
     * =========================================================================
     */
}