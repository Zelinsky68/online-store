FROM eclipse-temurin:11-jre-jammy
WORKDIR /app
COPY target/online-store-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
