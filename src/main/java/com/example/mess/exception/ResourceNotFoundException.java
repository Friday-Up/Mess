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
     * 【DDD 领域事件】异常即事件的设计思路（非可执行代码）
     * =========================================================================
     *
     * 一、领域事件 vs 异常
     *   异常：同步的、局部的、调用方必须处理
     *   事件：异步的、广播的、订阅方按需处理
     *   两者可以互补：
     *   - 同步路径用异常：资源不存在 -> 抛 ResourceNotFoundException -> 404
     *   - 异步路径用事件：资源不存在 -> 发布 ResourceNotFoundEvent -> 审计/监控
     *
     * 二、事件驱动架构（EDA）
     *   核心思想：服务间通过事件解耦，不直接调用
     *   对比：
     *   请求-响应（同步）：订单服务调用户服务 GET /api/users/1
     *     耦合：订单服务依赖用户服务可用性
     *   事件驱动（异步）：用户服务发布 UserCreated 事件到 Kafka，
     *     订单服务订阅事件维护本地用户缓存
     *     解耦：用户服务不关心谁消费，订单服务不依赖用户服务可用性
     *
     * 三、事件分类
     *   1) 领域事件（Domain Event）：业务上有意义的事实
     *      UserCreated / UserEmailChanged / UserDeleted
     *      特征：过去时态、不可变、包含关键业务数据
     *   2) 集成事件（Integration Event）：跨服务通信的事件
     *      与领域事件的区别：集成事件是领域事件的对外发布格式
     *      领域事件：UserCreated(userId, username, timestamp) - 内部
     *      集成事件：UserCreatedEvent(userId, username, email, timestamp, eventId) - 对外
     *      集成事件需含 eventId（幂等去重）、eventTime（排序）、schema 版本
     *   3) 通知事件（Notification Event）：只告知"发生了"，不含数据
     *      UserChanged(userId) - 消费方需回调查询最新数据
     *      优点：事件小；缺点：消费方需反查
     *
     * 四、事件溯源（Event Sourcing）
     *   传统：状态 = 当前数据库值（只存最新状态）
     *   事件溯源：状态 = 事件序列的回放结果（存所有事件）
     *   示例：
     *   传统：User(username="alice", email="new@x.com") - 只存最新
     *   事件溯源：
     *     UserCreated(id=1, username="alice", email="old@x.com")
     *     UserEmailChanged(id=1, email="new@x.com")
     *     -> 回放两个事件得到当前状态
     *   优点：完整审计日志、时间旅行（任意时刻状态）、天然事件驱动
     *   缺点：查询复杂（需 CQRS 读模型）、事件模式演进困难、存储量大
     *   适用：金融交易、审计要求高的场景
     *   实现：Axon Framework / 自建（事件表 + 快照）
     *
     * 五、发件箱模式（Transactional Outbox）
     *   问题：数据库提交成功但消息发送失败（或反之），数据与消息不一致
     *   方案：业务数据和事件写入同一数据库事务
     *   1) 业务操作：UPDATE user SET email='new@x.com'
     *   2) 写发件箱：INSERT INTO outbox(event_type, payload, status) VALUES(...)
     *   3) 后台任务：轮询 outbox 表，发送到 Kafka，标记已发送
     *   4) 保证：业务成功 <=> 事件一定发出（同一事务）
     *   进阶：Debezium CDC 监听 outbox 表 binlog，实时发送到 Kafka
     * =========================================================================
     */
}