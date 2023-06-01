# Development / Production

If you use a Dashjoin system in production, we do not recommend making configuration changes
like defining new queries or registering new user roles using the editors on the production
system. Instead, you can setup multiple development systems where these changes are
developed and tested.

You can put the model folder under version control by following these steps:

* create a repository, for instance on GitHub
* install Dashjoin locally
* clone the repository into your Dashjoin user home directory:

```bash
USER_HOME> git clone https://github.com/ORG/PROJECT.git .dashjoin
```

* start Dashjoin and start developing
* pull changes from others
* review and commit changes:

```bash
USER_HOME/.dashjoin> git status
```

Please refer to the [demo application](https://github.com/dashjoin/dashjoin-demo) to find out about the recommended app folder structure.

## Multi Line JSON

Markdown, queries, and JSONata expressions can be hard to read if they are stored in JSON files.
You can configure the platform to externalize strings in separate text files which are linked
from the JSON data using the "externalize-config-strings" setting.
By default, the query field of entries in the query catalog is externalized as follows:

test.json:

```
{
  "ID": "test",
  "query-pointer": "test.0.sql"
}
```

test.0.sql:

```
select * from test
  where id=4
```

These two files are equivalent to:

test.json:

```
{
  "ID": "test",
  "query": "select * from test\n  where id=4"
}
```


If you'd like to also externalize widget markdown for instance, simply add "page: markdown" to "externalize-config-strings".
This specifies the table and the (possibly nested) key containing the string to be externalized.


On the production system, there are three ways of deploying an application:

## Upload to the Configuration Database

You can upload an entire model folder to the config DB. On the database page, select "Configuration Database".
Open the "Database Management" tab and select "Upload". Select the model folder there and either append or replace
the contents of the config database.

## Automatic GIT Checkout

You can specify the DASHJOIN_APPURL environment variable and have it point to your app repository.
Upon startup, the system will perform a git clone if the model folder is empty or a git pull if the model has content already.
Note that you can specify credentials via the URL (http://user:password@domain.com/).
Please refer to the [demo application](https://github.com/dashjoin/dashjoin-demo) for an example of how to run
Dashjoin with the demo application installed.
If the git operation fails (e.g. due to incorrect credentials or illegal filenames), the platform will log the error and resume the startup process.

## Manual App Installation

Last but not least, you can also copy the app into the Dashoin working directory using other means
before starting the platform. 
If you are using containers, you can mount the model folder under /deployments/model.

## Specifying Development Resources

Resources like databases and REST endpoints are critical resources when working with Dashjoin.
Therefore, it is quite common to use different sets of resources for development and production.
As described in the section on automatic GIT checkout above, the production credentials are usually
checked into the code repository. During development, you can use [environment](../installation/#environment)
variables to specify alternative values for url, username, hostname, port, database, and password to be used for functions and databases as follows:

* dashjoin.database.NAME OF THE DATABSE.url: URL to use to connect to the database (overrides the url field in the DB's json file)
* dashjoin.function.NAME OF THE FUNCTION.url: URL to use to connect to the REST service (overrides the url field in the function's json file)

To change the username, simply replace url with username in the examples above.
Note that the development passwords are provided in plain text.

## Unit Testing

Unit tests are an important asset to ensure the quality of your app. Dashjoin leverages the [JUnit framework](https://junit.org/) and allows you to perform the following default tests to make sure that

* all JSON files can be parsed
* layout uses legal widget names
* all JSONata expressions are syntactically correct

In addition to these syntactical checks, it is possible to run JSONata expressions and provide desired outputs. 

To setup unit tests in your app, follow these steps:

* Install [Maven](https://maven.apache.org/)
* Copy this [maven project file](https://github.com/dashjoin/dashjoin-demo/blob/main/pom.xml) to your app's root directory
* Copy this [JUnit test file](https://github.com/dashjoin/dashjoin-demo/blob/main/src/test/java/org/dashjoin/app/AppTest.java) to "src/test/java/org/dashjoin/app"
* If you would like to test a JSONata expression, create a [JSON test file](https://github.com/dashjoin/dashjoin-demo/blob/main/test.json) that describes the file containing the expression, where the expression is located, which test cases should be run, and which outputs are to be expected. The file is structured as follows:

```
{
    "test": {
        "file": path to the file containing the expression to be checked
        "expression": JSONata expression that selects the expression to be checked
    },
    "basedata": optional common test data for the test cases
    "cases": {
        "name": {
            "data": test data (will be merged with the base data)
            "expected": expected JSONata output
        }
        ...
    }
}
```

To run the unit test:

```
dashjoin-demo>mvn test
```
