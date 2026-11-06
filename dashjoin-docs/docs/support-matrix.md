# Supported Databases

## Relational Databases

We support all of the SQL databases out of the [top 10 database engines](https://db-engines.com/en/ranking):

| Database  | Driver class  | Driver version | Status | Connection URL | Notes
|---|---|---|---|---|---|
Oracle | oracle.jdbc.OracleDriver | 19.3.0.0 | beta | jdbc:oracle:thin:@...:1521/ORCL |
MySQL | 	org.mariadb.jdbc.Driver | 3.5.9 | beta | jdbc:mariadb://...:3306/db | ANSI_QUOTES and allowPublicKeyRetrieval set by default
SQL Server | com.microsoft.sqlserver.jdbc.SQLServerDriver | 13.2.1 | beta | jdbc:sqlserver://...:1433;databaseName=db; |
PostgreSQL | org.postgresql.Driver | 42.7.13 | | jdbc:postgresql://...:5432/db |
SQLite | org.sqlite.JDBC  | 3.42.0.1 | | jdbc:sqlite:my.db |
DB2 | com.ibm.db2.jcc.DB2Driver | 12.1.0.0 | beta | jdbc:db2://...:50000/db |
MS Access | net.ucanaccess.jdbc.UcanaccessDriver| 5.0.1 | beta | jdbc:ucanaccess://db.accdb |
MariaDB | org.mariadb.jdbc.Driver | 3.5.9 | beta | jdbc:mariadb://...:3306/db | ANSI_QUOTES and allowPublicKeyRetrieval set by default
H2 | org.h2.Driver | 2.4.240 | | jdbc:h2:tcp://.../db |
Amazon RDS Aurora PostgreSQL | org.postgresql.Driver | 42.7.13 | beta | jdbc:postgresql://...:5432/db |
Amazon RDS Aurora MySQL | 	org.mariadb.jdbc.Driver | 3.5.9 | beta | jdbc:mariadb://...:3306/db | ANSI_QUOTES and allowPublicKeyRetrieval set by default

## Document Databases

| Database  | Driver class  | Driver version | Status |
|---|---|---|---|
Firestore | google-cloud-firestore  | 3.37.0 | Available in Dashjoin PaaS |
MongoDB | mongodb-driver-sync  | 5.6.4 | beta |

## Graph Databases

| Database  | Driver class  | Driver version | Status |
|---|---|---|---|
RDF4J | rdf4j-runtime  | 4.2.2 | beta / must be deployed as a remote database - see the [module page](https://github.com/dashjoin/platform/tree/master/dashjoin-rdf4j) for setup instructions |
ArangoDB | arangodb-java-driver  | 7.20.0 | beta |

[Click here](https://www.youtube.com/watch?v=_itCZjvw9D8) for a demo video.

## Vector Databases

| Database  | Driver class  | Driver version | Status |
|---|---|---|---|
pgvector | org.postgresql.Driver | 42.7.13 | Use via the KnowledgeBase database |
