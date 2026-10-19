package com.example.mess.service;

import com.example.mess.dto.UserDto;
import com.example.mess.entity.User;
import com.example.mess.exception.ResourceNotFoundException;
import com.example.mess.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * 用户服务类 - 处理用户相关的业务逻辑，是Controller层和Repository层之间的桥梁。
 * 
 * <p>服务职责:
 * <ul>
 *   <li>业务规则验证（唯一性检查、数据完整性）</li>
 *   <li>Entity与DTO之间的转换（convertToDto/convertToEntity）</li>
 *   <li>缓存管理（使用Spring Cache提高查询性能）</li>
 *   <li>异常处理（资源不存在时抛出ResourceNotFoundException）</li>
 *   <li>事务协调（调用Repository方法，由Spring管理事务）</li>
 * </ul>
 * 
 * <p>缓存策略详解:
 * <table border="1">
 *   <tr><th>操作</th><th>缓存注解</th><th>缓存行为</th><th>原因</th></tr>
 *   <tr><td>查询所有</td><td>@Cacheable</td><td>缓存分页结果</td><td>减少数据库查询，提高读取性能</td></tr>
 *   <tr><td>查询单个</td><td>@Cacheable</td><td>缓存用户详情</td><td>高频查询场景，避免重复查库</td></tr>
 *   <tr><td>创建</td><td>@CacheEvict(allEntries=true)</td><td>清除所有缓存</td><td>新用户影响列表，需全量刷新</td></tr>
 *   <tr><td>更新</td><td>@CacheEvict(key)</td><td>清除特定缓存</td><td>只影响单个用户，精准清除</td></tr>
 *   <tr><td>删除</td><td>@CacheEvict(key)</td><td>清除特定缓存</td><td>删除后缓存失效，防止脏读</td></tr>
 * </table>
 * 
 * <p>缓存配置说明:
 * <ul>
 *   <li>缓存名: "users"（在application.yml中配置）</li>
 *   <li>缓存键生成策略: 基于方法参数自动生成SpEL表达式</li>
 *   <li>缓存过期时间: 由CacheManager配置（如Redis TTL）</li>
 * </ul>
 * 
 * <p>DTO转换说明:
 * <ul>
 *   <li>convertToDto: Entity → DTO，隐藏实体内部结构，只暴露必要字段</li>
 *   <li>convertToEntity: DTO → Entity，不复制id和createdAt（由数据库管理）</li>
 *   <li>转换方法为private，确保只在Service内部使用</li>
 * </ul>
 * 
 * @see com.example.mess.controller.UserController 用户控制器
 * @see com.example.mess.repository.UserRepository 用户数据访问接口
 * @see com.example.mess.dto.UserDto 用户数据传输对象
 * @since 1.0
 */
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    /**
     * 获取所有用户（分页）。
     * <p>使用Spring Cache缓存分页结果，缓存键格式: allUsers-{page}-{size}。
     * 缓存策略: 查询时缓存，创建/更新/删除时清除。
     * 
     * <p>分页参数由Spring Data的Pageable自动解析，支持page、size、sort。
     * 
     * @param pageable 分页参数（页码、每页大小、排序）
     * @return 分页的用户DTO列表
     */
    @Cacheable(value = "users", key = "'allUsers-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<UserDto> getAllUsers(Pageable pageable) {
        // 调用Repository的分页查询，findAll(Pageable)由Spring Data JPA自动实现
        // 底层会执行两条SQL：一条查询当前页数据（LIMIT/OFFSET），一条统计总记录数（COUNT）
        // 返回的Page对象封装了当前页内容、总页数、总记录数等分页元信息
        // 使用map(this::convertToDto)对Page中的每个User实体进行转换
        // Page.map方法保留分页元信息不变，只转换内容元素类型 User → UserDto
        // 方法引用this::convertToDto等价于 user -> convertToDto(user)
        return userRepository.findAll(pageable).map(this::convertToDto);
    }

    /**
     * 根据ID获取用户详情。
     * <p>使用Spring Cache缓存用户详情，缓存键为用户ID。
     * 用户不存在时抛出ResourceNotFoundException（由GlobalExceptionHandler处理返回404）。
     * 
     * @param id 用户ID
     * @return 用户DTO，包含用户详细信息
     * @throws ResourceNotFoundException 当用户不存在时
     */
    @Cacheable(value = "users", key = "#id")
    public UserDto getUserById(Long id) {
        // 调用Repository的findById查询用户，返回Optional<User>包装可能不存在的结果
        // orElseThrow：当Optional为空（用户不存在）时，抛出ResourceNotFoundException
        // 使用Lambda表达式延迟创建异常对象，只有真正为空时才实例化异常，避免不必要开销
        // 抛出的异常最终由GlobalExceptionHandler捕获并转换为404 HTTP响应
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
        // 将查询到的JPA实体转换为DTO后返回，避免直接暴露实体内部结构
        return convertToDto(user);
    }

    /**
     * 创建新用户。
     * <p>保存用户到数据库，并清除所有用户缓存（allEntries=true）。
     * 因为新用户会影响分页列表，需要全量刷新缓存。
     * 
     * @param userDto 用户创建数据（不含id和createdAt）
     * @return 创建成功的用户DTO（包含自动生成的id和createdAt）
     */
    @CacheEvict(value = "users", allEntries = true)
    public UserDto createUser(UserDto userDto) {
        // convertToEntity：将传入的DTO转换为JPA实体（不含id和createdAt）
        // userRepository.save：执行INSERT语句持久化实体，save会返回带有数据库生成id的实体
        // 由于id为null，JPA判定为新增操作（若id非null则会执行UPDATE）
        // 保存后数据库自增生成id，createdAt由数据库默认值CURRENT_TIMESTAMP填充
        User savedUser = userRepository.save(convertToEntity(userDto));
        // 将保存后的实体（含自动生成的id和createdAt）转换为DTO返回给调用方
        return convertToDto(savedUser);
    }

    /**
     * 更新用户信息（全量更新）。
     * <p>根据ID更新用户所有可修改字段，并清除该用户的缓存。
     * 用户不存在时抛出ResourceNotFoundException。
     * 
     * @param id 用户ID
     * @param userDto 用户更新数据
     * @return 更新后的用户DTO
     * @throws ResourceNotFoundException 当用户不存在时
     */
    @CacheEvict(value = "users", key = "#id")
    public UserDto updateUser(Long id, UserDto userDto) {
        // 先查询待更新的用户，确保其存在；不存在则抛出异常返回404
        // 注意：这里查询出的existingUser处于JPA持久化上下文（persistence context）管理下
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
        // 逐个更新可修改字段（username、email、name），id和createdAt保持不变
        // 只更新DTO中携带的业务字段，避免覆盖数据库管理的系统字段
        existingUser.setUsername(userDto.getUsername());
        existingUser.setEmail(userDto.getEmail());
        existingUser.setName(userDto.getName());
        // save执行UPDATE语句（因为实体已有id），再将结果转换为DTO返回
        // 由于实体处于持久化上下文中，即使不显式调用save，事务提交时也会自动flush
        return convertToDto(userRepository.save(existingUser));
    }

    /**
     * 删除用户（物理删除）。
     * <p>根据ID从数据库永久删除用户记录，并清除该用户的缓存。
     * 先检查用户是否存在（existsById），不存在时抛出ResourceNotFoundException。
     * 注意：这是物理删除，数据不可恢复。
     * 
     * @param id 用户ID
     * @throws ResourceNotFoundException 当用户不存在时
     */
    @CacheEvict(value = "users", key = "#id")
    public void deleteUser(Long id) {
        // 删除前先用existsById检查用户是否存在（只查COUNT，不加载实体，性能更优）
        // 若直接调用deleteById删除不存在的记录，Spring Data会抛出异常，体验不友好
        if (!userRepository.existsById(id)) {
            // 用户不存在时主动抛出业务异常，由GlobalExceptionHandler转换为404响应
            throw new ResourceNotFoundException("用户不存在");
        }
        // 执行DELETE语句物理删除该用户记录，数据不可恢复
        // 生产环境通常采用软删除（标记deleted字段）以保留数据用于审计和恢复
        userRepository.deleteById(id);
    }

    /**
     * Entity → DTO转换。
     * <p>将JPA实体转换为数据传输对象，隐藏实体内部结构（JPA注解、数据库映射细节）。
     * 只复制对外暴露的必要字段，不包含密码等敏感信息。
     * 
     * @param user JPA实体对象
     * @return 用户DTO对象
     */
    private UserDto convertToDto(User user) {
        // 创建空的DTO对象，逐字段从实体复制到DTO
        UserDto dto = new UserDto();
        // 复制主键id，供前端标识和后续操作使用
        dto.setId(user.getId());
        // 复制用户名（登录标识）
        dto.setUsername(user.getUsername());
        // 复制邮箱地址
        dto.setEmail(user.getEmail());
        // 复制真实姓名（可能为null）
        dto.setName(user.getName());
        // 复制创建时间，供前端展示注册时间
        // 注意：此处不复制密码等敏感字段（本实体虽无密码字段，但转换层是过滤敏感信息的关键位置）
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }

    /**
     * DTO → Entity转换。
     * <p>将数据传输对象转换为JPA实体，用于保存到数据库。
     * 不复制id和createdAt（由数据库自动管理），避免覆盖系统生成的值。
     * 
     * @param dto 用户DTO对象
     * @return JPA实体对象（不含id和createdAt）
     */
    private User convertToEntity(UserDto dto) {
        // 创建空的实体对象，只复制业务字段
        User user = new User();
        // 复制用户名（须保证唯一，否则持久化时违反数据库唯一约束）
        user.setUsername(dto.getUsername());
        // 复制邮箱（须保证唯一）
        user.setEmail(dto.getEmail());
        // 复制真实姓名（可选字段）
        user.setName(dto.getName());
        // 特意不复制id：新增时id应为null，由数据库自增生成
        // 特意不复制createdAt：由数据库默认值CURRENT_TIMESTAMP自动填充
        // 这样可防止客户端伪造id或篡改创建时间
        return user;
    }

    /*
     * =========================================================================
     * 【性能优化】缓存策略与并发控制（非可执行代码）
     * =========================================================================
     *
     * 一、缓存命中率优化
     *   1) 热点数据缓存：用户信息读多写少，适合 @Cacheable
     *   2) 缓存 key 设计：单实体用 #id，列表用 'all' 或 #page+'-'+#size
     *   3) 缓存失效：写操作后 @CacheEvict，避免脏读
     *   4) TTL 设置：用户信息 30 分钟，列表 5 分钟，避免缓存雪崩
     *   5) 缓存预热：启动时加载热点数据，避免首次请求穿透
     *
     * 二、缓存常见问题
     *   1) 缓存穿透：查询不存在的 key，每次都查库
     *      解决：缓存空值（短 TTL）或布隆过滤器
     *   2) 缓存击穿：热点 key 过期瞬间大量请求查库
     *      解决：互斥锁（只允许一个线程查库回填）或逻辑过期
     *   3) 缓存雪崩：大量 key 同时过期
     *      解决：TTL 加随机偏移（30min + random(0,5min)）
     *
     * 三、并发控制
     *   1) 乐观锁：@Version 字段，冲突时抛 OptimisticLockException
     *   2) 悲观锁：SELECT ... FOR UPDATE，适合高并发写
     *   3) 分布式锁：Redisson，多实例场景下保护临界区
     *   4) 限流：Guava RateLimiter 或 Bucket4j，保护下游
     *
     * 四、异步化
     *   1) @Async：非核心逻辑（通知/日志）异步执行，不阻塞主流程
     *   2) 事件驱动：ApplicationEventPublisher + @EventListener
     *   3) 消息队列：RocketMQ/Kafka，削峰填谷，解耦上下游
     *
     * =========================================================================
     * 【监控可观测性】Service 层关键指标
     * =========================================================================
     *   - 缓存命中率：cache.gets{result=hit/miss}，命中率 <80% 需优化
     *   - 方法耗时：自定义 Micrometer Timer，P99 > 500ms 告警
     *   - 并发量：executor.active/queued（线程池指标）
     *   - 锁等待：分布式锁等待时长，>1s 告警
     *   - 事务回滚率：rollback/commit 比值，>5% 需排查
     * =========================================================================
     */

    /*
     * =========================================================================
     * 【性能优化补充】JVM 调优与 GC 策略（非可执行代码）
     * =========================================================================
     *
     * 一、GC 选择
     *   1) G1 GC（JDK 9+ 默认）：大堆（>4GB）表现好，停顿可控
     *      -XX:+UseG1GC -XX:MaxGCPauseMillis=200
     *   2) ZGC（JDK 11+ 实验，JDK 15+ 生产）：超低停顿 <1ms，适合超大堆
     *      -XX:+UseZGC
     *   3) Parallel GC（JDK 8 默认）：吞吐量优先，停顿长
     *      -XX:+UseParallelGC
     *   建议：JDK 17+ 用 G1 或 ZGC，JDK 8 用 Parallel 或 CMS（已废弃）
     *
     * 二、堆内存配置
     *   1) 容器环境：-XX:MaxRAMPercentage=70.0（自动适配容器内存限制）
     *   2) 物理机：-Xms4g -Xmx4g（Xms=Xmx 避免动态扩缩堆）
     *   3) 元空间：-XX:MetaspaceSize=256m -XX:MaxMetaspaceSize=512m
     *   4) 直接内存：-XX:MaxDirectMemorySize=1g（NIO/Netty 场景）
     *
     * 三、GC 日志与排障
     *   1) 开启 GC 日志：-Xlog:gc*:file=gc.log:time,uptime,level,tags
     *   2) 分析工具：GCViewer / GCEasy（在线） / JFR（Java Flight Recorder）
     *   3) 关键指标：GC 频率 >1次/秒 告警；单次 GC 停顿 >500ms 告警
     *   4) Full GC 排查：老年代不足/元空间不足/内存泄漏/System.gc() 误调
     *
     * 四、内存泄漏常见场景
     *   1) 静态集合无限增长：static Map<List> 只加不清
     *   2) ThreadLocal 未 remove：线程池场景下 ThreadLocal 泄漏
     *   3) 缓存无 TTL：ConcurrentHashMap 当缓存，无过期策略
     *   4) 监听器未注销：事件监听器持有大对象引用
     *   排查工具：jmap -histo / MAT（Memory Analyzer Tool）分析堆转储
     * =========================================================================
     */
}