# FAQ

* **How can I edit a fullscreen page?** Usually, you toggle the edit mode via the widget in the toolbar. If you are viewing a page in full mode (under the URL /#/full/Pagename), this element in the toolbar is not available. Simply view the page in normal mode (under /#/page/Pagename), and enter edit mode there.

* **The [Dashjoin Demo Application](https://github.com/dashjoin/dashjoin-demo) contains some interesting examples. How can I apply them to my application?** You can either locate your application on the file system and copy an example page there or you can look at the page in order to see which settings to add in the layout editor dialogs (e.g. a JSONata expression).

* **I have an object with special characters in the field names (e.g. a SQL query result). How can I access this field in JSONata?** In [JSONata](https://docs.jsonata.org/simple#navigating-json-objects), field names can be escaped using back-ticks (`). Click [here](https://demo.my.dashjoin.com/#/page/html) for a live example.

* **How can I dynamically customize the forms in the edit, button and variable widgets?** In Dashjoin Studio, you have the option to compute the schema you would normally define statically in the layout via the schemaExpression widget field.

* **Can I show a form field conditionally?** Yes, using the schemaExpression mechanism described above, you can create a schema that contains the switch and case keywords as follows:

```
{
    "widget": "button",
    "print": "form",
    "schemaExpression": "{'switch':'type', 'properties': {'type': {'widget': 'select', 'options':'[\"circle\"]'}, 'radius': {'case': 'circle'}}}"
}
```

For readability, here is the pretty-printed versino of the schemaExpression:

```
{
  "switch": "type",
  "properties": {
    "type": {
      "widget": "select",
      "options": "[\"circle\"]"
    },
    "radius": {
      "case": "circle"
    }
  }
}
```

* **How can I use values from the database in the edit, button and variable widgets?** This can be achieved by specifying an expression to compute the options array. The expression can evaluate to a simple array, or an array of objects containing the value (option value in the form) and name (UI option name) keys. 

```
{
    "widget": "button",
    "print": "form.field",
    "schema": {
        "type": "object",
        "properties": {
            "field": {
                "widget": "select",
                "options": "$all('northwind', 'EMPLOYEES').{'value':EMPLOYEE_ID, 'name': LAST_NAME}"
            }
        }
    }
}
```

* **Are SQL stored procedures supported?** Yes, simply use 'exec proc' or 'call proc(par)' as the query, depending on the SQL dialect used by your DB. In case a stored procedure has multiple result tables, the $query function returns them by wrapping them in a top level object.

* **How can I access a SQL Server stored procedure output variable?** This can be done on the query level as follows:

```sql
DECLARE @res INT;
exec dbo.sp @res output;
select @res;
```

* **Why am I getting the error: "User does not have the role required to read table page in database config" after logging in?** When the UI renders a page, it needs to get the page layout from the backend. Like with any other call, the user's credentials are checked. This error indicates, that the user is known to the system, but gets assigned insufficient roles to access this information in the config DB. To fix this, you can either assign the user the correct role in the IDM or you can provide read access to the config DB to the user's role (this is done on the System Configuration page).

* **Does the platform cache results?** Yes, all HTTP GET requests are cached by the browser UI. The cache is purged if 1) five minutes have passed since the last time the data was retrieved, 2) the data is changed in via the UI (e.g. by saving / updating a value), or 3) F5 / reload is pressed.

* **How can I download data from the platform?** The download happens via a JavaScript function that calls saveAs(new Blob([data]), filename). This snippet can be added as a client side expression with the // JavaScript marker:

```
// JavaScript
var blob = new Blob(["Hello, world!"], {type: "text/plain;charset=utf-8"});
saveAs(blob, "hello world.txt");
```

