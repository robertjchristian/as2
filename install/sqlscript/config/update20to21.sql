#
# Update script db version 20 to db version 21
# $Author: heller $
# $Revision: 1.2 $
#
#Add some indicies to the db
#
CREATE INDEX idx_statisticdetails_mdndate on statisticdetails(mdndate)
CREATE INDEX idx_statisticoverview_partnerid on statisticoverview(partnerid)
CREATE INDEX idx_messages_messagedate ON messages(messagedate)
CREATE INDEX idx_partner_islocal ON partner(islocal)
CREATE INDEX idx_partner_as2ident ON partner(as2ident)