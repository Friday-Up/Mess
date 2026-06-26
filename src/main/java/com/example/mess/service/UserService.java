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
 * 用户服务类 - 处理用户相关的业务逻辑
 * 
 * Controller层和Repository层之间的桥梁，负责业务规则验证和Entity/DTO转换。
 * 使用Spring Cache（缓存名"users"）提高查询性能。
 * 
 * 缓存策略:
 * - 查询: @Cacheable（缓存结果）
 * - 创建: @CacheEvict(allEntries=true)（清除所有缓存）
 * - 更新/删除: @CacheEvict(key)（清除特定缓存）
 * 
 * @see com.example.mess.controller.UserController
 * @see com.example.mess.repository.UserRepository
 */
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    /** 获取所有用户（分页），缓存键: allUsers-{page}-{size} */
    @Cacheable(value = "users", key = "'allUsers-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<UserDto> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::convertToDto);
    }

    /** 根据ID获取用户，缓存键: 用户ID。不存在时抛出ResourceNotFoundException */
    @Cacheable(value = "users", key = "#id")
    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
        return convertToDto(user);
    }

    /** 创建新用户，清除所有用户缓存 */
    @CacheEvict(value = "users", allEntries = true)
    public UserDto createUser(UserDto userDto) {
        User savedUser = userRepository.save(convertToEntity(userDto));
        return convertToDto(savedUser);
    }

    /** 更新用户信息，清除该用户缓存。不存在时抛出ResourceNotFoundException */
    @CacheEvict(value = "users", key = "#id")
    public UserDto updateUser(Long id, UserDto userDto) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
        existingUser.setUsername(userDto.getUsername());
        existingUser.setEmail(userDto.getEmail());
        existingUser.setName(userDto.getName());
        return convertToDto(userRepository.save(existingUser));
    }

    /** 删除用户，清除该用户缓存。不存在时抛出ResourceNotFoundException */
    @CacheEvict(value = "users", key = "#id")
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("用户不存在");
        }
        userRepository.deleteById(id);
    }

    /** Entity → DTO，隐藏实体内部结构，只暴露必要字段 */
    private UserDto convertToDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setName(user.getName());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }

    /** DTO → Entity，不复制id和createdAt（由数据库管理） */
    private User convertToEntity(UserDto dto) {
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setName(dto.getName());
        return user;
    }
}