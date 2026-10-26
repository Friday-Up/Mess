package com.example.mess.exception;

import com.example.mess.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器 - 使用@RestControllerAdvice统一捕获Controller层异常。
 * 
 * <p>设计目标:
 * <ul>
 *   <li>统一异常处理: 避免在每个Controller中重复编写try-catch</li>
 *   <li>标准化响应格式: 所有异常返回ApiResponse格式，确保前端解析一致</li>
 *   <li>安全防护: 不向客户端暴露具体异常堆栈信息，防止信息泄露</li>
 *   <li>日志记录: 记录异常详情，便于问题排查和监控</li>
 * </ul>
 * 
 * <p>异常处理策略:
 * <table border="1">
 *   <tr><th>异常类型</th><th>HTTP状态码</th><th>响应消息</th><th>日志级别</th></tr>
 *   <tr><td>ResourceNotFoundException</td><td>404 Not Found</td><td>异常消息原文</td><td>ERROR</td></tr>
 *   <tr><td>其他Exception</td><td>500 Internal Server Error</td><td>"系统内部错误"</td><td>ERROR（含堆栈）</td></tr>
 * </table>
 * 
 * <p>扩展指南:
 * <ul>
 *   <li>添加新的@ExceptionHandler处理特定业务异常（如ValidationException → 400）</li>
 *   <li>添加@ExceptionHandler(MethodArgumentNotValidException.class)处理参数校验失败</li>
 *   <li>添加@ExceptionHandler(AccessDeniedException.class)处理权限不足 → 403</li>
 *   <li>添加@ExceptionHandler(HttpMessageNotReadableException.class)处理请求体格式错误</li>
 * </ul>
 * 
 * @see com.example.mess.exception.ResourceNotFoundException 资源未找到异常
 * @see com.example.mess.dto.ApiResponse 统一响应对象
 * @since 1.0
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理资源未找到异常。
     * <p>当请求的资源（如用户、订单等）不存在时，返回404 Not Found状态码。
     * 直接使用异常消息作为响应内容，便于前端展示具体错误信息。
     * 
     * @param ex 资源未找到异常，包含具体的错误消息
     * @return 包含错误信息的404响应
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        // 记录ERROR级日志（资源未找到属于可预期的业务异常，无需打印堆栈）
        log.error("资源未找到: {}", ex.getMessage());
        // 用异常消息构建错误响应，便于前端展示具体的未找到原因
        ApiResponse<Void> response = ApiResponse.error(ex.getMessage());
        // 返回HTTP 404状态码及错误响应体
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * 处理通用异常（兜底处理）。
     * <p>捕获所有未被特定处理器处理的异常，返回500 Internal Server Error。
     * 不向客户端暴露具体异常信息，只返回"系统内部错误"，
     * 但会在日志中记录完整的异常堆栈，便于开发人员排查。
     * 
     * @param ex 未处理的异常
     * @return 包含通用错误消息的500响应
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        // 记录完整异常堆栈（第三个参数ex），便于开发人员定位未预期的系统错误
        log.error("系统异常: {}", ex.getMessage(), ex);
        // 对外只返回笼统的"系统内部错误"，不暴露堆栈细节，防止敏感信息泄露
        ApiResponse<Void> response = ApiResponse.error("系统内部错误");
        // 返回HTTP 500状态码及错误响应体
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

        /*
         * =========================================================================
         * 【数据库与SQL优化】DB 异常的统一处理与状态码映射（非可执行代码）
         * =========================================================================
         *
         * 一、Spring 包装的 DataAccessException 体系
         *   1) JDBC 的 SQLException 是受检异常，Spring 翻译为非受检体系：
         *      DuplicateKeyException extends DataIntegrityViolationException
         *      QueryTimeoutException / PessimisticLockingFailureException
         *      均继承 DataAccessException，全局处理器按体系分支处理
         *   2) 翻译机制：SQLErrorCodeSQLExceptionTranslator 按错误码映射
         *      MySQL 1062（重复键）-> DuplicateKeyException
         *   3) 好处：业务代码不 try SQLException，靠类型分支而非错误码字符串
         *
         * 二、DB 异常 -> HTTP 状态码映射表（errorCode 是契约）
         *   1) DuplicateKeyException -> 409 + DB_DUPLICATE_KEY
         *      场景：并发创建同 username，应用层检查通过但唯一约束拒绝
         *   2) QueryTimeoutException -> 504 + DB_TIMEOUT
         *      场景：JDBC socketTimeout 触发，DB 可能还在跑——重试易放大，需退避
         *   3) CannotGetJdbcConnectionException -> 503 + DB_CONNECTION_EXHAUSTED
         *      场景：Hikari 连接耗时尽，DB 不可达或满负荷，客户端立即降级
         *   4) OptimisticLockingFailureException -> 409 + DB_VERSION_CONFLICT
         *      场景：@Version 冲突，提示"数据已被他人修改，请刷新后重试"
         *   5) TransactionSystemException -> 500（commit 失败，罕见但严重）
         *
         * 三、处理 DB 异常的三条纪律
         *   1) 不把 SQLException/约束名（uk_user_username）回给前端：
         *      约束名是实现，错误码是契约，一层翻译不可省
         *   2) 记日志带 SQL 标识：错误码 + 表名 + 耗时，但不打参数值（防泄漏）
         *   3) 504/503 是容量信号：触发后应联动限流告警，而不是只返回错误
         =========================================================================
         */
}