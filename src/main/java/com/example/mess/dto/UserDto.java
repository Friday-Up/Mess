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
     * 【DDD 上下文映射】DTO 作为防腐层（ACL）（非可执行代码）
     * =========================================================================
     *
     * 一、限界上下文（Bounded Context）
     *   定义：一个业务模型的适用边界，同一术语在不同上下文有不同含义
     *   示例：
     *   - 用户上下文：User = 注册用户（含密码/角色/登录态）
     *   - 订单上下文：User = 下单人（只需 id/昵称/收货地址）
     *   - 营销上下文：User = 营销对象（只需 id/标签/手机号）
     *   同一个"用户"在不同上下文中的模型不同，不应共享同一个 User 类
     *
     * 二、上下文映射（Context Mapping）模式
     *   1) 防腐层（Anti-Corruption Layer，ACL）：
     *      下游上下文不直接使用上游模型，通过翻译层转换
     *      DTO 就是防腐层的实现：上游 User -> 下游 OrderUserDto
     *      作用：上游模型变更不影响下游业务逻辑
     *   2) 共享内核（Shared Kernel）：
     *      两个上下文共享一部分模型（如共用的值对象 Money）
     *      风险：共享部分变更影响双方，需协商
     *   3) 客户-供应商（Customer-Supplier）：
     *      上游优先，下游适配（如订单上下文适配用户上下文的 API）
     *   4) 遵奉者（Conformist）：
     *      下游无条件遵从上游模型（上游强势，无法协商）
     *   5) 开放主机服务（Open Host Service）：
     *      上游提供标准化 API/协议（如 RESTful + OpenAPI 规范）
     *   6) 发布语言（Published Language）：
     *      上游发布标准化的数据格式（如 JSON Schema/Avro/Protobuf）
     *
     * 三、本类在上下文映射中的角色
     *   UserDto 是用户上下文对外发布的"发布语言"：
     *   1) 对外契约：前端/API 消费者依赖 UserDto 的结构，不依赖 User 实体
     *   2) 字段裁剪：只暴露用户上下文愿意发布的字段
     *   3) 版本演进：UserDto 加字段向后兼容，删字段需版本化（v2）
     *   4) 微服务拆分后：订单服务通过用户服务的 API 获取 UserDto，
     *      订单服务内部有自己的 OrderUserDto（防腐层翻译）
     *
     * 四、CQRS（命令查询职责分离）
     *   命令（写）和查询（读）使用不同的模型：
     *   - 命令模型：User 实体（含业务规则/校验/状态变更）
     *   - 查询模型：UserDto/UserView（只读，可从读库/缓存/物化视图查询）
     *   - 命令走 Service -> Repository -> 写库
     *   - 查询可直接走查询服务 -> 读库/ES/缓存，不经过聚合根
     *   - 本项目当前读写共用 User 实体，数据量大后可引入 CQRS
     * =========================================================================
     */
}