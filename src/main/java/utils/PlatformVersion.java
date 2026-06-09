package utils;

import com.intellij.openapi.application.ApplicationInfo;

/**
 * IntelliJ Platform 版本检测工具类。
 *
 * <p>提供运行时版本检测功能，用于处理不同版本间的 API 差异。
 */
public class PlatformVersion {

    /** 缓存的构建号，避免重复获取 */
    private static int cachedBuildNumber = -1;

    /**
     * 获取当前 IntelliJ Platform 的构建号。
     *
     * @return 构建号（如 223 = 2022.3, 243 = 2024.3）
     */
    public static int getBuildNumber() {
        if (cachedBuildNumber == -1) {
            ApplicationInfo appInfo = ApplicationInfo.getInstance();
            String buildNumber = appInfo.getBuild().getNumber();
            try {
                cachedBuildNumber = Integer.parseInt(buildNumber);
            } catch (NumberFormatException e) {
                // 处理 EAP 版本号（如 "223.12345"）
                int dotIndex = buildNumber.indexOf('.');
                if (dotIndex > 0) {
                    cachedBuildNumber = Integer.parseInt(buildNumber.substring(0, dotIndex));
                } else {
                    cachedBuildNumber = 0;
                }
            }
        }
        return cachedBuildNumber;
    }

    /**
     * 检查当前平台版本是否 >= 指定版本。
     *
     * @param minBuildNumber 最低构建号
     * @return 如果当前版本 >= 指定版本返回 true
     */
    public static boolean isVersionAtLeast(int minBuildNumber) {
        return getBuildNumber() >= minBuildNumber;
    }

    /**
     * 检查当前平台版本是否为 2022.3+。
     *
     * @return 如果是 2022.3+ 返回 true
     */
    public static boolean isAtLeast2022_3() {
        return isVersionAtLeast(223);
    }

    /**
     * 检查当前平台版本是否为 2022.1+。
     *
     * @return 如果是 2022.1+ 返回 true
     */
    public static boolean isAtLeast2022_1() {
        return isVersionAtLeast(221);
    }

    /**
     * 检查当前平台版本是否为 2024.1+。
     *
     * @return 如果是 2024.1+ 返回 true
     */
    public static boolean isAtLeast2024_1() {
        return isVersionAtLeast(241);
    }
}
