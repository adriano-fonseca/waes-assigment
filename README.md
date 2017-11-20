# JAVA EE 6 Technologies and Unit and Integrating Tests

This project aim is to be a proof of concept of use of some frameworks, usually used to develop enterprise systems.
The idea is to test JPA, EJB, Hibernate in order to produce WebServices Rest and SOAP
The RESTFul API implemented in this project respects Richardson Maturity Model (HATEOAS)Level 2.

More about RichardsonMaturity Model [here](https://martinfowler.com/articles/richardsonMaturityModel.html) 

## WebServices REST

To access the services you shall use http://localhost:8080/PlayWithJavaEE/rest/{serviceName}/  

# Testing

I have configured integration tests through Arquillian. There are a few ways to run this tests, each one has a profile in pom.xml, as follow:

```xml
		<profile>
			<id>wildfly-as-managed</id>
			<properties>
				<arquillian.container.port>10090</arquillian.container.port>
				<arquillian.container>managed</arquillian.container>
			</properties>
			<dependencies>
				<dependency>
					<groupId>org.arquillian.container</groupId>
					<artifactId>arquillian-container-chameleon</artifactId>
					<version>${version.arquillian_chameleon}</version>
					<scope>test</scope>
				</dependency>
			</dependencies>
		</profile>
		<profile>
			<id>wildfly-as-remote</id>
			<properties>
				<arquillian.container.port>9990</arquillian.container.port>
				<arquillian.container>remote</arquillian.container>
			</properties>
			<dependencies>
				<dependency>
					<groupId>org.arquillian.container</groupId>
					<artifactId>arquillian-container-chameleon</artifactId>
					<version>${version.arquillian_chameleon}</version>
					<scope>test</scope>
				</dependency>
			</dependencies>
		</profile>
		<profile>
			<id>wildfly-docker</id>
			<properties>
				<launch>wildfly-docker</launch>
			</properties>
			<dependencies>
				<dependency>
					<groupId>org.arquillian.cube</groupId>
					<artifactId>arquillian-cube-docker</artifactId>
					<version>${version.arquillian_cube}</version>
					<scope>test</scope>
				</dependency>
				<dependency>
					<groupId>org.arquillian.container</groupId>
					<artifactId>arquillian-container-chameleon</artifactId>
					<version>${version.arquillian_chameleon}</version>
					<scope>test</scope>
				</dependency>
			</dependencies>
		</profile>
		<profile>
			<id>wildfly-docker-image</id>
			<properties>
				<launch>wildfly-docker-image</launch>
			</properties>
			<dependencies>
				<dependency>
					<groupId>org.arquillian.cube</groupId>
					<artifactId>arquillian-cube-docker</artifactId>
					<version>${version.arquillian_cube}</version>
					<scope>test</scope>
				</dependency>
				<dependency>
					<groupId>org.arquillian.cube</groupId>
					<artifactId>arquillian-cube-containerless</artifactId>
					<version>${version.arquillian_cube}</version>
					<scope>test</scope>
				</dependency>
			</dependencies>
		</profile>
```
Which one of them has a container qualifier associated in arquillian.xml:

```xml
    <container qualifier="chameleon" default="false">
		<configuration>
			<property name="chameleonTarget">wildfly:9.0.0.Final:${arquillian.container}</property>
			<property name="outputToConsole">true</property>
			<property name="javaVmArguments">-Xmx512m -XX:MaxPermSize=256m -Djboss.bind.address=localhost
                -Xrunjdwp:transport=dt_socket,address=8787,server=y,suspend=n
                -Djboss.socket.binding.port-offset=100
                -Djboss.management.native.port=9990
                -Duser.country=BR -Duser.language=pt -Duser.timezone=GMT-03:00
            </property>
			<!-- as portas sao diferentes entre o managed(usa offset para subir em 
				porta diferente) e remote (porta 9999) -->
			<property name="managementPort">${arquillian.container.port}</property>
		</configuration>
	</container>
	<container qualifier="wildfly-docker" default="false">
		<configuration>
			<property name="chameleonTarget">wildfly:9.0.0.Final:remote</property>
			<property name="username">admin</property>
			<property name="password">Admin#70365</property>
		</configuration>
	</container>
	<container qualifier="wildfly-docker-image" default="true">
		<configuration>
			<property name="containerlessDocker">wildfly-image</property>
			<property name="embeddedPort">8082</property>
		</configuration>
	</container>
```

Just one of them can be marked as default, up there the Arquillian is configured to run tests against a container. In that case Arquillian will use Chameleon conteiner to run tests remotely in a wildfly that runs in a docker container. 

The image docker which receives the application is published in docker hub as adrianofonseca/wildfly:9.0.0.Final. This is basecally the official wildfly image I just have added the iproute which provides ss command used by arquillian-cube.

To alternate between the just change the profile in the maven command, for instance, if you want that maven download the wildfly, start it and run the tests, just past the profile wildfly-as-managed:

**mvn clean package -Pwildfly-as-managed** (but, remember the chameleon qualifier must be marked as default true on arquillian.xml)

**PS:** to pay attention in the port number among the wildfly version.


#Data Base Configuration


For configure the data source from wildfly, you will follow the steps bellow:

1. Create the path $WILDFLY_HOME/modules/org/postgresql/main. The modules/org part should already exist, make directories for the rest.

2. In $JBOSS_HOME/modules/org/postgresql/main/module.xml with the following content, changing the resource-root entry for the PgJDBC driver to refer to the driver you wish to use.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<module xmlns="urn:jboss:module:1.1" name="org.postgresql">
     <resources>
         <resource-root path="postgresql-9.1-902.jdbc4.jar"/>
     </resources>
     <dependencies>
         <module name="javax.api"/>
         <module name="javax.transaction.api"/>
         <module name="javax.servlet.api" optional="true"/>
     </dependencies>
 </module>
 ```

3. Into the same directory as module.xml place postgresql-9.1-902.jdbc4.jar
4. Start JBoss AS
5. Open jboss-cli by running $JBOSS_HOME/bin/jboss-cli --connect
6. Run the command:

```
/subsystem=datasources/jdbc-driver=postgresql:add(driver-name=postgresql, driver-class-name=org.postgresql.Driver, driver-module-name=org.postgresql)
```

Thas is what you need to put in your standalone xml:

```xml	
			<datasource jndi-name="java:jboss/datasources/AppDS" pool-name="AppDS" enabled="true" use-java-context="true">
                    <connection-url>jdbc:postgresql://localhost:5432/app</connection-url>
                    <driver>postgresql-driver</driver>
                    <transaction-isolation>TRANSACTION_READ_COMMITTED</transaction-isolation>
                    <pool>
                        <min-pool-size>10</min-pool-size>
                        <max-pool-size>100</max-pool-size>
                        <prefill>true</prefill>
                    </pool>
                    <security>
                        <user-name>app</user-name>
                        <password>app</password>
                    </security>
                    <statement>
                        <prepared-statement-cache-size>32</prepared-statement-cache-size>
                        <share-prepared-statements>true</share-prepared-statements>
                    </statement>
                </datasource>
                 <drivers>
                    <driver name="postgresql-driver" module="org.postgresql"/>
                </drivers>
 ```