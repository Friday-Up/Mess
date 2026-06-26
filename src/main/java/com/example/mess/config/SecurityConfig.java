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
 * Spring Security安全配置
 * 
 * 使用Spring Security 6.x Lambda DSL风格配置安全策略。
 * 
 * 授权规则:
 * - /hello/**, /swagger-ui/**, /v3/api-docs/**, /login → 公开访问
 * - 其他请求 → 需要认证
 * 
 * 认证方式: 表单登录 + 内存用户存储（仅用于演示）
 * 用户: user/password(ROLE_USER), admin/admin(ROLE_ADMIN)
 * 
 * 注意: 生产环境需替换为数据库用户存储、BCrypt密码加密、HTTPS等。
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /** 配置安全过滤器链：授权规则、表单登录、CSRF禁用（REST API场景） */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/hello/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .permitAll()
            )
            .logout(logout -> logout.permitAll())
            .csrf(csrf -> csrf.disable());
        
        return http.build();
    }

    /** 配置内存用户: user/password(USER角色), admin/admin(ADMIN角色)。仅用于演示 */
    @Bean
    public InMemoryUserDetailsManager userDetailsService() {
        UserDetails user = User.withDefaultPasswordEncoder()
            .username("user")
            .password("password")
            .roles("USER")
            .build();
        
        UserDetails admin = User.withDefaultPasswordEncoder()
            .username("admin")
            .password("admin")
            .roles("ADMIN")
            .build();
        
        return new InMemoryUserDetailsManager(user, admin);
    }
}