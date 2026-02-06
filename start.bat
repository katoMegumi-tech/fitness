@echo off
chcp 65001 >nul
title 健身指导系统启动

echo ========================================
echo 健身指导系统 - 启动脚本
echo ========================================
echo.

REM 检查 Java 环境
echo [检查] Java 环境...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] 未检测到 Java 环境，请先安装 JDK 8+
    pause
    exit /b 1
)
echo [✓] Java 环境正常
echo.

REM 检查 Node.js 环境
echo [检查] Node.js 环境...
node -v >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] 未检测到 Node.js 环境，请先安装 Node.js 14+
    pause
    exit /b 1
)
echo [✓] Node.js 环境正常
echo.

REM 检查 MySQL 连接
echo [检查] MySQL 数据库...
echo 请确保 MySQL 服务已启动，数据库 fitness 已创建
echo.

REM 启动后端服务
echo [1/2] 启动后端服务...
cd fitness-backend
start "后端服务-8080" cmd /k "echo 正在启动后端服务... && mvn spring-boot:run"
cd ..
echo [提示] 后端服务窗口已打开，请查看启动日志
echo.

REM 等待后端启动
echo [等待] 后端服务启动中，请等待 15 秒...
echo 如果后端启动失败，请查看后端服务窗口的错误信息
timeout /t 15 /nobreak >nul

REM 启动前端服务
echo.
echo [2/2] 启动前端服务...
cd fitness-frontend
start "前端服务-3000" cmd /k "echo 正在启动前端服务... && npm run dev"
cd ..
echo [提示] 前端服务窗口已打开
echo.

echo ========================================
echo 启动完成！
echo ========================================
echo.
echo 后端: http://localhost:8080/api
echo 前端: http://localhost:3000
echo.
echo 管理员账号: admin / admin123
echo.
echo [提示] 如果遇到问题:
echo 1. 检查后端服务窗口是否有错误信息
echo 2. 确认 MySQL 服务已启动
echo 3. 确认数据库 fitness 已创建并执行了 SQL 脚本
echo.
timeout /t 5 /nobreak >nul
start http://localhost:3000
exit
