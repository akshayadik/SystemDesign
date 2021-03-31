# Social Media Application
This is sample social media application design prototype. Prototype demonstrate the create new post, follow/unfollow another user and display 20 most recent post in users news feed.

## Getting started
Project consist of 4 services
- Gateway Service: This is gateway service which will connect to service discovery service and access functionality provided by User and News Feed service. Service is implemented in Spring Cloud API gateway and Eureka Client service.
- Service Discovery Service: This is central service where all other services are registers as a client. Service discovery is implemented using Netflix Eureka server.
- User Service: This service perform the operation such as create Post, follow/unfollow functionality. Data created by this service is stored in Redis in memory nosql database.
- News Feed Service: This Service connects to User Service using Feign and pull the latest 20 latest post synchronously. This is idependent service and can be replaced with more reliable asychronous solution such as messaging queue using Kafka. Since data is in memory, retrieval is fast.

### Design & Data Model
- Modular mircorservices design to minimise coupling and horizantal scaling
- Microservices are self containtd and within bounded context
- In order to have high availability, In memory nosql solution is used. This can be replaced or enhanced as part of future work
- Load balancer and caching for better performance
- Model used in User service: User, Post, UserFollow
  User -> userId, name, email, dateOfBirth, creationDate, lastLogin
  Post -> postId, description, creationDate, userId
  UserFollow -> userFollowId, userId, type, followers(User)
- Model used in News Feed service: NewsFeed
  NewsFeed -> userId, description, creationDate
- Container based deployment which can be enhanced to implement CI/CD

### TODO
- Monitoring - can use Spring Atuators
- Security - Internal service endpoints are not accessigble externally. Gaytway can be secured using JWT/OAuth

### Prerequisites
- In order to run the application, user must have Docker installed
- Install Redis
- Postman for testing HTTP endpoint
```
docker run --name redis --hostname redis -p 6379:6379 -d redis
```

