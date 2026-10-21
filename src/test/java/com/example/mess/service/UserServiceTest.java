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
     * 【DDD 测试策略】领域层测试金字塔（非可执行代码）
     * =========================================================================
     *
     * 一、DDD 各层的测试策略
     *   领域层（实体/值对象/领域服务）：
     *   - 纯业务逻辑，无框架依赖
     *   - 测试：纯单元测试，无 Mock，无 Spring，毫秒级
     *   - 示例：测试 User.changeEmail() 的业务规则
     *     @Test void changeEmail_validEmail_updatesEmail() {
     *       User user = new User("alice", "old@x.com");
     *       user.changeEmail("new@x.com");
     *       assertEquals("new@x.com", user.getEmail());
     *     }
     *   - 覆盖率要求：最高（>90%），业务规则不能有漏洞
     *
     *   应用层（应用服务/用例编排）：
     *   - 编排逻辑，依赖领域层和仓储
     *   - 测试：单元测试 + Mock 仓储，验证编排流程
     *   - 本类 UserServiceTest 就是应用层测试
     *   - 验证点：调了正确的仓储方法、事务边界正确、事件发布正确
     *
     *   基础设施层（仓储实现/外部服务）：
     *   - 技术实现，依赖 JPA/HTTP/MQ
     *   - 测试：集成测试，@DataJpaTest + 真实数据库（Testcontainers）
     *   - 验证点：SQL 正确、映射正确、事务正确
     *
     *   门面层（Controller/Listener）：
     *   - 协议适配，依赖应用层
     *   - 测试：切片测试 @WebMvcTest + @MockBean 应用服务
     *   - 验证点：路由正确、参数绑定正确、序列化正确、状态码正确
     *
     * 二、测试替身（Test Double）分类
     *   1) Mock：验证交互（调了什么方法、传了什么参数）
     *      Mockito.when/verify 就是 Mock
     *      适用：验证编排逻辑（应用服务调了仓储）
     *   2) Stub：提供预设返回值（不验证交互）
     *      Mockito.when(repo.findById(1L)).thenReturn(user) 就是 Stub
     *      适用：为被测对象提供依赖数据
     *   3) Spy：部分真实 + 部分打桩
     *      @Spy 真实对象 + 覆盖部分方法
     *      适用：测试真实逻辑但拦截副作用（如发邮件）
     *   4) Fake：简化实现（如内存 UserRepository）
     *      InMemoryUserRepository implements UserRepository
     *      适用：领域层测试，不依赖 Mockito，更接近真实行为
     *   5) Dummy：只占位，不使用
     *      传 null 或空对象只为满足签名
     *
     * 三、Given-When-Then（BDD 风格）
     *   与 Arrange-Act-Assert 本质相同，但用业务语言描述：
     *   Given（假设）：一个已存在的用户，username 为 alice
     *   When（当）：调用 findByUsername("alice")
     *   Then（那么）：返回该用户，且 id 为 1
     *   代码映射：
     *   given(userRepository.findByUsername("alice")).willReturn(user);  // Given
     *   User result = userService.findByUsername("alice");               // When
     *   assertEquals(1L, result.getId());                                // Then
     *   BDDMockito：given().willReturn() 替代 when().thenReturn()，语义更贴合 BDD
     *
     * 四、测试数据构建模式
     *   1) Test Data Builder：
     *      UserBuilder.anUser().withUsername("alice").withEmail("a@x.com").build()
     *      优点：可读、可复用、默认值合理、按需覆盖
     *   2) Object Mother：
     *      UserFactory.alice() / UserFactory.bob()
     *      优点：预定义角色化测试数据，语义清晰
     *      缺点：角色膨胀后难维护
     *   3) EasyRandom/Instancio：自动生成随机测试数据
     *      优点：零模板代码
     *      缺点：数据不可预测，断言困难，适合填充不关心字段
     *   建议：核心实体用 Builder，辅助数据用随机生成
     * =========================================================================
     */
}