#
# Create a new as2 RUNTIME server database and initialize it
# $Author: heller $
# $Revision: 1.2 $
#
# Create table for update and version control
#
CREATE TABLE version(id INTEGER IDENTITY PRIMARY KEY,actualVersion INTEGER,updateDate TIMESTAMP,updateComment VARCHAR(255))
#
# Message details
#
CREATE CACHED TABLE messages(messageid VARCHAR(255) PRIMARY KEY,initdate TIMESTAMP,senddate TIMESTAMP,direction INTEGER,rawfilename VARCHAR(512),state INTEGER,signature INTEGER,encryption INTEGER,senderid VARCHAR(255),receiverid VARCHAR(255),syncmdn INTEGER,headerfilename VARCHAR(512),rawdecryptedfilename VARCHAR(512),senderhost VARCHAR(255),useragent VARCHAR(255),contentmic VARCHAR(255),compression INT DEFAULT 0 NOT NULL,messagetype INT DEFAULT 1 NOT NULL,asyncmdnurl VARCHAR(512),subject VARCHAR(255),resendcounter INT DEFAULT 0 NOT NULL)
CREATE INDEX idx_messages_initdate ON messages(initdate)
CREATE INDEX idx_messages_contentmic ON messages(contentmic)
#
# MDN details
#
CREATE CACHED TABLE mdn(messageid VARCHAR(255) PRIMARY KEY,relatedmessageid VARCHAR(255),initdate TIMESTAMP,direction INTEGER,rawfilename VARCHAR(512), state INTEGER, signature INTEGER, senderid VARCHAR(255),receiverid VARCHAR(255), headerfilename VARCHAR(512),senderhost VARCHAR(255),useragent VARCHAR(255),mdntext OBJECT,FOREIGN KEY(relatedmessageid)REFERENCES messages(messageid))
CREATE INDEX idx_mdn_initdate ON mdn(initdate)
#
#message log
#
CREATE CACHED TABLE messagelog( id INTEGER IDENTITY PRIMARY KEY,messageid VARCHAR(255),timestamp TIMESTAMP,loglevel INTEGER,details OBJECT,FOREIGN KEY(messageid)REFERENCES messages(messageid))
CREATE INDEX idx_messagelog_messageid ON messagelog(messageid)
#
#payload 
#
CREATE CACHED TABLE payload( id INTEGER IDENTITY PRIMARY KEY,messageid VARCHAR(255),originalfilename VARCHAR(512),payloadfilename VARCHAR(512),contentid VARCHAR(255),contenttype VARCHAR(255),FOREIGN KEY(messageid)REFERENCES messages(messageid))
CREATE INDEX idx_payload_messageid ON payload(messageid)
#
#statistic overview
#
CREATE TABLE statisticoverview(relationshipid VARCHAR(255)PRIMARY KEY,localstationid VARCHAR(255),partnerid VARCHAR(255),sendmessagecount INTEGER,receivedmessagecount INTEGER,sendwithfailurecount INTEGER,receivedwithfailurecount INTEGER,resetdate TIMESTAMP)
CREATE INDEX idx_statisticoverview_localstationid on statisticoverview(localstationid);
CREATE INDEX idx_statisticoverview_partnerid on statisticoverview(partnerid);
#
#statistic details
#
CREATE TABLE statisticdetails(id INTEGER IDENTITY PRIMARY KEY,localstation VARCHAR(255),partner VARCHAR(255),mdndate BIGINT,messageid VARCHAR(255),direction INTEGER,messagestate INTEGER)
CREATE INDEX idx_statisticdetails_localstation on statisticdetails(localstation)
CREATE INDEX idx_statisticdetails_partner on statisticdetails(partner)
CREATE INDEX idx_statisticdetails_mdndate on statisticdetails(mdndate)
#
#system statistic
#
CREATE TABLE serverstatistic(id INTEGER IDENTITY PRIMARY KEY,counter INTEGER DEFAULT 0 NOT NULL,server VARCHAR(255) NOT NULL,direction INTEGER DEFAULT 0 NOT NULL,sign INTEGER DEFAULT 0 NOT NULL,encrypt INTEGER DEFAULT 0 NOT NULL,compression INTEGER DEFAULT 0 NOT NULL,lastgood BIGINT DEFAULT 0 NOT NULL)
CREATE INDEX idx_serverstatistic_server on serverstatistic(server)
#
#CEM requests and responses go here
#
CREATE TABLE cem(id INTEGER IDENTITY PRIMARY KEY,initiatoras2id VARCHAR(255),receiveras2id VARCHAR(255),requestid VARCHAR(255),requestmessageid VARCHAR(255),responsemessageid VARCHAR(255),respondbydate BIGINT,requestmessageoriginated BIGINT,responsemessageoriginated BIGINT,category INTEGER,cemstate INTEGER,serialid VARCHAR(255),issuername VARCHAR(255),processed INT DEFAULT 0 NOT NULL,processdate BIGINT,reasonforrejection OBJECT)
#
#Send order queue
#
CREATE CACHED TABLE sendorder(id INTEGER IDENTITY PRIMARY KEY,scheduletime BIGINT NOT NULL,nextexecutiontime BIGINT NOT NULL,sendorder OBJECT NOT NULL,orderstate INTEGER NOT NULL)
