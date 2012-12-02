#
# Update script db version 35 to db version 36
# $Author: heller $
# $Revision: 1.2 $
#
#Keep the number of resends in the database per message
#
ALTER TABLE messages ADD COLUMN resendcounter INT DEFAULT 0 NOT NULL
