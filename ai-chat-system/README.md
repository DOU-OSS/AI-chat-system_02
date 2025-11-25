# AI Chat System

## 项目简介
基于Spring Boot + Vue的智能AI对话系统，支持多角色管理、实时聊天功能和多AI模型集成。

## 系统要求

### 必需软件
- **Java 17+** - [下载地址](https://www.oracle.com/java/technologies/downloads/)
- **Node.js 16+** - [下载地址](https://nodejs.org/)
- **MySQL 8.0+** - [下载地址](https://dev.mysql.com/downloads/)
- **Maven 3.8+** - [下载地址](https://maven.apache.org/download.cgi)

### 版本检查
```bash
java -version    # 应显示 java version "17" 或更高
node -v          # 应显示 v16.0.0 或更高
npm -v           # 应显示 8.0.0 或更高
mvn -v           # 应显示 Apache Maven 3.8.0 或更高
mysql --version  # 应显示 8.0.0 或更高
```

## 技术栈

### 后端
- Spring Boot 3.1.5
- Spring Security + JWT
- Spring Data JPA
- MySQL 8.0
- WebSocket
- Swagger/OpenAPI

### 前端
- Vue 3
- Vite
- Vue Router
- Pinia
- Element Plus
- Axios
- Marked (Markdown渲染)

## 功能特性
- 用户管理：注册、登录、JWT认证、邮件验证码注册
- AI对话：实时聊天、多模型支持
- 角色管理：创建和管理AI角色（助手、老师、朋友等）
- 对话管理：创建、删除、查看历史对话
- 使用统计：实时追踪对话、消息、Token使用情况
- 多模型支持：集成OpenAI、Claude、通义千问等多个AI服务

## 项目结构
```
ai-chat-system/
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/aichatsystem/
│   │   │   │   ├── config/        # 配置类
│   │   │   │   ├── controller/    # 控制器
│   │   │   │   ├── dto/           # 数据传输对象
│   │   │   │   ├── entity/        # 实体类
│   │   │   │   ├── exception/     # 异常处理
│   │   │   │   ├── repository/    # 数据访问层
│   │   │   │   ├── security/      # 安全配置
│   │   │   │   ├── service/       # 业务逻辑层
│   │   │   │   └── util/          # 工具类
│   │   │   └── resources/
│   │   │       └── application.yml # 配置文件
│   │   └── test/
│   └── pom.xml
└── frontend/
    ├── src/
    │   ├── views/          # 页面组件
    │   ├── stores/         # Pinia状态管理
    │   ├── utils/          # 工具函数
    │   └── router/         # 路由配置
    └── package.json
```

## 快速开始

### 1. 安装依赖
确保已安装所有必需软件（见上方系统要求）。如需快速安装：

#### Windows用户
```powershell
# 使用Chocolatey包管理器
choco install openjdk17 nodejs mysql maven -y
```

#### Linux/Mac用户
```bash
# Ubuntu/Debian
sudo apt install -y openjdk-17-jdk nodejs npm mysql-server maven

# Mac (使用Homebrew)
brew install openjdk@17 node mysql maven
```

### 2. 获取项目并安装依赖
下载项目ZIP文件或克隆仓库，然后进入项目目录，并安装后端和前端依赖。

#### 获取代码
```bash
# 示例（如果使用 Git）
git clone <your-repo-url>
cd ai-chat-system
```

#### 安装后端依赖
```bash
cd backend
mvn clean install -DskipTests
```

#### 安装前端依赖
```bash
cd ../frontend
npm install
```

安装完成后返回项目根目录继续下一步。

### 3. 配置AI服务

#### 创建环境变量文件
```bash
# 复制配置模板
cp .env.example .env

# 编辑配置文件
nano .env
```

#### 配置AI服务（至少配置一个）

**必填配置**（只需配置这三个参数）：
```env
# 模型名称（多个模型用逗号分隔）
model_name=your-model-name

# API地址
URL=https://your-api-url

# API密钥
key=your-api-key
```

**配置示例**：

```env
# 通义千问（推荐，有免费额度）
model_name=qwen-turbo,qwen-plus
URL=https://dashscope.aliyuncs.com/api/v1
key=sk-your-key-here

# OpenAI
model_name=gpt-3.5-turbo,gpt-4
URL=https://api.openai.com/v1
key=sk-proj-your-key-here

# Claude
model_name=claude-3-5-sonnet-20241022
URL=https://api.anthropic.com/v1
key=sk-ant-your-key-here

# 本地模型（如LM Studio）
model_name=llama-3.1-8b
URL=http://localhost:1234/v1
key=not-needed
```

**邮件配置**（用于注册验证码）：
```env
# QQ邮箱配置示例
MAIL_HOST=smtp.qq.com
MAIL_PORT=587
MAIL_USERNAME=your-email@qq.com
MAIL_PASSWORD=your-authorization-code  # QQ邮箱授权码
EMAIL_DEBUG_MODE=false  # 设置为true时在控制台显示验证码，不发送邮件

# Gmail配置示例
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
```

**获取QQ邮箱授权码**：
1. 登录QQ邮箱 → 设置 → 账户
2. 找到"POP3/IMAP/SMTP/Exchange/CardDAV/CalDAV服务"
3. 开启"IMAP/SMTP服务"，获取授权码
4. 将授权码填入`MAIL_PASSWORD`

**调试模式**：
- 设置`EMAIL_DEBUG_MODE=true`时，验证码会打印在后端控制台，方便开发测试
- 生产环境请设置为`false`

### 4. 一键启动（推荐）

> 💡 **智能初始化**：启动脚本会自动创建数据库，应用启动时会通过 `DataInitializer.java` 自动创建默认用户和AI角色，无需任何手动配置！

#### Linux/Mac/WSL
```bash
# 给脚本执行权限
chmod +x start-all.sh

# 启动整个应用
./start-all.sh
```

启动时会提示输入MySQL密码，脚本将自动：
- ✅ 检查并创建数据库
- ✅ 启动后端服务
- ✅ 启动前端服务  
- ✅ 自动创建默认用户和AI角色

#### Windows
```batch
# 双击运行或在CMD中执行
start-all.bat
```

### 5. 脚本说明

| 脚本名称 | 用途 | 平台 |
|---------|------|------|
| `start-all.sh` | 一键启动所有服务 | Linux/Mac/WSL |
| `start-all.bat` | 一键启动所有服务 | Windows |
| `stop-all.sh` | 停止所有服务 | Linux/Mac/WSL |
| `stop-all.bat` | 停止所有服务 | Windows |
| `restart-all.sh` | 重启所有服务 | Linux/Mac/WSL |

注：启动脚本会自动检查并创建数据库，无需手动初始化。

### 6. 自动初始化系统 ✨

系统采用多层自动化初始化策略：

#### **数据库自动创建**
- 启动脚本会自动检测并创建 `ai_chat_system` 数据库
- JPA 会自动创建所有必要的数据表

#### **用户自动创建** (`DataInitializer.java`)
应用首次启动时，会自动创建以下默认账户：

```java
// 测试用户
Username: demo
Password: demo123456
Email: demo@ai-chat.com
```

**特点：**
- ✅ **幂等性**：重复启动不会重复创建
- ✅ **安全性**：密码使用 BCrypt 加密
- ✅ **即用性**：启动后立即可用默认账户登录

#### **AI角色自动创建**
系统会自动创建6个默认AI角色：
- 通用助手
- 程序员
- 英语老师
- 创意写手
- 心理咨询师
- 数据分析师

#### **工作流程**
```
启动应用 → 检查数据库 → 创建表结构 → 
检查默认用户 → 创建用户（如不存在）→ 
创建AI角色（如不存在）→ 完成
```

### 7. 手动启动

#### 后端启动
```bash
cd backend
mvn clean install -DskipTests
mvn spring-boot:run
```
后端服务将在 http://localhost:8080 启动

#### 前端启动
```bash
cd frontend
npm install
npm run dev
```
前端服务将在 http://localhost:5173 启动

## API文档
启动后访问 http://localhost:8080/swagger-ui.html 查看API文档

## 默认配置
- 后端端口：8080
- 前端端口：5173
- JWT过期时间：24小时
- 数据库：ai_chat_system（自动创建）

## 默认账户 🔐

系统使用 **Java自动初始化** (`DataInitializer.java`) 在首次启动时创建默认账户：

| 账户类型 | 用户名 | 密码 | 说明 |
|---------|--------|------|------|
| 测试账户 | `demo` | `demo123456` | 主测试账号 |

> 💡 **自动创建**：这些账户在应用首次启动时由 `DataInitializer.java` 自动创建，密码使用 BCrypt 加密存储。详见上方"自动初始化系统"章节。

## 功能特性

### 核心功能
- ✅ 用户注册/登录（JWT认证）
- ✅ 邮件验证码注册（支持QQ邮箱、Gmail等）
- ✅ AI实时对话（WebSocket）
- ✅ 多AI模型支持（OpenAI、Claude、通义千问等）
- ✅ AI角色管理（创建、编辑、删除）
- ✅ 对话管理（创建、删除、查看历史）
- ✅ 使用统计（对话数、消息数、Token使用）
- ✅ 响应式UI设计

### 技术亮点
- 🚀 **零配置启动**：自动创建数据库和默认用户
- 🔐 **智能初始化**：`DataInitializer.java` 自动创建测试账户和AI角色
- 🎨 **现代化UI**：深空主题设计风格 + 流畅动画
- 🔄 **一键部署**：跨平台启动脚本（Windows/Linux/Mac）
- 📦 **开箱即用**：下载即可运行，无需手动配置
- 🛡️ **安全可靠**：BCrypt 密码加密 + JWT 认证
- 🤖 **多模型支持**：支持任意OpenAI兼容的AI服务

## 支持的AI服务

系统采用**通用配置**，支持任何OpenAI兼容的API。只需在 `.env` 文件中配置三个参数：

```env
model_name=你的模型名称
URL=API地址
key=API密钥
```

**支持的服务包括但不限于**：
- ✅ OpenAI (GPT系列)
- ✅ Anthropic (Claude系列)
- ✅ 阿里云通义千问
- ✅ 智谱AI (ChatGLM)
- ✅ 百度文心一言
- ✅ 讯飞星火
- ✅ MiniMax
- ✅ 零一万物
- ✅ 月之暗面(Kimi)
- ✅ 本地部署模型（LM Studio、Ollama等）
- ✅ 任何OpenAI兼容的API

系统会自动：
- 🎯 根据URL检测服务商
- 🎯 根据模型名显示对应图标
- 🎯 格式化模型显示名称

## 注意事项
1. 首次运行时，JPA会自动创建数据表
2. 确保MySQL服务已启动
3. 必须配置至少一个AI服务的API密钥才能使用对话功能
4. **邮件配置**：
   - 注册功能需要配置邮件服务
   - 开发时可设置`EMAIL_DEBUG_MODE=true`在控制台查看验证码
   - 验证码有效期10分钟，60秒内不能重复发送
5. 生产部署时，记得修改JWT密钥、数据库密码等敏感配置

## License
MIT
