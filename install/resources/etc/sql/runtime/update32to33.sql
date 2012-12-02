#
# Update script db version 9 to db version 10
# $Author: heller $
# $Revision: 1.2 $
#
#create a send date
#
ALTER TABLE messages ADD COLUMN senddate TIMESTAMP
ALTER TABLE messages ALTER COLUMN messagedate RENAME TO initdate
CREATE INDEX idx_messages_initdate ON messages(initdate)
ALTER TABLE mdn ALTER COLUMN messagedate RENAME TO initdate
CREATE INDEX idx_mdn_initdate ON mdn(initdate)