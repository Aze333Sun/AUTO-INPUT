package enums;

/**
 * 输入法状态枚举。
 *
 * <p>定义输入法的两种状态：
 * <ul>
 *   <li>{@link #ENGLISH} - 英文输入法</li>
 *   <li>{@link #CHINESE} - 中文输入法</li>
 * </ul>
 *
 * <p>使用场景：
 * <ul>
 *   <li>{@link CursorState} 中定义每种光标状态对应的输入法语言</li>
 *   <li>{@link inputmethod.InputMethodChecker#getCurrentMode()} 返回当前输入法状态</li>
 *   <li>在输入法切换逻辑中比较当前状态与目标状态</li>
 * </ul>
 *
 * @author crl
 * @version 1.0
 */
public enum InputState {

    /** 英文输入法 */
    ENGLISH,

    /** 中文输入法 */
    CHINESE;
}
