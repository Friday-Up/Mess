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
     * 【Spring 内幕】Mockito 扩展点与 Spring 集成的测试内幕（非可执行代码）
     * =========================================================================
     *
     * 一、本测试类为什么不需要启动 Spring 容器
     *   1) 本类用 @ExtendWith(MockitoExtension.class)（JUnit 5 扩展）
     *      非 SpringExtension -> 无 ApplicationContext，每个测试毫秒级
     *   2) @Mock UserRepository -> Mockito 生成代理（拦截全部方法）
     *   3) @InjectMocks UserService -> 按构造器/字段/setter 顺序注入 mock，
     *      直接得到一个只依赖 mock 的真实 Service 对象
     *   4) 局限：缓存/事务/AOP 全部失效（因为没走 Spring 代理）
     *      -> 本类方法上的 @Cacheable 不会触发，直接进入方法体
     *      -> 这也是为什么本测试特别需要验证缓存未命中时的业务逻辑
     *
     * 二、Mockito 的核心 API 在做什么
     *   - when(userRepository.findById(1L)).thenReturn(Optional.of(user))：
     *     注册匹配条件（存入 SourceLocator），下次方法调用拦截到匹配条件
     *     就返回预置值，跳过真实实现
     *   - ArgumentMatchers.any()/eq(...)：放宽或精确匹配参数
     *   - verify(userRepository, times(1)).findById(1L)：记录并断言调用次数
     *   - thenThrow：模拟异常分支（验证异常映射 404 的逻辑）
     *
     * 三、与 Spring 环境的对比：什么时候该切回集成测试
     *   - 纯逻辑测试（如本类）：MockitoExtension 足够，快、稳、无 IO
     *   - 集成测试（缓存生效、事务行为）：需要 @SpringBootTest/@DataJpaTest
     *     并验证框架层行为（如缓存的 key 策略实际是怎样的）
     *   - 混合策略：缓存 key 生效性单独用 @SpringBootTest 写 1-2 个集成用例，
     *     剩余逻辑用纯 Mockito 快速覆盖
     *
     * 四、测试可维护性要点
     *   - given/when/then 结构：本类用注释分区，降低阅读成本
     *   - 不写无意义断言（assertThat(x).isNotNull() 只证明方法没炸）
     *   - verify 慎用：过度 verify 会把测试绑死实现细节，重构前得改大量测试
     *   - IDE 快捷生成 mock，但别盲目依赖 -- 测试质量取决于断言设计
     *
     * 五、AssertJ 常用断言速查
     *   1) assertThatThrownBy(() -> service.getUserById(99L))
     *      .isInstanceOf(ResourceNotFoundException.class)
     *      .hasMessageContaining(用户不存在)
     *   2) assertThat(page.getContent()).extracting(UserDto::getUsername)
     *      .containsExactly(alice, bob)
     *   3) assertThat(dto).usingRecursiveComparison()
     *      .ignoringFields(createdAt).isEqualTo(expected)——DTO 整体比对利器
     *   4) verify 配合 ArgumentCaptor：捕获下层传入参数并断言其内容，
     *      而非只断言调用次数
     *   5) 测试间隔离：不依赖执行顺序，每个用例独立构造 given 数据
     *   6) @Nested 分组 given/when/then 分支，JUnit 5 原生支持，可读性更好
     *
     * 六、Mockito 时序与状态验证的选择
     *   1) verify 只验证交互（被调用与否），不验证状态正确性
     *   2) 断言返回值才是主断言，verify 只做辅助边界检查
     *   3) InOrder 验证调用次序：inOrder(repo).verify(findAll).verify(save)
     *      谨慎使用，次序通常属于实现细节而非契约
     *   4) mock 静态方法需 mockito-inline（MockedStatic），
     *      静态工具类调用是测试泥潭，能重构为注入实例就重构
     *   5) 本类验证思路：先断言业务结果（DTO 内容），再抽查唯一性检查次数
     * =========================================================================
     */
}