#
# Update script db version 2 to db version 3
# $Author: heller $
# $Revision: 1.2 $
#
#modify partner table: add poll ignore files 
#
ALTER TABLE partner ADD COLUMN pollignorelist VARCHAR(1024)
ALTER TABLE partner ADD COLUMN pollinterval INTEGER
