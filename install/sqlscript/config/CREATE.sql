#
# Create a new as2 CONFIG server database and initialize it
# $Author: heller $
# $Revision: 1.2 $
#
# Create table for update and version control
#
CREATE TABLE version(id INTEGER IDENTITY PRIMARY KEY,actualversion INTEGER,updatedate TIMESTAMP,updatecomment VARCHAR(255))
#
#Create partner table
#
CREATE TABLE partner(id INTEGER IDENTITY PRIMARY KEY,as2ident VARCHAR(255),name VARCHAR(255),islocal INTEGER,sign INTEGER,encrypt INTEGER,email VARCHAR(255),signalias VARCHAR(255),cryptalias VARCHAR(255),url VARCHAR(255),mdnurl VARCHAR(255),subject VARCHAR(255),contenttype VARCHAR(255),syncmdn INTEGER,pollignorelist VARCHAR(1024),pollinterval INTEGER,compression INTEGER,signedmdn INTEGER,commandonreceipt VARCHAR(2048),usecommandonreceipt INTEGER,usehttpauth INTEGER,httpauthuser VARCHAR(256),httpauthpass VARCHAR(256),usehttpauthasyncmdn INTEGER,httpauthuserasnymdn VARCHAR(256),httpauthpassasnymdn VARCHAR(256),keeporiginalfilenameonreceipt INTEGER,partnercomment OBJECT,notifysend INTEGER,notifyreceive INTEGER,notifysendreceive INTEGER,notifysendenabled INTEGER,notifyreceiveenabled INTEGER,notifysendreceiveenabled INTEGER,commandonsenderror VARCHAR(2048),usecommandonsenderror INTEGER,commandonsendsuccess VARCHAR(2048),usecommandonsendsuccess INTEGER,contenttransferencoding INTEGER,httpversion VARCHAR(3) DEFAULT '1.1' NOT NULL,maxpollfiles INTEGER DEFAULT 100 NOT NULL)
CREATE INDEX idx_partner_islocal ON partner(islocal)
CREATE INDEX idx_partner_as2ident ON partner(as2ident)
INSERT INTO partner(as2ident,name,islocal,sign,encrypt,email,url,mdnurl,subject,contenttype,syncmdn,pollignorelist,pollinterval,compression,signedmdn,usecommandonreceipt,usehttpauth,usehttpauthasyncmdn,keeporiginalfilenameonreceipt,notifysend,notifyreceive,notifysendreceive,notifysendenabled,notifyreceiveenabled,notifysendreceiveenabled,usecommandonsenderror,usecommandonsendsuccess,contenttransferencoding,commandonreceipt,commandonsenderror,commandonsendsuccess)VALUES('mycompanyAS2','mycompany',1,2,2,'as2@company.com','http://www.company.com:8080/as2/HttpReceiver','http://www.company.com:8080/as2/HttpReceiver','AS2 message','application/EDI-Consent',1,'*.tmp,*.temp',10,1,1,0,0,0,0,0,0,0,0,0,0,0,0,1,'c:/mendelson/mbi/SendToMBI.exe -file ${filename} -media as2','C:/mendelson/mbi/SendToLog.exe -tid ${filename} -title "AS2 sending process failed" -details "The AS2 message has NOT been sent:\n${log}" -state ERROR','C:/mendelson/mbi/SendToLog.exe -tid ${filename} -title "AS2 message sent" -details "Success.\nThe AS2 message has been sent:\n${log}" -state SUCCESS')
INSERT INTO partner(as2ident,name,islocal,sign,encrypt,email,url,mdnurl,subject,contenttype,syncmdn,pollignorelist,pollinterval,compression,signedmdn,usecommandonreceipt,usehttpauth,usehttpauthasyncmdn,keeporiginalfilenameonreceipt,notifysend,notifyreceive,notifysendreceive,notifysendenabled,notifyreceiveenabled,notifysendreceiveenabled,usecommandonsenderror,usecommandonsendsuccess,contenttransferencoding,commandonreceipt,commandonsenderror,commandonsendsuccess)VALUES('mendelsontestAS2','mendelsontest',0,2,2,'as2@mendelson-e-c.com','http://testas2.mendelson-e-c.com:8080/as2/HttpReceiver','http://testas2.mendelson-e-c.com:8080/as2/HttpReceiver','AS2 message','application/EDI-Consent',1,'*.tmp,*.temp',10,1,1,0,0,0,0,0,0,0,0,0,0,0,0,1,'c:/mendelson/mbi/SendToMBI.exe -file ${filename} -media as2','C:/mendelson/mbi/SendToLog.exe -tid ${filename} -title "AS2 sending process failed" -details "The AS2 message has NOT been sent:\n${log}" -state ERROR','C:/mendelson/mbi/SendToLog.exe -tid ${filename} -title "AS2 message sent" -details "Success.\nThe AS2 message has been sent:\n${log}" -state SUCCESS')
#
#notification
#
CREATE TABLE notification(id INTEGER IDENTITY PRIMARY KEY,mailhost VARCHAR(255),mailhostport INTEGER,mailaccountname VARCHAR(255),mailaccountpass VARCHAR(255),notificationemailaddress VARCHAR(255),notifycertexpire INTEGER DEFAULT 0 NOT NULL,notifytransactionerror INTEGER DEFAULT 0 NOT NULL,notifycem INTEGER DEFAULT 0 NOT NULL,notifysystemfailure INTEGER DEFAULT 0 NOT NULL,replyto VARCHAR(255),usesmtpauth INTEGER DEFAULT 0 NOT NULL,smtpauthuser VARCHAR(255),smtpauthpass VARCHAR(255),notifyresend INTEGER DEFAULT 0 NOT NULL)
INSERT INTO notification(mailhost,mailhostport,mailaccountname,mailaccountpass,notificationemailaddress,replyto)VALUES('mail.host.de',25,'SenderAccountName','SenderAccountPass','NotificationReceiver@otherhost.de','DoNotReply@otherhost.de')
#
# additional/modified http headers per partner
#
CREATE TABLE httpheader(id INTEGER IDENTITY PRIMARY KEY,partnerid INTEGER,key VARCHAR(255),value VARCHAR(255),FOREIGN KEY(partnerid)REFERENCES partner(id))
#
#certificates and keys go here
#
CREATE TABLE certificates(id INTEGER IDENTITY PRIMARY KEY,partnerid INTEGER,fingerprintsha1 VARCHAR(255),category INTEGER,prio INT DEFAULT 0 NOT NULL,FOREIGN KEY(partnerid)REFERENCES partner(id))
#set encryption cert to key1
INSERT INTO certificates(partnerid,fingerprintsha1,category,prio)VALUES((SELECT id FROM partner WHERE as2ident='mycompanyAS2'),'3D:A0:27:42:4D:92:6D:4:BB:74:66:1D:48:3E:61:6A:46:2A:5:B7',1, 1 )
#set signature cert to key1
INSERT INTO certificates(partnerid,fingerprintsha1,category,prio)VALUES((SELECT id FROM partner WHERE as2ident='mycompanyAS2'),'3D:A0:27:42:4D:92:6D:4:BB:74:66:1D:48:3E:61:6A:46:2A:5:B7',2, 1 )
#set encryption cert to key2
INSERT INTO certificates(partnerid,fingerprintsha1,category,prio)VALUES((SELECT id FROM partner WHERE as2ident='mendelsontestAS2'),'6D:9A:2C:79:02:B:F1:6B:20:78:E4:A3:BE:DF:93:DD:2A:AD:B7:40',1, 1 )
#set signature cert to key2
INSERT INTO certificates(partnerid,fingerprintsha1,category,prio)VALUES((SELECT id FROM partner WHERE as2ident='mendelsontestAS2'),'6D:9A:2C:79:02:B:F1:6B:20:78:E4:A3:BE:DF:93:DD:2A:AD:B7:40',2, 1 )
#
#information about the partner system
#
CREATE TABLE partnersystem(id INTEGER IDENTITY PRIMARY KEY,partnerid INTEGER,as2version VARCHAR(10),productname VARCHAR(255),compression INTEGER DEFAULT 0 NOT NULL,ma INTEGER DEFAULT 0 NOT NULL,cem INTEGER DEFAULT 0 NOT NULL,FOREIGN KEY(partnerid)REFERENCES partner(id))
