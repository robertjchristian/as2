#
# Update script db version 11 to db version 12
# $Author: heller $
# $Revision: 1.2 $
#
#Keep the original filename on receipt, stored per partner
#
ALTER TABLE partner ADD COLUMN keeporiginalfilenameonreceipt INTEGER
UPDATE partner SET keeporiginalfilenameonreceipt=0


