Linguisto Tools
================
This software is used for managing the DB used on http://linguisto.eu/

Features:
* Database export/import
* Vocabulary export

Installation
============


Installation (on Ubuntu Linux)
------------

### Build linguisto.war

1. Run `mvn clean install -DskipTests` in command line in directory /linguisto-tools , linguisto-tools-full-x.y.jar will be generated in /linguisto-tools/target/

### Export dictionary to XDXF format.

java -Djdbc.driver=com.mysql.jdbc.Driver -Djdbc.url="jdbc:mysql://yourhost/yourdatabase?useUnicode=true&characterEncoding=UTF-8" -Djdbc.user=dbuser -Djdbc.password=password -jar linguisto-tools-full-1.0-SNAPSHOT.jar lang_from lang_to output_file.xdxf

### Convert XDXF-Dictionary for application Aard.

