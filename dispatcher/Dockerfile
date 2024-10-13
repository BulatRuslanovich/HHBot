FROM maven:3.8-openjdk-17-slim

ENV HOME=/home/app
ENV MVN_SETTINGS=/usr/share/maven/ref/settings.xml

RUN mkdir -p ${HOME} \
    && mkdir -p /usr/share/maven/ref \
    && mkdir -p ${HOME}/dispatcher

ADD ./properties/settings.xml /usr/share/maven/ref/

ADD ./dispatcher ${HOME}/dispatcher
RUN mvn -f ${HOME}/dispatcher/pom.xml -s ${MVN_SETTINGS} clean package

CMD ["java", "-jar", "/home/app/dispatcher/target/dispatcher-0.0.1.jar"]