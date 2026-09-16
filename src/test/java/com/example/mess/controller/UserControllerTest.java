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
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * UserController单元测试 - 使用MockMvc + Mockito隔离测试Controller层。
 * 
 * <p>测试策略:
 * <ul>
 *   <li>MockitoExtension: 自动初始化@Mock和@InjectMocks注解</li>
 *   <li>Mock UserService: 隔离Service层依赖，只测试Controller逻辑</li>
 *   <li>MockMvc: 模拟HTTP请求，验证请求映射、参数绑定、响应格式</li>
 *   <li>覆盖所有端点: GET（列表+详情）、POST（创建）、PUT（更新）、DELETE（删除）</li>
 * </ul>
 * 
 * <p>测试覆盖:
 * <ul>
 *   <li>getAllUsers: GET /api/users 验证分页列表返回</li>
 *   <li>getUserById: GET /api/users/1 验证用户详情返回</li>
 *   <li>getUserByIdWhenUserNotExist: GET /api/users/999 验证404处理</li>
 *   <li>createUser: POST /api/users 验证用户创建</li>
 *   <li>updateUser: PUT /api/users/1 验证用户更新</li>
 *   <li>updateUserWhenUserNotExist: PUT /api/users/999 验证更新失败</li>
 *   <li>deleteUser: DELETE /api/users/1 验证用户删除</li>
 * </ul>
 * 
 * <p>Mockito使用说明:
 * <ul>
 *   <li>@Mock: 创建UserService的模拟对象</li>
 *   <li>@InjectMocks: 将模拟对象注入到UserController</li>
 *   <li>when().thenReturn(): 定义模拟行为</li>
 *   <li>verify(): 验证方法调用次数和参数</li>
 *   <li>verifyNoMoreInteractions(): 确保没有意外的额外调用</li>
 * </ul>
 * 
 * @since 1.0
 */
