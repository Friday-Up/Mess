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
 * 【检查清单】Repository 层性能与正确性自检表（非可执行代码）
 * =========================================================================
 *
 * [ ] 查询方法名语义清晰，不需要看实现就能推断 SQL
 * [ ] 存在性判断用 existsBy 而非 findBy（省一次实体映射）
 * [ ] 只读方法加 @Transactional(readOnly = true)
 * [ ] 分页用 Pageable，不用手写 limit/offset
 * [ ] 排序字段白名单校验，不把前端入参直接拼进 SQL
 * [ ] N+1 查询用 @EntityGraph 或 JOIN FETCH 解决
 * [ ] 批量删除用原生 SQL 或批量 API，不用循环逐条 delete
 *
 * 性能排障速查
 *   现象：列表接口慢
 *     -> 开 SQL 日志看查询数，N+1 会打出成倍 SQL
 *     -> EXPLAIN 看执行计划是否走索引
 *     -> 检查是否有 SELECT * 拉了大字段（text/blob）
 *
 *   现象：分页翻到后面越来越慢
 *     -> 深分页问题，LIMIT offset 很大时数据库要扫描并丢弃
 *     -> 改用游标分页（WHERE id < lastId）
 *
 *   现象：count 很慢
 *     -> 百万级表 count(*) 本身就慢
 *     -> 用 Slice 替代 Page（只判断有没有下一页，不算总数）
 * =========================================================================
 */

/*
 * =========================================================================
 * 【补充手册】JPA 关联映射与 N+1 问题速查（非可执行代码）
 * =========================================================================
 *
 * 一、关联关系注解速查
 *   @OneToMany   一对多（如 User -> Orders），默认 LAZY
 *   @ManyToOne   多对一（如 Order -> User），默认 EAGER（注意性能）
 *   @ManyToMany  多对多，需中间表 @JoinTable
 *   @OneToOne    一对一，可共享主键或外键
 *
 *   关键参数：
 *     fetch = FetchType.LAZY    延迟加载（用的时候才查）
 *     fetch = FetchType.EAGER   立即加载（查主实体时 JOIN 关联）
 *     cascade = CascadeType.ALL 级联操作（删父连子一起删）
 *     orphanRemoval = true      孤儿删除（子脱离父集合即删除）
 *
 * 二、N+1 问题
 *   现象：查 N 个用户，每个用户再查一次关联订单 -> 共 N+1 条 SQL
 *   检测：开 SQL 日志（spring.jpa.show-sql=true），看查询数是否异常
 *   解决方案：
 *     1) JOIN FETCH：一次 JOIN 查出主实体+关联（JPQL 中写）
 *     2) @EntityGraph：声明式指定关联图，运行时自动 JOIN
 *     3) 批量加载：hibernate.batch_fetch_size=20（分批 IN 查询）
 *
 * 三、JPA vs MyBatis 选择参考
 *   JPA：标准 ORM，适合 CRUD 为主的简单业务，对象导航方便
 *   MyBatis：SQL 驱动，适合复杂查询/报表/多表 JOIN，SQL 可控
 *   混用：简单 CRUD 用 JPA，复杂报表用 MyBatis，各取所长
 * =========================================================================
 */