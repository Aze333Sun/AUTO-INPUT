package inputmethod;

import enums.InputState;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;


import static com.sun.jna.platform.win32.WinUser.KEYBDINPUT.KEYEVENTF_KEYUP;
import static com.sun.jna.platform.win32.WinUser.VK_LSHIFT;

/**
 * Windows 输入法检测与切换工具类。
 *
 * <p>通过 JNA 调用 Windows 原生 API（imm32.dll 和 user32.dll）来实现：
 * <ul>
 *   <li>检测当前输入法状态（中文/英文）</li>
 *   <li>模拟 Shift 按键切换输入法</li>
 * </ul>
 *
 * <p>支持的输入法：微软拼音、搜狗输入法、百度输入法。
 * 不支持微信输入法（不兼容标准 IME 接口）。
 *
 * <p>设计说明：
 * <ul>
 *   <li>使用静态方法和静态初始化块，避免对象创建开销</li>
 *   <li>不适合重构为非静态模式（会影响性能）</li>
 *   <li>内置 100ms 防抖机制，避免频繁切换</li>
 * </ul>
 *
 * @author crl
 * @version 1.0
 */
public class InputMethodChecker {

    /**
     * 上次按下 Shift 的时间戳（毫秒）。
     * 用于实现防抖机制，避免频繁切换输入法。
     */
    private static long lastPressTime = 0;

    /**
     * 最小按键间隔（毫秒）。
     * 如果两次按键间隔小于此值，则跳过本次操作。
     */
    private static final long MIN_PRESS_INTERVAL_MS = 100;

    /**
     * 静态初始化块：加载 imm32.dll 库。
     * imm32.dll 是 Windows 输入法管理器库，用于获取输入法窗口句柄和状态。
     */
    static {
        try {
            Native.load("imm32", Imm32.class);
            System.out.println("imm加载成功!!!!!!");
        } catch (Exception e) {
            System.out.println("imm加载失败");
        }
    }

    /**
     * Imm32.dll JNA 接口定义。
     *
     * <p>Imm32.dll 是 Windows 输入法管理器（Input Method Manager）库，
     * 提供以下功能：
     * <ul>
     *   <li>获取默认 IME 窗口句柄</li>
     *   <li>获取和释放输入法上下文</li>
     * </ul>
     */
    public interface Imm32 extends Library {
        /** Imm32.dll 单例实例 */
        Imm32 INSTANCE = (Imm32) Native.load("imm32", Imm32.class);

        /**
         * 获取默认的 IME（Input Method Editor）窗口句柄。
         *
         * @param hWnd 应用程序窗口句柄
         * @return 默认 IME 窗口的句柄，如果失败返回 null
         */
        Pointer ImmGetDefaultIMEWnd(Pointer hWnd);

        /**
         * 获取输入法上下文（Input Context）。
         * 输入法上下文包含当前输入法的状态信息。
         *
         * @param hWnd 应用程序窗口句柄
         * @return 输入法上下文句柄，如果失败返回 null
         */
        Pointer ImmGetContext(HWND hWnd);

        /**
         * 释放输入法上下文。
         * 必须在使用完 ImmGetContext() 后调用，以避免资源泄漏。
         *
         * @param hWnd 应用程序窗口句柄
         * @param hIMC 输入法上下文句柄
         * @return 是否成功释放
         */
        boolean ImmReleaseContext(HWND hWnd, Pointer hIMC);
    }

    /**
     * User32.dll JNA 接口定义。
     *
     * <p>User32.dll 是 Windows 用户界面库，提供以下功能：
     * <ul>
     *   <li>获取前台窗口句柄</li>
     *   <li>发送窗口消息</li>
     *   <li>模拟键盘按键</li>
     * </ul>
     */
    public interface User32 extends StdCallLibrary {
        /** User32.dll 单例实例 */
        User32 INSTANCE = (User32) Native.load("user32", User32.class, W32APIOptions.DEFAULT_OPTIONS);

        /**
         * 获取当前前台（激活）窗口的句柄。
         *
         * @return 当前前台窗口的句柄，如果没有激活的窗口返回 null
         */
        HWND GetForegroundWindow();

        /**
         * 向指定窗口发送消息。
         *
         * @param hWnd 接收消息的窗口句柄
         * @param Msg 消息标识符（如 WM_IME_CONTROL）
         * @param wParam 消息的附加参数
         * @param lParam 消息的附加参数
         * @return 消息处理结果，具体类型视消息而定
         */
        long SendMessage(HWND hWnd, int Msg, long wParam, long lParam);

