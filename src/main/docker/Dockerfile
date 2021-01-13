FROM openjdk:8-jre-alpine

ENV SPRING_OUTPUT_ANSI_ENABLED=ALWAYS \
    JHIPSTER_SLEEP=0 \
    JAVA_OPTS=""

# Add a jhipster user to run our application so that it doesn't need to run as root
# RUN adduser -D -s /bin/sh jhipster
# WORKDIR /home/jhipster

ADD *.jar app.jar

CMD java -jar app.jar 

