package enums;

/**
 * 输入法全局状态管理类（单例模式）。
 *
 * <p>使用单例模式确保全局只有一个状态管理实例。
 *
 * <p>管理的状态：
 * <ul>
 *   <li>{@link #outOfIdea} - 鼠标是否在 IDE 外部</li>
 *   <li>{@link #outOfEditor} - 焦点是否在编辑器外部</li>
 *   <li>{@link #isInsertMode} - IdeaVim 是否处于 Insert 模式</li>
 * </ul>
 *
 * <p>注意：当前实现中，这些状态主要在 {@link listener.BaseInputMethodDetector} 中通过静态变量管理。
 * 本类提供了一种更面向对象的状态管理方式，但尚未完全使用。
 *
 * @author crl
 * @version 1.0
 */
public class InputMethodState {

    /** 单例实例 */
    private static final InputMethodState INSTANCE = new InputMethodState();

    /** 私有构造函数，防止外部实例化 */
    private InputMethodState() {}

    /**
     * 获取单例实例。
     *
     * @return InputMethodState 实例
     */
    public static InputMethodState getInstance() {
        return INSTANCE;
    }

    /** 鼠标是否在 IDE 外部 */
    private boolean outOfIdea;

    /** 焦点是否在编辑器外部 */
    private boolean outOfEditor;

    /** IdeaVim 是否处于 Insert 模式 */
    private boolean isInsertMode;

    /**
     * 检查鼠标是否在 IDE 外部。
     *
     * @return 如果鼠标在 IDE 外部返回 true，否则返回 false
     */
    public boolean isOutOfIdea() {
        return outOfIdea;
    }

    /**
     * 设置鼠标是否在 IDE 外部的状态。
     *
     * @param outOfIdea true 表示鼠标在 IDE 外部，false 表示在 IDE 内部
     */
    public void setOutOfIdea(boolean outOfIdea) {
        this.outOfIdea = outOfIdea;
    }

    /**
     * 检查焦点是否在编辑器外部。
     *
     * @return 如果焦点在编辑器外部返回 true，否则返回 false
     */
    public boolean isOutOfEditor() {
        return outOfEditor;
    }

    /**
     * 设置焦点是否在编辑器外部的状态。
     *
     * @param outOfEditor true 表示焦点在编辑器外部，false 表示在编辑器内部
     */
    public void setOutOfEditor(boolean outOfEditor) {
        this.outOfEditor = outOfEditor;
    }

    /**
     * 检查 IdeaVim 是否处于 Insert 模式。
     *
     * @return 如果 IdeaVim 处于 Insert 模式返回 true，否则返回 false
     */
    public boolean isInsertMode() {
        return isInsertMode;
    }

    /**
     * 设置 IdeaVim 的 Insert 模式状态。
     *
     * @param insertMode true 表示 Insert 模式，false 表示 Normal/Visual 等模式
     */
    public void setInsertMode(boolean insertMode) {
        isInsertMode = insertMode;
    }
}
