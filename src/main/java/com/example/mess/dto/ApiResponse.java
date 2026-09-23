package com.example.mess.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

/**
 * 统一API响应对象 - 标准化所有API接口的响应格式。
 * 
 * <p>采用泛型设计支持任意类型的数据负载，确保前后端通信的一致性。
 * 通过静态工厂方法（success/error）创建实例，避免直接使用构造函数。
 * 
 * <p>响应结构说明:
 * <ul>
 *   <li><b>success</b> - 请求是否成功，前端根据此字段决定如何处理响应数据</li>
 *   <li><b>message</b> - 响应消息，成功时为"Success"，失败时包含错误描述</li>
 *   <li><b>data</b> - 响应数据，泛型类型，成功时包含业务数据，失败时为null</li>
 *   <li><b>timestamp</b> - 响应时间戳，格式：yyyy-MM-dd'T'HH:mm:ss.SSSSSS</li>
 *   <li><b>path</b> - 请求路径，可选字段，用于日志关联和问题排查</li>
 * </ul>
 * 
 * <p>使用示例:
 * <pre>{@code
 *   // 成功响应（带数据）
 *   ApiResponse<UserDto> response = ApiResponse.success(userDto);
 *   // 成功响应（无数据，如删除操作）
 *   ApiResponse<Void> response = ApiResponse.success();
 *   // 错误响应
 *   ApiResponse<Void> response = ApiResponse.error("用户不存在");
 * }</pre>
 * 
 * <p>设计原则:
 * <ul>
 *   <li>不可变实例：通过私有构造函数和静态工厂方法确保一致性</li>
 *   <li>时间戳自动设置：构造函数中自动设置当前时间</li>
 *   <li>泛型灵活：支持任意类型的data字段</li>
 *   <li>Jackson序列化：使用@JsonFormat控制时间戳格式</li>
 * </ul>
 * 
 * @param <T> 响应数据的类型，可以是任意Java对象
 * @see com.example.mess.exception.GlobalExceptionHandler 全局异常处理器中使用此类
 * @see com.example.mess.controller.UserController 用户控制器中使用此类
 * @since 1.0
 */
public class ApiResponse<T> {
    
    /**
     * 请求是否成功标识。
     * <p>前端应根据此字段决定如何处理响应数据：
     * <ul>
     *   <li>true - 正常处理业务数据</li>
     *   <li>false - 显示错误信息或执行错误处理逻辑</li>
     * </ul>
     */
    private boolean success;

    /**
     * 响应消息文本。
     * <p>成功时为"Success"，失败时包含具体错误描述。
     * 前端可直接展示此消息给用户，或用于日志记录。
     */
    private String message;

    /**
     * 响应数据负载，泛型类型。
     * <p>成功时包含业务数据（如UserDto、Page等），失败时为null。
     * 使用泛型设计确保类型安全，避免前端类型转换错误。
     */
    private T data;

    /**
     * 响应生成时间戳。
     * <p>使用ISO 8601扩展格式：yyyy-MM-dd'T'HH:mm:ss.SSSSSS。
     * 用于记录请求处理时间，便于问题排查和性能分析。
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
    private LocalDateTime timestamp;

    /**
     * 请求路径，可选字段。
     * <p>用于日志关联和问题排查，在异常处理时自动填充。
     * 正常响应中通常为null，由GlobalExceptionHandler设置。
     */
    private String path;

    /**
     * 私有构造函数，防止外部直接实例化。
     * <p>自动设置当前时间戳，确保每个响应都有准确的时间记录。
     * 使用静态工厂方法创建实例，遵循不可变对象设计模式。
     */
    private ApiResponse() {
        this.timestamp = LocalDateTime.now();
    }

    /**
     * 创建成功响应（带数据）。
     * <p>自动设置success=true，message="Success"，并填充业务数据。
     * 
     * @param <T> 响应数据的类型
     * @param data 业务数据，可以为null（此时使用{@link #success()}）
     * @return 包含业务数据的成功响应对象
     */
    public static <T> ApiResponse<T> success(T data) {
        // 通过私有构造函数创建实例，构造时自动填充timestamp
        ApiResponse<T> response = new ApiResponse<>();
        // 标记为成功
        response.success = true;
        // 设置默认成功消息
        response.message = "Success";
        // 填充业务数据负载
        response.data = data;
        return response;
    }

    /**
     * 创建成功响应（无数据）。
     * <p>适用于删除、更新等不需要返回数据的操作。
     * 内部调用{@link #success(Object)}并传入null。
     * 
     * @param <T> 响应数据的类型
     * @return 不包含业务数据的成功响应对象
     */
    public static <T> ApiResponse<T> success() {
        // 复用success(T)方法并传入null，避免重复代码
        return success(null);
    }

    /**
     * 创建错误响应。
     * <p>自动设置success=false，data=null，message为传入的错误信息。
     * 不包含异常堆栈信息，保护系统内部细节。
     * 
     * @param <T> 响应数据的类型
     * @param message 错误描述信息，前端可直接展示给用户
     * @return 包含错误信息的失败响应对象
     */
    public static <T> ApiResponse<T> error(String message) {
        // 创建实例，构造时自动填充timestamp
        ApiResponse<T> response = new ApiResponse<>();
        // 标记为失败
        response.success = false;
        // 设置错误消息（data保持默认null，不返回业务数据）
        response.message = message;
        return response;
    }

