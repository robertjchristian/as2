#
# Update script db version 19 to db version 20
# $Author: heller $
# $Revision: 1.2 $
#
#Add content transfer encoding per partner
#
ALTER TABLE partner ADD COLUMN contenttransferencoding INTEGER
#default all existing partners content transfer encoding to "binary"
UPDATE partner SET contenttransferencoding=1