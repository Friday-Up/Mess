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
     * 【Java 并发】响应对象的安全发布、序列化线程安全与伪共享概念（非可执行代码）
     * =========================================================================
     *
     * 一、响应对象的线程旅程
     *   1) 本对象在单次请求的生命期内被三个阶段触碰：
     *      - Service 返回值构造（工作线程 A）
     *      - MessageConverter 序列化（仍是工作线程 A，方法栈内）
     *      - 异步/响应式场景：跨入异步线程后再序列化（线程 A 构造、线程 B 写出）
     *   2) 异步分支的风险点：若本对象在构造后被另一个线程修改，
     *      写出线程可能读到中间态 -> 字段前后不一致的响应体
     *   3) 防御模式：Service 一旦返回 ApiResponse，禁止再修改任何字段
     *      （把升操作封装到静态工厂内，业务侧拿到的就是"冻结"对象）；
     *      本类的 success(...) 与 error(...) 静态工厂即遵循此模式
     *
     * 二、时间戳与时区的并发陷阱
     *   1) 本类 timestamp 默认在构造器初始化为 LocalDateTime.now()，
     *      构造发生在工作线程 -> 写出时已定格，跨线程安全
     *   2) 陷阱场景：若改为"惰性生成"（getter 内 if null then now()），
     *      并发 getter 会产生两个不同的时间值，违反语义与可比性
     *   3) 多线程取时间的性能：LocalDateTime.now() 在 JDK 17 后每次纳秒级，
     *      无锁竞争，可放心高频调用；老代码用 System.currentTimeMillis
     *      并发高频调用在部分平台有全局锁影响（现代 JDK 已优化）
     *   4) 对齐时区：LocalDateTime 不含时区，序列化时使用系统默认时区；
     *      分布式多区域部署时建议统一存 Instant（UTC）+ 输出时转换
     *
     * 三、泛型字段与类型擦除的并发视角
     *   1) ApiResponse<T> 的 data 字段类型在运行时擦除为 Object：
     *      静态工厂传入的实参对象类型与安全性由调用方保证
     *   2) data 指向的实体可变性决定整体安全性：
     *      - Date/可变 POJO -> 跨线程存在被修改的可能
     *      - DTO/record/集合不可变包装 -> 绝对安全
     *   3) 深拷贝与时序：若 Service 返回时主动 copy，必须确保拷贝在
     *      请求线程完成，而非延后到异步写出线程再做（时序窗口会缩小
     *      错误难复现）
     *
     * 四、JML 基础复习与事务内构造对象
     *   1) happens-before 规则的记忆口诀（顺序由编译器与 CPU 都遵守）：
     *      - 程序顺序规则：单线程内前面的操作 hb 后面的操作
     *      - 锁规则：unlock hb 后续同一把锁的 lock
     *      - volatile 规则：写 hb 后续读
     *      - 线程启动规则：Thread.start hb 该线程内任一动作
     *      - 线程终止规则：线程内任一动作 hb 他线程观察其结束（join/isAlive）
     *   2) 应用到本对象：Service 在事务内构造 ApiResponse，事务提交
     *      保证 DB 写入可见；但响应对象实体的字段同步语义与 DB 事务无关
     *      两者是正交概念，不要混淆 DB 事务与内存可见性
     *   3) 伪共享（False Sharing）说明：两个高频写的独立字段落在同一
     *      CPU 缓存行（通常 64 字节），彼此 invalidate 对方的缓存行，
     *      引发显著的性能下降；修复用 @Contended 或手工补齐填充字段。
     *      本类字段写频率极低（构造即定），无伪共享之虑，仅作认识
     * =========================================================================
     */
}