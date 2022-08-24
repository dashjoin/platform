# Supported Databases

## Relational Databases

We support all of the SQL databases out of the [top 10 database engines](https://db-engines.com/en/ranking):

| Database  | Driver class  | Driver version | Status |
|---|---|---|---|
Oracle | oracle.jdbc.OracleDriver  | 19.6.0.0.0 | beta |
MySQL | com.mysql.cj.jdbc.Driver | 8.0.20 | beta
SQL Server | net.sourceforge.jtds.jdbc.Driver | 1.3.1 | beta
PostgreSQL | org.postgresql.Driver | 42.2.12
SQLite | org.sqlite.JDBC  | 3.31.1
DB2 | com.ibm.db2.jcc.DB2Driver | 1.4.0 | beta
MS Access | net.ucanaccess.jdbc.UcanaccessDriver| 4.0.1 | beta
MariaDB | org.mariadb.jdbc.Driver | 2.6.0 | beta
H2 | org.h2.Driver | 1.4.196
Amazon RDS Aurora PostgreSQL | org.postgresql.Driver | 42.2.12 | beta
Amazon RDS Aurora MySQL | com.mysql.cj.jdbc.Driver | 8.0.20 | beta
Sybase (SAP) ASE 16.2 | net.sourceforge.jtds.jdbc.Driver | 1.3.1 | beta
SAP HANA | com.sap.db.jdbc.Driver | 2.5.49 | beta

## Document Databases

| Database  | Driver class  | Driver version | Status |
|---|---|---|---|
Firestore | google-cloud-firestore  | 2.1.0 | Available in Dashjoin PaaS |
MongoDB | mongodb-driver-sync  | 4.1.0 | beta |

## Graph Databases

| Database  | Driver class  | Driver version | Status |
|---|---|---|---|
RDF4J | rdf4j-runtime  | 3.1.2 | beta / must be deployed as a remote database - see the [module page](https://github.com/dashjoin/platform/tree/master/dashjoin-rdf4j) for setup instructions |
ArangoDB | arangodb-java-driver  | 6.14.0 | beta |

[Click here](https://www.youtube.com/watch?v=_itCZjvw9D8) for a demo video.
