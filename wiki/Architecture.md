# 技术架构

## 整体架构

```
┌─────────────────────────────────────────────────────────┐
│                    IntelliJ Platform                      │
├───────────────┬─────────────────────────────────────────┤
│  plugin.xml   │  idea-vim-extension.xml (optional)      │
├───────────────┼─────────────────────────────────────────┤
│ InputMethod   │  VimInputMethod                         │
│ Plugin        │  Plugin                                 │
├───────────────┴─────────────────────────────────────────┤
│               BaseInputMethodDetector                     │
│               VimInputMethodDetector                      │
├─────────────────────────────────────────────────────────┤
│               EditorFocusTracker                          │
├─────────────────────────────────────────────────────────┤
│               CommentUtils (PSI)                          │
├─────────────────────────────────────────────────────────┤
│               InputMethodChecker (JNA)                    │
│               ┌─────────┐  ┌─────────┐                  │
│               │ imm32   │  │ user32  │                  │
│               └─────────┘  └─────────┘                  │
└─────────────────────────────────────────────────────────┘
```

## 入口点

插件有两个入口，通过 `plugin.xml` 和 `idea-vim-extension.xml` 注册：

| 入口 | 条件 | 监听器 |
|------|------|--------|
| `InputMethodPlugin` | IdeaVim 未安装 | `BaseInputMethodDetector` |
| `VimInputMethodPlugin` | IdeaVim 已安装 | `VimInputMethodDetector` |

**IdeaVim 检测方式：** 运行时通过 `Class.forName("com.maddyhome.idea.vim.VimPlugin")` 判断。

## 核心流程

### 光标变化处理流程

```
编辑器光标变化
    ↓
CommentUtils.isInComment()  ← PSI API 判断是否在注释中
    ↓
确定 CursorState:
  - INCODE    → 英文输入法
  - INCOMMENT → 中文输入法
  - OUTIDE    → 中文输入法
    ↓
InputMethodChecker.getCurrentMode()  ← JNA 查询当前输入法状态
    ↓
如果需要切换 → InputMethodChecker.pressShift()  ← 模拟 Shift 按键
```

### 焦点变化处理流程

```
IDE 焦点变化
    ↓
EditorFocusTracker 检测焦点来源
    ↓
如果是编辑器内部焦点变化 → 不操作
    ↓
如果是离开编辑器 → 检查是否需要切换输入法
    ↓
如果是离开 IDE → 切换到中文输入法
```

### IdeaVim 模式变化流程

```
Vim 模式变化
    ↓
VimInputMethodDetector.modeChanged()
    ↓
检查当前 Vim 模式:
  - Normal/Visual/Operator Pending → ISINSERT = false
  - Insert → ISINSERT = true
    ↓
checkAndPrint() → 检查光标位置并切换输入法
```

## 关键组件

### InputMethodChecker

使用 JNA 调用 Windows 原生 API：

- `imm32.dll` — 获取输入法窗口句柄、查询输入法状态
- `user32.dll` — 获取前台窗口、发送消息、模拟按键

**核心方法：**

| 方法 | 功能 |
|------|------|
| `getCurrentMode()` | 获取当前输入法状态（中文/英文） |
| `isEnglishMode()` | 检查是否为英文输入法模式 |
| `pressShift()` | 模拟 Shift 按键切换输入法 |

**防抖机制：** `pressShift()` 内置 100ms 最小间隔，防止频繁切换。

> ⚠️ **注意：** 此类使用静态方法和静态初始化块，不适合重构为非静态模式（会影响性能）。

### CommentUtils

基于 IntelliJ PSI API 检测光标是否处于注释中：

**核心方法：**

| 方法 | 功能 |
|------|------|
| `isInComment(Editor)` | 检查光标是否在注释中 |

**检测逻辑：**
1. 获取光标位置的 PsiElement
2. 检查是否为 PsiComment 实例
3. 向上遍历父元素链
4. 处理边界情况（偏移量越界等）

