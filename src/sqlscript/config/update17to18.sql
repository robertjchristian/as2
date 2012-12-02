#
# Update script db version 17 to db version 18
# $Author: heller $
# $Revision: 1.2 $
#
#Add command on send (success/failure)
#
ALTER TABLE partner ADD COLUMN commandonsenderror VARCHAR(2048)
ALTER TABLE partner ADD column usecommandonsenderror INTEGER
ALTER TABLE partner ADD column commandonsendsuccess VARCHAR(2048)
ALTER TABLE partner ADD column usecommandonsendsuccess INTEGER
UPDATE partner set usecommandonsenderror=0
UPDATE partner set usecommandonsendsuccess=0