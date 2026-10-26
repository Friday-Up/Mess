package com.example.mess.repository;

import com.example.mess.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户数据访问接口 - 继承JpaRepository获得基本CRUD操作和分页功能。
 * 
 * <p>Spring Data JPA自动实现此接口，根据方法名生成SQL查询。
 * 不需要编写实现类，框架在运行时动态代理生成。
 * 
 * <p>继承的方法（来自JpaRepository）:
 * <ul>
 *   <li>findAll() - 查询所有用户</li>
 *   <li>findAll(Pageable) - 分页查询所有用户</li>
 *   <li>findById(Long) - 根据ID查询用户，返回Optional</li>
 *   <li>save(User) - 保存或更新用户（有ID则更新，无ID则创建）</li>
 *   <li>deleteById(Long) - 根据ID删除用户</li>
 *   <li>existsById(Long) - 检查用户是否存在</li>
 *   <li>count() - 统计用户总数</li>
 * </ul>
 * 
 * <p>自定义查询方法（Spring Data JPA根据方法名自动生成SQL）:
 * <table border="1">
 *   <tr><th>方法名</th><th>生成SQL</th><th>用途</th></tr>
 *   <tr><td>findByUsername</td><td>SELECT * FROM USER WHERE username = ?</td><td>按用户名查询，返回Optional</td></tr>
 *   <tr><td>findByEmail</td><td>SELECT * FROM USER WHERE email = ?</td><td>按邮箱查询，返回Optional</td></tr>
 *   <tr><td>existsByUsername</td><td>SELECT COUNT(*) FROM USER WHERE username = ?</td><td>检查用户名唯一性，返回boolean</td></tr>
 *   <tr><td>existsByEmail</td><td>SELECT COUNT(*) FROM USER WHERE email = ?</td><td>检查邮箱唯一性，返回boolean</td></tr>
 * </table>
 * 
 * <p>性能建议:
 * <ul>
 *   <li>在username和email字段上创建数据库索引（已通过@Column(unique=true)自动创建）</li>
 *   <li>分页查询使用Pageable避免全表扫描</li>
 *   <li>使用existsByXxx代替findByXxx进行唯一性检查（性能更优）</li>
 * </ul>
 * 
 * @see com.example.mess.entity.User 对应的实体类
 * @see com.example.mess.service.UserService 使用此接口的业务服务
 * @since 1.0
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 根据用户名查询用户。
     * <p>Spring Data JPA自动生成SQL: SELECT * FROM USER WHERE username = ?。
     * 返回Optional，调用方需处理空值情况。
     * 建议在username字段上创建索引以提高查询性能（已通过@Column(unique=true)自动创建）。
     * 
     * @param username 用户名
     * @return 包含用户的Optional，不存在时为Optional.empty()
     */
    Optional<User> findByUsername(String username);

    /**
     * 根据邮箱查询用户。
     * <p>Spring Data JPA自动生成SQL: SELECT * FROM USER WHERE email = ?。
     * 返回Optional，调用方需处理空值情况。
     * 建议在email字段上创建索引以提高查询性能（已通过@Column(unique=true)自动创建）。
     * 
     * @param email 电子邮箱
     * @return 包含用户的Optional，不存在时为Optional.empty()
     */
    Optional<User> findByEmail(String email);

    /**
     * 检查用户名是否已存在。
     * <p>用于注册时唯一性校验，比findByUsername性能更优（只查询COUNT不加载实体）。
     * Spring Data JPA自动生成SQL: SELECT COUNT(*) FROM USER WHERE username = ?。
     * 
     * @param username 用户名
     * @return true表示用户名已存在，false表示可用
     */
    boolean existsByUsername(String username);

    /**
     * 检查邮箱是否已存在。
     * <p>用于注册时唯一性校验，比findByEmail性能更优（只查询COUNT不加载实体）。
     * Spring Data JPA自动生成SQL: SELECT COUNT(*) FROM USER WHERE email = ?。
     * 
     * @param email 电子邮箱
     * @return true表示邮箱已存在，false表示可用
     */
    boolean existsByEmail(String email);
}

