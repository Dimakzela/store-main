# Stage 1: Runtime extraction environment
FROM eclipse-temurin:17-jre-alpine AS runtime

# Production Security: Create an isolated system user so the container doesn't execute as 'root'
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Set working directory boundaries
WORKDIR /app

# Copy the compiled production executable JAR from the pipeline build workspace
COPY build/libs/*.jar app.jar

# Expose standard Spring Boot port profile hooks
EXPOSE 8080

# Configure memory optimizations to keep container orchestration weights stable
ENTRYPOINT ["java", "-XX:+UseG1GC", "-jar", "app.jar"]
