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
     * ============================================================================
     * 【设计文档】ApiResponse 统一响应封装设计说明（补充文档，非可执行代码）
     * ============================================================================
     *
     * 一、设计目标
     * ----------------------------------------------------------------------------
     * ApiResponse<T> 是全站统一的响应外壳，用于把所有 REST 接口的返回体规整为
     * 一致结构，从而：
     *   1) 前端只需实现一套解析逻辑，即可处理任意接口的成功/失败;
     *   2) 错误信息（code/message）与业务数据（data）解耦，语义清晰;
     *   3) 通过泛型 T 保持类型安全，避免使用裸 Object 带来的强转风险。
     *
     * 二、字段语义
     * ----------------------------------------------------------------------------
     *   code    —— 业务状态码，通常与 HTTP 状态对齐（200 成功、400 参数错、404 未找到…）;
     *   message —— 人类可读的提示信息，成功固定为 "success"，失败为具体原因;
     *   data    —— 泛型业务数据，成功时携带，失败或无返回体时为 null;
     *   path    —— 触发本响应的请求路径，便于前端与日志排查定位。
     *
     * 三、静态工厂方法约定
     * ----------------------------------------------------------------------------
     *   success(data)            —— 构造成功响应，code=200、message="success";
     *   success(message, data)   —— 成功但需自定义提示语的场景;
     *   error(code, message)     —— 构造失败响应，data 为 null;
     *   使用静态工厂而非直接 new，可隐藏构造细节、集中默认值、提升可读性。
     *
     * 四、泛型反序列化要点（前端/测试消费方必读）
     * ----------------------------------------------------------------------------
     *   由于 Java 泛型擦除，Jackson 反序列化 ApiResponse<T> 时无法自动推断 T。
     *   在测试中需借助 TypeFactory 显式构造带参类型：
     *     JavaType type = objectMapper.getTypeFactory()
     *         .constructParametricType(ApiResponse.class, String.class);
     *     ApiResponse<String> resp = objectMapper.readValue(json, type);
     *   若省略此步骤，data 会被反序列化为 LinkedHashMap 而非目标类型。
     *
     * 五、典型使用示例
     * ----------------------------------------------------------------------------
     *   // Controller 中构造成功响应
     *   return ApiResponse.success(userDto);
     *   // GlobalExceptionHandler 中构造失败响应
     *   return ApiResponse.error(404, "用户不存在");
     *
     * 六、设计决策
     * ----------------------------------------------------------------------------
     * 决策 1：code 与 HTTP 状态对齐而非自定义业务码段
     *   理由：降低前端记忆成本；如需精细业务码，可在不破坏该约定的前提下扩展区间。
     * 决策 2：失败时 data 统一为 null 而非省略字段
     *   理由：保持 JSON 结构稳定，前端无需判断字段是否存在。
     * 决策 3：保留 path 字段
     *   理由：便于在网关/前端集中展示"哪个接口出错"，提升排障效率。
     *
     * 七、扩展指南
     * ----------------------------------------------------------------------------
     *   - 需要链路追踪：可增加 traceId 字段并在过滤器中统一注入;
     *   - 需要分页元信息：可新增 PageResult<T> 或在 data 内嵌分页结构;
     *   - 需要时间戳：可增加 timestamp 字段辅助客户端做时序判断。
     * ============================================================================
     */

    /*
     * ============================================================================
     * 【补充文档】ApiResponse 请求时序与常见问题（FAQ，非可执行代码）
     * ============================================================================
     *
     * 一、成功请求的响应封装时序
     * ----------------------------------------------------------------------------
     *   客户端  ->  Controller     : 发起 HTTP 请求
     *   Controller -> Service      : 调用业务方法
     *   Service -> Repository      : 读写数据库
     *   Repository --> Service     : 返回实体/DTO
     *   Service --> Controller     : 返回业务结果
     *   Controller -> ApiResponse  : ApiResponse.success(data) 封装
     *   Controller --> 客户端       : 序列化为统一 JSON 结构返回
     *
     * 二、失败请求的响应封装时序
     * ----------------------------------------------------------------------------
     *   Service 抛出异常
     *     -> @RestControllerAdvice(GlobalExceptionHandler) 捕获
     *     -> 依据异常类型映射 HTTP 状态与 code
     *     -> ApiResponse.error(code, message) 封装
     *     -> 统一 JSON 结构返回；500 场景不透出堆栈细节
     *
     * 三、FAQ
     * ----------------------------------------------------------------------------
     * Q1：为什么前端拿到的 data 变成了 LinkedHashMap 而不是目标对象？
     *   A：泛型在运行期被擦除，反序列化时需显式告知类型。使用
     *      objectMapper.getTypeFactory()
     *          .constructParametricType(ApiResponse.class, TargetType.class)
     *      构造带泛型的 JavaType 再反序列化即可正确还原。
     *
     * Q2：成功但无返回数据（如删除操作）该怎么封装？
     *   A：可返回 ApiResponse.success(null)，保持结构一致；data 为 null 表示
     *      "操作成功但无数据体"，与失败语义（同样 data=null 但 code 非 2xx）通过
     *      code 字段区分。
     *
     * Q3：分页数据如何返回？
     *   A：推荐 data 内嵌分页结构（list + total + pageNo + pageSize），或新增
     *      PageResult<T> 作为 data 的类型，避免污染顶层 ApiResponse 结构。
     *
     * Q4：code 与 HTTP 状态码是否必须完全一致？
     *   A：默认对齐以降低认知成本；若确需业务细分码，建议在文档中约定区间
     *      （如 4xxxx 表示业务校验失败），并保持与 HTTP 语义不冲突。
     *
     * Q5：多语言 message 怎么处理？
     *   A：message 可只放 i18n key，由前端按 locale 渲染；或在服务端依据
     *      Accept-Language 解析后填充最终文案。
     *
     * 四、前端消费建议
     * ----------------------------------------------------------------------------
     *   - 统一在响应拦截器中判断 code：成功放行、失败集中提示;
     *   - 对 401/403 做统一跳转登录/无权限处理;
     *   - 对 5xx 展示兜底文案并上报监控，避免直接暴露技术细节给终端用户。
     * ============================================================================
     */
}