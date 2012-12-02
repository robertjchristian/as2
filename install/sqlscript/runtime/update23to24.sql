#
# Update script db version 23 to db version 24
# $Author: heller $
# $Revision: 1.2 $
#add user defined http headers per partner
CREATE TABLE httpheader( id INTEGER IDENTITY PRIMARY KEY, partnerid INTEGER, key VARCHAR(255), value VARCHAR(255),FOREIGN KEY(partnerid)REFERENCES partner(id))



