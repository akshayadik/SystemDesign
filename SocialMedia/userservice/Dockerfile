From java:8-jdk-alpine
RUN mkdir /usr/app
Copy ./target/userservice-0.0.1-SNAPSHOT.jar /usr/app
WORKDIR /usr/app
RUN sh -c 'touch userservice-0.0.1-SNAPSHOT.jar'
ENTRYPOINT ["java","-jar", "userservice-0.0.1-SNAPSHOT.jar"]