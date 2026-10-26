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
 * 【JVM内存管理与GC调优】JPA 代理的类加载与 Metaspace 控制（非可执行代码）
 * =========================================================================
 *
 * 一、Spring Data JPA 启动时 JIT 生成了多少类
 *   1) 启动期：Spring 为本接口创建 SimpleJpaRepository 子类代理 + 
 *      Hibernate 为每个实体生成 Metamodel 类 + 派生查询 JPQL->SQL ->
 *      编译期生成 ByteBuddy/CGLIB 字节码 -> 加载到 Metaspace
 *   2) 类的元数据不占堆，而占 Metaspace（堆外本地内存）：
 *      默认 -XX:MaxMetaspaceSize 不限——长期运行理论上可涨到耗尽本地内存
 *   3) Spring 动态代理引发的"类加载泄漏"：热部署/重新部署留下旧类
 *      元数据，Metaspace 只增不减——grep ClassLoader 数量诊断泄漏
 *
 * 二、Arthas 视角看本接口
 *   1) arthas sc -d UserRepository：查看被生成的代理类名（$Proxy / $$Enhancer），
 *      类加载器指向 ApplicationContextClassLoader
 *   2) 派生查询的 JIT 内联：findByUsername 与同名 native query 调用频次
 *      一致时性能无差——别为"方法名太长会慢"的传闻去重写 SQL
 *   3) 代理类的内存占用估算：本接口 12 个方法 -> 代理类几百 KB，
 *      与堆使用相比可忽略——别为"代理慢"过早手写实现
 *   4) 真实瓶颈永远在 SQL 执行：执行计划、索引、网络 RT——
 *      JVM 类加载通常不是热点（除非启动慢或类加载泄漏）
 * 三、Native Query 与派生 Query 的 JIT 视角差异
 *   1) 派生 query：方法名 -> JPQL -> SQL 在启动期一次性翻译，
 *      运行时直接使用缓存 SQL 字符串——运行时无 JIT 差异
 *   2) 热点永远在 JDBC 结果集映射（ResultSet -> Entity 的反射/字节码路径）：
 *      select 不要的列不仅浪费 SQL 传输，也浪费结果映射 CPU
 *
 * 四、连接池 Hikari 的 JVM 视角
 *   1) Hikari 连接持有真实数据库 Connection（堆外 Socket + 堆内包装对象）：
 *      池大小上限同时决定堆内对象数与线程并发数——
 *      配置不当要么内存不够，要么线程饥饿
 *   2) 连接泄漏诊断：Hikari 自带 leakDetectionThreshold：
 *      借出超阈值未还即告警——堆外资源泄漏无法靠 GC 发现，要靠监控
 *
 =========================================================================
 */
