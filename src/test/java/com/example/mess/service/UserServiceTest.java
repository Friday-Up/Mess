package com.example.mess.service;

import com.example.mess.entity.User;
import com.example.mess.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * UserService单元测试类
 * 使用Mockito框架测试用户服务层的业务逻辑
 * 
 * @ExtendWith(MockitoExtension.class) 启用Mockito扩展
 * @Mock 创建模拟对象，替代真实的依赖
 * @InjectMocks 创建被测试对象，并注入模拟的依赖
 * 
 * 测试策略:
 * - 隔离测试：只测试UserService，不依赖其他层
 * - 行为验证：验证方法调用次数和参数
 * - 状态验证：验证返回值和对象状态
 * - 异常测试：验证异常处理逻辑
 * 
 * 测试范围:
 * - getAllUsers(): 获取所有用户
 * - getUserById(): 根据ID获取用户
 * - getUserByUsername(): 根据用户名获取用户
 * - createUser(): 创建用户
 * - updateUser(): 更新用户
 * - deleteUser(): 删除用户
 * 
 * Mockito说明:
 * - @Mock: 创建UserRepository的模拟实例
 * - @InjectMocks: 创建UserService实例，并注入模拟的UserRepository
 * - when().thenReturn(): 定义模拟对象的行为
 * - verify(): 验证方法调用
 * 
 * 作者: zhangyaolong.5
 * 创建时间: 2026-05-26
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    /**
     * 模拟的UserRepository实例
     * 用于模拟数据访问层的行为
     * 
     * 由@Mock注解创建
     * 可以定义其行为和返回值
     * 验证其方法调用
     */
    @Mock
    private UserRepository userRepository;

    /**
     * 被测试的UserService实例
     * 由@InjectMocks注解创建
     * 自动注入模拟的UserRepository
     * 
     * 注意:
     * - 真实调用UserService的方法
     * - 依赖的UserRepository是模拟的
     */
    @InjectMocks
    private UserService userService;

    /**
     * 测试用的用户对象
     * 在每个测试方法执行前初始化
     * 提供一致的测试数据
     */
    private User testUser;

    /**
     * 测试前置方法
     * 在每个测试方法执行前运行
     * 初始化测试数据和对象状态
     * 
     * 初始化内容:
     * - 创建测试用户对象
     * - 设置用户属性
     * - 准备测试环境
     */
    @BeforeEach
    void setUp() {
        // 创建测试用户对象
        testUser = new User();
        // 设置用户ID
        testUser.setId(1L);
        // 设置用户名
        testUser.setUsername("testuser");
        // 设置邮箱
        testUser.setEmail("test@example.com");
        // 设置真实姓名
        testUser.setName("Test User");
        // 注意：创建时间由User构造函数自动设置
    }

    /**
     * 测试获取所有用户
     * 验证getAllUsers()方法的功能
     * 
     * 测试步骤:
     * 1. 创建第二个用户对象
     * 2. 模拟UserRepository.findAll()返回用户列表
     * 3. 调用UserService.getAllUsers()
     * 4. 验证返回结果
     * 5. 验证UserRepository.findAll()被调用一次
     * 
     * 预期结果:
     * - 返回用户列表包含2个用户
     * - UserRepository.findAll()被调用一次
     * - 返回的用户列表与模拟数据一致
     * 
     * 验证内容:
     * - 返回值大小为2
     * - 包含测试用户
     * - Repository方法调用正确
     */
    @Test
    void getAllUsers() {
        // 创建第二个用户对象，用于测试列表返回
        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");
        user2.setName("User Two");

        // 模拟UserRepository.findAll()的行为
        // 当调用findAll()时，返回包含两个用户的列表
        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser, user2));

        // 调用被测试的方法
        List<User> users = userService.getAllUsers();

        // 验证返回结果
        // 断言返回的用户列表大小为2
        assertEquals(2, users.size());
        // 断言返回的用户列表包含测试用户
        assertTrue(users.contains(testUser));
        // 断言返回的用户列表包含第二个用户
        assertTrue(users.contains(user2));

        // 验证UserRepository.findAll()被调用了一次
        verify(userRepository, times(1)).findAll();
        // 验证没有其他方法被调用
        verifyNoMoreInteractions(userRepository);
    }

    /**
     * 测试根据ID获取用户
     * 验证getUserById()方法的功能
     * 
     * 测试步骤:
     * 1. 模拟UserRepository.findById()返回存在用户
     * 2. 调用UserService.getUserById()
     * 3. 验证返回结果
     * 4. 验证UserRepository.findById()被调用一次
     * 
     * 预期结果:
     * - 返回的Optional包含用户对象
     * - 用户对象的用户名正确
     * - UserRepository.findById()被调用一次
     * 
     * 验证内容:
     * - Optional不为空
     * - Optional包含的用户用户名正确
     * - Repository方法调用正确
     */
    @Test
    void getUserById() {
        // 模拟UserRepository.findById()的行为
        // 当调用findById(1L)时，返回包含测试用户的Optional
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // 调用被测试的方法
        Optional<User> user = userService.getUserById(1L);

        // 验证返回结果
        // 断言Optional不为空
        assertTrue(user.isPresent());
        // 断言Optional包含的用户用户名正确
        assertEquals("testuser", user.get().getUsername());

        // 验证UserRepository.findById()被调用了一次
        verify(userRepository, times(1)).findById(1L);
        // 验证没有其他方法被调用
        verifyNoMoreInteractions(userRepository);
    }

    /**
     * 测试根据ID获取不存在的用户
     * 验证getUserById()方法处理不存在用户的情况
     * 
     * 测试步骤:
     * 1. 模拟UserRepository.findById()返回空Optional
     * 2. 调用UserService.getUserById()
     * 3. 验证返回结果为空
     * 4. 验证UserRepository.findById()被调用一次
     * 
     * 预期结果:
     * - 返回的Optional为空
     * - UserRepository.findById()被调用一次
     * 
     * 验证内容:
     * - Optional为空
     * - Repository方法调用正确
     */
    @Test
    void getUserByIdWhenUserNotExist() {
        // 模拟UserRepository.findById()的行为
        // 当调用findById(999L)时，返回空的Optional
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // 调用被测试的方法
        Optional<User> user = userService.getUserById(999L);

        // 验证返回结果
        // 断言Optional为空
        assertFalse(user.isPresent());

        // 验证UserRepository.findById()被调用了一次
        verify(userRepository, times(1)).findById(999L);
        // 验证没有其他方法被调用
        verifyNoMoreInteractions(userRepository);
    }

    /**
     * 测试创建用户
     * 验证createUser()方法的功能
     * 
     * 测试步骤:
     * 1. 模拟UserRepository.save()返回保存后的用户
     * 2. 调用UserService.createUser()
     * 3. 验证返回结果
     * 4. 验证UserRepository.save()被调用一次
     * 
     * 预期结果:
     * - 返回的用户对象不为null
     * - 返回的用户用户名正确
     * - UserRepository.save()被调用一次
     * 
     * 验证内容:
     * - 返回值不为null
     * - 返回值的用户名正确
     * - Repository方法调用正确
     */
    @Test
    void createUser() {
        // 模拟UserRepository.save()的行为
        // 当调用save()时，返回测试用户对象
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // 调用被测试的方法
        User createdUser = userService.createUser(testUser);

        // 验证返回结果
        // 断言返回的用户对象不为null
        assertNotNull(createdUser);
        // 断言返回的用户用户名正确
        assertEquals("testuser", createdUser.getUsername());

        // 验证UserRepository.save()被调用了一次
        verify(userRepository, times(1)).save(testUser);
        // 验证传入save方法的参数正确
        verify(userRepository).save(testUser);
        // 验证没有其他方法被调用
        verifyNoMoreInteractions(userRepository);
    }

    /**
     * 测试删除用户
     * 验证deleteUser()方法的功能
     * 
     * 测试步骤:
     * 1. 模拟UserRepository.deleteById()不抛出异常
     * 2. 调用UserService.deleteUser()
     * 3. 验证UserRepository.deleteById()被调用一次
     * 
     * 预期结果:
     * - 方法正常执行，不抛出异常
     * - UserRepository.deleteById()被调用一次
     * 
     * 验证内容:
     * - Repository方法调用正确
     * - 方法执行成功
     */
    @Test
    void deleteUser() {
        // 模拟UserRepository.deleteById()的行为
        // 当调用deleteById()时，不执行任何操作（void方法）
        doNothing().when(userRepository).deleteById(1L);

        // 调用被测试的方法
        userService.deleteUser(1L);

        // 验证UserRepository.deleteById()被调用了一次
        verify(userRepository, times(1)).deleteById(1L);
        // 验证传入deleteById方法的参数正确
        verify(userRepository).deleteById(1L);
        // 验证没有其他方法被调用
        verifyNoMoreInteractions(userRepository);
    }

    /**
     * 测试根据用户名获取用户
     * 验证getUserByUsername()方法的功能
     * 
     * 测试步骤:
     * 1. 模拟UserRepository.findByUsername()返回存在用户
     * 2. 调用UserService.getUserByUsername()
     * 3. 验证返回结果
     * 4. 验证UserRepository.findByUsername()被调用一次
     * 
     * 预期结果:
     * - 返回的Optional包含用户对象
     * - 用户对象的用户名正确
     * - UserRepository.findByUsername()被调用一次
     * 
     * 验证内容:
     * - Optional不为空
     * - Optional包含的用户用户名正确
     * - Repository方法调用正确
     */
    @Test
    void getUserByUsername() {
        // 模拟UserRepository.findByUsername()的行为
        // 当调用findByUsername("testuser")时，返回包含测试用户的Optional
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // 调用被测试的方法
        Optional<User> user = userService.getUserByUsername("testuser");

        // 验证返回结果
        // 断言Optional不为空
        assertTrue(user.isPresent());
        // 断言Optional包含的用户用户名正确
        assertEquals("testuser", user.get().getUsername());

        // 验证UserRepository.findByUsername()被调用了一次
        verify(userRepository, times(1)).findByUsername("testuser");
        // 验证没有其他方法被调用
        verifyNoMoreInteractions(userRepository);
    }

    /**
     * 测试根据用户名获取不存在的用户
     * 验证getUserByUsername()方法处理不存在用户的情况
     * 
     * 测试步骤:
     * 1. 模拟UserRepository.findByUsername()返回空Optional
     * 2. 调用UserService.getUserByUsername()
     * 3. 验证返回结果为空
     * 4. 验证UserRepository.findByUsername()被调用一次
     * 
     * 预期结果:
     * - 返回的Optional为空
     * - UserRepository.findByUsername()被调用一次
     * 
     * 验证内容:
     * - Optional为空
     * - Repository方法调用正确
     */
    @Test
    void getUserByUsernameWhenUserNotExist() {
        // 模拟UserRepository.findByUsername()的行为
        // 当调用findByUsername("nonexistent")时，返回空的Optional
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // 调用被测试的方法
        Optional<User> user = userService.getUserByUsername("nonexistent");

        // 验证返回结果
        // 断言Optional为空
        assertFalse(user.isPresent());

        // 验证UserRepository.findByUsername()被调用了一次
        verify(userRepository, times(1)).findByUsername("nonexistent");
        // 验证没有其他方法被调用
        verifyNoMoreInteractions(userRepository);
    }
}