        /**
         * 模拟键盘按键事件。
         *
         * @param bVk 虚拟键码（如 VK_LSHIFT）
         * @param bScan 扫描码（通常为 0）
         * @param dwFlags 按键标志（0 表示按下，KEYEVENTF_KEYUP 表示释放）
         * @param extraInfo 附加信息（通常为 0）
         */
        void keybd_event(byte bVk, byte bScan, int dwFlags, int extraInfo);
    }

    /** WM_IME_CONTROL 消息标识符，用于控制输入法 */
    private static final int WM_IME_CONTROL = 0x0283;

    /** IMC_GETOPENSTATUS 标志，用于查询输入法是否打开 */
    private static final int IMC_GETOPENSTATUS = 0x0001;

    /**
     * 获取当前输入法模式。
     *
     * @return 如果是英文输入法返回 {@link InputState#ENGLISH}，否则返回 {@link InputState#CHINESE}
     */
    public static InputState getCurrentMode() {
        return isEnglishMode() ? InputState.ENGLISH : InputState.CHINESE;
    }

    /**
     * 检测当前激活窗口是否处于英文输入法模式。
     *
     * <p>检测原理：
     * <ol>
     *   <li>获取当前前台窗口句柄</li>
     *   <li>通过 imm32.dll 获取默认 IME 窗口句柄</li>
     *   <li>向 IME 窗口发送 WM_IME_CONTROL 消息，查询输入法打开状态</li>
     *   <li>如果返回 0，表示输入法未打开（英文模式）</li>
     * </ol>
     *
     * @return 如果是英文输入法模式返回 true，否则返回 false
     */
    public static boolean isEnglishMode() {
        try {
            User32 user32 = User32.INSTANCE;
            Imm32 imm32 = Imm32.INSTANCE;

            // 获取当前前台窗口句柄
            HWND activeWindow = user32.GetForegroundWindow();
            if (activeWindow == null) {
                System.out.println("未找到激活窗口");
                return false;
            }

            // 获取默认 IME 窗口句柄
            Pointer imeWnd = imm32.ImmGetDefaultIMEWnd(activeWindow.getPointer());
            if (imeWnd == null) {
                System.out.println("未找到输入法窗口");
                return true; // 英文输入法可能没有 IME 窗口，返回 true 表示英文模式
            }

            // 发送 WM_IME_CONTROL 消息，查询 IMC_GETOPENSTATUS
            // 返回 0 表示输入法未打开（英文模式），非 0 表示输入法已打开（中文模式）
            long result = user32.SendMessage(new HWND(imeWnd), WM_IME_CONTROL, IMC_GETOPENSTATUS, 0);

            // 释放输入法上下文，避免资源泄漏
            Pointer hIMC = imm32.ImmGetContext(activeWindow);
            if (hIMC != null) {
                imm32.ImmReleaseContext(activeWindow, hIMC);
            }

            return result == 0;
        } catch (Exception e) {
            System.out.println("获取输入法异常");
            return false;
        }
    }

    /**
     * 模拟按下并释放左 Shift 键，切换输入法。
     *
     * <p>Windows 系统中，按下 Shift 键可以切换中英文输入法。
     * 本方法通过 user32.dll 的 keybd_event 函数模拟 Shift 按键事件。
     *
     * <p>防抖机制：
     * <ul>
     *   <li>内置 100ms 最小间隔</li>
     *   <li>如果距离上次按键间隔小于此值，则跳过本次操作</li>
     *   <li>避免在快速移动光标时频繁切换输入法</li>
     * </ul>
     */
    public static void pressShift() {
        long now = System.currentTimeMillis();
        if (now - lastPressTime < MIN_PRESS_INTERVAL_MS) {
            // 距离上次按键间隔小于 100ms，跳过本次操作（防抖）
            return;
        }

        User32 user32 = User32.INSTANCE;

        // 模拟按下左 Shift 键
        user32.keybd_event((byte) VK_LSHIFT, (byte) 0, 0, 0);
        System.out.println("按下 Shift");

        // 模拟释放左 Shift 键
        user32.keybd_event((byte) VK_LSHIFT, (byte) 0, KEYEVENTF_KEYUP, 0);
        System.out.println("释放 Shift");

        // 更新上次按键时间
        lastPressTime = now;
    }
}
