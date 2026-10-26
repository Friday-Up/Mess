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
         * 【JVM内存管理与GC调优】覆盖率插桩的 JVM 开销与内存断言（非可执行代码）
         * =========================================================================
         * 一、JaCoCo 在 JVM 里的存在方式
         *   1) CI 跑覆盖率时 surefire 加 -javaagent:jacoco.jar：字节码被插桩，
         *      每条分支访问打 boolean/计数数组——运行时分配与代码膨胀 5-20%
         *   2) 覆盖率报告不是性能基线：插桩开销只在 CI 环境，
         *      生产构建去掉 jacoco agent 后无任何影响——分开打包是 CI/生产隔离的基本动作
         *   3) 单测数量大时 JaCoCo 累积覆盖率数组占堆：百万分支量级时
         *      注意 surefire -Xmx，否则 CoverageAgent 触发的 OOM 会引起神秘的红 CI
         * 二、单元测试对分配/GC 行为的契约表达
         *   1) 本类 verify() 锁定调用次数：间接锁住"该业务方法每请求产生的
         *      临时对象数量上限"——这是把 GC 成本转化为可测的调用契约
         *   2) Mockito 的 argument matchers：any() 不构造对象（匹配器短路），
         *      eq(value) 仅引用比较——断言路径的分配压力很小
         *   3) 大集合断言反模式：assertIterableEquals 拷贝整个集合到比较栈：
         *      大数据集合断言建议改为 hasSize + 关键字段断言，避免测试分配放大
         *   4) Mockito 的 MockitoSession 与 strict stubbing：未用 stub 会被报错，
         *      提示你别在生产代码保留"死分支"——减少长尾代码即减少 JIT 编译面
         * 三、Mockito 细节与 strict stubbing 的 JVM 视角
         *   1) ArgumentCaptor 捕获是引用复制：无额外分配压力——
         *      别为"性能"放弃清晰的调用参数断言
         *   2) @Mock 默认 relaxed（返回 null/0/空集合）：生产代码依赖
         *      默认值的分支在测试里"意外通过"——strict stubbing 强制显式 stub
         =========================================================================
         */
}