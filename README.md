# Mess - Spring Boot 演示项目

这是一个基于Spring Boot 3.2.5的完整企业级演示项目，展示了现代化的Java Web应用开发最佳实践。

## 项目特性

- **用户管理**: 完整的CRUD操作
- **安全认证**: Spring Security集成
- **API文档**: OpenAPI 3 (Swagger)支持
- **缓存机制**: Redis和Spring Cache集成
- **监控指标**: Spring Boot Actuator
- **容器化**: Docker支持
- **测试覆盖**: 单元测试和集成测试

## 技术栈

- **框架**: Spring Boot 3.2.5
- **数据访问**: Spring Data JPA
- **数据库**: H2内存数据库
- **缓存**: Redis + Spring Cache
- **安全**: Spring Security
- **文档**: OpenAPI 3 (Swagger)
- **监控**: Spring Boot Actuator
- **构建**: Maven

## 快速开始

### 环境要求

- Java 17+
- Maven 3.6+
- Docker (可选)

### 运行项目

```bash
# 克隆项目
git clone https://github.com/Friday-Up/Mess.git
cd Mess

# 编译和运行
mvn clean compile spring-boot:run
```

### Docker运行

```bash
# 构建镜像
docker build -t mess-app .

# 运行容器
docker run -p 8080:8080 mess-app
```

## API接口

### 问候服务 (公开访问)

- `GET /hello` - 简单问候
- `GET /hello/advanced` - 高级问候
- `POST /hello` - POST问候

### 用户管理 (需要认证)

- `GET /api/users` - 获取用户列表
- `GET /api/users/{id}` - 获取用户详情
- `POST /api/users` - 创建用户
- `PUT /api/users/{id}` - 更新用户
- `DELETE /api/users/{id}` - 删除用户

### 认证信息

- 用户名: `user` / 密码: `password`
- 用户名: `admin` / 密码: `admin`

## API文档

启动应用后访问:
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI文档: http://localhost:8080/v3/api-docs

## 监控端点

- 健康检查: http://localhost:8080/actuator/health
- 指标监控: http://localhost:8080/actuator/metrics
- 配置信息: http://localhost:8080/actuator/env

## 开发指南

### 项目结构

```
src/
├── main/
│   ├── java/com/example/mess/
│   │   ├── controller/    # 控制器层
│   │   ├── service/         # 服务层
│   │   ├── repository/      # 数据访问层
│   │   ├── entity/          # 实体类
│   │   ├── dto/             # 数据传输对象
│   │   ├── config/          # 配置类
│   │   └── exception/       # 异常处理
│   └── resources/
│       ├── application.yml  # 主配置文件
│       └── data.sql         # 初始化数据
└── test/                    # 测试代码
```

### 测试

```bash
# 运行所有测试
mvn test

# 运行特定测试类
mvn test -Dtest=HelloControllerTest
```

## 贡献指南

欢迎提交Issue和Pull Request！

## 许可证

MIT License