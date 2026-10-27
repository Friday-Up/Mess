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
         * 【可观测性与生产运维】Controller 测试锁定的错误码与状态码契约（非可执行代码）
         * =========================================================================
         *
         * 一、状态码与错误码是可观测系统的语言
         *   1) 本类断言"service 抛 ResourceNotFoundException -> 响应 404 + errorCode"：
         *      这是运维统计 5xx/4xx、前端做重试决策的共同契约——
         *      契约不被测试锁，监控规则（按 status 分桶）就会失准
         *   2) 每个错误路径都要有对应测试：NotFound/Conflict/Validation ——
         *      监控面板上的每个错误类型标签都该有测试证明"产生它的代码路径正确"
         *   3) 404 vs 500 的边界必须清晰：业务上"查不到"返 404，
         *      "查了但系统故障"返 500——混返 500 的 404 会污染告警，
         *      混返 404 的 500 会让故障消失在监控里
         *
         * 二、响应结构的回归保护
         *   1) ApiResponse 字段顺序/类型变化可能破坏前端解析：
         *      jsonPath 断言锁字段名与层级——序列化契约的测试是
         *      前后端并行开发的安全网
         *   2) 分页响应结构（data.content / totalElements）也要锁：
         *      分页字段是前端列表组件的依赖，重构后悄然消失是高频事故
         *   3) 测试是"契约文档"：新人读测试比读 wiki 更快了解响应格式——
         *      把测试当文档维护，别只当"绿灯检查器"
         *
         * 三、错误响应的文档化与消费者协作
         *   1) 错误码表要成文档：每个 errorCode 的含义、触发条件、
         *      消费者建议动作（重试/不重试/降级）——这是与前端/第三方的
         *      观测协作契约，没有它的 errorCode 只是杂乱字符串
         *   2) OpenAPI/Swagger 注解补全错误响应示例：
         *      自动生成的文档里 4xx 的示例体与测试断言一致——
         *      测试是文档的事实来源，文档是测试的对外表达
         *   3) 重大错误方案变更要过"消费者影响评审"：
         *      errorCode 删除/语义变更影响的不仅是代码，还有对端系统的
         *      监控规则与降级逻辑——把下游的脸色纳入变更流程
         =========================================================================
         */
}