# Appendix: JSONata function library
## confirm
displays a confirmation dialog and returns true if the user clicked yes
```
$confirm('Are you sure') ? 'you clicked yes'
```
Sample output: 
```json
true
```
## setVariable
Sets the browser session variable x to 1. The new value can be read in other places via variable.x
```
$setVariable('x', 1)
```
Sample output: 
```json
null
```
## prompt
Prompts the user for an input. Returns the input or undefined if the prompt is cancelled
```
(
  $name := $prompt('What is your name?');
  $name ? ('Hello ' & $name);
)
```
Sample output: 
```json
"Joe"
```
## alert
Shows a model popup with a message
```
$alert('FYI')
```
Sample output: 
```json
"OK"
```
## dialog
Shows a dialog to enter 'to' and 'text'. If the user submits via 'ok', an object with these fields is returned
```
$dialog({
  'title':'Send Message', 
  'message':'Please enter your message', 
  'inputs':['to', 'content'], 
  'buttons':[{'label':'ok', 'type':'submit'},'cancel'], 
  'options':{'alert':'info'}
})
```
Sample output: 
```json
null
```
## notify
Shows a message in the snackbar at the bottom of the screen
```
$notify('Hi from the snackbar!')
```
Sample output: 
```json
null
```
## refresh
refreshes the screen just (just like hitting the refresh icon in the toolbar)
```
$refresh()
```
Sample output: 
```json
null
```
## reload
reloads the browser page
```
$reload()
```
Sample output: 
```json
null
```
## navigate
points the browser to the URL
```
$navigate('http://dashjoin.com')
```
Sample output: 
```json
null
```
## navigate
scrolls the page to the DOM element with the given id. In this case, we scroll to the widget that has the title 'Top'
```
$navigate({'id': 'dj-Top'})
```
Sample output: 
```json
null
```
## clearCache
clears the HTTP cache - can be used in conjunction with expressions that trigger side effects on the backend
```
$clearCache()
```
Sample output: 
```json
null
```
## progress
shows a progress indicator with the message 'working...' for 1 second until the expression execution completes
```
(
  $progress({'message':'working...'});
  $sleep(1000)
)
```
Sample output: 
```json
null
```
## sleep
sleeps for 1000 milliseconds
```
$sleep(1000)
```
Sample output: 
```json
null
```
## speak
performs a text to english speech for 'Test'
```
$speak('Test', 'en')
```
Sample output: 
```json
null
```
## stopSpeech
stops any text to speech that's in progress
```
$stopSpeech()
```
Sample output: 
```json
null
```
## translate
Lookup the text in the translation file and return the match for the current locale
```
$translate('ra.action.select_all_button')
```
Sample output: 
```json
"Select all"
```
## create
create a new record in database 'sqlite', table 'REQUESTS'
```
$create('sqlite', 'REQUESTS', {'ID': 7, 'name':'new request'})
```
Sample output: 
```json
{
  "database" : "sqlite",
  "table" : "REQUESTS",
  "pk" : [ 7 ]
}
```
## upsert
inserts or updates record in database 'sqlite', table 'REQUESTS'
```
$upsert('sqlite', 'REQUESTS', {'ID': 7, 'name':'updated request'})
```
Sample output: 
```json
null
```
## all
get all records of the REQUESTS table in the database 'sqlite'
```
$all('sqlite', 'REQUESTS')
```
Sample output: 
```json
[ {
  "ID" : 1,
  "name" : "Can you please send me an offer",
  "submitted" : "2021-01-01 10:20",
  "customer" : "ALFKI",
  "user" : "user"
} ]
```
## all
get the first ten records of the REQUESTS table in the database 'sqlite', sort by 'ID', descending=true, with the filter customer='ALFKI'
```
$all('sqlite', 'REQUESTS', 1, 10, 'ID', true, {'customer':'ALFKI'})
```
Sample output: 
```json
[ {
  "ID" : 1,
  "name" : "Can you please send me an offer",
  "submitted" : "2021-01-01 10:20",
  "customer" : "ALFKI",
  "user" : "user"
} ]
```
## read
read the records with primary key 7 from table 'REQUESTS' in database 'sqlite'
```
$read('sqlite', 'REQUESTS', 7)
```
Sample output: 
```json
{
  "ID" : 7,
  "name" : "updated request",
  "submitted" : null,
  "customer" : null,
  "user" : null
}
```
## traverse
gets all related records for ORDER 10250 from the ORDER_DETAILS table (via foreign key ORDER_DETAILS.ORDER_ID)
```
$traverse('northwind', 'ORDERS', '10250', 'dj/northwind/ORDER_DETAILS/ORDER_ID')
```
Sample output: 
```json
[ {
  "ORDER_ID" : 10250,
  "PRODUCT_ID" : 41,
  "UNIT_PRICE" : 7.7,
  "QUANTITY" : 10,
  "DISCOUNT" : 0
} ]
```
## traverse
get the related customer for ORDER 10250 from the CUSTOMER table (via foreign key CUSTOMER_ID)
```
$traverse('northwind', 'ORDERS', '10250', 'CUSTOMER_ID')
```
Sample output: 
```json
{
  "CUSTOMER_ID" : "HANAR",
  "COMPANY_NAME" : "Hanari Carnes",
  "CITY" : "Rio de Janeiro",
  "PHONE" : "(21) 555-0091"
}
```
## update
update REQUEST 7 in the database 'sqlite'
```
$update('sqlite', 'REQUESTS', 7, {'name':'updated request'})
```
Sample output: 
```json
null
```
## delete
delete REQUEST 7 in the database 'sqlite'
```
$delete('sqlite', 'REQUESTS', 7)
```
Sample output: 
```json
null
```
## call
call function 'add' from the function catalog using the provided parameter
```
$call('add', {'x':1, 'y':2})
```
Sample output: 
```json
{
  "result" : 3
}
```
## query
run query 'group' on database 'northwind'
```
$query('northwind', 'group')
```
Sample output: 
```json
[ {
  "CUSTOMERS.COUNTRY" : "Argentina",
  "Number of Customers" : 3
} ]
```
## query
run query 'list' on datanase 'northwind' using query parameters limit 1 and offset 3
```
$query('northwind', 'list', {'limit':1, 'offset':'3'})
```
Sample output: 
```json
[ {
  "CATEGORIES.CATEGORY_ID" : 4,
  "CATEGORIES.CATEGORY_NAME" : "Dairy Products",
  "CATEGORIES.DESCRIPTION" : "Cheeses",
  "CATEGORIES.PICTURE" : ""
} ]
```
## queryGraph
run graph query cypher on over all databases
```
$queryGraph('*', 'cypher')
```
Sample output: 
```json
null
```
## adHocQuery
Run query 'select * from EMPLOYEES' on database 'northwind'
```
$adHocQuery('northwind', 'select * from EMPLOYEES')
```
Sample output: 
```json
[ {
  "EMPLOYEES.EMPLOYEE_ID" : 1,
  "EMPLOYEES.LAST_NAME" : "Davolio",
  "EMPLOYEES.FIRST_NAME" : "Nancy"
} ]
```
## search

