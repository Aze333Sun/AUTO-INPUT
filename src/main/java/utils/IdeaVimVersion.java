package utils;

/**
 * IdeaVim 版本检测工具类。
 *
 * <p>提供运行时 IdeaVim 版本检测功能，用于处理不同版本间的 API 差异。
 *
 * <p>IdeaVim 2.x 引入了新的 API 架构：
 * <ul>
 *   <li>新增 `newapi` 包用于桥接 IdeaVim 抽象和 IntelliJ 原生对象</li>
 *   <li>新增 `VimInjector` 依赖注入框架</li>
 *   <li>新增 `Mode` 密封类表示 Vim 模式</li>
 * </ul>
 */
public class IdeaVimVersion {

    /** 缓存的版本检测结果，避免重复检测 */
    private static Boolean cachedIsVimAvailable = null;
    private static Boolean cachedIsVim2x = null;

    /**
     * 检查 IdeaVim 是否可用。
     *
     * @return 如果 IdeaVim 已安装并可用返回 true
     */
    public static boolean isVimAvailable() {
        if (cachedIsVimAvailable == null) {
            try {
                Class.forName("com.maddyhome.idea.vim.VimPlugin");
                cachedIsVimAvailable = true;
            } catch (ClassNotFoundException e) {
                cachedIsVimAvailable = false;
            }
        }
        return cachedIsVimAvailable;
    }

    /**
     * 检查 IdeaVim 是否为 2.x 版本。
     *
     * <p>IdeaVim 2.x 引入了新的 API 包：
     * <ul>
     *   <li>com.maddyhome.idea.vim.api.VimInjector</li>
     *   <li>com.maddyhome.idea.vim.newapi.IjVimInjectorKt</li>
     *   <li>com.maddyhome.idea.vim.state.mode.Mode</li>
     * </ul>
     *
     * @return 如果是 IdeaVim 2.x 返回 true，否则返回 false（1.x 或未安装）
     */
    public static boolean isVim2x() {
        if (cachedIsVim2x == null) {
            if (!isVimAvailable()) {
                cachedIsVim2x = false;
            } else {
                try {
                    // IdeaVim 2.x 特有的类
                    Class.forName("com.maddyhome.idea.vim.api.VimInjector");
                    Class.forName("com.maddyhome.idea.vim.newapi.IjVimInjectorKt");
                    cachedIsVim2x = true;
                } catch (ClassNotFoundException e) {
                    cachedIsVim2x = false;
                }
            }
        }
        return cachedIsVim2x;
    }

    /**
     * 获取 IdeaVim 版本字符串。
     *
     * @return 版本字符串，如 "2.22.0"，如果未安装返回 "N/A"
     */
    public static String getVersion() {
        if (!isVimAvailable()) {
            return "N/A";
        }
        try {
            Class<?> vimPluginClass = Class.forName("com.maddyhome.idea.vim.VimPlugin");
            java.lang.reflect.Method getVersionMethod = vimPluginClass.getMethod("getVersion");
            Object version = getVersionMethod.invoke(null);
            return version != null ? version.toString() : "unknown";
        } catch (Exception e) {
            return "unknown";
        }
    }
}
