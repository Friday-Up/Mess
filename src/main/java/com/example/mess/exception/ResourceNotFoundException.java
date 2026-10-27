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
         * 【可观测性与生产运维】业务异常的观测语义与噪声治理（非可执行代码）
         * =========================================================================
         *
         * 一、"查不到"是业务分支，不应污染错误告警
         *   1) 404 在本系统是正常业务结果（用户不存在），
         *      全局处理为 ERROR 会把正常业务流量变成"告警噪音"——
         *      治理原则：业务异常记 WARN/INFO + 计数，不进 ERROR 告警通道
         *   2) 但高频 404 是真实信号：脚本扫描不存在的 id、上游缓存失效风暴、
         *      前端 bug 传错参数——"404 突增"本身该有独立告警，阈值按业务正常水位设
         *   3) 日志要带"查了什么"：resourceType + resourceId + traceId，
         *      光记"ResourceNotFoundException"无法定位是哪个资源查不到
         *
         * 二、异常分类的观测价值
         *   1) 异常类名就是天然指标标签：ResourceNotFound / Conflict / Validation ——
         *      靠类继承体系而非字符串区分，监控维度随类型增长自动扩展
         *   2) 语义分层示例：用户主动删后再查=正常 404；
         *      上游刚写完立刻查不到=数据不一致风险——同一异常两种观测结论，
         *      需要调用方在日志里补"场景标签"，类本身无法区分
         *   3) 噪声治理闭环：每月评审一次告警/日志 Top 项，把"不可执行"的删掉——
         *      不做评审，日志系统会膨胀成没人看的垃圾场
         *
         * 三、404 的可观测面板设计
         *   1) 独立面板：404 频率按 resourceType 拆分（用户/订单/商品），
         *      分类型看趋势——聚合的"404 总数"会掩盖单类资源的问题
         *   2) 与上游对账：调用方声称"刚创建的 id 查不到"时，
         *      用 traceId 串联创建请求与查询请求的时间线——
         *      一致性问题要靠跨请求关联观测才能定论
         *   3) 404 的"业务正常水位"要基线化：平时 100/分钟，
         *      突变到 5000/分钟即告警——没有基线，阈值只能拍脑袋
         =========================================================================
         */
}