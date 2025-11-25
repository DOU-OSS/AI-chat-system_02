# 模型403错误调试指南

## 问题分析
你遇到的 "Model glm-4 not available" 错误是因为：

1. **前端硬编码了不存在的模型名称** - `glm-4` 在 .env 中没有配置
2. **前端设置了错误的默认模型** - 改为动态选择第一个可用模型
3. **模型验证逻辑问题** - 需要确保模型正确加载

## 已修复的问题

### 1. 前端默认模型设置
```javascript
// 修改前：硬编码不存在的模型
const selectedModel = ref('glm-4')

// 修改后：动态选择
const selectedModel = ref('')
```

### 2. 增加调试日志
- 在 AIModelService 中添加了详细的模型加载日志
- 在 DotenvConfig 中添加了所有MODEL配置的打印
- 设置了DEBUG日志级别

## 调试步骤

### 1. 重启后端服务
查看启动日志，确认：
```
=== Model Configuration ===
  Model 1: NAME=glm-4.5v, URL=..., KEY=cf4d...
  Model 2: NAME=glm-4.5-air, URL=..., KEY=cf4d...
  ...
==============================
```

### 2. 检查前端API调用
打开浏览器开发者工具，查看：
- `/api/ai-models` 请求返回的模型列表
- 确认返回的模型ID与.env中配置的一致

### 3. 验证模型选择
在前端界面：
- 模型下拉框应该显示已配置的模型
- 选择模型后应该能正常发送消息

## 可能的问题

### .env文件未正确加载
- 检查 .env 文件位置（应在项目根目录）
- 确认文件名正确（不是 .env.txt 等）

### 模型配置格式错误
- 确认 MODEL_X_NAME, MODEL_X_URL, MODEL_X_KEY 都配置了
- 检查是否有额外的空格或特殊字符

### API密钥无效
- 测试API密钥是否有效
- 检查API端点URL是否正确

## 快速测试

1. 重启后端，查看启动日志
2. 打开前端 http://localhost:5173
3. 在浏览器控制台执行：
```javascript
fetch('/api/ai-models').then(r=>r.json()).then(console.log)
```
4. 确认返回的模型列表正确

如果仍有问题，请检查后端启动日志的详细信息。