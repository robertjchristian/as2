#
# Update script db version 5 to db version 6
# $Author: heller $
# $Revision: 1.2 $
#
#modify partner table: add the request for signed mdn
#
ALTER TABLE partner ADD COLUMN signedmdn INTEGER
UPDATE partner SET signedmdn=1
