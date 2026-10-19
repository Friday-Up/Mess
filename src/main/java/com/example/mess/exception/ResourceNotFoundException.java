package com.example.mess.exception;

/**
 * 资源未找到异常 - 当请求的资源（如用户、订单等）不存在时抛出。
 * 
 * <p>设计说明:
 * <ul>
 *   <li>继承RuntimeException（非受检异常），不需要在方法签名中声明throws</li>
 *   <li>由GlobalExceptionHandler统一处理，自动转换为404 Not Found响应</li>
 *   <li>提供多个构造函数，支持不同的使用场景</li>
 * </ul>
 * 
 * <p>使用场景:
 * <ul>
 *   <li>用户查询: findById返回空时抛出 new ResourceNotFoundException("用户不存在")</li>
 *   <li>删除操作: 用户不存在时抛出 new ResourceNotFoundException("用户不存在")</li>
 *   <li>更新操作: 用户不存在时抛出 new ResourceNotFoundException("用户不存在")</li>
 * </ul>
 * 
 * <p>与Spring标准异常的关系:
 * <ul>
 *   <li>不同于NoSuchElementException（集合操作）</li>
 *   <li>不同于EntityNotFoundException（JPA标准异常）</li>
 *   <li>自定义异常更语义化，便于统一处理</li>
 * </ul>
 * 
 * @see com.example.mess.exception.GlobalExceptionHandler 全局异常处理器
 * @since 1.0
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * 使用自定义消息创建异常。
     * <p>最常用的构造函数，直接传入错误描述信息。
     * 
     * @param message 错误描述信息，如"用户不存在"、"订单未找到"
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * 使用自定义消息和原始异常创建异常。
     * <p>用于包装底层异常，保留异常链以便调试。
     * 
     * @param message 错误描述信息
     * @param cause 原始异常（如SQLException、NoResultException等）
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 使用默认消息创建异常。
     * <p>默认消息为"请求的资源不存在"，适用于不需要具体描述的场景。
     */
    public ResourceNotFoundException() {
        super("请求的资源不存在");
    }

    /*
     * =========================================================================
     * 【异常安全设计】资源不存在 vs 越权的安全语义（非可执行代码）
     * =========================================================================
     *
     * 一、404 vs 403 的安全权衡
     *   场景：用户 A 请求用户 B 的数据（/api/users/456）
     *   方案 A：返回 403 Forbidden
     *     语义：资源存在但你无权访问
     *     风险：泄露资源存在性，攻击者可枚举 ID 探测哪些 ID 有效
     *   方案 B：返回 404 Not Found
     *     语义：资源不存在（对无权用户而言，等同于不存在）
     *     安全：不泄露存在性，攻击者无法区分"不存在"和"无权"
     *   建议：敏感资源对无权用户返回 404；管理后台可返回 403（内部系统）
     *
     * 二、枚举攻击防护
     *   场景：攻击者遍历 /api/users/1 到 /api/users/10000
     *   防护：
     *   1) 统一响应：存在/无权都返回 404，消除差异
     *   2) 速率限制：单 IP 请求频率限制，枚举行为被限流
     *   3) ID 混淆：自增 ID 换 UUID/雪花 ID，无法顺序枚举
     *   4) 响应时间一致：存在/不存在的查询耗时一致，
     *      避免时间侧信道（存在时查库快，不存在时快返回，时间差异可探测）
     *
     * 三、异常 message 的安全边界
     *   安全的 message：
     *     "请求的资源不存在"（通用，不泄露细节）
     *     "用户名或密码错误"（不区分哪个错，防枚举）
     *   不安全的 message：
     *     "用户 ID=456 不存在"（泄露 ID 有效性）
     *     "邮箱 zhang@x.com 未注册"（泄露注册状态）
     *     "密码错误"（确认了用户名有效）
     *   原则：对未认证用户，响应不区分"不存在"和"无权"和"凭证错误"
     *
     * 四、异常与审计日志
     *   1) 404 高频：可能是枚举攻击，记安全日志 + 告警
     *   2) 403 高频：可能是越权尝试，记安全日志 + 告警
     *   3) 401 高频：暴力破解，触发锁定 + 告警
     *   4) 审计字段：时间/用户/IP/UA/目标资源/异常类型
     * =========================================================================
     */
}