/*
 * =========================================================================
 * 【HTTP/REST】分页与排序的 HTTP 语义（非可执行代码）
 * =========================================================================
 *
 * 一、分页参数的 REST 表达规范
 *   1) 位置选择：query 参数（?page=0&size=20）vs 路径（/page/0）
 *      —— REST 派共识用 query，因为"分页是筛选，不是资源"
 *   2) 参数命名约定：page / size（Spring Data 默认，0-based）
 *      别名：offset/limit（偏移式，SQL 友好）
 *      选择后全项目统一，不要混用（前端心智成本）
 *   3) 上限约束：size 必须设 max 限制（如 size=1000 拒绝），
 *      否则客户端一次请求把全表拖走——DoS 与 OOM 双重风险
 *   4) 排序：sort=username,desc&sort=createdAt,asc 多字段逗号串
 *      Spring Data Pageable 自动解析，方向默认 asc

 * 二、分页响应的两种语义模型
 *   1) Page 模型（Spring Data 当前选用）：
 *      内容 + 元数据（totalElements / totalPages / number / size）
 *      代价：每次都执行 count(*) 查询（全表扫描）
 *   2) Slice 模型：内容 + hasNext 布尔（不查 count）
 *      代价：前端不显示总页数，只能"上一页/下一页"
 *   3) 决策树：
 *      前端需要页码跳转 / 总数展示 -> Page（接受 count 成本）
 *      无限滚动 / feed 流 / 列表式 UI -> Slice（首选，性能优）
 *   4) 深分页陷阱：LIMIT 10000000, 20 在 MySQL 上要扫前 1000 万行，
 *      解决：游标分页 WHERE id > ? ORDER BY id LIMIT 20（用索引）

 * 三、本接口在 REST 链路中的角色
 *   1) 签名 findAll(Pageable)：接口层把 HTTP query 参数透传到 DB，
 *      语义对齐——控制器不需要手工解析分页字符串
 *   2) 是弹簧的关键节点：PageableHandlerMethodArgumentResolver
 *      做协议到领域的翻译（query -> PageRequest），Repository 直接消费
 *   3) 排序安全：sort 字段名直接拼入 SQL，若客户端传不存在的字段
 *      会抛 PropertyReferenceException -> 全局处理器映射 400，
 *      拒绝非法字段比静默忽略更安全（避免看似成功的行为不一致）
 *   4) 跨字段排序：sort=email,asc 自动按实体字段名解析为
 *      列名 email——Spring Data 处理映射，无需手写 SQL 片段

 * 四、HTTP 缓存头与 Repository 行为的联动
 *   1) GET /api/users（分页列表）默认动态，缓存价值低
 *   2) GET /api/users/{id} 若是低频变更，可加 Cache-Control: max-age=60
 *      + ETag（基于实体 hash 或 version 字段）
 *   3) ETag 配合 If-None-Match：304 响应无 body，吞吐极大提升
 *   4) 条件更新：客户端 If-Match: <etag> → 服务端比较后决定
 *      是否执行更新，避免"读-改-写"并发覆盖（HTTP 乐观锁机制）
 *   5) 本项目未启用 ETag，最小可行；当 RT 与带宽是瓶颈时再引入

 * 五、HATEOAS（Hypertext As The Engine Of Application State）
 *   1) REST 成熟度模型（Richardson）第三级：响应中嵌超链接，
 *      客户端通过链接发现下一步动作而非硬编码 URL
 *   2) 示例：GET /api/users/1 响应带 _links: { self, orders, avatar }
 *   3) Spring 实现：Spring HATEOAS（EntityModel / CollectionModel）
 *   4) 本项目未启用 HATEOAS——企业内部 API 大多停在第二级
 *      （资源+动词+状态码），第三级成本高、收益场景窄（开放 API）
 * =========================================================================
 */
