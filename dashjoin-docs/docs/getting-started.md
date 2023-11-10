# Getting Started: 15 Minute Tour

This section will guide you through the various features of the Dashjoin low code development platform. We assume you are in the admin role and have the [demo application](https://github.com/dashjoin/dashjoin-demo) installed. This application bootstraps a sample northwind database which allows us to demonstrate advanced queries.

We will guide you through a scenario where northwind is an internal fictional enterprise resource planning (ERP) system. You a being tasked with developing an application that allows customers to interface with you via a web portal.

## Database Management

To get to the data management page, click on the gear symbol in the toolbar. The table shows the databases that are available to the system so far. You should see the northwind database there.

Since northwind is a non-persistent in memory database, we will create a new database. This can be done with the create widget on the page. Select the following values and press "create":

```bash
type: SQLDatabase
name: sqlite
url:  jdbc:sqlite:your_database.db
```

From the database page, click on the sqlite database you just created. This brings you to the database details page. The connection information section allows you to make changes to the connection. Note that you can also simply click update in order to recollect the database metadata in case the schema was changed using another application.

If you are in the admin role, you can expand the database management section. Using the form, create a table called "REQUESTS". This operation creates the table with two columns: ID is the numeric primary key and name is a string column describing the record.

Go to the table by following the REQUESTS link in the database management table.
On the page you can start entering some test data. Note that the SQL database enforces the uniqueness constraint on the ID column. If you try to create a record with an existing ID, you will get an error message: [SQLITE_CONSTRAINT] Abort due to constraint violation (UNIQUE constraint failed: `REQUESTS.ID`).
By inspecting the created table, the system also automatically picks up the datatypes of the columns and requires you to provide an ID.

Open the column metadata control and add three more columns:

* submitted: date
* customer: string
* user: string

We can enter, edit and delete records via the user interface. Another option is to upload data. To do this, create a file called REQUESTS.csv (note that the file name must match the case sensitive table name):

```csv
ID,name,submitted,customer,user
1,Can you please send me an offer,2021-01-01 10:20,ALFKI,user
2,Delivery arrived,2021-01-01 10:20,ALFKI,user
3,Delivery delayed,2021-01-07 11:32,ALFKI,user
4,Are you out of crackers,2021-01-04 08:21,BLAUS,other
5,Need more crackers,2021-11-06 05:20,,admin
```

To upload this file, navigate back to the sqlite database page, expand the database management control and select "Upload Data". In the dialog, select "choose files" and select the file REQUESTS.csv you just created.
This brings up a table with a preview (the first 10 rows) of the table. Since the requests table already exists, you cannot select a primary key or the column data type. These options are available if the target table does not yet exist in the database.

You have the choice of appending to or replacing the existing data. Choose "replace" in order to avoid further primary key clashes. Since we are permanently deleting existing data, we need to confirm this operation by entering "delete tables". Note that tables is plural since we can also upload multiple tables at once by selecting a Excel spreadsheet or multiple csv files.

Since our application should not change the northwind ERP database, we created the new table in a new database. Nevertheless, there is a logical connection between the two databases, since the customer field in the requests table references the CUSTOMERS table in the northwind database. Dashjoin allows you to express this relationship even tough it links columns and records in different databases.

Navigate to the REQUESTS table and open the column metadata control. In the table, select the customers column and click edit. In the popup, type "CUSTOMER_ID" in the foreign key reference field. The system suggests all known columns that contain this substring. Select "dj/northwind/CUSTOMERS/CUSTOMER_ID" and press ok.
Since we made a change to the database metadata and the application caches this data, we need to clear the browser application cache by reloading the page.

After the reload, you will notice that the customer column in the table now shows a hyperlink to the related record in the customer table. Likewise, if you navigate onto a request or onto a customer, the related records are displayed even though they reside in a different database. In addition, if you start typing in the request creation's customer field, you will notice that the matching northwind customer IDs are showing up.

Navigate to the customer ALFKI (northwind/CUSTOMERS/ALFKI). The list of requests made by this customer shows up as a list of hyperlinks (1, 2, and 3). As a default, Dashjoin uses the primary key value as a link label, however we can customize this. Go to the REQUESTS table page, open the table metadata control and enter `${name}` in the dj-label field. This string is a template syntax where constant strings can be mixed with template variables referencing columns. So a person template could be `${LAST_NAME}, ${FIRST_NAME}`.

Save this change and reload the browser.
The visit the first request. You will notice that the browser window title now displays the new label. Going back to the requests table you will see that any request that was visited, now shows a nice name. Likewise, if you go back to customer ALFKI, the list also shows the readable link labels (assuming they all have been visited).

Go back to the requests table and enter the letter 'a' in the customer field of the create form. You will see the autocomplete options with the customer IDs starting with 'a'.
The customer IDs are five letter strings. This is better than a plain number, but let's also choose a display name for customers.
Again, we can do this by navigating to the customer table (/table/northwind/CUSTOMERS), opening the table metadata control and entering the dj-label `${COMPANY_NAME}`.
Reload the browser and go back to the requests table. If you type 'a' into the customers create field, you will see the list of customer display names that start with 'a'.
Note that the tooltip shows the underlying five letter primary key. This feature is very useful if tables use unreadable keys.

## Restricting Access

Dashjoin makes it very easy to secure data based on user roles. To view the roles known to the system, go to the info page linked in the toolbar.
The top left widget display the following information:

* The name of the current user (should be user name 'admin')
* The roles the current user is in (should be the user role 'admin')
* A link to the role management page

Follow the link to the roles page. On there, you can define new roles and define the home page for users in this role.
In the system there are several places where you will be able to select the roles defined here.
The role IDs you choose depend on the identity management system that is configured.
In the Dashjoin PaaS, this is OpenID. If you are using the open source default installation,
local users and their role associations are defined in the files djusers.properties and djroles.properties:

You already have an admin user. To add a user "authenticated" with password djdjdj that is in the role with the same nave,
edit the files as shown below:

```bash
# djusers.properties
admin=1395a3149fee498061e6c06581a3decf
authenticated=4a699242c282b1180a24df1ff411001f
```

```bash
# djroles.properties
admin=admin
authenticated=authenticated
```

In the next step, use a different browser or an incognito window and login user user with password djdjdj.
Except for the toolbar, the system looks pretty much the same. Navigate to the info page. You will need
to type /page/Info into the browser, since the toolbar icon is not displayed.
Verify that the page shows user in role authenticated.
Click on "system roles" and "admin" and press delete. You will get the error message:
"User does not have the role required to delete table dj-role in database config". The authenticated role
has read access to the config database, but cannot create, delete or update any records.

By default, new databases are only accessible for the admin. We can demonstrate this by searching for the term "cracker". In the admin browser, you get a total of seven results from both the northwind and sqlite databases.

If you perform the same search in the user browser, you only get the five northwind results.

The northwind database grants read only access to the authenticated role. You can check this on the page /config/dj-database/dj%2Fnorthwind.

Now let's grant access to the sqlite database. Go to the page /config/dj-database/dj%2Fsqlite, select the authenticated role for both the read and write roles and save your change. Note that the admin role already has implicit access, therefore it is not listed in the options.

Go back to the user browser and repeat the search. Now you'll get the same result as in the admin window. You can also navigate to a request and make a change since write access has been granted.

## User Layouts

This section explains how we can customize the layouts and how we can display different user interfaces depending on which role the user is in.

For our application, we'd like the users to have a page where they can see their past requests and where they can issue a new request. A request should only consist of the text. The fields ID, submitted and user should be determined by the system.

We start with the admin browser and navigate to the "dashboard pages" via the toolbar. Using the "Create a new page" control, create a new page called "Start". We will use this as the homepage for authenticated users. This can be setup on the authenticated role page (config/dj-role/authenticated). Enter a new property 
with the key "homepage" and the value "/page/Start" to specify the start page as the homepage for users in this role.
We need to logout and back in using the user browser to pick up this setting. Clicking the home icon will now get you to the start page which at this point only shows a single tile with the text "New page".

In order to create this page, we need to use the admin browser. Before we add widgets to this page, we need to create a query that filters the user's requests and that projects the request columns in a suitable way.

This can be done using the query catalog and query editor. Navigate to the query catalog via the toolbar and in the create form, press the editor button. In the popup, select the sqlite database and the requests table. Using the dropdown, you can add the field user to the query. In the filter field, enter "user". Now we just need to hide the user column (select remove column from the column context menu) and drag & drop the name column to the first position. The query should be:

```sql
SELECT
  "REQUESTS"."name", "REQUESTS"."submitted"
FROM
  "REQUESTS"
WHERE
  REQUESTS.user = 'user'
```

Press OK to leave the query editor. Before creating the query, we need to add the ID (requests), type (read), and roles (admin, authenticated). The query needs one more argument, namely the current user. This can be specified by pressing the + symbol and adding the parameter user with type string and example "user". The example is used when editing a parameterized query in the editor. Finally, in the query text field, replace 'user' with `${user}`. This indicates that the query has a dynamic parameter that is inserted into the query before it is run. Now save the query by pressing "create". At a later point, you can always go back and make changes to the query (e.g. add a join or another projection).

Now we navigate to the page start and enter the layout editor by pressing the pen symbol. We can now make changes to the page. The page contains one widget which currently is a text widget displaying a static text. You can delete this widget and instead add a table widget showing our query result. 
After adding the table widget from the left drawer, enter the following widget proerties in the editor:

```text
widget: table
query: requests
database: sqlite
title: My Requests
arguments: {"user": $.user}
```

Press the floppy disk symbol to save the new layout. You should now see a table with one row. Go to the user browser and reload the page. You should see three requests there.

We created a table widget that runs the requests query on the sqlite database. Now the requests query needs an argument called user. Dashjoin uses a JSON object to pass such parameters. Specifically, `$.user` reads the current username from the context. We will leave it at that, please refer to the developer guide for a full documentation of these expressions.

Now we are missing the functionality to submit new data. We can achieve this with the button widget. Enter the edit mode again and add button widget with the following parameters:

```text
widget: button
text: Submit
title: New Requests
```

In the button widget, add an inout widget:

```text
widget: inout
name: name
```


This adds a form and a button to the page. There are three context menus, the top one for the widget, the middle one for the form, and the lower one for the name form element. Edit the last one and select these values:

```text
widget: textarea
title: Your message
CSS styles: width / 400px, height / 200px
```

These settings change the default text field to a larger multi line text area.
Finally, let's edit the button widget again to define what happens when the button is pressed. Enter the following expression in the field "run this when clicked and display the result":

```text
$create(
  "sqlite", 
  "REQUESTS", 
  {
    "ID": $ceil($random()*1000000), 
    "user": $.user, 
    "name": $.form.name, 
    "submitted": $now()
  }
)
```

Let's break down what is happening here. `$create` is a function which creates the record (3rd parameter) in the database (1st parameter) and the table (2nd parameter) specified. Database and table are static strings. The record consists of four dynamic fields:

The ID is computed by taking a random number (between 0 and 1), multiplying it with 1 million and rounding it up. Thus the ID is a random number between 1 and 1 million, providing reasonable protection from duplicate IDs.

The user is computed using the same construct (`$.user`) as for the table widget above.

The name is specified as `$.form.name`. The rationale is the following: The user entries are stored in a JSON object form which hangs under the context $. In this object, we choose the name specified as the button argument.

Finally, the submitted field is the current timestamp computed with `$now()`.

After saving the layout, you can test the functionality. Note that you need to refresh the page after a value is submitted.

## Admin Layout

The application administrator already has the ability to browse and search the data. However, it would be nice to add a chart to the system. To do this we first need to create another query.

Follow the steps as before and project the columns user and name. Next, select the column user and group by this column. The resulting query should be:

```sql
SELECT
  "REQUESTS"."user", COUNT("REQUESTS"."name")
FROM
  "REQUESTS"
GROUP BY
   "REQUESTS"."user"
```

Save the query under the name "requestsPerUser". Next, navigate to the REQUESTS table and enter the layout editor. Add a widget to the page and choose these settings:

```text
widget: chart
title: Requests Per User
query: requestsPerUser
database: sqlite
chart: doughnut
```

Finally, let's assume we'd like a notification when a new request is submitted. We can do this by creating a trigger on the request table. Open the table metadata section and enter the following expression for the field "Trigger to call before a new record is created":

```text
$echo($)
```

This trigger is a simple expression that calls the echo function. Echo takes an object which is written to the system console. In this case the entire context is written. Once you save and submit another request, you should see a line like this in the console:

```text
{database=sqlite, search=null, command=create, table=REQUESTS, object={ID=762613, user=user, name=My test entry, submitted=2020-12-31T15:50:35.755459500Z}}
```

Instead of calling the echo function, we can of course send an email or perform any other kind of action. A common use case is to automatically
set the createdBy and createdOn fields. This can be achieved by setting the after create trigger to:

```text
$update(database, table, object.ID, {"createdBy": $djUser(), "createdOn": $now()})
```

Note that triggers can invoke each other recursively. If this expression would be the update trigger, 
we might end up with an endless recursion resulting in a stack overflow. This can be avoided by performing the 
update only if `$isRecursiveTrigger()` is false.
