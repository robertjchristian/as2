#
# Update script db version 9 to db version 10
# $Author: heller $
# $Revision: 1.2 $
#
#add the subject to the message
#
ALTER TABLE messages ADD COLUMN subject VARCHAR(255)