### VimInputMethodDetector

继承 `BaseInputMethodDetector`，额外实现 IdeaVim 的 `ModeChangeListener`：

**模式映射：**

| Vim 模式 | ISINSERT | 输入法行为 |
|----------|----------|-----------|
| Normal | false | 英文 |
| Visual | false | 英文 |
| Operator Pending | false | 英文 |
| Insert（代码中） | true | 英文 |
| Insert（注释中） | true | 中文 |

### EditorFocusTracker

监听编辑器焦点变化：

**功能：**
- 追踪每个项目的 `MessageBusConnection`
- 在编辑器切换时自动重新绑定焦点监听
- 防止重复注册
- 检测焦点是否在编辑器内部

### BaseInputMethodDetector

基础光标和焦点监听器：

**功能：**
- 监听光标位置变化
- 监听 IDE 焦点变化（激活/ deactivate）
- 检测光标是否在编辑器外部
- 调用 InputMethodChecker 切换输入法

**状态变量：**

| 变量 | 类型 | 说明 |
|------|------|------|
| `cursorState` | `CursorState` | 当前光标状态（静态） |
| `OUTIDEA` | `boolean` | 是否在 IDE 外部 |
| `OUTEDITOR` | `boolean` | 是否在编辑器外部 |
| `ISINSERT` | `boolean` | 是否在 Insert 模式 |

## 数据模型

### CursorState 枚举

```java
public enum CursorState {
    OUTIDE("OUTSIDE", InputState.CHINESE),    // 在 IDE 外
    INCOMMENT("INCOMMENT", InputState.CHINESE), // 在注释中
    INCODE("INCODE", InputState.ENGLISH);      // 在代码中
}
```

### InputState 枚举

```java
public enum InputState {
    ENGLISH,
    CHINESE;
}
```

### InputMethodState 单例

```java
public class InputMethodState {
    private boolean outOfIdea;
    private boolean outOfEditor;
    private boolean isInsertMode;
}
```

## 源码结构

```
src/main/java/
├── starter/
│   ├── InputMethodPlugin.java       # 普通模式入口
│   ├── VimInputMethodPlugin.java    # IdeaVim 模式入口
│   └── StarterUtils.java            # 工厂方法，注册监听器
├── listener/
│   ├── BaseInputMethodDetector.java # 基础光标/焦点监听
│   ├── VimInputMethodDetector.java  # IdeaVim 模式监听
│   └── EditorFocusTracker.java      # 编辑器焦点追踪
├── inputmethod/
│   └── InputMethodChecker.java      # Windows 输入法检测 (JNA)
├── enums/
│   ├── CursorState.java             # 光标状态枚举
│   ├── InputState.java              # 输入法状态枚举
│   └── InputMethodState.java        # 输入法全局状态（单例）
└── utils/
    └── CommentUtils.java            # 注释检测工具 (PSI)
```

## 依赖关系

```
InputMethodPlugin / VimInputMethodPlugin
    ↓
StarterUtils
    ↓
BaseInputMethodDetector / VimInputMethodDetector
    ↓
CommentUtils (PSI)
InputMethodChecker (JNA)
EditorFocusTracker
```

## 线程模型

- 光标变化事件在 EDT（Event Dispatch Thread）中处理
- 使用 `ApplicationManager.getApplication().invokeLater()` 确保在 EDT 中执行
- 使用 `WriteCommandAction.runWriteCommandAction()` 执行 PSI 操作
- JNA 调用在当前线程中执行（Windows API 调用）

## 性能考虑

1. **防抖机制：** 100ms 最小间隔避免频繁切换
2. **PSI 缓存：** 使用 `PsiDocumentManager.commitAllDocuments()` 确保 PSI 树最新
3. **焦点追踪：** 使用 `ConcurrentHashMap` 追踪项目连接，避免内存泄漏
4. **静态方法：** InputMethodChecker 使用静态方法，避免对象创建开销
