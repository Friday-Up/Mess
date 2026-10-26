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
     * 【HTTP/REST】RESTful 端点契约测试矩阵（非可执行代码）
     * =========================================================================
     *
     * 一、本类用例对应的 REST 契约场景
     *   1) GET /api/users（分页查询）——安全方法 + 分页参数透传验证
     *   2) GET /api/users/{id} —— 子资源定位 + 404 异常路径
     *   3) POST /api/users —— 创建动作 + 请求体验证 + 重复创建（409）
     *   4) PUT /api/users/{id} —— 全量更新 + 不存在 404
     *   5) DELETE /api/users/{id} —— 幂等删除 + 不存在路径行为约定
     *   6) GET /api/users/username/{username} —— 备用键查询
     *   7) 每个用例的双断言维度：HTTP 状态码 + 包体结构（ApiResponse 格式）

     * 二、状态码断言与 ApiResponse 字段断言的联合校验
     *   1) NOT_FOUND 用例示范（契约完整性）：
     *      mockMvc.perform(get("/api/users/999"))
     *        .andExpect(status().isNotFound())                    // HTTP 层
     *        .andExpect(jsonPath("$.success").value(false))       // 包体业务层
     *        .andExpect(jsonPath("$.data").doesNotExist())        // 成功才有的字段
     *        .andExpect(jsonPath("$.message").exists())           // 错误描述
     *        .andExpect(jsonPath("$.timestamp").exists())         // 时间戳
     *        .andExpect(jsonPath("$.path").value("/api/users/999")); // 路径回显
     *   2) CREATED 用例（当前 200，演进方向 201）：
     *      .andExpect(status().isOk())  // 当前
     *      // .andExpect(status().isCreated())  // 演进目标
     *      .andExpect(jsonPath("$.data.id").isNumber())
     *      .andExpect(jsonPath("$.data.createdAt").exists())
     *   3) 分页用例：
     *      .andExpect(jsonPath("$.data.content").isArray())
     *      .andExpect(jsonPath("$.data.totalElements").isNumber())
     *      .andExpect(jsonPath("$.data.number").value(0))  // 当前页
     *   4) 双断言的必要性：单状态码 / 单包体断言各缺一面
     *   5) 契约测试模板化：可封装 helper（getAndAssert / postAndAssert）

     * 三、HTTP 方法测试的细节陷阱
     *   1) PUT 全量替换语义：body 必须含全部可改字段，缺失字段不等于"不改"
     *      —— 用例设计：PUT body 只给 username，应出现 email 被置 null 的行为
     *      （若实现是"非 null 才改"——这是 PATCH 语义的错位实现，测试应揪出）
     *   2) Content-Type 缺失：POST 无 application/json 头会 415
     *      —— 一条用例覆盖这个客户端常见错误
     *   3) 请求体格式坏（坏 JSON）：应 400 而非 500
     *      —— 全局处理器对 HttpMessageNotReadableException 的映射（补强点）
     *   4) 必填字段空：Bean Validation 触发 MethodArgumentNotValidException
     *      —— 断言 400 + 字段级错误信息（fieldName + message）
     *   5) 超大请求体：server.max-http-header-size 限制触发
     *      —— 集成层冒烟测试覆盖（此层不测）
     *   6) 幂等键场景（若引入 Idempotency-Key）：
     *      重复同一 key + same body -> 第二次返回第一次结果快照（200 而非重复创建）

     * 四、路由不存在与方法不匹配的契约测试
     *   1) 404 路由不存在：get("/api/userx") -> 404 + 包体错误码（若统一）
     *   2) 405 方法不匹配：put("/hello")（hello 只有 GET/POST）
     *      -> 405 + 响应头 Allow 包含 GET, POST
     *      断言：.andExpect(header().string("Allow", containsString("GET")))
     *   3) 404 vs 405 区分：路径错（路由表里没有）-> 404；
     *      路径对但方法错（路由表里有该路径）-> 405
     *   4) Spring 版本差异：Boot 3 默认 404 路由不存在抛 NoResourceFoundException
     *      （需配置 throw-exception-if-no-handler-found 或用默认机制）

     * 五、契约演进测试的"半严格"模式（平衡稳定与演进）
     *   1) 严格 equals：断言 data.email 等于固定值
     *      ——格式或默认值改了测试就红，但能 100% 拦截意外变更
     *   2) 半严格：只断言字段存在与类型（hasJsonPath + isString）
     *      ——可弹性接收"加字段"，但拦不住"值语义"变化
     *   3) 选择依据：
     *      核心业务字段（username/id/success）-> 严格 equals
     *      辅助字段（timestamp/message 精确文案）-> 半严格
     *   4) 删字段必须让测试红：用 hasJsonPath 断言存在，
     *      把"字段下线"主动加入测试用例的删除流程
     *   5) 用 @Tag 分组：@Tag("contract") 标记契约核心用例，
     *      CI 在任何提交都跑，确保变更不会静默破坏契约
     * =========================================================================
     */
}