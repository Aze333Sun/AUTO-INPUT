package enums;

/**
 * 光标状态枚举。
 *
 * <p>定义光标在不同场景下的状态，以及每种状态对应的输入法语言。
 *
 * <p>状态说明：
 * <ul>
 *   <li>{@link #OUTIDE} - 光标在 IDE 外部（中文输入法）</li>
 *   <li>{@link #INCOMMENT} - 光标在注释中（中文输入法）</li>
 *   <li>{@link #INCODE} - 光标在代码中（英文输入法）</li>
 * </ul>
 *
 * <p>状态转换：
 * <pre>
 * IDE 外部(中文)  → IDE 内部代码区域(英文)  → 注释区域(中文) → 代码区域(英文) → ...
 * </pre>
 *
 * @author crl
 * @version 1.0
 */
public enum CursorState {

    /**
     * 光标在 IDE 外部。
     * 对应输入法：中文
     * 触发条件：鼠标点击 IDE 窗口外部
     */
    OUTIDE("OUTSIDE", InputState.CHINESE),

    /**
     * 光标在注释中。
     * 对应输入法：中文
     * 触发条件：光标移动到注释区域
     */
    INCOMMENT("INCOMMENT", InputState.CHINESE),

    /**
     * 光标在代码中。
     * 对应输入法：英文
     * 触发条件：光标移动到代码区域
     */
    INCODE("INCODE", InputState.ENGLISH);

    /** 状态代码（用于日志和调试） */
    private final String Code;

    /** 该状态对应的输入法语言 */
    private final InputState Language;

    /**
     * 构造函数。
     *
     * @param Code 状态代码
     * @param Language 该状态对应的输入法语言
     */
    CursorState(String Code, InputState Language) {
        this.Code = Code;
        this.Language = Language;
    }

    /**
     * 获取状态代码。
     *
     * @return 状态代码字符串
     */
    public String getCode() {
        return Code;
    }

    /**
     * 获取该状态对应的输入法语言。
     *
     * @return 输入法语言枚举值
     */
    public InputState getLanguage() {
        return Language;
    }
}
