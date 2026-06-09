package utils;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;

/**
 * IntelliJ Platform 兼容性工具类。
 *
 * <p>提供向后兼容的 API 调用封装。
 * 不同版本的 IntelliJ Platform 中，某些 API 的签名可能不同。
 */
public class PlatformUtils {

    /**
     * 兼容的 invokeLater 调用。
     *
     * <p>不同版本的 IntelliJ Platform 中，invokeLater 的签名可能不同：
     * <ul>
     *   <li>2022.1+: invokeLater(Runnable, ModalityState)</li>
     *   <li>更早版本: invokeLater(Runnable)</li>
     * </ul>
     *
     * @param runnable 要执行的任务
     */
    public static void invokeLaterCompat(Runnable runnable) {
        if (PlatformVersion.isAtLeast2022_1()) {
            ApplicationManager.getApplication().invokeLater(runnable, getDefaultModalityState());
        } else {
            ApplicationManager.getApplication().invokeLater(runnable);
        }
    }

    /**
     * 获取默认 ModalityState（兼容旧版本）。
     *
     * <p>ModalityState.defaultModalityState() 在 2022.1+ 中引入。
     * 旧版本使用 ModalityState.NON_MODAL 作为回退。
     *
     * @return ModalityState 实例
     */
    public static ModalityState getDefaultModalityState() {
        try {
            // 尝试调用 defaultModalityState()
            java.lang.reflect.Method method = ModalityState.class.getMethod("defaultModalityState");
            return (ModalityState) method.invoke(null);
        } catch (Exception e) {
            // 回退到 NON_MODAL
            return ModalityState.NON_MODAL;
        }
    }
}
