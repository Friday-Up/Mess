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
     *============================================================================
     * 【设计文档】SecurityConfig 安全策略设计说明（补充文档，非可执行代码）
     * ============================================================================
     *
     * 一、职责定位
     * ----------------------------------------------------------------------------
     * SecurityConfig 是 Spring Security 的中枢配置类，负责定义：
     *   1) 过滤链（SecurityFilterChain）：请求进入业务前经过的安全过滤规则;
     *   2) 认证源（UserDetailsService）：系统认可的用户及其凭证、角色;
     *   3) 密码编码策略（PasswordEncoder）：凭证的加密与校验方式;
     *   4) CSRF / 会话 / 授权等横切安全策略。
     *
     * 二、当前安全策略要点
     * ----------------------------------------------------------------------------
     *   - CSRF：已禁用。适用于无状态 REST API（凭 Token/Basic 认证）场景，
     *          否则每个 POST/DELETE 都需携带 CSRF Token，与前后端分离风格不符;
     *   - 认证方式：基于内存用户（InMemoryUserDetailsManager），仅用于演示/测试;
     *   - 授权规则：由过滤链中的 authorizeHttpRequests 定义放行与保护路径。
     *
     * 三、关键设计决策
     * ----------------------------------------------------------------------------
     * 决策 1：使用内存用户而非数据库用户
     *   理由：本项目侧重演示，内存用户零依赖、易于测试；生产环境应替换为
     *        基于 UserRepository 的 UserDetailsService 实现。
     * 决策 2：禁用 CSRF
     *   理由：REST API 采用无状态认证，CSRF 主要防御基于 Cookie 的会话攻击，
     *        无状态场景下收益有限却增加调用复杂度。
     * 决策 3：使用 BCryptPasswordEncoder（若已配置）
     *   理由：BCrypt 自带盐值且计算强度可调，抗彩虹表与暴力破解，是业界推荐算法。
     *
     * 四、安全警示
     * ----------------------------------------------------------------------------
     *   !! 内存用户的用户名/密码若硬编码在源码中，切勿用于生产环境;
     *   !! 禁用 CSRF 的前提是"无状态 + 非 Cookie 会话"，若改用会话认证需重新评估;
     *   !! 生产环境应启用 HTTPS，避免 Basic 认证凭证在传输中被窃听。
     *
     * 五、迁移到生产的检查清单
     * ----------------------------------------------------------------------------
     *   [ ] 用数据库用户替换内存用户（实现 UserDetailsService 查 UserRepository）;
     *   [ ] 用环境变量/密钥管理服务托管敏感凭证，杜绝硬编码;
     *   [ ] 按最小权限原则细化 authorizeHttpRequests 授权矩阵;
     *   [ ] 引入 JWT 或 OAuth2 资源服务器完善无状态认证;
     *   [ ] 配置 CORS 白名单，避免任意来源跨域;
     *   [ ] 开启 HTTPS 与安全响应头（HSTS、X-Content-Type-Options 等）。
     *
     * 六、与其他组件的关系
     * ----------------------------------------------------------------------------
     *   - 保护 UserController / HelloController 等所有 Web 端点;
     *   - 与 GlobalExceptionHandler 协同：认证/授权失败由 Security 过滤链处理，
     *     业务异常由 GlobalExceptionHandler 处理，二者职责边界不同;
     *   - 测试中可通过 @WithMockUser 或关闭安全配置来简化端点测试。
     * ============================================================================
     */
}