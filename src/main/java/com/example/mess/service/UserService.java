package com.example.mess.service;

import com.example.mess.entity.User;
import com.example.mess.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 用户服务类
 * 处理所有与用户相关的业务逻辑
 * 
 * @Service 标识这是一个服务层组件，会被Spring自动扫描并注册为Bean
 * 
 * 主要功能:
 * - 用户数据的CRUD操作
 * - 用户数据的缓存管理
 * - 用户数据的验证和转换
 * 
 * 设计原则:
 * - 单一职责原则：只处理用户相关的业务逻辑
 * - 依赖倒置原则：依赖接口而非具体实现
 * - 缓存策略：使用Spring Cache提高性能
 * 
 * 作者: zhangyaolong.5
 * 创建时间: 2026-05-26
 */
@Service
public class UserService {

    /**
     * 用户仓库接口
     * 用于数据访问层的操作
     * 通过构造函数注入，确保不可变性和可测试性
     */
    private final UserRepository userRepository;

    /**
     * 构造函数注入
     * 使用构造函数注入而不是字段注入，这是Spring推荐的最佳实践
     * 
     * @param userRepository 用户仓库实例
     * 
     * 优点:
     * - 确保依赖不可变
     * - 提高可测试性
     * - 明确依赖关系
     */
    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 获取分页的用户列表
     * 
     * @param pageable 分页参数，包含页码、每页大小、排序等信息
     * @return Page<User> 分页的用户列表
     * 
     * 使用场景:
     * - 管理后台用户列表页面
     * - API分页查询
     * - 大数据量展示
     * 
     * 性能考虑:
     * - 使用数据库分页，避免内存溢出
     * - 不缓存分页结果，因为数据可能频繁变化
     * 
     * 示例:
     * Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
     * Page<User> users = userService.getAllUsers(pageable);
     */
    public Page<User> getAllUsers(Pageable pageable) {
        // 直接调用JPA Repository的分页查询方法
        // JPA会自动生成优化的SQL查询
        return userRepository.findAll(pageable);
    }

    /**
     * 获取所有用户列表（不分页）
     * 
     * @return List<User> 所有用户的列表
     * 
     * 使用场景:
     * - 下拉选择框
     * - 导出所有用户
     * - 数据同步
     * 
     * 性能警告:
     * - 如果用户数量很大，可能导致内存问题
     * - 建议在小数据量场景使用，或考虑分批处理
     * 
     * 替代方案:
     * - 对于大数据量，请使用分页版本getAllUsers(Pageable)
     */
    public List<User> getAllUsers() {
        // 直接调用JPA Repository的findAll方法
        // 返回所有用户，不应用任何过滤条件
        return userRepository.findAll();
    }

    /**
     * 根据用户ID获取用户详情
     * 
     * @param id 用户ID
     * @return Optional<User> 用户对象，如果不存在则返回空Optional
     * 
     * 缓存策略:
     * - 使用@Cacheable注解，缓存键为"users::id"
     * - 首次查询后，结果会缓存到Redis
     * - 后续相同ID的查询将直接从缓存获取
     * 
     * 使用场景:
     * - 用户详情页面
     * - 用户验证
     * - 关联查询
     * 
     * 缓存失效:
     * - 当调用updateUser或deleteUser方法时，对应缓存会被清除
     * 
     * 示例:
     * Optional<User> user = userService.getUserById(1L);
     * if (user.isPresent()) {
     *     User u = user.get();
     *     // 处理用户数据
     * }
     */
    @Cacheable(value = "users", key = "#id")
    public Optional<User> getUserById(Long id) {
        // 调用JPA Repository的findById方法
        // 返回Optional类型，避免空指针异常
        return userRepository.findById(id);
    }

    /**
     * 根据用户名获取用户详情
     * 
     * @param username 用户名
     * @return Optional<User> 用户对象，如果不存在则返回空Optional
     * 
     * 使用场景:
     * - 登录验证
     * - 用户名唯一性检查
     * - 用户搜索
     * 
     * 注意事项:
     * - 用户名应该唯一，但此约束在数据库层面强制执行
     * - 此查询不使用缓存，因为用户名查询不频繁
     * 
     * 性能考虑:
     * - 在UserRepository中应该为username字段创建索引
     * - 查询性能取决于数据库索引优化
     */
    public Optional<User> getUserByUsername(String username) {
        // 调用自定义的Repository方法
        // 该方法需要在UserRepository中定义
        return userRepository.findByUsername(username);
    }

