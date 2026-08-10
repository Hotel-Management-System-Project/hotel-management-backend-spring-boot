FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=build /app/target/hotel-management-0.0.1-SNAPSHOT.jar app.jar

# Keep the Spring Boot process within Render's 512 MB free-instance limit.
ENV JAVA_TOOL_OPTIONS="-Xms64m -Xmx256m -XX:MaxMetaspaceSize=128m -XX:MaxDirectMemorySize=32m -XX:ReservedCodeCacheSize=64m -XX:+UseSerialGC"

CMD ["sh", "-c", "java -jar app.jar --server.port=${PORT:-8080}"]
