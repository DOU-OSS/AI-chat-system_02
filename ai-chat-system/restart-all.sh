#!/bin/bash

# AI聊天系统 - 重启脚本

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
echo "AI聊天系统 - 重启服务"
echo "======================================${NC}"
echo ""

# 停止所有服务
echo -e "${BLUE}步骤 1/2: 停止现有服务...${NC}"
echo ""
./stop-all.sh

echo ""
echo -e "${BLUE}步骤 2/2: 启动服务...${NC}"
echo ""
sleep 2

# 启动所有服务
./start-all.sh
