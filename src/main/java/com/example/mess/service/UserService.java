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
     * 【技术债务】TD-006 Service 层
     * =========================================================================
     *
     * TD-006-1: 写方法缺少 @Transactional
     *   现状：create/delete 方法未显式标注 @Transactional
     *   影响：多步数据库操作无原子性保障（当前单步操作影响不大）
 *   优先级：P1
     *   修复方案：写方法加 @Transactional，只读方法加 @Transactional(readOnly=true)
     *   预估工时：0.5d
     *
     * TD-006-2: 唯一性校验存在竞态窗口
     *   现状：existsByUsername 检查与 insert 之间存在时间窗口
     *   影响：并发创建相同用户名可能绕过检查
     *   优先级：P2
     *   修复方案：依赖数据库唯一索引兜底 + catch DataIntegrityViolationException
     *   预估工时：0.5d
     *
     * TD-006-3: 缺少更新方法
     *   现状：只有 create/delete，无 updateUser
     *   影响：无法修改用户信息
     *   优先级：P1
     *   修复方案：加 updateUser 方法 + PUT /api/users/{id}
     *   预估工时：1d
     *
     * TD-006-4: 缓存注解可能自调用失效
     *   现状：@Cacheable/@CacheEvict 存在，但同类内自调用不走代理
     *   影响：缓存静默失效，每次查库
     *   优先级：P2
     *   修复方案：确保缓存方法从外部调用，或注入自身代理
     *   预估工时：0.5d
     *
     * TD-006-5: 缺少批量操作
     *   现状：无批量创建/删除
     *   影响：批量导入场景需逐条调用
     *   优先级：P3
     *   修复方案：加 createUsers(List)/deleteUsers(Lists) + 批量 Controller
     *   预估工时：1d
     *
     * =========================================================================
     * 【重构路线图】Service 层演进方向
     * =========================================================================
     * Phase 1（当前）：基本 CRUD + 缓存 + 无事务注解
     * Phase 2：加 @Transactional + 唯一约束兜底 + updateUser
     * Phase 3：批量操作 + 事件驱动（领域事件）+ 缓存策略优化
     * =========================================================================
     */

    /*
     * =========================================================================
     * 【技术债务】TD-006-S Service 层补充
     * =========================================================================
     *
     * TD-006-6: 缺少事件发布
     *   现状：创建/删除用户后无事件通知
     *   影响：其他模块（如通知、审计）无法感知用户变更
     *   优先级：P3
     *   修复方案：用 ApplicationEventPublisher 发布 UserCreatedEvent
     *   预估工时：1d
     *
     * TD-006-7: 缺少审计日志
     *   现状：谁在什么时候创建/修改/删除了用户无记录
     *   影响：无法追溯操作历史，安全审计缺失
     *   优先级：P2
     *   修复方案：用 AOP 或事件监听记录操作日志（操作人/操作/时间/目标）
     *   预估工时：1d
     *
     * TD-006-8: 缺少导入导出
     *   现状：无批量导入/导出用户功能
     *   影响：运营需手动逐条操作
     *   优先级：P3
     *   修复方案：加 import/export 接口，支持 CSV/Excel
     *   预估工时：2d
     *
     * TD-006-9: 缺少搜索/排序
     *   现状：findAll 返回全部数据，无搜索/排序
     *   影响：管理后台无法按条件筛选
     *   优先级：P2
     *   修复方案：加 Specification 动态查询 或 QueryDSL
     *   预估工时：1.5d
     * =========================================================================
     */
}