FROM gradle:7.6-jdk17 AS build

ENV HOME=/home/app

ADD ./properties/settings.xml /usr/share/maven/ref/

ADD ./node ${HOME}/node
RUN gradle clean build -p ${HOME}/node

CMD ["java", "-jar", "/home/app/node/build/libs/node-0.0.1.jar"]