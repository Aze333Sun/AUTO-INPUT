package starter;

import listener.BaseInputMethodDetector;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.EditorFactoryEvent;
import com.intellij.openapi.editor.event.EditorFactoryListener;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;


/**
 * 普通模式输入法插件入口。
 *
 * <p>当 IdeaVim 未安装时，使用此入口。
 *
 * <p>实现 {@link EditorFactoryListener} 接口，监听编辑器创建事件。
 * 当编辑器创建时，自动注册输入法检测器。
 *
 * <p>工作流程：
 * <ol>
 *   <li>检测 IdeaVim 是否安装（通过 {@link Class#forName}）</li>
 *   <li>如果 IdeaVim 未安装，创建 {@link BaseInputMethodDetector} 实例</li>
 *   <li>使用 {@link StarterUtils} 注册监听器</li>
 * </ol>
 *
 * <p>注册位置：{@code plugin.xml} 中的 {@code <editorFactoryListener>} 扩展点
 *
 * @author crl
 * @version 1.0
 */
public class InputMethodPlugin implements EditorFactoryListener {

    /**
     * 编辑器创建事件处理方法。
     *
     * <p>当用户打开文件或创建新编辑器时，IntelliJ 平台会调用此方法。
     *
     * @param event 编辑器工厂事件，包含编辑器信息
     */
    @Override
    public void editorCreated(@NotNull EditorFactoryEvent event) {
        try {
            // 尝试加载 IdeaVim 插件类
            // 如果 IdeaVim 已安装，Class.forName() 会成功，不会进入 catch 块
            // 如果 IdeaVim 未安装，Class.forName() 会抛出 ClassNotFoundException
            Class.forName("com.maddyhome.idea.vim.VimPlugin");
        } catch (Exception e) {
            // IdeaVim 未安装，使用普通模式
            System.out.println("现在是普通模式");

            StarterUtils starterUtils = new StarterUtils();

            Editor editor = event.getEditor();
            Project project = editor.getProject();

            System.out.println("现在无vim模式");

            // 创建基础输入法检测器并注册监听器
            BaseInputMethodDetector listener = new BaseInputMethodDetector();
            starterUtils.baseMethodFactory(editor, project, listener);
        }
    }

    /**
     * 编辑器释放事件处理方法。
     *
     * <p>当编辑器关闭时，IntelliJ 平台会调用此方法。
     * 当前实现为空，因为监听器会随项目关闭自动清理。
     *
     * @param event 编辑器工厂事件
     */
    @Override
    public void editorReleased(@NotNull EditorFactoryEvent event) {
        // 不需要清理，监听器会随项目关闭自动清理
    }
}
