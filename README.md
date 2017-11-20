# The architecture

This project uses Maven 3.3 or higher and was developed using Java 8 through the JavaEE Framework. It was tested in a Wildfly 10, that have a Data Source called AppDS. There are two ways to run this application.

**1** Running in a container Java Like WildFly or Jboss

Build the .war file and deploy it, copying this file in the deployments folder in a wildfly or Jboss EAP. 

To build run the war

```
 mvn clean package`

```
PS: You will need to have Data Source AppDS set in your server.


**2** Running the application in docker

Just copy the docker-compose.yml to some directory on yout machine and run:


```
 docker-compose up

```

PS: You will need to have docker 1.13 or Higher and docker-compose 1.10 or higher.


a **docker-compose.yml**, that uses some images that I have build and pushed to docker hub to run this assigment.



```
version: '3'
services:

  java-api:
    image: adrianofonseca/waes:latest
    ports:
     - "8080:8080"
     - "9990:9990"
    networks:
      - app
    depends_on:
      - postgres
    deploy:
     replicas: 1
     update_config:
       parallelism: 1
       delay: 30s
     restart_policy:
       condition: on-failure 
     labels: [info=backend]


  postgres:
      image: adrianofonseca/postgres:9.5
      ports:
          - "5432:5432"
      networks:
      - app
      environment:
          - DEBUG=false
          - DB_USER=app
          - DB_PASS=app
          - DB_NAME=app
      volumes:
          -  /home/adr-fonseca/docker/docker-postgres/postgresql/:/var/lib/postgresql
      deploy:
        labels: [info=database]
        placement:
          constraints: [node.role == manager]

networks:
      app:

```

The application image to docker is build with docker-maven-plugin do spotify (see on the pom.xml)

**Images availiable in dockerhub**

![](https://github.com//adriano-fonseca/waes-assigment/blob/master/src/main/prints/docker_hub.png "Docker Hub")




# The assignment

* Provide 2 http endpoints that accepts JSON base64 encoded binary data on both endpoints o <host>/v1/diff/<ID>/left and <host>/v1/diff/<ID>/right

I could have the v1 accessible on the '/' setting this information in the jboss-web.xml localized in the WEB-INF Folder, but I decide to keep as usual 
in Jboss server where each application raises a context. I did that for make a maven plugin (jaxrs-analyzer-maven-plugin), work properly and automatically 
to generate a documentation to API using Swagger.

That documentation are availiable in 

**http://<host>:8080/waes/apidocs**

So, to access the endpoints you need to hit:

 **http://<hots>:8080/waes/v1/diff/<ID>/left**
 **http://<hots>:8080/waes/v1/diff/<ID>/right**
 
# The test enviroment 

To Unit Test I am runnin Junit With Mockito and Powermock.
To Integration Tests I am using Arquilian.

To run Unit and Integration tests run the command bellow and the arquillian chameleon will download a wildfly container and will configure
a H2 Database to run the integration tests.

```

mvn clean install -Pwildfly-as-managed -P tests

```


 
* The provided data needs to be diff-ed and the results shall be available on a third end point o <host>/v1/diff/<ID>

 **http://<hots>:8080/waes/v1/diff/<ID>**


• The results shall provide the following info in JSON format

* If equal return:


```
{
    "status": "EQUALS",
}

```

* If not of equal size just return:

```

{
    "STATUS": "DIFFERENT_LENGHTS"
}

```
* If of same size provide insight in where the diffs are, for example:


```

{
    "STATUS": "SAME_LENGHTS",
    "DIFFERENCES": "[2]"
}

```



* Make assumptions in the implementation explicit, choices are good but need to be
communicated

There is mpre the one way to approach the problem, the simplest would be have just one entity (Diff), that will have to binary data (left and right). 
Assuming that this "API" Will continue to evolve I decide by one alternative approach to have an entity called Diff that have relationships with
another entity called Data. I chose this approach, because having this data separation would be simple to introduce another entity between this two, that
could establish a relationship among them, doing that we could have diff that combine datas already persisted. For example:

Diff(1) compares Data(1) and Data(2)
Diff(2) compares Data(2) and Data(3)
Diff(3) compares Data(1) and Data(3)

Of course this is an improvement suggestion. Currently we are on the middle of the path, but we could implement that with litle affort. 
In Order to persue the HATEOS level 2 at leaest a try to keep the conventions about the HTTP Verbs, to use post to create data, delete to remove and get
to retrieve them. When some change in Data is tried using POST The sistem generate a HTTP 405 (method not allowed). Therefore, a nice improvement would be 
implement the endpoints to update left and right data.


# Extras

**DELETE** Diff - Sending DELETE HTTP Verb to  **http://<hots>:8080/waes/v1/diff/<ID>**, will dele Diff with left and right data;
**GET** All Diffs  - Sending GET HTTP Verb to  **http://<hots>:8080/waes/v1/diff/>**
**GET** Data from a specific Diff - **http://<hots>:8080/waes/v1/diff/<ID>/Data>**
