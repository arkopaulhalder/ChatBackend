FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy the fat jar built by spring-boot-maven-plugin
COPY target/APIGateway-1.0.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]