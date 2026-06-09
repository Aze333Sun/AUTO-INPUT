# AUTO-INPUT-CH-EN

根据光标位置自动切换中英文输入法的 JetBrains IDE 插件。

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
[![Platform](https://img.shields.io/badge/Platform-Windows-blue.svg)](https://github.com/lin-uice/AUTO-INPUT-CH-EN)
[![IDE](https://img.shields.io/badge/IDE-IntelliJ%202024.3.3+-orange.svg)](https://www.jetbrains.com/idea/)

## 目录

| 页面 | 说明 |
|------|------|
| [安装指南](Installation.md) | 下载与安装步骤 |
| [使用说明](Usage.md) | 各场景下的输入法切换规则 |
| [支持的 IDE 与语言](Supported-IDEs.md) | 兼容的 IDE 和编程语言 |
| [技术架构](Architecture.md) | 插件内部实现原理 |
| [开发指南](Development.md) | 本地构建与调试 |
| [常见问题](FAQ.md) | 故障排查 |
| [更新日志](Changelog.md) | 版本历史 |

## 功能特性

### 核心功能

- **代码区域** → 自动切换为英文输入法
- **注释区域** → 自动切换为中文输入法
- **工具窗口** → 自动切换为英文输入法
- **离开 IDE** → 自动切换为中文输入法

### IdeaVim 支持

- Normal / Visual / Operator Pending 模式 → 英文输入法
- Insert 模式 → 根据光标位置自动切换中英文

### 支持的输入法

| 输入法 | 支持状态 |
|--------|---------|
| 微软拼音 | ✅ |
| 搜狗输入法 | ✅ |
| 百度输入法 | ✅ |
| 微信输入法 | ❌ |

## 系统要求

- **操作系统：** Windows（仅支持）
- **IDE 版本：** IntelliJ IDEA 2024.3.3+ 或其他 JetBrains IDE
- **JDK：** 17 或 21（运行时由 IDE 提供）

## 快速开始

1. 从 [JetBrains 插件市场](https://plugins.jetbrains.com/plugin/AUTO-INPUT-CH-EN) 安装
2. 重启 IDE
3. 手动切换一次输入法，让插件学习默认状态
4. 之后插件会根据光标位置自动切换

## 许可证

本项目采用 [GPL-3.0](LICENSE) 许可证。
