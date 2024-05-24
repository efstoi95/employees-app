# Use a Java base image
FROM openjdk:22

# Set the working directory to /app
WORKDIR /app

# Copy the Spring Boot application JAR file into the Docker image
COPY target/employees-0.0.1-SNAPSHOT.jar /app/employees-0.0.1-SNAPSHOT.jar

# Run the Spring Boot application when the container starts
CMD ["java", "-jar", "employees-0.0.1-SNAPSHOT.jar"]