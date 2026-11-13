# Functions

Apart from changing data in databases, Dashjoin can call functions on the backend. Functions come in two flavors: First, there are functions that simply extend
the functionality you can use in expressions. An example would be a simple toUpperCase function that transforms a string to upper case.
These functions are described in the [JSONata Expression Reference](jsonata-reference.md). Second, there are configurable functions. These work very much like their counterpart,
however, they require additional configuration parameters. An example would be a function to send an email. The actual function call requires you to specify subject, sender, receiver, and the body. But you would not want to have to repeat the email server address and credentials every time. So you can
register an instance of email service with specific parameters and call it email-service-1. This page describes the latter configurable functions.

[Click here](https://www.youtube.com/watch?v=XuymQh6aMIk) for a demo video.

## Function Reference

The system supports the following functions. Each section lists the function configuration parameters that are constant any time this function is called as well as the parameters that are specific for each invocation.

### AIApp

Allows configuring an AI application that can be called from the AIChat widget or any other JSONata expression:

Configuration

* systemPrompt: the AI system prompt to instruct AI behaviour
* ai: URL of the KnowledgeBase database. Only required if there are multiple KnowledgeBases.
* model: name of the AI model to use - those can be configured in settings.yml (see AI & ML integration for details)
* tools: names of the tools the AI is allowed to use. Tools can be Invoke functions with comment and parameter annotations, builtin MCP tools configured in the MCP proxy, or tools of an external MCPServer
* tags: all documents with these tags are used as the RAG source
* documents: individual documents to be used as the RAG source

Invocation parameter

* array: array of messages containing role and content. Note that if the last content contains #full, the entire RAG corpus will be added to the request. This is called Cache-Augmented Generation (CAG) and is suitable for smaller document sets that fit in the LLM context.
If the content contains #query, the text behind the query marker is used as a RAG query. Consider the following example: can you explain the difference between RAG and CAG #query RAG CAG. In this case the documents would be search using the term "RAG CAG" rather than the entire question. This can be used to optimize RAG retrieval accuracy.

If you would like to invoke the LLM using JSON mode, you can pass the target JSON Schema as follows:


```
$parseJson(
  $call('aia', {
    'response_format': { 
      'properties': {
        'res': {
          'type': 'array'
        }
      }
    },
    'messages': [
      {
        'role':'user', 
        'content': 'list of European capitals with a population of at least 3 million'
      }
    ]
  }).choices.message.content
)
```

This returns the following JSON structure:

```
{
  "res": [
    "Berlin",
    "Madrid",
    "Rome",
  ...
```

Note that the top level schema must contain "properties" and the returned value is always an object. 
The AIApp function makes sure the schema is compatible with the LLM requirements. 
For instance, if "type" is omitted, it is assumed to be "string".

### RestJson

Calls an external REST service. If you need more control over the how the call is performed, please use the JSONata function curl.

Configuration

* url: the URL of the REST service to call (the URL may contain template variables `${var}` which are replaced with the respective argument field)
* username: optional HTTP basic authentication user name
* password: optional HTTP basic authentication password
* method: GET or POST
* headers: HTTP headers
* contentType: content-type header application/json or url-form-encoded
* apiKey: if true, use username / password as another header - this feature can be used to pass API keys etc. while not having to show the key in plain text in the header input form. If this value is omitted or false, username and password are converted to a basic authentication header
* timeoutSeconds: Optional HTTP timeout in seconds (default is 10s)
* insertCredentials: array of field names. If set, any key:value found in the object passed is replaced with key:value of the credential with name "value" (Dashjoin Studio only)

Invocation parameter

* object: If object is specified, POSTs the object serialized as JSON. If object is null, GETs the result

Return value

* JSON result returned by the service

### Email

Sends an email.

Configuration

* username: username to log into the email service
* password: password to log into the email service
* properties: [SMTP server configuration](https://www.tutorialspoint.com/javamail_api/javamail_api_smtp_servers.htm)

Invocation parameter

* from: email sender in RFC822 syntax
* to: email recipient in RFC822 syntax
* subject: email subject line
* text: email text

Return value

* none

### Invoke

Allows saving an expression on the server. When run, we evaluate / apply the expression with the data context passed as an argument.

Configuration

* expression: The expression to save and run when invoked
* parameters: optional JSON Schema describing the objects expected by the function
* runas: optional role the expression runs as (see [Running Invoke Functions as a Role](concepts.md#running-invoke-functions-as-a-role))

Invocation parameter

* object: the expression evaluation context (see [JSONata in Invoke Functions](concepts.md#jsonata-in-invoke-functions))

Return value

* expression result

### Credentials

Allows saving credentials that can be referenced by the JSONata curl function. This is done by
specifying the authorization header and setting it to the name of the credential:

```
$curl("GET", "http://localhost:8080/rest/manage/version", {}, {"Authorization": "credential name"})
```

This works for both basic authentication and passing an API key via some generic HTTP header.

Configuration:

* username: credential username
* password: credential password
* apiKey: if true, use username / password as another header - this feature can be used to pass API keys etc. while not having to show the key in plain text in the header input form. If this value is omitted or false, username and password are converted to a basic authentication header

Examples:

Basic authentication: create a Credentials function with ID "dashjoin", username "admin" and password "djdjdj".
The call below sends the header "Authorization: Basic YWRtaW46ZGpkamRq":

```
$curl("GET", "http://localhost:8080/rest/manage/version", null, {"Authorization": "dashjoin"})
```

Bearer token / API key: create a Credentials function with ID "openai", username "Authorization",
password "Bearer sk-..." and apiKey set to true. The header named by username is set to the password:

```
$curl("GET", "https://api.openai.com/v1/models", null, {"Authorization": "openai"})
```

If the service expects the key in a different header (e.g. "X-API-Key: ..."), set username to "X-API-Key".
The Authorization header is still used to reference the credential, but it is removed before the request is sent,
so the call above sends only "X-API-Key: ...".

Secrets in the request body: some services expect the secret in the payload (e.g. a login form).
Use the header "dj-insert-credentials" to list the fields of the data object (a single field name or an array)
whose value is the ID of a Credentials function. The value is replaced with the password (apiKey = true) or the basic authentication value (apiKey omitted or false):

```
$curl("POST", "https://example.com/api/login", {"user": "admin", "password": "exampleLogin"}, {"dj-insert-credentials": ["password"]})
```

Users must have one of the roles of the Credentials function in order to use it (admin only if it has no roles).

### MCPServer

Allows configuring an external MCP server. Tools offered by this server can be attached to AIApp.

Configuration

* url: MCP server URL
* username: credential username
* password: credential password
* apiKey: if true, use username / password as another header - this feature can be used to pass API keys etc. while not having to show the key in plain text in the header input form. If this value is omitted or false, username and password are converted to a basic authentication header

## Mapping Functions

Mapping functions are specialized functions that have no invocation parameters and outputs.
They are used to write data into a database and can be run in a scheduled fashion.
All mapping functions perform the following three steps.

[Click here](https://www.youtube.com/watch?v=ZsjyucALidY) for a demo video.

### Gathering Data

It is up to the mapping function how this task is achieved. The only requirement is that the function
gathers a set of tables. 

### The Mapping Step

This step is common to all mapping functions and is supported by a specialized mapping editor. The mapping step
transforms the gathered set of tables into another set of tables. The mapping step supports the following operations:

* remove table: a table from the initial step can be removed / ignored
* remove column: drops a column from a table
* rename table: a table from the initial step can be renamed
* rename column: renames a column in a table
* add table: a table can be added by providing the name of an initial table
* add column: a column can be added to a table
* modify column: sets the column to a new expression (the default simply copies the original value 1:1 using `$.columnname`; please see the [JSONata Expression Reference](jsonata-reference.md) for more details on expressions)
* extract table: if an input table contains a column with array values, extracts the union of these arrays into a new table

### The Save Step

The save step writes the output of the mapping step into the database. The following modes are supported:

**Ignore**

Simply add the data (update in case the record already is in the DB, insert if not). We follow the "normal" update semantics meaning that key=null actually deletes the value in the DB, whereas missing keys remain untouched.

Database

| id | _dj_source | name |
|----|------------|------|
| 1  |            | Joe  |
| 2  |            | Mike |

Extracted Data from ETL "ignore"

| id | name |
|----|------|
| 1  | John |
| 3  | Nate |

Result

| id | _dj_source | name |
|----|------------|------|
| 1  |            | John |
| 2  |            | Mike |
| 3  | ignore     | Nate |

Row 1 is updated. Row 2 remains unchanged. Row 3 is added and thus gets marked as having source "ignore".

Extracted Data from ETL "ignore"

| id | name |
|----|------|
| 1  | John |
| 4  | Jane |

Result

| id | _dj_source | name |
|----|------------|------|
| 1  |            | John |
| 2  |            | Mike |
| 3  | ignore     | Nate |
| 4  | ignore     | Jane |

Row 4 gets added. Row 3 remains even though it is no longer in the extraction result.

**Refresh**

All records from the target tables that have the _dj_source column matching the ID of this function are updated. If a key is no longer present in the new data, the record is deleted.

Database

| id | _dj_source | name |
|----|------------|------|
| 1  |            | Joe  |
| 2  |            | Mike |

Extracted Data from ETL "refresh"

| id | name |
|----|------|
| 1  | John |
| 3  | Nate |

Result

| id | _dj_source | name |
|----|------------|------|
| 1  |            | John |
| 2  |            | Mike |
| 3  | refresh    | Nate |

The first run of "refresh" has the same effect as "ignore".

Extracted Data from ETL "refresh"

| id | name |
|----|------|
| 1  | John |
| 4  | Jane |

Result

| id | _dj_source | name |
|----|------------|------|
| 1  |            | John |
| 2  |            | Mike |
| 4  | refresh    | Jane |

The third row, which was added by "refresh" previously, is deleted and row 4 is added.

**Delete All**

All records from the target tables are deleted, if createSchema is true, the tables are also dropped in case columns are no longer needed or previously had another datatype.

Database

| id | _dj_source | name | age |
|----|------------|------|-----|
| 1  |            | Joe  | 33  |
| 2  |            | Mike | 44  |

Extracted Data from ETL "delete-all"

| id | name |
|----|------|
| 1  | John |
| 3  | Nate |

Result

| id | _dj_source | name |
|----|------------|------|
| 1  | delete-all | John |
| 3  | delete-all | Nate |

The table content is deleted. If create schema is specified, the age column is also deleted.
Rows 1 and 3 get added with the respective source.

Extracted Data from ETL "delete-all"

| id | name |
|----|------|
| 1  | John |
| 4  | Jane |

Result

| id | _dj_source | name |
|----|------------|------|
| 1  | delete-all | John |
| 4  | delete-all | Jane |

The table content is deleted and rows 1 and 4 are added.

**Sync**

The sync mode works like Ignore. In conjunction with the foreach expression, it can be used to keep
the database in sync with a file system or a web download location, by deleting certain entries.
This is explained in the next section.

## Mapping Function Reference

### ETL

The ETL function uses an expression as input into the mapping process.
The expression result can be a map of table names to an array of rows (JSON objects).
If the expression result has a simpler structure (for instance only a single table),
the ETL function wraps this in a default table called "table".

If you want to load a large amount of data, you can use the "foreach" expression to specify
how to split the loading process into smaller parts. Assume you have a directory with
thousands of files to load. The foreach expression can list the files using `$ls("url")`.
The expression then specifies how each file is handled. Its `$` context is set to
each individual URL and the expression and subsequent ETL are called for each URL individually.

Note that you can also stream large JSON, XML, or CSV files via the streamJson, streamXml, and streamCsv
functions. In this case, these functions split a large file into smaller chunks which are then
passed to the mapping expression.

The setting "ETL worker threads" can be used to achieve parallel writes to the database.
This setting is only applicable if a foreach expression is specified.
In this case, the setting "ignore ETL errors and continue process" specifies that any error
that occurs when streaming a large file (e.g. a formatting error towards the end of the file)
or when workers map and write the contents to the database (e.g. due a malformatted date string)
are ignored and do not stop the other workers.

### ETL Sync

The foreach construct also allows you to conveniently keep the database in sync with a set of files
on the file system or the web. Consider the following foreach expression:

```
$ls("file:upload")
```

It returns a list of objects describing files in the upload folder. Let's assume those are JSON
files, that are mapped to the database using this expression:

```
{
  "url": url,
  "modified": modified,
  "content": $openJson(url)
}
```

Now we can compute the urls and modified timestamps that are in the database:

```
$all(db, table).{'url': url, 'modified': modified} // please use a native distinct query for large datasets
```

The platform offers the etlSync function that computes the set of URLs that must be loaded
for the next run and the records that might have been deleted using the URLs and modified
pairs:

```
$etlSync($ls("file:upload"), $all(db, table).{'url': url, 'modified': modified}, "url")
```

This example compares the files in the upload folder to the records in the database table, matching them using the 'url' field.
If the modified / etag / version field of the filesystem is greater than the respective field in the database, the file
will be included in the result. Consider the example below that shows a sync ETL function:

```
{
    "ID": "sync",
    "djClassName": "com.dashjoin.function.ETL",
    "expressions": {
        "foreach": "$etlSync($ls('file:upload'), $all('db', 'table'), 'url')",
        "expression": "{'url': url, 'modified': modified, 'content': $openText(url)}"
    },
    "oldData": "Sync",
}
```

We use the etlSync function in the foreach expression to compute the list of files that needs to be loaded.
The expression maps the file to the database record as usual.
The etlSync function can also determine which files are in the database but no longer on the filesystem.
The mode "Sync" indicates, that deleted and modified files will be removed from the DB first.

### Receive

The receive function allows handling cases, where the platform is being sent data that is to be processed and saved into a database.
This use case is common in IoT scenarios where a stream of sensor data is passed via the REST API.
The Receive function can be configured like the ETL function and allows mapping the data into the desired structure.
The create schema parameter works like in the ETL case and optionally adapts the underlying schema to accommodate new fields and tables.
Receive defines a parameter called sample where a stream data sample can be added.
This sample data is used to edit the mapping.
Note that Receive always appends the new data like the Ignore mode in the ETL case. The difference is that there
is no expression that fetches data. Instead, the data is passed via the API call.

## Scheduling

Functions can be scheduled to run periodically. Using Dashjoin Studio, you can either set an interval or a cron schedule.
Use cron if your jobs should run based on a calendar, every weeknight at 2am, for instance. Use intervals for
simple schedules like: run every hour.

When a function is run by the scheduler, it uses the role "scheduler". Therefore, make sure that the function roles include this role.
Consider the following example that runs an invoke function every hour (note that the function is run without input parameter):

```
{
    "ID": "interval",
    "djClassName": "org.dashjoin.function.Invoke",
    "expression": "...",
    "every": "1h",
    "roles": [
        "scheduler"
    ]
}
```

Please refer to this [documentation](https://quarkus.io/guides/scheduler-reference#intervals) for information about the interval string format.
The next example uses the [cron](https://quarkus.io/guides/scheduler-reference#cron) syntax to run the function every day at 10:15:

```
{
    "ID": "cron",
    "djClassName": "org.dashjoin.function.Invoke",
    "expression": "...",
    "cron": "0 15 10 * * ?",
    "roles": [
        "scheduler"
    ]
}
```
