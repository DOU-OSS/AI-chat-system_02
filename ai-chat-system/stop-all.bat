@echo off
chcp 65001 >nul

REM AI聊天系统 - Windows一键停止脚本

echo ======================================
echo AI聊天系统 - 停止所有服务 (Windows)
echo ======================================
echo.

REM 切换到脚本所在目录
cd /d "%~dp0"

REM 尝试关闭端口 8080 (后端)
echo [后端] 正在检查端口 8080...
for /f "tokens=5" %%p in ('netstat -ano ^| findstr ":8080" ^| findstr "LISTENING"') do (
    echo [后端] 发现进程 PID=%%p ，尝试结束...
    taskkill /F /PID %%p >nul 2>&1
    echo [后端] 已尝试结束进程 %%p
)

REM 尝试关闭端口 5173 (前端)
echo.
echo [前端] 正在检查端口 5173...
for /f "tokens=5" %%p in ('netstat -ano ^| findstr ":5173" ^| findstr "LISTENING"') do (
    echo [前端] 发现进程 PID=%%p ，尝试结束...
    taskkill /F /PID %%p >nul 2>&1
    echo [前端] 已尝试结束进程 %%p
)

echo.
echo ======================================
echo 所有相关服务已尝试停止
echo 如有残留窗口，可手动关闭 cmd 窗口
echo ======================================
echo.
pause
