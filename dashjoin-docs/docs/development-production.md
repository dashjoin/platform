# Development / Production

If you use a Dashjoin system in production, we do not recommend making configuration changes
like defining new queries or registering new user roles using the editors on the production
system. Instead, you can setup multiple development systems where these changes are
developed and tested.

You can put the model folder under version control by following these steps:

* create a repository, for instance on GitHub
* install Dashjoin locally
* clone the repository into your Dashjoin user home directory:

```bash
USER_HOME> git clone https://github.com/ORG/PROJECT.git .dashjoin
```

* start Dashjoin and start developing
* pull changes from others
* review and commit changes:

```bash
USER_HOME/.dashjoin> git status
```

Please refer to the [demo application](https://github.com/dashjoin/dashjoin-demo) to find out about the recommended app folder structure.

## Multi Line JSON

Markdown, queries, and JSONata expressions can be hard to read if they are stored in JSON files.
You can use multi-line JSON strings to make these more readable. Rather than escaping the newline character within the string, 
simply add a newline in the file. Note that quotes still need to be escaped:

```
"regular": "  line 1\n  line \"2\"",
// comments are allowed too
"multi": "
  line 1
  line \"2\"
"
```

Important: if you edit the file via the query, expression, or layout editors, the file will be reverted back to the default single line
representation.

On the production system, there are three ways of deploying an application:

## Upload to the Configuration Database

You can upload an entire model folder to the config DB. On the database page, select "Configuration Database".
Open the "Database Management" tab and select "Upload". Select the model folder there and either append or replace
the contents of the config database.

## Automatic GIT Checkout

You can specify the DASHJOIN_APPURL environment variable and have it point to your app repository.
Upon startup, the system will perform a git clone if the model folder is empty or a git pull if the model has content already.
Note that you can specify credentials via the URL (http://user:password@domain.com/).
Please refer to the [demo application](https://github.com/dashjoin/dashjoin-demo) for an example of how to run
Dashjoin with the demo application installed.

## Manual App Installation

Last but not least, you can also copy the app into the Dashoin working directory using other means
before starting the platform. 
If you are using containers, you can mount the model folder under /deployments/model.