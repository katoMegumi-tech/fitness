# 健身指导系统 - 快速启动指南

## 🚀 快速开始

### 1. 环境准备
- JDK 8+
- Node.js 14+
- MySQL 8.0+
- Maven 3.6+

### 2. 数据库初始化
```bash
mysql -u root -p
CREATE DATABASE fitness CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE fitness;
source fitness_system_complete.sql;
```

### 3. 后端启动
```bash
cd fitness-backend
mvn clean install
mvn spring-boot:run
```
访问：http://localhost:8080

### 4. 前端启动
```bash
cd fitness-frontend
npm install
npm run dev
```
访问：http://localhost:3000

### 5. 登录系统
- **管理员**：admin / admin123
- **测试账号**：需要注册创建

## 📋 功能模块

### 用户端
- 注册登录
- 身体数据管理
- 选择教练
- 查看健身计划
- 每日打卡
- 数据趋势分析

### 教练端
- 教练认证
- 处理绑定申请
- 查看学员列表
- 制定健身计划
- 点评学员打卡

### 管理员端
- 审核教练认证
- 审核健身计划
- 系统数据统计

## 🎯 演示流程

1. 注册用户/教练账号
2. 教练提交认证（管理员审核）
3. 用户录入身体数据
4. 用户申请绑定教练
5. 教练接受绑定
6. 教练制定计划（管理员审核）
7. 用户每日打卡
8. 教练点评打卡
9. 查看数据趋势

## 📊 技术栈

**后端**：Spring Boot + MyBatis-Plus + Sa-Token + MySQL
**前端**：Vue 3 + Element Plus + ECharts + Pinia

## 📝 更多文档

- `README.md` - 项目介绍
- `IMPLEMENTATION_STATUS.md` - 实施状态
- `PROJECT_COMPLETION_SUMMARY.md` - 完成总结
