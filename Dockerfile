FROM gradle:jdk21 AS build
WORKDIR /app
COPY src /app/src
COPY build.gradle.kts /app/build.gradle.kts
COPY settings.gradle.kts /app/settings.gradle.kts
RUN gradle build -x test

FROM openjdk:21
WORKDIR /app
COPY --from=build /app/build/libs/Idento.jar Idento.jar

CMD ["java", "-jar", "Idento.jar"]
