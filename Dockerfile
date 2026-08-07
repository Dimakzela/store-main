# ---------------------------------------------------------------------------
# Stage 1: Build
#
# Compiles from source inside the image, so `docker build .` works from a clean
# checkout without depending on a JAR produced by some earlier step.
# ---------------------------------------------------------------------------
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /build

# Gradle wrapper first: the distribution download is cached in its own layer.
COPY gradlew ./
COPY gradle ./gradle
RUN chmod +x ./gradlew && ./gradlew --version --no-daemon

# Then the build scripts, so dependency resolution is cached independently of
# source changes. Editing a .java file no longer re-downloads the world.
COPY build.gradle settings.gradle ./
RUN ./gradlew dependencies --no-daemon --quiet

# Source last: this is the only layer that rebuilds on a normal code change.
COPY src ./src
RUN ./gradlew bootJar --no-daemon

# Split the executable JAR into Spring Boot layers ordered by change frequency.
RUN java -Djarmode=layertools -jar build/libs/*.jar extract --destination extracted

# ---------------------------------------------------------------------------
# Stage 2: Runtime
#
# Carries a JRE and the application only - no JDK, no Gradle, no source, no
# dependency cache.
# ---------------------------------------------------------------------------
FROM eclipse-temurin:17-jre-alpine AS runtime

# Production security: run as an unprivileged system user, never root.
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copy each layer separately, least volatile first. Dependencies rarely change,
# so redeploys push and pull only the small application layer.
COPY --from=builder --chown=appuser:appgroup /build/extracted/dependencies/ ./
COPY --from=builder --chown=appuser:appgroup /build/extracted/spring-boot-loader/ ./
COPY --from=builder --chown=appuser:appgroup /build/extracted/snapshot-dependencies/ ./
COPY --from=builder --chown=appuser:appgroup /build/extracted/application/ ./

USER appuser

EXPOSE 8080

# MaxRAMPercentage lets the JVM size its heap from the container memory limit
# rather than the host's total RAM.
ENTRYPOINT ["java", "-XX:+UseG1GC", "-XX:MaxRAMPercentage=75.0", "org.springframework.boot.loader.launch.JarLauncher"]
