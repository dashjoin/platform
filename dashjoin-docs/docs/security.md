# Security and Access Control

## Security

We strongly advise to:

* Consider using read-only database credentials when registering a database with data that is managed by another application
* When adding a new database, make sure the access control settings are setup correctly
* Restrict access to the system in case you store confidential data in any of the registered databases
* All credentials that are entered into the system are encrypted using strong SHA-256 encryption. The master key resides in the file model/.secrets.id on the web server. Keep this file secured
* Add a OpenID provider in order to authenticate organization users

In order to register new databases, the user must be in the "admin" role.

## Access Control

Several other sections already touched on access control and how certain functionality is only allowed for certain roles. This section explains how roles are defined and how users are assigned to be in a role.

### Info Page

The info page shows various system data. At the top of the page you find the username of the current user as well as the roles he or she is in. From there you also find a link to the roles dashboard and, on the PaaS offering, a link to the tenant users.

### Roles Dashboard

The roles dashboard allows the administrator to define system roles. The role names should correspond on the roles defined in the identity management system (IDM) you are using.
Let's assume that the IDM defines a user to be in the role "consulting".
If this role "consulting" is defined in Dashjoin and the user logs in, 
the user also has this role when using Dashjoin.
If you are using the Dashjoin Cloud, you can choose arbitrary role names and assign users to these roles using the tenant user dashboard.

### Tenant User Dashboard

The previous section explained how IDM roles are carried over to Dashjoin.
However, there might be situations, where an application requires a role which is not yet
defined in the IDM. For instance, only some of the consultants in the IDM group "consultant"
should be granted access to the application.
One way would be to create a new Dashjoin role and a new corresponding IDM role.
In some organizations, this might not be feasible though, since the IDM is usually
managed by a different entity within the organization.

The tenant user dashboard can be used in these cases. It allows the administrator
of the Dashjoin instance to explicitly assign roles to IDM users without having to
explicitly create and assign the role in the IDM.

This mechanism can also be used to request access to a Dashjoin application.
Let's assume a user is registered in the IDM, but has no access to Dashjoin.
If he logs in, he'll get a "permission denied" error, however, the user will show up
on this dashboard, with the "active" flag set to false.
The administrator can then activate the user and assign the proper roles.

If all users of a domain are to be added, the tenant user id can be set to "@domain.org".
In this case any user id ending with this domain suffix gets the roles defined in this record.

Finally, you can use the tenant id "@EVERYONE" to define roles for all users that are defined in the IDM, regardless of the roles they are assigned in the IDM.

### Assigning Roles

Roles can be assigned to the following elements:

* Container widgets and the top level page widget: In the layout editor, you can specify that a container or page is only shown if the user is in a given role. Note that this feature does not replace the backend checks listed below. Containers are simply hidden. The page shows an error message on the bottom.
* Toolbar elements: You can customize which elements are visible on the toolbar by editing the toolbar widget at /#/config/widget/dj-toolbar. Roles can be assigned to icons, the sidenav toggle, the page edit button, and the search box
* Functions: Functions can be restricted to be executable only to users in certain roles
* Queries: Queries can be restricted to be runnable only to users in certain roles
* Tables
  * readRoles: Users in one of these roles get to read rows of this table
  * writeRoles: Users in one of these roles get to update, delete, and create rows of this table
* Databases: defines a database wide default for any table that does not define read or write roles

### Row-Level Security

Row-Level Security allows restricting access to certain table rows depending on the value of a column. A common use case are portals where different tenants should only see their data. Consider the following example where the owner column is the email of the user the item belongs to:

| id | name   | owner             |
|----|--------|-------------------|
| 1  | item 1 | joe@example.com   |
| 2  | item 2 | mike@localhost    |

Using the table metadata editor, we can define "owner" to be the "tenantColumn", i.e. the column that defines the row-level security.
If user mike logs in, he will only see item 2 on the item table.

Row-level security can also be defined users being in a certain role. Consider this customer table:

| id | name       | region |
|----|------------|--------|
| 1  | customer 1 | south  |
| 2  | customer 2 | north  |

Now, let's assume role "sales-south" should get access to rows where region is "south". This can be accomplished by defining region to be the tenantColumn in addition to defining the following role mapping:

* sales-south: south
* sales-north: north

Row-level security is applied to the entire dashjoin platform automatically, except for queries.
If a query involves a table that has row-level security defined, the query must have a parameter that is set accordingly. We show two examples for the tables defined above. A query on the item table might look as follows:

* query: select * from item where owner = ${tenant}
* query parameter expression: { "tenant": user }

The role mappings on the customer are translated to a JSONata expression:

* query: select * from customer where region = ${tenant}
* query parameter expression: { "tenant": "sales-south" in roles ? "south" : ("sales-north" in roles ? "north") }

### User Dashboard

As the platform admin, the user dashboard shows all the users that have already logged into the system, along with
the time of the first and last login.
This information can be used by app developers to implement billing mechanisms for their users respectively.
