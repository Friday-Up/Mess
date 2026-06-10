# ============================================================
# Mess 应用 Dockerfile
# 
# 多阶段构建的简化版本，用于将Spring Boot应用容器化。
# 基于OpenJDK 17精简镜像，适合生产环境部署。
# 
# 构建前提:
#   1. 先使用Maven构建JAR包: mvn clean package -DskipTests
#   2. JAR包位于target/目录下
# 
# 构建镜像:
#   docker build -t mess-app .
# 
# 运行容器:
#   docker run -p 8080:8080 mess-app
# 
# 或使用docker-compose:
#   docker-compose up -d
# 
# 镜像大小优化:
#   - 使用jdk-slim精简镜像（约200MB，比完整JDK小60%）
#   - 只包含运行时必需的组件
#   - 不包含编译工具和调试工具
# 
# 安全建议:
#   - 生产环境应使用非root用户运行
#   - 添加HEALTHCHECK指令进行健康检查
#   - 限制容器资源使用（CPU、内存）
# 
# 作者: zhangyaolong.5
# 创建时间: 2026-05-26
# ============================================================

# 基础镜像: OpenJDK 17 精简版
# slim变体去除了不必要的包，减小镜像体积
FROM openjdk:17-jdk-slim

# 设置工作目录
# 所有后续操作（COPY、RUN等）都在此目录下执行
WORKDIR /app

# 复制构建产物到容器
# target/mess-*.jar 匹配Maven构建的JAR包
# 重命名为app.jar以简化启动命令
# 注意: 需要先在宿主机执行 mvn clean package
COPY target/mess-*.jar app.jar

# 声明容器暴露的端口
# 8080是Spring Boot默认端口
# 此指令仅声明端口，实际映射需在docker run时指定
EXPOSE 8080

# 容器启动命令
# 使用exec格式（JSON数组），直接执行java命令
# 比shell格式更安全，能正确处理信号（如SIGTERM）
ENTRYPOINT ["java", "-jar", "app.jar"]