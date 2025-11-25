#!/bin/bash

# AI聊天系统 - 一键启动脚本
# 自动启动后端和前端服务

# 颜色定义
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 获取脚本所在目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

echo -e "${BLUE}======================================"
echo "AI聊天系统 - 一键启动"
echo "======================================${NC}"
echo ""

# 检查环境变量文件
if [ ! -f ".env" ]; then
    echo -e "${YELLOW}⚠ 未找到 .env 文件，正在创建...${NC}"
    if [ -f ".env.example" ]; then
        cp .env.example .env
        echo -e "${GREEN}✓ 已创建 .env 文件${NC}"
        echo -e "${YELLOW}⚠ 请编辑 .env 文件，配置 OPENAI_API_KEY${NC}"
        echo -e "${YELLOW}  使用命令: vim .env${NC}"
        echo ""
        read -p "按回车键继续启动（或 Ctrl+C 取消）..."
    else
        echo -e "${RED}✗ 未找到 .env.example 文件${NC}"
    fi
fi

# 加载环境变量
if [ -f ".env" ]; then
    export $(cat .env | grep -v '^#' | xargs)
    echo -e "${GREEN}✓ 已加载环境变量${NC}"
fi

# 检查Java
echo ""
echo -e "${BLUE}[1/6] 检查Java环境...${NC}"
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
    if [ "$JAVA_VERSION" -ge 17 ]; then
        echo -e "${GREEN}✓ Java版本: $(java -version 2>&1 | head -n 1)${NC}"
    else
        echo -e "${RED}✗ Java版本过低，需要Java 17+${NC}"
        exit 1
    fi
else
    echo -e "${RED}✗ 未安装Java${NC}"
    exit 1
fi

# 检查Maven
echo ""
echo -e "${BLUE}[2/6] 检查Maven环境...${NC}"
if command -v mvn &> /dev/null; then
    echo -e "${GREEN}✓ Maven版本: $(mvn -version | head -n 1)${NC}"
else
    echo -e "${RED}✗ 未安装Maven${NC}"
    exit 1
fi

# 检查Node.js
echo ""
echo -e "${BLUE}[3/6] 检查Node.js环境...${NC}"
if command -v node &> /dev/null; then
    echo -e "${GREEN}✓ Node.js版本: $(node -v)${NC}"
    echo -e "${GREEN}✓ npm版本: $(npm -v)${NC}"
else
    echo -e "${RED}✗ 未安装Node.js${NC}"
    exit 1
fi

# 检查MySQL
echo ""
echo -e "${BLUE}[4/6] 检查MySQL服务...${NC}"
if command -v mysql &> /dev/null; then
    echo -e "${YELLOW}⚠ 请输入MySQL root密码（直接回车跳过）:${NC}"
read -s MYSQL_PASSWORD
if [ ! -z "$MYSQL_PASSWORD" ]; then
        # 尝试创建数据库
    mysql -u root -p$MYSQL_PASSWORD <<EOF 2>/dev/null
CREATE DATABASE IF NOT EXISTS ai_chat_system CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
EOF
    if [ $? -eq 0 ]; then
            echo -e "${GREEN}✓ 数据库已就绪${NC}"
        else
            echo -e "${YELLOW}⚠ 无法创建数据库，可能已存在${NC}"
        fi
    else
        echo -e "${YELLOW}⚠ 跳过数据库检查${NC}"
    fi
else
    echo -e "${YELLOW}⚠ 未安装MySQL客户端（服务可能正在运行）${NC}"
fi

# 检查端口占用
echo ""
echo -e "${BLUE}[5/6] 检查端口占用...${NC}"

check_port() {
    local port=$1
    local service=$2
    if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1; then
        echo -e "${YELLOW}⚠ 端口 $port 已被占用 ($service)${NC}"
        read -p "是否终止占用该端口的进程？(y/n) " -n 1 -r
        echo
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            lsof -ti:$port | xargs kill -9 2>/dev/null
            echo -e "${GREEN}✓ 已终止端口 $port 的进程${NC}"
        else
            echo -e "${YELLOW}⚠ 跳过端口 $port${NC}"
            return 1
        fi
    else
        echo -e "${GREEN}✓ 端口 $port 可用 ($service)${NC}"
    fi
    return 0
}

check_port 8080 "后端"
BACKEND_PORT_OK=$?

check_port 5173 "前端"
FRONTEND_PORT_OK=$?

# 启动服务
echo ""
echo -e "${BLUE}[6/6] 启动服务...${NC}"
echo ""

# 创建日志目录
mkdir -p logs

