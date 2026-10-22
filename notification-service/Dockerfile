
# ---------- Stage 1: Build the JAR with Maven ----------
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /build

# Copy Maven configuration first for dependency caching
COPY pom.xml .

# Download dependencies
RUN mvn -B dependency:go-offline

# Copy source code and build the application
COPY src ./src
RUN mvn -B clean package -DskipTests

# ---------- Stage 2: Runtime image ----------
FROM eclipse-temurin:17-jre

WORKDIR /app

# Create a non-root user
RUN useradd --system --uid 1001 appuser

# Copy the generated Spring Boot JAR
# Change the filename if your pom.xml produces a different JAR name
COPY --from=build /build/target/notification-service-0.0.1-SNAPSHOT.jar app.jar

# Run as a non-root user
USER appuser

# Notification service port
EXPOSE 8083

# Start the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]

