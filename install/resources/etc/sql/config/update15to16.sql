#
# Update script db version 15 to db version 16
# $Author: heller $
# $Revision: 1.2 $
#
#Add partner notification counter
#
ALTER TABLE partner ADD COLUMN notifysend INTEGER
ALTER TABLE partner ADD COLUMN notifyreceive INTEGER
ALTER TABLE partner ADD COLUMN notifysendreceive INTEGER
ALTER TABLE partner ADD COLUMN notifysendenabled INTEGER
ALTER TABLE partner ADD COLUMN notifyreceiveenabled INTEGER
ALTER TABLE partner ADD COLUMN notifysendreceiveenabled INTEGER
UPDATE partner SET notifysend=0,notifyreceive=0,notifysendreceive=0,notifysendenabled=0,notifyreceiveenabled=0,notifysendreceiveenabled=0
