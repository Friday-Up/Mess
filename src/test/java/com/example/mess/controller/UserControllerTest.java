package com.example.mess.controller;

import com.example.mess.dto.UserDto;
import com.example.mess.entity.User;
import com.example.mess.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * UserController单元测试类
 * 使用MockMvc测试用户控制器层的HTTP接口
 * 
 * @ExtendWith(MockitoExtension.class) 启用Mockito扩展
 * @Mock 创建模拟对象，替代真实的依赖
 * @InjectMocks 创建被测试对象，并注入模拟的依赖
 * 
 * 测试策略:
 * - 集成测试：测试Controller层和HTTP层
 * - 隔离测试：使用Mock对象模拟Service层
 * - HTTP测试：验证HTTP请求和响应
 * - JSON序列化：验证JSON数据的序列化和反序列化
 * 
 * 测试范围:
 * - GET /api/users: 获取所有用户
 * - GET /api/users/{id}: 根据ID获取用户
 * - POST /api/users: 创建用户
 * - PUT /api/users/{id}: 更新用户
 * - DELETE /api/users/{id}: 删除用户
 * 
 * MockMvc说明:
 * - MockMvc: Spring提供的MVC测试框架，模拟HTTP请求
 * - standaloneSetup(): 独立设置控制器进行测试
 * - perform(): 执行HTTP请求
 * - andExpect(): 验证响应结果
 * - ObjectMapper: Jackson的JSON序列化工具
 * 
 * 作者: zhangyaolong.5
 * 创建时间: 2026-05-26
 */
