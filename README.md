# ConnectPhoenix

It is the example of how to connect and run simple queries against Phoenix service in a Kerberized environment.<br> Phoenix is a SQL engine on the top of Apache HBase.<br>
https://phoenix.apache.org/server.html<br>
The solution can be executed as Intellij IDEA project or as a standalone test.<br>
The test is using full JDBC client or thin client. In order to connect using thin client, Phoenix Query Server should be installed and running.

# Files description

* /out/artifacts/ConnectPhoenix/ConnectPhoenix.jar Java jar with the test
* src/main/java/Main.java Source code
* sh Directory to run the test as standalone
  * sh/run.sh Shell script to execute
  * sh/template Directory with resource files to configure
    * sh/template/env.rc Evironment variables used by run.sh script
    * sh/template/param.properties Phoenix JDBC URL parameters
    * sh/template/log4j.properties 
    
# param.properties
https://community.cloudera.com/t5/Community-Articles/Phoenix-JDBC-Client-Setup/ta-p/244284

Parameter | Description | Sample value
------------ | ------------- | -------
url | Phoenix JDBC URL (without Kerberos) | jdbc:phoenix:a1.fyre.ibm.com,aa1.fyre.ibm.com,hurds1.fyre.ibm.com:2181:/hbase-secure
driver | Driver name | org.apache.phoenix.jdbc.PhoenixDriver or org.apache.phoenix.queryserver.client.Driver
kerberos | Kerberos part, format (principal):(path to keytab) | techuser@FYRE.NET:/home/sb/techuser.keytab
query | Optional, a sample query to run | SELECT COUNT(\*) FROM SYSTEM.CATALOG
update | Optional, a sample update statement | create table test (mykey integer not null primary key, mycolumn varchar)

