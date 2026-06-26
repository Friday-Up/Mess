package com.example.mess.exception;

/**
 * 资源未找到异常 - 当请求的资源不存在时抛出
 * 继承RuntimeException，由GlobalExceptionHandler统一处理返回404
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ResourceNotFoundException() {
        super("请求的资源不存在");
    }
}