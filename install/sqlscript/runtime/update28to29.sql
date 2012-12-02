#
# Update script db version 28 to db version 29
# $Author: heller $
# $Revision: 1.2 $
#create an index for the message search by the content MIC
CREATE INDEX idx_messages_contentmic ON messages(contentmic)



