# Use the official Gradle image with JDK 18 as the base image
FROM gradle:jdk18 AS builder

# Set the working directory
WORKDIR /usr/src/app

# Copy the Gradle files
COPY ./app/build.gradle.kts ./settings.gradle.kts ./
COPY ./app ./app/

# Build the application
RUN gradle clean build

# Create the final image with JRE 18
FROM openjdk:17-jdk-slim

# Set the working directory
WORKDIR /app

# Copy the built JAR from the builder stage
COPY --from=builder /usr/src/app/app/build/libs/app.jar ./

# Expose the port
EXPOSE 9090-9099

# Run the application
CMD ["java", "-jar", "app.jar"]