#
# Update script db version 10 to db version 11
# $Author: heller $
# $Revision: 1.2 $
#
#split up the certificate alias into a sign- and a crypt alias
#
ALTER TABLE partner ADD COLUMN signalias VARCHAR(255)
ALTER TABLE partner ADD COLUMN cryptalias VARCHAR(255)
UPDATE partner SET signalias = (SELECT certalias FROM partner AS partner1 WHERE partner1.id=partner.id)
UPDATE partner SET cryptalias = (SELECT certalias FROM partner AS partner1 WHERE partner1.id=partner.id)
ALTER TABLE partner DROP COLUMN certalias


