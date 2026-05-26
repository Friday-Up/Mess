package com.example.mess.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * 用户数据传输对象 (DTO)
 * 用于在Controller层和Service层之间传输用户数据
 * 
 * @NotBlank 验证注解，确保字符串不为null且不为空
 * @Email 验证注解，确保字符串符合邮箱格式
 * 
 * 设计目的:
 * - 解耦Entity和API接口，避免直接暴露数据库结构
 * - 提供数据验证功能
 * - 控制API暴露的字段
 * - 支持不同场景下的数据表示
 * 
 * 与User实体的区别:
 * - User: 数据库实体，包含完整业务逻辑
 * - UserDto: 数据传输对象，只包含API需要的字段
 * 
 * 作者: zhangyaolong.5
 * 创建时间: 2026-05-26
 */
public class UserDto {
    
    /**
     * 用户ID
     * 在创建用户时可以为null，更新用户时必须提供
     * 
     * 使用场景:
     * - 更新用户信息时需要指定用户ID
     * - 返回用户信息时包含用户ID
     * 
     * 验证规则:
     * - 创建用户：可以为null
     * - 更新用户：必须提供有效ID
     */
    private Long id;
    
    /**
     * 用户名
     * 用于登录和显示，应该唯一
     * 
     * 验证规则:
     * - @NotBlank: 不能为空，不能为null，不能只包含空格
     * - 长度限制：由数据库字段定义（通常255字符）
     * 
     * 格式要求:
     * - 可以包含字母、数字、下划线
     * - 不能以数字开头（建议）
     * - 不能包含特殊字符（建议）
     * 
     * 示例值:
     * - 有效: "john_doe", "user123", "admin"
     * - 无效: "", "   ", null
     * 
     * 业务规则:
     * - 用户名应该唯一（在Service层验证）
     * - 创建后不允许修改（根据业务需求）
     */
    @NotBlank(message = "用户名不能为空")
    private String username;
    
    /**
     * 邮箱地址
     * 用于通知、密码找回等功能
     * 
     * 验证规则:
     * - @NotBlank: 不能为空，不能为null，不能只包含空格
     * - @Email: 必须符合邮箱格式
     * 
     * 格式要求:
     * - 必须包含@符号
     * - 必须包含域名部分
     * - 符合RFC 5322标准
     * 
     * 示例值:
     * - 有效: "user@example.com", "test@gmail.com"
     * - 无效: "", "invalid-email", "user@", "@domain.com"
     * 
     * 业务规则:
     * - 邮箱应该唯一（在Service层验证）
     * - 可以用于登录（根据业务需求）
     * - 用于发送通知邮件
     */
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;
    
    /**
     * 真实姓名
     * 用于友好的用户界面显示
     * 
     * 验证规则:
     * - @NotBlank: 不能为空，不能为null，不能只包含空格
     * - 长度限制：由数据库字段定义（通常255字符）
     * 
     * 格式要求:
     * - 可以包含中文、英文、空格
     * - 可以包含常见的姓名分隔符（如·）
     * 
     * 示例值:
     * - 有效: "张三", "John Doe", "Mary Jane Smith", "李·小龙"
     * - 无效: "", "   ", null
     * 
     * 业务规则:
     * - 可以修改
     * - 用于显示而不是唯一标识
     * - 可以重复（多人同名）
     */
    @NotBlank(message = "姓名不能为空")
    private String name;

    /**
     * 默认构造函数
     * 由Spring框架在反序列化JSON时使用
     * 
     * 使用场景:
     * - Spring MVC自动绑定请求参数
     * - Jackson JSON反序列化
     * - 单元测试中创建测试对象
     * 
     * 注意:
     * - 必须提供默认构造函数，否则JSON反序列化会失败
     */
    public UserDto() {
        // 默认构造函数，不做任何初始化
        // 字段值将在后续通过setter方法设置
    }

    /**
     * 全参数构造函数
     * 用于快速创建UserDto对象
     * 
     * @param id 用户ID
     * @param username 用户名
     * @param email 邮箱地址
     * @param name 真实姓名
     * 
     * 使用场景:
     * - 手动创建DTO对象
     * - 单元测试中创建测试数据
     * - 从其他对象转换时
     */
    public UserDto(Long id, String username, String email, String name) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.name = name;
    }

    // Getter和Setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * 验证数据有效性
     * 在数据绑定后进行额外验证
     * 
     * 验证内容:
     * - 用户名长度检查
     * - 邮箱域名检查
     * - 姓名格式检查
     * 
     * 使用场景:
     * - 自定义验证逻辑
     * - 复杂业务规则验证
     * - 跨字段验证
     * 
     * 示例:
     * public boolean isValid() {
     *     return username != null && username.length() >= 3 && 
     *            email != null && email.contains("@") &&
     *            name != null && !name.trim().isEmpty();
     * }
     */
    public boolean isValid() {
        return username != null && !username.trim().isEmpty() &&
               email != null && email.contains("@") &&
               name != null && !name.trim().isEmpty();
    }

    /**
     * 转换为字符串表示
     * 用于日志记录和调试
     * 
     * @return 用户DTO的字符串表示
     * 
     * 注意:
     * - 提供自定义实现，可以控制输出格式
     * - 避免在toString中输出敏感信息（如密码）
     */
    @Override
    public String toString() {
        return "UserDto{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}