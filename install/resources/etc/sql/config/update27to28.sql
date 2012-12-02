#
# Update script db version 27 to db version 28
# $Author: heller $
# $Revision: 1.2 $
#add http version for each partner
ALTER TABLE partner add COLUMN httpversion VARCHAR(3) DEFAULT '1.1' NOT NULL



