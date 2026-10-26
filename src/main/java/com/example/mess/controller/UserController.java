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
     * 【HTTP/REST】RESTful 资源路由规范与 URL 设计（非可执行代码）
     * =========================================================================
     *
     * 一、资源路由七字诀（项目规范速查）
     *   1) 名词复数：/api/users 而非 /api/user 或 /api/getUserList
     *   2) 层级表达归属：/api/users/{id}/orders 表示"某用户的订单"
     *   3) HTTP 动词定操作：GET 读 / POST 建 / PUT 全量改 /
     *      PATCH 部改 / DELETE 删——路径里不应出现动词
     *   4) 筛选用 query：GET /api/users?role=admin&page=0&size=20
     *   5) 版本前缀：/api/v1/users（重大破坏性变更用 v2 并行，见节四）
     *   6) 稳定路径：路径段一旦发布即契约，废弃要走 deprecation 流程
     *   7) 层级不过三：超过三层的嵌套说明该拆独立资源路由

     * 二、本类端点的 REST 合规性体检
     *   1) GET /api/users（分页查询）——合规：复数名词 + query 分页
     *   2) POST /api/users（创建）——合规：动词隐含在 POST
     *   3) GET /api/users/{id} —— 合规：子路径定位单资源
     *   4) GET /api/users/username/{username} —— 有效性尚可，
     *      REST 派推荐改为 /api/users?username=x（query 是筛选语义）；
     *      路径参数表达"唯一标识定位"是可接受变体
     *   5) PUT /api/users/{id} —— 合规：全量替换语义（与 PATCH 区别见下）
     *   6) DELETE /api/users/{id} —— 合规：幂等删除
     *   7) 一个偏离：删除返回 ApiResponse(false?) 的设计选择——
     *      REST 派主张 204 No Content + 空体，
     *      项目实用派返 200 + 业务 message，两者都有支持者

     * 三、PUT 与 PATCH 的本质差异（新人最容易混）
     *   1) PUT：全量替换——客户端发送的 body 即新状态，缺字段=置 null
     *      实现：service.findById -> 把 DTO 完整覆盖 entity -> save
     *   2) PATCH：部分更新——只改 body 里的字段，其他保持
     *      实现：先 load entity，再按 body 里非 null 字段选择性覆盖
     *   3) 本类用的是 PUT + UserDto 全字段，等价于全量替换——合规；
     *      未来需求"只改邮箱"必须新增 PATCH，不能靠 PUT 少传字段
     *   4) 客户端契约声明：PUT 必须 Body 自包含所有必填字段，
     *      服务端做 Bean Validation 校验全字段责无旁贷

     * 四、API 版本化策略四选一
     *   1) URI 版本：/api/v1/users —— 直观、可路由、缓存友好（项目推荐）
     *   2) Header 版本：Accept: application/vnd.myapp.v1+json
     *      —— 干净，但客户端/文档/缓存成本高
     *   3) Query 参数：?version=1 —— 灵活但污染契约
     *   4) 无版本（设计约束式）：用兼容性规则（只加字段不删字段）
     *      —— 适合内部小项目，规模化后难以兑现
     *   5) 本项目现状无版本前缀；规模化演进起点：/api/v1/users

     * 五、响应头来完整化 REST 语义（项目可补强清单）
     *   1) 分页：Content-Range: users 0-19/100 或 Link 头 + next/prev
     *   2) ETag：GET /api/users/{id} 可带 ETag，客户端再请求附
     *      If-None-Match，命中返 304 Not Modified（省带宽）
     *   3) Location：201 创建成功指向新资源 Location: /api/users/42
     *   4) Allow：405 响应必须带 Allow: GET, POST（告诉客户端可用方法）
     *   5) Cache-Control：GET 类接口显式声明 max-age=60 复用 CDN 缓存
     * =========================================================================
     */
}