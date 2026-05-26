package com.example.mess.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security配置类
 * 配置应用的安全策略，包括认证、授权、密码加密等
 * 
 * @Configuration 标识这是一个配置类，会被Spring自动加载
 * @EnableWebSecurity 启用Spring Security的Web安全支持
 * 
 * 主要功能:
 * - 配置HTTP请求的安全策略
 * - 配置用户认证信息
 * - 配置密码加密方式
 * - 配置跨站请求伪造(CSRF)防护
 * 
 * 安全策略说明:
 * - 使用HTTP Basic认证
 * - 禁用CSRF（适用于REST API）
 * - 配置URL级别的访问权限
 * - 使用内存存储的用户信息（演示用）
 * 
 * 作者: zhangyaolong.5
 * 创建时间: 2026-05-26
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * 配置安全过滤器链
     * 定义哪些URL需要认证，哪些可以匿名访问
     * 
     * @param http HttpSecurity对象，用于配置安全策略
     * @return SecurityFilterChain 安全过滤器链
     * @throws Exception 配置过程中可能抛出的异常
     * 
     * 配置策略:
     * 1. 禁用CSRF保护 - 适用于REST API
     * 2. 配置URL访问权限
     * 3. 配置H2控制台的frame选项
     * 4. 启用HTTP Basic认证
     * 
     * URL权限配置:
     * - /api/users/**: 需要认证
     * - /h2-console/**: 允许匿名访问（开发环境）
     * - /swagger-ui/**, /v3/api-docs/**: 允许匿名访问（API文档）
     * - /, /hello: 允许匿名访问（测试接口）
     * - 其他所有请求: 需要认证
     * 
     * 注意: 在生产环境中，应该：
     * - 启用CSRF保护
     * - 使用数据库存储用户信息
     * - 配置更复杂的权限控制
     * - 使用JWT或OAuth2等现代认证方式
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 禁用CSRF保护
            // 注意：在生产环境中应该启用CSRF保护
            // 这里禁用是因为我们开发的是REST API，使用JWT认证
            .csrf(AbstractHttpConfigurer::disable)
            
            // 配置URL访问权限
            .authorizeHttpRequests(auth -> auth
                // 用户管理API需要认证
                // 只有登录用户才能访问用户相关的API
                .requestMatchers("/api/users/**").authenticated()
                
                // H2控制台允许匿名访问
                // 这是为了方便开发环境调试数据库
                // 生产环境应该移除或添加更严格的限制
                .requestMatchers("/h2-console/**").permitAll()
                
                // Swagger API文档允许匿名访问
                // 方便开发和测试API
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                
                // 基础问候接口允许匿名访问
                // 用于测试应用是否正常运行
                .requestMatchers("/", "/hello").permitAll()
                
                // 其他所有请求都需要认证
                // 这是一种安全策略，默认拒绝访问
                .anyRequest().authenticated()
            )
            
            // 配置H2控制台的frame选项
            // H2控制台使用iframe，需要禁用X-Frame-Options
            // 注意：这可能会带来点击劫持风险，生产环境需要谨慎配置
            .headers(headers -> headers.frameOptions().disable())
            
            // 启用HTTP Basic认证
            // 客户端需要在请求头中添加: Authorization: Basic base64(username:password)
            // 这是一种简单的认证方式，适合内部API或开发环境
            // 生产环境建议使用JWT或OAuth2
            .httpBasic();
        
        // 构建并返回安全过滤器链
        return http.build();
    }

    /**
     * 配置用户详细信息服务
     * 这里使用内存存储的用户信息，仅用于演示
     * 
     * @return UserDetailsService 用户详情服务
     * 
     * 用户信息:
     * - admin/admin123: 管理员用户
     * - user/user123: 普通用户
     * 
     * 密码加密:
     * - 使用BCryptPasswordEncoder进行加密
     * - 明文密码在存储前会被加密
     * 
     * 角色配置:
     * - admin: ADMIN角色
     * - user: USER角色
     * 
     * 注意: 在生产环境中，应该：
     * - 使用数据库存储用户信息
     * - 实现自定义的UserDetailsService
     * - 支持动态用户管理
     * 
     * 安全警告:
     * - 内存中的用户信息在应用重启后会丢失
     * - 密码虽然是加密存储，但仍需妥善保管
     */
    @Bean
    public UserDetailsService userDetailsService() {
        // 创建管理员用户
        // 用户名: admin
        // 密码: admin123 (会被BCrypt加密)
        // 角色: ADMIN
        UserDetails admin = User.builder()
            .username("admin")                    // 用户名
            .password(passwordEncoder().encode("admin123"))  // 加密后的密码
            .roles("ADMIN")                       // 角色，会自动添加"ROLE_"前缀
            .build();
            
        // 创建普通用户
        // 用户名: user
        // 密码: user123 (会被BCrypt加密)
        // 角色: USER
        UserDetails user = User.builder()
            .username("user")                     // 用户名
            .password(passwordEncoder().encode("user123"))   // 加密后的密码
            .roles("USER")                        // 角色，会自动添加"ROLE_"前缀
            .build();
            
        // 使用内存用户详情管理器
        // 在生产环境中应该替换为数据库实现
        return new InMemoryUserDetailsManager(admin, user);
    }

    /**
     * 配置密码编码器
     * 用于密码的加密和验证
     * 
     * @return PasswordEncoder 密码编码器实例
     * 
     * 使用BCryptPasswordEncoder的原因:
     * 1. 安全性高: BCrypt是业界标准的密码哈希算法
     * 2. 自适应: 可以调整计算强度，抵御暴力破解
     * 3. 内置盐值: 自动为每个密码生成随机盐值
     * 4. Spring Security推荐: 与框架完美集成
     * 
     * 配置建议:
     * - 强度(strength)参数可以调整，默认为10
     * - 数值越大，加密越慢，安全性越高
     * - 生产环境建议使用12或更高
     * 
     * 注意:
     * - 同一个密码每次加密结果都不同（因为盐值不同）
     * - 验证时不需要知道原始盐值，BCrypt会自动处理
     * 
     * 示例:
     * String encodedPassword = passwordEncoder.encode("rawPassword");
     * boolean matches = passwordEncoder.matches("rawPassword", encodedPassword);
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        // 使用BCrypt密码编码器
        // 强度参数使用默认值10
        // 可以根据服务器性能进行调整
        return new BCryptPasswordEncoder();
    }
}