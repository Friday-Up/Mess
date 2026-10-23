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
 * 【Java 并发】数据访问层并发画像：连接池、事务绑定与游标资源（非可执行代码）
 * =========================================================================
 *
 * 一、HikariCP 的线程与池化模型
 *   1) 连接池不是业务线程池：工作线程（Tomcat）向池 borrow 连接，
 *      使用期间独占一个物理 JDBC 连接，用完 return 归还
 *   2) Hikari 自身只有少量后台线程：
 *      - houseKeeper：定期调的线程（线程池定时调度），负责空闲剔除
 *      - addConnectionExecutor：池空且未达上限时异步建新连接
 *      - closeConnectionExecutor：异常连接的异步关闭
 *   3) 池大小经验值（官方公式）：connections = core_count * 2 + 有效磁盘数
 *   4) 池太小的症状：Tomcat 工作线程阻塞在 HikariPool.getConnection
 *      （connectionTimeout 抛 SQLTransientConnectionException）；
 *      池太大的症状：MySQL 侧大量活跃连接竞争 CPU/锁，RT 全体上升
 *   5) 切勿用线程数反推连接数：200 工作线程配 500 连接是灾难，
 *      连接上限本质是数据库并发处理能力，而非应用线程数
 *
 * 二、事务与连接的 ThreadLocal 绑定
 *   1) 开始事务时，Spring 从 Hikari 借出连接，并通过
 *      TransactionSynchronizationManager.bindResource 把连接绑到
 *      当前线程的 ThreadLocal（key=dataSource，value=ConnectionHolder）
 *   2) 本接口内任一 SQL 执行都隐式 fetch 当前线程绑定的连接，
 *      同一线程同一事务内多次调用共享同一连接
 *   3) ThreadLocal 意味着事务与线程强绑定：
 *      - @Async 的子线程拿不到外层事务（新事务必需新连接）
 *      - CompletableFuture.supplyAsync(() -> repo.save(x)) 在 ForkJoinPool
 *        上执行，连接与事务全部与调用方脱钩（常见 Bug 源头）
 *   4) 事务方法内必须避免：远程调用的线程切换（并 await）、
 *      手动 new Thread 做数据库写操作、CompletableFuture 跨池编排
 *      -> 事务边界内只做同线程的 DB 操作
 *
 * 三、JPA 代理对象的线程安全边界
 *   1) 本接口的代理对象（com.sun.proxy 类）内部主要持有方法映射表
 *      与 EntityManager（每次方法调用各自获取），没有可变业务状态
 *   2) 返回的 Optional 是不可变的，跨线程传递安全（前提：实体本身
 *      在传递期间不被并发修改，见 User 注释块）
 *   3) 注意 Stream 查询（@Query 返回 Stream）：必须 try-with-resources，
 *      未 close 会泄漏底层 ResultSet 与连接，连接池被抽干是常见事故
 *   4) 分页查询的 Page 实现类（PageImpl）持有 List 快照，是"弱不可变"：
 *      内部 List 可变 -> 跨线程共享前考虑 unmodifiableList 包装
 *
 * 四、乐观锁与数据库原子抵探单例
 *   1) existsByUsername 的并发问题：两个线程同时查询"是否已存在"
 *      都返回 false，随后都 save -> 由数据库唯一索引兜底（应用层
 *      不报错，数据库抛唯一约束冲突 -> DataIntegrityViolationException）
 *   2) 正确的"计数类更新"应走原子 SQL 而非"查-改-存"：
 *      UPDATE user SET login_count = login_count + 1 WHERE id = ?
 *      （单条 SQL 在数据库侧原子，无需 Java 层加锁）
 *   3) JPA 之所以在并发更新用 save + version：
 *      - 无 version 字段时，两次 save 是 put 一致，后者覆盖前者（丢更新）
 *      - 加 @Version 后，update 带版本条件，失败抛
 *        ObjectOptimisticLockingFailureException，由调用方决定重试
 *   4) 项目决策点：当前 User 无高并发写，最小可行解是非乐观锁；
 *      若未来引入"并发改名/改邮箱"，version 是成本最低的修正方案
 * =========================================================================
 */
