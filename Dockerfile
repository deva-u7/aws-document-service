# Use a minimal JDK image for running the application
FROM openjdk:17
# Set the working directory for the runtime container
WORKDIR /app

# Copy the existing JAR file into the container
COPY ./target/document-service-0.0.1-SNAPSHOT.jar ./document-service.jar

# Expose the port that the application will run on
EXPOSE 8080

# Command to run the application
ENTRYPOINT ["java", "-jar", "todo-service.jar"]
