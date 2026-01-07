# Appendix: Databases, Queries, Functions, and Configurations
## Query Catalog
### Query with permissions
a query on database 'dj/northwind' called 'group', executable for the 'authenticated' role
```json
{
  "ID" : "group",
  "database" : "dj/northwind",
  "query" : "SELECT CUSTOMERS.COUNTRY, COUNT(*) AS \"Number of Customers\" FROM CUSTOMERS GROUP BY CUSTOMERS.COUNTRY",
  "roles" : [ "authenticated" ],
  "type" : "read"
}
```
### Query with parameters
a query on database 'northwind' called 'list'. Parameters limit and offset can be passed to the query
```json
{
  "ID" : "list",
  "database" : "dj/northwind",
  "query" : "select * from CATEGORIES limit ${limit} offset ${offset}",
  "type" : "read",
  "arguments" : {
    "limit" : {
      "type" : "number",
      "sample" : "5"
    },
    "offset" : {
      "type" : "number",
      "sample" : "0"
    }
  }
}
```
### Calling a stored procedure
stored procedure 'sp' on database 'postgres' called with parameter 'test'
```json
{
  "ID" : "sp",
  "database" : "dj/postgres",
  "query" : "CALL my_sp('test')",
  "type" : "read"
}
```
## Databases
### Database connection
postgres database connection information with encrypted password
```json
{
  "name" : "postgres",
  "djClassName" : "org.dashjoin.service.SQLDatabase",
  "username" : "postgres",
  "url" : "jdbc:postgresql://localhost:5432/postgres",
  "ID" : "dj/postgres",
  "password" : "DJ1#\b/gbzX8DDZa1lVaiLat0HdX9cDST2KHJk"
}
```
### Database audit log trigger
sqlite database definition. The before-update trigger is called accordingly and logs an audit record to the table audit
```json
{
  "name" : "sqlite",
  "ID" : "dj/sqlite",
  "djClassName" : "org.dashjoin.service.SQLDatabase",
  "url" : "jdbc:sqlite:dashjoin-demo.db",
  "tables" : {
    "REQUESTS" : {
      "before-update" : "$create('db', 'audit', {'timestamp': $now(), 'user': user, 'operation': command, 'payload': object})"
    }
  }
}
```
### Database with initial create table
sqlite database definition with init script that contains: CREATE TABLE IF NOT EXISTS MY_TABLE(ID INT PRIMARY KEY, NAME VARCHAR(255))
```json
{
  "name" : "sqlite",
  "ID" : "dj/sqlite",
  "djClassName" : "org.dashjoin.service.SQLDatabase",
  "url" : "jdbc:sqlite:dashjoin-demo.db",
  "initScripts" : [ "upload/init.sql" ]
}
```
### Database with foreign key
sqlite database definition with a foreign key pointing to the CUSTOMERS table in the northwind database
```json
{
  "name" : "sqlite",
  "ID" : "dj/sqlite",
  "djClassName" : "org.dashjoin.service.SQLDatabase",
  "url" : "jdbc:sqlite:dashjoin-demo.db",
  "tables" : {
    "REQUESTS" : {
      "properties" : {
        "customer" : {
          "ref" : "dj/northwind/CUSTOMERS/CUSTOMER_ID",
          "displayWith" : "fk"
        }
      }
    }
  }
}
```
### Database with foreign key array
postgres database definition with an array of foreign keys pointing to the CUSTOMERS table in the northwind database
```json
{
  "name" : "postgres",
  "djClassName" : "org.dashjoin.service.SQLDatabase",
  "username" : "postgres",
  "password" : "DJ1#\bApQHRIfZwu6WSIJrlI2aBqbMhnLRPlsg",
  "url" : "jdbc:postgresql://localhost:5432/postgres",
  "ID" : "dj/postgres",
  "tables" : {
    "test" : {
      "properties" : {
        "arr" : {
          "type" : "array",
          "items" : {
            "ref" : "dj/northwind/CUSTOMERS/CUSTOMER_ID",
            "type" : "string",
            "displayWith" : "fk"
          }
        }
      }
    }
  }
}
```
### Database with record label
EMPLOYEES table defines the record label to be the LAST_NAME. All links and page titles for EMPLOYEE records use the LAST_NAME column as labels
```json
{
  "ID" : "dj/northwind",
  "name" : "northwind",
  "parent" : "dj",
  "djClassName" : "org.dashjoin.service.SQLDatabase",
  "url" : "jdbc:h2:mem:northwind",
  "tables" : {
    "EMPLOYEES" : {
      "dj-label" : "${LAST_NAME}"
    }
  }
}
```
## Function Catalog
### Invoke
Function that adds two numbers passed in the argument object. It can be called via $call('add') or via REST
```json
{
  "ID" : "add",
  "djClassName" : "org.dashjoin.function.Invoke",
  "expression" : "{'result': x+y}",
  "roles" : [ "authenticated" ],
  "type" : "read"
}
```
### RestJson
Function that calls a web service. The fields of the function argument are used to construct the URL via from string template
```json
{
  "djClassName" : "org.dashjoin.function.RestJson",
  "url" : "https://api.geoapify.com/v1/geocode/search?street=${street}&postcode=${postcode}&city=${city}&country=${country}&apiKey=...",
  "method" : "GET",
  "contentType" : "application/json",
  "ID" : "address"
}
```
### Credentials
Encrypted credentials for OpenAI to be used in $curl and $chat functions
```json
{
  "ID" : "openai",
  "djClassName" : "org.dashjoin.function.Credentials",
  "username" : "Authorization",
  "password" : "DJ1#\b7Zw3EGtmVKaDuwwOtwXfWDG1y+awbon7WNQp9NmJ6EgUXEpYUMC8O7zRUw2kSnDxyATO0R3ke3NxjaT9zCwYyDGS5VDgYt/L",
  "apiKey" : true
}
```
### ETL
Extract load transform function. Loads the result of 'expression' into the database sqlite. The data is mapped using 'mappings'
```json
{
  "djClassName" : "com.dashjoin.function.ETL",
  "database" : "sqlite",
  "ID" : "misp",
  "type" : "write",
  "oldData" : "Delete All",
  "createSchema" : true,
  "mappings" : {
    "MISP_Event" : {
      "sourceTable" : "table",
      "extractColumn" : null,
      "extractKey" : null,
      "pk" : "uuid",
      "rowMapping" : null,
      "rowFilter" : null
    }
  },
  "expressions" : {
    "expression" : "$openJson(\"https://www.circl.lu/doc/misp/feed-osint/0b988513-9535-42f0-9ebc-5d6aec2e1c79.json\").Event.Attribute"
  }
}
```
### Email
Configures an SMTP server
```json
{
  "djClassName" : "org.dashjoin.function.Email",
  "ID" : "email",
  "type" : "write",
  "properties" : {
    "mail.smtp.port" : "25"
  },
  "username" : "user",
  "password" : "DJ1#\btW06MCaBJjnRvgvGgTaTpQ=="
}
```
## Configuration
### login configuration
Login configuration for ACME Corp. App with a specific login screen background image. Users can choose between the 'de' and 'en' locales. The default is the browser locale.
```json
{
  "signInTabText" : "ACME Corp. App",
  "defaultLocale" : "browser",
  "locales" : [ "en", "de" ],
  "backgroundImage" : "https://example.org/logo.jpg"
}
```
### theme
sets the UI theme. in this example, we set the primary and secondary color
```json
{
  "ID" : "theme",
  "map" : {
    "palette.primary.main" : "#3d7dbc",
    "palette.secondary.main" : "#3d7dbc"
  }
}
```
### dark-theme
sets the UI dark theme. in this example, we set the primary and secondary color
```json
{
  "ID" : "theme",
  "map" : {
    "palette.primary.main" : "#3d7dbc",
    "palette.secondary.main" : "#3d7dbc"
  }
}
```
### sidenav-width-px
sets the sidenav width to 100px. 0 hides the sidenav
```json
{
  "ID" : "authenticated",
  "properties" : {
    "sidenav-width-px" : "100"
  }
}
```
### sidenav-open
sidenav is closed by default
```json
{
  "ID" : "sidenav-open",
  "boolean" : false
}
```
### search-timeout-ms
Query timeout in milliseconds for queries issued when searching data. To disable the timeout, set to 0
```json
{
  "ID" : "search-timeout-ms",
  "integer" : 200
}
```
### prioritize-table-in-search
Tables in this list are searched first
```json
{
  "ID" : "prioritize-table-in-search",
  "list" : [ "EMPLOYEES" ]
}
```
### on-start
Expression to run when the system starts. Can be used to initialize the database, etc...
```json
{
  "ID" : "on-start",
  "string" : "$log('starting...')"
}
```
### on-login
Expression run whenever a user logs in. In this example, only allow the admin user to login
```json
{
  "ID" : "on-login",
  "string" : "email != 'admin@localhost' ? $error('Only user admin allowed')"
}
```
### logo-url
specifies the logo to show in the toolbar
```json
{
  "ID" : "logo-url",
  "string" : "/assets/logo.svg"
}
```
### include-table-in-search
Only search the EMPLOYEES table
```json
{
  "ID" : "include-table-in-search",
  "list" : [ "EMPLOYEES" ]
}
```
### homepage
Page to open after the user logs in
```json
{
  "ID" : "homepage",
  "string" : "/page/Info"
}
```
### exclude-table-from-search
Do not search the EMPLOYEES tables
```json
{
  "ID" : "exclude-table-from-search",
  "list" : [ "EMPLOYEES" ]
}
```
### exclude-database-from-search
do not search the sqlite database
```json
{
  "ID" : "exclude-database-from-search",
  "list" : [ "sqlite" ]
}
```
### i18n
Specify german translations for strings appearing in the app
```json
{
  "ID" : "de",
  "map" : {
    "Search Results" : "Suchergebnisse"
  }
}
```
### database-search-query
Configures searches on the northwind DB to use the query 'search' from the query catalog (select * from EMPLOYEES where LAST_NAME like CONCAT(${search}, '%'))
```json
{
  "ID" : "database-search-query",
  "map" : {
    "northwind" : "search"
  }
}
```
### allow-dark-mode
disallow dark mode
```json
{
  "ID" : "allow-dark-mode",
  "boolean" : false
}
```
### base-theme
change the base theme
```json
{
  "ID" : "base-theme",
  "string" : "B&W"
}
```
### autocomplete-timeout-ms
sets the timeout for autocomplete queries to 1 second
```json
{
  "ID" : "autocomplete-timeout-ms",
  "integer" : 1000
}
```
### all-timeout-ms
Query timeout in milliseconds for queries issued when browsing data. To disable the timeout, set to 0.
```json
{
  "ID" : "all-timeout-ms",
  "integer" : 1000
}
```
### tenantusers
user@example.org is allowed (active) on the platform and is in the role 'authenticated'
```json
{
  "ID" : "user@example.org",
  "active" : true,
  "roles" : [ "authenticated" ]
}
```
### tenantusers
Sets the 'homepage' variable (the initial page after login) to '/page/test' for user@example.org (overrides the global and role setting for 'homepage')
```json
{
  "ID" : "user@example.org",
  "properties" : {
    "homepage" : "/page/test"
  }
}
```
### dj-role
defines the role 'admin'. Sets the 'homepage' variable (the initial page after login) is set to '/page/Info' for all users in this role (overrides the global setting 'homepage')
```json
{
  "ID" : "admin",
  "properties" : {
    "homepage" : "/page/Info"
  }
}
```
