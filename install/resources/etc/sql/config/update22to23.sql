#
# Update script db version 22 to db version 23
# $Author: heller $
# $Revision: 1.2 $
#Add compression to the message info
ALTER TABLE messages ADD column compression INT DEFAULT 0 NOT NULL
#
#introducing the system statistic
#
CREATE TABLE serverstatistic(id INTEGER IDENTITY PRIMARY KEY,counter INTEGER DEFAULT 0 NOT NULL,server VARCHAR(255) NOT NULL,direction INTEGER DEFAULT 0 NOT NULL,sign INTEGER DEFAULT 0 NOT NULL,encrypt INTEGER DEFAULT 0 NOT NULL,compression INTEGER DEFAULT 0 NOT NULL,lastgood BIGINT DEFAULT 0 NOT NULL)
CREATE INDEX idx_serverstatistic_server on serverstatistic(server)


