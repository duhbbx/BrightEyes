# BrightEyes 明眸

<p align="center">
  <img src="docs/logo.png" alt="BrightEyes Logo" width="120" height="120">
</p>

<p align="center">
  <strong>一款专为儿童弱视治疗设计的开源 Android 应用</strong>
  <br>
  <strong>献给乐乐，以及所有需要帮助的孩子们</strong>
</p>

<p align="center">
  <a href="#项目故事">项目故事</a> •
  <a href="#功能特性">功能特性</a> •
  <a href="#快速开始">快速开始</a> •
  <a href="#贡献指南">贡献指南</a> •
  <a href="#许可证">许可证</a> •
  <a href="README_EN.md">English</a>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-green.svg" alt="Platform">
  <img src="https://img.shields.io/badge/API-24%2B-brightgreen.svg" alt="API">
  <img src="https://img.shields.io/badge/Language-Java-orange.svg" alt="Language">
  <img src="https://img.shields.io/badge/License-Apache%202.0-blue.svg" alt="License">
</p>

---

## 项目故事

> **为什么要做这个项目？**

我的儿子乐乐（Lele）在 1 岁多的时候被发现患有斜视和弱视。作为父亲，我带着他跑遍了各大医院，医生建议除了佩戴矫正眼镜外，还需要配合视觉训练来帮助改善视力。

然而，当我去了解市面上的弱视训练软件时，发现**商用软件的价格让普通家庭难以承受**——动辄几千甚至上万元的费用，对于很多和我一样的家庭来说是一笔不小的负担。

我是一名程序员。我想，为什么不自己开发一个呢？

于是，**BrightEyes（明眸）** 诞生了。

这个项目最初是为乐乐开发的，但我希望它能帮助到更多有同样困扰的家庭。每一个孩子都值得拥有明亮的眼睛，**医疗不应该成为少数人的特权**。

我选择将这个项目开源，希望：
- 让更多家庭能够免费使用科学的弱视训练工具
- 让更多开发者参与进来，一起完善这个项目
- 让这份父爱能够传递给更多需要帮助的孩子

**如果这个项目对你有帮助，请给一个 Star，让更多人看到它。**

---

## 简介

**BrightEyes（明眸）** 是一款开源的儿童弱视训练应用，通过科学的视觉刺激训练帮助弱视儿童改善视力。应用提供多种训练模式，记录训练数据，帮助家长和孩子坚持每日训练。

> **声明**：本应用仅作为辅助训练工具，不能替代专业医疗诊断和治疗。使用前请咨询眼科医生。

## 功能特性

### 训练模块

| 训练类型 | 描述 | 原理 |
|---------|------|------|
| 红光闪烁 | 红色光点闪烁刺激 | 增强视网膜敏感度 |
| 光栅训练 | 条纹图案视觉刺激 | 刺激视觉皮层发育 |
| 追踪训练 | 追踪移动目标 | 锻炼眼肌协调能力 |
| 聚焦训练 | 远近焦点切换 | 提升调节能力 |
| 色彩识别 | 颜色辨认游戏 | 增强色彩辨识力 |
| 形状配对 | 图形匹配记忆 | 提高图形识别能力 |

### 核心功能

- 多种科学训练模式
- 训练时长与得分记录
- 每日训练目标设定
- 连续训练天数统计
- 训练历史数据查看
- 用户档案管理

## 截图预览

| 主页 | 训练 | 设置 |
|:---:|:---:|:---:|
| ![主页](docs/screenshots/home.png) | ![训练](docs/screenshots/training.png) | ![设置](docs/screenshots/settings.png) |

## 快速开始

### 环境要求

- Android Studio Hedgehog (2023.1.1) 或更高版本
- JDK 17
- Android SDK 34
- Gradle 8.2

### 构建步骤

1. **克隆仓库**

```bash
git clone https://github.com/duhbbx/BrightEyes.git
cd BrightEyes
```

2. **打开项目**

使用 Android Studio 打开项目根目录

3. **同步 Gradle**

等待 Android Studio 自动同步 Gradle 依赖

4. **运行应用**

连接 Android 设备或启动模拟器，点击 Run 按钮

### 最低系统要求

- Android 7.0 (API 24) 及以上

> 📖 **详细运行指南**：如果遇到问题，请查看 [完整运行指南](docs/RUNNING_GUIDE.md)

## 项目结构

```
BrightEyes/
├── app/
│   ├── src/main/
│   │   ├── java/com/brighteyes/app/
│   │   │   ├── BrightEyesApp.java       # Application 入口
│   │   │   ├── database/                 # Room 数据库
│   │   │   ├── model/                    # 数据模型
│   │   │   ├── repository/               # Repository 层
│   │   │   ├── ui/                       # UI 层
│   │   │   ├── viewmodel/                # ViewModel 层
│   │   │   └── utils/                    # 工具类
│   │   ├── res/                          # 资源文件
│   │   └── AndroidManifest.xml
│   └── build.gradle
├── gradle/
├── build.gradle
├── settings.gradle
└── LICENSE
```

## 技术栈

- **架构模式**: MVVM (Model-View-ViewModel)
- **数据库**: Room
- **异步处理**: LiveData
- **UI 组件**: Material Design Components
- **导航**: AndroidX Navigation
- **图片加载**: Glide

## 贡献指南

我们欢迎所有形式的贡献！详情请查看 [CONTRIBUTING.md](CONTRIBUTING.md)

### 如何贡献

1. **Fork** 本仓库
2. 创建你的特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交你的更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启一个 **Pull Request**

### 特别欢迎

- 眼科医生或视光师提供专业建议
- UI/UX 设计师改进儿童友好界面
- 开发者完善训练模块
- 翻译者帮助国际化

## 路线图

- [ ] 完善各训练模块的具体实现
- [ ] 添加训练数据统计图表
- [ ] 支持多用户切换（多个孩子）
- [ ] 添加训练提醒功能
- [ ] 支持训练数据导出
- [ ] 添加家长监控模式
- [ ] iOS 版本
- [ ] 更多语言支持

## 常见问题

<details>
<summary>应用适合多大年龄的孩子？</summary>

本应用主要针对 3-12 岁的弱视儿童设计，具体训练方案请咨询眼科医生。
</details>

<details>
<summary>每天应该训练多长时间？</summary>

建议每天训练 15-30 分钟，分 2-3 次进行。具体时长请遵医嘱。
</details>

<details>
<summary>训练数据会上传到服务器吗？</summary>

不会。所有数据都保存在本地设备上，我们重视用户隐私。
</details>

<details>
<summary>这个应用能治好弱视吗？</summary>

本应用是辅助训练工具，需要配合专业医疗方案使用。弱视治疗是一个长期过程，需要坚持训练并定期复查。
</details>

## 致谢

- 献给我的儿子乐乐，你是爸爸的动力
- 感谢所有为弱视儿童康复事业做出贡献的医疗工作者
- 感谢所有开源项目的贡献者
- 感谢每一位给这个项目 Star 的朋友

## 联系我们

- 提交 Issue: [GitHub Issues](https://github.com/duhbbx/BrightEyes/issues)
- 邮箱: duhbbx@gmail.com

## 支持项目

如果这个项目帮助到了你：

- 给项目一个 **Star**
- 分享给有需要的家庭
- 提交 Issue 或 PR 帮助改进
- 告诉我们你的故事

## 许可证

本项目采用 Apache License 2.0 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

```
Copyright 2024 BrightEyes Contributors

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0
```

---

<p align="center">
  <strong>每个孩子都值得拥有明亮的眼睛</strong>
  <br>
  <sub>Made with love for Lele and all children who need help</sub>
</p>
