FROM gradle:jdk21-alpine AS builder
WORKDIR /home/gradle/project

COPY build.gradle settings.gradle ./
COPY gradle/ gradle/
RUN gradle dependencies --no-daemon || true

COPY src/ src/
RUN gradle clean bootJar --no-daemon

FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app

COPY --from=builder /home/gradle/project/build/libs/*.jar app.jar
EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]
