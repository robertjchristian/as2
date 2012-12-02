#
# Update script db version 7 to db version 8
# $Author: heller $
# $Revision: 1.2 $
#
#add payload table
#
CREATE CACHED TABLE payload( messageid VARCHAR(255),originalfilename VARCHAR(512),payloadfilename VARCHAR(512))
CREATE INDEX idx_payload_messageid ON payload(messageid)

