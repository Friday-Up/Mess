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
     * 【Java 并发】自定义异常的并发属性：栈快照、构造开销与跨线程传播（非可执行代码）
     * =========================================================================
     *
     * 一、Throwable 对象本身的并发安全性
     *   1) 构造时通过 fillInStackTrace 抓取栈快照写入 detailMessage 与
     *      backtrace 数组，之后只读 -> 异常对象可跨线程安全传递与打印
     *   2) 唯一的可变状态：cause 字段通过 initCause 设置一次（之后抛
     *      IllegalStateException），所以"构造完不要 initCause"是约定
     *   3) 本类两构造器：单参 message、双参 message+cause 均安全；
     *      无参默认消息构造在抛出线程执行，也是构造即定值
     *
     * 二、栈快照的线程语义
     *   1) 快照来自"调用 fillInStackTrace 的线程"，而非"最初 new Throwable
     *      的业务线程"——绝大多数场景两者相同
     *   2) 跨线程传播后，日志看栈回溯到的是抛出点，不是接收点：
     *      对异步排错非常关键，不要被"接收线程的栈"误导
     *   3) 栈是"线程当时的快照"：后续方法进出不会更新已抓取的栈
     *   4) 性能视角：fillInStackTrace 是 synchronized native 方法，
     *      高频抛同一异常会成为隐形热点；可子类覆盖它返回 this 省栈
     *      （代价：丢失排错信息，适用于确定性极强的控制流异常）
     *
     * 三、异常与线程的"中断标记"交互
     *   1) InterruptedException 的语义：线程在阻塞期间被告知中断
     *      - 收到后应尽快结束任务或重新设置中断标记
     *      - 不处理会破坏上层停止逻辑
     *   2) 本类不抛中断异常，但业务里调用线程池 shutdownNow / future.cancel
     *      会让任务在 sleep/queue.take 时收到 InterruptedException
     *   3) 正确模板：
     *      try { Thread.sleep(1000); } catch (InterruptedException e) {
     *         Thread.currentThread().interrupt(); // 恢复中断标记供上级判断
     *         throw new ResourceNotFoundException(任务被中断, e);
     *      }
     *   4) 优雅停机场景：Tomcat 关闭时工作线程可能在 IO 阻塞上，
     *      Spring 的 Lifecycle 机制会给超时 -> 强制退出前尽量写好响应
     *
     * 四、异常链与堆栈深度的连带影响
     *   1) cause 链：打印时会完整输出整条链，深度过深（套娃异常）会让
     *      日志膨胀 -> 最多保留两层语义异常，底层用 root cause
     *   2) 线程栈过深（如递归查询、深层代理包装）打印代价大，
     *      监控型场景建议截断前 N 帧（logback %ex{5} 只打 5 帧）
     *   3) 本异常被 GlobalExceptionHandler 转 404 响应，不在响应体
     *      里带栈 -> 安全也简洁；JSON 明文栈只用于内部日志
     *
     * 五、并发异常设计的两条结语
     *   1) 异常对象"构造即快照"，跨线程传递安全，但理解栈的原生线程
     *      能避免排错方向走偏
     *   2) 同步代码用异常表达业务分支可以，但高频路径内频繁抛异常
     *      会与 synchronized/IO 一起成为并发热点 ->
     *      "可预期的业务不快"（404 查询不存在）应考虑前置缓存或布隆
     *      校验减量，而非让异常当业务结果用
     * =========================================================================
     */
}