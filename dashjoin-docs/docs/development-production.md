# Development / Production

If you use a Dashjoin system in production, we do not recommend making configuration changes
like defining new queries or registering new user roles using the editors on the production
system. Instead, you can setup multiple development systems where these changes are
developed and tested.

## Development Container

The most convenient way to setup a development system is by using docker development container.
This container includes all the required tools and setup.
To start the container, run:

```
docker run -p 3000:3000 -p 8080:8080 -p 8081:8081 -e DJ_ADMIN_PASS=djdjdj dashjoin/development
```

If you would like to work on an existing app that is located on a Git repository, also specify the DASHJOIN_HOME and
DASHJOIN_APPURL parameters (see section automatic Git checkout below)

```
docker run -p 3000:3000 -p 8080:8080 -p 8081:8081 -e DJ_ADMIN_PASS=djdjdj -e DASHJOIN_HOME=dashjoin-demo -e DASHJOIN_APPURL=https://github.com/dashjoin/dashjoin-demo dashjoin/development
```

The development container works like the platform container, but offers two additional services.
On port 8081, it allows connecting a browser based integrated development environment (expert mode) to the container.
On port 3000 an additional web service provides access to the user interface that includes
custom widgets you add to the app.

## Expert Mode

To open the expert mode, open the general information page and follow the expert mode link under "App".
To log in, please use the password you specified for the admin user (djdjdj in our examples).
This opens VS Code in your browser. For more information about VS Code, please refer to
[this documentation](https://code.visualstudio.com/docs).

The project that is open in VS Code represents the app you are writing.
Adding a query in the query catalog, for example, will create a JSON file in
model/dj-query-catalog. If you change the file in VS Code, the change will be
visible immediately in the query catalog as well.

### App Folder Structure

The contains the following folders:

* assets: any media that should be available via http(s)://<app url>/assets
* model
  * dj-config: Contains any system configuration
  * dj-database: All connected databases and any table and record page layouts. If a database JSON file is open in the editor, you can test connecting to it via the "Connect to this database" button at the top of the file
  * dj-function: All functions defined on the functions page. Also offers the ability to "Run this function". Note that passing parameters to the function is not supported. You can use the JSONata notebook for this.
  * dj-query-catalog: All queries in the catalog. Offers the "Run this query" and "Run this query metadata" commands. Note that passing query parameters is not supported. You can use the JSONata notebook for this.
  * dj-role: All roles defined in the system
  * page: All app dashboard pages. Offers the "Open this page in the browser" button.
  * tenantusers: All tenant users
  * widget: Custom toolbar and sidebar are located here
* upload: this folder can contain files that can be accessed from JSONata functions like openJson("file:upload/...")

### Source Control

If you would like to commit and publish your changes to the production system, you can
switch to the source control tab on the left. The usual workflow is to:

* review your changes in the diff editor
* stage your change by adding the file via the + icon
* entering a message describing your change and committing the change
* publishing your change to the remote Git repository

For more details, please refer to [this guide](https://code.visualstudio.com/docs/sourcecontrol/overview).

## Developing a Custom Widget

One of the most powerful features of the expert mode is the ability to write your own widgets.
Let's assume we have a database containing ...

### TODO

## Multi Line JSON

Markdown, queries, and JSONata expressions can be hard to read if they are stored in JSON files.
You can externalize strings in separate text files which are linked
from the JSON data as follows:

test.json:

```
{
  "ID": "test",
  "query-pointer": "0.sql"
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

You can use this mechanism anytime when you're editing the files manually.
When making changes via the platform, by default, only the query strings of
the query catalog are externalized.

This is controlled by the "externalize-config-strings" setting.
If you'd like to also externalize widget markdown for instance, simply add "page: markdown" to "externalize-config-strings".
This specifies the table and the (possibly nested) key containing the string to be externalized.


On the production system, there are three ways of deploying an application:

## Upload to the Configuration Database

You can upload an entire model folder to the config DB. On the database page, select "Configuration Database".
Open the "Database Management" tab and select "Upload". Select the model folder there and either append or replace
the contents of the config database.

## Automatic Git Checkout

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
As described in the section on automatic Git checkout above, the production credentials are usually
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
