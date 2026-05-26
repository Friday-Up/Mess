package com.example.mess.repository;

import com.example.mess.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户数据访问接口
 * 继承JpaRepository，获得基本的CRUD操作
 * 
 * @Repository 标识这是一个数据访问层组件
 * @JpaRepository 提供基本的JPA操作，包括CRUD和分页查询
 * 
 * 继承的常用方法:
 * - findAll(): 查询所有用户
 * - findById(ID id): 根据ID查询用户
 * - save(S entity): 保存或更新用户
 * - deleteById(ID id): 根据ID删除用户
 * - existsById(ID id): 判断用户是否存在
 * - count(): 统计用户总数
 * 
 * 自定义方法:
 * - findByUsername(String username): 根据用户名查询用户
 * - findByEmail(String email): 根据邮箱查询用户
 * 
 * 设计原则:
 * - 接口隔离原则：只定义必要的查询方法
 * - 单一职责原则：只处理用户数据访问
 * - 开闭原则：通过继承扩展功能，不修改已有代码
 * 
 * 作者: zhangyaolong.5
 * 创建时间: 2026-05-26
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 根据用户名查询用户
     * 
     * Spring Data JPA会根据方法名自动生成查询SQL
     * 方法名遵循规则：findBy + 属性名（首字母大写）
     * 
     * 生成的SQL类似：SELECT * FROM user WHERE username = ?
     * 
     * 使用场景:
     * - 用户登录验证
     * - 用户名唯一性检查
     * - 用户搜索功能
     * 
     * 性能优化:
     * - 建议在数据库中为username字段创建索引
     * - 示例SQL: CREATE INDEX idx_user_username ON user(username);
     * 
     * 返回类型:
     * - Optional<User> 避免空指针异常
     * - 如果找不到用户，返回Optional.empty()
     * 
     * 示例:
     * Optional<User> user = userRepository.findByUsername("admin");
     * if (user.isPresent()) {
     *     User u = user.get();
     *     // 处理用户数据
     * }
     * 
     * @param username 用户名，区分大小写
     * @return Optional<User> 用户对象，如果不存在则返回空Optional
     */
    Optional<User> findByUsername(String username);

    /**
     * 根据邮箱查询用户
     * 
     * Spring Data JPA会根据方法名自动生成查询SQL
     * 方法名遵循规则：findBy + 属性名（首字母大写）
     * 
     * 生成的SQL类似：SELECT * FROM user WHERE email = ?
     * 
     * 使用场景:
     * - 用户邮箱验证
     * - 邮箱唯一性检查
     * - 通过邮箱找回密码
     * 
     * 性能优化:
     * - 建议在数据库中为email字段创建索引
     * - 示例SQL: CREATE INDEX idx_user_email ON user(email);
     * 
     * 返回类型:
     * - Optional<User> 避免空指针异常
     * - 如果找不到用户，返回Optional.empty()
     * 
     * 示例:
     * Optional<User> user = userRepository.findByEmail("user@example.com");
     * if (user.isPresent()) {
     *     User u = user.get();
     *     // 处理用户数据
     * }
     * 
     * @param email 邮箱地址，区分大小写
     * @return Optional<User> 用户对象，如果不存在则返回空Optional
     */
    Optional<User> findByEmail(String email);

    /**
     * 检查用户名是否存在
     * 
     * Spring Data JPA会根据方法名自动生成查询SQL
     * 方法名遵循规则：existsBy + 属性名（首字母大写）
     * 
     * 生成的SQL类似：SELECT COUNT(*) FROM user WHERE username = ?
     * 
     * 使用场景:
     * - 用户注册时检查用户名是否已被占用
     * - 用户名唯一性验证
     * 
     * 性能优化:
     * - 数据库会自动优化COUNT查询
     * - 建议在username字段上创建索引
     * 
     * 示例:
     * boolean exists = userRepository.existsByUsername("newuser");
     * if (exists) {
     *     throw new RuntimeException("用户名已存在");
     * }
     * 
     * @param username 用户名
     * @return boolean 如果用户名存在返回true，否则返回false
     */
    boolean existsByUsername(String username);

    /**
     * 检查邮箱是否存在
     * 
     * Spring Data JPA会根据方法名自动生成查询SQL
     * 方法名遵循规则：existsBy + 属性名（首字母大写）
     * 
     * 生成的SQL类似：SELECT COUNT(*) FROM user WHERE email = ?
     * 
     * 使用场景:
     * - 用户注册时检查邮箱是否已被占用
     * - 邮箱唯一性验证
     * 
     * 性能优化:
     * - 数据库会自动优化COUNT查询
     * - 建议在email字段上创建索引
     * 
     * 示例:
     * boolean exists = userRepository.existsByEmail("user@example.com");
     * if (exists) {
     *     throw new RuntimeException("邮箱已被注册");
     * }
     * 
     * @param email 邮箱地址
     * @return boolean 如果邮箱存在返回true，否则返回false
     */
    boolean existsByEmail(String email);
}