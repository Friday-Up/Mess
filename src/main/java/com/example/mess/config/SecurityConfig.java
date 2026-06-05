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
 * Spring Security安全配置类
 * 
 * 配置应用的安全策略，包括认证和授权规则。
 * 使用Spring Security 6.x的Lambda DSL风格进行配置。
 * 
 * 设计原则:
 * - 最小权限原则：默认拒绝所有请求，只开放必要的端点
 * - 公开API优先：问候服务和API文档无需认证
 * - 认证保护：用户管理API需要认证后访问
 * - 简单认证：使用内存存储的用户信息（仅用于演示）
 * 
 * 安全策略:
 * - /hello/**: 公开访问，无需认证
 * - /swagger-ui/**, /v3/api-docs/**: 公开访问，API文档
 * - /login: 公开访问，登录页面
 * - 其他所有请求: 需要认证
 * 
 * 认证方式:
 * - 表单登录: 适用于浏览器访问
 * - 内存用户存储: 适用于开发和演示环境
 * - CSRF禁用: 适用于REST API场景
 * 
 * 用户配置:
 * - user/password: 普通用户角色（USER）
 * - admin/admin: 管理员角色（ADMIN）
 * 
 * 安全警告:
 * - 当前配置仅用于演示，生产环境需要:
 *   1. 使用数据库存储用户信息
 *   2. 密码加密存储（BCrypt）
 *   3. 启用CSRF保护
 *   4. 配置HTTPS
 *   5. 实现JWT或OAuth2认证
 *   6. 配置CORS策略
 * 
 * @Configuration 注解标识这是一个配置类，由Spring容器管理
 * @EnableWebSecurity 注解启用Spring Security的Web安全功能
 * 
 * 作者: zhangyaolong.5
 * 创建时间: 2026-05-26
 * 
 * @see SecurityFilterChain 安全过滤器链
 * @see InMemoryUserDetailsManager 内存用户详情管理器
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * 配置安全过滤器链
     * 
     * 定义HTTP请求的安全策略，包括授权规则、登录配置和CSRF设置。
     * 使用Lambda DSL风格，比传统的链式调用更清晰和安全。
     * 
     * 授权规则（按优先级从高到低）:
     * 1. /hello/** - 允许所有用户访问（公开API）
     * 2. /swagger-ui/** - 允许所有用户访问（Swagger UI界面）
     * 3. /v3/api-docs/** - 允许所有用户访问（OpenAPI文档）
     * 4. /login - 允许所有用户访问（登录页面）
     * 5. 其他所有请求 - 需要认证
     * 
     * 登录配置:
     * - 使用Spring Security提供的默认登录页面
     * - 登录页面路径: /login
     * - 登录成功后重定向到之前访问的页面
     * 
     * CSRF配置:
     * - 当前禁用CSRF保护
     * - 原因: REST API通常使用无状态的Token认证
     * - 注意: 生产环境如使用Cookie认证应启用CSRF
     * 
     * 安全过滤器链执行流程:
     * 1. 请求到达SecurityFilterChain
     * 2. 检查请求路径是否匹配公开端点
     * 3. 如果需要认证，检查是否已登录
     * 4. 未登录则重定向到登录页面
     * 5. 已登录则检查授权规则
     * 6. 授权通过则继续处理请求
     * 
     * @param http HttpSecurity对象，用于配置HTTP安全策略
     * @return SecurityFilterChain 配置好的安全过滤器链
     * @throws Exception 配置过程中可能抛出的异常
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 配置授权规则
            .authorizeHttpRequests(auth -> auth
                // 问候服务API公开访问，无需认证
                .requestMatchers("/hello/**").permitAll()
                // Swagger UI和OpenAPI文档公开访问
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                // 其他所有请求需要认证
                .anyRequest().authenticated()
            )
            // 配置表单登录
            .formLogin(form -> form
                // 使用默认登录页面，路径为/login
                .loginPage("/login")
                // 允许所有用户访问登录页面
                .permitAll()
            )
            // 配置登出功能
            .logout(logout -> logout
                // 允许所有用户访问登出端点
                .permitAll()
            )
            // 禁用CSRF保护
            // 注意：REST API场景下通常禁用，因为使用Token认证
            // 生产环境如使用Cookie认证应启用CSRF
            .csrf(csrf -> csrf.disable());
        
        return http.build();
    }

    /**
     * 配置内存用户详情管理器
     * 
     * 创建两个内存用户，用于开发和演示环境。
     * 使用InMemoryUserDetailsManager存储用户信息。
     * 
     * 用户配置:
     * - user/password: 普通用户，拥有USER角色
     *   - 用于普通API访问
     *   - 可访问用户管理API
     * 
     * - admin/admin: 管理员，拥有ADMIN角色
     *   - 用于管理操作
     *   - 可访问所有API（未来可扩展权限控制）
     * 
     * 密码编码:
     * - 使用withDefaultPasswordEncoder()进行密码编码
     * - 注意：此方法仅用于演示，不推荐生产使用
     * - 生产环境应使用BCryptPasswordEncoder
     * 
     * InMemoryUserDetailsManager说明:
     * - 用户信息存储在内存中，应用重启后丢失
     * - 适用于开发和测试环境
     * - 不适用于生产环境（应使用数据库存储）
     * - 实现了UserDetailsManager接口
     * 
     * 安全建议:
     * - 生产环境替换为JdbcUserDetailsManager或自定义UserDetailsService
     * - 密码使用BCrypt加密存储
     * - 实现密码复杂度验证
     * - 添加账户锁定机制
     * - 添加密码过期策略
     * 
     * @return InMemoryUserDetailsManager 配置好的内存用户详情管理器
     */
    @Bean
    public InMemoryUserDetailsManager userDetailsService() {
        // 创建普通用户
        // 用户名: user, 密码: password, 角色: USER
        UserDetails user = User.withDefaultPasswordEncoder()
            .username("user")
            .password("password")
            .roles("USER")
            .build();
        
        // 创建管理员用户
        // 用户名: admin, 密码: admin, 角色: ADMIN
        UserDetails admin = User.withDefaultPasswordEncoder()
            .username("admin")
            .password("admin")
            .roles("ADMIN")
            .build();
        
        // 返回包含两个用户的内存用户详情管理器
        return new InMemoryUserDetailsManager(user, admin);
    }
}