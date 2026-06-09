package starter;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.EditorFactoryEvent;
import com.intellij.openapi.editor.event.EditorFactoryListener;
import com.intellij.openapi.project.Project;
import listener.VimInputMethodDetector;
import org.jetbrains.annotations.NotNull;
import utils.IdeaVimVersion;

/**
 * IdeaVim 模式输入法插件入口。
 *
 * <p>当 IdeaVim 已安装时，使用此入口。
 * 支持 IdeaVim 1.x 和 2.x 版本。
 *
 * @author crl
 * @version 1.0
 */
public class VimInputMethodPlugin implements EditorFactoryListener {

    public VimInputMethodPlugin() {
        System.out.println("VimInputMethodPlugin");
    }

    @Override
    public void editorCreated(@NotNull EditorFactoryEvent event) {
        System.out.println("现在是vim模式");

        StarterUtils starterUtils = new StarterUtils();

        Editor editor = event.getEditor();
        Project project = editor.getProject();

        VimInputMethodDetector listener = new VimInputMethodDetector();

        // 使用反射注册 Vim 模式监听器
        registerVimModeListener(starterUtils, listener);

        // 注册基础监听器
        starterUtils.baseMethodFactory(editor, project, listener);
    }

    /**
     * 使用反射注册 Vim 模式监听器。
     */
    private void registerVimModeListener(StarterUtils starterUtils, VimInputMethodDetector listener) {
        try {
            if (IdeaVimVersion.isVim2x()) {
                Class<?> modeChangeListenerClass = Class.forName("com.maddyhome.idea.vim.common.ModeChangeListener");
                if (modeChangeListenerClass.isInstance(listener)) {
                    starterUtils.vimMethodFactory(listener);
                } else {
                    System.out.println("IdeaVim 2.x: listener 未实现 ModeChangeListener");
                }
            } else {
                starterUtils.vimMethodFactory(listener);
            }
        } catch (Exception e) {
            System.out.println("Vim 模式监听器注册失败: " + e.getMessage());
        }
    }

    @Override
    public void editorReleased(@NotNull EditorFactoryEvent event) {
        // 不需要清理
    }
}
