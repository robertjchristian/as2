#
# Update script db version 16 to db version 17
# $Author: heller $
# $Revision: 1.2 $
#
#Modify details table
#
DROP TABLE statisticdetails
CREATE TABLE statisticdetails(id INTEGER IDENTITY PRIMARY KEY,localstation VARCHAR(255),partner VARCHAR(255),mdndate BIGINT,messageid VARCHAR(255),direction INTEGER,messagestate INTEGER)
CREATE INDEX idx_statisticdetails_localstation on statisticdetails(localstation)
CREATE INDEX idx_statisticdetails_partner on statisticdetails(partner)