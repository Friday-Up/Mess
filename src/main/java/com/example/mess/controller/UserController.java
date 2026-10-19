package com.example.mess.controller;

import com.example.mess.dto.ApiResponse;
import com.example.mess.dto.UserDto;
import com.example.mess.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器 - 提供用户管理RESTful API，遵循REST架构风格。
 * 
 * <p>控制器职责:
 * <ul>
 *   <li>接收HTTP请求，参数校验和绑定</li>
 *   <li>调用Service层业务逻辑</li>
 *   <li>返回标准化的ApiResponse响应</li>
 *   <li>不包含业务逻辑，保持控制器薄层</li>
 * </ul>
 * 
 * <p>RESTful端点设计:
 * <table border="1">
 *   <tr><th>HTTP方法</th><th>路径</th><th>说明</th><th>请求体</th><th>响应体</th></tr>
 *   <tr><td>GET</td><td>/api/users</td><td>分页获取用户列表</td><td>-</td><td>Page&lt;UserDto&gt;</td></tr>
 *   <tr><td>GET</td><td>/api/users/{id}</td><td>获取用户详情</td><td>-</td><td>UserDto</td></tr>
 *   <tr><td>POST</td><td>/api/users</td><td>创建新用户</td><td>UserDto</td><td>UserDto</td></tr>
 *   <tr><td>PUT</td><td>/api/users/{id}</td><td>全量更新用户</td><td>UserDto</td><td>UserDto</td></tr>
 *   <tr><td>DELETE</td><td>/api/users/{id}</td><td>删除用户</td><td>-</td><td>Void</td></tr>
 * </table>
 * 
 * <p>分页参数说明:
 * <ul>
 *   <li><b>page</b> - 页码从0开始，默认0（第一页）</li>
 *   <li><b>size</b> - 每页大小，默认20</li>
 *   <li><b>sort</b> - 排序字段，格式: property,asc|desc（如: username,asc）</li>
 * </ul>
 * 
 * <p>安全策略:
 * <ul>
 *   <li>所有接口需要认证（SecurityConfig中配置）</li>
 *   <li>基础路径: /api/users</li>
 *   <li>使用@RestController确保所有响应为JSON格式</li>
 * </ul>
 * 
 * <p>异常处理:
 * <ul>
 *   <li>资源不存在 → 404 Not Found（由GlobalExceptionHandler处理）</li>
 *   <li>参数校验失败 → 400 Bad Request（Spring自动处理）</li>
 *   <li>服务器错误 → 500 Internal Server Error</li>
 * </ul>
 * 
 * @see com.example.mess.service.UserService 用户业务逻辑服务
 * @see com.example.mess.dto.UserDto 用户数据传输对象
 * @see com.example.mess.config.SecurityConfig 安全配置
 * @since 1.0
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "用户管理", description = "用户相关的API接口")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 获取用户列表（分页）。
     * <p>支持page、size、sort参数，返回分页的用户列表。
     * 使用Spring Data的Pageable自动解析分页参数。
     * 
     * <p>请求示例: GET /api/users?page=0&size=10&sort=username,asc
     * 
     * @param pageable 分页参数，由Spring自动从请求参数构建
     * @return 包含分页用户列表的ApiResponse
     */
    @GetMapping
    @Operation(summary = "获取用户列表", description = "获取所有用户的分页列表")
    public ApiResponse<Page<UserDto>> getAllUsers(Pageable pageable) {
        // 调用Service层获取分页用户数据，Pageable由Spring从请求参数(page/size/sort)自动构建
        // 使用ApiResponse.success包装结果，统一返回格式便于前端解析
        return ApiResponse.success(userService.getAllUsers(pageable));
    }

    /**
     * 根据ID获取用户详情。
     * <p>通过路径变量获取用户ID，查询数据库返回用户详情。
     * 用户不存在时由Service层抛出ResourceNotFoundException，返回404。
     * 
     * <p>请求示例: GET /api/users/1
     * 
     * @param id 用户ID，从URL路径中提取
     * @return 包含用户详情的ApiResponse
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取用户详情", description = "根据用户ID获取用户详细信息")
    public ApiResponse<UserDto> getUserById(@PathVariable Long id) {
        // @PathVariable从URL路径(/api/users/{id})中提取id
        // 委托Service查询用户详情，若不存在Service会抛出异常由全局处理器返回404
        return ApiResponse.success(userService.getUserById(id));
    }

    /**
     * 创建新用户。
     * <p>接收UserDto JSON请求体，创建新用户记录。
     * username和email需唯一（由数据库唯一约束保证），id和createdAt由系统自动生成。
     * 
     * <p>请求示例: POST /api/users
     * <pre>{@code
     * {
     *   "username": "newuser",
     *   "email": "newuser@example.com",
     *   "name": "New User"
     * }
     * }</pre>
     * 
     * @param userDto 用户数据，从请求体JSON反序列化
     * @return 包含创建成功的用户信息的ApiResponse
     */
    @PostMapping
    @Operation(summary = "创建用户", description = "创建新的用户")
    public ApiResponse<UserDto> createUser(@RequestBody UserDto userDto) {
        // @RequestBody将请求体JSON反序列化为UserDto对象
        // 委托Service完成持久化，返回包含自动生成id和createdAt的用户信息
        return ApiResponse.success(userService.createUser(userDto));
    }

    /**
     * 更新用户信息（全量更新）。
     * <p>根据ID更新用户所有可修改字段。id和createdAt不可修改（由系统管理）。
     * 用户不存在时返回404。使用PUT方法符合REST全量更新语义。
     * 
     * <p>请求示例: PUT /api/users/1
     * <pre>{@code
     * {
     *   "username": "updateduser",
     *   "email": "updated@example.com",
     *   "name": "Updated User"
     * }
     * }</pre>
     * 
     * @param id 用户ID，从URL路径中提取
     * @param userDto 用户更新数据，从请求体JSON反序列化
     * @return 包含更新后用户信息的ApiResponse
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新用户", description = "根据用户ID更新用户信息")
    public ApiResponse<UserDto> updateUser(@PathVariable Long id, @RequestBody UserDto userDto) {
        // 同时接收路径变量id（定位待更新用户）和请求体userDto（新的字段值）
        // 委托Service执行全量更新，返回更新后的用户信息
        return ApiResponse.success(userService.updateUser(id, userDto));
    }

    /**
     * 删除用户（物理删除，不可恢复）。
     * <p>根据ID从数据库中永久删除用户记录。用户不存在时返回404。
     * 注意：这是物理删除，删除后数据不可恢复。生产环境建议使用软删除（逻辑删除）。
     * 
     * <p>请求示例: DELETE /api/users/1
     * 
     * @param id 用户ID，从URL路径中提取
     * @return 包含空数据的成功响应（Void类型）
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户", description = "根据用户ID删除用户")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        // 委托Service删除指定用户，无返回数据
        userService.deleteUser(id);
        // 删除成功返回不含数据的成功响应（data为null），符合REST删除操作语义
        return ApiResponse.success(null);
    }

    /*
     * =========================================================================
     * 【OWASP Top 10 映射】Controller 层安全检查表（非可执行代码）
     * =========================================================================
     *
     * A01:2021 失效的访问控制
     *   [ ] 每个接口有明确的权限要求（permitAll/authenticated/hasRole）
     *   [ ] 资源操作前校验归属（防水平越权）
     *   [ ] 管理接口有 ADMIN 角色要求（防垂直越权）
     *   [ ] CORS 显式配置，不用 *
     *
     * A02:2021 加密机制失效
     *   [ ] 强制 HTTPS（HTTP 自动跳转 HTTPS）
     *   [ ] 敏感字段（password）不返回前端
     *   [ ] JWT/Session 用安全 Cookie（HttpOnly + Secure + SameSite）
     *
     * A03:2021 注入
     *   [ ] @RequestBody 反序列化用 DTO，不用 Map/Object
     *   [ ] 排序/搜索参数白名单校验
     *   [ ] 不拼接 SQL/命令/LDAP 查询
     *
     * A04:2021 不安全设计
     *   [ ] 创建/删除接口幂等设计
     *   [ ] 敏感操作有二次确认（如删除用户需确认密码）
     *   [ ] 接口有速率限制（防暴力枚举/CC 攻击）
     *
     * A05:2021 安全配置错误
     *   [ ] 错误响应不泄露堆栈/表名/SQL
     *   [ ] 生产环境关闭 actuator 敏感端点（env/heapdump/beans）
     *   [ ] 不使用默认凭证（admin/admin）
     *
     * A07:2021 身份识别和认证失效
     *   [ ] 登录失败不提示具体原因（"用户名或密码错误"而非"密码错误"）
     *   [ ] 连续失败锁定（5次失败锁15分钟）
     *   [ ] 密码强度策略（长度>=8，含大小写/数字/特殊字符）
     *   [ ] 会话超时（30分钟无操作自动登出）
     *
     * A08:2021 软件和数据完整性故障
     *   [ ] 依赖定期扫描 CVE（OWASP Dependency-Check / Snyk）
     *   [ ] 反序列化用白名单（Jackson default typing 禁用）
     *
     * A09:2021 安全日志和监控失效
     *   [ ] 登录成功/失败记审计日志
     *   [ ] 敏感操作（删除/权限变更）记审计日志
     *   [ ] 异常请求（403/429）触发告警
     * =========================================================================
     */

    /*
     * =========================================================================
     * 【安全补充】API 网关与边缘安全（非可执行代码）
     * =========================================================================
     *
     * 一、API 网关安全职责
     *   网关层（Spring Cloud Gateway / Nginx / Kong / APISIX）：
     *   1) SSL 终止：HTTPS 在网关解密，后端 HTTP，减少后端 CPU 开销
     *   2) 认证前置：网关统一验证 JWT，后端免认证（信任网关头）
     *      注意：后端需校验请求确实来自网关（内网 IP 白名单/mTLS）
     *   3) 限流：网关限流（令牌桶/滑动窗口），保护后端
     *   4) WAF：网关集成 WAF，拦截 SQL 注入/XSS/CC 攻击
     *   5) 熔断：网关熔断，后端故障时返回降级响应
     *
     * 二、WAF 规则
     *   1) OWASP Core Rule Set（CRS）：ModSecurity 规则集
     *      检测：SQL 注入/XSS/路径穿越/远程代码执行/文件包含
     *   2) 自定义规则：
     *      封禁已知攻击 IP（威胁情报）
     *      限制异常 UA（扫描器/爬虫特征）
     *      限制异常请求频率（CC 攻击）
     *   3) 模式：检测模式（只记日志）-> 阻断模式（拦截请求）
     *      建议先检测模式运行 1-2 周，调优误报后切阻断
     *
     * 三、DDoS 防护
     *   1) L3/L4 DDoS（网络层）：云厂商防护（AWS Shield/阿里云 DDoS 防护）
     *   2) L7 DDoS（应用层 CC）：网关限流 + WAF + 验证码挑战
     *   3) 速率限制策略：
     *      全局：10000 req/s（超过触发限流页）
     *      单 IP：100 req/s（超过返回 429）
     *      单用户：10 req/s（超过返回 429）
     *      登录接口：10 req/min/IP（防暴力破解）
     *   4) CDN 静态资源：静态资源走 CDN，源站只处理 API，减少源站压力
     *
     * 四、零信任架构（Zero Trust）
     *   原则：永不信任，始终验证（Never Trust, Always Verify）
     *   1) 不信任内网：服务间调用也需认证（mTLS/Service Token）
     *   2) 最小权限：每个服务只授予所需最小权限
     *   3) 持续验证：每次请求都验证，不依赖网络位置
     *   4) 微隔离：服务间网络隔离，只开放必要端口
     *   5) 实现：服务网格（Istio）自动 mTLS + 策略执行
     * =========================================================================
     */
}