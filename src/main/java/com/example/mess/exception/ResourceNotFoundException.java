package com.example.mess.exception;

/**
 * 资源未找到异常
 * 当请求的资源（如用户、文件等）不存在时抛出此异常
 * 
 * 继承RuntimeException，属于非受检异常
 * 不需要在方法签名中声明，调用方可以选择捕获或继续向上传播
 * 
 * 设计目的:
 * - 提供语义化的异常名称，便于理解
 * - 统一资源不存在时的错误处理
 * - 支持自定义错误消息
 * 
 * 使用场景:
 * - 根据ID查询用户，但用户不存在
 * - 根据用户名查询用户，但用户不存在
 * - 访问不存在的文件或资源
 * 
 * 与标准异常的区别:
 * - 比IllegalArgumentException更具体
 * - 比NoSuchElementException更具业务含义
 * - 便于统一异常处理
 * 
 * 作者: zhangyaolong.5
 * 创建时间: 2026-05-26
 * 
 * 示例:
 * throw new ResourceNotFoundException("用户不存在，ID: " + userId);
 * throw new ResourceNotFoundException("文件未找到: " + filename);
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * 构造函数
     * 
     * @param message 详细的错误消息
     * 
     * 消息格式建议:
     * - 明确指出资源类型
     * - 包含查找条件
     * - 提供友好的用户提示
     * 
     * 示例:
     * "用户不存在，ID: 123"
     * "文件未找到: document.pdf"
     * "订单不存在，订单号: ORDER_20230001"
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * 带原因的构造函数
     * 
     * @param message 错误消息
     * @param cause 原始异常
     * 
     * 使用场景:
     * - 包装底层异常
     * - 保留完整的异常链
     * - 调试和日志记录
     * 
     * 示例:
     * try {
     *     user = userRepository.findById(id);
     * } catch (Exception e) {
     *     throw new ResourceNotFoundException("查询用户失败，ID: " + id, e);
     * }
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 默认构造函数
     * 提供默认的错误消息
     * 
     * 注意:
     * - 建议使用带消息参数的构造函数
     * - 默认消息可能不够具体
     */
    public ResourceNotFoundException() {
        super("请求的资源不存在");
    }
}