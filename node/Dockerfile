FROM maven:3.8-openjdk-17-slim

ENV HOME=/home/app
ENV MVN_SETTINGS=/usr/share/maven/ref/settings.xml

ADD ./properties/settings.xml /usr/share/maven/ref/

ADD ./node ${HOME}/node
RUN mvn -f ${HOME}/node/pom.xml -s ${MVN_SETTINGS} clean package

CMD ["java", "-jar", "/home/app/node/target/node-0.0.1.jar"]