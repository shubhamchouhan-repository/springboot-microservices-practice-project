
# ---------- Stage 1: Build the JAR with Maven ----------
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /build

# Copy Maven configuration first for dependency caching
COPY pom.xml .
RUN mvn -B dependency:go-offline

# Copy source code and build the application
COPY src ./src
RUN mvn -B clean package -DskipTests

# ---------- Stage 2: Runtime image ----------
FROM eclipse-temurin:21-jre

WORKDIR /app

# Create a non-root user
RUN useradd --system --uid 1001 appuser

# Copy the generated Spring Boot JAR
COPY --from=build /build/target/user-service-1.0.0.jar app.jar

# Run as a non-root user
USER appuser

# User service port
EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
