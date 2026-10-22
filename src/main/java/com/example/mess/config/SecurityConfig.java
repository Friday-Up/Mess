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
     * 【Spring 内幕】@Configuration 的 CGLIB 增强与 BeanDefinition 注册机制（非可执行代码）
     * =========================================================================
     *
     * 一、Full 模式 vs Lite 模式：@Configuration 与 @Component 的关键差异
     *   本类的 @Configuration 让 ConfigurationClassPostProcessor 走 Full 模式：
     *   - Full 模式：配置类会被 CGLIB 生成子类（Enhancer），任何 @Bean 方法
     *     调用同类中其他 @Bean 方法都会被拦截并走 BeanFactory.getBean()，
     *     确保返回的是同一个单例 bean
     *   - Lite 模式（proxyBeanMethods = false 或 @Component）：类不被代理，
     *     @Bean 方法间的直接调用等价于普通 Java 方法调用，每次都 new 新对象
     *   演示差异：假设本类有两个 @Bean 方法
     *     @Bean A a() { return new A(b()); }
     *     @Bean B b() { return new B(); }
     *   Full 模式下 a() 中的 b() 被拦截 -> 返回容器中已有的 B 单例；
     *   Lite 模式下 a() 中的 b() 是原生调用 -> new 出第二个 B
     *   Spring Boot 2.2+ 的自动配置类大量使用 Lite 模式以避免 CGLIB 开销
     *   （配置类里方法互不调用时 proxyBeanMethods=false 是最佳实践）
     *
     * 二、@Configuration 类如何被解析为 BeanDefinition
     *   1) ConfigurationClassPostProcessor（BeanFactoryPostProcessor）在
     *      refresh() 第 5 步被触发，扫描所有 @Configuration 类
     *   2) ConfigurationClassParser 解析本类：
     *      - 类级注解 @EnableWebSecurity 上有 @Import（引入安全核心配置类）
     *      - @Bean 注解的方法 securityFilterChain / userDetailsService 各生成
     *        一个 ConfigurationClassBeanDefinition，beanClass 指向工厂方法
     *        所在类，factoryMethodName 记录方法名
     *   3) BeanDefinition 元数据：scope=singleton、lazyInit=false、
     *      primary=false、依赖关系（@DependsOn）等
     *   4) @Bean 方法间的依赖顺序由方法参数决定：
     *      securityFilterChain(HttpSecurity http) 需要注入 HttpSecurity bean，
     *      Spring 推断 BeanDefinition 依赖：先创建 HttpSecurity -> 再构建本方法
     *
     * 三、BeanPostProcessor：所有 Bean 实例化后的流水线工位
     *   本类产生的两个 Bean 都会流经已注册的后置处理器链：
     *   - AutowiredAnnotationBeanPostProcessor：处理 @Autowired（本类方法
     *     参数注入即其职责延伸）
     *   - CommonAnnotationBeanPostProcessor：处理 @PostConstruct / @PreDestroy
     *   - ApplicationContextAwareProcessor：回调 Aware 接口
     *   - Servlet 环境：ServletContextAwareProcessor / WebApplicationContextAwareProcessor
     *   每个后置处理器有 postProcessBeforeInitialization 与
     *   postProcessAfterInitialization 两个钩子，初始化注解、Aware 回调、
     *   AOP 代理生成都在这里发生
     *   生产提示：后置处理器数量是启动性能的最大变量之一，过多会使每个 Bean
     *   实例化都经过大量判断（Spring Boot 空项目约 12 个后置处理器）
     *
     * 四、生产环境安全配置的演进清单
     *   1) 弃用 InMemoryUserDetailsManager + withDefaultPasswordEncoder，
     *      改为 JdbcUserDetailsManager 或自定义 UserDetailsService + BCrypt，
     *      cost 因子 >=10 保证单次哈希 >100ms，砌慢暴力破解
     *   2) 会话固定保护默认开启（migrateSession）：登录后换新 sessionId 防劫持
     *   3) Remember-Me（RememberMeConfigurer）：TokenBasedRememberMeServices
     *      与 PersistentTokenRepository 任选，后者更安全（落库可吊销）
     *   4) CORS 必须显式 .cors(...) + CorsConfigurationSource Bean
     *   5) HTTPS：容器层 TLS 终止或 requiresChannel().anyRequest().requiresSecure()
     *   6) 审计事件：注入 AuthenticationSuccessListener 即可捕获登录事件，供风控使用
     *
     * 五、SecurityFilterChain 多链匹配与 RequestMatcher
     *   1) 一个 WebSecurityCustomizer / HttpSecurity 可注册多条链，
     *      FilterChainProxy 按 RequestMatcher 的注册顺序逐一匹配，
     *      命中第一条链即处理请求（后面的链不再执行）
     *   2) 多租户示例（本项目演进方向）：
     *      securityFilterChainApi(HttpSecurity http)：匹配 /api/**，无状态 JWT
     *      securityFilterChainWeb(HttpSecurity http)：其余请求，表单登录
     *      -> 二者用 @Order 与 securityMatcher("/api/**") 区分
     *   3) @Bean 方法返回类型相同（SecurityFilterChain）不会冲突，
     *      Spring Boot 3.x 已不再需要设置 bean 名称来区分多条链
     *   4) 匹配规则常用：requestMatchers().hasRole() / hasAuthority()
     *      / access((auth, ctx) -> ...)（SpEL 或自定义 AuthorizationManager）
     *   5) 方法级安全：@EnableMethodSecurity + @PreAuthorize("hasRole('ADMIN')")
     *      在 Service 方法上再拦一层，与 URL 级规则互补
     * =========================================================================
     */
}