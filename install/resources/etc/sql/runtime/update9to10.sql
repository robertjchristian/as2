#
# Update script db version 9 to db version 10
# $Author: heller $
# $Revision: 1.2 $
#
#add HTTP authorization for data and async mdn
#
ALTER TABLE partner ADD COLUMN usehttpauth INTEGER
ALTER TABLE partner ADD COLUMN httpauthuser VARCHAR(256)
ALTER TABLE partner ADD COLUMN httpauthpass VARCHAR(256)
ALTER TABLE partner ADD COLUMN usehttpauthasyncmdn INTEGER
ALTER TABLE partner ADD COLUMN httpauthuserasnymdn VARCHAR(256)
ALTER TABLE partner ADD COLUMN httpauthpassasnymdn VARCHAR(256)
UPDATE partner SET usehttpauth=0
UPDATE partner SET usehttpauthasyncmdn=0

