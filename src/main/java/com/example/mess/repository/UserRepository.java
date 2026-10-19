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
 * 【数据访问安全】SQL 注入防护与权限边界（非可执行代码）
 * =========================================================================
 *
 * 一、JPA 的 SQL 注入天然防护
 *   派生方法（findBy/existsBy）：参数绑定（PreparedStatement），天然安全
 *   @Query JPQL：:param 参数绑定，天然安全
 *   @Query nativeQuery：?1 参数绑定，天然安全
 *   不安全场景：
 *     1) 原生 SQL 拼接：@Query("SELECT * FROM users WHERE name = '" + name + "'")
 *        绝对禁止，用 :name 参数绑定替代
 *     2) 排序字段拼接：PageRequest.of(0, 10, Sort.by(userInput))
 *        userInput 可能是 "id; DROP TABLE users--"
 *        防护：排序字段白名单校验
 *     3) LIKE 拼接：username LIKE '%' + input + '%'
 *        防护：LIKE CONCAT('%', :input, '%')，特殊字符转义（% _ \）
 *
 * 二、权限边界（行级安全）
 *   1) 数据归属校验：查/改/删前验证资源属于当前用户
 *      反模式：DELETE /api/users/123，不校验 123 是否属于当前用户（越权删除）
 *      正确：先 findById(123)，校验 user.getId() == currentUser.getId()
 *   2) IDOR（不安全的直接对象引用）：
 *      攻击者遍历 ID（/api/users/1, /api/users/2...）获取他人数据
 *      防护：每个查询加 currentUser 过滤条件
 *   3) 批量操作越权：DELETE /api/users/batch，ids 列表含他人 ID
 *      防护：批量操作前校验所有 ID 的归属
 *
 * 三、敏感查询的额外防护
 *   1) 查询日志：敏感表查询记审计日志（谁查了什么）
 *   2) 查询限制：单次查询结果集上限（LIMIT 1000），防全表导出
 *   3) 慢查询监控：>1s 的查询告警，可能是攻击者在拖库
 *   4) 数据库账号最小权限：应用账号无 DDL 权限，无跨库权限
 * =========================================================================
 */