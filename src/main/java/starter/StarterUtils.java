package starter;

import com.intellij.openapi.application.ApplicationActivationListener;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import listener.BaseInputMethodDetector;
import listener.EditorFocusTracker;
import utils.IdeaVimVersion;

/**
 * 插件启动工具类。
 *
 * <p>提供工厂方法，用于注册各种监听器。
 * 支持 IdeaVim 1.x 和 2.x 版本。
 *
 * @author crl
 * @version 1.0
 */
public class StarterUtils {

    /**
     * 标记 Vim 组件是否已初始化。
     */
    boolean vimInitialized = false;

    /**
     * 注册基础监听器。
     */
    public void baseMethodFactory(Editor editor, Project project, BaseInputMethodDetector listener) {
        editor.getCaretModel().addCaretListener(listener);

        ApplicationManager.getApplication().getMessageBus().connect()
                .subscribe(ApplicationActivationListener.TOPIC, listener);

        EditorFocusTracker.addFocusListener(project, hasFocus -> {
            if (hasFocus) {
                BaseInputMethodDetector.OUTEDITOR = false;
                System.out.println("获得了注意");
            } else {
                System.out.println("失去了注意");
                BaseInputMethodDetector.OUTEDITOR = true;
                listener.chekOutEditor();
            }
        });
    }

    /**
     * 注册 IdeaVim 模式监听器。
     *
     * <p>根据 IdeaVim 版本选择不同的注册方式。
     */
    public void vimMethodFactory(Object listener) {
        if (IdeaVimVersion.isVim2x()) {
            vimMethodFactoryV2(listener);
        } else {
            vimMethodFactoryV1(listener);
        }
    }

    /**
     * 注册 IdeaVim 2.x 模式监听器。
     */
    private void vimMethodFactoryV2(Object listener) {
        try {
            if (!vimInitialized) {
                System.out.println("初始化Vim组件 (IdeaVim 2.x)");
                Class<?> injectorKtClass = Class.forName("com.maddyhome.idea.vim.newapi.IjVimInjectorKt");
                java.lang.reflect.Method initInjectorMethod = injectorKtClass.getMethod("initInjector");
                initInjectorMethod.invoke(null);
                vimInitialized = true;
            }

            Class<?> vimInjectorKtClass = Class.forName("com.maddyhome.idea.vim.api.VimInjectorKt");
            java.lang.reflect.Method getInjectorMethod = vimInjectorKtClass.getMethod("getInjector");
            Object vimInjector = getInjectorMethod.invoke(null);

            if (vimInjector != null) {
                java.lang.reflect.Method getListenersNotifierMethod = vimInjector.getClass().getMethod("getListenersNotifier");
                Object listenersNotifier = getListenersNotifierMethod.invoke(vimInjector);

                if (listenersNotifier != null) {
                    java.lang.reflect.Method getModeChangeListenersMethod = listenersNotifier.getClass().getMethod("getModeChangeListeners");
                    Object listeners = getModeChangeListenersMethod.invoke(listenersNotifier);

                    java.lang.reflect.Method addMethod = listeners.getClass().getMethod("add", Object.class);
                    addMethod.invoke(listeners, listener);

                    System.out.println("成功添加Vim模式变更监听器 (IdeaVim 2.x)");
                }
            }
        } catch (Exception e) {
            System.out.println("IdeaVim 2.x 初始化失败: " + e.getMessage());
        }
    }

    /**
     * 注册 IdeaVim 1.x 模式监听器。
     */
    private void vimMethodFactoryV1(Object listener) {
        try {
            System.out.println("使用 IdeaVim 1.x 兼容模式");
            Class<?> vimPluginClass = Class.forName("com.maddyhome.idea.vim.VimPlugin");
            java.lang.reflect.Method getInstanceMethod = vimPluginClass.getMethod("getInstance");
            Object vimPlugin = getInstanceMethod.invoke(null);

            if (vimPlugin != null) {
                System.out.println("IdeaVim 1.x 监听器注册方式需要进一步适配");
            }
        } catch (Exception e) {
            System.out.println("IdeaVim 1.x 初始化失败: " + e.getMessage());
        }
    }
}
