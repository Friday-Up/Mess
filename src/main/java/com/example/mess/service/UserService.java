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
 * 处理用户相关的业务逻辑
 * 
 * @Service 注解标识这是一个服务层组件
 * 
 * 作者: zhangyaolong.5
 * 创建时间: 2026-05-26
 */
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    /**
     * 获取所有用户
     * 
     * @param pageable 分页参数
     * @return Page<UserDto> 用户分页数据
     */
    @Cacheable(value = "users", key = "'allUsers-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<UserDto> getAllUsers(Pageable pageable) {
        Page<User> users = userRepository.findAll(pageable);
        return users.map(this::convertToDto);
    }

    /**
     * 根据ID获取用户
     * 
     * @param id 用户ID
     * @return UserDto 用户信息
     * @throws ResourceNotFoundException 用户不存在时抛出异常
     */
    @Cacheable(value = "users", key = "#id")
    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
        return convertToDto(user);
    }

    /**
     * 创建新用户
     * 
     * @param userDto 用户信息
     * @return UserDto 创建的用户信息
     */
    @CacheEvict(value = "users", allEntries = true)
    public UserDto createUser(UserDto userDto) {
        User user = convertToEntity(userDto);
        User savedUser = userRepository.save(user);
        return convertToDto(savedUser);
    }

    /**
     * 更新用户信息
     * 
     * @param id 用户ID
     * @param userDto 更新的用户信息
     * @return UserDto 更新后的用户信息
     * @throws ResourceNotFoundException 用户不存在时抛出异常
     */
    @CacheEvict(value = "users", key = "#id")
    public UserDto updateUser(Long id, UserDto userDto) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
        
        existingUser.setUsername(userDto.getUsername());
        existingUser.setEmail(userDto.getEmail());
        existingUser.setName(userDto.getName());
        
        User updatedUser = userRepository.save(existingUser);
        return convertToDto(updatedUser);
    }

    /**
     * 删除用户
     * 
     * @param id 用户ID
     */
    @CacheEvict(value = "users", key = "#id")
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("用户不存在");
        }
        userRepository.deleteById(id);
    }

    /**
     * 实体转DTO
     * 
     * @param user 用户实体
     * @return UserDto 用户数据传输对象
     */
    private UserDto convertToDto(User user) {
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setUsername(user.getUsername());
        userDto.setEmail(user.getEmail());
        userDto.setName(user.getName());
        userDto.setCreatedAt(user.getCreatedAt());
        return userDto;
    }

    /**
     * DTO转实体
     * 
     * @param userDto 用户数据传输对象
     * @return User 用户实体
     */
    private User convertToEntity(UserDto userDto) {
        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        user.setName(userDto.getName());
        return user;
    }
}