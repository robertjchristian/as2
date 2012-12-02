#
# Update script db version 33 to db version 34
# $Author: heller $
# $Revision: 1.2 $
#
#Perform the database split: drop all unused tables and idicies for the config database
#
DROP TABLE notification
DROP TABLE httpheader
DROP TABLE certificates
DROP TABLE partnersystem
#partner
DROP INDEX idx_partner_islocal IF EXISTS
DROP INDEX idx_partner_as2ident IF EXISTS
DROP TABLE partner
