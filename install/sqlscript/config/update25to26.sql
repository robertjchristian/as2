#
# Update script db version 25 to db version 26
# $Author: heller $
# $Revision: 1.2 $
#add system failure notification
ALTER TABLE notification ADD COLUMN notifysystemfailure INTEGER DEFAULT 0 NOT NULL



