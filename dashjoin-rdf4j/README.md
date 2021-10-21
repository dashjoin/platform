# RDF4J Database Driver

The RDF4J database driver is built using the [Dashjoin SDK](https://github.com/dashjoin/platform/tree/master/dashjoin-sdk) 
which allows running it in its own container.

Run:

```
docker run --name rdf4j -p 8082:8082 gcr.io/djfire-1946d/rdf4j
```

Or if you're building locally, you can start the process using:

```
mvn compile quarkus:dev 
```

Test:

```
http://localhost:8082/swagger-ui/
```

On the main system, use this database config to access the remote container:

```
{
    "djClassName": "org.dashjoin.service.RemoteDatabase",
    "ID": "dj/rdf4j",
    "url": "http://localhost:8082/database",
    "name": "rdf4j"
}
```

By default, the container uses an in memory database that is bootstraped with this [dataset](https://www.dbis.informatik.uni-goettingen.de/Mondial/#LOD).
The RDF database can be configured via the following Quarkus config options:

* dashjoin.database.mode
    * memory: uses a nonpersistent memory database
    * local: uses a sesame local RDF database
    * sesame: uses a sesame HTTP database
    * client: uses a SPARQL endpoint
* dashjoin.database.folder: determines the path where the local RDF database is stored
* dashjoin.database.endpoint: the endpoint for the sesame HTTP and SPARQL repositories
* dashjoin.database.username: the username for the sesame HTTP and SPARQL repositories
* dashjoin.database.password: the password for the sesame HTTP and SPARQL repositories

So for instance if you'd like to use a blank local RDF store, you can start the container by setting the dashjoin.database.mode and dashjoin.database.folder environment variables using the -e command line option:

```
docker run --name rdf4j -p 8082:8082 -e dashjoin.database.mode=local -e dashjoin.database.folder=. gcr.io/djfire-1946d/rdf4j
```
