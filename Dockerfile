# Use a base image with JDK
FROM openjdk:19-jdk-slim

# Set working directory
WORKDIR /app

# Copy gradle wrapper and build files
COPY gradlew gradlew.bat build.gradle.kts settings.gradle.kts ./
COPY gradle/ gradle/

# Copy source code
COPY src/ src/

# Make gradlew executable
RUN chmod +x ./gradlew

# Build the application
RUN ./gradlew build --no-daemon

# Expose port (if your bot needs to listen to webhooks)
# EXPOSE 8080

# Run the application
CMD ["./gradlew", "run", "--no-daemon"]