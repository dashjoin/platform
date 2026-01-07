# Appendix: Widgets
## display widget simple value
Used to visualize the result of any jsonata expression. Here, we display the value of the session variable x
```json
{
  "widget" : "display",
  "display" : "variable.x"
}
```
## display widget current database record
Used to visualize the result of any jsonata expression. Here, we display the current record on an instance page
```json
{
  "widget" : "display",
  "display" : "value"
}
```
## display widget UI state
Used to visualize the result of any jsonata expression. Here, we display current URL, theme (light/dark), screen width, and location info
```json
{
  "display" : "[href, theme, width, loc]",
  "widget" : "display"
}
```
## display widget showing object
Used to visualize the result of any jsonata expression. Here, we display the current time as a key-value list
```json
{
  "widget" : "display",
  "display" : "{'time': $now()}"
}
```
## display widget showing link
Used to visualize the result of any jsonata expression. Here, we display a link to a record in the database
```json
{
  "display" : "{'database': 'northwind', 'table': 'EMPLOYEES', 'pk1':2}",
  "widget" : "display"
}
```
## display widget showing table
Used to visualize the result of any jsonata expression. Here, we display a list of objects as a table
```json
{
  "display" : "[{'x':1, 'y':1}, {'x':2, 'y':2}]",
  "widget" : "display"
}
```
## display widget showing an image
if the object has exactly the key 'img' (with optional width and height), the result is displayed as an HTML image with the value of the img field being used as the image src attribute
```json
{
  "display" : "{'img': 'https://dashjoin.com/img/fav.png'}",
  "widget" : "display"
}
```
## display widget showing a hyperlink
if the object has exactly the key 'href' or the keys 'href' and 'label', the object is displayed as a hyperlink
```json
{
  "display" : "{'href':'http://dashjoin.com', 'label':'DJ Homepage'}",
  "widget" : "display"
}
```
## display widget showing a hyperlink
absolute or relative links to another page in the app are specified without the 'slash hash' part of the URL - for instance, the href 'Info' or '/page/Info' links to the Info page
```json
{
  "display" : "{'href':'/#/page/Home', 'label':'Home'}",
  "widget" : "display"
}
```
## display widget on dashboard page
shows how a display widget is added on a dashboard page
```json
{
  "ID" : "test",
  "layout" : {
    "widget" : "page",
    "children" : [ {
      "display" : "{'database': 'northwind', 'table': 'EMPLOYEES', 'pk1':2}",
      "widget" : "display"
    } ]
  }
}
```
## display widget on instance page
shows how a display widget is added on a database instance page
```json
{
  "name" : "sqlite",
  "ID" : "dj/sqlite",
  "djClassName" : "org.dashjoin.service.SQLDatabase",
  "url" : "jdbc:sqlite:dashjoin-demo.db",
  "tables" : {
    "REQUESTS" : {
      "instanceLayout" : {
        "widget" : "page",
        "children" : [ {
          "display" : "$now()",
          "widget" : "display"
        } ]
      }
    }
  }
}
```
## display widget on table page
shows how a display widget is added on a table page
```json
{
  "name" : "sqlite",
  "ID" : "dj/sqlite",
  "djClassName" : "org.dashjoin.service.SQLDatabase",
  "url" : "jdbc:sqlite:dashjoin-demo.db",
  "tables" : {
    "REQUESTS" : {
      "tableLayout" : {
        "widget" : "page",
        "children" : [ {
          "display" : "$now()",
          "widget" : "display"
        } ]
      }
    }
  }
}
```
## display widget with JSON keys containing special characters
SQL queries create JSON with field names 'table.column'. In JSONata, enclose the field name in backticks (`)
```json
{
  "display" : "$adHocQuery('sqlite', 'select ID from REQUESTS').`REQUESTS.ID`",
  "widget" : "display"
}
```
## card widget
The card widget shows its children within a card
```json
{
  "widget" : "card"
}
```
## container widget for authenticated role
Provides a logical container for its children. This example displays children only if the user in the the role 'authenticated'
```json
{
  "roles" : [ "authenticated" ],
  "widget" : "container"
}
```
## container widget conditionally showing contents
Provides a logical container for its children. This example displays children only if the jsonata expression is true
```json
{
  "if" : "email = 'admin@localhost'",
  "widget" : "container"
}
```
## container widget showing contents for each value
Provides a logical container for its children. This example shows the child for each value of the foreach expression
```json
{
  "foreach" : "[1..5]",
  "widget" : "container",
  "children" : [ {
    "display" : "value",
    "widget" : "display"
  } ]
}
```
## expansion widget shows children in an expansion panel
Provides a logical container for its children and shows them in an expansion panel
```json
{
  "text" : "Open Me",
  "widget" : "expansion"
}
```
## stepper widget displays its children as a wizard
allows stepping through its children. It displays the step number and title along with the child at the current step position
```json
{
  "widget" : "stepper",
  "children" : [ {
    "title" : "One",
    "text" : "Next",
    "print" : "$stepForward()",
    "widget" : "button"
  }, {
    "text" : "Back",
    "title" : "Two",
    "print" : "$stepBack()",
    "widget" : "button"
  } ]
}
```
## tabs widget displays its children as tabs
displays its children in a tab container which allows selecting the current tab on top
```json
{
  "widget" : "tabs",
  "children" : [ {
    "title" : "First",
    "widget" : "text"
  } ]
}
```
## button widget
shows a button to trigger an action with an optional set of inputs. This example prints the contents of the form input
```json
{
  "widget" : "button",
  "print" : "form.name",
  "schema" : {
    "type" : "object",
    "properties" : {
      "name" : {
        "widget" : "string"
      }
    }
  }
}
```
## button widget: Dynamically compute form fields
schemaExpression is an expression that computes JSON Schema which in turn defines the form fields dynamically
```json
{
  "schemaExpression" : "{'properties':{'name': {'type':'string'}}}",
  "print" : "form.name",
  "widget" : "button"
}
```
## button widget: show a form field conditionally
schemaExpression is an expression that computes JSON Schema which in turn defines the form fields dynamically: The 'type' field is defined as the switch. Other fields can specify 'case' with the type value of when they are shown
```json
{
  "widget" : "button",
  "print" : "form",
  "schemaExpression" : "{'switch':'type', 'properties': {'type': {'widget': 'select', 'options':'[\"circle\"]'}, 'radius': {'case': 'circle'}}}"
}
```
## button widget: computing select options
The button form has a select field to input 'id'. The select options are computed from the database
```json
{
  "widget" : "button",
  "print" : "form.field",
  "schema" : {
    "type" : "object",
    "properties" : {
      "id" : {
        "widget" : "select",
        "options" : "$all('northwind', 'EMPLOYEES').{'value':EMPLOYEE_ID, 'name': LAST_NAME}"
      }
    }
  }
}
```
## button widget: JavaScript expressions
Expressions that start with // JavaScript can use JavaScript. This example triggers a file download
```json
{
  "widget" : "button",
  "print" : "// JavaScript\nvar blob = new Blob(['Hello, world!'], {type: 'text/plain;charset=utf-8'});\nsaveAs(blob, 'hello world.txt');"
}
```
print
```
// JavaScript
var blob = new Blob(['Hello, world!'], {type: 'text/plain;charset=utf-8'});
saveAs(blob, 'hello world.txt');
```
## button widget: JavaScript expressions
Expressions that start with // JavaScript can use JavaScript. This example calls the $read() function and returns the result
```json
{
  "widget" : "button",
  "print" : "// JavaScript\nreturn await read('sqlite', 'REQUESTS', 1)"
}
```
print
```
// JavaScript
return await read('sqlite', 'REQUESTS', 1)
```
## create widget
Shows a form on the database table page to create a new database record
```json
{
  "name" : "sqlite",
  "ID" : "dj/sqlite",
  "djClassName" : "org.dashjoin.service.SQLDatabase",
  "url" : "jdbc:sqlite:dashjoin-demo.db",
  "tables" : {
    "REQUESTS" : {
      "tableLayout" : {
        "widget" : "page",
        "children" : [ {
          "widget" : "create",
          "schema" : {
            "type" : "object",
            "properties" : {
              "ID" : {
                "ID" : "dj/sqlite/REQUESTS/ID",
                "parent" : "dj/sqlite/REQUESTS",
                "dbType" : "INTEGER",
                "type" : "number",
                "pkpos" : 0,
                "createOnly" : true
              }
            }
          }
        } ]
      }
    }
  }
}
```
## edit widget
Shows a form on the instance page to edit or delete the instance
```json
{
  "name" : "sqlite",
  "ID" : "dj/sqlite",
  "djClassName" : "org.dashjoin.service.SQLDatabase",
  "url" : "jdbc:sqlite:dashjoin-demo.db",
  "tables" : {
    "REQUESTS" : {
      "instanceLayout" : {
        "widget" : "page",
        "children" : [ {
          "widget" : "edit",
          "schema" : {
            "type" : "object",
            "properties" : {
              "ID" : {
                "ID" : "dj/sqlite/REQUESTS/ID",
                "parent" : "dj/sqlite/REQUESTS",
                "dbType" : "INTEGER",
                "type" : "number",
                "pkpos" : 0,
                "createOnly" : true,
                "size" : 6
              }
            }
          }
        } ]
      }
    }
  }
}
```
## variable widget
Shows a form that sets session variables. This example sets the variable 'field'
```json
{
  "widget" : "variable",
  "schema" : {
    "type" : "object",
    "properties" : {
      "field" : {
        "widget" : "number"
      }
    }
  }
}
```
## action table
Visualizes 'expression' as a table. The 'properties' field contains an object where the keys are button labels and the values contain actions to be performed when the button is pressed. The action expression gets the context passed which includes the selected objects in the 'selected' field
```json
{
  "expression" : "$all('sqlite', 'REQUESTS')",
  "properties" : {
    "print" : "selected.$echo($)"
  },
  "widget" : "actionTable"
}
```
## aichat widget
chat widget that allows you to react to user inputs and file uploads. this example answers (without AI) by simply repeating what the user asks.
```json
{
  "onChat" : "$append(messages, [{'role':'user', 'content': query}, {'role':'assistant', 'content': 'you said: ' & query}])",
  "widget" : "aichat"
}
```
## aichat widget
chat widget that allows you to react to user inputs and file uploads. this example chats with OpenAI's gpt-4o-mini model. The API key must be provided using the 'openai' credentials in the function catalog. The onChat callback passes messages (the chat history) and the new user query to the $chat function. The widget is configured using 'expression'. This expression specifies the initial messages - in this case the system message containing the system prompt
```json
{
  "onChat" : "/* called when a new question is entered in the chat */\n$chat('https://api.openai.com/v1/chat/completions', query, messages, [], {'model':'gpt-4o-mini'}, {'Authorization': 'openai', 'dj-timeout-seconds': 60})",
  "expression" : "/* initial chat state with system prompt in messages */\n{'messages': [{'role': 'system', 'content': 'always answer in a funny way'}]}",
  "widget" : "aichat"
}
```
onChat
```
/* called when a new question is entered in the chat */
$chat('https://api.openai.com/v1/chat/completions', query, messages, [], {'model':'gpt-4o-mini'}, {'Authorization': 'openai', 'dj-timeout-seconds': 60})
```
expression
```
/* initial chat state with system prompt in messages */
{'messages': [{'role': 'system', 'content': 'always answer in a funny way'}]}
```
## analytics widget
displays the results of a SQL query as a chart or table. Allows placing filters on the widget that the user can set when viewing the widget. The database, table, columns, and filter fields determine the SQL query: in this case, it is 'select user, count(ID) from REQUESTS where submitted >= ?' on the database 'sqlite'. Possible filter input widgets are: text, slider, switch, date, select, and selectmultiple.
```json
{
  "columns" : [ {
    "name" : "user",
    "aggregation" : "GROUP_BY"
  }, {
    "name" : "ID",
    "aggregation" : "COUNT"
  } ],
  "filter" : [ {
    "name" : "submitted",
    "operator" : ">=",
    "input" : "date"
  } ],
  "database" : "sqlite",
  "table" : "REQUESTS",
  "chart" : "bar",
  "widget" : "analytics"
}
```
## chart widget
displays a chart based on a query or expression. the first column is used as labels for the X-axis. The second column holds the Y-Values. This example shows the results of the 'group' query from the query catalog run on the northwind database. The chart is a bar chart.
```json
{
  "database" : "northwind",
  "query" : "group",
  "chart" : "bar",
  "widget" : "chart"
}
```
## chart widget
displays a chart based on a query or expression. the first column is used as labels for the X-axis. The second column holds the Y-Values. This example uses a JSONata expression to compute a table using two columns in order to display it as a line chart.
```json
{
  "chart" : "line",
  "expression" : "[{'name':'joe', 'age':11}, {'name':'jane', 'age': 22}]",
  "arguments" : "",
  "widget" : "chart"
}
```
## chart widget
chart with style options (see ChartJS)
```json
{
  "widget" : "chart",
  "query" : "orders-over-time",
  "database" : "northwind",
  "chart" : "bar",
  "style" : {
    "elements.bar.backgroundColor" : "green",
    "plugins.title.text" : "Orders in February",
    "plugins.title.display" : "true",
    "scales.x.type" : "time",
    "scales.x.min" : "1998-02-01",
    "scales.x.max" : "1998-02-28"
  }
}
```
## datagrid widget
Displays the result of a JSONata expression as an editable table. We have callbacks ondelete (context 'id' contains the primary key of the row to be deleted), onchange (updatedRow contains the new value of the row), and oncreate (no context is given - in this example, the code computes a new unique primary key by running select max(id) from table)
```json
{
  "widget" : "datagrid",
  "expression" : "$all('northwind', 'EMPLOYEES')",
  "onchange" : "$update('northwind', 'EMPLOYEES', id, updatedRow)",
  "ondelete" : "$delete('northwind', 'EMPLOYEES', id)",
  "oncreate" : "$create('northwind', 'EMPLOYEES', {'EMPLOYEE_ID': $adHocQuery('northwind', 'select max(EMPLOYEE_ID)+1 as X from EMPLOYEES').X, 'LAST_NAME': ' ', 'FIRST_NAME': ' '})",
  "idColumn" : "EMPLOYEE_ID"
}
```
## diagram widget
displays a boxes and lines diagram where each box represents a database record. The expression 'nodes' computes the boxes. The fields database, table, and id denote the database record (in the example an EMPLOYEE). Optionally, you can specify the box label (data.label) and the box position (position.x and position.y)
```json
{
  "nodes" : "$all('northwind', 'EMPLOYEES').{'database': 'northwind', 'table': 'EMPLOYEES', 'id': EMPLOYEE_ID, 'data': {'label': LAST_NAME}}",
  "edges" : "$all('northwind', 'EMPLOYEES').{'source': EMPLOYEE_ID, 'target': REPORTS_TO}",
  "widget" : "diagram"
}
```
## edit related widget
Allows editing related records of a database record. In this example, the order instance page allows editing all the order details directly. The link between the tables is established via the 'prop' field, which contains the column ID of the foreign key
```json
{
  "ID" : "dj/northwind",
  "name" : "northwind",
  "djClassName" : "org.dashjoin.service.SQLDatabase",
  "url" : "jdbc:h2:mem:northwind",
  "tables" : {
    "ORDERS" : {
      "instanceLayout" : {
        "widget" : "page",
        "children" : [ {
          "columns" : [ "ORDER_ID", "PRODUCT_ID", "UNIT_PRICE", "QUANTITY", "DISCOUNT" ],
          "prop" : "dj/northwind/ORDER_DETAILS/ORDER_ID",
          "widget" : "editRelated"
        } ]
      }
    }
  }
}
```
## graph widget
display a directed labelled graph showing the result of an OpenCypher query
```json
{
  "nodes" : "$adHocQueryGraph('northwind', 'match p=(x:EMPLOYEES)-[:REPORTS_TO]->(y) return p').p",
  "widget" : "graph"
}
```
## graph widget
display a directed labelled graph showing the nodes specified by a JSONata expression. Initially, this graph has no edges
```json
{
  "nodes" : "$all('northwind', 'EMPLOYEES').EMPLOYEE_ID.{  'database': 'northwind',  'table': 'EMPLOYEES',  'pk': [$]}",
  "widget" : "graph"
}
```
## html widget
Allows you to specify HTML. The context expression can be used to inject dynamic content into the HTML via the template ${context}. You can also provide JavaScript functions that can be called from your HTML code (e.g. function 'go')
```json
{
  "title" : "HTML Widgets",
  "html" : "<html lang=\"en\"><style>.card {padding:2em;background:#fff;border-radius:1em;box-shadow:0 4px 12px rgba(0,0,0,.1);text-align:center}</style><body><div class=\"card\"><h1>Hello 👋</h1><p><a href='javascript:go()'>${context.x}</a></p></div></body></html>",
  "context" : "{'x': 'Hello World!'}",
  "script" : "function go(){alert('Hi')}",
  "widget" : "html"
}
```
## icon widget
shows a material icon with an optional link (href) and tooltip text
```json
{
  "icon" : "verified_user",
  "href" : "/page/Home",
  "tooltip" : "tooltip text",
  "widget" : "icon"
}
```
## links widget
on an instance page, shows links to all other related records and the corresponding table page
```json
{
  "widget" : "links"
}
```
## map widget showing a list of addresses
The address can be as simple as 'jp' for Japan, or it can be a specific street address. The widget chooses the appropriate center and zoom level automatically
```json
{
  "display" : "['paris', 'berlin']",
  "widget" : "map"
}
```
## map widget showing a specific address with a popup linking to a record
Using the points field, you can specify a popup which display the data like the display widget. In this case, we show a link to a database record
```json
{
  "display" : "{'points': [{'address': 'London','popup': {'database': 'northwind','table': 'CUSTOMERS','pk': [ 'AROUT' ]}}]}",
  "widget" : "map"
}
```
## markdown widget
shows markdown that can be fed dynamic content via the 'context' JSONata expression
```json
{
  "markdown" : "# Hello ${user} ${context}",
  "context" : "42",
  "widget" : "markdown"
}
```
## mdxeditor widget
a WYSIWYG editor that can be extended with custom menus. 'properties' defines the actions, 'expression' defines the menu structure
```json
{
  "markdown" : "# Hello ${user} ${context}",
  "context" : "42",
  "properties" : {
    "alert" : "$alert('selection: ' & selection & '. markdown: ' & markdown)",
    "log" : "$log($)",
    "sleep" : "($progress({'message': 'working'});$sleep(1000))"
  },
  "expression" : "{'menu': [{'type': 'select','title': 'tooltip','label': 'longer operation','value': 'sleep','items': [{}]},{'type': 'select','title': 'tooltip','label': 'Label','items': [{'label': 'alert popup','value': 'alert'},{'label': 'doc to console','value': 'log'}]}]}",
  "widget" : "mdxeditor"
}
```
## table widget
shows the result of a query. this example uses mode auto which chooses a mobile friendly table layout when viewed on small devices
```json
{
  "mode" : "auto",
  "database" : "northwind",
  "query" : "group",
  "widget" : "table"
}
```
## table widget
shows the result of a JSONata expression as a table
```json
{
  "expression" : "[{'x':1}]",
  "widget" : "table"
}
```
## text widget
shows a simple static text
```json
{
  "text" : "Hello World!",
  "widget" : "text"
}
```
## text widget
shows a hyperlink with an icon in front
```json
{
  "href" : "/page/Home",
  "text" : "Homepage",
  "icon" : "home",
  "widget" : "text"
}
```
## tree widget
shows a tree using a recursive query 'orgchart'. The query must have a parameter 'node' and is evaluated every time a node is opened in the tree view
```json
{
  "widget" : "tree",
  "query" : "orgchart",
  "database" : "northwind"
}
```
## tree widget
shows a static tree that is suitable for a side navigation. The tree nodes can provide an icon, text, href, and nested child nodes
```json
{
  "expression" : "[{'data': {'text': 'Menu','icon': 'person'},'children': [{'text': 'Info', 'icon': 'info', 'href': '/page/Info'}, {'text': 'Home', 'icon': 'home', 'href': '/page/Info'}]}]",
  "widget" : "tree"
}
```
## uploadfile widget
Allows uploading files to the upload directory. Note that WebDAV must be turned on using the WEBDEV_ENABLED environment variable
```json
{
  "widget" : "uploadfile"
}
```
