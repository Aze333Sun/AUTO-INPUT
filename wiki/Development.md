# 开发指南

## 环境准备

### 必需工具

| 工具 | 版本要求 | 说明 |
|------|---------|------|
| JDK | 17 或 21 | 编译和运行 |
| Maven | 3.6+ | 构建工具 |
| IntelliJ IDEA | 2024.3.3+ | 开发 IDE |

### 可选工具

| 工具 | 用途 |
|------|------|
| Git | 版本控制 |
| Docker | 运行 Qodana 代码分析 |

## 项目结构

```
AUTO-INPUT-CH-EN/
├── pom.xml                          # Maven 配置
├── src/main/java/                   # Java 源码
│   ├── starter/                     # 插件入口
│   ├── listener/                    # 监听器
│   ├── inputmethod/                 # 输入法检测
│   ├── enums/                       # 枚举类型
│   └── utils/                       # 工具类
├── src/main/resources/              # 资源文件
│   └── META-INF/
│       ├── plugin.xml               # 插件配置
│       └── idea-vim-extension.xml   # IdeaVim 扩展
├── .run/                            # IntelliJ Run Configuration
├── qodana.yaml                      # Qodana 配置
├── AGENTS.md                        # 代理指南
└── wiki/                            # 项目文档
```

## 构建命令

### 常用命令

```bash
# 编译项目
mvn compile

# 构建插件 JAR
mvn package

# 清理并构建
mvn clean package

# 在沙盒 IDE 中运行调试
mvn intellij-platform:run-ide

# 验证插件
mvn intellij-platform:verify-plugin
```

### Maven 生命周期

```bash
# 验证项目是否正确
mvn validate

# 编译源码
mvn compile

# 运行测试（当前无测试）
mvn test

# 打包
mvn package

# 安装到本地仓库
mvn install

# 部署到远程仓库
mvn deploy
```

## 调试方法

### 方法一：使用 IntelliJ IDEA（推荐）

1. 打开项目
2. 使用预配置的 Run Configuration：`.run/Run IDE with Plugin.run.xml`
3. 点击 Run，会启动一个带插件的沙盒 IDE
4. 在沙盒 IDE 中操作，断点会命中

### 方法二：使用 Maven 命令

```bash
# 在沙盒 IDE 中运行
mvn intellij-platform:run-ide

# 调试模式运行
mvn intellij-platform:debug
```

### 方法三：远程调试

1. 启动沙盒 IDE 并开启远程调试端口
2. 在 IntelliJ IDEA 中配置远程调试
3. 连接到调试端口

## 验证插件功能

### 基本功能测试

1. 在沙盒 IDE 中打开任意代码文件
2. 点击注释区域 → 应自动切换到中文输入法
3. 点击代码区域 → 应自动切换到英文输入法
4. 切换到 IdeaVim Normal 模式 → 应为英文输入法
5. 切换到 IdeaVim Insert 模式 → 应根据光标位置自动切换

### 边界情况测试

1. 在空文件中点击 → 应使用英文输入法
2. 在文件开头/结尾点击 → 应正确处理
3. 快速移动光标 → 应有防抖效果
4. 切换编辑器标签页 → 应根据新光标位置切换
5. 离开 IDE 后返回 → 应恢复之前的输入法状态

### IdeaVim 测试

1. 确保 IdeaVIM 插件已安装
2. 在 Normal 模式下移动光标 → 应为英文输入法
3. 按 `i` 进入 Insert 模式 → 应根据光标位置切换
4. 按 `Esc` 返回 Normal 模式 → 应为英文输入法
5. 在 Visual 模式下选择文本 → 应为英文输入法

## 代码风格

### 编码规范

- Java 源码，无 Kotlin 代码
- 日志输出使用 `System.out.println()`（非标准，后续可改为 IntelliJ Logger）
- 注释语言：中文
- 包结构扁平，按职责分包

### 包结构

| 包名 | 职责 |
|------|------|
| `starter` | 插件入口和工厂方法 |
| `listener` | 光标、焦点、模式监听器 |
| `inputmethod` | Windows 输入法检测 |
| `enums` | 枚举类型定义 |
| `utils` | 工具类 |

### 命名规范

- 类名：大驼峰（`InputMethodChecker`）
- 方法名：小驼峰（`isEnglishMode`）
- 常量：全大写下划线（`MIN_PRESS_INTERVAL_MS`）
- 包名：全小写（`inputmethod`）

## 常见开发任务

### 添加新的输入法支持

1. 在 `InputMethodChecker` 中添加新的输入法检测逻辑
2. 如果需要，添加新的 JNA 接口
3. 更新 `isEnglishMode()` 方法
4. 测试新输入法的兼容性

### 添加新的场景

1. 在 `CursorState` 枚举中添加新状态
2. 在 `BaseInputMethodDetector.check()` 和 `VimInputMethodDetector.check()` 中添加判断逻辑
3. 设置新状态对应的默认输入法语言
4. 更新文档

### 修改切换行为

1. 修改 `InputMethodChecker.pressShift()` 方法
2. 调整 `MIN_PRESS_INTERVAL_MS` 值（当前 100ms）
3. 测试切换效果

### 添加新的 IDE 支持

1. 确保使用标准 IntelliJ Platform API
2. 测试在目标 IDE 中的功能
3. 更新 `pom.xml` 中的 `untilBuild`（如果需要）

## 代码分析

### Qodana

项目配置了 Qodana（`qodana.yaml`），使用 `qodana-jvm:2025.1` linter，JDK 21。

```bash
# 本地运行 Qodana（需要 Docker）
docker run --rm \
  -v $(pwd):/data/project \
  -v $(pwd)/qodana.yaml:/data/config/qodana.yaml \
  jetbrains/qodana-jvm:2025.1
```

### 代码质量检查

```bash
# 编译时检查
mvn compile

# 打包时检查
mvn package

# 运行所有检查
mvn verify
```

## 调试技巧

### 查看日志

插件日志输出到 IDE 的 stdout。在沙盒 IDE 中运行时，日志位于：
```
target/idea-sandbox/system/log/idea.log
```

搜索关键字：
- `现在是普通模式` — 普通模式启动
- `现在无vim模式` — IdeaVim 未检测到
- `按下 Shift` — 输入法切换
- `imm加载成功` — JNA 库加载成功

### 常见问题排查

1. **插件未加载：** 检查 `plugin.xml` 配置
2. **输入法未切换：** 检查 `InputMethodChecker` 日志
3. **IdeaVim 未检测：** 检查 `Class.forName()` 是否成功
4. **JNA 加载失败：** 检查 `imm32.dll` 和 `user32.dll` 是否可用

## 发布流程

### 版本发布

1. 更新 `pom.xml` 中的版本号
2. 更新 `plugin.xml` 中的 `changeNotes`
3. 构建插件：`mvn clean package`
4. 测试插件功能
5. 提交代码并创建 Release

### 插件发布

1. 构建插件：`mvn clean package`
2. 登录 [JetBrains Plugin Portal](https://plugins.jetbrains.com/)
3. 上传插件 JAR
4. 填写版本信息和更新说明
5. 提交审核