    /** 获取成功标识。 @return true表示请求成功，false表示失败 */
    public boolean isSuccess() {
        // 返回成功标识，前端据此判断如何处理响应
        return success;
    }

    /** 设置成功标识，一般由静态工厂方法内部设置，反序列化时也会调用。 @param success 成功标识 */
    public void setSuccess(boolean success) {
        // 赋值成功标识
        this.success = success;
    }

    /** 获取响应消息。 @return 提示文本 */
    public String getMessage() {
        // 返回提示消息文本
        return message;
    }

    /** 设置响应消息，用于向前端传递提示或错误描述。 @param message 提示文本 */
    public void setMessage(String message) {
        // 赋值提示消息
        this.message = message;
    }

    /** 获取响应数据负载。 @return 泛型业务数据，失败时通常为null */
    public T getData() {
        // 返回泛型业务数据
        return data;
    }

    /** 设置响应数据负载。 @param data 泛型业务数据 */
    public void setData(T data) {
        // 赋值泛型业务数据
        this.data = data;
    }

    /** 获取响应时间戳。 @return 响应生成时间 */
    public LocalDateTime getTimestamp() {
        // 返回响应生成时间戳
        return timestamp;
    }

    /** 设置响应时间戳，默认在构造时自动生成，一般无需手动覆盖。 @param timestamp 响应时间 */
    public void setTimestamp(LocalDateTime timestamp) {
        // 赋值时间戳（构造时已自动生成，通常无需覆盖）
        this.timestamp = timestamp;
    }

    /** 获取请求路径。 @return 请求路径，正常响应时通常为null */
    public String getPath() {
        // 返回请求路径（异常时用于定位出错接口）
        return path;
    }

    /** 设置请求路径，常用于异常处理时标记出错的接口，便于日志排查。 @param path 请求路径 */
    public void setPath(String path) {
        // 赋值请求路径
        this.path = path;
    }

    /*
     * =========================================================================
     * 【技术债务】TD-004 统一响应
     * =========================================================================
     *
     * TD-004-1: 缺少 timestamp 字段
     *   现状：响应体无时间戳，前端无法判断响应新鲜度
     *   影响：缓存策略无法实现，排障时无法对齐前后端时间
     *   优先级：P2
     *   修复方案：加 timestamp 字段，在工厂方法中自动赋值
     *   预估工时：0.5d
     *
     * TD-004-2: 缺少 traceId 字段
     *   现状：无法将前端报错与后端日志关联
     *   影响：用户反馈"出错了"但无法定位后端日志行
     *   优先级：P2
     *   修复方案：加 traceId 字段，在过滤器中用 MDC 注入
     *   预估工时：1d
     *
     * TD-004-3: Controller 手写 ApiResponse.success()
     *   现状：每个 Controller 方法都手写封装，冗余
     *   影响：新增接口时易遗漏或格式不一致
     *   优先级：P3（当前接口少）
     *   修复方案：实现 ResponseBodyAdvice 自动包装
     *   预估工时：1d
     *
     * TD-004-4: 错误码未细分
     *   现状：code 直接用 HTTP 状态码，无业务细分
     *   影响：前端无法区分"用户名重复"和"邮箱重复"（都是 409）
     *   优先级：P3
     *   修复方案：code 用 6 位数字（如 409001=用户名重复, 409002=邮箱重复）
     *   预估工时：1d
     *
     * =========================================================================
     * 【重构路线图】统一响应演进方向
     * =========================================================================
     * Phase 1（当前）：code=HTTP状态码 + 手写封装 + 无 timestamp/traceId
     * Phase 2：加 timestamp + traceId + 错误码文档
     * Phase 3：ResponseBodyAdvice 自动封装 + 业务码细分 + RFC7807 Problem Details
     * =========================================================================
     */

    /*
     * =========================================================================
     * 【技术债务】TD-004-S 统一响应补充
     * =========================================================================
     *
     * TD-004-5: 缺少国际化支持
     *   现状：message 硬编码中文
     *   影响：多语言场景无法切换
     *   优先级：P3
     *   修复方案：message 放 i18n key，前端按 locale 渲染；或服务端依 Accept-Language 解析
     *   预估工时：1d
     *
     * TD-004-6: 缺少分页元信息
     *   现状：列表查询返回裸 List，无 total/page/size
     *   影响：前端无法实现分页 UI
     *   优先级：P1
     *   修复方案：data 内嵌分页结构 { list, total, page, size }
     *   预估工时：0.5d
     *
     * TD-004-7: 工厂方法不完整
     *   现状：只有 success 和 error 两个工厂方法
     *   影响：常见场景（如创建成功 201、部分成功 207）需手写构造
     *   优先级：P3
     *   修复方案：加 created()/partial()/fail() 等语义化工厂方法
     *   预估工时：0.5d
     * =========================================================================
     */
}