//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/partner/PartnerSystemAccessDB.java,v 1.1 2012/04/18 14:10:32 heller Exp $
package de.mendelson.comm.as2.partner;

import de.mendelson.comm.as2.notification.Notification;
import de.mendelson.comm.as2.server.AS2Server;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Logger;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Database access wrapper for partner system information. This is the information that is
 * collected if the AS2 system connects to an other AS2 system, it will be displayed in the
 * partner panel
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class PartnerSystemAccessDB {

    /**Logger to log inforamtion to*/
    private Logger logger = Logger.getLogger(AS2Server.SERVER_LOGGER_NAME);
    /**Connection to the database*/
    private Connection configConnection;
    private Connection runtimeConnection;

    public PartnerSystemAccessDB(Connection configConnection, Connection runtimeConnection) {
        this.configConnection = configConnection;
        this.runtimeConnection = runtimeConnection;
    }

    public PartnerSystem getPartnerSystem(Partner partner) {
        PreparedStatement statement = null;
        ResultSet result = null;
        try {
            statement = this.configConnection.prepareStatement("SELECT * FROM partnersystem WHERE partnerid=?");
            statement.setEscapeProcessing(true);
            statement.setInt(1, partner.getDBId());
            result = statement.executeQuery();
            if (result.next()) {
                PartnerSystem system = new PartnerSystem();
                system.setPartner(partner);
                system.setAs2Version(result.getString("as2version"));
                system.setProductName(result.getString("productname"));
                system.setCEM(result.getInt("cem") == 1);
                system.setCompression(result.getInt("compression") == 1);
                system.setMa(result.getInt("ma") == 1);
                return (system);
            }
        } catch (Exception e) {
            this.logger.severe("PartnerSystemAccessDB.getPartnerSystem: " + e.getMessage());
            Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
            return (null);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (Exception e) {
                    this.logger.severe("PartnerSystemAccessDB.getPartnerSystem: " + e.getMessage());
                    Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
                }
            }
            if (result != null) {
                try {
                    result.close();
                } catch (Exception e) {
                    this.logger.severe("PartnerSystemAccessDB.getPartnerSystem: " + e.getMessage());
                    Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
                }
            }
        }
        return (null);
    }

    /**Updates a single partnersystem in the db*/
    private void updatePartnerSystem(PartnerSystem system) {
        PreparedStatement statement = null;
        try {
            statement = this.configConnection.prepareStatement(
                    "UPDATE partnersystem SET as2version=?,productname=?,compression=?,ma=?,cem=? WHERE partnerid=?");
            statement.setEscapeProcessing(true);
            statement.setString(1, system.getAs2Version());
            statement.setString(2, system.getProductName());
            statement.setInt(3, system.supportsCompression() ? 1 : 0);
            statement.setInt(4, system.supportsMA() ? 1 : 0);
            statement.setInt(5, system.supportsCEM() ? 1 : 0);
            statement.setInt(6, system.getPartner().getDBId());
            statement.execute();
        } catch (Exception e) {
            this.logger.severe("PartnerSystemAccessDB.updatePartnerSystem: " + e.getMessage());
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

    /**Deletes a single partnersystem from the database
     */
    public void deletePartnerSystem(Partner partner) {
        PreparedStatement statement = null;
        try {
            statement = this.configConnection.prepareStatement("DELETE FROM partnersystem WHERE partnerid=?");
            statement.setEscapeProcessing(true);
            statement.setInt(1, partner.getDBId());
            statement.execute();
        } catch (Exception e) {
            this.logger.severe("PartnerSystemAccessDB.deletePartnerSystem: " + e.getMessage());
            Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (Exception e) {
                    this.logger.severe("PartnerSystemAccessDB.deletePartnerSystem: " + e.getMessage());
                    Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
                }
            }
        }
    }

    /**Inserts a new entry into the database or updates an existing one*/
    public synchronized void insertOrUpdatePartnerSystem(PartnerSystem partnerSystem) {
        PartnerSystem system = this.getPartnerSystem(partnerSystem.getPartner());
        if (system == null) {
            this.insertPartnerSystem(partnerSystem);
        } else {
            this.updatePartnerSystem(partnerSystem);
        }
    }

    /**Inserts a new partnersystem into the database
     */
    private void insertPartnerSystem(PartnerSystem partnerSystem) {
        PreparedStatement statement = null;
        try {
            statement = this.configConnection.prepareStatement(
                    "INSERT INTO partnersystem(partnerid,as2version,productname,compression,ma,cem)VALUES(?,?,?,?,?,?)");
            statement.setEscapeProcessing(true);
            statement.setInt(1, partnerSystem.getPartner().getDBId());
            statement.setString(2, partnerSystem.getAs2Version());
            statement.setString(3, partnerSystem.getProductName());
            statement.setInt(4, partnerSystem.supportsCompression() ? 1 : 0);
            statement.setInt(5, partnerSystem.supportsMA() ? 1 : 0);
            statement.setInt(6, partnerSystem.supportsCEM() ? 1 : 0);
            statement.execute();
        } catch (Exception e) {
            this.logger.severe("PartnerSystemAccessDB.insertPartnerSystem: " + e.getMessage());
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
