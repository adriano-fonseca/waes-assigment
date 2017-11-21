# The architecture

This project uses maven 3.3 or higher and was developed using Java 8 through the JavaEE Framework. It was tested in a Wildfly 10, that have a Data Source called AppDS. There are two ways to run this application.

**1** Running in a container Java Like WildFly or Jboss

Build the .war file and deploy it, copying this file in the deployments folder in a wildfly or Jboss EAP. 

To build run the war

```
mvn clean package
```
PS: You will need to have Data Source AppDS set in your server.


**2** Running the application in docker

Just copy the docker-compose.yml to some directory on yout machine and run:


```
 docker-compose up
```

PS: You will need to have docker 1.13 or Higher and docker-compose 1.10 or higher.


a **docker-compose.yml**, that uses some images that I have build and pushed to docker hub to run this assigment.



```yaml
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

```xml

	<plugin>
		<groupId>com.spotify</groupId>
		<artifactId>docker-maven-plugin</artifactId>
		<version>0.4.12</version>
		<configuration>
			<serverId>docker-hub</serverId>
			<imageName>adrianofonseca/waes</imageName>
			<baseImage>adrianofonseca/wildfly-base-image</baseImage>
			<resources>
				<resource>
					<targetPath>/opt/jboss/wildfly/standalone/deployments</targetPath>
					<directory>${project.build.directory}</directory>
					<include>${project.build.finalName}.war</include>
				</resource>
			</resources>
			<imageTags>
				<imageTag>${project.version}</imageTag>
				<imageTag>latest</imageTag>
			</imageTags>
		</configuration>
	</plugin>

```

**Images availiable in dockerhub**

![Alt](https://github.com/adriano-fonseca/waes-assigment/blob/master/src/main/prints/docker_hub.png?raw=true "Docker Hub")




# The assignment

* Provide 2 http endpoints that accepts JSON base64 encoded binary data on both endpoints o <host>/v1/diff/<ID>/left and <host>/v1/diff/<ID>/right

I could have the v1 accessible on the '/' setting this information in the jboss-web.xml localized in the WEB-INF Folder, but I decide to keep as usual 
in Jboss server where each application raises a context. I did that for make a maven plugin (jaxrs-analyzer-maven-plugin), work properly and automatically 
to generate a documentation to API using Swagger.

That documentation are available in **http://<host>:8080/waes/apidocs**

![Alt](https://github.com/adriano-fonseca/waes-assigment/blob/master/src/main/prints/swaggerUI.png?raw=true "Swagger UI")

So, to access the endpoints you need to hit:

 **http://<hots>:8080/waes/v1/diff/<ID>/left**
 **http://<hots>:8080/waes/v1/diff/<ID>/right**
 
# The test environment 

To Unit Test I am running Junit With Mockito and Powermock to test static classes.
To Integration Tests I am using Arquilian, to run Unit and Integration tests run the command bellow and the Arquillian chameleon will download a Wildfly container and will configure
a H2 Database to run the integration tests.


```

mvn clean install -Pwildfly-as-managed -P tests

```


 
* The provided data needs to be diff-ed and the results shall be available on a third end point o <host>/v1/diff/<ID>

 **http://<hots>:8080/waes/v1/diff/<ID>**


• The results shall provide the following info in JSON format

* If equal return:


```json
{
    "status": "EQUALS",
}

```

* If not of equal size just return:

```json

{
    "STATUS": "DIFFERENT_LENGHTS"
}

```
* If of same size provide insight in where the diffs are, for example:


```json

{
    "STATUS": "SAME_LENGHTS",
    "DIFFERENCES": "[2]"
}

```


# Assumptions


There are more than one way to approach the problem, the simplest would be have just one entity (Diff), that will have to binary data (left and right). 
Assuming tha this "API" Will continue to evolve I decide by one alternative approach to have an entity called Diff that have relationships with
another entity called Data. I chose this approach, because having this data separation would be simple to introduce another entity between this two, that
could establish a relationship among them, doing that we could have diff that combine datas already persisted. For example:

```
Diff(1) compares Data(1) and Data(2)
Diff(2) compares Data(2) and Data(3)
Diff(3) compares Data(1) and Data(3)
```

Of course this is an improvement suggestion. Currently we are on the middle of the path, but we could implement that with litle affort. 



# Extras

## About API

```

**DELETE** Diff - Sending DELETE HTTP Verb to  **http://<hots>:8080/waes/v1/diff/<ID>**
**GET** All Diffs  - Sending GET HTTP Verb to  **http://<hots>:8080/waes/v1/diff/>**
**GET** Data from a specific Diff - **http://<hots>:8080/waes/v1/diff/<ID>/Data>**

```

## About architecture

This project are Ready to be a multi-stage pipeline and enable to produce the Java artefact and The docker image to this App, this brings huge 
advantages to scale the application horizontally. The docker compose is written in the version 3, which makes possible deploy this application as
stack and easelly scale up and down.  

# Interacting with API

There are two ways to do data. Usin the Swager in  **http://<host>:8080/waes/apidocs** or Using some Http Client. I suggest to use Postman, you can find
a export from it with the endpoints and some initial data [here](https://github.com/adriano-fonseca/waes-assigment/blob/master/src/main/postman/diff_waes_postman.json) 

![Alt](https://github.com/adriano-fonseca/waes-assigment/blob/master/src/main/prints/postman.png?raw=true "Postman")



# Improvements

In Order to pursue the HATEOS level 2 at least, I try to keep the conventions about the HTTP Verbs, to use post to create data, delete to remove and get
to retrieve them. When some change in Data is tried using POST The system generate a HTTP 405 (method not allowed). Therefore, a nice improvement would be 
implement the endpoints to update left and right data. 
From the architecture view I think that would be interesting to put a NGINX as reverse proxy to the application providing https and the requirements about to have a url in the format <host>/v1/diff/<ID>/. Another thing to be improved is the security a JWT could be applied to authenticate the requests to the API.

# Disclaimers

I know that documentaion and test could be more extensive, however the time was a little tight to everything that I would like to do.
