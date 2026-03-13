# STAGE 1: Build (Keep as is, it's efficient)
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN apk add --no-cache maven
RUN mvn clean package -DskipTests

# STAGE 2: Run (Optimised for 1GB RAM)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create a non-root user for security
RUN addgroup -g 1000 appgroup && \
    adduser -u 1000 -G appgroup -D appuser

# Copy the built JAR from the builder stage
COPY --from=builder /app/target/*.jar app.jar

RUN chown -R appuser:appgroup /app
USER appuser

# This is internal to the container, we map it to 8081 later
EXPOSE 8080

# CRITICAL: Reduced limits for 1GB RAM server
# -Xms128m (Starts small)
# -Xmx256m (Caps it so 3 projects = 768MB + OS/Nginx)
ENV JAVA_OPTS="-Xms128m -Xmx256m"

# Added -Dserver.port=8080 just to be safe inside the container
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dserver.port=8080 -Djava.security.egd=file:/dev/./urandom -jar app.jar"]
