#
# Update script db version 4 to db version 5
# $Author: heller $
# $Revision: 1.2 $
#
#modify partner table: add compression alg
#
ALTER TABLE partner ADD COLUMN compression INTEGER
