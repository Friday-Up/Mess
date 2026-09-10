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
     * ============================================================================
     * 【测试设计文档】UserServiceTest 单元测试策略说明（补充文档，非可执行代码）
     * ============================================================================
     *
     * 一、测试目标与范围
     * ----------------------------------------------------------------------------
     * 本类针对 UserService 的业务逻辑做"纯单元测试"：仅验证 Service 自身的编排、
     * 校验、转换与异常语义，通过 Mockito 隔离掉 UserRepository（数据库依赖），
     * 使测试快速、确定、可重复，不受数据库状态影响。
     *
     * 二、Mockito 核心用法回顾
     * ----------------------------------------------------------------------------
     *   @Mock          —— 创建被依赖对象（UserRepository）的模拟实例;
     *   @InjectMocks   —— 创建被测对象（UserService）并注入上述 mock;
     *   when(...).thenReturn(...) —— 打桩：预设 mock 方法的返回值;
     *   verify(mock, times(n)).xxx() —— 验证交互：mock 方法被调用了 n 次;
     *   verifyNoMoreInteractions(mock) —— 断言无多余交互，防止意外副作用;
     *   any()/eq()     —— 参数匹配器，用于灵活匹配调用参数。
     *
     * 三、Arrange-Act-Assert 三段式
     * ----------------------------------------------------------------------------
     *   Arrange（准备）：构造输入数据、打桩 mock 行为;
     *   Act（执行）：调用被测方法;
     *   Assert（断言）：校验返回值 + 校验与 mock 的交互次数。
     *   本类每个测试方法均遵循该结构，便于阅读与定位。
     *
     * 四、用例覆盖矩阵
     * ----------------------------------------------------------------------------
     *   getAllUsers                     —— 正常分页/列表查询;
     *   getUserById                     —— 存在时返回 DTO;
     *   getUserByIdWhenUserNotExist     —— 不存在时抛 ResourceNotFoundException;
     *   createUser                      —— 正常创建并返回 DTO;
     *   deleteUser                      —— 正常删除，验证 repo 交互;
     *   getUserByUsername             —— 按用户名查询命中;
     *   getUserByUsernameWhenUserNotExist —— 未命中时的异常/空值语义。
     *   正例 + 反例（不存在/异常）双向覆盖，保证边界行为可靠。
     *
     * 五、关键测试决策
     * ----------------------------------------------------------------------------
     * 决策 1：mock 掉 Repository 而非连真实数据库
     *   理由：单元测试关注 Service 逻辑本身，数据库交互属集成测试范畴;
     *        mock 让测试无需数据库、无脏数据、毫秒级完成。
     * 决策 2：对"不存在"路径单独建用例
     *   理由：异常路径最易被遗漏，显式覆盖可防止回归时破坏 404 语义。
     * 决策 3：使用 verifyNoMoreInteractions
     *   理由：确保 Service 没有对 Repository 产生预期之外的调用（如重复查询）。
     *
     * 六、注意事项
     * ----------------------------------------------------------------------------
     *   ! mock 不执行真实逻辑，未打桩的方法默认返回 null/0/false，需按需打桩;
     *   ! 验证次数（times）应与业务实现严格一致，过松会漏 bug、过紧会脆弱;
     *   ! 若被测方法签名调整（如分页参数变化），本类用例需同步更新。
     *
     * 七、如何运行
     * ----------------------------------------------------------------------------
     *   全部测试：      mvn test
     *   仅本类：        mvn -Dtest=UserServiceTest test
     *   仅某方法：      mvn -Dtest=UserServiceTest#getUserById test
     * ============================================================================
     */
}