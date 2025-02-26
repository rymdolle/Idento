FROM openjdk:21
WORKDIR /app
COPY /build/libs/Idento.jar Idento.jar
CMD ["java", "-jar", "Idento.jar"]