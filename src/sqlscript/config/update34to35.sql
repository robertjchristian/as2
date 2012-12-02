#
# Update script db version 34 to db version 35
# $Author: heller $
# $Revision: 1.2 $
#
#Add a resend notification
#
ALTER TABLE notification ADD COLUMN notifyresend INTEGER DEFAULT 0 NOT NULL