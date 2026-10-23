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
     * 【设计模式】特化异常与异常/事件双轨制：控制流 vs 广播流（非可执行代码）
     * =========================================================================
     *
     * 一、特化异常（Specialized Exception）：语义即类型
     *   1) 本类继承 RuntimeException，为一类业务事实（资源不存在）
     *      建立专属类型——模式上近似值对象的精神：
     *      让类型携带语义，而非靠 message 字符串约定
     *   2) 三重收益在项目兑现：
     *      a) GlobalExceptionHandler 用 @ExceptionHandler(本类) 精确映射：
     *         404 + ApiResponse(false)，无字符串魔法判断
     *      b) 调用方可按类型分支：catch (ResourceNotFoundException e)
     *         与其他异常清晰区分
     *      c) 监控/日志可按异常类埋点：Micrometer Counter 加 tag
     *         exception=ResourceNotFoundException，趋势可观测
     *   3) 反面警示：每个错误建一个异常类会稀释可读性，
     *      按 HTTP 语义粒度收敛（4xx 业务类 2-3 个足够）
     *
     * 二、异常 vs 事件：同一业务事实的双轨表达
     *   1) 异常：同步、局部、调用方必须处理，表达"走不下去"
     *   2) 事件：广播、可同步可异步、订阅者按需响应，
     *      表达"这事儿发生了，谁关心谁处理"
     *   3) 双轨并行的架构收益（本项目演进方向）：
     *      Service 抛本异常 -> 全局处理器返回 404（主流程，快返回）
     *      Service 同时发布 ResourceNotFoundEvent ->
     *      审计订阅者写访问日志、监控订阅者打点（旁路，不拖主流程）
     *   4) 分工铁律：主流程错误码靠异常决定；
     *      跨模块联动靠事件传递——混用会让调用链难追踪
     *
     * 三、异常构造的工厂方法化（可选收敛）
     *   1) 本类现有三构造器（message / message+cause / 默认）
     *   2) 若调用点高频重复："用户不存在: " + id 散落多处，
     *      可收敛静态工厂：notFound(Long id) 统一文案格式，
     *      与 ApiResponse.success/error 风格呼应
     *   3) 判定：构造点 >= 3 处且文案需统一时引入工厂；
     *      散点各异文案则保留构造器直接调用
     * =========================================================================
     */
}