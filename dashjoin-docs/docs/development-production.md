# Development / Production

If you use a Dashjoin system in production, we do not recommend making configuration changes
like defining new queries or registering new user roles using the editors on the production
system. Instead, you can setup multiple development systems where these changes are
developed and tested.

## Dashjoin Studio

The most convenient way to setup a development system is by using the Dashjoin Studio container.
This container includes all the required tools and setup.
To start the container, run:

```
docker run -p 3000:3000 -p 8080:8080 -p 8081:8081 -e DJ_ADMIN_PASS=djdjdj dashjoin/studio
```

If you would like to work on an existing app that is located on a Git repository, also specify the DASHJOIN_HOME and
DASHJOIN_APPURL parameters (see section automatic Git checkout below)

```
docker run -p 3000:3000 -p 8080:8080 -p 8081:8081 -e DJ_ADMIN_PASS=djdjdj -e DASHJOIN_HOME=dashjoin-demo -e DASHJOIN_APPURL=https://github.com/dashjoin/dashjoin-demo dashjoin/studio
```

Dashjoin Studio works like the platform container, but offers two additional services.
On port 8081, it allows connecting a browser based integrated development environment to the container.
On port 3000 an additional web service provides access to the user interface that includes
custom widgets you add to the app.

To open Dashjoin Studio, navigate to the general information page and follow the Dashjoin Studio link under "App".
To log in, please use the password you specified for the admin user (djdjdj in our examples).
This opens VS Code in your browser. For more information about VS Code, please refer to
[this documentation](https://code.visualstudio.com/docs).

The project that is open in VS Code represents the app you are writing.
Adding a query in the query catalog, for example, will create a JSON file in
model/dj-query-catalog. If you change the file in VS Code, the change will be
visible immediately in the query catalog as well.

### App Folder Structure

The contains the following folders:

* assets: any media that should be available via http(s)://<app url>/assets. To customize the favicon, for instance, add your favicon to assets/public_html/favicon.ico.
* model
  * dj-config: Contains any system configuration
  * dj-database: All connected databases and any table and record page layouts. If a database JSON file is open in the editor, you can test connecting to it via the "Connect to this database" button at the top of the file
  * dj-function: All functions defined on the functions page. Also offers the ability to "Run this function". Note that passing parameters to the function is not supported. You can use the JSONata notebook for this.
  * dj-query-catalog: All queries in the catalog. Offers the "Run this query" and "Run this query metadata" commands. Note that passing query parameters is not supported. You can use the JSONata notebook for this.
  * dj-role: All roles defined in the system
  * page: All app dashboard pages. Offers the "Open this page in the browser" button.
  * tenantusers: All tenant users
  * widget: Custom toolbar and sidebar are located here
* upload: this folder can contain files that can be accessed from JSONata functions like openJson("file:upload/...")

### Source Control

If you would like to commit and publish your changes to the production system, you can
switch to the source control tab on the left. The usual workflow is to:

* review your changes in the diff editor
* stage your change by adding the file via the + icon
* entering a message describing your change and committing the change
* publishing your change to the remote Git repository

For more details, please refer to [this guide](https://code.visualstudio.com/docs/sourcecontrol/overview).

## Installing the App on the Users' Operating Systems

Dashjoin Apps can be accessed via the web on desktop and mobile devices. In some situations, it may be useful
to install an App on the users' operating systems (Windows, MacOS, iOS, Android, etc.). This might enable some
offline capabilities, preserve logon sessions, or simplify quick access using the App icon on the homescreen.

Dashjoin supports this functionality out of the box by serving this default manifest:

```
{
    "name": "Dashjoin",
    "short_name": "Dashjoin",
    "start_url": "/",
    "display": "standalone",
    "display_override": ["fullscreen", "minimal-ui"],
    "id": "dashjoin",
    "background_color": "#fff",
    "theme_color": "#1976d2",
    "description": "Rapidly Build Apps Driven by Data and AI",
    "dashjoin": {
        "show_install_dialog": false
    },
    "icons": [
        {
            "src": "djlogo192.webp",
            "type": "image/webp",
            "sizes": "192x192",
            "purpose": "any"
        },
        {
            "src": "djlogo512.png",
            "type": "image/png",
            "sizes": "512x512",
            "purpose": "any"
        }
    ],
	"screenshots": [
		{
			"src": "djscreenshot.avif",
			"sizes": "1200x627",
			"type": "image/avif"
		},
		{
			"src": "djscreenshot.avif",
			"sizes": "1200x627",
			"type": "image/avif",
            "form_factor": "wide"
		},
		{
			"src": "djscreenshot.avif",
			"sizes": "1200x627",
			"type": "image/avif",
            "form_factor": "narrow"
		}
	]
}
```

The installation process depends on your operating system and browser. The Chrome browser for instance,
displays an "Install" icon to the right of the URL textfield. Safari requires you to click the "share"
button and select "Add to Dock". You can customize the manifest by changing the default values above and
saving the file in assets/manifest/manifest.json. If you set the show_install_dialog flag to true,
the app will display installation instructions depending on your device and browser.

## Developing a Custom Widget

One of the most powerful features of Dashjoin Studio is the ability to write your own widgets.
Let's assume we have a database containing chemical molecules. One of the columns contains the [SMILES string](https://en.wikipedia.org/wiki/Simplified_molecular-input_line-entry_system). We're going to write a widget, that draws a 2D representation of the molecule
based off this information.

### Adding a 3rd Party Library

The first step is to find a 3rd party library that is suitable for the task. The Dashjoin UI is written
in [React](https://react.dev/), therefore, keep this in mind when selecting a library.
For this example, we'll use [smilesDrawer](https://github.com/reymond-group/smilesDrawer).

In VS Code, open a terminal (on the top left, click the menu, then terminal, and finally new terminal. When prompted where to open the terminal, please select "Dashjoin Platform UI") and enter:

```
yarn add smiles-drawer
```

Yarn is a JavaScript package manager, that will retrieve the latest version of the library and all other required components. Depending on your internet connection speed, this command might take a while to complete.
Now we can start the development webserver that will run on port 3000:

```
yarn rsbuild dev
```

### Adding a new Widget

Adding a new widget requires three changes: First, create the file Smiles.tsx in src/widgets:

```
import { Icon } from "@mui/material"
import { Widget } from "../model/widget"
import { text, title } from "../api/Const"
import { useEffect, useRef } from "react"
const SmilesDrawer = require('smiles-drawer')

export const Smiles = ({ widget }: { widget: Widget }) => {

    const imgRef = useRef<HTMLImageElement>(null);
    const drawer = new SmilesDrawer.SmiDrawer();

    useEffect(() => {
        drawer.draw(widget.text ? widget.text : 'C', imgRef.current, 'light');
    });

    return (
        <div>
            <img ref={imgRef} width={300}></img>
        </div>
    );
}

export const config = {
    id: 'smiles',
    title: 'Smiles',
    description: 'Renders a SMILES string as a 3D molecule',
    version: 1,
    icon: <Icon>science</Icon>,
    controls: {
        type: 'autoform',
        schema: {
            properties: {
                title: title,
                text: text
            }
        }
    }
}
```

This code contains two blocks. The first block is the actual widget. It gets the parameter "widget",
which is a JSON structure to configure the widget. The rest of the code is taken from the 
library's documentation. The key point is in line 13, where widget.text is passed as the parameter
to the draw function. This parameter contains the SMILES string passed to the widget.

The second part defines how the widget edit dialog looks like. It contains an icon, some description, and
a list of controls. In this case, we allow editing the widget title (this is a property shared by all
widgets) and the text to contain the SMILES string. You could add other properties. For instance,
the width of the generated image is fixed at 300 pixels. This could be replaced with a widget parameter.

The demo application contains a [version of this widget](https://github.com/dashjoin/dashjoin-demo/blob/main/custom-widget/Smiles.tsx) that computes the SMILES string via JSONata. It uses the "useExpression" React hook. This allows reading SMILES data from a DB or a RESTful web service.

Now edit the file CustomWidgets.tsx in src and add the following lines:

```
import { config } from "./widgets/Smiles";
import { Smiles } from "./widgets/Smiles";

export const customWidgets = [
    {
        widget: Smiles,
        config: config
    }
]
```

Custom widgets is an array. Therefore, you can add other custom widgets as well.
Now we're all set. We can create a test page and add the widget with the following parameters in order to [see the molecule in all its glory](https://en.wikipedia.org/wiki/Simplified_molecular-input_line-entry_system#/media/File:Beta-D-Glucose.svg):

```json
{
    "widget": "smiles",
    "title": "Glucose",
    "text": "OC[C@@H](O1)[C@@H](O)[C@H](O)[C@@H](O)[C@H](O)1"
}
```

An example is available in the [demo app](https://demo.my.dashjoin.com/#/page/Smiles) as well.

### Packaging the Widget and Deploying it to Production

Once you are done with your widget development, you can start the build process via the terminal
and deploy the result to your app:
```
yarn build
yarn deploy
```

This command will create a folder with the compiled user interface in the folder
of your app. You can commit these assets along with the query catalog and the other files in your app.
You can view an example in the [demo app](https://github.com/dashjoin/dashjoin-demo/tree/main/assets/public_html).
If the app is deployed onto a production system, the user interface containing the new widget
will be active.

Note that this process must be repeated when the platform is updated to a newer version.

### Making Changes to the Platform

Dashjoin Studio gives you full access to the entire user interface. 
Therefore, you can not only add widgets, but also make changes to the core UI. For instance,
you can make changes to the login screen or the legal cookie and data privacy disclaimer
presented to new users.

Any of these changes are picked up by the build and deploy process described in the previous section.
Please note however, that changes to the core platform must be re-applied manually
once a new version of Dashjoin is released.

## Multi Line JSON

Markdown, queries, and JSONata expressions can be hard to read if they are stored in JSON files.
You can externalize strings in separate text files which are linked
from the JSON data as follows:

test.json:

```
{
  "ID": "test",
  "query-pointer": "0.sql"
}
```

test.0.sql:

```
select * from test
  where id=4
```

These two files are equivalent to:

test.json:

```
{
  "ID": "test",
  "query": "select * from test\n  where id=4"
}
```

You can use this mechanism anytime when you're editing the files manually.
When making changes via the platform, by default, only the query strings of
the query catalog are externalized.

This is controlled by the "externalize-config-strings" setting.
If you'd like to also externalize widget markdown for instance, simply add "page: markdown" to "externalize-config-strings".
This specifies the table and the (possibly nested) key containing the string to be externalized.


On the production system, there are three ways of deploying an application:

## Upload to the Configuration Database

You can upload an entire model folder to the config DB. On the database page, select "Configuration Database".
Open the "Database Management" tab and select "Upload". Select the model folder there and either append or replace
the contents of the config database.

## Automatic Git Checkout

You can specify the DASHJOIN_APPURL environment variable and have it point to your app repository.
Upon startup, the system will perform a git clone if the model folder is empty or a git pull if the model has content already.
Note that you can specify credentials via the URL (http://user:password@domain.com/).
Please refer to the [demo application](https://github.com/dashjoin/dashjoin-demo) for an example of how to run
Dashjoin with the demo application installed.
If the git operation fails (e.g. due to incorrect credentials or illegal filenames), the platform will log the error and resume the startup process.

### Private GitHub Repositories

If you would like to launch an App that is stored in a private GitHub repository, you can use
a [personal access token](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/managing-your-personal-access-tokens). You can assign the right to clone the repository to the token and add it to the DASHJOIN_APPURL as follows:

```
https://user:token@github.com/org/project
```

## Manual App Installation

Last but not least, you can also copy the app into the Dashoin working directory using other means
before starting the platform. 
If you are using containers, you can mount the model folder under /deployments/model.

## Specifying Development Resources

Resources like databases and REST endpoints are critical resources when working with Dashjoin.
Therefore, it is quite common to use different sets of resources for development and production.
As described in the section on automatic Git checkout above, the production credentials are usually
checked into the code repository. During development, you can use [environment](installation.md/#environment)
variables to specify alternative values for url, username, hostname, port, database, and password to be used for functions and databases as follows:

* dashjoin.database.NAME OF THE DATABSE.url: URL to use to connect to the database (overrides the url field in the DB's json file)
* dashjoin.function.NAME OF THE FUNCTION.url: URL to use to connect to the REST service (overrides the url field in the function's json file)

To change the username / password, simply replace url with username / password in the examples above.
Note that the development passwords are provided in plain text.

## Unit Testing

Unit tests are an important asset to ensure the quality of your app. Dashjoin leverages the [JUnit framework](https://junit.org/) and allows you to perform the following default tests to make sure that

* all JSON files can be parsed
* layout uses legal widget names
* all JSONata expressions are syntactically correct

In addition to these syntactical checks, it is possible to run JSONata expressions and provide desired outputs. 

To setup unit tests in your app, follow these steps:

* Open Dashjoin Studio
* Copy this [maven project file](https://github.com/dashjoin/dashjoin-demo/blob/main/pom.xml) to your app's root directory
* Copy this [JUnit test file](https://github.com/dashjoin/dashjoin-demo/blob/main/src/test/java/org/dashjoin/app/AppTest.java) to "src/test/java/org/dashjoin/app"
* If you would like to test a JSONata expression, create a [JSON test file](https://github.com/dashjoin/dashjoin-demo/blob/main/test.json) that describes the file containing the expression, where the expression is located, which test cases should be run, and which outputs are to be expected. The file is structured as follows:

```
{
    "test": {
        "file": path to the file containing the expression to be checked
        "expression": JSONata expression that selects the expression to be checked
    },
    "basedata": optional common test data for the test cases
    "cases": {
        "name": {
            "data": test data (will be merged with the base data)
            "expected": expected JSONata output
        }
        ...
    }
}
```

To run the unit test:

* Via the VS Code extensions tab, install the [Debugger for Java](https://open-vsx.org/extension/vscjava/vscode-java-debug) and the [Test Runner for Java](https://open-vsx.org/extension/vscjava/vscode-java-test) plugins
* Press run in the new JUnit tab to run all the tests

## Start Expression

The platform allows defining an expression that is run whenever the platform starts. This expression can be defined via the
on-startup setting in the system configuration. A typical use case is setting up the database and maybe running an ETL.
Let's assume a "create table if not exists MyTable(...)" is located in the query catalog under the name "create" and
an ETL job is defined as "etl", the following expression would create the table and run the ETL job:

```
(
  $query('postgres', 'create');
  $call('etl');
)
```

The expression is run with admin credentials. Any errors are ignored and the result is logged to the console.

## App Customization

Assume you have two instances of an app running, but you would like
to customize the theme color. One approach would be to fork the repository, change the setting there and
start the second instance using the new app URL. This works, but it is a bit cumbersome to handle subsequent
changes in both repositories. For small changes, you can use the startup expression. The expression is called
with the value of the DASHJOIN_ONSTART environment variable string: {"onStart": VALUE}.
Let's assume, every instance gets the value of its theme color. We can use the start expression to set the color:

```
$update('config', 'dj-config', 'theme', {'map': {'pallette.primary.main': onStart}})
```

If your app instances need to have different login configs, favicons, or other web assets such as logos, you
can use the DASHJOIN_WEBROOT environment variable to serve different content.
An might have the following folder structure:

```
assets
  logincfg.json
  public_html
    assets
      logo.png
```

If you need to have two instances of the app with different logo and login config, you can move logincfg.json into
the public_html/assets folder, create a copy of that folder for each app instance:

```
assets
  public_html
    assets
      logincfg.json
      logo.png
  public_html2
    assets
      logincfg.json
      logo.png
```

The second instance can be lauched with this environment variable:

```
DASHJOIN_WEBROOT=assets/public_html2
```

If DASHJOIN_HOME is set, please prepend it as follows:

```
DASHJOIN_HOME=MYHOME
DASHJOIN_WEBROOT=MYHOME/assets/public_html2
```
