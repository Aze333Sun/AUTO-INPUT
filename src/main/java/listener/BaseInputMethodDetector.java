package listener;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.psi.PsiDocumentManager;
import enums.CursorState;
import inputmethod.InputMethodChecker;
import com.intellij.openapi.application.ApplicationActivationListener;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.*;
import com.intellij.openapi.wm.IdeFrame;
import org.jetbrains.annotations.NotNull;
import utils.CommentUtils;

/**
 * 基础输入法检测器。
 *
 * <p>监听编辑器光标变化和 IDE 焦点变化，根据光标位置自动切换输入法。
 *
 * <p>核心逻辑：
 * <ul>
 *   <li>光标在代码区域 → 切换到英文输入法</li>
 *   <li>光标在注释区域 → 切换到中文输入法</li>
 *   <li>离开 IDE → 切换到中文输入法</li>
 *   <li>离开编辑器但在 IDE 内 → 切换到英文输入法</li>
 * </ul>
 *
 * <p>本类是 {@link VimInputMethodDetector} 的父类，
 * 提供基础的光标监听和焦点追踪功能。
 *
 * @author crl
 * @version 1.0
 */
public class BaseInputMethodDetector implements CaretListener, ApplicationActivationListener {

    /**
     * 当前光标状态（静态变量，跨实例共享）。
     * 初始状态为 INCODE（英文输入法）。
     */
    public static CursorState cursorState = CursorState.INCODE;

    /**
     * 标记鼠标是否在 IDE 主窗口外部。
     * true 表示鼠标在 IDE 外部，false 表示在 IDE 内部。
     */
    public static boolean OUTIDEA;

    /**
     * 标记焦点是否在编辑器外部。
     * true 表示焦点在编辑器外部，false 表示在编辑器内部。
     */
    public static boolean OUTEDITOR;

    /**
     * 标记 IdeaVim 是否处于 Insert 模式。
     * true 表示 Insert 模式，false 表示 Normal/Visual 等模式。
     */
    public static boolean ISINSERT = false;

    /**
     * 光标位置变化事件处理方法。
     * 当用户移动光标时，IntelliJ 平台会调用此方法。
     *
     * @param e 光标事件对象，包含编辑器和光标位置信息
     */
    @Override
    public void caretPositionChanged(@NotNull CaretEvent e) {
        checkAndPrint(e.getEditor());
    }

    /**
     * 检查光标位置并切换输入法（带 EDT 线程调度）。
     *
     * <p>此方法将检查操作调度到 EDT（Event Dispatch Thread）执行，
     * 因为 PSI 操作必须在 EDT 中进行。
     *
     * <p>执行流程：
     * <ol>
     *   <li>使用 invokeLater() 调度到 EDT</li>
     *   <li>使用 WriteCommandAction 包装 PSI 操作</li>
     *   <li>提交所有文档以确保 PSI 树最新</li>
     *   <li>调用 check() 方法执行实际检查</li>
     * </ol>
     *
     * @param editor 当前编辑器
     */
    protected void checkAndPrint(Editor editor) {
        ApplicationManager.getApplication().invokeLater(() -> {
            WriteCommandAction.runWriteCommandAction(editor.getProject(), () -> {
                // 提交所有文档，确保 PSI 树是最新的
                PsiDocumentManager.getInstance(editor.getProject()).commitAllDocuments();
                check(editor);
            });
        }, ModalityState.defaultModalityState());
    }

    /**
     * IDE 窗口激活事件处理方法。
     * 当用户切换回 IDE 窗口时调用。
     *
     * @param ideFrame IDE 主窗口
     */
    @Override
    public void applicationActivated(IdeFrame ideFrame) {
        OUTIDEA = false;
    }

    /**
     * IDE 窗口失去焦点事件处理方法。
     * 当用户切换到其他应用程序时调用。
     *
     * <p>行为：
     * <ul>
     *   <li>设置 OUTIDEA = true</li>
     *   <li>将光标状态设置为 OUTIDE（中文输入法）</li>
     *   <li>如果状态变化，触发输入法切换</li>
     * </ul>
     *
     * @param ideFrame IDE 主窗口
     */
    @Override
    public void applicationDeactivated(IdeFrame ideFrame) {
        OUTIDEA = true;

        System.out.println("鼠标已离开IDEA主窗口");
        CursorState newCursorState = CursorState.OUTIDE;
        System.out.println("之前光标状态为：" + cursorState);
        System.out.println("现在光标状态为：" + newCursorState);

        if (!cursorState.equals(newCursorState)) {
            cursorState = newCursorState;
            // 如果当前输入法与目标状态不匹配，触发切换
            if (!cursorState.getLanguage().equals(InputMethodChecker.getCurrentMode())) {
                InputMethodChecker.pressShift();
            }
        }
    }

    /**
     * 检查焦点离开编辑器时的输入法状态。
     *
     * <p>当焦点离开编辑器但在 IDE 内部时（如点击工具窗口），
     * 将输入法切换到英文模式。
     *
     * <p>使用 10ms 延迟确保焦点状态已完全更新。
     */
    public void chekOutEditor() {
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            // 忽略中断异常
        }

        if (OUTIDEA == false && OUTEDITOR) {
            System.out.println("当前在IDE内,但是在editor外");
            CursorState newCursorState = CursorState.INCODE;
            if (!cursorState.equals(newCursorState)) {
                cursorState = newCursorState;
                if (!cursorState.getLanguage().equals(InputMethodChecker.getCurrentMode())) {
                    InputMethodChecker.pressShift();
                }
            }
        }
    }

    /**
     * 检查光标位置并切换输入法（核心逻辑）。
     *
     * <p>检查流程：
     * <ol>
     *   <li>使用 {@link CommentUtils#isInComment(Editor)} 判断光标是否在注释中</li>
     *   <li>根据判断结果确定新的光标状态（INCOMMENT 或 INCODE）</li>
     *   <li>比较新状态与当前状态</li>
     *   <li>如果状态变化，更新当前状态并触发输入法切换</li>
     * </ol>
     *
     * @param editor 当前编辑器
     */
    protected void check(Editor editor) {
        CursorState newCursorState;

        // 判断光标是否在注释中
        boolean commentType = CommentUtils.isInComment(editor);
        if (commentType) {
            newCursorState = CursorState.INCOMMENT; // 注释中 → 中文输入法
        } else {
            newCursorState = CursorState.INCODE;    // 代码中 → 英文输入法
        }

        // 比较新状态与当前状态
        if (newCursorState.equals(cursorState)) {
            // 状态未变化，不做任何操作
        } else {
            // 状态变化，更新当前状态
            cursorState = newCursorState;

            // 如果当前输入法与目标状态不匹配，触发切换
            if (cursorState.getLanguage().equals(InputMethodChecker.getCurrentMode())) {
                // 输入法已匹配，无需切换
            } else {
                // 输入法不匹配，触发切换
                InputMethodChecker.pressShift();
            }
        }
    }
}
