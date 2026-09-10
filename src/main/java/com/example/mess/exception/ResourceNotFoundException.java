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
}

/*
 * ============================================================================
 * 【设计文档】ResourceNotFoundException 自定义异常设计说明（补充文档，非可执行代码）
 * ============================================================================
 *
 * 一、为什么自定义而非复用标准异常
 * ----------------------------------------------------------------------------
 * 相比 NoSuchElementException（集合语义）或 JPA 的 EntityNotFoundException，
 * 自定义 ResourceNotFoundException 语义更贴合"业务资源不存在"，
 * 便于 GlobalExceptionHandler 精准识别并映射为 HTTP 404，也让业务代码自解释。
 *
 * 二、为什么继承 RuntimeException
 * ----------------------------------------------------------------------------
 *   - 非受检异常无需在方法签名声明 throws，避免污染业务方法签名;
 *   - 契合 Spring 一贯的"运行时异常 + 全局兜底"风格;
 *   - 由 @RestControllerAdvice 统一捕获，无需逐层手动传递。
 *
 * 三、三个构造函数的适用场景
 * ----------------------------------------------------------------------------
 *   (String message)                 —— 最常用，给出具体资源描述;
 *   (String message, Throwable cause) —— 需要保留底层异常链以便排障;
 *   ()                                —— 使用默认消息"请求的资源不存在"。
 *
 * 四、典型使用与流转
 * ----------------------------------------------------------------------------
 *   UserService.getUserById():
 *     return repo.findById(id)
 *                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
 *   ↓ 抛出后由 GlobalExceptionHandler 捕获
 *   ↓ 转换为 ApiResponse.error("用户不存在") + HTTP 404
 *
 * 五、扩展指南
 * ----------------------------------------------------------------------------
 *   - 需要携带资源类型/ID：可增加字段（如 resourceName、fieldName、fieldValue）
 *     并重写消息格式，便于前端做更精细的错误提示;
 *   - 需要错误码：可引入统一错误码枚举并在异常中携带，供响应体使用。
 * ============================================================================
 */