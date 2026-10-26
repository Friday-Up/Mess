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
 * 【数据库与SQL优化】JPA 派生查询的 SQL 生成与索引设计（非可执行代码）
 * =========================================================================
 *
 * 一、派生查询方法的 SQL 翻译过程
 *   1) findByUsername(String) -> PartTree 解析 ->
 *      JPQL "select u from User u where u.username = ?1" ->
 *      Hibernate 翻译为 SQL "select * from USER where username = ?"
 *   2) existsByUsername -> Hibernate 优化为 exists 子查询
 *      "select 1 from USER where username = ? limit 1"
 *      只判断命中，不加载实体，比 findByUsername 快
 *   3) 返回类型对 SQL 的影响：
 *      Optional<User>：select 全字段
 *      boolean：select 1 / count 优化路径
 *      Stream：游标（需 close 释放 ResultSet 与连接，见并发块）

 * 二、索引设计的因果链（2019+ 复习）
 *   1) 字段上的唯一约束自动建唯一索引（@Column(unique=true)）
 *   2) 覆盖索引（Covering Index）：select 的列全在索引里免回表
 *      示例：select username from USER where username=? -> 唯一索引覆盖，免主键回查
 *   3) 最左前缀原则：复合索引 (a, b, c) 只对 a、(a,b)、(a,b,c) 有效
 *      where b=? 不走索引——设计复合索引必须按查询模式排序
 *   4) 函数失效：where lower(username)=? 不走索引，
 *      改用函数索引（MySQL 8 支持）或存储时就 lowercase
 *   5) 隐式类型转换：where varchar_col = 123（数字）-> MySQL 改转字符串列，
 *      索引失效，性能断崖——参数绑定必须类型对齐（JPA 自动绑定天然避免）

 * 三、N+1 查询的识别与根治
 *   1) 症状：findAll() 返回 N 条，循环里访问关联属性触发 N 次额外 SQL
 *   2) 日志辨别：spring.jpa.show-sql=true 看 select 语句数量；
 *      或开 Hibernate 统计 spring.jpa.properties.hibernate.generate_statistics=true
 *   3) 根治路径：
 *      a) 实体图：@EntityGraph(attributePaths = "orders") 单查询 JOIN
 *      b) 批量抓取：@BatchSize(size=50) 把 N 次 select 合并为 N/50 次 IN 查询
 *      c) DTO 投影：直接 select 需要的字段（跨实体不建关联映射）
 *      d) 本项目当前 User 无关联，天然无 N+1——加关联时的预防清单
 *   4) 判定原则：关联仅在真的需要懒加载时用 @ManyToOne FetchType.LAZY
 *      （默认 EAGER 是 N+1 的最大隐患源），读侧模型优先投影
 =========================================================================
 */
