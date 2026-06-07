FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY target/UserService-1.0.0.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]