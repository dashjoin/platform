# JSONata Expression Reference

In addition to the default JSONata built-in functions ([see Function Library](https://docs.jsonata.org/overview.html)), the following Dashjoin functions are added
(some internal functions are omitted - you can refer to the platform's info page for a full list):

These functions can be classified as frontend and backend functions. Frontend functions run in the browser
and can be used to trigger a screen popup or to set a browser session variable.
Backend functions typically access backend data.
You can mix both kinds in a single JSONata expression tree. 
See [appendix JSONata](appendix-jsonata.md) for examples. 

## Frontend Expressions

Function | Syntax |  Returns
---|---|---
alert | $alert(message) | Shows a modal alert message
clearCache | $clearCache() | clears the HTTP cache - can be used in conjunction with expressions that trigger side effects on the backend
confirm | $confirm(message) | Opens a confirm dialog. Returns true if confirmation was given, false otherwise
dialog | $dialog({title, message, inputs, buttons, options, schema, value}) | Shows a modal dialog with title and message. The inputs and buttons arrays denote which input fields and buttons are shown. The result of the call is defined as follows. If the dialog is closed, the result is undefined. If a button is pressed, the result is the button id or - if the button is an object with label and type: submit - an object with the inputs. If options is set to an object with alert: info / warning / error, the dialog message is shows with the respective label. If you want to display a more complex form, you can specify form elements via schema (see [Form Widgets](widget-reference.md#form-widgets)). A schema allows you to optionally pre-fill the form via the value field.
getVariable | $getVariable(key) | Gets the value of the variable key. Note that this is similar to accessing variable.key in the context. The difference is that getVariable will get the last value set whereas variable.key gets the value while the page loaded.
log | $log(value) | logs value to the developer console
navigate | $navigate(url) | points the browser to the URL
navigate | $navigate( {id, options} ) | scrolls the page to the [DOM element with the id](https://developer.mozilla.org/en-US/docs/Web/API/Element/scrollIntoView)
notify | $notify(message) | Shows the message at the bottom of the screen
progress | $progress({message?, linear?, value?, variant?}) | Shows progress message during long running operations. If the variant is set to determinate, the progress shows the value between 0 and 100. If linear is true, the progress is shown using a linear bar. If progress is called without parameter, the progress display disappears.
prompt | $prompt(message) | Prompts the user for an input. Returns the input or undefined if the prompt is cancelled
refresh | $refresh() | refreshes the screen (just like hitting the refresh icon in the toolbar)
reload | $reload() | reloads the browser page
setActiveStep | $setActiveStep(i) | Sets the current step of any stepper widget on the page
setVariable | $setVariable(key, value) | Sets variable key to value. The key value pair then becomes accessible via the context by other expressions
sleep | $sleep(millisecs) | Sleep for x milliseconds
speak | $speak(message, language?) | Text to speech
sse | $sse(aiapp name, [{role, content}]) | Calls the AIApp function called aia with messages. Use only in the AIChat widget's onChat expression
stepBack | $stepBack() | Sets back the current step of any stepper widget on the page
stepForward | $stepForward() | Advances the current step of any stepper widget on the page
stopSpeech | $stopSpeech() | Stop any text to speech that is still in progress
translate | $translate(text) | Lookup the text in the translation file and return the match for the current locale

## Backend Expressions

Function | Syntax |  Returns
---|---|---
adHocQuery| $adHocQuery(database, query, limit?) | Runs as ad hoc select / read query
all| $all(database, table) | array of all table records
all| $all(database, table, offset, limit, sort, descending, filter) | array of all table records whose columns match the filter key's values
call| $call(function, argument) |   Dashjoin function result
chat | $chat(url, query, messages, tools?, options?, headers?) | Encapsulates API calls to LLMs running at url. Query is the next user message. messages is an array of role / content containing the previous chat interactions. Tools is an array of strings referencing Invoke functions in the function catalog that should be used as tools during the LLM call. Options contains additional LLM parameters like "model": "gpt-4o-mini", etc. Finally, headers works like with the curl function. 
classifyEntities | $classifyEntities([entities], entity-language?, limit?, subclass-depth?) | Reconciles entities and finds common classifications that all entities are an instance of. The parameters are similar to the reconcileEntity function. The subclass depth (default 1) describes the number of superclasses that are included in the results (see [AI & ML Integration](ai-ml-integration.md))
collectMetadata | $collectMetadata(id) | Reconnects database with id and updates and schema changes
create| $create(database, table, object) | ID of the new record
curl | $curl(method, url, data?, headers?) | Full fledged HTTP client. Use header {"Authorization": credential} to reference a credential set defined in functions (e.g. $curl("GET", url, null, {"Authorization": "openai"})). Use header {"dj-insert-credentials": [field, ...]} to replace the values of these fields in data (which contain credential names) with the credential secret. Use header {"dj-timeout-seconds": ...} to define a HTTP timeout other than the default of 10s. Use header {"dj-encoding": ...} to encode in UTF_8 (default), BASE_64, ISO_8859_1. Use header {"dj-produces": "text/plain"} to return plain text rather than JSON. Use header {"Content-Type": "multipart/form-data"} to upload files encoded in data URLs in the data object (see Input widget)
delete| $delete(database, table, pk1)
djLint | $djLint() | Scans the app for errors and warnings
djRoles | $djRoles() | Returns the roles of the current user
djUser | $djUser() | Returns the current user's name
djVersion | $djVersion() | Returns the platform version information
echo | $echo(any) | Prints the parameter to the log
erDiagram| $erDiagram(database?) | Generate an ER diagram for https://dbdiagram.io/d
etl | $etl(foreach, expression, database) | Run an ETL process programmatically. The function requires an expression string. If you want to pass data directly, simply convert the data to a string by using the expression $string(data)
etlSync | $etlSync(source, target, url column) | Compute the files changed since the last run
exec | $exec(executable, arguments, [json, xml, csv, yaml]) | runs the script or executable located in the app's bin folder and optionally parses the output to JSON, XML, or CSV
generateCsv | $generateCsv(data, options) | Converts a table to CSV (for options see openCsv)
generateExcel | $generateExcel(table or map of sheetname to table) | Generates a base64 encoded Excel file which can be downloaded from the browser (see FAQ)
generateXml | $generateXml(data) | Converts data to an XML string
generateYaml | $generateYaml(data) | Converts data to a YAML string
gitCheckout| $gitCheckout() | Checkout and switch to branch
gitCommit| $gitCommit(message, [paths]) | Run git commit and push
gitPull| $gitPull() | Run git pull
gitRestore| $gitRestore(path) | Revert a change
gitStatus| $gitStatus() | Run git status
hmac | $hmac(keyId, message, algorithm?, encoding?) | Signs message with the secret of the Credentials function keyId. algorithm: HmacSHA256 (default), HmacSHA384, HmacSHA512. encoding: base64 (default), base64url, hex. The secret is never returned. Only users with one of the roles of the Credentials function may use it (admin only if it has none). Wrap $hmac in an Invoke function with runas to let other users sign a fixed kind of message
incoming| $incoming(database, table, pk1)| [{id: ID of the record where the link originates, pk: ID of the pk column, fk: ID of the fk column}, ...]
index | $index() | Generates a unique row index ID
isRecursiveTrigger | $isRecursiveTrigger() | true if the current expression is called from a trigger expression (trigger calls trigger)
ls | $ls(url, preview-limit) | Lists all URLs found at url (the URL can also contain filter wildcards like *.txt). preview limit determines how many results are returned in preview mode (defaults to 10)
openCsv | $openCsv(url, options) | Parses CSV at the url and converts it to JSON. By default, CSV is parsed as RFC4180. Options can be provided, where the key is a "with" method like withDelimiter and the value is the argument. Please see the [documentation](https://commons.apache.org/proper/commons-csv/apidocs/org/apache/commons/csv/CSVFormat.html) for more details.
openExcel | $openExcel(url) | Parses Excel at the url and converts it to JSON
openJson | $openJson(url) | Parses JSON at the url
openText | $openText(url, encoding) | Parses the url and converts it to a string. Possible encodings are: UTF_8 (default), BASE_64, ISO_8859_1
openXml | $openXml(url, arrays) | Parses XML at the url and converts it to JSON. In this process, openXml guesses which XML tags need to be converted to arrays and which become simple fields. This process might produce inconsistent results when the XML tree contains lists with single entries. To avoid this, you can optionally pass a list of tag names that must be arrays.
openYaml | $openYaml(url) | Parses YAML at the url and converts it to JSON
parseCsv | $parseCsv(csv, options) | Parses CSV and converts it to JSON (see openCsv)
parseExcel | $parseExcel(base64) | Parses Excel and converts it to JSON (see openExcel). The parameter must be a base64 encoded data URL (RFC 2397)
parseFrontmatter | $parseFrontmatter(markdown) | Parses markdown containing YAML frontmatter metadata
parseHtml | $parseHtml(html, query, xpath/css?) | Parses HTML and selects nodes via CSS or XPath (see https://jsoup.org/cookbook/extracting-data/xpath-syntax)
parseJson | $parseJson(json) | Parses JSON (see openJson)
parseUrl | $parseUrl(url) | Parses a URL into protocol, host, port, path, query, etc.
parseXml | $parseXml(xml, arrays) | Parses XML and converts it to JSON (see openXml)
parseYaml | $parseYaml(yaml) | Parses YAML and converts it to JSON (see openYaml)
query| $query(database, queryId, arguments) |Query result table
queryGraph| $queryGraph(database, queryId, arguments) |Graph query result, specifying the database as * runs an OpenCypher query over all DBs
read|  $read(database, table, pk1) | The record
reconcileEntity | $reconcileEntity(entity, entity-language?, limit?) | Uses the wikidata query service to reconcile a string to a wikidata id. The entity is a simple string. The entity language is the language the entity is expressed in (defaults to en). The limit (default 1) determines the number of results returned (see [AI & ML Integration](ai-ml-integration.md))
saveTable| $saveTable(Ignore \| Refresh \| Delete All, database, table, data, pk?) | Allows performing an ad hoc ETL operation (note that createSchema is true)
search| $search(term, limit?, database?, table?) | Searches the databases(s)
setIfAbsent| $setIfAbsent(database, table, pk1, object) | like update, but only sets fields that are not yet set
stats| $stats(database, table, limit?) | Generate statistics for a database table (type, min, max, count, distinct values, etc.)
streamCsv | $streamCsv(url, options) | Parses CSV at the url and splits it at the record boundaries. By default, CSV is parsed as RFC4180. Options can be provided, where the key is a "with" method like withDelimiter and the value is the argument. Please see the [documentation](https://commons.apache.org/proper/commons-csv/apidocs/org/apache/commons/csv/CSVFormat.html) for more details.
streamDb | $streamDb(database, table) | Streams records from the database table specified
streamJson | $streamJson(url, jsonPointer) | Parses JSON at the url and splits it at the [json pointer](https://datatracker.ietf.org/doc/html/rfc6901) location
streamXml | $streamXml(url, jsonPointer) | Parses XML at the url, converts it to JSON, and splits it at the [json pointer](https://datatracker.ietf.org/doc/html/rfc6901) location
synonym | $synonym({algorithm: threshold}, [terms], [variants], ignoreCase?, ignoreEquality?) | Allows generating synonym table to match keys despite small typos etc. (see [AI & ML Integration](ai-ml-integration.md))
traverse|  $traverse(database, table, pk1, fk) | Record(s) related to the current record via the property fk. If fk is a simple column name, fk is an outgoing foreign key and the single related record is returned. If fk is a full property ID like dj/database/table/column, then a list of records from that table that have a fk pointing to the current record are returned
try | $try(function(){...}, function($ex){...}) | Try-catch with the second function being called when there's an error thrown by the first function. If the second function is omitted, the try function returns undefined upon an error
update| $update(database, table, pk1, object)
upsert| $upsert(database, table, object) | first tries to create the record, if the record already exists, applies an update by retrieving the keys from the object and calling update
urlExists| $urlExists(url) | Returns true if the url is reachable, false otherwise
use | $use(id) | Reads the library with the given id, and returns the expression. Usually, used to import functions from the library: $f := $use('f') ~> $eval
uuid | $uuid() | Generates a random UUID
validate | $validate(schema, json) | Validates the JSON value against JSON schema. Raises an error if validation fails. Supports type, required, properties, and items keywords
wait| $wait(object, millisecs) | Wait millisecs provided before returning object
