# 贡献指南

感谢你对 BrightEyes 项目的关注！我们欢迎所有形式的贡献。

## 如何贡献

### 报告 Bug

如果你发现了 Bug，请通过 GitHub Issues 报告，包含以下信息：

1. **Bug 描述**：简要描述问题
2. **复现步骤**：详细的复现步骤
3. **期望行为**：你期望发生什么
4. **实际行为**：实际发生了什么
5. **环境信息**：
   - 设备型号
   - Android 版本
   - 应用版本

### 提出功能建议

我们欢迎新功能的建议！请在 Issues 中描述：

1. 功能的具体内容
2. 为什么需要这个功能
3. 可能的实现方式（可选）

### 提交代码

1. Fork 本仓库
2. 创建特性分支：`git checkout -b feature/your-feature-name`
3. 编写代码并测试
4. 提交更改：`git commit -m 'Add: your feature description'`
5. 推送分支：`git push origin feature/your-feature-name`
6. 创建 Pull Request

## 代码规范

### Java 代码规范

- 遵循 [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- 类名使用 PascalCase：`MainActivity`
- 方法名和变量名使用 camelCase：`getUserName()`
- 常量使用 UPPER_SNAKE_CASE：`MAX_RETRY_COUNT`
- 资源 ID 使用 snake_case：`btn_start_training`

### 提交信息规范

使用以下前缀：

- `Add:` 新增功能
- `Fix:` 修复 Bug
- `Update:` 更新功能
- `Refactor:` 代码重构
- `Docs:` 文档更新
- `Style:` 代码格式调整
- `Test:` 测试相关

示例：
```
Add: 训练数据导出功能
Fix: 修复训练计时器在后台暂停的问题
Docs: 更新 README 安装说明
```

### 分支命名

- 功能分支：`feature/feature-name`
- Bug 修复：`fix/bug-description`
- 文档更新：`docs/description`

## 开发环境设置

1. 安装 Android Studio (最新稳定版)
2. 安装 JDK 17
3. Clone 仓库并导入 Android Studio
4. 等待 Gradle 同步完成

## Pull Request 检查清单

提交 PR 前请确认：

- [ ] 代码可以正常编译
- [ ] 遵循代码规范
- [ ] 添加了必要的注释
- [ ] 更新了相关文档（如需要）
- [ ] 测试通过

## 行为准则

- 尊重所有贡献者
- 保持友善和专业的沟通
- 接受建设性的批评
- 关注项目的最佳利益

## 联系方式

如有问题，可以通过以下方式联系：

- GitHub Issues
- 邮箱：duhbbx@gmail.com

再次感谢你的贡献！
