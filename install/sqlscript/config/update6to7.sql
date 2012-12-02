#
# Update script db version 6 to db version 7
# $Author: heller $
# $Revision: 1.2 $
#
#modify partner table: add the filename of the status of a transaction for integration purpose
#
ALTER TABLE messages ADD COLUMN statefilename VARCHAR(512)
