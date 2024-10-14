FROM gradle:7.6-jdk17 AS build

ENV HOME=/home/app

RUN mkdir -p ${HOME} && mkdir -p ${HOME}/dispatcher

ADD ./dispatcher ${HOME}/dispatcher
RUN gradle clean build -p ${HOME}/dispatcher

CMD ["java", "-jar", "/home/app/dispatcher/build/libs/dispatcher-0.0.1.jar"]