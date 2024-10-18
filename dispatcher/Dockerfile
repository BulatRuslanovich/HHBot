FROM gradle:7.6-jdk17 AS build

ENV HOME=/home/app

RUN mkdir -p ${HOME} && mkdir -p ${HOME}/dispatcher

WORKDIR ${HOME}/dispatcher

ADD ./dispatcher ${HOME}/dispatcher
RUN gradle clean build -x test

CMD ["java", "-jar", "/home/app/dispatcher/build/libs/dispatcher-0.0.1.jar"]