FROM gradle:7.6-jdk17 AS build

ENV HOME=/home/app

RUN mkdir -p ${HOME} && mkdir -p ${HOME}/node

ADD ./node ${HOME}/node
WORKDIR ${HOME}/node
RUN gradle clean build -x test

CMD ["java", "-jar", "/home/app/node/build/libs/node-0.0.1.jar"]