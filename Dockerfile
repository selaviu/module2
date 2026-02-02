# Етап 1: Збірка (Build) за допомогою Java 21
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Копіюємо конфігурацію Maven
COPY pom.xml .
# Копіюємо вихідний код
COPY src ./src

# Збираємо JAR файл
RUN mvn clean package -DskipTests

# Етап 2: Запуск (Run) на легкій JRE 21
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Копіюємо зібраний JAR з першого етапу
COPY --from=build /app/target/*.jar app.jar

# Відкриваємо порт твого бекенду
EXPOSE 8088

# Запускаємо додаток
ENTRYPOINT ["java", "-jar", "app.jar"]