package com.example.mess.exception;

import com.example.mess.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 * 
 * 使用@RestControllerAdvice统一捕获Controller层异常，返回标准化的ApiResponse格式。
 * - ResourceNotFoundException → 404 Not Found
 * - 其他Exception → 500 Internal Server Error（不暴露具体异常信息）
 * 
 * @see com.example.mess.exception.ResourceNotFoundException
 * @see com.example.mess.dto.ApiResponse
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** 处理资源未找到异常，返回404状态码 */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        log.error("资源未找到: {}", ex.getMessage());
        ApiResponse<Void> response = ApiResponse.error(ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /** 处理通用异常，返回500状态码，不向客户端暴露具体异常信息 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        log.error("系统异常: {}", ex.getMessage(), ex);
        ApiResponse<Void> response = ApiResponse.error("系统内部错误");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}