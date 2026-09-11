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
     * ============================================================================
     * 【设计文档】UserService 架构说明与协作关系（补充文档，非可执行代码）
     * ============================================================================
     *
     * 一、层次定位
     * ----------------------------------------------------------------------------
     * UserService 属于 Spring Boot 经典三层架构中的「业务逻辑层」（Service Layer），
     * 位于 Controller（表现层）与 Repository（数据访问层）之间，承担如下职责：
     *   1) 编排业务流程：将一次请求所需的多个数据操作组合为一个完整的业务用例；
     *   2) 保证事务边界：通过 @Transactional 声明式事务，确保多步写操作的原子性；
     *   3) 领域校验：在数据落库前进行唯一性、合法性等业务规则校验；
     *   4) 对象转换：负责 Entity 与 DTO 之间的双向映射，隔离内外部数据模型；
     *   5) 缓存治理：借助 Spring Cache 抽象降低热点数据的数据库压力。
     *
     * 二、上下游协作关系
     * ----------------------------------------------------------------------------
     *   上游调用方：UserController —— 接收 HTTP 请求后委派给本类处理业务；
     *   下游依赖方：UserRepository —— 由本类注入并调用以完成持久化操作；
     *   横向协作方：ResourceNotFoundException —— 资源缺失时由本类抛出，
     *              最终由 GlobalExceptionHandler 统一转换为 404 响应。
     *
     * 三、关键设计决策与理由
     * ----------------------------------------------------------------------------
     * 决策 1：查询方法标注 @Transactional(readOnly = true)
     *   理由：只读事务可提示底层 JDBC 驱动与数据库进行读优化（如关闭脏检查、
     *        使用只读连接），既提升性能又避免误写。
     *
     * 决策 2：写操作前先做 existsByXxx 唯一性校验，而非依赖数据库唯一约束抛异常
     *   理由：提前校验可返回更友好的业务错误信息，避免把底层
     *        DataIntegrityViolationException 直接暴露给调用方；数据库唯一约束
     *        仍作为最后一道防线保留，二者互为补充。
     *
     * 决策 3：convertToEntity 刻意不复制 id 与 createdAt
     *   理由：id 由数据库自增生成、createdAt 由默认值 CURRENT_TIMESTAMP 填充，
     *        若允许客户端传入将带来「伪造主键」「篡改创建时间」的安全风险。
     *
     * 决策 4：对外一律返回 UserDto，绝不返回 User 实体
     *   理由：实体含有持久化细节与潜在敏感字段（如密码），DTO 收敛对外契约，
     *        使内部模型演进不破坏 API 兼容性。
     *
     * 四、典型使用示例
     * ----------------------------------------------------------------------------
     *   // 在 Controller 中注入并调用
     *   @Autowired
     *   private UserService userService;
     *
     *   // 分页查询
     *   Page<UserDto> page = userService.getAllUsers(PageRequest.of(0, 20));
     *
     *   // 按 ID 查询（不存在则抛 ResourceNotFoundException → 404）
     *   UserDto dto = userService.getUserById(1L);
     *
     *   // 创建（用户名/邮箱重复则抛业务异常 → 400）
     *   UserDto created = userService.createUser(newDto);
     *
     * 五、边界条件与异常语义
     * ----------------------------------------------------------------------------
     *   - 查询不存在的 ID：抛 ResourceNotFoundException，映射为 HTTP 404；
     *   - 创建时用户名或邮箱已存在：抛业务异常，映射为 HTTP 400；
     *   - 传入 null 的 DTO：属于调用方编程错误，不做防御，任其抛 NPE 以尽早暴露；
     *   - 分页参数越界：Spring Data 返回空页而非异常，调用方需据 totalElements 判断。
     *
     * 六、扩展指南
     * ----------------------------------------------------------------------------
     *   - 新增「按邮箱查询」：直接复用 UserRepository#findByEmail，无需改动仓储层；
     *   - 引入更新用户功能：先 findById 校验存在性，再复制可变字段后 save；
     *   - 引入软删除：在 User 增加 deleted 标记字段，查询方法统一追加过滤条件；
     *   - 引入 MapStruct：可用注解处理器自动生成转换代码替换手写 convertToXxx。
     *
     * 七、常见问题（FAQ）
     * ----------------------------------------------------------------------------
     *   Q: 为什么不把校验逻辑放在 Controller？
     *   A: Controller 应保持轻薄，只做协议适配；业务规则集中在 Service 便于复用与测试。
     *
     *   Q: 缓存与数据库如何保持一致？
     *   A: 写操作通过 @CacheEvict 主动失效相关缓存，读操作再按需回填，属最终一致策略。
     * ============================================================================
     */

    /*
     * ============================================================================
     * 【补充文档】UserService 事务与并发要点（非可执行代码）
     * ============================================================================
     *
     * 一、事务边界建议
     * ----------------------------------------------------------------------------
     *   - 写方法（create/update/delete）应加 @Transactional，保证多步 DB 操作原子性;
     *   - 只读方法可加 @Transactional(readOnly = true)，让底层做只读优化;
     *   - 事务应在 Service 层开启，而非 Controller 或 Repository，
     *     以对齐"一个业务用例 = 一个事务"的粒度。
     *
     * 二、并发场景与防护
     * ----------------------------------------------------------------------------
     *   - 唯一约束冲突（如重复用户名/邮箱）：依赖数据库唯一索引兜底，
     *     应用层的 existsBy 预检查只能降低概率，无法根除竞态，
     *     故仍需捕获 DataIntegrityViolationException 转为友好业务异常;
     *   - 丢失更新：并发更新同一记录时可用乐观锁（@Version 字段）或悲观锁;
     *   - 读已提交 vs 可重复读：按业务对一致性的要求选择隔离级别，默认沿用数据库设置。
     *
     * 三、异常与回滚
     * ----------------------------------------------------------------------------
     *   - Spring 默认仅对 RuntimeException 及其子类回滚;
     *   - 若抛出受检异常仍需回滚，需显式 @Transactional(rollbackFor = Exception.class);
     *   - 自定义业务异常（如 ResourceNotFoundException）继承 RuntimeException，
     *     天然触发回滚，符合预期。
     *
     * 四、FAQ
     * ----------------------------------------------------------------------------
     *   Q: 为什么预检查存在后仍可能插入失败？
     *   A: 检查与插入之间存在时间窗口，另一线程可能已插入同值，
     *      最终以数据库唯一索引为准，应用需捕获并转译冲突异常。
     *
     *   Q: 批量操作如何保证性能与一致性？
     *   A: 单事务内批量写入保证一致性；性能上可分批提交并结合
     *      JPA 批处理配置（hibernate.jdbc.batch_size）减少往返。
     * ============================================================================
     */
}