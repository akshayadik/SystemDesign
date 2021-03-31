From java:8-jdk-alpine
RUN mkdir /usr/app
Copy ./target/newsfeed-0.0.1-SNAPSHOT.jar /usr/app
WORKDIR /usr/app
RUN sh -c 'touch newsfeed-0.0.1-SNAPSHOT.jar'
ENTRYPOINT ["java","-jar", "newsfeed-0.0.1-SNAPSHOT.jar"]