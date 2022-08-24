# Security

We strongly advise to:

* Consider using read-only database credentials when registering a database with data that is managed by another application
* When adding a new database, make sure the access control settings are setup correctly
* Restrict access to the system in case you store confidential data in any of the registered databases
* All credentials that are entered into the system are encrypted using strong SHA-256 encryption. The master key resides in the file model/.secrets.id on the web server. Keep this file secured
* Add a OpenID provider in order to authenticate organization users

In order to register new databases, the user must be in the "admin" role.
