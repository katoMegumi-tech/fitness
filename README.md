# 健身指导系统

基于Vue3 + Spring Boot + MySQL的综合性健身服务平台

## 项目结构

```
fitness-system/
├── fitness-backend/          # 后端项目
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/fitness/
│   │   │   │   ├── FitnessApplication.java    # 启动类
│   │   │   │   ├── common/                    # 通用类
│   │   │   │   │   ├── Result.java           # 统一响应结果
│   │   │   │   │   └── ResultCode.java       # 响应状态码
│   │   │   │   ├── config/                    # 配置类
│   │   │   │   │   ├── CorsConfig.java       # 跨域配置
│   │   │   │   │   └── MyBatisPlusConfig.java # MyBatis-Plus配置
│   │   │   │   ├── controller/                # 控制器层
│   │   │   │   ├── service/                   # 服务层
│   │   │   │   ├── mapper/                    # 数据访问层
│   │   │   │   ├── entity/                    # 实体类
│   │   │   │   ├── dto/                       # 数据传输对象
│   │   │   │   ├── exception/                 # 异常处理
│   │   │   │   │   ├── BusinessException.java # 业务异常
│   │   │   │   │   └── GlobalExceptionHandler.java # 全局异常处理器
│   │   │   │   └── util/                      # 工具类
│   │   │   └── resources/
│   │   │       ├── application.yml            # 配置文件
│   │   │       └── mapper/                    # MyBatis XML映射文件
│   │   └── test/                              # 测试代码
│   └── pom.xml                                # Maven配置
│
├── fitness-frontend/         # 前端项目
│   ├── src/
│   │   ├── views/                             # 页面组件
│   │   │   ├── common/                        # 公共页面
│   │   │   │   ├── Login.vue                 # 登录页
│   │   │   │   └── Register.vue              # 注册页
│   │   │   ├── user/                          # 用户端页面
│   │   │   ├── coach/                         # 教练端页面
│   │   │   └── admin/                         # 管理员端页面
│   │   ├── components/                        # 可复用组件
│   │   ├── router/                            # 路由配置
│   │   │   └── index.js
│   │   ├── store/                             # 状态管理
│   │   │   └── user.js
│   │   ├── utils/                             # 工具类
│   │   │   └── request.js                    # Axios封装
│   │   ├── api/                               # API接口
│   │   ├── App.vue                            # 根组件
│   │   └── main.js                            # 入口文件
│   ├── index.html
│   ├── vite.config.js                         # Vite配置
│   └── package.json
│
├── fitness_system_complete.sql                # 数据库脚本
└── README.md                                  # 项目说明
```

## 技术栈

### 后端
- Spring Boot 2.7.18
- MyBatis-Plus 3.5.3.1
- Sa-Token 1.34.0 (认证授权)
- MySQL 8.0
- Lombok
- Hutool (工具类)

### 前端
- Vue 3.3.4
- Element Plus 2.3.14
- Vue Router 4.2.4
- Pinia 2.1.6 (状态管理)
- Axios 1.5.0
- ECharts 5.4.3 (数据可视化)
- Vite 4.4.9 (构建工具)

## 快速开始

### 1. 数据库初始化

```bash
# 创建数据库
mysql -u root -p

# 执行建表脚本
source fitness_system_complete.sql
```

### 2. 后端启动

```bash
cd fitness-backend

# 使用Maven安装依赖
mvn clean install

# 启动项目
mvn spring-boot:run
```

后端服务将在 http://localhost:8080/api 启动

### 3. 前端启动

```bash
cd fitness-frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端服务将在 http://localhost:3000 启动

## 数据库配置

数据库连接信息在 `fitness-backend/src/main/resources/application.yml` 中配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/fitness
    username: root
    password: Zch12.1159357
```

## 默认账号

- 管理员账号：admin
- 管理员密码：admin123

## API接口规范

所有API接口遵循RESTful风格设计：

- GET: 查询资源
- POST: 创建资源
- PUT: 更新资源
- DELETE: 删除资源

统一响应格式：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {},
  "timestamp": 1234567890
}
```

## 开发规范

1. 后端代码遵循阿里巴巴Java开发规范
2. 前端代码遵循Vue3官方风格指南
3. 所有API接口必须添加注释说明
4. 提交代码前必须通过测试

## 项目进度

查看 `.kiro/specs/fitness-guidance-system/tasks.md` 了解详细的开发任务列表

## 许可证

MIT License