@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private User testUser;
    private UserDto testUserDto;

    @BeforeEach
    void setUp() {
        // 用 standaloneSetup 仅加载被测 Controller，不启动完整 Spring 容器，测试更轻更快
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
        // ObjectMapper 用于把测试对象序列化成 JSON 作为请求体
        objectMapper = new ObjectMapper();

        // 构造测试用实体对象（模拟 Service 返回值）
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setName("Test User");

        // 构造测试用 DTO 对象（模拟客户端请求体，故不含 id）
        testUserDto = new UserDto();
        testUserDto.setUsername("testuser");
        testUserDto.setEmail("test@example.com");
        testUserDto.setName("Test User");
    }

    /** GET /api/users → 返回用户列表 */
    @Test
    void getAllUsers() throws Exception {
        // Arrange: 再构造一个用户，凑成列表以验证多元素返回
        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");
        user2.setName("User Two");

        // 打桩 Service.getAllUsers 返回两个用户
        when(userService.getAllUsers()).thenReturn(Arrays.asList(testUser, user2));

        // Act & Assert: 发起 GET 请求并逐项断言状态码与 JSON 内容
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())                       // HTTP 200
                .andExpect(jsonPath("$", hasSize(2)))             // 顶层数组长度为 2
                .andExpect(jsonPath("$[0].id", is(1)))            // 第一个元素 id
                .andExpect(jsonPath("$[0].username", is("testuser")))
                .andExpect(jsonPath("$[1].id", is(2)));

        // 验证 Service 方法调用符合预期
        verify(userService, times(1)).getAllUsers();
        verifyNoMoreInteractions(userService);
    }

    /** GET /api/users/1 → 返回指定用户 */
    @Test
    void getUserById() throws Exception {
        // Arrange: 打桩 getUserById 返回存在的用户
        when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));

        // Act & Assert: GET 详情，断言 200 及返回字段
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.username", is("testuser")));

        verify(userService, times(1)).getUserById(1L);
        verifyNoMoreInteractions(userService);
    }

    /** GET /api/users/999 → 用户不存在返回404 */
    @Test
    void getUserByIdWhenUserNotExist() throws Exception {
        // Arrange: 打桩返回空 Optional，模拟用户不存在
        when(userService.getUserById(999L)).thenReturn(Optional.empty());

        // Act & Assert: 期望 Controller 将空结果转换为 404
        mockMvc.perform(get("/api/users/999"))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).getUserById(999L);
        verifyNoMoreInteractions(userService);
    }

    /** POST /api/users → 创建用户 */
    @Test
    void createUser() throws Exception {
        // Arrange: 打桩 createUser 返回带 id 的持久化后用户
        when(userService.createUser(any(User.class))).thenReturn(testUser);

        // Act & Assert: 发起 POST，请求体为 DTO 的 JSON，期望 201 Created
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)          // 声明请求体为 JSON
                .content(objectMapper.writeValueAsString(testUserDto)))
                .andExpect(status().isCreated())                  // HTTP 201
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.username", is("testuser")));

        // 用 any(User.class) 匹配任意入参，因 DTO→Entity 转换后对象不同一
        verify(userService, times(1)).createUser(any(User.class));
        verifyNoMoreInteractions(userService);
    }

    /** PUT /api/users/1 → 更新用户 */
    @Test
    void updateUser() throws Exception {
        // Arrange: 先打桩查询命中，再打桩保存返回，模拟"存在则更新"流程
        when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));
        when(userService.createUser(any(User.class))).thenReturn(testUser);

        // Act & Assert: PUT 更新，期望 200
        mockMvc.perform(put("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUserDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));

        // 验证先查后存两步都被调用
        verify(userService, times(1)).getUserById(1L);
        verify(userService, times(1)).createUser(any(User.class));
        verifyNoMoreInteractions(userService);
    }

    /** PUT /api/users/999 → 用户不存在返回404 */
    @Test
    void updateUserWhenUserNotExist() throws Exception {
        // Arrange: 打桩查询返回空，模拟目标用户不存在
        when(userService.getUserById(999L)).thenReturn(Optional.empty());

        // Act & Assert: 期望更新失败返回 404
        mockMvc.perform(put("/api/users/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUserDto)))
                .andExpect(status().isNotFound());

        // 关键断言：用户不存在时绝不应触发保存操作（never()）
        verify(userService, times(1)).getUserById(999L);
        verify(userService, never()).createUser(any(User.class));
        verifyNoMoreInteractions(userService);
    }

    /** DELETE /api/users/1 → 删除用户 */
    @Test
    void deleteUser() throws Exception {
        // Arrange: deleteUser 无返回值，用 doNothing 声明其行为
        doNothing().when(userService).deleteUser(1L);

        // Act & Assert: DELETE 期望返回 204 No Content
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).deleteUser(1L);
        verifyNoMoreInteractions(userService);
    }

    /*
     * =========================================================================
     * 【面试问答】关于 MockMvc 测试的常见面试题（非可执行代码）
     * =========================================================================
     *
     * Q1: standaloneSetup 和 @WebMvcTest 有什么区别？
     * A1: standaloneSetup 手动创建 MockMvc，只装指定 Controller，
     *     不含 Security/全局异常处理器/拦截器，轻快但不够真实；
     *     @WebMvcTest 加载 Web 切片，含 MVC 基础设施和全局异常处理器，
     *     更接近真实但需 @MockBean 替换 Service。
     *
     * Q2: jsonPath 断言失败怎么排查？
     * A2: 加 .andDo(print()) 打印完整响应 JSON，看实际结构和路径。
     *     常见错误：$.data vs $.data[0] vs $.data.content（分页结构）。
     *
     * Q3: standaloneSetup 下异常不被全局处理器拦截怎么办？
     * A3: standaloneSetup 不加载 @ControllerAdvice。
     *     需手动 .setControllerAdvice(GlobalExceptionHandler.class)，
     *     或改用 @WebMvcTest（自动加载）。
     *
     * Q4: @WebMvcTest 注入 Service 失败？
     * A4: @WebMvcTest 只装 Web 层，Service 需用 @MockBean 替换。
     *     检查 @MockBean 的类型与 @Autowired 参数类型一致。
     *
     * Q5: POST 测试返回 403 怎么办？
     * A5: 若 @WebMvcTest 加载了 SecurityFilterChain，POST 需要 CSRF Token。
     *     测试中加 .with(csrf())。本项目 Security 已禁用 CSRF，一般不会触发。
     *
     * Q6: 测试命名有什么约定？
     * A6: methodName_scenario_expectedResult（如 createUser_validInput_returnsCreated）。
     *     好处：测试报告自解释，不看代码就知道测了什么。
     * =========================================================================
     */
}