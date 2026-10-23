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
 * 【设计模式】仓储模式与 JDK 动态代理：从 DAO 到领域仓储（非可执行代码）
 * =========================================================================
 *
 * 一、仓储模式（Repository / DDD 企业模式）
 *   1) 定位：模仿集合（Set/Map）语义管理领域对象（聚合根），
 *      屏蔽持久化细节（库/ORM/文件），领域层只面向"集合"
 *   2) 与 DAO 的关键差异：
 *      - 面向对象：DAO 面向数据表（UserDao 的方法映射 SQL），
 *        Repository 面向聚合根（返回 User 聚合整体而非数据行）
 *      - 方法语义：DAO 技术语义（selectByEmail），
 *        Repository 业务语义（findByUsername / existsByEmail）
 *      - 边界：一个聚合根一个 Repository；子实体不单独建
 *      （UserAddress 随 User 仓储一起加载保存）
 *   3) 本接口天然符合：方法名领域语义、管理 User 聚合、
 *      隐藏 JPA 实现细节（调用方不知道底层是 Hibernate 还是别的）

 * 二、工厂模式（Factory Method）：JpaRepositoryFactoryBean
 *   1) 本接口在容器中的 BeanDefinition 不是接口本身，
 *      而是 JpaRepositoryFactoryBean（FactoryBean 接口的实现）
 *   2) FactoryBean.getObject() 即工厂方法：运行时决定产出什么——
 *      这里产出的是"实现了本接口的 JDK 动态代理对象"
 *   3) 动态代理（Proxy 的运行时形态）
 *      a. 构造默认实现 SimpleJpaRepository 实例（带 EntityManager）
 *      b. QueryExecutorMethodInterceptor 作为 InvocationHandler
 *      c. Proxy.newProxyInstance 生成 $ProxyNN，implements 本接口
 *   4) 一次方法调用的旅程序：
 *      proxy.findByUsername("x") -> handler.invoke ->
 *      判断：派生查询 or 默认方法？-> 找到对应执行 -> 返回
 *      - existsByUsername：存在检查走 exists 投影
 *   5) 设计启示：接口 + 默认行为 + 代理拦截 的组合
 *      让"声明即实现"成为可能，Spring Data 的核心竞争力即此
 *
 * 三、规范模式（Specification）与查询对象
 *   1) Specification<T> 是标准模式：用对象封装查询条件，
 *      而不是拼字符串或写死 JPQL
 *   2) 本项目如有多条件搜索需求：让本接口 extends
 *      JpaSpecificationExecutor<User> 获得 where(and/or) 组合能力
 *   3) 模式边界：Specification 适合"运行时动态条件"，
 *      固定查询仍用派生方法命名（更简洁、可读）
 * =========================================================================
 */
