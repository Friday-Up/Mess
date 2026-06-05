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
 * 用户服务类
 * 
 * 处理用户相关的业务逻辑，是Controller层和Repository层之间的桥梁。
 * 遵循Spring推荐的分层架构，负责业务规则验证和数据转换。
 * 
 * 设计原则:
 * - 单一职责：只处理用户相关的业务逻辑
 * - 依赖注入：通过@Autowired注入UserRepository
 * - 缓存策略：使用Spring Cache提高查询性能
 * - DTO转换：在Service层完成Entity和DTO之间的转换
 * 
 * 缓存策略:
 * - 查询操作使用@Cacheable，缓存结果
 * - 创建操作使用@CacheEvict(allEntries=true)，清除所有缓存
 * - 更新操作使用@CacheEvict(key)，清除特定缓存
 * - 删除操作使用@CacheEvict(key)，清除特定缓存
 * 
 * 缓存配置:
 * - 缓存名称: "users"
 * - 缓存键: 方法参数（如用户ID）
 * - 缓存条件: 所有查询操作
 * - 缓存失效: 所有修改操作
 * 
 * 异常处理:
 * - 资源不存在时抛出ResourceNotFoundException
 * - 由GlobalExceptionHandler统一处理
 * 
 * 事务管理:
 * - 默认使用Spring声明式事务
 * - 只读操作不需要事务
 * - 写操作自动开启事务
 * 
 * 扩展建议:
 * - 添加用户名唯一性校验
 * - 添加邮箱唯一性校验
 * - 添加密码加密逻辑
 * - 添加用户角色管理
 * - 添加用户状态管理
 * - 添加分页查询优化
 * 
 * @Service 注解标识这是一个服务层组件，由Spring容器管理
 * 
 * 作者: zhangyaolong.5
 * 创建时间: 2026-05-26
 * 
 * @see com.example.mess.controller.UserController 调用此服务的控制器
 * @see com.example.mess.repository.UserRepository 依赖的数据访问层
 * @see com.example.mess.dto.UserDto 数据传输对象
 * @see com.example.mess.entity.User 实体类
 */
@Service
public class UserService {

    /**
     * 用户数据访问接口
     * 由Spring自动注入，提供用户数据的CRUD操作
     * 
     * 依赖注入方式: @Autowired字段注入
     * 注意: 推荐使用构造器注入，此处使用字段注入仅为简化演示
     */
    @Autowired
    private UserRepository userRepository;

    /**
     * 获取所有用户（分页）
     * 
     * 使用Spring Data的分页功能，返回分页后的用户数据。
     * 结果被缓存以提高后续查询性能。
     * 
     * 缓存策略:
     * - 缓存名称: "users"
     * - 缓存键: "allUsers-{pageNumber}-{pageSize}"
     * - 缓存条件: 所有查询
     * - 缓存失效: 创建、更新、删除用户时
     * 
     * 分页参数:
     * - page: 页码（从0开始）
     * - size: 每页大小
     * - sort: 排序字段和方向
     * 
     * 数据转换:
     * - 从Entity转换为DTO
     * - 隐藏内部字段
     * - 只暴露必要的字段
     * 
     * @param pageable 分页参数，包含页码、大小和排序信息
     * @return Page<UserDto> 分页的用户DTO数据
     */
    @Cacheable(value = "users", key = "'allUsers-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<UserDto> getAllUsers(Pageable pageable) {
        // 从数据库查询分页数据
        Page<User> users = userRepository.findAll(pageable);
        // 将Entity分页数据转换为DTO分页数据
        return users.map(this::convertToDto);
    }

    /**
     * 根据ID获取用户
     * 
     * 根据用户ID查询用户信息，结果被缓存以提高性能。
     * 如果用户不存在，抛出ResourceNotFoundException异常。
     * 
     * 缓存策略:
     * - 缓存名称: "users"
     * - 缓存键: 用户ID
     * - 缓存条件: 所有查询
     * - 缓存失效: 更新、删除此用户时
     * 
     * 异常处理:
     * - 用户不存在时抛出ResourceNotFoundException
     * - 异常由GlobalExceptionHandler处理
     * - 返回HTTP 404状态码
     * 
     * 使用示例:
     * UserDto user = userService.getUserById(1L);
     * // 如果用户存在，返回用户信息
     * // 如果用户不存在，抛出ResourceNotFoundException
     * 
     * @param id 用户ID，必须大于0
     * @return UserDto 用户DTO对象
     * @throws ResourceNotFoundException 用户不存在时抛出此异常
     */
    @Cacheable(value = "users", key = "#id")
    public UserDto getUserById(Long id) {
        // 根据ID查询用户，如果不存在则抛出异常
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
        // 将Entity转换为DTO
        return convertToDto(user);
    }

    /**
     * 创建新用户
     * 
     * 创建新的用户记录，创建后清除所有用户缓存。
     * 
     * 缓存策略:
     * - 使用@CacheEvict清除所有用户缓存
     * - 原因: 新用户可能影响列表查询结果
     * - allEntries=true: 清除缓存中的所有条目
     * 
     * 数据转换:
     * - 将输入的DTO转换为Entity
     * - 保存Entity到数据库
     * - 将保存后的Entity转换为DTO返回
     * 
     * 注意事项:
     * - 不检查用户名和邮箱的唯一性
     * - 如果违反唯一约束，数据库会抛出异常
     * - 建议在创建前检查唯一性
     * 
     * @param userDto 用户信息DTO，包含用户名、邮箱和姓名
     * @return UserDto 创建后的用户DTO，包含自动生成的ID
     */
    @CacheEvict(value = "users", allEntries = true)
    public UserDto createUser(UserDto userDto) {
        // 将DTO转换为Entity
        User user = convertToEntity(userDto);
        // 保存Entity到数据库，获得包含ID的Entity
        User savedUser = userRepository.save(user);
        // 将保存后的Entity转换为DTO返回
        return convertToDto(savedUser);
    }

