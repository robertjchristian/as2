#
# Update script db version 9 to db version 10
# $Author: heller $
# $Revision: 1.2 $
#
#add max poll files to the poll threads
#
ALTER TABLE partner ADD COLUMN maxpollfiles INTEGER DEFAULT 100 NOT NULL
