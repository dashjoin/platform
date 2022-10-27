# API

The Dashjoin architecture features an Angular Single Page Application (SPA) that is driven by RESTful APIs. The APIs support the OpenAPI standard.
The OpenAPI description is available at <https://demo.my.dashjoin.com/openapi>. Dashjoin also ships the Swagger GUI at <https://demo.my.dashjoin.com/swagger-ui>.
Please note that the API is subject to change.

## Authentication

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

## JSON:API

Dashjoin supports [JSON:API](https://jsonapi.org/) for read operations. 
JSON:API describes how clients should request or edit data from a server, and how the server should respond to said requests.
The endpoint is available under /rest/jsonapi.

## OData

Dashjoin also supports [OData](https://www.odata.org/) for read operations. 
OData (Open Data Protocol) is an ISO/IEC approved, OASIS standard that defines a set of best practices for building and consuming RESTful APIs.
The endpoint is available under /rest/odata.

## PDF Export

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

