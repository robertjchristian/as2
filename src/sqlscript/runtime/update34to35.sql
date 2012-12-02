#
# Update script db version 34 to db version 35
# $Author: heller $
# $Revision: 1.2 $
#
#Create a send order queue in the database
#
CREATE CACHED TABLE sendorder(id INTEGER IDENTITY PRIMARY KEY,scheduletime BIGINT NOT NULL,nextexecutiontime BIGINT NOT NULL,sendorder OBJECT NOT NULL,orderstate INTEGER NOT NULL)
