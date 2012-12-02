#
# Update script db version 1 to db version 2
# $Author: heller $
# $Revision: 1.2 $
#
#modify message table: store the original filename
#
ALTER TABLE messages ADD COLUMN originalfilename VARCHAR(512)