@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    /**
     * 模拟的UserService实例
     * 用于模拟服务层的行为
     * 
     * 由@Mock注解创建
     * 可以定义其行为和返回值
     * 验证其方法调用
     */
    @Mock
    private UserService userService;

    /**
     * 被测试的UserController实例
     * 由@InjectMocks注解创建
     * 自动注入模拟的UserService
     * 
     * 注意:
     * - 真实调用UserController的方法
     * - 依赖的UserService是模拟的
     */
    @InjectMocks
    private UserController userController;

    /**
     * MockMvc实例
     * 用于模拟HTTP请求和验证响应
     * 
     * 在setUp()方法中初始化
     * 配置为独立测试UserController
     */
    private MockMvc mockMvc;

    /**
     * ObjectMapper实例
     * 用于JSON序列化和反序列化
     * 
     * 在setUp()方法中初始化
     * 用于将Java对象转换为JSON字符串
     */
    private ObjectMapper objectMapper;

    /**
     * 测试用的用户对象
     * 在每个测试方法执行前初始化
     * 提供一致的测试数据
     */
    private User testUser;

    /**
     * 测试用的UserDto对象
     * 在每个测试方法执行前初始化
     * 用于测试请求数据的序列化
     */
    private UserDto testUserDto;

    /**
     * 测试前置方法
     * 在每个测试方法执行前运行
     * 初始化测试数据和对象状态
     * 
     * 初始化内容:
     * - 创建MockMvc实例
     * - 创建ObjectMapper实例
     * - 创建测试用户对象
     * - 创建测试UserDto对象
     * - 准备测试环境
     */
    @BeforeEach
    void setUp() {
        // 创建MockMvc实例，独立测试UserController
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
        
        // 创建ObjectMapper实例，用于JSON序列化
        objectMapper = new ObjectMapper();
        
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
        
        // 创建测试UserDto对象
        testUserDto = new UserDto();
        // 设置用户名
        testUserDto.setUsername("testuser");
        // 设置邮箱
        testUserDto.setEmail("test@example.com");
        // 设置真实姓名
        testUserDto.setName("Test User");
    }

    /**
     * 测试获取所有用户
     * 验证GET /api/users接口的功能
     * 
     * 测试步骤:
     * 1. 创建第二个用户对象
     * 2. 模拟UserService.getAllUsers()返回用户列表
     * 3. 执行GET请求到/api/users
     * 4. 验证HTTP响应状态码
     * 5. 验证JSON响应内容
     * 6. 验证UserService.getAllUsers()被调用一次
     * 
     * 预期结果:
     * - HTTP状态码为200 OK
     * - 返回JSON数组包含2个用户
     * - 用户数据正确
     * - UserService.getAllUsers()被调用一次
     * 
     * 验证内容:
     * - HTTP状态码
     * - JSON数据结构
     * - 用户数量
     * - 服务层方法调用
     */
    @Test
    void getAllUsers() throws Exception {
        // 创建第二个用户对象，用于测试列表返回
        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");
        user2.setName("User Two");

        // 模拟UserService.getAllUsers()的行为
        // 当调用getAllUsers()时，返回包含两个用户的列表
        when(userService.getAllUsers()).thenReturn(Arrays.asList(testUser, user2));

        // 执行GET请求并验证结果
        mockMvc.perform(get("/api/users"))
                // 验证HTTP状态码为200 OK
                .andExpect(status().isOk())
                // 验证JSON响应是一个数组
                .andExpect(jsonPath("$", hasSize(2)))
                // 验证第一个用户的ID
                .andExpect(jsonPath("$[0].id", is(1)))
                // 验证第一个用户的用户名
                .andExpect(jsonPath("$[0].username", is("testuser")))
                // 验证第一个用户的邮箱
                .andExpect(jsonPath("$[0].email", is("test@example.com")))
                // 验证第一个用户的真实姓名
                .andExpect(jsonPath("$[0].name", is("Test User")))
                // 验证第二个用户的ID
                .andExpect(jsonPath("$[1].id", is(2)))
                // 验证第二个用户的用户名
                .andExpect(jsonPath("$[1].username", is("user2")));

        // 验证UserService.getAllUsers()被调用了一次
        verify(userService, times(1)).getAllUsers();
        // 验证没有其他方法被调用
        verifyNoMoreInteractions(userService);
    }

    /**
     * 测试根据ID获取用户
     * 验证GET /api/users/{id}接口的功能
     * 
     * 测试步骤:
     * 1. 模拟UserService.getUserById()返回存在用户
     * 2. 执行GET请求到/api/users/1
     * 3. 验证HTTP响应状态码
     * 4. 验证JSON响应内容
     * 5. 验证UserService.getUserById()被调用一次
     * 
     * 预期结果:
     * - HTTP状态码为200 OK
     * - 返回JSON对象包含用户数据
     * - 用户数据正确
     * - UserService.getUserById()被调用一次
     * 
     * 验证内容:
     * - HTTP状态码
     * - JSON数据结构
     * - 用户属性
     * - 服务层方法调用
     */
    @Test
    void getUserById() throws Exception {
        // 模拟UserService.getUserById()的行为
        // 当调用getUserById(1L)时，返回包含测试用户的Optional
        when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));

        // 执行GET请求并验证结果
        mockMvc.perform(get("/api/users/1"))
                // 验证HTTP状态码为200 OK
                .andExpect(status().isOk())
                // 验证JSON响应中的用户ID
                .andExpect(jsonPath("$.id", is(1)))
                // 验证JSON响应中的用户名
                .andExpect(jsonPath("$.username", is("testuser")))
                // 验证JSON响应中的邮箱
                .andExpect(jsonPath("$.email", is("test@example.com")))
                // 验证JSON响应中的真实姓名
                .andExpect(jsonPath("$.name", is("Test User")));

        // 验证UserService.getUserById()被调用了一次
        verify(userService, times(1)).getUserById(1L);
        // 验证没有其他方法被调用
        verifyNoMoreInteractions(userService);
    }

    /**
     * 测试根据ID获取不存在的用户
     * 验证GET /api/users/{id}接口处理不存在用户的情况
     * 
     * 测试步骤:
     * 1. 模拟UserService.getUserById()返回空Optional
     * 2. 执行GET请求到/api/users/999
     * 3. 验证HTTP响应状态码
     * 4. 验证UserService.getUserById()被调用一次
     * 
     * 预期结果:
     * - HTTP状态码为404 Not Found
     * - UserService.getUserById()被调用一次
     * 
     * 验证内容:
     * - HTTP状态码
     * - 服务层方法调用
     */
    @Test
    void getUserByIdWhenUserNotExist() throws Exception {
        // 模拟UserService.getUserById()的行为
        // 当调用getUserById(999L)时，返回空的Optional
        when(userService.getUserById(999L)).thenReturn(Optional.empty());

        // 执行GET请求并验证结果
        mockMvc.perform(get("/api/users/999"))
                // 验证HTTP状态码为404 Not Found
                .andExpect(status().isNotFound());

        // 验证UserService.getUserById()被调用了一次
        verify(userService, times(1)).getUserById(999L);
        // 验证没有其他方法被调用
        verifyNoMoreInteractions(userService);
    }

    /**
     * 测试创建用户
     * 验证POST /api/users接口的功能
     * 
     * 测试步骤:
     * 1. 模拟UserService.createUser()返回创建的用户
     * 2. 将UserDto对象序列化为JSON
     * 3. 执行POST请求到/api/users
     * 4. 验证HTTP响应状态码
     * 5. 验证JSON响应内容
     * 6. 验证UserService.createUser()被调用一次
     * 
     * 预期结果:
     * - HTTP状态码为201 Created
     * - 返回JSON对象包含新创建的用户数据
     * - 用户数据正确
     * - UserService.createUser()被调用一次
     * 
     * 验证内容:
     * - HTTP状态码
     * - JSON数据结构
     * - 用户属性
     * - 服务层方法调用
     * - 请求内容类型
     */
    @Test
    void createUser() throws Exception {
        // 模拟UserService.createUser()的行为
        // 当调用createUser()时，返回测试用户对象
        when(userService.createUser(any(User.class))).thenReturn(testUser);

        // 执行POST请求并验证结果
        mockMvc.perform(post("/api/users")
                // 设置请求内容类型为JSON
                .contentType(MediaType.APPLICATION_JSON)
                // 设置请求内容为UserDto序列化的JSON
                .content(objectMapper.writeValueAsString(testUserDto)))
                // 验证HTTP状态码为201 Created
                .andExpect(status().isCreated())
                // 验证JSON响应中的用户ID
                .andExpect(jsonPath("$.id", is(1)))
                // 验证JSON响应中的用户名
                .andExpect(jsonPath("$.username", is("testuser")))
                // 验证JSON响应中的邮箱
                .andExpect(jsonPath("$.email", is("test@example.com")))
                // 验证JSON响应中的真实姓名
                .andExpect(jsonPath("$.name", is("Test User")));

        // 验证UserService.createUser()被调用了一次
        verify(userService, times(1)).createUser(any(User.class));
        // 验证没有其他方法被调用
        verifyNoMoreInteractions(userService);
    }

    /**
     * 测试更新用户
     * 验证PUT /api/users/{id}接口的功能
     * 
     * 测试步骤:
     * 1. 模拟UserService.getUserById()返回存在用户
     * 2. 模拟UserService.createUser()返回更新后的用户
     * 3. 将UserDto对象序列化为JSON
     * 4. 执行PUT请求到/api/users/1
     * 5. 验证HTTP响应状态码
     * 6. 验证JSON响应内容
     * 7. 验证UserService方法调用
     * 
     * 预期结果:
     * - HTTP状态码为200 OK
     * - 返回JSON对象包含更新后的用户数据
     * - 用户数据正确
     * - UserService方法被正确调用
     * 
     * 验证内容:
     * - HTTP状态码
     * - JSON数据结构
     * - 用户属性
     * - 服务层方法调用
     * - 请求内容类型
     */
    @Test
    void updateUser() throws Exception {
        // 模拟UserService.getUserById()的行为
        // 当调用getUserById(1L)时，返回包含测试用户的Optional
        when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));
        
        // 模拟UserService.createUser()的行为
        // 当调用createUser()时，返回测试用户对象
        when(userService.createUser(any(User.class))).thenReturn(testUser);

        // 执行PUT请求并验证结果
        mockMvc.perform(put("/api/users/1")
                // 设置请求内容类型为JSON
                .contentType(MediaType.APPLICATION_JSON)
                // 设置请求内容为UserDto序列化的JSON
                .content(objectMapper.writeValueAsString(testUserDto)))
                // 验证HTTP状态码为200 OK
                .andExpect(status().isOk())
                // 验证JSON响应中的用户ID
                .andExpect(jsonPath("$.id", is(1)))
                // 验证JSON响应中的用户名
                .andExpect(jsonPath("$.username", is("testuser")))
                // 验证JSON响应中的邮箱
                .andExpect(jsonPath("$.email", is("test@example.com")))
                // 验证JSON响应中的真实姓名
                .andExpect(jsonPath("$.name", is("Test User")));

        // 验证UserService.getUserById()被调用了一次
        verify(userService, times(1)).getUserById(1L);
        // 验证UserService.createUser()被调用了一次
        verify(userService, times(1)).createUser(any(User.class));
        // 验证没有其他方法被调用
        verifyNoMoreInteractions(userService);
    }

    /**
     * 测试更新不存在的用户
     * 验证PUT /api/users/{id}接口处理不存在用户的情况
     * 
     * 测试步骤:
     * 1. 模拟UserService.getUserById()返回空Optional
     * 2. 将UserDto对象序列化为JSON
     * 3. 执行PUT请求到/api/users/999
     * 4. 验证HTTP响应状态码
     * 5. 验证UserService.getUserById()被调用一次
     * 
     * 预期结果:
     * - HTTP状态码为404 Not Found
     * - UserService.getUserById()被调用一次
     * 
     * 验证内容:
     * - HTTP状态码
     * - 服务层方法调用
     */
    @Test
    void updateUserWhenUserNotExist() throws Exception {
        // 模拟UserService.getUserById()的行为
        // 当调用getUserById(999L)时，返回空的Optional
        when(userService.getUserById(999L)).thenReturn(Optional.empty());

        // 执行PUT请求并验证结果
        mockMvc.perform(put("/api/users/999")
                // 设置请求内容类型为JSON
                .contentType(MediaType.APPLICATION_JSON)
                // 设置请求内容为UserDto序列化的JSON
                .content(objectMapper.writeValueAsString(testUserDto)))
                // 验证HTTP状态码为404 Not Found
                .andExpect(status().isNotFound());

        // 验证UserService.getUserById()被调用了一次
        verify(userService, times(1)).getUserById(999L);
        // 验证UserService.createUser()没有被调用
        verify(userService, never()).createUser(any(User.class));
        // 验证没有其他方法被调用
        verifyNoMoreInteractions(userService);
    }

    /**
     * 测试删除用户
     * 验证DELETE /api/users/{id}接口的功能
     * 
     * 测试步骤:
     * 1. 模拟UserService.deleteUser()不抛出异常
     * 2. 执行DELETE请求到/api/users/1
     * 3. 验证HTTP响应状态码
     * 4. 验证UserService.deleteUser()被调用一次
     * 
     * 预期结果:
     * - HTTP状态码为204 No Content
     * - UserService.deleteUser()被调用一次
     * 
     * 验证内容:
     * - HTTP状态码
     * - 服务层方法调用
     */
    @Test
    void deleteUser() throws Exception {
        // 模拟UserService.deleteUser()的行为
        // 当调用deleteUser()时，不执行任何操作（void方法）
        doNothing().when(userService).deleteUser(1L);

        // 执行DELETE请求并验证结果
        mockMvc.perform(delete("/api/users/1"))
                // 验证HTTP状态码为204 No Content
                .andExpect(status().isNoContent());

        // 验证UserService.deleteUser()被调用了一次
        verify(userService, times(1)).deleteUser(1L);
        // 验证没有其他方法被调用
        verifyNoMoreInteractions(userService);
    }
}