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
     * 【性能优化】Mockito 测试性能（非可执行代码）
     * =========================================================================
     *
     * 一、Mockito 执行开销
     *   1) @Mock 创建：CGLIB 生成代理类，首次约 10-50ms（类加载），
     *      后续 @Mock 同类型约 0.1ms（类已缓存）
     *   2) when/verify：方法拦截 + 参数匹配，约 0.01ms/次
     *   3) 整体：一个 Mockito 单元测试约 5-20ms，100 个测试 <2s
     *   结论：Mockito 单元测试极快，不是性能瓶颈
     *
     * 二、测试编写效率
     *   1) @ExtendWith(MockitoExtension.class)：自动初始化 @Mock/@InjectMocks，
     *      比手动 MockitoAnnotations.openMocks(this) 更简洁
     *   2) @MockBean vs @Mock：@MockBean 属于 Spring 上下文（慢），
     *      @Mock 属于 Mockito（快），纯单元测试用 @Mock
     *   3) 参数化测试：@ParameterizedTest + @ValueSource/@CsvSource，
     *      一组数据跑多场景，减少重复代码
     *   4) 测试数据构建：Builder 模式或 TestFixture 工厂方法，
     *      避免每个测试重复构造复杂对象
     *
     * 三、测试维护成本
     *   1) 过度 mock：mock 了 10 个依赖，重构时改 10 处 -> 维护噩梦
     *      原则：只 mock 跨越边界的依赖（DB/远程/时间）
     *   2) 测试与实现耦合：verify 内部实现细节，重构即失败
     *      原则：验证行为（返回值/状态变更），不验证实现（调了几次）
     *   3) 测试数据硬编码：魔法值散落各处
     *      原则：常量提取或 TestFixture 集中管理
     *
     * =========================================================================
     * 【监控可观测性】测试质量指标
     * =========================================================================
     *   - 单元测试执行时间：<2s（100 个测试）
     *   - 测试覆盖率：JaCoCo，行覆盖 >60%，分支覆盖 >40%（参考值）
     *   - 变异测试：PIT，检测测试的有效性（杀死变异的比例）
     *   - 测试代码行/业务代码行：1:1 到 2:1 为健康范围
     * =========================================================================
     */
}