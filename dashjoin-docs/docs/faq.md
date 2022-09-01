# FAQ

* **How can I edit a fullscreen page?** Usually, you toggle the edit mode via the widget in the toolbar. If you're creating a page without the toolbar, you have two options: 1) If you're working with docker or the installer, you can edit the dashboard page in the respective file on the file system. 2) You can navigate to any "normal" page, enter edit mode, navigate to the fullscreen page, make changes via the edit context menu, navigate back to the "normal" page and save there.

* **The [Dashjoin Demo Application](https://github.com/dashjoin/dashjoin-demo) contains some interesting examples. How can I apply them to my application?** You can either locate your application on the file system and copy an example page there or you can look at the page in order to see which settings to add in the layout editor dialogs (e.g. a JSONata expression).

* **I have an object with special characters in the field names (e.g. a SQL query result). How can I access this field in JSONata?** In [JSONata](https://docs.jsonata.org/simple#navigating-json-objects), field names can be escaped using back-ticks (`). Click [here](https://demo.my.dashjoin.com/#/page/html) for a live example.

* **I have an object with special characters in the field names (e.g. a SQL query result). How can I access this field in the HTML widget?** The HTML widget uses [EJS](https://ejs.co/), which allows embedding JavaScript templates in HTML. In Javascript, you can access non-alphanummeric field names as follows: `object["field.name"]`. Click [here](https://demo.my.dashjoin.com/#/page/html) for a live example.

* **How can I customize the forms in the edit, button and variable widgets?** These widgets use the JSON Schema Form component. This [online playground]() lets you experiment with the various features. This component comes with a WYSIWYG editor which is available in edit mode by clicking the three vertical dot icon. Note that not all features of the component are exposed in the WYSIWYG editor. You can leverage the advanced features by editing the underlying JSON directly. The demo application shows two examples. The "createSchema" of the [customer page](https://github.com/dashjoin/dashjoin-demo/blob/main/model/dj-database/dj%252Fnorthwind.json) section shows the form of the email button, which displays the email body input field with a larger text box. The city instance page shows a similar layout for the edit widget. The [variable example](https://github.com/dashjoin/dashjoin-demo/blob/main/model/page/variable.json) shows how a select widget with display names and values can be rendered.

* **Are SQL stored procedures supported?** Yes, simply use 'exec proc' or 'call proc(par)' as the query, depending on the SQL dialect used by your DB. In case a stored procedure has multiple result tables, the $query function returns them by wrapping them in a top level object.

* **How can I access a SQL Server stored procedure output variable?** This can be done on the query level as follows:

```sql
DECLARE @res INT;
exec dbo.sp @res output;
select @res;
```

* **Why am I getting the error: "User does not have the role required to read table page in database config" after logging in?** When the UI renders a page, it needs to get the page layout from the backend. Like with any other call, the user's credentials are checked. This error indicates, that the user is known to the system, but gets assigned insufficient roles to access this information in the config DB. To fix this, you can either assign the user the correct role in the IDM or you can provide read access to the config DB to the user's role (this is done on the System Configuration page).
