FROM maven:3.8-openjdk-17-slim

ENV HOME=/home/app
ENV MVN_SETTINGS=/usr/share/maven/ref/settings.xml

RUN mkdir -p ${HOME} \
    && mkdir -p /usr/share/maven/ref \
    && mkdir -p ${HOME}/common \
    && mkdir -p ${HOME}/node

ADD ./settings.xml /usr/share/maven/ref/

ADD ./common ${HOME}/common
RUN mvn -f ${HOME}/common/pom.xml -s ${MVN_SETTINGS} clean package

RUN mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file \
    -Dfile=${HOME}/common/target/common-0.0.1.jar \
    -DgroupId=com.bipbup \
    -DartifactId=common \
    -Dversion=0.0.1 \
    -Dpackaging=jar \
    -s ${MVN_SETTINGS}

ADD ./node ${HOME}/node
RUN mvn -f ${HOME}/node/pom.xml -s ${MVN_SETTINGS} clean package

CMD ["java", "-jar", "${HOME}/node/target/node-0.0.1.jar"]