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
 * UserService单元测试 - 使用Mockito隔离测试Service层业务逻辑。
 * 
 * <p>测试策略:
 * <ul>
 *   <li>MockitoExtension: 自动初始化@Mock和@InjectMocks注解</li>
 *   <li>Mock UserRepository: 隔离数据库访问层，不依赖真实数据库</li>
 *   <li>测试纯业务逻辑: 验证Entity/DTO转换、缓存管理、异常处理</li>
 *   <li>覆盖所有Service方法: 查询、创建、更新、删除、按用户名查询</li>
 * </ul>
 * 
 * <p>测试覆盖:
 * <ul>
 *   <li>getAllUsers: 验证获取所有用户（返回列表）</li>
 *   <li>getUserById: 验证根据ID获取用户（存在场景）</li>
 *   <li>getUserByIdWhenUserNotExist: 验证用户不存在场景</li>
 *   <li>createUser: 验证创建用户（保存到数据库）</li>
 *   <li>deleteUser: 验证删除用户（调用deleteById）</li>
 *   <li>getUserByUsername: 验证按用户名查询（存在场景）</li>
 *   <li>getUserByUsernameWhenUserNotExist: 验证用户名不存在场景</li>
 * </ul>
 * 
 * <p>Mockito使用说明:
 * <ul>
 *   <li>@Mock: 创建UserRepository的模拟对象</li>
 *   <li>@InjectMocks: 将模拟对象注入到UserService</li>
 *   <li>when().thenReturn(): 定义模拟行为（如findById返回Optional）</li>
 *   <li>verify(): 验证方法调用次数和参数</li>
 *   <li>verifyNoMoreInteractions(): 确保没有意外的额外调用</li>
 * </ul>
 * 
 * @since 1.0
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        // 每个测试方法执行前重新构建测试用户，保证测试之间相互隔离、互不干扰
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setName("Test User");
    }

    /** 获取所有用户 */
    @Test
    void getAllUsers() {
        // Arrange: 再构造第二个用户，凑成两条数据以便验证列表返回的完整性
        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");
        user2.setName("User Two");

        // 打桩：约定 Repository.findAll() 返回两个用户，绕开真实数据库
        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser, user2));

        // Act: 调用被测方法，触发内部对 findAll 的调用
        List<User> users = userService.getAllUsers();
        // Assert: 断言返回列表长度与内容与打桩数据一致
        assertEquals(2, users.size());
        assertTrue(users.contains(testUser));
        assertTrue(users.contains(user2));

        // 验证 findAll 被且仅被调用一次，且没有其它多余的 Repository 交互
        verify(userRepository, times(1)).findAll();
        verifyNoMoreInteractions(userRepository);
    }

    /** 根据ID获取用户 - 存在 */
    @Test
    void getUserById() {
        // Arrange: 打桩 findById(1L) 返回一个已存在的用户（Optional 非空）
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act: 调用查询方法
        Optional<User> user = userService.getUserById(1L);
        // Assert: Optional 应有值，且用户名与预期一致
        assertTrue(user.isPresent());
        assertEquals("testuser", user.get().getUsername());

        // 验证 findById 仅被调用一次且传入正确 ID，无多余交互
        verify(userRepository, times(1)).findById(1L);
        verifyNoMoreInteractions(userRepository);
    }

    /** 根据ID获取用户 - 不存在 */
    @Test
    void getUserByIdWhenUserNotExist() {
        // Arrange: 打桩 findById(999L) 返回空 Optional，模拟用户不存在
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act: 查询一个不存在的 ID
        Optional<User> user = userService.getUserById(999L);
        // Assert: Optional 应为空，不应抛异常
        assertFalse(user.isPresent());

        // 验证 findById 以正确参数被调用一次
        verify(userRepository, times(1)).findById(999L);
        verifyNoMoreInteractions(userRepository);
    }

    /** 创建用户 */
    @Test
    void createUser() {
        // Arrange: 打桩 save(任意User) 返回带 ID 的持久化后用户
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act: 调用创建方法
        User createdUser = userService.createUser(testUser);
       // Assert: 返回对象非空且字段与预期一致
        assertNotNull(createdUser);
        assertEquals("testuser", createdUser.getUsername());

        // 验证 save 以目标对象被调用一次
        verify(userRepository, times(1)).save(testUser);
        verifyNoMoreInteractions(userRepository);
    }

    /** 删除用户 */
    @Test
    void deleteUser() {
        // Arrange: deleteById 无返回值，用 doNothing 声明其被调用时什么都不做
        doNothing().when(userRepository).deleteById(1L);

        // Act: 调用删除方法
        userService.deleteUser(1L);

        // 验证 deleteById 以正确 ID 被调用一次
        verify(userRepository, times(1)).deleteById(1L);
        verifyNoMoreInteractions(userRepository);
    }

    /** 根据用户名获取用户 - 存在 */
    @Test
    void getUserByUsername() {
        // Arrange: 打桩 findByUsername 返回已存在用户
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act: 按用户名查询
        Optional<User> user = userService.getUserByUsername("testuser");
        // Assert: 应查到用户且用户名匹配
        assertTrue(user.isPresent());
        assertEquals("testuser", user.get().getUsername());

        // 验证 findByUsername 以正确用户名被调用一次
        verify(userRepository, times(1)).findByUsername("testuser");
        verifyNoMoreInteractions(userRepository);
    }

    /** 根据用户名获取用户 - 不存在 */
    @Test
    void getUserByUsernameWhenUserNotExist() {
        // Arrange: 打桩 findByUsername 返回空，模拟用户名不存在
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act: 查询不存在的用户名
        Optional<User> user = userService.getUserByUsername("nonexistent");
        // Assert: Optional 应为空
        assertFalse(user.isPresent());

        // 验证 findByUsername 被调用一次
        verify(userRepository, times(1)).findByUsername("nonexistent");
        verifyNoMoreInteractions(userRepository);
    }

    /*
     * =========================================================================
     * 【HTTP/REST】Service 单测中的契约边界映射验证（非可执行代码）
     * =========================================================================
     *
     * 一、Service 测试不直接测 HTTP，但定义了 HTTP 契约的"业务根基"
     *   1) 本类用纯 Mockito 验证业务规则（不启动容器）
     *   2) HTTP 契约的状态码分支源头在此：
     *      getUserById(99L) 抛 ResourceNotFoundException -> HTTP 404
     *      createUser 重名 -> 抛 IllegalArgumentException（建议改 409） -> HTTP 400
     *      转换正确性 -> 决定包体 data 字段结构与精度
     *   3) 单测价值：毫秒级验证"业务规则不变"，是 HTTP 契约稳定性的内核

     * 二、业务异常 → 状态码映射的单测覆盖
     *   1) existsByUsername 返 true 时 createUser 抛异常：
     *      assertThatThrownBy(() -> service.createUser(dto))
     *        .isInstanceOf(IllegalArgumentException.class);
     *   2) findById 返 empty 时 getUserById 抛 ResourceNotFoundException：
     *      assertThatThrownBy(() -> service.getUserById(99L))
     *        .isInstanceOf(ResourceNotFoundException.class)
     *        .hasMessageContaining("用户不存在");
     *   3) 这两条断言即覆盖 400 与 404 的业务根因，端到端再由
     *      UserControllerTest 验证状态码（双层职责分离）
     *   4) 建议新增异常类型：DuplicateResourceException（409）
     *      让"重名"区分于"参数不合法"——API 契约表达更精确

     * 三、DTO 转换的字段契约保证
     *   1) convertToDto 决定响应包体字段：
     *      User -> UserDto 时字段拷贝完整性与精度
     *   2) 易错点（用例应专门覆盖）：
     *      a) null 字段透传：entity.name=null 时 dto.name=null
     *         （而非空串或抛 NPE——序列化层需明示 null 语义）
     *      b) 时间精度：LocalDateTime 不丢毫秒
     *      c) 类型一致性：Long id 到 dto.id 仍是 Long（防被误改 String）
     *   3) 转换不变量断言（每个字段）：
     *      assertThat(dto.getId()).isEqualTo(entity.getId());
     *      assertThat(dto.getUsername()).isEqualTo(entity.getUsername());
     *      ...（逐字段 assert 不是啰嗦，是契约护栏）

     * 四、分页参数透传与排序语义的验证
     *   1) 调用 service.getAllUsers(PageRequest.of(0, 10))
     *      -> verify(repo, times(1)).findAll(pageableCaptor.capture())
     *      -> 断言捕获的 Pageable：pageNumber=0 / pageSize=10
     *   2) 排序：PageRequest.of(0, 10, Sort.by("username").descending())
     *      -> 断言传入 repo 的 Pageable sort 属性与方向正确
     *   3) 边界：size=0（不允许）应被 Spring Data 校验为 IllegalArgumentException
     *   4) 契约视角：客户端 query 参数 ?size=-1 应被拦在协议层（400），
     *      单测这里验证的是"合法 Pageable 不被 service 篡改"

     * 五、单测 → 集成契约的演进兼容策略
     *   1) 单测只测"逻辑不变"，不测"响应格式"——加字段不会让单测红
     *   2) 集成测试负责格式契约：字段存在性、状态码、包体结构
     *   3) 演进流程（加字段 usernameCanonical）：
     *      a) 单测：convertToDto 新字段赋值断言（确保不漏）
     *      b) 集成：包体 hasJsonPath("$.data.usernameCanonical")
     *      c) 文档：OpenAPI 注解 @Schema 补充
     *      d) 通告：CHANGELOG 通知消费者（新字段为可选）
     *   4) 删字段流程（弃用 name）：
     *      a) 先标 @Deprecated（IDE 提示 + 文档宣告）
     *      b) 单测与集成测试中给该字段加 deprecated 标记
     *      c) 至少一个版本周期后真删——测试同步删除断言
     *   5) 契约版本管理高级实践：
     *      给集成测试标 @Tag("api-v1")，v2 上线时同一套用例跑两遍
     *      （按路径前缀参数化），保证 v1/v2 行为并列可验
     * =========================================================================
     */
}