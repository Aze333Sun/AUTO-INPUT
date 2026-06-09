# 安装指南

## 前置条件

| 条件 | 要求 |
|------|------|
| 操作系统 | Windows |
| IDE 版本 | IntelliJ IDEA 2024.3.3+ 或其他 JetBrains IDE |
| JDK | 17 或 21（运行时由 IDE 提供） |

## 安装方式

### 方式一：从 JetBrains 插件市场安装（推荐）

1. 打开 IDE
2. 进入 `File` → `Settings` → `Plugins` → `Marketplace`
3. 搜索 **AUTO-INPUT-CH-EN**
4. 点击 `Install`
5. 重启 IDE

### 方式二：从 GitHub Releases 安装

1. 前往 [Releases 页面](https://github.com/lin-uice/AUTO-INPUT-CH-EN/releases)
2. 下载最新版本的 `.zip` 文件
3. 打开 IDE → `File` → `Settings` → `Plugins`
4. 点击 ⚙️ 图标 → `Install Plugin from Disk...`
5. 选择下载的 `.zip` 文件
6. 重启 IDE

### 方式三：本地构建安装

#### 前置要求

- JDK 17 或 21
- Maven 3.6+

#### 构建步骤

```bash
# 克隆仓库
git clone https://github.com/lin-uice/AUTO-INPUT-CH-EN.git
cd AUTO-INPUT-CH-EN

# 构建插件
mvn package
```

#### 安装步骤

1. 构建完成后，在 `target/` 目录下找到 `.zip` 文件
2. 打开 IDE → `File` → `Settings` → `Plugins`
3. 点击 ⚙️ 图标 → `Install Plugin from Disk...`
4. 选择 `target/` 目录下的 `.zip` 文件
5. 重启 IDE

## 安装后配置

### 首次使用

1. **重启 IDE**（必须）
2. 打开任意代码文件
3. 手动切换一次中英文输入法，让插件学习当前窗口的默认输入状态
4. 之后插件会根据光标位置自动切换

### 验证安装

1. 在代码区域点击 → 应自动切换到英文输入法
2. 在注释区域点击 → 应自动切换到中文输入法
3. 鼠标离开 IDE 窗口 → 应自动切换到中文输入法

## 卸载

1. 打开 IDE → `File` → `Settings` → `Plugins`
2. 找到 **AUTO-INPUT-CH-EN**
3. 点击 `Uninstall`
4. 重启 IDE

## 常见安装问题

### 插件安装后没有生效

- 确保已重启 IDE
- 检查插件是否在 `Installed` 列表中显示为启用状态

### 安装时提示版本不兼容

- 确保 IDE 版本为 2024.3.3 或更高
- 检查 IDE 构建号是否在支持范围内（`sinceBuild=243.3`，`untilBuild=253.*`）

### 本地构建失败

- 确保已安装 JDK 17 或 21
- 确保已安装 Maven 3.6+
- 检查网络连接（需要下载 IntelliJ Platform 依赖）
