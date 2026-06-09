# 支持的 IDE 与语言

## JetBrains IDE

插件严格遵循 JetBrains Platform API 编写，理论支持所有 JetBrains IDE：

| IDE | 支持状态 | 说明 |
|-----|---------|------|
| IntelliJ IDEA | ✅ | 主要测试环境 |
| PyCharm | ✅ | Python 开发 |
| WebStorm | ✅ | JavaScript/TypeScript 开发 |
| GoLand | ✅ | Go 开发 |
| CLion | ✅ | C/C++ 开发 |
| PhpStorm | ✅ | PHP 开发 |
| Rider | ✅ | .NET 开发 |
| RubyMine | ✅ | Ruby 开发 |
| DataGrip | ✅ | 数据库开发 |
| Android Studio | ✅ | Android 开发 |
| Fleet | ✅ | 轻量级 IDE |

### 版本要求

- **最低版本：** IntelliJ Platform 2022.3+
- **构建号范围：** `sinceBuild=223.*`，`untilBuild=253.*`

### IdeaVim 兼容性

- IdeaVim 支持版本：2022.3+（与 IDE 版本一致）
- IdeaVim 为可选依赖，运行时通过 `Class.forName()` 检测

### 兼容性说明

- 插件使用 IntelliJ Platform API，因此只要 IDE 基于 IntelliJ Platform，就支持
- 不支持非 IntelliJ 平台的 IDE（如 RustRover 早期版本）

## 编程语言

插件通过 IntelliJ PSI API 检测注释，因此只要 IDE 能解析该语言的语法，插件就能正确识别注释区域。

### 官方支持的语言

| 语言 | 支持状态 |
|------|---------|
| Java | ✅ |
| Kotlin | ✅ |
| Python | ✅ |
| JavaScript | ✅ |
| TypeScript | ✅ |
| C | ✅ |
| C++ | ✅ |
| Go | ✅ |
| Rust | ✅ |
| Ruby | ✅ |
| PHP | ✅ |
| Swift | ✅ |
| Scala | ✅ |
| Perl | ✅ |
| Lua | ✅ |
| SQL | ✅ |
| CSS | ✅ |
| HTML | ✅ |
| XML | ✅ |
| YAML | ✅ |
| Shell | ✅ |

### 其他语言

只要 IDE 能解析语法并识别注释，插件就能工作。例如：
- Groovy
- Kotlin Script
- Markdown（在 IDE 中编辑时）
- JSON
- properties 文件

## 输入法

### 支持的输入法

| 输入法 | 支持状态 | 说明 |
|--------|---------|------|
| 微软拼音 | ✅ | Windows 自带，完全支持 |
| 搜狗输入法 | ✅ | 第三方输入法，完全支持 |
| 百度输入法 | ✅ | 第三方输入法，完全支持 |

### 不支持的输入法

| 输入法 | 支持状态 | 原因 |
|--------|---------|------|
| 微信输入法 | ❌ | 不兼容标准 IME 接口 |

### 输入法检测原理

插件通过以下 Windows API 检测输入法状态：

1. `imm32.dll` — 获取输入法窗口句柄
2. `user32.dll` — 发送 `WM_IME_CONTROL` 消息查询输入法状态
3. `user32.dll` — 模拟 Shift 按键切换输入法

> **注意：** 如果输入法不兼容标准 IME 接口，可能无法检测或切换。

## 平台支持

| 平台 | 支持状态 | 说明 |
|------|---------|------|
| Windows | ✅ | 完全支持 |
| Linux | ❌ | 计划中 |
| macOS | ❌ | 暂无计划 |

### Windows 版本要求

- Windows 10 或更高版本
- 需要支持 IMM32 API

### Linux 支持计划

- 未来版本可能添加 Linux 支持
- 需要使用不同的输入法检测方式（ibus/fcitx）

### macOS 支持计划

- 由于开发者没有 Mac，暂无 macOS 支持计划
- 如果需要 macOS 支持，欢迎贡献代码
