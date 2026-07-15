# ============================================================
# Mess应用Dockerfile - 多阶段构建配置
# ============================================================
# 构建流程:
#   1. 先在本地执行: mvn clean package -DskipTests
#   2. 再构建镜像:   docker build -t mess-app .
#   3. 运行容器:     docker run -p 8080:8080 mess-app
#
# 镜像说明:
#   - 基础镜像: openjdk:17-jdk-slim（Debian精简版，约200MB）
#   - 生产环境建议: eclipse-temurin:17-jre-alpine（更小，约80MB）
#   - slim镜像包含JDK完整工具链，alpine只含JRE，体积更小
#
# 注意事项:
#   - 当前为单阶段构建，JAR需在构建镜像前手动打包
#   - 生产环境建议改为多阶段构建（Maven构建+JRE运行）
#   - ENTRYPOINT使用exec格式["java","-jar"]，正确处理SIGTERM信号
#   - 容器时区默认UTC，如需东八区加: ENV TZ=Asia/Shanghai
# ============================================================

# 基础镜像: OpenJDK 17 slim版本，包含JDK但去除非必要工具
FROM openjdk:17-jdk-slim

# 工作目录: 容器内应用根路径
WORKDIR /app

# 复制Maven构建的JAR包并重命名为app.jar
# 前置条件: 需先执行 mvn clean package -DskipTests 生成target/mess-*.jar
COPY target/mess-*.jar app.jar

# 声明容器监听端口（仅文档作用，实际映射需在docker run时指定-p）
# docker run -p 8080:8080 才会真正将容器8080映射到主机8080
EXPOSE 8080

# 启动命令: exec格式，Spring Boot收到SIGTERM时优雅关闭
# 也可添加JVM参数: ["java","-Xmx512m","-jar","app.jar"]
ENTRYPOINT ["java", "-jar", "app.jar"]