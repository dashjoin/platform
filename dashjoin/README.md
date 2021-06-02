# Welcome to Dashjoin !

Dashjoin is a next generation zero-code data integration platform.

Please support our community at https://github.com/dashjoin, or our commercial offerings at https://www.dashjoin.com

## Running the application in dev mode

Dashjoin uses Quarkus as runtime framework (https://quarkus.io).

You can run your application in dev mode that enables live coding using:
```shell script
./mvnw compile quarkus:dev
```
## Develop and debug in Eclipse

You can use the provided launch file to directly run from within Eclipse (right-click on the launch file, "Run as" or "Debug as" -> "Dashjoin").

This requires the Eclipse Quarkus plugin to be installed (https://quarkus.io/blog/eclipse-got-quarkused/).

Launching with "Debug as" will also enable live coding mode.

## Launching Dashjoin

The application can be packaged using:
```shell script
./mvnw package
```
It produces the `dashjoin-0.0.1-SNAPSHOT-runner.jar` file in the `/target` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/lib` directory.

If you want to build an _über-jar_, execute the following command:
```shell script
./mvnw package -Dquarkus.package.type=uber-jar
```

The application is now runnable using `java -jar target/dashjoin-0.0.1-SNAPSHOT-runner.jar`.