    /**
     * 创建新用户
     * 
     * @param user 用户实体对象
     * @return User 创建成功的用户对象，包含生成的ID
     * 
     * 缓存策略:
     * - 使用@CachePut注解，将新创建的用户添加到缓存
     * - 缓存键为"users::user.id"
     * - 这样后续getUserById可以直接从缓存获取
     * 
     * 业务逻辑:
     * - 保存用户到数据库
     * - 自动生成用户ID
     * - 自动设置创建时间
     * 
     * 数据验证:
     * - 在Controller层已经通过@Valid进行了初步验证
     * - 这里可以添加更多业务逻辑验证
     * 
     * 使用场景:
     * - 用户注册
     * - 管理员添加用户
     * - 批量导入用户
     * 
     * 示例:
     * User newUser = new User("john", "john@example.com", "John Doe");
     * User savedUser = userService.createUser(newUser);
     */
    @CachePut(value = "users", key = "#user.id")
    public User createUser(User user) {
        // 调用JPA Repository的save方法
        // save方法会处理插入或更新操作
        // 对于新对象，会执行INSERT操作
        return userRepository.save(user);
    }

    /**
     * 更新用户信息
     * 
     * @param id 要更新的用户ID
     * @param userDetails 包含更新信息的用户对象
     * @return User 更新后的用户对象
     * @throws RuntimeException 当用户不存在时抛出异常
     * 
     * 缓存策略:
     * - 使用@CachePut注解，更新缓存中的用户信息
     * - 缓存键为"users::id"
     * - 确保缓存与数据库数据一致性
     * 
     * 业务逻辑:
     * - 首先检查用户是否存在
     * - 更新用户字段
     * - 保存到数据库
     * - 更新缓存
     * 
     * 数据验证:
     * - 用户必须存在
     * - 更新后的数据必须符合业务规则
     * 
     * 使用场景:
     * - 用户修改个人信息
     * - 管理员更新用户信息
     * - 批量更新操作
     * 
     * 示例:
     * User updateInfo = new User();
     * updateInfo.setName("Updated Name");
     * User updatedUser = userService.updateUser(1L, updateInfo);
     */
    @CachePut(value = "users", key = "#id")
    public User updateUser(Long id, User userDetails) {
        // 首先查询用户是否存在
        // 如果用户不存在，抛出异常
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        
        // 更新用户字段
        // 注意：这里只更新指定的字段，不更新ID和创建时间
        user.setUsername(userDetails.getUsername());
        user.setEmail(userDetails.getEmail());
        user.setName(userDetails.getName());
        
        // 保存更新后的用户
        // 由于用户已存在，save方法会执行UPDATE操作
        return userRepository.save(user);
    }

    /**
     * 删除用户
     * 
     * @param id 要删除的用户ID
     * 
     * 缓存策略:
     * - 使用@CacheEvict注解，删除缓存中的用户信息
     * - 缓存键为"users::id"
     * - 确保缓存与数据库数据一致性
     * 
     * 业务逻辑:
     * - 从数据库删除用户
     * - 清除相关缓存
     * - 级联删除相关数据（如果有的话）
     * 
     * 注意事项:
     * - 删除操作是不可逆的
     * - 应该考虑软删除而不是硬删除
     * - 需要处理外键约束
     * 
     * 使用场景:
     * - 用户注销账户
     * - 管理员删除用户
     * - 数据清理
     * 
     * 安全考虑:
     * - 需要适当的权限检查
     * - 应该记录删除操作日志
     * - 考虑数据恢复机制
     * 
     * 示例:
     * userService.deleteUser(1L);
     */
    @CacheEvict(value = "users", key = "#id")
    public void deleteUser(Long id) {
        // 调用JPA Repository的deleteById方法
        // 如果用户不存在，此方法不会抛出异常
        // 如果需要检查用户是否存在，可以先调用findById
        userRepository.deleteById(id);
    }
}