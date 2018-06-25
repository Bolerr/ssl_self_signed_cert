FROM openjdk:8-jdk-alpine
VOLUME /tmp

# add minimal troubleshoot tools
RUN apk update \
    && apk add bash \
    && apk add curl

ARG JAR_FILE
ADD ${JAR_FILE} app.jar

ARG P12_FILE
ADD ${P12_FILE} localhost.p12

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app.jar"]