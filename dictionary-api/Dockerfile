FROM openjdk:8-jdk-alpine
COPY build/libs/dictionary-api-0.0.1-SNAPSHOT.jar dictionary_api.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/dictionary_api.jar"]