# 构建（确保镜像存在：maven:3.9.6-eclipse-temurin-23 或 3.9-eclipse-temurin-23）
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -DskipTests package

# 运行
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar /app/app.jar
ENV JAVA_OPTS="-Dserver.port=${PORT}"
EXPOSE 8080
CMD ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
