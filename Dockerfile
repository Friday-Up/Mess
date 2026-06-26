# Mess应用Dockerfile - 基于OpenJDK 17 slim镜像
# 构建: mvn clean package -DskipTests && docker build -t mess-app .
# 运行: docker run -p 8080:8080 mess-app
FROM openjdk:17-jdk-slim

WORKDIR /app

# 复制Maven构建的JAR包(需先执行 mvn clean package)
COPY target/mess-*.jar app.jar

# 声明端口(实际映射需在docker run时指定)
EXPOSE 8080

# 启动命令(exec格式，正确处理信号)
ENTRYPOINT ["java", "-jar", "app.jar"]