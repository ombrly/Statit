# Use a lightweight Java 17 image (update the version if you are using Java 21, etc.)
FROM eclipse-temurin:17-jdk-alpine

# Set the working directory inside the container
WORKDIR /app

# Copy your compiled Java application into the container
# (Adjust "target/ranking-system.jar" to match your actual build output path)
COPY target/ranking-system.jar app.jar

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]