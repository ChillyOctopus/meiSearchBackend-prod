FROM openjdk:19

WORKDIR /app

# Copy necessary files
COPY src/serverCode/myapp.jar /app/myapp.jar
COPY lib /app/lib

# Set environment variables for file paths
ENV FILE_BASE_PATH=/app/files

# Include all JARs in the lib folder along with the main JAR
CMD ["java", "-cp", "/app/myapp.jar:/app/lib/*", "serverCode.HttpServer", "PORT"]
