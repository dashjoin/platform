# Dashjoin Microservice SDK

The purpose of this SDK is to allows developers to create custom functions and database implementations and deploy them
as a standalone microservice.

Specifically, this SDK

* provides a sample environment for Java-based implementations
* provides an OpenAPI specification that can be implemented in other programming languages

## Java / Quarkus / Maven Environment

In order to package your java function or database implementation, simply create a maven project
that includes the libraries dashjoin-core and dashjoin-sdk.
Your code must implement the org.dashjoin.function.Function<ARG, RET> or 
org.dashjoin.service.Database interfaces.
The configuration of this code can be achieved via the Quarkus application.properties file.
As you can see in the example below, the structure follows the JSON configuration structure:

```
# Pointer to implementation classes
quarkus.index-dependency.dashjoin.group-id=org.dashjoin
quarkus.index-dependency.dashjoin.artifact-id=dashjoin-sdk

quarkus.http.port=8081

dashjoin.database.djClassName=org.example.MyDatabase
dashjoin.database.key=value

dashjoin.function.myfunction1.djClassName=org.example.MyFunction1

dashjoin.function.myfunction2.djClassName=org.example.MyFunction2
dashjoin.function.myfunction2.key=value
```

This configuration defines 

* a database service using org.example.MyDatabase with config settings key=value (note that one service can only expose a single database)
* a function service called myfunction1 using org.example.MyFunction1
* a function service called myfunction2 using org.example.MyFunction2 with config settings key=value

Once the quarkus plugin is configured in the pom.xml, the container can be started using:

```
mvn compile quarkus:dev
```

## Configuring Remote Functions

Functions deployed as microservices can be accessed using the RestJson function:

```
{
  "djClassName" : "org.dashjoin.function.RestJson",
  "ID" : "myfunction1",
  "url" : "http://.../function/run/myfunction1",
  "contentType" : "application/json",
  "method" : "POST"
}
```

## Configuring Remote Databases

Databases deployed as microservices can be accessed using the RemoteDatabase client:

```
{
  "djClassName" : "org.dashjoin.service.RemoteDatabase",
  "ID" : "dj/remote",
  "url" : "http://.../database",
  "name" : "remote",
}
```
