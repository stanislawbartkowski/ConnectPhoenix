# ConnectPhoenix

It is the example how to connect and run simple queries againt Phoenix service in Kerberized environment. Phoenix is SQL engine on the top of Apache HBase.
https://phoenix.apache.org/server.html<bt>
The solution can be executed as Intellij IDEA project or as a standalone test.

# Files description

* /out/artifacts/ConnectPhoenix/ConnectPhoenix.jar Java jar with the test
* src/main/java/Main.java Source code
* sh Directory to run the test as standalone
  * sh/run.sh Shell script to execute
  * sh/template Directory with resource files to configure
    * sh/template/emv.rc Evironment variables used by run.sh script
    * sh/template/param.properties Phoenix JDBC URL parameters
    * sh/template/log4j.properties 
    
# param.properties
https://community.cloudera.com/t5/Community-Articles/Phoenix-JDBC-Client-Setup/ta-p/244284

Parameter | Description | Sample value
------------ | ------------- | -------
url | Phoenix JDBC URL (without Kerberos) | jdbc:phoenix:a1.fyre.ibm.com,aa1.fyre.ibm.com,hurds1.fyre.ibm.com:2181:/hbase-secure
kerberos | Kerberos part, format (principal):(path to keytab) | techuser@FYRE.NET:/home/sb/techuser.keytab
query | Optional, a sample query to run | SELECT COUNT(\*) FROM SYSTEM.CATALOG
update | Optional, a sample update statement | create table test (mykey integer not null primary key, mycolumn varchar)

# Test
The program is connecting to Phoenix server, outputing the tables found (select DISTINCT(\"TABLE_NAME\") from SYSTEM.CATALOG") and, optionally, execute *query* and *update* statements found in *param.properties* file.<br>
The program is accepting a single parameter, path to *param.properties* file.<br>
Troubleshooting: change to looging level to *DEBUG* in *log4j.properties* file.<br>
In Kerberized environment, obtain valid Kerbers keytab file.

# Run as Intellij IDEA project
## Clone the project
Clone to project into Intellij IDEA.
## Copy hadoop and hbase configuration data
From target Hadoop (HDP) environment, copy *hadoop/conf* and *hbase/conf* directories. Create a directory structure.
* sh/hadoop/conf
* sh/hbase/conf
## Copy templates and modify
Copy *sh/template/param.properties* and *sh/template/log4j.properties* to *sh* directory. Modify *param.properties* file according to your environmet.
## Prepare the launch configuration
* VM options: -Dlog4j.configuration=file:sh/log4j.properties 
* Program arguments: sh/param.properties

Add *sh/hadoop/conf* and *sh/hbase/conf* to Java ClassPath. That's very important, otherwise the program will not be able to access Phoenix server.

# Run the test as a standalone application
## Clone the repository
> git clone https://github.com/stanislawbartkowski/ConnectPhoenix.git
## Copy and modify the templates
> cd ConnectPhoenix/sh<br>
> cp templates/* .<br>

Modify *param.templates* and *env.rc* configuration files.

## Run the test
> ./run.sh
