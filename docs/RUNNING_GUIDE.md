# BrightEyes 运行指南

本文档详细介绍如何在 Android Studio 中运行 BrightEyes 项目。

## 目录

- [环境准备](#环境准备)
- [打开项目](#打开项目)
- [Gradle 同步](#gradle-同步)
- [运行应用](#运行应用)
  - [使用模拟器](#使用模拟器)
  - [使用真机](#使用真机)
- [常见问题](#常见问题)
- [国内镜像配置](#国内镜像配置)

---

## 环境准备

### 1. 安装 Android Studio

1. 访问 [Android Studio 官网](https://developer.android.com/studio)
2. 下载最新稳定版（推荐 Hedgehog 2023.1.1 或更高版本）
3. 安装并完成初始化设置

### 2. 安装 JDK 17

Android Studio 通常自带 JDK，如需单独安装：

**macOS (Homebrew):**
```bash
brew install openjdk@17
```

**Windows:**
- 下载 [Oracle JDK 17](https://www.oracle.com/java/technologies/downloads/#java17) 或 [OpenJDK 17](https://adoptium.net/)

### 3. 安装 Android SDK

首次启动 Android Studio 会引导安装 SDK，确保安装：
- Android SDK Platform 34
- Android SDK Build-Tools 34
- Android Emulator（如需使用模拟器）

---

## 打开项目

### 方式一：从欢迎界面

1. 启动 Android Studio
2. 在欢迎界面点击 **Open**
3. 导航到项目目录：`/Users/a9/Projects/BrightEyes`
4. 点击 **Open**

### 方式二：从菜单

1. 点击菜单 **File → Open...**
2. 选择项目目录
3. 点击 **OK**

### 方式三：从终端

```bash
# macOS
open -a "Android Studio" /Users/a9/Projects/BrightEyes

# 或者使用命令行工具（需先配置）
studio /Users/a9/Projects/BrightEyes
```

---

## Gradle 同步

### 自动同步

首次打开项目时，Android Studio 会自动开始 Gradle 同步：

1. 右下角会显示同步进度条
2. 等待显示 "BUILD SUCCESSFUL" 或 "Gradle sync finished"
3. 首次同步可能需要 5-15 分钟（取决于网络速度）

### 手动同步

如果需要手动触发同步：

- 点击工具栏的 **Sync Project with Gradle Files** 按钮（大象图标）
- 或点击菜单 **File → Sync Project with Gradle Files**

### 同步过程中会下载

- Gradle 8.2
- Android Gradle Plugin 8.2.0
- AndroidX 依赖库
- Room、Glide 等第三方库

---

## 运行应用

### 使用模拟器

#### 创建模拟器

1. 点击菜单 **Tools → Device Manager**（或点击工具栏的手机图标）
2. 点击 **Create Device**
3. 选择设备类型：
   - 推荐选择 **Pixel 6** 或 **Pixel 7**
   - 点击 **Next**
4. 选择系统镜像：
   - 推荐选择 **API 34 (UpsideDownCake)** 或 **API 33 (Tiramisu)**
   - 如果未下载，点击 **Download** 下载镜像
   - 点击 **Next**
5. 配置模拟器：
   - AVD Name：可自定义名称
   - 其他选项保持默认即可
   - 点击 **Finish**

#### 启动模拟器

1. 在 Device Manager 中找到创建的模拟器
2. 点击 **▶️** 按钮启动
3. 等待模拟器完全启动（首次可能需要 1-2 分钟）

### 使用真机

#### 准备工作

1. **开启开发者选项**
   - 进入手机 **设置 → 关于手机**
   - 连续点击 **版本号** 7 次
   - 提示"您已处于开发者模式"

2. **开启 USB 调试**
   - 进入 **设置 → 系统 → 开发者选项**
   - 开启 **USB 调试**

3. **连接电脑**
   - 使用数据线连接手机和电脑
   - 手机上弹出提示时，点击 **允许 USB 调试**
   - 建议勾选"始终允许"

#### 验证连接

在终端运行：
```bash
adb devices
```

应显示类似：
```
List of devices attached
XXXXXXXX    device
```

### 运行项目

1. **选择设备**
   - 在工具栏的设备下拉菜单中选择模拟器或真机

2. **选择运行配置**
   - 确保选择的是 **app**

3. **点击运行**
   - 点击绿色 **▶️ Run** 按钮
   - 或使用快捷键：
     - macOS: `⌃R` 或 `Control + R`
     - Windows/Linux: `Shift + F10`

4. **等待安装**
   - 首次编译可能需要 1-3 分钟
   - 编译完成后会自动安装到设备并启动

---

## 常见问题

### Gradle 同步失败

#### 问题：下载依赖超时

**解决方案：** 配置国内镜像（见下一节）

#### 问题：Gradle 版本不兼容

**解决方案：**
1. 点击提示中的 **Update** 或 **Fix**
2. 或手动修改 `gradle/wrapper/gradle-wrapper.properties`

### SDK 相关问题

#### 问题：找不到 SDK

**解决方案：**
1. 点击菜单 **File → Project Structure**
2. 选择 **SDK Location**
3. 设置正确的 Android SDK 路径

#### 问题：缺少 SDK Platform

**解决方案：**
1. 点击菜单 **Tools → SDK Manager**
2. 在 **SDK Platforms** 标签页勾选需要的版本
3. 点击 **Apply** 下载

### JDK 相关问题

#### 问题：JDK 版本不正确

**解决方案：**
1. 点击菜单 **File → Project Structure**
2. 选择 **SDK Location**
3. 在 **JDK location** 中选择 JDK 17

### 运行相关问题

#### 问题：找不到设备

**解决方案：**
- 模拟器：检查模拟器是否已启动
- 真机：检查 USB 调试是否开启，重新插拔数据线

#### 问题：安装失败

**解决方案：**
1. 检查手机存储空间
2. 卸载旧版本应用后重试
3. 检查是否有安装权限限制

#### 问题：应用闪退

**解决方案：**
1. 查看 Logcat 日志（View → Tool Windows → Logcat）
2. 过滤 `com.brighteyes.app` 查看错误信息
3. 根据错误信息排查问题

---

## 国内镜像配置

如果 Gradle 同步很慢或失败，可以配置国内镜像。

### 方法一：修改项目配置

编辑项目根目录的 `settings.gradle`：

```groovy
pluginManagement {
    repositories {
        maven { url 'https://maven.aliyun.com/repository/gradle-plugin' }
        maven { url 'https://maven.aliyun.com/repository/google' }
        maven { url 'https://maven.aliyun.com/repository/public' }
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven { url 'https://maven.aliyun.com/repository/google' }
        maven { url 'https://maven.aliyun.com/repository/public' }
        google()
        mavenCentral()
    }
}
```

### 方法二：全局配置

创建或编辑 `~/.gradle/init.gradle`：

```groovy
allprojects {
    repositories {
        maven { url 'https://maven.aliyun.com/repository/public' }
        maven { url 'https://maven.aliyun.com/repository/google' }
        maven { url 'https://maven.aliyun.com/repository/gradle-plugin' }
        mavenCentral()
        google()
    }
}
```

---

## 快捷键参考

| 操作 | macOS | Windows/Linux |
|-----|-------|---------------|
| 运行 | `⌃R` | `Shift + F10` |
| 调试 | `⌃D` | `Shift + F9` |
| 停止 | `⌘F2` | `Ctrl + F2` |
| 同步 Gradle | `⌘⇧I` | `Ctrl + Shift + O` |
| 重新构建 | `⌘⇧F9` | `Ctrl + Shift + F9` |
| 清理项目 | 菜单 Build → Clean | 菜单 Build → Clean |

---

## 获取帮助

如果遇到本文档未涵盖的问题：

1. 查看 [Android 开发者文档](https://developer.android.com/docs)
2. 在项目 [GitHub Issues](https://github.com/duhbbx/BrightEyes/issues) 提问
3. 发送邮件至 duhbbx@gmail.com

---

<p align="center">
  <sub>祝你开发顺利！</sub>
</p>
