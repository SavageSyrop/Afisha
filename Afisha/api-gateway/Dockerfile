FROM maven:3.8.3-openjdk-17 AS builder

# add pom.xml and source code
ADD ./pom.xml pom.xml
ADD ./src src/
# package jar
RUN mvn clean package

# Second stage: minimal runtime environment
FROM openjdk:17-jdk-slim

# copy jar from the first stage
COPY --from=builder /api-gateway/target/api-gateway-1.0-SNAPSHOT.jar /api-gateway/src/main/docker

WORKDIR /api-gateway/src/main/docker
ENTRYPOINT ["java", "-jar", "application.jar"]
#EXPOSE 8080
#
#CMD ["java", "-jar", "api-gateway-1.0-SNAPSHOT.jar"]