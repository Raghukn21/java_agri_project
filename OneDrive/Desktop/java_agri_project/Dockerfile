FROM openjdk:17-jdk-slim as java-builder

WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY backend/mvnw backend/mvnw
COPY backend/.mvn backend/.mvn
COPY backend/pom.xml backend/pom.xml

# Build Java application
WORKDIR /app/backend
RUN chmod +x mvnw && ./mvnw clean package -DskipTests

# Final stage
FROM openjdk:17-jdk-slim

# Install Python and required tools
RUN apt-get update && apt-get install -y \
    python3 \
    python3-pip \
    curl \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy Java built application
COPY --from=java-builder /app/backend/target/*.jar ./backend/app.jar

# Copy AI service
COPY ai_service/ ./ai_service/

# Install Python dependencies
WORKDIR /app/ai_service
RUN pip install --no-cache-dir -r requirements.txt

# Copy database scripts
COPY database/ /app/database/

# Copy start scripts
COPY start_all.sh /app/
COPY start_all.ps1 /app/

WORKDIR /app

# Expose ports
EXPOSE 8080 5000 3306

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8080/health || exit 1

# Start both services
CMD ["sh", "-c", "java -jar ./backend/app.jar & python3 ./ai_service/app.py & wait"]
