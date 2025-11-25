@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

REM AI聊天系统 - Windows一键启动脚本

echo ======================================
echo AI聊天系统 - 一键启动
echo ======================================
echo.

REM 获取脚本所在目录
cd /d "%~dp0"

REM 检查环境变量文件
if not exist ".env" (
    echo [警告] 未找到 .env 文件，正在创建...
    if exist ".env.example" (
        copy .env.example .env >nul
        echo [成功] 已创建 .env 文件
        echo [提示] 请编辑 .env 文件，配置 OPENAI_API_KEY
        echo.
        pause
    ) else (
        echo [错误] 未找到 .env.example 文件
    )
)

REM 检查Java
echo [1/6] 检查Java环境...
java -version >nul 2>&1
if errorlevel 1 (
    echo [错误] 未安装Java或未配置环境变量
    pause
    exit /b 1
) else (
    echo [成功] Java环境正常
)

REM 检查Maven
echo.
echo [2/6] 检查Maven环境...
mvn -version >nul 2>&1
if errorlevel 1 (
    echo [错误] 未安装Maven或未配置环境变量
    pause
    exit /b 1
) else (
    echo [成功] Maven环境正常
)

REM 检查Node.js
echo.
echo [3/6] 检查Node.js环境...
node -v >nul 2>&1
if errorlevel 1 (
    echo [错误] 未安装Node.js或未配置环境变量
    pause
    exit /b 1
) else (
    echo [成功] Node.js环境正常
)

REM 检查MySQL
echo.
echo [4/6] 检查MySQL服务...
sc query MySQL >nul 2>&1
if errorlevel 1 (
    echo [警告] MySQL服务未运行或未安装
    echo [提示] 请确保MySQL服务正在运行
) else (
    echo [成功] MySQL服务正常
)

REM 检查端口占用
echo.
echo [5/6] 检查端口占用...
netstat -ano | findstr ":8080" | findstr "LISTENING" >nul 2>&1
if not errorlevel 1 (
    echo [警告] 端口 8080 已被占用（后端）
    echo [提示] 请手动停止占用该端口的进程
)

netstat -ano | findstr ":5173" | findstr "LISTENING" >nul 2>&1
if not errorlevel 1 (
    echo [警告] 端口 5173 已被占用（前端）
    echo [提示] 请手动停止占用该端口的进程
)

REM 创建日志目录
if not exist "logs" mkdir logs

REM 启动服务
echo.
echo [6/6] 启动服务...
echo.

REM 启动后端
echo [启动] 正在启动后端服务...
cd backend

REM 检查是否需要编译
if not exist "target" (
    echo [提示] 首次启动，正在编译项目...
    call mvn clean install -DskipTests > ..\logs\backend-build.log 2>&1
    if errorlevel 1 (
        echo [错误] 编译失败，查看日志: logs\backend-build.log
        cd ..
        pause
        exit /b 1
    )
    echo [成功] 编译完成
)

REM 启动后端（新窗口）
start "AI聊天系统-后端" cmd /k "mvn spring-boot:run"
echo [成功] 后端服务已启动
echo [信息] 访问地址: http://localhost:8080
echo [信息] API文档: http://localhost:8080/swagger-ui.html

cd ..

REM 等待后端启动
echo.
echo [等待] 等待后端服务启动...
timeout /t 10 /nobreak >nul

REM 启动前端
echo.
echo [启动] 正在启动前端服务...
cd frontend

REM 检查node_modules
if not exist "node_modules" (
    echo [提示] 首次启动，正在安装依赖...
    call npm install > ..\logs\frontend-install.log 2>&1
    if errorlevel 1 (
        echo [错误] 依赖安装失败，查看日志: logs\frontend-install.log
        cd ..
        pause
        exit /b 1
    )
    echo [成功] 依赖安装完成
)

REM 启动前端（新窗口）
start "AI聊天系统-前端" cmd /k "npm run dev"
echo [成功] 前端服务已启动
echo [信息] 访问地址: http://localhost:5173

cd ..

REM 启动完成
echo.
echo ======================================
echo 启动完成！
echo ======================================
echo.
echo 服务信息：
echo   后端: http://localhost:8080
echo   API文档: http://localhost:8080/swagger-ui.html
echo   前端: http://localhost:5173
echo.
echo 提示：
echo   - 首次启动会自动创建数据库和默认AI角色
echo   - 确保已在 .env 文件中配置 OPENAI_API_KEY
echo   - 服务在独立窗口运行，关闭窗口即停止服务
echo   - 查看日志: logs\backend.log 和 logs\frontend.log
echo.
echo 按任意键打开浏览器...
pause >nul

REM 打开浏览器
start http://localhost:5173

echo.
echo 浏览器已打开，祝您使用愉快！
echo.
