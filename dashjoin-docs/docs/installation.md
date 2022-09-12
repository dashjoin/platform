# Installation

The easiest and recommended way to run Dashjoin as a production service is to use the [container image.](#docker)

## Download and local developer setup

Installers and binaries for Windows, MacOS, and Linux are available at <https://download.dashjoin.com/>

Important: These binaries run Dashjoin as a developer application, [not as a service.](#run-local-dashjoin-installation-as-service)

- windows/dashjoin-exe.zip

Portable Dashjoin Platform Executable including Java Runtime.
Unzip and start dashjoin.exe in a command prompt.

- windows/dashjoin-version.msi

Dashjoin Windows Installer.

Same contents as windows/dashjoin-exe.zip plus start menu icon.

- macos/dashjoin-exe.zip

Portable Dashjoin Platform Executable including Runtime.

Unzip and start ```MacOS/Dashjoin``` in a command prompt 

Note: if you get an error saying "This Application is broken", [Apple Quarantine](https://developer.apple.com/forums/thread/666452#answers-list) needs to be cleared with this command:
```
xattr -cr Contents
```

- macos/Dashjoin-version.dmg

Dashjoin MacOS Installer

Note: if you get an error saying "This Application is broken", [Apple Quarantine](https://developer.apple.com/forums/thread/666452#answers-list) needs to be cleared with this command:
```
xattr -cr Dashjoin-*.dmg
```

- linux/dashjoin-exe.zip

Portable Dashjoin Platform Executable including Java Runtime.

Unzip and start ```bin/Dashjoin``` in a command prompt

- linux/dashjoin-version.deb

Dashjoin Linux Installer. Same contents as linux/dashjoin-exe.zip

- dashjoin-jar.zip

Generic Java Archive (JAR) for all platforms

## Creating a local Admin User

After installing Dashjoin, no user is set up in the system (a user can be defined via the environment variables - see below for more information).
To set up the local development admin user, navigate to <http://localhost:8080/#/setup>.

Choose a name, a username, and the password.
Example: Name 'Local Admin', username 'admin', password 'My.secure.pass!'

Note: this only works the very first time! After a development admin is created, no more local users can be created from the UI. To change or disable the local user, please edit or delete the files djroles.properties and djusers.properties in the application root directory.

The Dashjoin authentication is configured to allow log in using social Google or Github accounts, or to allow registration of users by e-mail and password (authentication via e-mail uses the Google Firebase authentication).

[Click here](https://www.youtube.com/watch?v=_xmFRwhbAFA) for a demo video.

## Opening the Dashjoin application

To access the application, navigate to <http://localhost:8080>

## Docker

The official container image ```dashjoin/platform``` is hosted on [dockerhub](https://hub.docker.com/r/dashjoin/platform)

```bash
docker pull dashjoin/platform
docker run -p 8080:8080 dashjoin/platform
```

Point your browser to <http://localhost:8080>.

If you would like to make the registered databases and credentials persistent, you can mount the "model" folder:

```bash
docker run -p 8080:8080 -v PERSISTENT_FOLDER:/deployments/model dashjoin/platform
```

## Run local Dashjoin installation as service

If you have installed Dashjoin locally and want to run the application as a service,
here are links on how to set up the service (not supported for production):

- [Windows](https://download.dashjoin.com/dashjoin/support/windows/)
- [Linux](https://automationrhapsody.com/install-java-application-linux-service/) (link to external site)

## Environment

Dashjoin uses the [Quarkus configuration framework](https://quarkus.io/guides/config-reference).
A Dashjoin instance can be configured using the following environment variables:

* DJ_ADMIN_USER: admin user name (defaults to "admin")
* DJ_ADMIN_PASS: admin password (default is blank)
* DJ_ADMIN_ROLES: initial admin roles (defaults to the "admin" role)
* DASHJOIN_HOME: defines the dashjoin working directory (defaults to /deployments/model when using docker or the directory where the platform was launched). If you are using a platfrom executable or installer version, the working directory is set to userhome/.dashjoin and cannot be modified
* DASHJOIN_APPURL: optional git url where an app is cloned / pulled from

For configuring HTTP ports, keystores etc. please refer to the [Quarkus HTTP reference](https://quarkus.io/guides/http-reference).
The following example shows how to change the HTTP port using the windows executable:

```bash
> set QUARKUS_HTTP_PORT=3333
> Dashjoin.exe
```

## Build Locally

Prerequisites:

* [Java](https://openjdk.java.net/) (11 or later)
* [Node](https://nodejs.org/) (12 or later)
* [Maven](https://maven.apache.org/) (3.6 or later)
* [Angular CLI](https://angular.io/cli) (11 or later)
* For Windows users: you need to create the symbolic link in "platform\dashjoin\src\main\resources\META-INF": `mklink /D resources ..\..\..\..\..\angular\dist\angular`

Dashjoin uses Quarkus as runtime framework (<https://quarkus.io>).
You can run your application in dev mode using:

```bash
platform/angular$ npm install [--legacy-peer-deps # required if nvm -version > 7.0!)
platform/angular$ ng build
platform$ mvn install
platform/dashjoin$ mvn compile quarkus:dev
```

Point your browser to <http://localhost:8080>.

You can use the provided launch file to directly run from within Eclipse (right-click on the launch file, "Run as" or "Debug as" -> "Dashjoin", 
note that you need to adjust your jdk, mvn, and working directory location in the file "Dashjoin.launch").
This requires the Eclipse Quarkus plugin (<https://quarkus.io/blog/eclipse-got-quarkused/>) and the Lombok
plugin (<https://projectlombok.org/setup/eclipse>) to be installed.
Launching with "Debug as" will also enable live coding mode.

The application can be packaged and installed locally using:
```bash
platform/dashjoin$ mvn package install
```

It produces the `dashjoin-0.0.1-SNAPSHOT-runner.jar` file in the `/target` directory.
Be aware that it’s not an _uber-jar_ as the dependencies are copied into the `target/lib` directory.

If you want to build an _uber-jar_, execute the following command:

```bash
platform/dashjoin$ mvn package -Dquarkus.package.type=uber-jar
```

The application is now runnable using `java -jar target/dashjoin-0.0.1-SNAPSHOT-runner.jar`.

### Eclipse

The Eclipse IDE can be used to develop and debug locally.
First install the [Eclipse Quarkus tools](https://marketplace.eclipse.org/content/quarkus-tools).

After cloning the Github repository, you need to import at least the following Maven projects into the Eclipse workspace:

* dashjoin
* dashjoin-core

Optional additional modules:
* dashjoin-demo
* dashjoin-kafka
* dashjoin-mongodb

The Maven dependencies need to be initialized with (right click dashjoin project folder) -> Maven -> Update Project.
The Angular UI must be built using the CLI, please refer to the previous section.

When everything was build successfully, you can use the Dashjoin.launch configuration to run or debug the platform.
The Quarkus launcher supports hot loading of resources, i.e. any changes made will be adjusted at runtime without having to restart the platform.
(Note: you will have to adjust the absolute folder references in the launch file to you own workspace settings)

## Version History

### 1.0 (May 2021)

* Launch of the platform

### 2.0 (Jan 2022)

* Extended support for databases: MongoDB, ArangoDB, RDF
* Support for graph queries: database drivers can implement graph / path queries, widgets can display graph query results and the platform has basic support for OpenCypher
* Chart improvements: support for stacked bar charts, ability to configure charts with all ChartJS options
* New Function: the "receive" function supports streaming data / IoT use cases
* App development / lifecycle: apps can be written collaboratively on GitHub, auto-installed upon platform launch
* Functions and databases can be deployed as micro-services using the RemoteDatabase driver

### 2.1 (April 2022)

* UI: HTML component, icons editor and display icon specification, run write queries from catalog, JSONata editor, MAP widget, PDF export
* ETL: streaming XML, YAML, transparent HTTP caching
* JSONata: 100% compatibility to JSONataJS, functions with multiple parameters
* Graph Search: PathQL integration
* Data Model: JSONb arrays can be foreign keys, allow specifying external ontology

### 2.5 (July 2022)

* UI
  * Components can be scheduled to redraw live data
  * Chart, table and tree widget support JSONata in addition to DB queries
  * Page variables can be set via the URL
  * Charts are clickable and navigate to the respective instance page
  * Forms support document upload
* Search: Results can be restricted per database and table
* Data Model
  * Databases, tables and properties can be assigned a "title" that is used in forms and hyperlinks
  * A description can be set for databases, tables and properties, enhancing the technical metadata with semantics
  * Support for SQL views
* SDK
  * Monaco editor integrated for editing SQL, HTML, and CSS
  * Introduced CSV parsing options
  * New openText function allows web scaping
