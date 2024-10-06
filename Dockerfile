# Build stage
FROM openjdk:17-jdk-slim AS build

# Set the working directory
WORKDIR /app

# Copy the Gradle wrapper files
COPY gradlew ./gradlew
COPY gradlew.bat ./gradlew.bat
COPY gradle ./gradle

# Copy the project build files
COPY build.gradle.kts settings.gradle.kts gradle.properties ./

# Copy the source code
COPY src ./src

# Make the gradlew script executable
RUN chmod +x ./gradlew

# Build the application without running tests
RUN ./gradlew clean build --no-daemon -x test

# List files in /app/build/libs/ to debug if the JAR is created
RUN ls -l /app/build/libs/

# Application stage
FROM openjdk:17-jdk-slim

# Copy the built application from the previous stage
COPY --from=build /app/build/libs/*.jar /app/app.jar

# Expose the port that your application runs on
EXPOSE 8080

# Command to run the application
CMD ["java", "-jar", "/app/app.jar"]
