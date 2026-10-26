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
     * 【HTTP/REST】资源不存在异常的 REST 契约表达（非可执行代码）
     * =========================================================================
     *
     * 一、"404"的三种业务含义与区分
     *   1) 路由不存在：客户端请求了 /api/userx（拼错路径）
     *      框架级 404，由 Spring 默认 NoResourceFoundException 处理
     *      建议：包体也走 ApiResponse(success=false, errorCode=ROUTE_NOT_FOUND)
     *   2) 资源不存在：客户端请求 /api/users/999（路径拼写正确但 ID 无对应记录）
     *      业务级 404，由本异常表达——这正是 GlobalExceptionHandler 映射的
     *   3) 软删除资源：曾存在但已标记 deleted -> 视业务策略 404 或 410 Gone
     *   4) 三者 HTTP 状态同为 404 但业务语义不同：
     *      前端用 errorCode 字段分支（"提示检查路径"vs"用户不存在"）
     *      监控用 errorCode 分桶统计（真 404 与接口误用分开看）

     * 二、本异常的 REST 契约要素
     *   1) 状态码：404 Not Found（由全局处理器设置）✓
     *   2) 包体：ApiResponse(success=false, message="用户不存在", data=null) ✓
     *   3) 建议补充：errorCode 字段（"USER_NOT_FOUND"）——机器可读
     *   4) 建议补充：message 中不包含敏感数据（"ID 999 不存在"可以，
     *      "用户张三的手机 138... 不存在"不行——日志与响应同标准脱敏）
     *   5) 不建议：响应中暴露内部 SQL/堆栈/类名——攻击面与信息泄漏

     * 三、404 与其他状态码的边界判定（避免语义混淆）
     *   1) 用户存在但当前用户无权看 -> 403 Forbidden
     *      （ vs 404 的隐私策略选择：对未授权用户"装作不存在"返 404，
     *        防止探测资源存在性——隐私敏感 API 常用此策略）
     *   2) 用户存在但被禁用 -> 403 或自定义 422 Unprocessable Entity
     *   3) 用户存在但请求字段不支持 -> 404 子资源不存在
     *      （如 /api/users/1/avatar 还未上传：404 而非 200+null）
     *   4) 请求方法被禁（GET 不允许）-> 405 Method Not Allowed
     *   5) 请求路径格式不合法（/api/users/abc，id 非数字）-> 400 Bad Request

     * 四、幂等性与 404 的兼容
     *   1) DELETE 幂等语义：删除已不存在的资源，仍应返 204 或 200
     *      不应返 404——"幂等"要求重复执行结果与一次相同（删除已删 = 成功）
     *   2) 本项目 delete 的设计要点：删除前先查，不存在抛本异常 -> 404
     *      ——严格说这破坏了 DELETE 的幂等语义；
     *      两种处理方案：
     *      A) 严格 REST：delete 时不预查，直接 deleteById（JPA deleteById
     *         对不存在 ID 抛 EmptyResultDataAccessException），catch 后返 204
     *      B) 务实派：保持 404（前端显示"已被删除"提示，业务可接受）
     *   3) GET 幂等：同一 ID 反复 GET 也返 404——一致，OK

     * 五、错误响应的演进兼容
     *   1) 错误码引入策略：
     *      初版：仅 message（当前）
     *      升级：errorCode 字段加入包体，旧客户端忽略（向后兼容）
     *      老化：文档废弃 message 做分支判断，errorCode 才是契约
     *   2) 错误码命名规范：
     *      资源_动作_原因：USER_NOT_FOUND / USER_DUPLICATE_USERNAME /
     *      USER_VERSION_CONFLICT——大写下划线，全局唯一注册表
     *   3) 错误码与 HTTP 状态码并存：状态码表达大类（4xx/5xx），
     *      errorCode 表达细分（具体业务原因）——双轨互补
     *   4) 错误响应的版本化：errorCode 一旦发布不可复用，
     *      含义微调 = 新码（旧码保留映射），避免破坏客户端
     * =========================================================================
     */
}