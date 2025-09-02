# Appendix: Databases, Queries, Functions, and Configurations
These are possible children of the create, edit, button, and variable widgets.## query catalog
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
## query catalog
a query on database 'dj/northwind' called 'list'. Parameters limit and offset can be passed to the query
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
## database definition
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
## Function catalog: Invoke
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
## Function catalog: RestJson
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
## Function catalog: Credentials
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
## Function catalog: ETL
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
## Function catalog: Email
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
