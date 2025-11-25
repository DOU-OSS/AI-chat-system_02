#!/bin/bash

# AI聊天系统 - 停止脚本

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
echo "AI聊天系统 - 停止服务"
echo "======================================${NC}"
echo ""

# 停止后端
if [ -f "logs/backend.pid" ]; then
    BACKEND_PID=$(cat logs/backend.pid)
    if ps -p $BACKEND_PID > /dev/null 2>&1; then
        echo -e "${BLUE}正在停止后端服务 (PID: $BACKEND_PID)...${NC}"
        kill $BACKEND_PID 2>/dev/null
        
        # 等待进程结束
        for i in {1..10}; do
            if ! ps -p $BACKEND_PID > /dev/null 2>&1; then
                echo -e "${GREEN}✓ 后端服务已停止${NC}"
                break
            fi
            sleep 1
        done
        
        # 如果还在运行，强制终止
        if ps -p $BACKEND_PID > /dev/null 2>&1; then
            echo -e "${YELLOW}⚠ 正在强制停止后端服务...${NC}"
            kill -9 $BACKEND_PID 2>/dev/null
            echo -e "${GREEN}✓ 后端服务已强制停止${NC}"
        fi
    else
        echo -e "${YELLOW}⚠ 后端服务未运行${NC}"
    fi
    rm -f logs/backend.pid
else
    echo -e "${YELLOW}⚠ 未找到后端PID文件${NC}"
    # 尝试通过端口查找并终止
    if lsof -ti:8080 > /dev/null 2>&1; then
        echo -e "${BLUE}正在通过端口停止后端服务...${NC}"
        lsof -ti:8080 | xargs kill -9 2>/dev/null
        echo -e "${GREEN}✓ 后端服务已停止${NC}"
    fi
fi

echo ""

# 停止前端
if [ -f "logs/frontend.pid" ]; then
    FRONTEND_PID=$(cat logs/frontend.pid)
    if ps -p $FRONTEND_PID > /dev/null 2>&1; then
        echo -e "${BLUE}正在停止前端服务 (PID: $FRONTEND_PID)...${NC}"
        kill $FRONTEND_PID 2>/dev/null
        
        # 等待进程结束
        for i in {1..10}; do
            if ! ps -p $FRONTEND_PID > /dev/null 2>&1; then
                echo -e "${GREEN}✓ 前端服务已停止${NC}"
                break
            fi
            sleep 1
        done
        
        # 如果还在运行，强制终止
        if ps -p $FRONTEND_PID > /dev/null 2>&1; then
            echo -e "${YELLOW}⚠ 正在强制停止前端服务...${NC}"
            kill -9 $FRONTEND_PID 2>/dev/null
            echo -e "${GREEN}✓ 前端服务已强制停止${NC}"
        fi
    else
        echo -e "${YELLOW}⚠ 前端服务未运行${NC}"
    fi
    rm -f logs/frontend.pid
else
    echo -e "${YELLOW}⚠ 未找到前端PID文件${NC}"
    # 尝试通过端口查找并终止
    if lsof -ti:5173 > /dev/null 2>&1; then
        echo -e "${BLUE}正在通过端口停止前端服务...${NC}"
        lsof -ti:5173 | xargs kill -9 2>/dev/null
        echo -e "${GREEN}✓ 前端服务已停止${NC}"
    fi
fi

echo ""
echo -e "${GREEN}======================================"
echo "✓ 所有服务已停止"
echo "======================================${NC}"
echo ""
