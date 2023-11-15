# Concepts

Before we dive into the user guide for the platform, this section explains a couple of key concepts.

## Data Model

The Dashjoin low code platform sits on top of one or more databases. These databases can be empty, ready to store application data,
or they can contain an existing schema and data, possibly under the control of other software and systems.
Dashjoin connects to these databases and maps the data using coordinates for each data record:

### Record Coordinates

1. Dashjoin: The first coordinate is the ID of the Dashjoin installation that accesses the database.
2. Database: The unique name of the database containing the record.
3. Table: The table name (unique within the database) of the table containing the record.
4. Record key(s): The unique ID of the record within its table. This might be a list of keys if we are dealing with composite keys, for instance in a relational database.

These coordinates (DJ, DB, TABLE, PK) translate to RESTful URLs:

* Visualizing a record: `https://dashjoin.host.name/#/resource/DB/TABLE/PK`
* API access to the record: `https://dashjoin.host.name/rest/database/crud/DB/TABLE/PK`

### Tables and Columns

Dashjoin organizes data in tables and columns. Columns can be of simple types such as strings, integers, etc., but they can also be complex
JSON documents. Therefore, Dashjoin is able to connect to multiple kinds of databases. Each of these drivers aligns the database specific nomenclature
to the common data model. Therefore a document database's collections become tables, the documents become records and the document fields become columns.

### Primary and Foreign Keys

Each table usually defines one or more primary key columns that uniquely define each record in the table.
In addition, there can be foreign keys in a table that reference another table. This information is crucial for Dashjoin, since it is used to
automatically display hyperlinks between records.

Usually the key information is extracted from the databases by the drivers. However, some databases do not allow
for expressing foreign key information. In this case, the information can be added to the Dashjoin metadata by the user.
This mechanism even allows setting foreign key relationships between databases and even from one Dashjoin system to another.
This does not enable you to run a federated query like you would within a single SQL database. However, Dashjoin
uses this metadata to hyperlink records between databases and logical Dashjoin installations.

## Table and Instance Layout and Navigation

The Dashjoin user interface concept is inspired by the Semantic MediaWiki. The database is thought of as a large linked data cloud. Dashjoin user interface pages can be one of the following types:

1. Record page: Assume the user navigates to the page /resource/DB/TABLE/PK. The system displays a page that corresponds to this record.
2. Table page: Assume the user navigates to the page /table/DB/TABLE. The system displays a page that corresponds to this concept / table.
3. Dashboard page: Assume the user navigates to the page /page/Page. The system displays this page which has no direct related context in the database.

Unless the low code developer specifies otherwise, table and record pages are displayed as follows:

### Default Table Layout

Table pages show

* a pageable and sortable data table
* a form to create a new record
* if you are in the admin role, controls to edit the table schema and metadata

Within the data table, any primary or foreign key field links to the corresponding record page,

### Default Record Layout

Record pages show

* a form to edit the record
* a delete button
* a link back to the table page
* links to any related record (this can either a foreign key field of the record or records in other tables containing foreign key references to this record's primary key)

The default layout allows the user to easily navigate the data regardless of which specific database it is located in.

### Widgets and Custom Layouts

For each table, the default table and instance layout can be adapted.
A layout is a hierarchy of widgets. Widgets that contain other widgets are called containers.
Every widget has a couple of properties. The chart widget for example, defines which query to visualize.

## Schema Metadata

We already mentioned that a developer can define foreign key relationships, even if the underlying database does not support this concept.
Dashjoin allows a number of information to be entered about databases, tables, and columns. This data is usually called metadata.
Dashjoin stores this metadata in the built-in configuration database, but this database behaves just like any other database.
Therefore, each database and table are records and their pages behave just like any other page in the system.

## Interacting with the System

So far, we mostly looked at the way Dashjoin organizes and especially how it visualizes information.
This section describes how an application interacts and changes the underlying systems in other ways.

### Create, Read, Update, Delete (CRUD)

A database driver usually exposes CRUD operations to the platform. These operations are used to display data but also
to make changes from the forms displayed in the default layout. Note that, unless configured otherwise, the form shows
an edit element for all columns.

### Running Queries

Besides simple read and browse operations, the underlying databases usually have the ability to run powerful
queries. Dashjoin allows the developer to design such queries and save them in the query catalog consumption.
These queries usually drive table and chart displays on customized layout pages and dashboards.
Note that queries can also be used to run delete or update operations nd that they can also be parameterized.

### Executing Functions

Apart from changing data in databases, Dashjoin can call functions on the backend. You can think of a function
as a small pre-built and configurable service. Examples for function types are sending email or making a REST call.
These can be instantiated in a system as "sendGmail" and "getStockPrice" by using the function type and providing the required configuration.
These functions can then be used by active page elements such as buttons.

### Evaluating Expressions

Expressions are small programs that can be used to configure widgets on a page.
The display widget for instance can display texts on the UI. The text to display is computed
by an expression. This expression can for instance call the stock market function on the backend, and do some additional
JSON transformation on the results before displaying the data.

You can think of the expressions being the glue between widgets on the top and queries, CRUD and functions on the backend:

<table>
<tbody>
  <tr><td colspan="3">Widget</td></tr>
  <tr><td colspan="3">Expression</td></tr>
  <tr><td>CRUD</td><td>Function</td><td>Query</td></tr>
</tbody>
</table>

## Expression Context

Every time an expression is evaluated (e.g. by pressing a button or in order to render a display widget),
a context is passed to this expression for processing. This section describes what this context looks like
under different circumstances.

### Record Page Context

If expressions are run on a record page, the context is structured as follows:

```text
{
  database: the database the record is in
  table: the table the record is in
  pk1, ..., pk4: the (composite) key(s) of the record
  loc: location information about the current page
  user: the ID of the user that's currently logged in
  roles: the roles the user is in
  email: the user's email address (not set for local users)
  href: the current URL
  value: the current record
  variable: the session variables (see below)
  form: data entered via a form (see below)
}
```

### Table Page Context

On table pages, the context looks like this:

```text
{
  database: the database the record is in
  table: the table the record is in
  loc: location information about the current page
  user: the ID of the user that's currently logged in
  roles: the roles the user is in
  email: the user's email address (not set for local users)
  href: the current URL
  value: schema information of the current table in JSON Schema format
  variable: the session variables (see below)
  form: data entered via a form (see below)
}
```

### Dashboard Page Context

Dashboard pages provide the following context:

```text
{
  loc: location information about the current page
  user: the ID of the user that's currently logged in
  roles: the roles the user is in
  email: the user's email address (not set for local users)
  href: the current URL
  variable: the session variables (see below)
  form: data entered via a form (see below)
}
```

### Session Variable Context

The user interface can store variables per browser tab. Note that these variables are lost once
the tab is closed or the user logs out of Dashjoin. A variable can be set from any expression
using the "setVariable" JSONata function. Therefore, calling setVariable("x", 1) will cause
the expression context to be:

```json
{
  ...
  "variable": {
    "x": 1
  }
}
```

### Form Context

Several widgets allow adding form elements (see section "Form Widgets" in the next chapter).
If a number form element with name "y" has the value 2 and the form is submitted, the context is:

```json
{
  ...
  "form": {
    "y": 2
  }
}
```

### Database Trigger Context

Database triggers are a special case for how JSONata expressions are used and have a different
context. Please refer to section JSONata in Triggers for more details.