# Test
The program is connecting to Phoenix server, outputs the tables found (select DISTINCT(\"TABLE_NAME\") from SYSTEM.CATALOG") and, optionally, executes *query* and *update* statements found in *param.properties* file.<br>
The program is accepting a single parameter, path to *param.properties* file.<br>
Troubleshooting: change to logging level to *DEBUG* in *log4j.properties* file.<br>
In Kerberized environment, obtain valid Kerberos keytab file.<br>
If the test passes, there should be outputted the list of Phoenix tables.
```
CATALOG
FUNCTION
LOG
SEQUENCE
STATS
TEST
```

# Run as Intellij IDEA project
## Clone the project
Clone to project into Intellij IDEA.
## Copy hadoop and hbase configuration data
This step is necessary only while using full JDBC client.<br>
From target Hadoop (HDP) environment, copy */etc/hadoop/conf* and */etc/hbase/conf* directories. Create a directory structure.
* sh/hadoop/conf
* sh/hbase/conf
## Copy templates and modify
Copy *sh/template/param.properties* and *sh/template/log4j.properties* to *sh* directory. Modify *param.properties* file according to your environment.<br>
* For full JDBC client, set *param=driver=org.apache.phoenix.jdbc.PhoenixDriver*<br>
* For thin client, set *driver=org.apache.phoenix.queryserver.client.Driver*<br>
## Prepare the launch configuration
* VM options: -Dlog4j.configuration=file:sh/log4j.properties 
* Program arguments: sh/param.properties

Add *sh/hadoop/conf* and *sh/hbase/conf* to Java ClassPath (only JDBC client).
* Project Structure
* Modules
* Dependencies (right panel)
* Add both directories as 
  * Add "Jars or directories"
  * Select Path
  * Select as "Classes"
  
## pom.xml
The *pom.xml* contains dependency for both client, JDBC and thin.<br>
JDBC client
```XML
  <dependency>
            <groupId>org.apache.phoenix</groupId>
            <artifactId>phoenix-client</artifactId>
            <version>5.0.0.3.1.0.6-1</version>
  </dependency>
```
Thin client
```XML
      <dependency>
            <groupId>org.apache.phoenix</groupId>
            <artifactId>phoenix-server-client</artifactId>
            <version>4.7.0-HBase-1.1</version>
      </dependency>
```

# Run the test as a standalone application
## Clone the repository
> git clone https://github.com/stanislawbartkowski/ConnectPhoenix.git
## Copy and modify the templates
> cd ConnectPhoenix/sh<br>
> cp templates/* .<br>

Modify *param.properties* and *env.rc* configuration files.
JDBC client<br>
*param.properties : driver=org.apache.phoenix.jdbc.PhoenixDriver
*env.rc : LIB=$LIB:/usr/hdp/current/phoenix-client/*
<br>
Think client<br>
*param.properties : driver=org.apache.phoenix.queryserver.client.Driver
*env.rc : LIB=/usr/hdp/current/phoenix-server/phoenix-5.0.0.3.1.0.0-78-thin-client.jar
<br>
## Run the test
> ./run.sh
----------
# How to get access to Phoenix JDBC SQL engine
## Command line
That's very simple, just run *phoenix-sqlline* command. It's should be already linked in */usr/bin* directory. In Kerberos environment, obtain Keberos ticked beforehand.
```
Setting property: [incremental, false]
Setting property: [isolation, TRANSACTION_READ_COMMITTED]
issuing: !connect jdbc:phoenix: none none org.apache.phoenix.jdbc.PhoenixDriver
...........
Connected to: Phoenix (version 5.0)
Driver: PhoenixEmbeddedDriver (version 5.0)
Autocommit status: true
Transaction isolation: TRANSACTION_READ_COMMITTED
Building list of tables and columns for tab-completion (set fastconnect to true to skip)...
133/133 (100%) Done
Done
sqlline version 1.2.0
0: jdbc:phoenix:> !tables
+------------+--------------+-------------+---------------+----------+------------+----------------------------+-----------------+--------------+-------------+
| TABLE_CAT  | TABLE_SCHEM  | TABLE_NAME  |  TABLE_TYPE   | REMARKS  | TYPE_NAME  | SELF_REFERENCING_COL_NAME  | REF_GENERATION  | INDEX_STATE  | IMMUTABLE_R |
+------------+--------------+-------------+---------------+----------+------------+----------------------------+-----------------+--------------+-------------+
|            | SYSTEM       | CATALOG     | SYSTEM TABLE  |          |            |                            |                 |              | false       |
|            | SYSTEM       | FUNCTION    | SYSTEM TABLE  |          |            |                            |                 |              | false       |
|            | SYSTEM       | LOG         | SYSTEM TABLE  |          |            |                            |                 |              | true        |
|            | SYSTEM       | SEQUENCE    | SYSTEM TABLE  |          |            |                            |                 |              | false       |
|            | SYSTEM       | STATS       | SYSTEM TABLE  |          |            |                            |                 |              | false       |
+------------+--------------+-------------+---------------+----------+------------+----------------------------+-----------------+--------------+-------------+
0: jdbc:phoenix:> 

```
## Phoenix Query Server and thin client
Alternative method to access Phoenix SQL engine and Phoenix Query Server is thin client (https://phoenix.apache.org/server.html).<br>
Prerequisites.<br>
* Install Phoenix Query Server on one of the HDP nodes. It is not necessary to be HBase Master node.
* Verify the *hadoop.proxyuser.HTTP.groups* in HDFS configuration panel. Replace default *users* with proper group or simply insert \* (star).
Launch<br>
> phoenix-sqlline-thin \<Phoenix Query Server\>
```
Setting property: [incremental, false]
Setting property: [isolation, TRANSACTION_READ_COMMITTED]
issuing: !connect jdbc:phoenix:thin:url=http://mdp2:8765;serialization=PROTOBUF;authentication=SPNEGO none none org.apache.phoenix.queryserver.client.Driver
Connecting to jdbc:phoenix:thin:url=http://mdp2:8765;serialization=PROTOBUF;authentication=SPNEGO
Connected to: Apache Phoenix (version unknown version)
Driver: Phoenix Remote JDBC Driver (version unknown version)
Autocommit status: true
Transaction isolation: TRANSACTION_READ_COMMITTED
Building list of tables and columns for tab-completion (set fastconnect to true to skip)...
135/135 (100%) Done
Done
sqlline version 1.2.0
0: jdbc:phoenix:thin:url=http://mdp2:8765> 
```
Troubleshooting<br>
Problem:<br>
*phoenix-sqlline-think* is stuck and prompt is not available. <br>
In the *phoenix* log file are repeating entries<br>

```
2020-01-28 00:11:19,900 INFO org.apache.hadoop.hbase.client.RpcRetryingCallerImpl: Call exception, tries=23, retries=36, started=290638 ms ago, cancelled=false, msg=Connection closed, details=row 'SYSTEM:CATALOG' on table 'hbase:meta' at region=hbase:meta,,1.1588230740, hostname=mdp3.sb.com,16020,1580165834898, seqNum=-1
```
Solution<br>
Phoenix Query Server cannot connect to HBase Master. Verify *hadoop.proxyuser.HTTP.groups* parameter (look above)

The Phoenix SQL service should be already configured as a part of JDBC interpreter.
```
%jdbc(phoenix)
select DISTINCT("TABLE_NAME") from SYSTEM.CATALOG;
```
### Jupyter troubleshooting
```
WARN [2019-12-05 22:20:15,370] ({pool-3-thread-2} NotebookServer.java[afterStatusChange]:2302) - Job 20191128-100737_189998758 is finished, status: ERROR, exception: null, result: %text org.apache.zeppelin.interpreter.InterpreterException: Error in doAs
        at org.apache.zeppelin.jdbc.JDBCInterpreter.getConnection(JDBCInterpreter.java:464)
        at org.apache.zeppelin.jdbc.JDBCInterpreter.executeSql(JDBCInterpreter.java:673)
        at org.apache.zeppelin.jdbc.JDBCInterpreter.interpret(JDBCInterpreter.java:801)

```
To track down the problem, it is necessary to increase Zeppelin log level (*Zeppelin->Configs->Advanced zeppelin-log4j-properties*). Then tail */var/log/zeppelin/zeppelin....log* file. Usually, the problem boils down to assign proper privileges in HBase. It can be accomplished in a friendly way through Ranger UI.

## Connect to Ambari Metrics database
Ambari Metrics Collector is supported by separate HBase and Phoenix service. Even it is configured to keep containers in the HDFS, it uses different instance of HBase/Phoenix services. <br>
The database can be queried by Phoenix command line utility.<br>
Firstly log on the the host where Ambari Metrics Collector and switch to *ams* user.
> su - ams<br>

Obtain Kerberos ticket.<br>
> kinit -kt /etc/security/keytabs/ams.collector.keytab amshbase/\<host name\>@FYRE.NET<br>

Point to another *hbase* config directory related to Ambari Metrics Collector.
>  export HBASE_CONF_DIR=/etc/ams-hbase/conf<br>

Run Phoenix command line and include in the URL the Zookeeper znode specific to Ambari Metrics Collector.

> /usr/hdp/3.1.4.0-315/phoenix/bin/sqlline.py /<Zookeeper quorum/>:/ams-hbase-secure<br>
```
Setting property: [incremental, false]
Setting property: [isolation, TRANSACTION_READ_COMMITTED]
issuing: !connect jdbc:phoenix:a1.fyre.ibm.com,aa1.fyre.ibm.com,hurds1.fyre.ibm.com:/ams-hbase-secure none none org.apache.phoenix.jdbc.PhoenixDriver
Connecting to jdbc:phoenix:a1.fyre.ibm.com,aa1.fyre.ibm.com,hurds1.fyre.ibm.com:/ams-hbase-secure
SLF4J: Class path contains multiple SLF4J bindings.
...............
 !tables
+------------+--------------+-------------------------------+---------------+----------+------------+----------------------------+----------------+
| TABLE_CAT  | TABLE_SCHEM  |          TABLE_NAME           |  TABLE_TYPE   | REMARKS  | TYPE_NAME  | SELF_REFERENCING_COL_NAME  | REF_GENERATION |
+------------+--------------+-------------------------------+---------------+----------+------------+----------------------------+----------------+
|            | SYSTEM       | CATALOG                       | SYSTEM TABLE  |          |            |                            |                |
|            | SYSTEM       | FUNCTION                      | SYSTEM TABLE  |          |            |                            |                |
|            | SYSTEM       | LOG                           | SYSTEM TABLE  |          |            |                            |                |
|            | SYSTEM       | SEQUENCE                      | SYSTEM TABLE  |          |            |                            |                |
|            | SYSTEM       | STATS                         | SYSTEM TABLE  |          |            |                            |                |
|            |              | CONTAINER_METRICS             | TABLE         |          |            |                            |                |
|            |              | HOSTED_APPS_METADATA_UUID     | TABLE         |          |            |                            |                |
|            |              | INSTANCE_HOST_METADATA        | TABLE         |          |            |                            |                |
|            |              | METRICS_METADATA_UUID         | TABLE         |          |            |                            |                |
|            |              | METRIC_AGGREGATE_DAILY_UUID   | TABLE         |          |            |                            |                |
|            |              | METRIC_AGGREGATE_HOURLY_UUID  | TABLE         |          |            |                            |                |
|            |              | METRIC_AGGREGATE_MINUTE_UUID  | TABLE         |          |            |                            |                |
|            |              | METRIC_AGGREGATE_UUID         | TABLE         |          |            |                            |                |
|            |              | METRIC_RECORD_DAILY_UUID      | TABLE         |          |            |                            |                |
|            |              | METRIC_RECORD_HOURLY_UUID     | TABLE         |          |            |                            |                |
|            |              | METRIC_RECORD_MINUTE_UUID     | TABLE         |          |            |                            |                |
|            |              | METRIC_RECORD_UUID            | TABLE         |          |            |                            |                |
|            |              | METRIC_TRANSIENT              | TABLE         |          |            |                            |                |
+------------+--------------+-------------------------------+---------------+----------+------------+----------------------------+----------------+

```


