FROM openjdk:21-slim

WORKDIR /app

COPY build.gradle gradlew settings.gradle ./
COPY gradle/ gradle/
COPY gradle.properties ./

RUN apt-get update && apt-get install -y findutils
RUN chmod +x gradlew
RUN ./gradlew wrapper

COPY src/ src

RUN ./gradlew build -x test

ENTRYPOINT ["java", "-jar", "build/libs/sleep-0.0.1-SNAPSHOT.jar"]