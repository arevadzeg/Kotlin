# Use the official Kotlin image
FROM openjdk:17-jdk-slim AS build

# Set the working directory
WORKDIR /app

# Copy the Gradle wrapper files
COPY gradlew ./
COPY gradlew.bat ./
COPY build.gradle.kts settings.gradle.kts ./

# Copy the source code
COPY src ./src

# Make the gradlew script executable
RUN chmod +x ./gradlew

# Build the application
RUN ./gradlew build -x test

# Create a new image for running the application
FROM openjdk:17-jdk-slim

# Copy the built application from the previous stage
COPY --from=build /app/build/libs/*.jar /app/app.jar

# Expose the port that your application runs on
EXPOSE 8080

# Command to run the application
CMD ["java", "-jar", "/app/app.jar"]
