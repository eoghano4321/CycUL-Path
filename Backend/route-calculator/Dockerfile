FROM eclipse-temurin:17-jdk-alpine
COPY target/*.jar app.jar
COPY src/main/resources /app/resources
ENTRYPOINT ["java", "-Xms256m", "-Xmx512m", "-jar","/app.jar"]
EXPOSE 8080