# API

This section is comprised of two parts. First, we look at the API that ships with the Dashjoin platform. Second, we highlight how you can build APIs as part of your app.

## Platform API

The Dashjoin architecture features an Angular Single Page Application (SPA) that is driven by RESTful APIs. The APIs support the OpenAPI standard.
The OpenAPI description is available at <https://demo.my.dashjoin.com/openapi>. Dashjoin also ships the Swagger GUI at <https://demo.my.dashjoin.com/swagger-ui>.
Please note that the API is subject to change.

### Authentication

The API requires any request using a local admin user to be authenticated with HTTP basic authentication:

```bash
curl -u admin:djdjdj http://localhost:8080/rest/manage/version
```

In order to login using an OpenID user, you need to specify a bearer token as follows:

```bash
curl -H "Authorization: Bearer ..." https://demo.my.dashjoin.com/rest/manage/version
```

The easiest way to obtain a bearer token is to login using a browser and copying the token via the browser development tools.
Depending on your OpenID provider, a bearer token can also be obtained via a seperate login call.

In addition to the API, it is possible to create custom function and database microservices and use them via the RestJson function and
RemoteDatabase clients. For more information, please refer to the [dashjoin-sdk](https://github.com/dashjoin/platform/tree/master/dashjoin-sdk) module documentation.

### JSON:API

Dashjoin supports [JSON:API](https://jsonapi.org/) for read operations. 
JSON:API describes how clients should request or edit data from a server, and how the server should respond to said requests.
The endpoint is available under /rest/jsonapi.

### OData

Dashjoin also supports [OData](https://www.odata.org/) for read operations. 
OData (Open Data Protocol) is an ISO/IEC approved, OASIS standard that defines a set of best practices for building and consuming RESTful APIs.
The endpoint is available under /rest/odata.

### PDF Export

Any platform page can be exported to PDF using the puppeteer framework. For your convenience, we deployed a cloud function to do this for any
installation available on the web. Please note that your credentials and the PDF content are sent via this third party if you use this function:

```bash
curl -X POST https://europe-west1-djfire-1946d.cloudfunctions.net/exportPdf --output cloudfunction.pdf -H "Content-Type:application/json" -d '{
    "url": "https://demo.my.dashjoin.com/#/page/html-Dashboard2",
    "username": "...",
    "password": "...",
    "pdfOptions": {
        "format": "a4",
        "landscape": true
    },
    "toggleNavBar": true
}'
```

Alternatively, you can use a bearer token in the "authentication" field instead of user name and password.

## App API

Apart from building user interfaces, the Dashjoin platform can also be used to develop, document, and deploy APIs based on the OpenAPI standard.
The platform provides generic APIs to read and write tables, run queries, and execute functions. In this context, generic means that a new function
"myfunction" does not show up in the API definition. Its name can rather can be used as parameter in the execute function API. Using the App API concept,
this pattern can be changed and "myfunction" can be exposed as an individual API. This makes it easier for developers to document APIs and expose them to
clients.

### Documenting an Existing App

In the first use case, we assume that there already is an existing Dashjoin App. It works using the UI and the developer would now like to
expose parts of this app via OpenAPI.

Consider the following example. The app contains a simple invoke function that computes "{"result": x+y}" and this function
should be exposed as an OpenAPI REST service.

First, you need to create an OpenAPI skeleton description and place it into the app's upload folder, e.g. at upload/openapi.yaml:

```yaml
openapi: "3.0.3"
info:
  version: "1"
  title: "test"
x-dashjoin:
  x-functions:
  - add
security:
- Basic_Auth: []
components:
  securitySchemes:
    Basic_Auth:
      type: "http"
      scheme: "basic"
```

This specification is written in YAML and contains some basic metadata about the API version and description. It also specifies that clients
can connect using HTTP basic authentication. Please note that you can author these specifications using this [online editor](https://editor.swagger.io/).
The SwaggerHub offering also allows you to save the specs in the cloud.
See the [dashjoin repository](https://app.swaggerhub.com/apis/dashjoin/add/1.0.0) and the
[raw YAML spec](https://api.swaggerhub.com/apis/dashjoin/add/1.0.0/swagger.yaml).

In the "x-dashjoin" section, we specify that the function "add" is to be added to the API. Normally, the API path, schema, response types, etc. would have to be added. In our case, the Dashjoin platform generates that into this template.

Once this file is saved, follow these steps:

* Connect the template with the platform, by adding "url": "file:upload/openapi.yaml" to the "openapi" system configuration setting (/#/resource/config/dj-config/openapi)
* Open the platform Swagger-UI (/swagger-ui/)
* In the text field at the top, change "/openapi" to "/rest/manage/openapi"
* Authorize using your credentials
* Test the service by sending this JSON payload: {"x":1, "y": 2}

Apart from functions, you can also publish queries and schemas. Queries work just like functions. Schemas can be derived from tables known to the system. To include these in the App API, you can use these keywords in the OpenAPI template:

```yaml
x-dashjoin:
  x-functions:
  - add
  x-queries:
  - group
  x-schemas:
  - dj/northwind/US_STATES
```

Unfortunately, it might not be clear to clients, that the request is expected to be an object containing the keys x and y.
In order to make this clear, we can either provide a more description JSON Schema for the request object or we can provide a descriptive example.
You can add the following fragment to the OpenAPI description:

```yaml
paths:
  /rest/function/add:
    post:
      operationId: "add"
      requestBody:
        content:
          application/json:
            schema:
              example:
                x: 1
                y: 2
```

This YAML tree structure will be merged with the one generated by the Dashjoin platform. Specifically, the example key will be merged providing clear documentation on how to call the service.

The OpenAPI specification can be accessed without credentials. If credentials are provided (e.g. via curl http://admin:djdjdj@localhost:8080/rest/manage/openapi), the platform also generates result set metadata for the queries.
For instance, if the group query in the [Dashjoin Demo App](https://github.com/dashjoin/dashjoin-demo/blob/main/model/dj-query-catalog/group.json) is included,
the following OpenAPI path response is generated:

```yaml
  responses:
    "200":
      content:
        application/json:
          schema:
            type: "array"
            items:
              type: "object"
              properties:
                COUNTRY:
                  type: "string"
                  x-dbType: "CHARACTER VARYING"
                Number of Customers:
                  x-dbType: "BIGINT"
      description: "group response"
```

### Implementing an existing OpenAPI Specification

The second use case highlights a scenario where we work in an API centric fashion. This means that the OpenAPI spec is designed first instead
of being generated from existing code.

As an example, we will implement the Petstore example that is featured in the [Swagger Editor](https://editor.swagger.io/). Simply select "Save as YAML"
and place the file in "upload/petstore.yaml". Then, change the openapi config (/#/resource/config/dj-config/openapi) to "url": "file:upload/petstore.yaml".

#### Creating Tables

The Petstore API defines several data structures such as Pet, User, etc. Not all of them necessarily need to be mapped to tables. The Address structure for example, merely defines a complex column in the table customer. Since OpenAPI schemas usually contain nested types and arrays, we should choose a
database that supports these datatypes. For this example, we'll use a Postgres DB that is registered under the name "postgres".

The platform offers an API that allows creating a table directly from the OpenAPI spec. Note that the first property is assumed to be the primary key.
We'll create the Pet table using this curl command:

```text
curl -X 'POST' \
  'http://localhost:8080/rest/manage/createTable/postgres' \
  -H 'accept: */*' \
  -H 'Authorization: Basic YWRtaW46ZGpkamRq' \
  -H 'Content-Type: text/plain' \
  -d 'Pet'
```

Once you update the database to trigger the table scan, the Pet table shows up.
Note that nested types and arrays get converted to [jsonb columns](https://www.postgresql.org/docs/current/datatype-json.html).

#### Creating Function Stubs

Next, we'll create function stubs from the OpenAPI spec. This functionality is also available on the platform API:

```text
curl http://admin:djdjdj@localhost:8080/rest/manage/createStubs
```

This creates an invoke function that simply echos the input for each operation in the spec. The name of the function is the 
value of the OpenAPI operationId.

#### Testing a Stub

Before we can test a generated function stub, we need to change the value of the server field. This field provides options for the 
APIs to be tested from the Swagger UI. We set this value to:

```yaml
servers:
  - url: /rest/app
```

The endpoint /rest/app is a generic API handler that delegates incoming calls to the appropriate function.

The Petstore example comes with open authentication and API key authentication. For simplicity, we'll add basic authentication:

```yaml
  securitySchemes:
    Basic_Auth:
      type: "http"
      scheme: "basic"
```

We'll implement the addPet and getPetById calls. So you'll have to allow basic authentication with these calls by adding:

```yaml
  /pet/{petId}:
      ...
      security:
        - Basic_Auth: []
        - api_key: []
        - petstore_auth:
            - write:pets
            - read:pets
```

```yaml
  /pet:
    put:
      ...
    post:
      ...
      security:
        - Basic_Auth: []
        - petstore_auth:
            - write:pets
            - read:pets
```

Now you can reload the Swagger UI with the OpenAPI spec at /rest/manage/openapi, login using basic authentication, and test the find pet by ID call
with petId 123. The resulting JSON is:

```json
{
  "parameters": {
    "petId": "123"
  },
  "body": null
}
```

Likewise, the result of the addPet call - using the sample parameters provided - is:

```json
{
  "parameters": null,
  "body": {
    "id": 10,
    "name": "doggie",
    "category": {
      "id": 1,
      "name": "Dogs"
    },
    "photoUrls": [
      "string"
    ],
    "tags": [
      {
        "id": 0,
        "name": "string"
      }
    ],
    "status": "available"
  }
}
```

You can see that the generic API handler always passes an object to the function. This object contains the keys parameters and body.
Parameters contains all path, query, cookie, and header parameters defined. The body optionally contains the JSON payload.

#### Implementing the Functions

Using the JSONata functions, we can now implement the functions. AddPet can be handled with this expression:

```text
(
  $echo($);
  $create("postgres", "Pet", body);
  body;
)
```

First we echo the object passed to the function so we can trace the call on the log. Since the structure
of the body matches the JSON schema that was used to generate the table, we can pass it one to one.
Finally, we return the body since that is expected by the OpenAPI specification.

The find pet by ID call is also very simple:

```text
$read("postgres", "Pet", parameters.petId)
```

### Contributing to an existing OpenAPI Specification

The third use case extends both the first and second use case. We assume you're working in an API centric fashion, however, 
parts of the spec should be derived from existing systems. Typically, there's a strong correlation between schemas and
existing databases, so we'll extend the petstore example with a schema generated from the northwind sample database.

First, we'll need to indicate that a certain table should be added to the OpenAPI spec:

```yaml
x-dashjoin:
  x-schemas:
  - dj/northwind/US_STATES
```

This makes the schema for US_STATES appear in the spec. Note that the schema is marked with the flag x-generated=true.
Currently, the extended spec is only available via the platform. Therefore, we can export it using another API call:

```text
http://admin:djdjdj@localhost:8080/rest/manage/saveapi
```

Since we configured the OpenAPI spec to be read from a file URL, the system updates the file to include the new schema, making
it available for other tooling.
In case the database schema is updated, e.g. another column is added, this process can be repeated. The x-generated flag
makes sure that updates on the database are reflected, despite the schema already being present in the file.

### SwaggerHub Integration

SwaggerHub is a popular tool for authoring and managing your APIs. Consequently, instead of using a file URL, we allow
using a SwaggerHub URL. In order to have the platform authenticate against the SwaggerHub API, you must generated an API
key and provide it in the openapi system configuration:

```json
{
    "ID": "openapi",
    "map": {
        "url": "https://api.swaggerhub.com/apis/[ORG-NAME]/[API-NAME]/[VERSION]/swagger.yaml",
        "apiKey": "..."
    }
}
```

In this case the save operation adds generated fragments to this specific API version.
