#
# Update script db version 14 to db version 15
# $Author: heller $
# $Revision: 1.2 $
#
#Add statistic and message counter
#
CREATE TABLE statisticoverview(relationshipid VARCHAR(255)PRIMARY KEY,localstationid VARCHAR(255),partnerid VARCHAR(255),sendmessagecount INTEGER,receivedmessagecount INTEGER,sendwithfailurecount INTEGER,receivedwithfailurecount INTEGER,resetdate TIMESTAMP)
CREATE TABLE statisticdetails(id INTEGER IDENTITY PRIMARY KEY,relationshipid VARCHAR(255),mdndate TIMESTAMP,messageid VARCHAR(255),direction INTEGER,messagestate INTEGER,FOREIGN KEY(relationshipid)REFERENCES statisticoverview(relationshipid))