```
$search('Fuller')
```
Sample output: 
```json
[ {
  "id" : {
    "database" : "northwind",
    "table" : "EMPLOYEES",
    "pk" : [ 2 ]
  },
  "column" : "LAST_NAME",
  "match" : "Fuller"
} ]
```
## search
run a full text search for 'Fuller'over the EMPLOYEES table in the 'northwind' database. Limit the result size to 5.
```
$search('Fuller', 5, 'northwind', 'EMPLOYEES')
```
Sample output: 
```json
[ {
  "id" : {
    "database" : "northwind",
    "table" : "EMPLOYEES",
    "pk" : [ 2 ]
  },
  "column" : "LAST_NAME",
  "match" : "Fuller"
} ]
```
## incoming
get all records from all databases that point to northwind EMPLOYEE 2
```
$incoming('northwind', 'EMPLOYEES', 2)
```
Sample output: 
```json
[ {
  "id" : {
    "database" : "northwind",
    "table" : "EMPLOYEES",
    "pk" : [ 1 ]
  },
  "pk" : "dj/northwind/EMPLOYEES/EMPLOYEE_ID",
  "fk" : "dj/northwind/EMPLOYEES/REPORTS_TO"
} ]
```
## echo
prints 'hi' to the log and returns 'hi'
```
$echo('hi')
```
Sample output: 
```json
"hi"
```
## index
In ETL mapping expressions, generates a unique row index ID
```
$index()
```
Sample output: 
```json
42
```
## djVersion
Returns the platform version information
```
$djVersion()
```
Sample output: 
```json
{
  "version" : "6.1.0-d26fc67-6f4f682",
  "title" : "dashjoin-platform",
  "vendor" : "Dashjoin",
  "name" : "Dashjoin Low Code Development and Integration Platform",
  "buildTime" : "2025-07-31T11:22:48+0000",
  "runtime" : "21.0.6",
  "workingDir" : "/deployments",
  "home" : "/mnt/dashjoin/app",
  "appurl" : "https://github.com/dashjoin/dashjoin-demo"
}
```
## djRoles
Returns the roles of the current user
```
$djRoles()
```
Sample output: 
```json
[ "authenticated", "admin" ]
```
## djUser
Returns the current user
```
$djUser()
```
Sample output: 
```json
"admin@localhost"
```
## isRecursiveTrigger
checks if a trigger expression (e.g. onUpdate) calls itself in order to avoid endless recursions
```
$isRecursiveTrigger()
```
Sample output: 
```json
false
```
## ls
Lists all URLs found at file:upload
```
$ls('file:upload')
```
Sample output: 
```json
[ {
  "url" : "file:upload/openapi.yaml",
  "folder" : false,
  "size" : 480,
  "modified" : 1737640794571,
  "etag" : null
} ]
```
## streamJson
open file:upload/test.json, locate the array at the JSON Pointer /data and stream the array items into the ETL queue from the foreach expression
```
$streamJson(''file:upload/test.json, '/data')
```
Sample output: 
```json
null
```
## streamXml
open file:upload/test.xml, locate the array at the JSON Pointer /data and stream the array items into the ETL queue from the foreach expression
```
$streamJson(''file:upload/test.xml, '/data')
```
Sample output: 
```json
null
```
## streamCsv
open semi-colon delimited file file:upload/test.csv and stream the records into the ETL queue from the foreach expression
```
$streamCsv('file:upload/test.csv', {'withDelimiter': ';'})
```
Sample output: 
```json
null
```
## streamDb
stream the records of the ORDERS table of the northwind database into the ETL queue from the foreach expression
```
$streamDb('northwind', 'ORDERS')
```
Sample output: 
```json
null
```
## curl
open the HTTP GET service at the given URL and pass the authorization header
```
$curl('GET', 'http://localhost:8080/rest/manage/version', null, {'Authorization': 'Basic YWRtaW46ZGpkamRq'})
```
Sample output: 
```json
{
  "vendor" : "Dashjoin",
  "name" : "Dashjoin Low Code Development and Integration Platform"
}
```
## chat
Call the LLM comlpetion API of OpenAI with the query 'hi', the system prompt 'be funny', and the 'openai' credential
```
$chat('https://api.openai.com/v1/chat/completions', 'hi', [{'role': 'system', 'content': 'be funny!'}], [], {'model':'gpt-4o-mini'}, {'Authorization': 'openai', 'dj-timeout-seconds': 60})
```
Sample output: 
```json
[ {
  "role" : "system",
  "content" : "be funny!"
}, {
  "content" : "hi",
  "role" : "user"
}, {
  "role" : "assistant",
  "content" : "Hello! How can I tickle your funny bone today?",
  "refusal" : null,
  "annotations" : [ ]
} ]
```
## openJson
Open 'file:upload/test.json', parse the JSON content and return it
```
$openJson('file:upload/test.json')
```
Sample output: 
```json
{
  "x" : 3.1415
}
```
## openCsv
Open 'file:upload/test.csv', parse the semi-colon delimited CSV, convert it to JSON and return it
```
$openCsv('file:upload/test.csv', {'withDelimiter': ';'})
```
Sample output: 
```json
[ {
  "id" : "1",
  "name" : "Joe"
} ]
```
## openXml
Open 'file:upload/test.xml', parse the XML content, convert it to JSON and return it
```
$openXml('file:upload/test.xml')
```
Sample output: 
```json
{
  "x" : 3.1415
}
```
## openYaml
Open 'file:upload/test.yaml', parse the YAML content, convert it to JSON and return it
```
$openYaml('file:upload/test.yaml')
```
Sample output: 
```json
{
  "x" : 3.1415
}
```
## openExcel
Open 'file:upload/test.xlsx', and return a JSON map of all sheets
```
$openExcel('file:upload/test.xlsx')
```
Sample output: 
```json
{
  "Sheet2" : [ {
    "id" : 1,
    "name" : "Joe"
  } ]
}
```
## openText
Open 'file:upload/test.json' and return it base64 encoded
```
$openText('file:upload/test.json', 'BASE_64')
```
Sample output: 
```json
"ewoJIngiOiAzLjE0MTUKfQ=="
```
## openText
Open 'file:upload/test.json' using ISO_8859_1 encoding
```
$openText('file:upload/test.json', 'ISO_8859_1')
```
Sample output: 
```json
"{\"x\": 3.1415}"
```
## parseJson
Parse a string to JSON and return it
```
$parseJson('{"x":1}')
```
Sample output: 
```json
{
  "x" : 1
}
```
## parseCsv
Parse a string to CSV, convert it to JSON and return it
```
$parseCsv('id,name
1,joe')
```
Sample output: 
```json
[ {
  "id" : "1",
  "name" : "joe"
} ]
```
## parseHtml
Open the dashjoin website, parse the HTML and extract all h1 elements
```
$parseHtml($openText('https://dashjoin.com'), '//h1')
```
Sample output: 
```json
[ "<h1 class=\"text-white\">Rapidly Build Apps Driven by Data and AI</h1>" ]
```
## parseXml
Parse a string to XML, convert it to JSON and return it
```
$parseXml('<x>1</x>')
```
Sample output: 
```json
{
  "x" : "1"
}
```
## parseYaml
Parse a string to YAML, convert it to JSON and return it
```
$parseYaml('x: 1')
```
Sample output: 
```json
{
  "x" : "1"
}
```
## parseExcel
Parses a base64 data URL to Excel, converts that to JSON and return it
```
$parseExcel('data:;base64,' & $openText('file:upload/test.xlsx', 'BASE_64'))
```
Sample output: 
```json
{
  "Sheet2" : [ {
    "id" : 1,
    "name" : "Joe"
  } ]
}
```
## parseUrl
Parses a URL and return an object describing it
```
$parseUrl('https://dashjoin.com/s?q')
```
Sample output: 
```json
{
  "authority" : "dashjoin.com",
  "defaultPort" : 443,
  "file" : "/s?q",
  "host" : "dashjoin.com",
  "path" : "/s",
  "port" : -1,
  "protocol" : "https",
  "query" : "q",
  "userInfo" : null
}
```
## uuid
Generates a random UUID
```
$uuid()
```
Sample output: 
```json
"2b10beb5-d83d-4733-a67d-b6c5e4315a29"
```
## exec
run the script 'bin/test.sh'
```
$exec('bin/test.sh', 'call echo $*')
```
Sample output: 
```json
{
  "out" : "\"call echo $*\"\n",
  "err" : "",
  "code" : 0
}
```
## erDiagram
generate database schema information for database 'sqlite'
```
$erDiagram('sqlite')
```
Sample output: 
```json
"Table REQUESTS{\n  ID INTEGER [primary key]\n  name VARCHAR\n  submitted VARCHAR\n  customer VARCHAR [ref: > CUSTOMERS.CUSTOMER_ID]\n  user VARCHAR\n}"
```
## stats
return database statistics
```
$stats('sqlite', 'REQUESTS')
```
Sample output: 
```json
[ {
  "ID" : "dj/sqlite/REQUESTS/ID",
  "count" : 6,
  "nulls" : 0,
  "countdistinct" : 6,
  "min" : 1,
  "max" : 8,
  "dbType" : "INTEGER",
  "type" : "number",
  "detectedType" : "string"
} ]
```
## gitStatus
return the git status of the dashjoin app
```
$gitStatus()
```
Sample output: 
```json
[ {
  "path" : "dashjoin-demo.db",
  "type" : "modified",
  "diff" : "diff --git a/dashjoin-demo.db b/dashjoin-demo.db\nindex 6fdb4ce..d29b8f1 100644\n--- a/dashjoin-demo.db\n+++ b/dashjoin-demo.db\nBinary files differ\n"
} ]
```
## gitPull
Pull the dashjoin app
```
$gitPull()
```
Sample output: 
```json
null
```
## gitRestore
git restore 'dashjoin-demo.db'
```
$gitRestore('dashjoin-demo.db')
```
Sample output: 
```json
null
```
## gitCommit
git commit a set of files
```
$gitCommit('db change', ['dashjoin-demo.db'])
```
Sample output: 
```json
null
```
## saveTable
save the data (with primary key id) to the test table in the sqlite database
```
$saveTable('Delete All', 'sqlite', 'test', [{'id':1, 'name':'joe'}], 'id')
```
Sample output: 
```json
null
```
## reconcileEntity
Uses the wikidata query service to reconcile a string to a wikidata id
```
$reconcileEntity('Apple')
```
Sample output: 
```json
[ {
  "id" : "Q312",
  "label" : "Apple Inc.",
  "description" : "American multinational technology company based in Cupertino, California"
} ]
```
## classifyEntities
Reconciles entities and finds common classifications that all entities are an instance of
```
$classifyEntities(['VW', 'Tesla'])
```
Sample output: 
```json
[ "business", "enterprise", "juridical person", "organization", "economic entity", "group" ]
```
## urlExists
checks if a URL exists
```
$urlExists('https://dashjoin.com')
```
Sample output: 
```json
true
```
## etl
Run an ETL that loads data into the sqlite database with createSchema set to true
```
$etl('', '[{"id":1, "name":"mike"}]', 'sqlite', null, null, true)
```
Sample output: 
```json
null
```
## etlSync
Compares two tables with URL / file information to determine which files changed. These can be used for a delta ETL.
```
$etlSync([{'url':'file:a', 'version': 2}], [{'url':'file:a', 'version': 1}], 'url')
```
Sample output: 
```json
[ {
  "url" : "file:a",
  "version" : 2
} ]
```
