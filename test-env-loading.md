# .env文件加载问题诊断和修复

## 🔍 问题诊断步骤

### 1. 立即重启后端服务
重启后，仔细查看启动日志，寻找以下关键信息：

```
=== Debug: Environment Variables ===
Env check - 1: NAME=glm-4.5v, URL=..., KEY length=44
...
===================================

=== Model Configuration ===
  Model 1: NAME=glm-4.5v, URL=..., KEY=cf4d...
==============================
```

### 2. 检查当前工作目录
在IDEA中运行时，确保：
- 当前工作目录是 `ai-chat-system/backend/`
- `.env`文件位于 `ai-chat-system/.env`

### 3. 验证文件路径
创建一个简单的测试：
```bash
cd d:/code/project/ai-chat-system/ai-chat-system/ai-chat-system/backend
dir ../.env
```

## 🛠️ 修复方案

### 方案1: 复制.env文件（最快）
```bash
# 将.env文件复制到backend目录下
cp ../.env .env
```

### 方案2: 使用IDEA环境变量
在IDEA中设置运行时环境变量：
1. 打开 Run/Debug Configurations
2. 在 Application > AiChatSystemApplication 中
3. 添加 Environment variables：
```
MODEL_1_NAME=glm-4.5v
MODEL_1_URL=https://open.bigmodel.cn/api/paas/v4/chat/completions
MODEL_1_KEY=cf4d75a776814a2d8ba941f4496daa0f.eLMzBdrfOQjZXSLo
```

### 方案3: 使用备用配置
我已经添加了备用配置，如果.env加载失败，会自动使用硬编码的模型配置。

## 🚀 测试验证

重启后端后，访问：
```javascript
// 在浏览器控制台执行
fetch('/api/ai-models').then(r=>r.json()).then(console.log)
```

应该返回包含glm-4.5v模型的数组。

## 📋 常见问题

1. **IDEA工作目录不正确**
   - 确保IDEA的Working directory设置为backend目录

2. **文件编码问题**
   - 确保.env文件使用UTF-8编码

3. **权限问题**
   - 确保Spring Boot进程有读取.env文件的权限

## 🎯 快速修复建议

如果急需使用，建议直接：
1. 复制.env到backend目录
2. 重启服务
3. 测试功能

这样可以立即解决问题，之后再调试路径配置。