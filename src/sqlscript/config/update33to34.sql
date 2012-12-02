#
# Update script db version 33 to db version 34
# $Author: heller $
# $Revision: 1.2 $
#
#Perform the database split: drop all unused tables and idicies for the config database
#
#table mdn
DROP INDEX idx_mdn_initdate IF EXISTS
DROP TABLE mdn
#message log
DROP INDEX idx_messagelog_messageid IF EXISTS
DROP TABLE messagelog
#payload
DROP INDEX idx_payload_messageid IF EXISTS
DROP TABLE payload
#table messages
DROP INDEX idx_messages_initdate IF EXISTS
DROP INDEX idx_messages_contentmic IF EXISTS
DROP TABLE messages
#cem
DROP TABLE cem
#statistic
DROP INDEX idx_statisticoverview_localstationid IF EXISTS
DROP INDEX idx_statisticoverview_partnerid IF EXISTS
DROP TABLE statisticoverview IF EXISTS
#
#statistic details
#
DROP INDEX idx_statisticdetails_localstation IF EXISTS
DROP INDEX idx_statisticdetails_partner IF EXISTS
DROP INDEX idx_statisticdetails_mdndate IF EXISTS
DROP TABLE statisticdetails IF EXISTS
#
#system statistic
#
DROP INDEX idx_serverstatistic_server IF EXISTS
DROP TABLE serverstatistic IF EXISTS

