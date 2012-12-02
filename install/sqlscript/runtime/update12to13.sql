#
# Update script db version 12 to db version 13
# $Author: heller $
# $Revision: 1.2 $
#
#Add a partner comment
#
ALTER TABLE partner ADD COLUMN partnercomment OBJECT