Alternatively, the script can also be added to the HTML widget. And example can be found [here](https://github.com/dashjoin/dashjoin-demo/blob/main/model/page/html.json).  

* **How can I download binary data such as PDFs or images?** This works like the regular download. You usually have a JSONata expression that loads the data in the backend. You can use $openText(url, "BASE_64") to get a base64 encoded representation. In the HTML widget, you can use this code to have the browser download the data:

```javascript
function go() {
  const byteCharacters = atob(context);
  const byteNumbers = new Array(byteCharacters.length);
  for (let i = 0; i < byteCharacters.length; i++) {
    byteNumbers[i] = byteCharacters.charCodeAt(i);
  }
  const byteArray = new Uint8Array(byteNumbers);
  const blob = new Blob([byteArray], {type: "application/pdf"});
  saveAs(blob, 'download.pdf')
}
```

* **On the table page, my primary key column is not on the very left and I need to scroll right to get to the instance page link. How can I change this?** The default layout uses the native column order defined in the database. This order is used for the overview table as well as for the forms on the instance pages. For the table, simply define a query with your desired column projection order. Note that you can also omit columns if you'd like. Enter the layout editor and use this query for the table widget. On the edit form, you can enter the layout editor and change the positioning there.

* **How can I format dates or currency in tables?** This can be done on the database query level. If you're using PostgreSQL for instance, this query will format the "born" and "salary" columns accordingly (assuming their database type is date and int): select to_char(born, 'DD-MON-YYYY'), cast(salary as money) from employee.

* **Why does the browser not show changes performed via an expression called from a button?** You need to include the clearCache function if your expression makes changes to the database. Otherwise, old values might be shown for five minutes.

* **The display widget shows an object as a [material list](https://material.angular.io/components/list/examples). Can I transpose the object such that it is displayed as a two column table with colums key and value?** This can be done using the following JSONata transformation. For each object key, we create an object where key is the current key and value is the key lookup. This array of objects is then shown as a table.

```javascript
value.(
    $x := $;
    $keys($).{"key": $, "value": $lookup($x, $)}
)
```

* **How can I determine whether it makes sense to define a foreign key on a given column?** In some data integration scenarios, it may not be clear
whether a column is a good candidate to reference a primary key. Some keys might match, others won't. You can use the following piece of
JSONata code to determine to which degree the values intersect. We first get the table data and project the column. The intersection
is computed using a JSONata filter which only includes the values in the other array.

```javascript
(
  $t1 := $all("db1", "table1").column1;
  $t2 := $all("db2", "table2").column2;
  {
    "count1": $count($t1),
    "count2": $count($t2),
    "intersect": $count($t2[$ in $t1])
  }
)
```

* **Can I trigger a git pull of the App in production without a restart?** You can run $gitPull() as a function or on the Dashjoin Notebook.

* **Can I call the OpenAI APIs from Dashjoin?** Yes, register the following function (replace your API key accordingly) can call it:

```javascript
{
    "djClassName": "org.dashjoin.function.RestJson",
    "ID": "openai",
    "type": "read",
    "method": "POST",
    "contentType": "application/json",
    "headers": {
        "Authorization": "Bearer YOUR-API-KEY-HERE"
    },
    "url": "https://api.openai.com/v1/chat/completions"
}
```

```javascript
$call("openai", {
  "model": "gpt-3.5-turbo",
  "messages": [{"role": "user", "content": "Say this is a test!"}],
  "temperature": 0.7
})
```

* **I have a table with a unique column which is not the primary key, can I ETL into this table from a datasource which only contains this key and not the primary key?** Yes, you can lookup a record using the $all function. Let's consider the following example from the northwind database, where employee names are given with an email address, but not the employee ID. We can use the $all function to retrieve the record by last name and project the id which is then merged into the original record. Note that this runs one query per row. Alternatively, you can create a lookup table (unique2key), store it in a variable, and use $lookup to get the primary key to merge.

```javascript
[{"id": "Davolio", "email": "davolio@example.org"}, {"id": "Fuller", "email": "fuller@acme.org"}]
  .$merge([
    $, 
    {"EMPLOYEE_ID": $all("northwind", "EMPLOYEES", null, null, null, false, {"LAST_NAME": id}).EMPLOYEE_ID}
  ])
```

```javascript
(
  $input := [{"id": "Davolio", "email": "davolio@example.org"}, {"id": "Fuller", "email": "fuller@acme.org"}];
  $unique2key := $all("northwind", "EMPLOYEES").{LAST_NAME: EMPLOYEE_ID};
  $input.$merge([$, {"EMPLOYEE_ID": $lookup($unique2key, $.id)}])
)
```

* **I need to ETL from an API that has a rate limit. How can I throttle my requests?** You can use the wait function in your JSONata expression:
Let's assume $openJson(url) is called on several array elements. Simply change it to $wait($openJson(x), 1000) to introduce a 1 second delay after each call.

* **How can I realize an audit log that keeps track of all changes to a table?** You can define a triggers for create, update, and delete operations on the table that must be audited. Create an audit log table with the following columns: autoincrementing ID, user, timestamp, operation, and payload. The trigger `$create("db", "audit", {"timestamp": $now(), "user": user, "operation": "update", "payload": $})` will log changes to the table.

* **Can I compute the homepage based who is logged in?** Yes, you can use the on-login expression and do some computation based on the email and user context values. The result can be fed into the setVariable function: `$setVariable("homepage", some computation using user or email)`

* **Can I initialize a SQL database with schema and data?** Yes, in Dashjoin Studio, you can add SQL scripts to your database:

```
{
    "ID": ...,
    "djClassName": "org.dashjoin.service.SQLDatabase",
    "initScripts": [
        "upload/init.sql",
    ],
    ...
```

These scripts are run when the database is connected. To setup a DB schema, you can use:

```
CREATE TABLE IF NOT EXISTS MY_TABLE(ID INT PRIMARY KEY, ...)
```

Of course you can also use SQL insert statements to load data. 
Furthermore, the H2 database (select it using the JDBC URL jdbc:h2:mem:...) offers a CSVREAD function.
This way, you can present static data to the user via SQL while being able to have a CSV version of the data as part of the app.

```
DROP TABLE IF EXISTS TEST;
CREATE TABLE TEST (ID INT PRIMARY KEY, NAME VARCHAR(255)) AS SELECT * FROM CSVREAD('dashjoin-demo/upload/test.csv');
```

Note that the CSVREAD function uses the working directory and not the app home directory. Therefore, we also append the app name, "dashjoin-demo" in this example.

* **Can I define a JSON database column to be an array of foreign keys?** Yes, using Dashjoin Studio, you can define the column metadata as follows:

```
"ID": "dj/pg",
...
"tables": {
    "test": {
        "properties": {
            "arr": {
                "type": "array",
                "items": {
                    "ref": "dj/northwind/CUSTOMERS/CUSTOMER_ID",
                    "type": "string",
                    "displayWith": "fk"
                }
            }
        }
    }
}
```

Using the following create table statement and insert JSONata statement, the record in the test table shows links to the customer table.

```
create table test (id int primary key, arr jsonb)
$create("pg", "test", {"id":1, "arr": ['BLAUS', 'ALFKI']})
```

