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
 * 【阅读笔记】Spring Data JPA 继承体系与自定义查询（非可执行代码）
 * ============================================================================
 *
 * 一、常用继承接口对比
 * ----------------------------------------------------------------------------
 *   Repository                —— 标记接口，只提供元信息;
 *   CrudRepository            —— 基础 CRUD：save/findById/findAll/delete 等;
 *   PagingAndSortingRepository —— 增加分页和排序;
 *   JpaRepository             —— 再增批量删除、flush、示例匹配等 JPA 特有方法。
 *   日常直接继承 JpaRepository 即可，没必要叠加多级接口。
 *
 * 二、三种查询方式的选择
 * ----------------------------------------------------------------------------
 *   1) 派生方法名（默认）—— 看名字生成 SQL，适合 80% 简单场景;
 *   2) @Query JPQL / HQL —— 语义清晰,可复用,适合多条件/关联查询;
 *   3) @Query(nativeQuery = true) —— 原生 SQL,仅当 JPQL 表达不必要时。
 *   建议按复杂度迭代：先试方法名，不够再写 JPQL，最后才落到原生 SQL。
 *
 * 三、常见注解用法速查
 * ----------------------------------------------------------------------------
 *   @Param("name")           —— 绑定 JPQL 中的 :name 参数;
 *   @Modifying               —— 标识 UPDATE/DELETE，需配合 @Transactional;
 *   @Transactional(readOnly) —— 只读查询让底层做优化;
 *   EntityGraph / JOIN FETCH —— 一次性加载关联，规避 N+1;
 *   Pageable + Page<T>       —— 分页参数透传，返回带总数与元信息。
 *
 * 四、常见误区
 * ----------------------------------------------------------------------------
 *   - existsBy 只判断存在，返回布尔，比先 findBy 再判 null 更轻;
 *   - deleteBy 派生方法会先查后删,大批量删除建议原生 SQL 或批量 API;
 *   - 派生方法名太长时会生成超复杂动态 SQL，此时应直接写 JPQL。
 * ============================================================================
 */

/*
 * ============================================================================
 * 【补充阅读】分页与排序实战（非可执行代码）
 * ============================================================================
 *
 * 一、基本用法
 * ----------------------------------------------------------------------------
 *   Page<User> page = userRepository.findAll(PageRequest.of(0, 20, sort));
 *   返回的 Page<T> 携带：内容列表、总条数、总页数、当前页等信息，
 *   直接作为 data 内嵌结构返回前端即可。
 *
 * 二、排序的正确姿势
 * ----------------------------------------------------------------------------
 *   Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
 *   多字段：Sort.by("status").ascending().and(Sort.by("id").descending());
 *   注意：不要把前端传入的字段名直接拼进 SQL——会产生注入风险;
 *   派生查询走参数绑定天然安全，自定义原生 SQL 时排序字段必须白名单校验。
 *
 * 三、深分页的性能陷阱
 * ----------------------------------------------------------------------------
 *   LIMIT 100000, 20 会让数据库先扫描前 100020 行再丢弃，成本随页码线性上升。
 *   常见优化：
 *     1) 游标分页：WHERE id < lastId ORDER BY id DESC LIMIT 20;
 *     2) 延迟关联：先查主键页，再 JOIN 回表取数据;
 *     3) 对超大数据集考虑搜索引擎（ES）而非关系库分页。
 *
 * 四、count 查询的成本
 * ----------------------------------------------------------------------------
 *   Spring Data 会自动生成 count 查询，百万级表上可能是慢查询;
 *   可用 Slice<T>（只查是否有下一页，不查总数）或自行优化 count SQL。
 * ============================================================================
 */