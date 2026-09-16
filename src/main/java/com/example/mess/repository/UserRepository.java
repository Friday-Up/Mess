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
 * 【面试问答】关于 Spring Data JPA 的常见面试题（非可执行代码）
 * =========================================================================
 *
 * Q1: JpaRepository、CrudRepository、Repository 有什么区别？
 * A1: Repository 是标记接口；CrudRepository 加了基础 CRUD；
 *     PagingAndSortingRepository 加了分页排序；
 *     JpaRepository 再加批量操作和 flush。日常直接继承 JpaRepository。
 *
 * Q2: 派生方法名的命名规则是什么？
 * A2: find/exists/count + By + 字段名 + 关键词。
 *     findByUsernameAndEmail -> WHERE username=? AND email=?
 *     findByUsernameOrderByIdDesc -> ORDER BY id DESC
 *     名称太长或太复杂时就写 @Query JPQL。
 *
 * Q3: existsBy 和 findBy 哪个判断存在性更好？
 * A3: existsBy。底层用 SELECT COUNT 或 EXISTS，不加载实体，开销更小。
 *
 * Q4: 什么是 N+1 问题？怎么解决？
 * A4: 查 N 条主记录，每条的关联懒加载触发一条额外 SQL，共 N+1 条。
 *     解决：JOIN FETCH 一次查出；@EntityGraph 声明式预加载；
 *     或 hibernate.batch_fetch_size 分批 IN 查询。
 *
 * Q5: 深分页为什么慢？怎么优化？
 * A5: LIMIT 100000,20 要扫描前 100020 行再丢弃，offset 越大越慢。
 *     优化：游标分页 WHERE id < lastId；或先查主键页再 JOIN 回表。
 *
 * Q6: @Modifying 为什么要配 @Transactional？
 * A6: @Modifying 标识 UPDATE/DELETE，属于写操作，必须在事务内执行。
 *     没有 @Transactional 会报 TransactionRequiredException。
 *
 * Q7: JPA 和 MyBatis 怎么选？
 * A7: JPA 适合 CRUD 为主的简单业务，对象导航方便；
 *     MyBatis 适合复杂 SQL/报表/多表 JOIN，SQL 可控性更强。
 *     可混用：简单 CRUD 用 JPA，复杂报表用 MyBatis。
 * =========================================================================
 */