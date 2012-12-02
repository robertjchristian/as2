//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/notification/NotificationAccessDB.java,v 1.1 2012/04/18 14:10:31 heller Exp $
package de.mendelson.comm.as2.notification;

import de.mendelson.comm.as2.server.AS2Server;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.logging.Logger;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Implementation of a server log for the as2 server database
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class NotificationAccessDB {

    /**Logger to log inforamtion to*/
    private Logger logger = Logger.getLogger(AS2Server.SERVER_LOGGER_NAME);
    /**Connection to the database*/
    private Connection configConnection = null;

    /** Creates new message I/O log and connects to localhost
     *@param host host to connect to
     */
    public NotificationAccessDB(Connection configConnection) {
        this.configConnection = configConnection;
    }

    /**Reads the notification data from the db, there is only one available*/
    public NotificationData getNotificationData() {
        ResultSet result = null;
        PreparedStatement statement = null;
        try {
            statement = this.configConnection.prepareStatement("SELECT * FROM notification");
            result = statement.executeQuery();
            if (result.next()) {
                NotificationData data = new NotificationData();
                data.setAccountName(result.getString("mailaccountname"));
                data.setAccountPassword(result.getString("mailaccountpass").toCharArray());
                data.setMailServer(result.getString("mailhost"));
                data.setMailServerPort(result.getInt("mailhostport"));
                data.setNotificationMail(result.getString("notificationemailaddress"));
                data.setNotifyCertExpire(result.getInt("notifycertexpire") == 1 ? true : false);
                data.setNotifyTransactionError(result.getInt("notifytransactionerror") == 1 ? true : false);
                data.setNotifyCEM(result.getInt("notifycem") == 1 ? true : false);
                data.setNotifySystemFailure(result.getInt("notifysystemfailure") == 1 ? true : false);
                data.setNotifyResendDetected(result.getInt("notifyresend") == 1 ? true : false);
                data.setReplyTo(result.getString("replyto"));
                data.setUseSMTHAuth(result.getInt("usesmtpauth") == 1 ? true : false);
                data.setSmtpUser(result.getString("smtpauthuser"));
                String smtpPass = result.getString("smtpauthpass");
                if (!result.wasNull()) {
                    data.setSmtpPass(smtpPass.toCharArray());
                }
                return (data);
            }
        } catch (Exception e) {
            this.logger.severe("NotificationAccessDB.getNotificationData: " + e.getMessage());
            return (null);
        } finally {
            if (result != null) {
                try {
                    result.close();
                } catch (Exception e) {
                    this.logger.severe("NotificationAccessDB.getNotificationData: " + e.getMessage());
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (Exception e) {
                    this.logger.severe("NotificationAccessDB.getNotificationData: " + e.getMessage());
                }
            }
        }
        return (null);
    }

    /**Inserts a new message entry into the database
     */
    public void updateNotification(NotificationData data) {
        PreparedStatement statement = null;
        try {
            statement = this.configConnection.prepareStatement(
                    "UPDATE notification SET mailhost=?,mailhostport=?,mailaccountname=?,mailaccountpass=?,notificationemailaddress=?,notifycertexpire=?,notifytransactionerror=?,notifycem=?,notifysystemfailure=?,replyto=?,usesmtpauth=?,smtpauthuser=?,smtpauthpass=?,notifyresend=?");
            statement.setEscapeProcessing(true);
            statement.setString(1, data.getMailServer());
            statement.setInt(2, data.getMailServerPort());
            statement.setString(3, data.getAccountName());
            statement.setString(4, new String(data.getAccountPassword()));
            statement.setString(5, data.getNotificationMail());
            statement.setInt(6, data.notifyCertExpire() ? 1 : 0);
            statement.setInt(7, data.notifyTransactionError() ? 1 : 0);
            statement.setInt(8, data.notifyCEM() ? 1 : 0);
            statement.setInt(9, data.notifySystemFailure() ? 1 : 0);
            statement.setString(10, data.getReplyTo());
            statement.setInt(11, data.isUseSMTHAuth() ? 1 : 0);
            if (data.getSmtpUser() != null) {
                statement.setString(12, data.getSmtpUser());
            } else {
                statement.setNull(12, Types.VARCHAR);
            }
            if (data.getSmtpPass() != null) {
                statement.setString(13, String.valueOf(data.getSmtpPass()));
            } else {
                statement.setNull(13, Types.VARCHAR);
            }
            statement.setInt(14, data.notifyResendDetected() ? 1 : 0);
            statement.execute();
        } catch (Exception e) {
            this.logger.severe("NotificationAccessDB.updateNotification: " + e.getMessage());
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (Exception e) {
                    this.logger.severe("NotificationAccessDB.updateNotification: " + e.getMessage());
                }
            }
        }
    }
}
