package listener;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiDocumentManager;
import enums.CursorState;
import inputmethod.InputMethodChecker;
import utils.CommentUtils;
import utils.IdeaVimVersion;

/**
 * IdeaVim 输入法检测器。
 *
 * <p>继承 {@link BaseInputMethodDetector}，
 * 用于监听 Vim 模式变化并自动切换输入法。
 * 支持 IdeaVim 1.x 和 2.x 版本。
 *
 * @author crl
 * @version 1.0
 */
public class VimInputMethodDetector extends BaseInputMethodDetector {

    /**
     * IdeaVim 模式变化事件处理方法（使用反射）。
     *
     * @param vimEditor Vim 编辑器对象
     * @param mode 新的 Vim 模式
     */
    public void modeChanged(Object vimEditor, Object mode) {
        try {
            Editor editor = convertToEditor(vimEditor);
            if (editor == null) return;

            boolean isInsertMode = checkIfInsertMode(vimEditor, mode);
            ISINSERT = isInsertMode;

            checkAndPrint(editor);
        } catch (Exception e) {
            System.out.println("IdeaVim 模式变化处理失败: " + e.getMessage());
        }
    }

    /**
     * 将 VimEditor 转换为 IntelliJ Editor。
     */
    private Editor convertToEditor(Object vimEditor) {
        try {
            if (IdeaVimVersion.isVim2x()) {
                Class<?> ijVimEditorKtClass = Class.forName("com.maddyhome.idea.vim.newapi.IjVimEditorKt");
                java.lang.reflect.Method getIjMethod = ijVimEditorKtClass.getMethod("getIj",
                        Class.forName("com.maddyhome.idea.vim.api.VimEditor"));
                return (Editor) getIjMethod.invoke(null, vimEditor);
            } else {
                if (vimEditor instanceof Editor) {
                    return (Editor) vimEditor;
                }
            }
        } catch (Exception e) {
            System.out.println("VimEditor 转换失败: " + e.getMessage());
        }
        return null;
    }

    /**
     * 检查当前是否为 Insert 模式。
     */
    private boolean checkIfInsertMode(Object vimEditor, Object mode) {
        try {
            if (IdeaVimVersion.isVim2x()) {
                if (mode != null) {
                    Class<?> insertClass = Class.forName("com.maddyhome.idea.vim.state.mode.Mode$INSERT");
                    return insertClass.isInstance(mode);
                }

                if (vimEditor != null) {
                    java.lang.reflect.Method getModeMethod = vimEditor.getClass().getMethod("getMode");
                    Object currentMode = getModeMethod.invoke(vimEditor);
                    Class<?> insertClass = Class.forName("com.maddyhome.idea.vim.state.mode.Mode$INSERT");
                    return insertClass.isInstance(currentMode);
                }
            } else {
                if (vimEditor != null) {
                    java.lang.reflect.Method getModeMethod = vimEditor.getClass().getMethod("getMode");
                    Object currentMode = getModeMethod.invoke(vimEditor);
                    String modeString = currentMode.toString().toLowerCase();
                    return modeString.contains("insert");
                }
            }
        } catch (Exception e) {
            System.out.println("Insert 模式检查失败: " + e.getMessage());
        }
        return false;
    }

    @Override
    protected void checkAndPrint(Editor editor) {
        ApplicationManager.getApplication().invokeLater(() -> {
            WriteCommandAction.runWriteCommandAction(editor.getProject(), () -> {
                PsiDocumentManager.getInstance(editor.getProject()).commitAllDocuments();
                check(editor);
            });
        }, ModalityState.defaultModalityState());
    }

    @Override
    protected void check(Editor editor) {
        CursorState newCursorState;

        if (!ISINSERT) {
            newCursorState = CursorState.INCODE;
        } else {
            boolean commentType = CommentUtils.isInComment(editor);
            if (commentType) {
                newCursorState = CursorState.INCOMMENT;
            } else {
                newCursorState = CursorState.INCODE;
            }
        }

        System.out.println("旧模式" + cursorState);
        System.out.println("新模式" + newCursorState);

        if (!newCursorState.equals(cursorState)) {
            cursorState = newCursorState;
            if (!cursorState.getLanguage().equals(InputMethodChecker.getCurrentMode())) {
                InputMethodChecker.pressShift();
            }
        }
    }
}
