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

TODO
