# RDF4J Database Driver

The RDF4J database driver is built using the [Dashjoin SDK](https://github.com/dashjoin/platform/tree/master/dashjoin-sdk) 
which allows running it in its own container.

Run:

```
docker run --name rdf4j -p 8082:8082 dashjoin/rdf4j
```

Or if you're building locally, you can start the process using:

```
mvn compile quarkus:dev -Denforcer.skip -Ddebug=5006
```

Test:

```
http://localhost:8082/swagger-ui/
```

To start the main system:

```
docker run --name dashjoin -p 8080:8080 -e DJ_ADMIN_PASS=djdjdj dashjoin/platform
```

Login using admin / djdjdj

```
http://localhost:8080/
```

On the main system, use this database config (http://localhost:8080/#/config/dj-database) to access the remote container (use url http://localhost:8082/database if you're not using docker):

```
{
    "djClassName": "org.dashjoin.service.RemoteDatabase",
    "ID": "dj/rdf4j",
    "url": "http://host.docker.internal:8082/database",
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
* dashjoin.database.datasets: list of resources to load into the memory database
* dashjoin.database.ontologies: list of resources that comprise the ontology (default is to inspect the database)
* dashjoin.database.language: optional literal language. When specified, sets this language to string literals. When reading a single value string, prefers this language

So for instance if you'd like to use a blank local RDF store, you can start the container by setting the dashjoin.database.mode and dashjoin.database.folder environment variables using the -e command line option:

```
docker run --name rdf4j -p 8082:8082 -e DASHJOIN_DATABASE_MODE=local -e DASHJOIN_DATABASE_FOLDER=. dashjoin/rdf4j
```
