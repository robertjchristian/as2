#
# Update script db version 3 to db version 4
# $Author: heller $
# $Revision: 1.2 $
#
#modify message table: add additional files
#
ALTER TABLE messages ADD COLUMN headerfilename VARCHAR(512)
ALTER TABLE messages ADD COLUMN rawdecryptedfilename VARCHAR(512)
ALTER TABLE messages ADD COLUMN senderhost VARCHAR(255)
ALTER TABLE messages ADD COLUMN useragent VARCHAR(255)
