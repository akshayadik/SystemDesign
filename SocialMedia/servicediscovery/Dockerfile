From java:8-jdk-alpine
RUN mkdir /usr/app
Copy ./target/servicediscovery-0.0.1-SNAPSHOT.jar /usr/app
WORKDIR /usr/app
RUN sh -c 'touch servicediscovery-0.0.1-SNAPSHOT.jar'
ENTRYPOINT ["java","-jar", "servicediscovery-0.0.1-SNAPSHOT.jar"]