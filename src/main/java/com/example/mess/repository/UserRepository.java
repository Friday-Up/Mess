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
 * 【性能优化】查询性能优化清单（非可执行代码）
 * =========================================================================
 *
 * 一、索引优化
 *   1) 唯一索引：username/email 加 UNIQUE INDEX，既保唯一又加速等值查询
 *   2) 组合索引：WHERE a=? AND b=? 加 (a,b) 组合索引，遵循最左前缀
 *   3) 覆盖索引：SELECT 的字段都在索引中，免回表
 *   4) 避免索引失效：LIKE '%x%' 前导通配符不走索引；
 *      函数包裹字段 WHERE YEAR(created)=2024 不走索引
 *
 * 二、N+1 问题专项
 *   检测：spring.jpa.show-sql=true + 统计查询数
 *   场景：查 N 个用户，每个用户的关联数据懒加载触发 N 条 SQL
 *   解决方案对比：
 *     JOIN FETCH：一次 JOIN 查出，最直接，但结果集可能笛卡尔积膨胀
 *     @EntityGraph：声明式，代码侵入小，Hibernate 自动生成 LEFT JOIN
 *     batch_fetch_size：分批 IN 查询，N 条降为 N/50 条，适合批量场景
 *
 * 三、深分页专项
 *   问题：LIMIT 100000,20 扫描 100020 行丢弃前 100000 行
 *   方案一：游标分页 WHERE id < lastId ORDER BY id DESC LIMIT 20（最快）
 *   方案二：延迟关联 SELECT * FROM user WHERE id IN
 *           (SELECT id FROM user ORDER BY id LIMIT 100000,20)
 *   方案三：Slice<T> 只查 hasNext，不算 total，省去 COUNT
 *
 * =========================================================================
 * 【监控可观测性】Repository 层关键指标
 * =========================================================================
 *   - 查询耗时：hibernate.query.execution.time（P99 > 100ms 告警）
 *   - 慢查询日志：MySQL slow_query_log + pt-query-digest 分析
 *   - 连接池等待：hikaricp.connections.pending > 0 说明连接不够
 *   - 事务时长：spring.transaction.active > 5s 告警（长事务锁表）
 *   - 行锁等待：MySQL innodb_row_lock_waits 指标
 * =========================================================================
 */

/*
 * =========================================================================
 * 【性能优化补充】数据库连接池与读写分离（非可执行代码）
 * =========================================================================
 *
 * 一、HikariCP 调优详解
 *   HikariCP 是 Spring Boot 默认连接池，性能最优（无锁设计）：
 *   - maximum-pool-size：10（官方建议，CPU 核数 * 2 + 磁盘数）
 *     公式：connections = (core_count * 2) + effective_spindle_count
 *   - minimum-idle：与 maximum-pool-size 相同（固定大小池，避免动态创建）
 *   - idle-timeout：600000ms（空闲连接超时，固定池时无意义）
 *   - max-lifetime：1800000ms（连接最大存活，需 < MySQL wait_timeout=28800s）
 *   - connection-timeout：30000ms（获取连接超时，超时抛 SQLTransientConnectionException）
 *   - validation-timeout：5000ms（连接活性检测超时）
 *
 * 二、读写分离
 *   1) 主从复制：MySQL binlog 复制，主写从读
 *   2) Spring 实现：AbstractRoutingDataSource 动态切换数据源
 *   3) 注解驱动：@ReadOnly -> 从库，@WriteOnly -> 主库
 *   4) 复制延迟：主从延迟 1-100ms，写后立即读需走主库（强制路由）
 *   5) 分库分表：ShardingSphere，单表 >1000 万行时考虑
 *
 * 三、SQL 优化检查清单
 *   [ ] EXPLAIN 查看执行计划，type 至少 range，避免 ALL（全表扫描）
 *   [ ] rows 扫描行数 < 表行数 10%
 *   [ ] Using filesort：排序无索引，需加排序字段索引
 *   [ ] Using temporary：临时表，需优化 GROUP BY/DISTINCT
 *   [ ] 索引选择性：COUNT(DISTINCT col)/COUNT(*) > 0.3 才值得加索引
 *   [ ] 避免 SELECT *：只查需要的字段，减少 IO 和网络传输
 * =========================================================================
 */