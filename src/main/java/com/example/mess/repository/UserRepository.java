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
 * 【Spring 内幕】接口如何被实例化：JDK 动态代理与 JPA Repository 工厂（非可执行代码）
 * =========================================================================
 *
 * 一、一个只有方法签名、没有任何实现类的接口，为什么能被 @Autowired 注入？
 *   本接口 extends JpaRepository，启动时 Spring 容器会为它凭空创建 Bean：
 *   1) @EnableJpaRepositories（JpaRepositoriesAutoConfiguration 引入）
 *      -> @Import(JpaRepositoriesRegistrar)
 *   2) JpaRepositoriesRegistrar 扫描包下所有 Repository 接口，
 *      为每个接口注册一个 BeanDefinition：JpaRepositoryFactoryBean
 *   3) 实例化时机：JpaRepositoryFactoryBean 是 FactoryBean -> getBean 时
 *      调用 getObject()，内部委托给 RepositoryFactorySupport
 *   4) RepositoryFactorySupport.getRepository(...)：
 *      a. 创建 JdkDynamicProxyFactory
 *      b. 构造默认实现 SimpleJpaRepository 的实例（带 EntityManager）
 *      c. 生成实现 InvocationHandler 的代理对象，
 *         包装 RepositoryProxyPostProcessor + QueryExecutorMethodInterceptor
 *   5) Proxy.newProxyInstance(classLoader, new Class[]{UserRepository, ...},
 *      handler) -> 生成对象类型形如 com.sun.proxy.$Proxy23
 *   所以 beanName=userRepository 指向的其实是 JDK Proxy，它的每个方法调用
 *   都被拦截 -> QueryExecutorMethodInterceptor 决定走派生查询还是默认实现
 *
 * 二、派生查询方法的解析与转换：findByUsername 是怎么变成 SQL 的
 *   1) PartTree 解析：findByUsername -> findBy 为谓词前缀，Username 为属性名
 *   2) JpaQueryCreator 遍历 PartTree，为 Username 生成 Equal 条件
 *   3) CriteriaBuilder 构造 JPQL 语义：select user from User user
 *      where user.username = :username
 *   4) Hibernate 将 JPQL 转换为 SQL，参数用 PreparedStatement 绑定
 *      （防注入由此保证）
 *   5) 解析结果缓存：AbstractJpaQuery 在第一次调用时 build，之后复用
 *   existsByUsername 走 exists() 投影（select 1），而不是加载实体，
 *   这就是本接口注释中比 findByUsername 性能更优的框架层实现
 *
 * 三、BeanDefinition 的作用：把描述与实例分离
 *   - 代理类的 BeanDefinition 记录 factoryBeanName 与真实接口类型，
 *     实例化时再解析 -> 容器启动时所有 Repository 接口都注册为 Definition
 *     （轻量），真正的代理对象只在被注入或调用时才创建
 *
 * 四、为什么 Repository 接口默认不需要加 @Repository 注解
 *   - @Repository 的主要作用是声明符号（供扫描）与异常转换
 *     （PersistenceExceptionTranslationPostProcessor 转换原生 ORM 异常）
 *   - Spring Data 系列：扫描行为由 @EnableJpaRepositories 自带，
 *     异常转换则通过 EntityManager + PersistenceExceptionTranslator
 *     在代理内部完成 -> 接口不加注解依然能被扫描并具备异常翻译能力
 *
 * 五、分页接口的内幕（SimpleJpaRepository.findAll(Pageable)）
 *   1) 框架默认实现 SimpleJpaRepository 的方法带 @Transactional(readOnly=true)
 *   2) findAll(Pageable) 执行过程：QueryUtils 生成带 OrderHint 的 JPQL，
 *      TypedQuery.setFirstResult(offset).setMaxResults(size)，
 *      底层 Hibernate 翻译为 select * from USER limit ? offset ?
 *   3) Page vs Slice：Page 会额外执行 count 查询，Slice 不查 count；
 *      深分页（offset > 10000）时 count(*) 与 limit offset 都会退化，
 *      高吞吐列表接口建议改用 Slice + 唯一键游标分页
 *   4) 本接口在 UserService 层被 @Cacheable(key=allUsers-页码-页大小) 包裹，
 *      注意 sort 参数变化会生成不同 key，缓存击穿面需评估
 *   5) @EntityGraph 与 @Query(fetch=JOIN) 是关联查询 N+1 的两种框架级解药
 *
 * 六、查询进阶：@Query / 命名查询 / 投影接口
 *   1) @Query("select u from User u where u.email like %:kw%")：
 *      自定义 JPQL，适合派生命名过长或较复杂的条件
 *      注意：从 JPA 实体名（User）而非表名（USER）书写
 *   2) @Query(nativeQuery=true, value="select * from USER where ...")：
 *      原生 SQL，绕开 JPQL 表达力限制，但失去跨方言可移植性
 *   3) 投影接口（Projection）：interface UserNameView { String getUsername(); }
 *      findBy(...) 返回 UserNameView，只 select 需要的列，
 *      适合轻量列表场景，避免加载整个实体
 *   4) Specifications（JpaSpecificationExecutor）：
 *      动态条件拼接（多条件搜索），本接口可 extends
 *      JpaSpecificationExecutor<User> 获得能力
 *   5) 查询缓存：Hibernate 二级缓存 + @org.hibernate.annotations.Cache，
 *      只对主键/findById 生效复杂查询不命中，慎用以免一致性问题
 * =========================================================================
 */
