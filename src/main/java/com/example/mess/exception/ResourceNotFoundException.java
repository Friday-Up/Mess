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
         * 【数据库与SQL优化】"查不到"的 SQL 语义与异常成本（非可执行代码）
         * =========================================================================
         *
         * 一、findById 返回 Optional 的 SQL 视角
         *   1) JPQL 翻译：select u from User u where u.id = ?1
         *      SQL：select 列 from USER where id = ?（主键聚簇索引等值查询）
         *   2) 性能：单行等值查询 O(log N)，主键命中即返回，1 行 RT < 1ms
         *   3) Optional.empty() 的成本：SQL 执行了但 0 行，
         *      与命中 1 行成本几乎相同——"找不到"并不便宜
         *   4) 反模式：在循环里 findById 再判空验证存在性 -> 应改 existsById，
         *      select 1 ... limit 1 更省，且 Hibernate 不实例化实体
         *
         * 二、404 的一致性窗口与穿透防护
         *   1) 查到与返回之间有窗口：findById 命中后行可能被并发删除，
         *     READ_COMMITTED 下最终返回 404 是最终一致——审计场景需 select for update
         *   2) 缓存穿透：高频查询不存在的 id（恶意扫描），每次都打到 DB——
         *      是系统设计问题而非"正常 404"，需布隆过滤器/缓存空值，而非仅打日志
         *   3) 异常驱动控制流的成本：throw + 全局捕获比 Optional 判断贵百倍，
         *      "找不到"是正常业务分支用返回值表达；异常留给"不该发生的事"
         =========================================================================
         */
}