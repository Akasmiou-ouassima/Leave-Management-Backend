FROM openjdk:17-jdk

WORKDIR /app

COPY target/sid-0.0.1-SNAPSHOT.jar /app/LeaveManagementBackend.jar

EXPOSE 8080

CMD ["java", "-jar", "LeaveManagementBackend.jar"]
