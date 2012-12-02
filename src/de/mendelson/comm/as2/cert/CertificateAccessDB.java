//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/cert/CertificateAccessDB.java,v 1.1 2012/04/18 14:10:22 heller Exp $
package de.mendelson.comm.as2.cert;

import de.mendelson.comm.as2.notification.Notification;
import de.mendelson.comm.as2.partner.Partner;
import de.mendelson.comm.as2.partner.PartnerCertificateInformation;
import de.mendelson.comm.as2.server.AS2Server;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.logging.Logger;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Access the certificate lists in the database
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class CertificateAccessDB {

    /**Logger to log inforamtion to*/
    private Logger logger = Logger.getLogger(AS2Server.SERVER_LOGGER_NAME);
    /**Connection to the database*/
    private Connection configConnection;
    private Connection runtimeConnection;

    /** Creates new message I/O log and connects to localhost
     *@param host host to connect to
     */
    public CertificateAccessDB(Connection configConnection, Connection runtimeConnection) {
        this.configConnection = configConnection;
        this.runtimeConnection = runtimeConnection;
    }

    /**Returns the list of certificates used by the passed partner*/
    public void loadPartnerCertificateInformation(Partner partner) {
        PreparedStatement statement = null;
        ResultSet result = null;
        try {
            statement = this.configConnection.prepareStatement("SELECT * FROM certificates WHERE partnerid=?");
            statement.setInt(1, partner.getDBId());
            statement.setEscapeProcessing(true);
            result = statement.executeQuery();
            while (result.next()) {
                String fingerprint = result.getString("fingerprintsha1");
                PartnerCertificateInformation information = new PartnerCertificateInformation(
                        fingerprint, result.getInt("category"));
                information.setPriority(result.getInt("prio"));
                partner.setCertificateInformation(information);
            }
        } catch (Exception e) {
            this.logger.severe("CertificateAccessDB.loadPartnerCertificateInformation: " + e.getMessage());
            Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (Exception e) {
                    //nop
                }
            }
            if (result != null) {
                try {
                    result.close();
                } catch (Exception e) {
                    //nop
                }
            }
        }
    }

    /**Stores the actual partner certificate list of a partner*/
    public void storePartnerCertificateInformationList(Partner partner) {
        this.deletePartnerCertificateInformationList(partner);
        Collection<PartnerCertificateInformation> list = partner.getPartnerCertificateInformationList().asList();
        for (PartnerCertificateInformation certInfo : list) {
            PreparedStatement statement = null;
            try {
                statement = this.configConnection.prepareStatement("INSERT INTO certificates(partnerid,fingerprintsha1,category,prio)VALUES(?,?,?,?)");
                statement.setEscapeProcessing(true);
                statement.setInt(1, partner.getDBId());
                statement.setString(2, certInfo.getFingerprintSHA1());
                statement.setInt(3, certInfo.getCategory());
                statement.setInt(4, certInfo.getPriority());
                statement.execute();
            } catch (SQLException e) {
                this.logger.severe("storePartnerCertificateInformationList: " + e.getMessage());
                Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
            } finally {
                if (statement != null) {
                    try {
                        statement.close();
                    } catch (Exception e) {
                        //nop
                    }
                }
            }
        }
    }

    /**Deletes the actual partner certificate list of a partner*/
    public void deletePartnerCertificateInformationList(Partner partner) {
        PreparedStatement statement = null;
        try {
            statement = this.configConnection.prepareStatement("DELETE FROM certificates WHERE partnerid=?");
            statement.setEscapeProcessing(true);
            statement.setInt(1, partner.getDBId());
            statement.execute();
        } catch (SQLException e) {
            this.logger.severe("CertificateAccessDB.deletePartnerCertificateInformationList: " + e.getMessage());
            Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (Exception e) {
                    this.logger.severe("CertificateAccessDB.deletePartnerCertificateInformationList: " + e.getMessage());
                    Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
                }
            }
        }
    }
}
