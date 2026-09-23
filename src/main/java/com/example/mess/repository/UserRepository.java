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
 * 【ADR-005】Spring Data JPA 派生查询策略
 * =========================================================================
 * 上下文：Repository 层需要定义数据访问方法，可选择派生方法名、@Query JPQL、
 *         或原生 SQL 三种方式。
 * 决策：简单查询（单表、条件少）用派生方法名（findBy/existsBy/countBy），
 *       复杂查询用 @Query JPQL，原生 SQL 仅在 JPQL 无法表达时使用。
 * 替代方案：
 *   A) 全部用 @Query —— 语义清晰但简单查询也写 JPQL 增加维护量。
 *   B) 全部用原生 SQL —— 绕过 JPA 抽象，失去跨数据库能力。
 *   C) 用 Specification 动态查询 —— 适合条件组合查询，简单场景过重。
 * 后果：80% 的查询用方法名自动生成，减少手写 SQL；复杂查询集中写在 @Query 中，
 *       语义清晰可维护。派生方法名过长时自动切换到 @Query。
 *
 * =========================================================================
 * 【代码审查要点】Repository
 * =========================================================================
 * [ ] 查询方法名语义清晰，能推断出 SQL 语义
 * [ ] 存在性判断用 existsBy 而非 findBy（省一次实体映射）
 * [ ] 只读方法加 @Transactional(readOnly = true)
 * [ ] 分页用 Pageable，不用手写 limit/offset
 * [ ] 排序字段白名单校验，不把前端入参直接拼进 SQL
 * [ ] N+1 查询用 @EntityGraph 或 JOIN FETCH 解决
 * [ ] 批量删除用原生 SQL 或批量 API，不用循环逐条 delete
 * [ ] @Modifying 配合 @Transactional 使用
 * [ ] 派生方法名过长（>5个条件）时改用 @Query JPQL
 * =========================================================================
 */

/*
 * =========================================================================
 * 【ADR-005-S】分页与N+1问题策略（补充）
 * =========================================================================
 * 上下文：列表查询需要分页，关联查询容易产生 N+1 问题。
 * 决策：分页用 Spring Data 的 Pageable；N+1 用 @EntityGraph 或 JOIN FETCH；
 *       深分页用游标分页（WHERE id < lastId）替代 OFFSET。
 * 替代方案：
 *   A) 手写 limit/offset —— 绕过 Spring Data 抽象，失去分页元信息。
 *   B) 不分页，全量返回 —— 数据量大时内存溢出，前端卡顿。
 *   C) 用 Slice 替代 Page —— 不查总数，适合"加载更多"场景。
 * 后果：Pageable 提供统一的分页抽象；深分页优化需额外处理；
 *       @EntityGraph 声明式预加载，比 JOIN FETCH 更易维护。
 *
 * N+1 检测与解决：
 *   检测：开 SQL 日志（spring.jpa.show-sql=true），看查询数是否异常
 *   解决方案一：JOIN FETCH —— JPQL 中写，一次 JOIN 查出主实体+关联
 *   解决方案二：@EntityGraph —— 声明式指定关联图，运行时自动 JOIN
 *   解决方案三：hibernate.batch_fetch_size —— 分批 IN 查询，减少 SQL 数
 *
 * 深分页优化：
 *   问题：LIMIT 100000,20 要扫描前 100020 行再丢弃，offset 越大越慢
 *   方案一：游标分页 WHERE id < lastId ORDER BY id DESC LIMIT 20
 *   方案二：延迟关联，先查主键页再 JOIN 回表取数据
 *   方案三：对超大数据集考虑搜索引擎（ES）而非关系库分页
 * =========================================================================
 */