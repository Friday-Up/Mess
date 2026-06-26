package com.example.mess.repository;

import com.example.mess.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户数据访问接口 - 继承JpaRepository获得基本CRUD操作
 * 
 * 自定义查询方法（Spring Data JPA根据方法名自动生成SQL）:
 * - findByUsername/findByEmail: 按字段查询，返回Optional
 * - existsByUsername/existsByEmail: 检查唯一性，返回boolean
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /** 根据用户名查询，建议在username字段上创建索引 */
    Optional<User> findByUsername(String username);

    /** 根据邮箱查询，建议在email字段上创建索引 */
    Optional<User> findByEmail(String email);

    /** 检查用户名是否已存在（用于注册时唯一性校验） */
    boolean existsByUsername(String username);

    /** 检查邮箱是否已存在（用于注册时唯一性校验） */
    boolean existsByEmail(String email);
}