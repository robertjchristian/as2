#
# Update script db version 13 to db version 14
# $Author: heller $
# $Revision: 1.2 $
#
#Add a notification entry
#
CREATE TABLE notification(id INTEGER IDENTITY PRIMARY KEY,mailhost VARCHAR(255),mailhostport INTEGER,mailaccountname VARCHAR(255),mailaccountpass VARCHAR(255),notificationemailaddress VARCHAR(255),notifycertexpire INTEGER,notifytransactionerror INTEGER,replyto VARCHAR(255))
INSERT INTO notification(mailhost,mailhostport,mailaccountname,mailaccountpass,notificationemailaddress,notifycertexpire,notifytransactionerror,replyto)VALUES('mail.host.de',25,'SenderAccountName','SenderAccountPass','NotificationReceiver@otherhost.de',0,0,'DoNotReply@otherhost.de')