### Installing
Each project has Docker file. Below command can be used to start the service.
Build project using maven command
```
mvn clean install
```
Start the service discovery service
```
docker build -t servicediscovery .

[+] Building 9.5s (10/10) FINISHED
 => [internal] load build definition from Dockerfile                                                                                                          0.2s
 => => transferring dockerfile: 281B                                                                                                                          0.0s
 => [internal] load .dockerignore                                                                                                                             0.3s
 => => transferring context: 2B                                                                                                                               0.0s
 => [internal] load metadata for docker.io/library/java:8-jdk-alpine                                                                                          2.3s
 => CACHED [1/5] FROM docker.io/library/java:8-jdk-alpine@sha256:d49bf8c44670834d3dade17f8b84d709e7db47f1887f671a0e098bafa9bae49f                             0.0s
 => [internal] load build context                                                                                                                             0.2s
 => => transferring context: 92B                                                                                                                              0.0s
 => [2/5] RUN mkdir /usr/app                                                                                                                                  1.5s
 => [3/5] COPY ./target/servicediscovery-0.0.1-SNAPSHOT.jar /usr/app                                                                                          0.8s
 => [4/5] WORKDIR /usr/app                                                                                                                                    0.5s
 => [5/5] RUN sh -c 'touch servicediscovery-0.0.1-SNAPSHOT.jar'                                                                                               2.0s
 => exporting to image                                                                                                                                        1.8s
 => => exporting layers                                                                                                                                       1.5s
 => => writing image sha256:6b69a40d01e4730d95e7069ba9cf1deb89f75e10cb059a903a99212e3443dc95                                                                  0.1s
 => => naming to docker.io/library/servicediscovery                                                                                                           0.1s
```
Verify the image in docker
```
docker images
REPOSITORY         TAG       IMAGE ID       CREATED              SIZE
servicediscovery   latest    6b69a40d01e4   About a minute ago   234MB
redis              latest    a617c1c92774   12 days ago          105MB
```
Run the docker container
```
docker run -p 9090:9090 servicediscovery

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v2.4.4)

2021-03-25 06:30:29,255 INFO  [background-preinit] org.hibernate.validator.internal.util.Version: HV000001: Hibernate Validator 6.1.7.Final
2021-03-25 06:30:29,302 INFO  [main] org.springframework.boot.StartupInfoLogger: Starting ServicediscoveryApplication v0.0.1-SNAPSHOT using Java 1.8.0_111-internal
 on 938204a8dff0 with PID 1 (/usr/app/servicediscovery-0.0.1-SNAPSHOT.jar started by root in /usr/app)
2021-03-25 06:30:29,334 INFO  [main] org.springframework.boot.SpringApplication: No active profile set, falling back to default profiles: default
2021-03-25 06:30:34,805 INFO  [main] org.springframework.cloud.context.scope.GenericScope: BeanFactory id=f3eb8a44-9013-3020-86f2-591ce7d0c459
2021-03-25 06:30:35,815 INFO  [main] org.springframework.boot.web.embedded.tomcat.TomcatWebServer: Tomcat initialized with port(s): 9090 (http)
2021-03-25 06:30:35,878 INFO  [main] org.apache.juli.logging.DirectJDKLog: Initializing ProtocolHandler ["http-nio-9090"]
```
Perform above steps for other project
```
mvn clean install
docker build -t gateway .
docker run -p 8888:8888 gateway
docker build -t newsfeed .
docker run -p 8126:8126 newsfeed
docker build -t userservice .
docker run -p 8125:8125 userservice
```
## Running the tests
Postman tool can be used to test the project.
Test Create User service
```
localhost:8888/userservice/createuser?name=akshay&email=a@gmail.com
Sample Output:
{
    "userId": -8950636969097434627,
    "name": "akshay",
    "email": "a@gmail.com",
    "dateOfBirth": null,
    "creationDate": null,
    "lastLogin": null
}
```
Test create post endpoint
```

```
Test news feed endpoint
```
localhost:8888/newsfeed/newsfeed?userId=-8950636969097434627
Sample Output:
[
    {
        "newsFeedId": null,
        "userId": -8950636969097434627,
        "description": "hi",
        "creationDate": "2021-03-24T20:42:29.849"
    },
    {
        "newsFeedId": null,
        "userId": -8950636969097434627,
        "description": "hi",
        "creationDate": "2021-03-24T21:53:41.059"
    },
    {
        "newsFeedId": null,
        "userId": -8950636969097434627,
        "description": "hi%0Alocalhost:8888/userservice/createpost?userId=-8557533354400603078,hi%0Alocalhost:8888/userservice/createpost?userId=3890102191369401412,hi%0Alocalhost:8888/userservice/createpost?userId=3795187933465625350,hi%0Alocalhost:8888/userservice/createpost?userId=6796120783867310727,hi%0Alocalhost:8888/userservice/createpost?userId=-8950636969097434627,hi%0Alocalhost:8888/userservice/createpost?userId=-8950636969097434627,hi%0Alocalhost:8888/userservice/createpost?userId=-8950636969097434627,hi%0Alocalhost:8888/userservice/createpost?userId=-8557533354400603078,hi%0Alocalhost:8888/userservice/createpost?userId=3890102191369401412,hi%0Alocalhost:8888/userservice/createpost?userId=3795187933465625350,hi%0Alocalhost:8888/userservice/createpost?userId=-6796120783867310727,hi%0Alocalhost:8888/userservice/createpost?userId=-8950636969097434627,hi%0Alocalhost:8888/userservice/createpost?userId=-8557533354400603078,hi%0Alocalhost:8888/userservice/createpost?userId=3890102191369401412,hi%0Alocalhost:8888/userservice/createpost?userId=3795187933465625350,hi%0Alocalhost:8888/userservice/createpost?userId=6796120783867310727,hi%0Alocalhost:8888/userservice/createpost?userId=-8950636969097434627,hi%0Alocalhost:8888/userservice/createpost?userId=-8950636969097434627,hi",
        "creationDate": "2021-03-24T21:54:44.812"
    }
]
```
Test follow/unfollow user
```
localhost:8888/userservice/follow?followerId=-8950636969097434627&followeeId=6796120783867310727
or
localhost:8888/userservice/unfollow?followerId=-8950636969097434627&followeeId=6796120783867310727
Sample Output:
{
    "userFollowId": 7970501073134487700,
    "userId": -8950636969097434627,
    "type": "FRIEND",
    "followers": [
        {
            "userId": 3890102191369401412,
            "name": "abhay",
            "email": "abhay@gmail.com",
            "dateOfBirth": null,
            "creationDate": "2021-03-24T21:40:56.275",
            "lastLogin": null
        },
        {
            "userId": 3795187933465625350,
            "name": "pratap",
            "email": "pratap@gmail.com",
            "dateOfBirth": null,
            "creationDate": "2021-03-24T21:41:08.278",
            "lastLogin": null
        }
    ]
}
```
## Authors

* **Akshay Adik** - *Initial work*

## License

This project is created as part for educational purpose.

## Acknowledgments
* Referred documentation related to Spring, Spring API gateway, Feign, Redis, Netflix Eureka etc.
