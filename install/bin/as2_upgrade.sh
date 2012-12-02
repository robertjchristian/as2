#! /bin/sh
# -----------------------------------------------------------
# Database script to upgrade an existing pre as2 2012 
# database to as2 2012 or above.
#
# (c) mendelson-e-commerce GmbH, Berlin
# -----------------------------------------------------------
jre/bin/java -Xmx1300M -Xms92M -classpath ".:upgrade/hsqldb1.8.1.3.jar:as2.jar" de.mendelson.upgrade.DB18ToScript
jre/bin/java -Xmx1300M -Xms92M -classpath ".:upgrade/hsqldb2.2.7.jar:as2.jar" de.mendelson.upgrade.DBScriptTo20

