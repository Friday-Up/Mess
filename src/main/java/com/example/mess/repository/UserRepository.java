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
 * ============================================================================
 * 【设计文档】UserRepository 数据访问层设计说明（补充文档，非可执行代码）
 * ============================================================================
 *
 * 一、Spring Data JPA 的"接口即实现"范式
 * ----------------------------------------------------------------------------
 * 本接口无需编写任何实现类：Spring Data JPA 在启动时为其生成动态代理，
 * 代理会根据方法名（派生查询）或 @Query 注解自动生成并执行 SQL。
 * 这一范式将开发者从大量样板 DAO 代码中解放出来。
 *
 * 二、方法命名派生查询规则
 * ----------------------------------------------------------------------------
 *   findByUsername      → WHERE username = ?
 *   findByEmail         → WHERE email = ?
 *   existsByUsername    → SELECT COUNT/EXISTS ... WHERE username = ?
 *   existsByEmail       → SELECT COUNT/EXISTS ... WHERE email = ?
 * 命名关键字（findBy/existsBy/countBy/deleteBy + And/Or/Between/Like 等）
 * 由框架解析为对应的查询语义，务必保持字段名与实体属性名一致。
 *
 * 三、返回类型语义
 * ----------------------------------------------------------------------------
 *   Optional<User> —— 表达"可能不存在"，强制调用方显式处理空值，避免 NPE;
 *   boolean        —— 存在性判断，仅查 COUNT/EXISTS，不加载实体，性能更优。
 *
 * 四、关键设计决策
 * ----------------------------------------------------------------------------
 * 决策 1：唯一性校验用 existsByXxx 而非 findByXxx
 *   理由：existsBy 只需数据库返回是否存在，无需回传整行数据，开销更小。
 * 决策 2：查询返回 Optional 而非可能为 null 的 User
 *   理由：Optional 在类型层面表达"缺失"语义，配合业务层可优雅转 404。
 * 决策 3：继承 JpaRepository 而非 CrudRepository
 *   理由：JpaRepository 额外提供分页、排序、批量、flush 等能力，更契合本项目。
 *
 * 五、性能与索引建议
 * ----------------------------------------------------------------------------
 *   - username/email 已通过 @Column(unique=true) 建立唯一索引，等值查询高效;
 *   - 列表查询务必分页（Pageable），避免全表扫描导致内存与延迟问题;
 *   - 高频复杂查询可考虑 @Query 手写 JPQL/原生 SQL 并配合执行计划优化。
 *
 * 六、与相关组件的关系
 * ----------------------------------------------------------------------------
 *   - 上游：UserService 注入并调用本接口完成持久化;
 *   - 关联实体：User（<User, Long> 中的类型参数分别是实体与主键类型）;
 *   - 事务：读写事务边界由 UserService 的 @Transactional 统一管理。
 *
 * 七、扩展指南
 * ----------------------------------------------------------------------------
 *   - 复杂动态查询：引入 JpaSpecificationExecutor 使用 Specification 组合条件;
 *   - 只取部分字段：定义投影接口（Projection）或 DTO 构造表达式查询;
 *   - 批量操作：使用 @Modifying + @Query 或 saveAll，注意清理持久化上下文。
 * ============================================================================
 */

/*
 * ============================================================================
 * 【补充文档】UserRepository 查询命名与性能 FAQ（非可执行代码）
 * ============================================================================
 *
 * 一、派生查询命名速查
 * ----------------------------------------------------------------------------
 *   findByUsername(String)        -> WHERE username = ?
 *   existsByEmail(String)         -> SELECT COUNT>0，仅判断存在性，性能优于查实体
 *   countByStatus(int)            -> SELECT COUNT(*) WHERE status = ?
 *   findByUsernameAndEmail(..)    -> AND 组合条件
 *   findByUsernameOrderByIdDesc() -> 带排序
 *
 * 二、返回类型语义
 * ----------------------------------------------------------------------------
 *   Optional<T> ：可能不存在，调用方显式处理空，避免 NPE;
 *   List<T>     ：多结果集合，无匹配返回空列表而非 null;
 *   boolean     ：存在性判断，配合 existsBy 前缀最高效;
 *   long        ：计数，配合 countBy 前缀。
 *
 * 三、性能 FAQ
 * ----------------------------------------------------------------------------
 *   Q: 判断"是否存在"该用 findBy 还是 existsBy？
 *   A: 用 existsBy，底层只做 COUNT/EXISTS，不加载实体，开销更小。
 *
 *   Q: 列表查询很慢怎么办？
 *   A: 为过滤/排序字段加索引；避免 SELECT * 拉大字段；必要时用分页 Pageable。
 *
 *   Q: 如何避免 N+1？
 *   A: 对关联查询使用 @EntityGraph 或 JOIN FETCH 一次性加载所需关联。
 * ============================================================================
 */