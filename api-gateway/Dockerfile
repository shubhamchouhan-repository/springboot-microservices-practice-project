# ---------- Stage 1: build the JAR with Maven ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# Copy only the pom first so dependencies are cached between builds
COPY pom.xml .
RUN mvn -B dependency:go-offline

# Now copy the source code and build
COPY src ./src
RUN mvn -B clean package -DskipTests

# ---------- Stage 2: small runtime image ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

# Run as a non-root user
RUN useradd --system --uid 1001 appuser
USER appuser

COPY --from=build /build/target/api-gateway-1.0.0.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]