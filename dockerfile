FROM eclipse-temurin:17-jre-jammy

# Set working directory
WORKDIR /app

# Create non-root user
RUN groupadd -r spring && useradd -r -g spring spring
RUN chown -R spring:spring /app
USER spring

# Copy JAR file
COPY target/notification-service-*.jar app.jar

# Create directories for logs and attachments
RUN mkdir -p logs attachments

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Expose port
EXPOSE 8080

# JVM options
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:MaxMetaspaceSize=256m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/app/logs -Djava.security.egd=file:/dev/./urandom"

# Run application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