# 启动后端
if [ $BACKEND_PORT_OK -eq 0 ]; then
    echo -e "${BLUE}正在启动后端服务...${NC}"
    cd backend
    
    # 检查是否需要编译
    if [ ! -d "target" ]; then
        echo -e "${YELLOW}首次启动，正在编译项目...${NC}"
        mvn clean install -DskipTests > ../logs/backend-build.log 2>&1
        if [ $? -eq 0 ]; then
            echo -e "${GREEN}✓ 编译成功${NC}"
        else
            echo -e "${RED}✗ 编译失败，查看日志: logs/backend-build.log${NC}"
            exit 1
        fi
    fi
    
    # 启动后端（后台运行）
    nohup mvn spring-boot:run > ../logs/backend.log 2>&1 &
    BACKEND_PID=$!
    echo $BACKEND_PID > ../logs/backend.pid
    echo -e "${GREEN}✓ 后端服务已启动 (PID: $BACKEND_PID)${NC}"
    echo -e "${GREEN}  访问地址: http://localhost:8080${NC}"
    echo -e "${GREEN}  API文档: http://localhost:8080/swagger-ui.html${NC}"
    echo -e "${GREEN}  日志文件: logs/backend.log${NC}"
    
    cd ..
else
    echo -e "${YELLOW}⚠ 跳过后端启动${NC}"
fi

# 等待后端启动
if [ $BACKEND_PORT_OK -eq 0 ]; then
    echo ""
    echo -e "${BLUE}等待后端服务启动...${NC}"
    for i in {1..30}; do
        if curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
            echo -e "${GREEN}✓ 后端服务已就绪${NC}"
            break
        fi
        echo -n "."
        sleep 2
    done
    echo ""
fi

# 启动前端
if [ $FRONTEND_PORT_OK -eq 0 ]; then
    echo ""
    echo -e "${BLUE}正在启动前端服务...${NC}"
    cd frontend
    
    # 检查node_modules
    if [ ! -d "node_modules" ]; then
        echo -e "${YELLOW}首次启动，正在安装依赖...${NC}"
        npm install > ../logs/frontend-install.log 2>&1
        if [ $? -eq 0 ]; then
            echo -e "${GREEN}✓ 依赖安装成功${NC}"
        else
            echo -e "${RED}✗ 依赖安装失败，查看日志: logs/frontend-install.log${NC}"
            exit 1
        fi
    fi
    
    # 启动前端（后台运行）
    nohup npm run dev > ../logs/frontend.log 2>&1 &
    FRONTEND_PID=$!
    echo $FRONTEND_PID > ../logs/frontend.pid
    echo -e "${GREEN}✓ 前端服务已启动 (PID: $FRONTEND_PID)${NC}"
    echo -e "${GREEN}  访问地址: http://localhost:5173${NC}"
    echo -e "${GREEN}  日志文件: logs/frontend.log${NC}"
    
    cd ..
else
    echo -e "${YELLOW}⚠ 跳过前端启动${NC}"
fi

# 等待前端启动
if [ $FRONTEND_PORT_OK -eq 0 ]; then
    echo ""
    echo -e "${BLUE}等待前端服务启动...${NC}"
    for i in {1..15}; do
        if curl -s http://localhost:5173 > /dev/null 2>&1; then
            echo -e "${GREEN}✓ 前端服务已就绪${NC}"
            break
        fi
        echo -n "."
        sleep 2
    done
    echo ""
fi

# 启动完成
echo ""
echo -e "${GREEN}======================================"
echo "✓ 启动完成！"
echo "======================================${NC}"
echo ""
echo -e "${BLUE}服务信息：${NC}"
if [ $BACKEND_PORT_OK -eq 0 ]; then
    echo -e "  后端: ${GREEN}http://localhost:8080${NC}"
    echo -e "  API文档: ${GREEN}http://localhost:8080/swagger-ui.html${NC}"
fi
if [ $FRONTEND_PORT_OK -eq 0 ]; then
    echo -e "  前端: ${GREEN}http://localhost:5173${NC}"
fi
echo ""
echo -e "${BLUE}日志文件：${NC}"
echo -e "  后端日志: logs/backend.log"
echo -e "  前端日志: logs/frontend.log"
echo ""
echo -e "${BLUE}管理命令：${NC}"
echo -e "  查看后端日志: tail -f logs/backend.log"
echo -e "  查看前端日志: tail -f logs/frontend.log"
echo -e "  停止所有服务: ./stop-all.sh"
echo -e "  重启所有服务: ./restart-all.sh"
echo ""
echo -e "${YELLOW}提示：${NC}"
echo -e "  - 首次启动会自动创建数据库和默认AI角色"
echo -e "  - 确保已在 .env 文件中配置 OPENAI_API_KEY"
echo -e "  - 服务在后台运行，关闭终端不会停止服务"
echo ""
