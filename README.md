# Mess - Spring Boot Demo Project

这是一个功能完整的Spring Boot演示项目，展示了现代Java Web开发的完整技术栈。

## 🚀 功能特性

- **RESTful API** - 完整的用户管理API
- **Spring Security** - 基于HTTP Basic的安全认证
- **Swagger/OpenAPI** - API文档自动生成
- **JPA/Hibernate** - 数据持久化
- **H2数据库** - 内存数据库，便于开发和测试
- **Lombok** - 减少样板代码
- **统一响应格式** - 标准化的API响应
- **全局异常处理** - 统一的错误处理机制
- **单元测试** - 完整的测试覆盖

## 📁 项目结构

```
src/
├── main/
│   ├── java/com/example/mess/
│   │   ├── config/          # 配置类
│   │   ├── controller/      # 控制器
│   │   ├── dto/             # 数据传输对象
│   │   ├── entity/          # 实体类
│   │   ├── exception/       # 异常处理
│   │   ├── repository/      # 数据访问层
│   │   ├── service/         # 业务逻辑层
│   │   └── MessApplication.java
│   └── resources/
│       ├── application.yml    # 应用配置
│       └── data.sql          # 初始化数据
└── test/                     # 测试代码
```

## 🛠️ 技术栈

- **Spring Boot 3.2.5**
- **Spring Security**
- **Spring Data JPA**
- **H2 Database**
- **OpenAPI 3 (Swagger)**
- **Lombok**
- **JUnit 5 & Mockito**

## 🔧 快速开始

### 1. 克隆项目
```bash
git clone https://github.com/Friday-Up/Mess.git
cd Mess
```

### 2. 运行项目
```bash
./mvnw spring-boot:run
# 或者
mvn spring-boot:run
```

### 3. 访问应用

- **应用主页**: http://localhost:8080
- **API文档**: http://localhost:8080/swagger-ui.html
- **H2控制台**: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:testdb`
  - 用户名: `sa`
  - 密码: `password`

## 🔐 安全认证

项目使用HTTP Basic认证，默认用户：
- **用户名**: `admin` / **密码**: `admin123`
- **用户名**: `user` / **密码**: `user123`

## 📖 API接口

### 基础接口
- `GET /` - 欢迎信息
- `GET /hello` - 问候语

### 用户管理接口
- `GET /api/users` - 获取所有用户
- `GET /api/users/{id}` - 根据ID获取用户
- `GET /api/users/username/{username}` - 根据用户名获取用户
- `POST /api/users` - 创建新用户
- `PUT /api/users/{id}` - 更新用户信息
- `DELETE /api/users/{id}` - 删除用户

## 🧪 运行测试

```bash
./mvnw test
# 或者
mvn test
```

## 📄 许可证

MIT License