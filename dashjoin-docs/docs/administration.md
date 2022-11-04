# Administration

This section describes administration and operating procedures for the Dashjoin platform.

## Configuration Changes

A system is defined by the following configurations: Dashboards, layout pages, user roles, registered databases, and functions.
These settings are stored in the configuration database. For the open source version, this data is kept on the file system
in the model folder. In the docker container, this folder is located under /deployments/model. For locally installed
systems, this folder can be found under USER_HOME/.dashjoin/model.

## Configuring OpenID

The Dashjoin platform can be setup to delegate identity management to an OpenID provider such as Google, Microsoft Azure AD, Okta, or Keycloak.

### Registering the Dashjoin Application

The first step is to register the Dashjoin application in your OpenID management console.
This [example](https://docs.microsoft.com/en-us/azure/active-directory/develop/quickstart-register-app) explains the process
for Azure AD. Note that you will have to have a redirect URL such as "https://dashjoin-app.example.com/login" available.

### Configuring the OpenID Provider in Dashjoin

The Dashjoin login page can be configured via a configuration file named ```/assets/logincfg.json```.

The default config is:

```json
{
    "signInTabText": "My Dashjoin",
    "signInCardTitleText": "Sign In",
    "emailText": "E-Mail or Username",
    "registerTabText": "New User",
    "resetPasswordTabText": "Reset My Password",
    "resetPasswordInputText": "Enter your E-Mail. Password reset instructions will be sent",
    "emailLoginEnabled": true,
    "registrationEnabled": true,
    "guestEnabled": false,
    "guestLoginEnabled": true,
    "providers": "google",
    "openIdConfigs": []
}
```

The information you gathered from registering your application in the previous step can be added in the openIdConfigs array as shown in the 
following Azure AD example:

```json
{
    ...
    "openIdConfigs": [
        { 
            "domain": "dashjoin.com", 
            "name": "Dashjoin Example.com", 
            "logo": "/favicon.ico", 
            "config": { 
                "issuer": "https://login.microsoftonline.com/.../v2.0", 
                "clientId": "...", 
                "redirectUri": "https://dashjoin-app.example.com/login", 
                "scope": "openid profile email", 
                "requestAccessToken": false, 
                "strictDiscoveryDocumentValidation": false 
            } 
        }
    ]
}
```

This config fields are defined as follows:

* Domain: the domain the application is running on
* Name: Application name in the IDM
* Logo: Absolute or relative URL to the IDM logo to be displayed on teh login screen
* Issuer: URL / UUID of the IDM issuing authorizations
* Client ID: ID of the registered application in the IDM
* Redirect URI: URL of the Dashjoin login page
* Scopes: scopes are used by an application during authentication to authorize access to a user's details
* Request Access Token: obtain an Access Token, an ID Token, and optionally a Refresh Token
* Strict Discovery Document Validation: ensure that all of the endpoints provided via the ID Provider discovery document share the same base URL as the issuer parameter

You can configure multiple OpenID providers:

![Login with multiple OpenID providers](assets/openid.png)

### Creating and Assigning Application Roles

* After the application is registered within the IDM and the IDM made known to the application, you need to define
the roles an IDM user has within the application. On Azure AD, this is the "App roles" dialog. Note that these roles must match
the role names defined in the Dashjoin platform. The IDM must be configured to emit the groups as role claims. On Azure AD, this is done in the "Token configuration" dialog.

### Adding the Open ID Config to the Platform

The Open ID configuration must be stored as 
```
META-INF/resources/assets/logincfg.json
```
relative to the current working directory of the platform.

Note that the current working directory depends on the OS and the way the platform is installed / started.

The next sections list the locations on different operating systems.

#### Installed Dashjoin application


- On Windows, the default location is
```
C:\Users\<username>\AppData\Local\Dashjoin\META-INF\resources\assets\logincfg.json
```

- On Linux and MacOS, store the file relative to the location Dashjoin is launched (current working directory).
I.e. if the platform is launched from ```/home/dashjoin```, store the config at
```
/home/dashjoin/META-INF/resources/assets/logincfg.json
```

- On MacOS, you need to launch the application manually from a terminal, otherwise the working directory is ```/``` which does not allow to store the config.
The executable of the application is by default located at
```
/Applications/Dashjoin.app/Contents/MacOS/Dashjoin
```

#### Dashjoin container

The current working directory in the container is ```/deployments```.
Use the Docker -v option to mount logincfg.json to ```/deployments/META-INF/resources/assets/logincfg.json```

Command Line Example:
```
docker run --rm -p 8080:8080 -v /my/path/to/logincfg.json:/deployments/META-INF/resources/assets/logincfg.json:ro dashjoin/platform
```

* Dashjoin PaaS Cloud

[please send an email](https://dashjoin.com/#contact) to request the change.


### Minimalistic logincfg.json customization example

The following example disables all OpenID providers, disables password reset, and disables user registration.
Also it re-defines the displayed texts.

Note: all settings not specified will use their defaults (see above).

```json
{
    "signInTabText": "My New Demo App",
    "signInCardTitleText": "Enter Your Credentials",
    "registrationEnabled": false,
    "resetPasswordEnabled": false,
    "providers": ""
}
```

With this config the login dialog will look similar to this:

![Login with minimalistic config](assets/logincfg_mini.png)
## Query Performance

When hooking up large databases, you might have to perform some performance tuning in order for the platform to scale.
The query performance page (/table/config/dj-query-performance) is linked from the main database page and helps you
with this task. It shows recent query statistics in a table. The columns are defined as follows:

* query: shows the query that was run along with the database prefixed
* type: 
  * key: determining possible foreign key autocomplete values
  * search: the toolbar search 
  * query: the query editor, tables or charts
  * all: the all records table view
  * read: read from an instance page
  * update: update from an instance page 
  * create: create from a table page or the upload feature
  * delete: delete from an instance page or the upload feature
* lastRun: the time the query was last run
* count: how often was the query run
* errorCount: of these, how often did an error occur (e.g. a timeout)
* lastError: the last error message
* totalTimeMs: the runtime in milliseconds all query runs took combined
* lastTimeoutMs: optional timeout set for the last run
* lastLimit: optional limit set for the last run (does not include limits in the query)
* averageTimeMs: the average time a query evaluation took in milliseconds

The table helps you to identify queries with long runtimes. Possible remedies are:

### Creating Database Indices

All key columns should be indexed in the database in order to avoid full table sweeps
when a record is accessed by its key.

### Specific Search Queries

By default, Dashjoin will perform searches on all database tables which can be a very costly
operation. If a database contains more data, you can either opt out of it being searched, or
you can associate a query from the catalog to be used. For instance, you might want employees
to be searchable by firstname or lastname, but other tables are not relevant for searches.
In this case, you can define the following query:

```sql
select ID, NAME 
from EMP 
where NAME like concat(${search}, '%')
```

You can also use union queries to search over multiple tables. Note that in this case,
the query needs to project table, id, match:

```sql
select 'EMP', ID, NAME from EMP where NAME=${search}
union 
select 'PRJ', ID, NAME from PRJ where NAME=${search}
```

Note that the query must have a single parameter "search" in order to be used this way.
The platform will run this query by replacing the search parameter with the contents of the search field.

### Global Timeout Settings

Finally, the system configuration page allows setting some global contraints that prevent
"rogue" queries to deteriorate the overall system and database performance.
