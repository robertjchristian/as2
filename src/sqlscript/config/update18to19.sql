#
# Update script db version 18 to db version 19
# $Author: heller $
# $Revision: 1.2 $
#
#Add content mic storage for the message
#
ALTER TABLE messages ADD COLUMN contentmic VARCHAR(255)