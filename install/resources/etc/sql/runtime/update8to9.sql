#
# Update script db version 8 to db version 9
# $Author: heller $
# $Revision: 1.2 $
#
#add receipt command that will be executed on message receipt
#
ALTER TABLE partner ADD COLUMN commandonreceipt VARCHAR(2048)
ALTER TABLE partner ADD COLUMN usecommandonreceipt INTEGER
UPDATE partner SET usecommandonreceipt=0

