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
 * 【DDD 仓储模式】Repository 与 DAO 的区别（非可执行代码）
 * =========================================================================
 *
 * 一、Repository vs DAO
 *   DAO（Data Access Object）：
 *   - 面向数据表：UserDao.selectByEmail() 直接映射 SQL
 *   - 方法名：技术导向（select/insert/update/delete）
 *   - 返回：数据行/DTO，无业务语义
 *   - 一个表一个 DAO
 *   Repository（DDD 仓储）：
 *   - 面向聚合根：UserRepository 管理 User 聚合整体
 *   - 方法名：业务导向（findByUsername/existsByEmail）
 *   - 返回：聚合根实体（含关联对象），有业务语义
 *   - 一个聚合根一个 Repository
 *   - 隐藏持久化细节：调用方不知道底层是 JPA/MyBatis/内存
 *   本项目 UserRepository 已符合 Repository 模式：
 *   - 方法名业务导向（findByUsername/existsByEmail）
 *   - 管理 User 聚合（当前聚合只有 User 本身）
 *
 * 二、Repository 的设计规范
 *   1) 只为聚合根创建 Repository：
 *      User 是聚合根 -> 有 UserRepository
 *      UserAddress 是聚合内对象 -> 无 UserAddressRepository，
 *      通过 UserRepository 一起加载/保存
 *   2) 方法语义化：
 *      findByUsername（业务语义）而非 selectByUsernameAndStatus（技术语义）
 *   3) 返回聚合根或聚合根集合：
 *      List<User> 而非 List<UserDTO>（查询模型走 CQRS 另建查询服务）
 *   4) 保存聚合整体：
 *      save(user) 保存 User 及其关联的 UserAddress（级联）
 *      而非分别 save(user) + saveAddress(address)
 *
 * 三、CQRS 下的读写分离
 *   写模型（Command）：UserRepository（JPA），走聚合根，保证业务规则
 *   读模型（Query）：UserQueryService，不走聚合根，直接查 DTO/视图
 *   读模型实现选择：
 *   1) JPA 投影：interface UserView { String getUsername(); } + findBy(...)
 *   2) 原生 SQL DTO 投影：@Query("SELECT new com.example.UserDto(...) FROM User")
 *   3) MyBatis：复杂查询走 MyBatis，简单 CRUD 走 JPA
 *   4) Elasticsearch：全文搜索场景，数据同步到 ES
 *   5) 物化视图/只读从库：读多写少场景，读走从库
 *   好处：读模型可以随意优化（加字段/换存储），不影响写模型的业务规则
 *
 * 四、微服务拆分后的数据访问
 *   单体：直接注入 UserRepository 访问用户数据
 *   微服务：订单服务不能直接访问用户库（数据库隔离）
 *   替代方案：
 *   1) API 调用：订单服务调用户服务的 /api/users/{id}
 *   2) 数据冗余：订单存储下单时的用户快照（昵称/手机号），不实时查询
 *   3) 事件同步：用户服务发布 UserUpdated 事件，订单服务更新本地缓存
 *   4) CQRS 跨服务：订单服务订阅用户服务的事件，维护本地用户读模型
 * =========================================================================
 */