    /**
     * 更新用户信息
     * 
     * 根据用户ID更新用户信息，更新后清除该用户的缓存。
     * 如果用户不存在，抛出ResourceNotFoundException异常。
     * 
     * 缓存策略:
     * - 使用@CacheEvict清除特定用户的缓存
     * - key="#id": 只清除指定ID的用户缓存
     * - 比allEntries=true更精确，减少缓存失效范围
     * 
     * 更新逻辑:
     * - 先查询用户是否存在
     * - 如果存在，更新用户属性
     * - 如果不存在，抛出ResourceNotFoundException
     * - 保存更新后的用户
     * 
     * 可更新字段:
     * - username: 用户名
     * - email: 电子邮箱
     * - name: 真实姓名
     * 
     * 不可更新字段:
     * - id: 主键，不可修改
     * - createdAt: 创建时间，不可修改
     * 
     * @param id 用户ID，必须大于0
     * @param userDto 更新的用户信息DTO
     * @return UserDto 更新后的用户DTO
     * @throws ResourceNotFoundException 用户不存在时抛出此异常
     */
    @CacheEvict(value = "users", key = "#id")
    public UserDto updateUser(Long id, UserDto userDto) {
        // 根据ID查询用户，如果不存在则抛出异常
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
        
        // 更新用户属性
        existingUser.setUsername(userDto.getUsername());
        existingUser.setEmail(userDto.getEmail());
        existingUser.setName(userDto.getName());
        
        // 保存更新后的用户到数据库
        User updatedUser = userRepository.save(existingUser);
        // 将更新后的Entity转换为DTO返回
        return convertToDto(updatedUser);
    }

    /**
     * 删除用户
     * 
     * 根据用户ID删除用户，删除后清除该用户的缓存。
     * 如果用户不存在，抛出ResourceNotFoundException异常。
     * 
     * 缓存策略:
     * - 使用@CacheEvict清除特定用户的缓存
     * - key="#id": 只清除指定ID的用户缓存
     * - 删除后相关缓存不再需要
     * 
     * 删除逻辑:
     * - 先检查用户是否存在
     * - 如果存在，执行删除操作
     * - 如果不存在，抛出ResourceNotFoundException
     * 
     * 注意事项:
     * - 物理删除：直接从数据库中删除记录
     * - 不可恢复：删除后数据无法恢复
     * - 建议使用软删除（逻辑删除）替代
     * 
     * @param id 用户ID，必须大于0
     * @throws ResourceNotFoundException 用户不存在时抛出此异常
     */
    @CacheEvict(value = "users", key = "#id")
    public void deleteUser(Long id) {
        // 检查用户是否存在，不存在则抛出异常
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("用户不存在");
        }
        // 执行删除操作
        userRepository.deleteById(id);
    }

    /**
     * 实体转DTO
     * 
     * 将User实体对象转换为UserDto数据传输对象。
     * 在Service层进行转换，避免直接暴露实体类。
     * 
     * 转换规则:
     * - id: 直接复制
     * - username: 直接复制
     * - email: 直接复制
     * - name: 直接复制
     * - createdAt: 直接复制
     * 
     * 设计考虑:
     * - 隐藏实体类的内部结构
     * - 只暴露必要的字段
     * - 未来可在转换中添加数据脱敏逻辑
     * - 未来可在转换中添加字段格式化逻辑
     * 
     * @param user 用户实体对象，不能为null
     * @return UserDto 用户数据传输对象
     */
    private UserDto convertToDto(User user) {
        UserDto userDto = new UserDto();
        // 复制用户ID
        userDto.setId(user.getId());
        // 复制用户名
        userDto.setUsername(user.getUsername());
        // 复制邮箱
        userDto.setEmail(user.getEmail());
        // 复制真实姓名
        userDto.setName(user.getName());
        // 复制创建时间
        userDto.setCreatedAt(user.getCreatedAt());
        return userDto;
    }

    /**
     * DTO转实体
     * 
     * 将UserDto数据传输对象转换为User实体对象。
     * 用于创建和更新操作时的数据转换。
     * 
     * 转换规则:
     * - username: 直接复制
     * - email: 直接复制
     * - name: 直接复制
     * 
     * 不转换的字段:
     * - id: 由数据库自动生成，不从DTO复制
     * - createdAt: 由数据库自动设置，不从DTO复制
     * 
     * 设计考虑:
     * - 防止客户端篡改ID和创建时间
     * - 保护服务器生成的字段
     * - 确保数据完整性
     * 
     * @param userDto 用户数据传输对象，不能为null
     * @return User 用户实体对象
     */
    private User convertToEntity(UserDto userDto) {
        User user = new User();
        // 复制用户名
        user.setUsername(userDto.getUsername());
        // 复制邮箱
        user.setEmail(userDto.getEmail());
        // 复制真实姓名
        user.setName(userDto.getName());
        // 注意：不复制id和createdAt，由数据库管理
        return user;
    }
}