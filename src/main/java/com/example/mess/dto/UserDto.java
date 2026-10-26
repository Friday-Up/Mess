package com.example.mess.dto;

import java.time.LocalDateTime;

/**
 * 用户数据传输对象（DTO） - 用于各层之间安全传输用户数据。
 * 
 * <p>DTO模式的核心价值在于解耦内部实体与外部接口：
 * <ul>
 *   <li>避免直接暴露实体类内部结构（如JPA注解、数据库映射细节）</li>
 *   <li>只包含对外暴露的必要字段，不含密码等敏感信息</li>
 *   <li>支持API版本演进，实体变更不影响外部接口契约</li>
 *   <li>转换逻辑集中在UserService中，便于维护和测试</li>
 * </ul>
 * 
 * <p>字段说明:
 * <ul>
 *   <li><b>id</b> - 用户唯一标识，由数据库自动生成</li>
 *   <li><b>username</b> - 用户名，用于登录认证，具有唯一性约束，1-50字符</li>
 *   <li><b>email</b> - 电子邮箱，用于通信通知，具有唯一性约束，1-100字符</li>
 *   <li><b>name</b> - 用户真实姓名，用于显示，无唯一性约束，1-100字符</li>
 *   <li><b>createdAt</b> - 用户创建时间，由数据库自动设置，只读字段</li>
 * </ul>
 * 
 * <p>与实体类的区别:
 * <ul>
 *   <li>不包含JPA注解（@Entity, @Table, @Column等）</li>
 *   <li>不包含数据库字段映射细节</li>
 *   <li>不包含密码等敏感字段</li>
 *   <li>可用于Jackson序列化，直接返回给前端</li>
 * </ul>
 * 
 * @see com.example.mess.entity.User 对应的实体类
 * @see com.example.mess.service.UserService 包含DTO转换逻辑
 * @since 1.0
 */
public class UserDto {
    
    /**
     * 用户唯一标识。
     * <p>由数据库自动生成（IDENTITY策略），创建时不需要指定。
     * 作为主键用于所有查询、更新、删除操作。
     */
    private Long id;

    /**
     * 用户名，用于登录认证。
     * <p>具有唯一性约束，长度1-50字符。
     * 在用户注册时需要检查唯一性（通过existsByUsername）。
     */
    private String username;

    /**
     * 电子邮箱，用于通信通知。
     * <p>具有唯一性约束，长度1-100字符。
     * 在用户注册时需要检查唯一性（通过existsByEmail）。
     */
    private String email;

    /**
     * 用户真实姓名，用于显示。
     * <p>无唯一性约束，长度1-100字符。
     * 可选字段，允许为空。
     */
    private String name;

    /**
     * 用户创建时间。
     * <p>由数据库自动设置为CURRENT_TIMESTAMP，只读字段。
     * 创建用户时不需要指定，更新时不可修改。
     */
    private LocalDateTime createdAt;

    /** 获取用户ID。 @return 用户唯一标识，新建用户序列化输入时可为null */
    public Long getId() {
        return id;
    }

    /** 设置用户ID，创建请求中通常留空,由服务端在响应中回填。 @param id 用户唯一标识 */
    public void setId(Long id) {
        this.id = id;
    }

    /** 获取用户名。 @return 登录用用户名 */
    public String getUsername() {
        return username;
    }

    /** 设置用户名,须唯一且长度控制在1-50字符,创建/更新时为必填项。 @param username 用户名 */
    public void setUsername(String username) {
        this.username = username;
    }

    /** 获取电子邮箱。 @return 邮箱地址 */
    public String getEmail() {
        return email;
    }

    /** 设置电子邮箱,须唯一且符合邮箱格式,长度1-100字符。 @param email 邮箱地址 */
    public void setEmail(String email) {
        this.email = email;
    }

    /** 获取用户真实姓名。 @return 姓名，可能为null */
    public String getName() {
        return name;
    }

    /** 设置用户真实姓名,为可选展示字段,允许为空。 @param name 姓名 */
    public void setName(String name) {
        this.name = name;
    }

    /** 获取创建时间。 @return 用户创建时间 */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /** 设置创建时间,该字段为只读,客户端传入的值会被服务端忽略。 @param createdAt 创建时间 */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

        /*
         * =========================================================================
         * 【数据库与SQL优化】DTO 投影与查询对象的 SQL 优化路径（非可执行代码）
         * =========================================================================
         *
         * 一、DTO 投影的 SQL 收益（读路径优化核心手法）
         *   1) 全实体查询：select * -> 加载所有列 -> 实体对象 + Hibernate 快照开销
         *   2) DTO 投影查询：只 select 需要的列 -> 减少网络传输、内存分配、序列化大小
         *   3) Spring Data JPA 三种投影方式：
         *      a) 基于接口：interface UserView { String getUsername(); Long getId(); }
         *         findBy(...) 返回 List<UserView>，只查投影列
         *      b) 基于类：@Query("select new com.example.UserDto(u.id, u.username) from User u")
         *         JPQL 构造器表达式，直接生成 DTO
         *      c) native SQL：@Query(value="select id, username from USER", nativeQuery=true)
         *         性能最优但失去 JPQL 抽象与跨方言能力
         *   4) 选择策略：默认接口投影（简洁），复杂查询/聚合用构造器表达式，
         *      特殊 DB 函数优化时才用 native
    
         * 二、本类作为"读模型"的语义定位
         *   1) 本项目当前 convertToDto（手工转换）发生在 service 层——
         *      这意味着 DB 查询仍 select * 全字段，投影 0 收益
         *   2) 优化路径（演进方向）：
         *      a) 列表页只需 id/username/createdAt -> 换接口投影
         *      b) 详情页需要全字段 -> 保持 convertToDto
         *   3) 接口投影 vs 手工转换的权衡：
         *      接口投影：省 SQL/内存，但调用方拿到动态代理对象（不能直接序列化为 Jackson 问题）
         *      手工转换：多一次对象复制，但稳定可测试、易扩展
         *   4) 决策时机：列表场景 QPS > 1000 或表字段 > 30 列时，接口投影收益显著
    
         * 三、分页 DTO 的查询成本再优化
         *   1) Page<UserDto>：SELECT * LIMIT + COUNT(*) 两条 SQL
         *   2) Slice<UserDto>：只 SELECT * LIMIT + hasNext 判断（count 免了）
         *      无限滚动 UI 优先 Slice
         *   3) countProjection：复杂查询时手写 count 优化版
         *      @Query(countQuery = "select count(u) from User u where u.status = ?1")
         *   4) 缓存叠加：分页结果缓存 TTL 60s，
         *      同 page/size 请求直接跳过 DB——读多写少列表场景收益最大
         *   5) 一致性代价：写后短期读到旧数据（见 HTTP 块），做一致性 SLA 声明
         =========================================================================
         */
}