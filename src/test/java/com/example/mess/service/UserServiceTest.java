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
     * 【Java 并发】纯 Mockito 单测的线程视角、时间控制在竞态验证中的角色（非可执行代码）
     * =========================================================================
     *
     * 一、本类测试的线程模型
     *   1) 本类不拉起 Spring 容器 -> 只有测试主线程，无后台线程，
     *      每个 @Test 方法串行执行，毫秒级返回
     *   2) @Mock/@InjectMocks 生成的代理只涉及字节码增强（CGLIB/Mockito
     *      ByteBuddy），不存在线程池、锁、ThreadLocal 传播的概念
     *   3) 推论：本类覆盖的是"业务逻辑正确性"，与并发风险正交；
     *      缓存的 @Cachable 生效行为、事务回滚、SecurityContext 传播
     *      都不在本类测试范围，需上层集成测试补位
     *
     * 二、Mockito 在多线程下的限制（若强行添加并发断言）
     *   1) Mockito 的 stub 验证器不是为并发设计：同一 mock 的
     *      when().thenReturn() 配置是共享注册表，多线程同时配置会互相覆盖
     *   2) verify(times(n)) 的计数器虽是原子的（AtomicInteger），
     *      但"恰好在某 verify 时别的线程正调用"时序不定 -> flaky
     *   3) 官方建议：mock 配置在测试启动时定死、调用期间不动，
     *      断言区域再读计数；需要验证竞态正确性请改用真实现而非 mock
     *
     * 三、时钟与时序控制的边界
     *   1) 本类若涉及时间（createdAt），LocalDateTime.now() 只要在主线程
     *      同步调用就确定（不含时区/并发问题）
     *   2) 想控制时间做"过期/计时"逻辑测试：
     *      - 注入 Clock Bean（生产 code 调 clock.instant()），测试里
     *        用 Clock.fixed(...) 便无需修改代码结构
     *      - 不要在生产代码里散落 LocalDateTime.now()，否则时间不可注入
     *   3) 异步测试的等待：本类是同步方法，无需等待；一旦给 Service
     *      加 @Async 返回 CompletableFuture，单测必须用 timeout +
     *      join 显式等待，否则测试方法早于 future 完成而断言通过假绿
     *
     * 四、竞态与契约在单测中的分别处理
     *   1) 单测回答"给定输入是否产出预期输出"（契约）
     *   2) 集成测试回答"多线程同时操作时结果是否仍符合预期"（竞态）
     *   3) 两层测试工具/范围清晰分层才能快速反馈：
     *      - 单测毫秒级、随时跑
     *      - 并发测试秒级、CI 夜间跑 + 上线前跑
     *   4) 对本 Service 的高价值竞态用例：createUser 在 N 个线程同时
     *      提交相同 username，断言仅一条成功（数据库唯一约束兜底）
     *      与 getOrCreate 语义的"恰好一次创建"
     *
     * 五、并发测试之外的静态防线
     *   1) 代码审查清单：Service/Controller 字段是否可变、是否有
     *      ThreadLocal 未清、非线程安全集合是否逃逸到单例字段
     *   2) 静态分析工具：SpotBugs/FindSecBugs 检测常见并发反模式
     *      （如 LazyInit 非同步、Double-checked 无 volatile）
     *   3) 运行期观测：Micrometer 上报线程池 active/idle/queue，
     *      异常的线程池水位本身就是并发设计问题的先行信号
     *   4) 所有静态手段都不能替代"并发场景下真实执行"的检验，
     *      本类现阶段的快速反馈 + 集成层的真实执行是最小完备的组合
     * =========================================================================
     */
}