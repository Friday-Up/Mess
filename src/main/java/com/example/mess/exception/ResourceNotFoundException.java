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
     * ============================================================================
     * 【阅读笔记】自定义异常的设计原则（非可执行代码）
     * ============================================================================
     *
     * 一、Checked vs Unchecked
     * ----------------------------------------------------------------------------
     *   继承 Exception（checked）—— 调用方必须处理，适合"可恢复的业务性失败";
     *   继承 RuntimeException（unchecked）—— 不强制捕获，适合"程序性错误"或
     *       我们希望统一由上层收敛的失败。
     *   本类继承 RuntimeException：让业务方法签名保持干净，由全局异常处理器
     *   统一接收并转 404，是现代 Spring 项目的惯用做法。
     *
     * 二、一个好用的自定义异常通常有三件套
     * ----------------------------------------------------------------------------
     *   1) 无参构造 + 默认文案 —— 快速抛出，语义明确;
     *   2) 带 message 的构造   —— 覆盖"用户 123 不存在"这样的动态信息;
     *   3) (可选) 业务码字段    —— 前端/网关可据 code 做差异化处理。
     *
     * 三、异常语义要"能说人话"
     * ----------------------------------------------------------------------------
     *   推荐：ResourceNotFoundException、DuplicateResourceException;
     *   避免：Biz222Exception、XException——光有 code 没人看得懂;
     *   习惯上 message 也会尽量带上关键上下文，不要只写"出错"。
     *
     * 四、什么时候不要自定义异常
     * ----------------------------------------------------------------------------
     *   - 纯粹的"校验失败"可以复用 IllegalArgumentException;
     *   - 调用别人接口失败，优先用对方的领域异常或标准的 IOException;
     *   - 每个字段/每个返回值都设计异常类——爆炸性增加维护成本，收益低。
     * ============================================================================
     */
}