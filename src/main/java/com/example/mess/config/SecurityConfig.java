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
     * 【Java 并发】SecurityContext 的线程传播、ThreadLocal 策略与异步链路（非可执行代码）
     * =========================================================================
     *
     * 一、SecurityContextHolder 的四种策略（MODE_*）
     *   1) MODE_THREADLOCAL（默认）：上下文存 ThreadLocal，绑定当前请求线程
     *   2) MODE_INHERITABLETHREADLOCAL：子线程会"继承"父线程上下文副本
     *      用途有限：仅对"主线程直接 new Thread"的场景生效
     *      局限：Tomcat 线程池复用线程 -> 不会走继承路径，上下文丢失
     *   3) MODE_GLOBAL：静态 JVM 全局共享，测试代码偶尔使用，Web 勿用
     *   4) 自定义策略：实现 SecurityContextHolderStrategy 并注册，
     *      响应式项目里常用"基于 Reactor Context"的实现
     *
     * 二、Tomcat 线程池与 InheritableThreadLocal 的错配
     *   1) 期望场景：请求线程启动异步任务，任务线程自动拿到用户身份
     *   2) 实际行为：Tomcat 工作线程从线程池取线程，此线程不是
     *      "当前请求线程 new 出来的子线程"，不走 Inheritable 逻辑
     *   3) 后果：开 MODE_INHERITABLETHREADLOCAL 后，从池中借到的线程
     *      还带着上一个请求残留的安全上下文 -> 身份串号 + 越权风险
     *   4) Spring 的解法：DelegatingSecurityContextRunnable/Callable 包装，
     *      把父线程上下文复制进任务再传给线程池（TaskDecorator 配合）
     *   5) Spring Security 的 WebAsyncManagerIntegrationFilter 正是
     *      自动为 @Async、DeferredResult、Callable 完成这个复制与清理
     *
     * 三、SecurityContext 在 @Async 场景的正确姿势
     *   1) 场景：Controller 里 @Async 方法记录审计日志，日志需要用户信息
     *   2) 步骤：
     *      - 全局配置 TaskDecorator：复制 SecurityContext、MDC、RequestContextHolder
     *      - 线程执行完任务后清理（finally），否则线程复用引起串号
     *   3) 代码骨架：
     *      class ContextTaskDecorator implements TaskDecorator {
     *         public Runnable decorate(Runnable task) {
     *            var ctx = SecurityContextHolder.getContext();
     *            var mdc = MDC.getCopyOfContextMap();
     *            return () -> {
     *               try { SecurityContextHolder.setContext(ctx); if (mdc != null) MDC.setContextMap(mdc); task.run(); }
     *               finally { SecurityContextHolder.clearContext(); MDC.clear(); }
     *            };
     *         }
     *      }
     *   4) 注意：setContext 传引用是安全的，因为 Authentication 绝大多数实现
     *      不可变或构造后不修改（UsernamePasswordAuthenticationToken 字段定后不变）
     *
     * 四、Spring Security 的并发相关隐患清单
     *   1) SecurityContextHolderStrategy 被自定义为按请求存储但未在 Filter
     *      末尾清理 -> 线程复用时身份泄漏（安全漏洞级别，高于脏数据）
     *   2) 使用 SecurityContextRepository 手动 saveContext 但未走
     *      SecurityContextHolderFilter 的标准链路 -> 读写不一致
     *   3) CSRF 禁用（本类 disable）只对无状态 API 安全，
     *      若未来要加表单登录/Session 功能，必须恢复 CSRF
     *   4) 并发会话控制：maximumSessions(n).maxSessionsPreventsLogin(true)
     *      会基于 SessionRegistry 做并发登录限制；与并发线程无关，
     *      是"同一用户的会话数上限"，配版权系统时常用
     *
     * 五、线程爆炸的检查表（安全相关）
     *   1) Tomcat 工作线程：jstack 看不到空闲 -> accept 排队 -> 连接拒绝
     *   2) @Async 线程：队列堆积 -> 内存涨 -> 满了走拒绝策略
     *   3) 定时任务：若 run() 阻塞且未配并发控制 -> 下次触发又起一线程
     *      （Spring 的 ThreadPoolTaskScheduler 不会为同任务并发调度）
     *   4) 缓存加载线程：大 key 全量预热时占满池 -> 业务请求排队等待
     *      缓存初始化完成后才恢复 -> 用低优先级或独立池隔离
     * =========================================================================
     */
}