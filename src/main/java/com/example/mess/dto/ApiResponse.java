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
     * 【HTTP/REST】统一响应封装与状态码语义的工程权衡（非可执行代码）
     * =========================================================================
     *
     * 一、"包体状态码"vs"HTTP 状态码"两种流派
     *   1) 流派 A（严格 REST）：HTTP 状态码表达一切结果
     *      2xx 成功 / 4xx 客户端错误 / 5xx 服务端错误，
     *      包体只放数据（或 RFC 7807 Problem Details 见节四）
     *   2) 流派 B（包体统一，本类）：HTTP 几乎总是 200，
     *      包体 ApiResponse(success, message, data) 表达业务结果
     *   3) 工程权衡对比：
     *      严格 REST：语义纯正、CDN/网关/监控天然识别错误、
     *         代价是前端/代理需按状态码分支解析，特殊状态处理分散
     *      包体统一：前端解析一致、错误处理模式单一、
     *         代价是 HTTP 级监控"错误率"指标失真（成功 200 但业务失败）
     *   4) 混合派（推荐演进方向）：保留包体统一，
     *      但让 HTTP 状态码与业务结果尽量对齐：
     *      success=true -> 200/201；业务失败也用对应 4xx（404/409/400），
     *      未知异常 500——本类全局处理器已部分实践（见 GlobalExceptionHandler 块）

     * 二、本类字段的语义承诺与设计约束
     *   1) success：布尔，业务级成败标志——注意"成功"定义：
     *      getUserById 找到用户 = true；找不到应返 success=false + 404，
     *      而不是 success=false + 200（语义割裂是常见反模式）
     *   2) message：人类可读描述，前端提示用；机器处理应看 success/errorCode
     *      不要用 message 做业务分支判断（文案可变，不稳定契约）
     *   3) data：泛型业务数据，失败时通常 null —— 前端 null 判空必要
     *   4) timestamp：响应生成时间（ISO 字符串），客户端可断言时间偏差
     *      （时钟漂移检测/审计），但对缓存不友好（每次都变，ETAG 麻烦）
     *   5) path：异常时回填请求路径，排障友好；非异常场景维护 null
     *   6) errorCode（建议新增）：机器可读错误码（"USER_NOT_FOUND"），
     *      让前端做精确分支（404 有多种业务含义：用户不存在/订单不存在）

     * 三、响应头的配套设计（包体统一派更需要头）
     *   1) X-Request-Id：每次请求分配 UUID 响应回给客户端，
     *      客户端上报问题时带此 ID，日志秒定位
     *   2) X-RateLimit-Remaining：限流余量提示，客户端据此节流
     *   3) Deprecation: true + Sunset: <date>：废弃接口的标准化宣告
     *   4) Link: <https://api.example.com/docs>; rel="help"：错误响应引导文档
     *   5) Vary: Accept-Encoding：CDN 缓存维度（gzip 与未压缩分桶）

     * 四、RFC 7807 Problem Details（包体错误的标准格式）
     *   1) 严格 REST 派的错误响应标准：
     *      Content-Type: application/problem+json
     *      {
     *        "type": "https://example.com/probs/user-not-found",
     *        "title": "用户不存在",
     *        "status": 404,
     *        "detail": "ID 42 的用户不存在",
     *        "instance": "/api/users/42"
     *      }
     *   2) 字段语义：type（问题类型 URI）/ title（简述）/ status（HTTP 码）
     *      detail（细节）/ instance（出问题的请求 URI）
     *   3) 可扩展字段（本类对应）：errorCode（扩展 type）/ timestamp（扩展）
     *   4) Spring 支持：spring-web 的 ResponseEntity.problem(...).build()
     *      或 ProblemDetail（Spring 6 原生支持）——演进路线清晰
     *   5) 选型：内部 API 可继续 ApiResponse；对外 API 建议 Problem Details
     *      或双模式（按 Accept 头切换）

     * 五、泛型 ApiResponse<T> 的边界
     *   1) T 是数据载荷类型，编译期类型安全
     *   2) 运行时擦除：前端不知道 T 的具体类，文档（OpenAPI）补全类型
     *   3) T 的类型一致性：success 时 data 是 UserDto；失败时是 null
     *      —— 前端 TS 类型 data: UserDto | null 是正确表达
     *   4) 分页响应：T = Page<UserDto>，包体含分页元数据——
     *      包体统一派把"HTTP 头表达的分页信息"也放包体，契约自包含
     *   5) 枚举/状态类响应：T = String（如"ok"）或专用 ResultType，
     *      比裸 String 更有契约感
     * =========================================================================
     */
}