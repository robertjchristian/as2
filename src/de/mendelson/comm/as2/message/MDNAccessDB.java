//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/message/MDNAccessDB.java,v 1.1 2012/04/18 14:10:30 heller Exp $
package de.mendelson.comm.as2.message;

import de.mendelson.comm.as2.notification.Notification;
import de.mendelson.comm.as2.server.AS2Server;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Access MDN
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class MDNAccessDB {

    /**Logger to log inforamtion to*/
    private Logger logger = Logger.getLogger(AS2Server.SERVER_LOGGER_NAME);
    /**Connection to the database*/
    private Connection runtimeConnection = null;
    private Connection configConnection = null;

    /** Creates new message I/O log and connects to localhost
     *@param host host to connect to
     */
    public MDNAccessDB(Connection configConnection, Connection runtimeConnection) {
        this.runtimeConnection = runtimeConnection;
        this.configConnection = configConnection;
    }

    /**Returns all overview rows from the datase*/
    public List<AS2MDNInfo> getMDN(String relatedMessageId) {
        List<AS2MDNInfo> messageList = new ArrayList<AS2MDNInfo>();
        ResultSet result = null;
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = this.runtimeConnection.prepareStatement("SELECT * FROM mdn WHERE relatedmessageid=? ORDER BY initdate ASC");
            preparedStatement.setEscapeProcessing(true);
            preparedStatement.setString(1, relatedMessageId);
            result = preparedStatement.executeQuery();
            while (result.next()) {
                AS2MDNInfo info = new AS2MDNInfo();
                info.setMessageId(result.getString("messageid"));
                info.setInitDate(result.getTimestamp("initdate"));
                info.setDirection(result.getInt("direction"));
                info.setRelatedMessageId(result.getString("relatedmessageid"));
                info.setRawFilename(result.getString("rawfilename"));
                info.setReceiverId(result.getString("receiverid"));
                info.setSenderId(result.getString("senderid"));
                info.setSignType(result.getInt("signature"));
                info.setState(result.getInt("state"));
                info.setHeaderFilename(result.getString("headerfilename"));
                info.setSenderHost(result.getString("senderhost"));
                info.setUserAgent(result.getString("useragent"));
                Object mdnTextObj = result.getObject("mdntext");
                if (!result.wasNull() && mdnTextObj instanceof String) {
                    info.setRemoteMDNText((String)mdnTextObj);
                } else {
                    info.setRemoteMDNText(null);
                }
                messageList.add(info);
            }
            return (messageList);
        } catch (Exception e) {
            this.logger.severe("MDNAccessDB.getMDN: " + e.getMessage());
            Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
            return (null);
        } finally {
            if (result != null) {
                try {
                    result.close();
                } catch (Exception e) {
                    Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
                }
            }
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (Exception e) {
                    Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
                }
            }
        }
    }

    /**Adds a MDN to the database
     */
    public void initializeOrUpdateMDN(AS2MDNInfo info) {
        String messageId = info.getRelatedMessageId();
        //check if a related message exists
        MessageAccessDB messageAccess = new MessageAccessDB(this.configConnection, this.runtimeConnection);
        if (!messageAccess.messageIdExists(messageId)) {
            throw new RuntimeException("Unexpected MDN received: No related message exists for inbound MDN \"" + messageId + "\"");
        }
        List<AS2MDNInfo> list = this.getMDN(messageId);
        if (list == null || list.isEmpty()) {
            this.initializeMDN(info);
        } else {
            this.updateMDN(info);
        }
    }

    /**Adds a MDN to the datasbase
     */
    private void updateMDN(AS2MDNInfo info) {
        PreparedStatement statement = null;
        try {
            statement = this.runtimeConnection.prepareStatement(
                    "UPDATE mdn SET rawfilename=?,receiverid=?,senderid=?,signature=?,state=?,headerfilename=?,"
                    + "mdntext=? WHERE messageid=?");
            statement.setEscapeProcessing(true);
            statement.setString(1, info.getRawFilename());
            statement.setString(2, info.getReceiverId());
            statement.setString(3, info.getSenderId());
            statement.setInt(4, info.getSignType());
            statement.setInt(5, info.getState());
            statement.setString(6, info.getHeaderFilename());
            if (info.getRemoteMDNText() == null) {
                statement.setNull(7, Types.JAVA_OBJECT);
            } else {
                statement.setObject(7, info.getRemoteMDNText());
            }
            //condition
            statement.setString(8, info.getMessageId());
            statement.execute();
        } catch (SQLException e) {
            this.logger.severe("MDNAccessDB.updateMDN: " + e.getMessage());
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

    /**Checks if the MDN id does already exist in the database. In this case an error occured -
     * a MDNs message id has to be unique
     */
    private void checkForUniqueMDNMessageId(AS2MDNInfo info) {
        PreparedStatement statement = null;
        ResultSet result = null;
        try {
            //get SSL and sign certificates
            String query = "SELECT COUNT(1) AS counter FROM mdn WHERE messageid=?";
            statement = this.runtimeConnection.prepareStatement(query);
            statement.setString(1, info.getMessageId());
            result = statement.executeQuery();
            if (result.next()) {
                if (result.getInt("counter") > 0) {
                    throw new RuntimeException("The received MDN with the message id "
                            + "\"" + info.getMessageId() + "\" does already exist in the system."
                            + " The message id of MDN must be unique, this MDN is related to the message "
                            + "\"" + info.getRelatedMessageId() + "\".");
                }
            }
        } catch (SQLException e) {
            //keep a SQL exception here, do not catch the runtime exception
            this.logger.severe("MDNAccessDB.checkForUniqueMDNMessageId: " + e.getMessage());
            Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
        } finally {
            if (result != null) {
                try {
                    result.close();
                } catch (Exception e) {
                    this.logger.severe("MDNAccessDB.checkForUniqueMDNMessageId: " + e.getMessage());
                    Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (Exception e) {
                    this.logger.severe("MDNAccessDB.checkForUniqueMDNMessageId: " + e.getMessage());
                    Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
                }
            }
        }
    }

    /**Adds a MDN to the datasbase
     */
    private void initializeMDN(AS2MDNInfo info) {
        this.checkForUniqueMDNMessageId(info);
        PreparedStatement statement = null;
        try {
            statement = this.runtimeConnection.prepareStatement(
                    "INSERT INTO mdn(messageid,relatedmessageid,initdate,direction,rawfilename,receiverid,senderid,signature,state,headerfilename,senderhost,useragent,mdntext)"
                    + "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)");
            statement.setEscapeProcessing(true);
            statement.setString(1, info.getMessageId());
            statement.setString(2, info.getRelatedMessageId());
            statement.setTimestamp(3, new java.sql.Timestamp(info.getInitDate().getTime()));
            statement.setInt(4, info.getDirection());
            statement.setString(5, info.getRawFilename());
            statement.setString(6, info.getReceiverId());
            statement.setString(7, info.getSenderId());
            statement.setInt(8, info.getSignType());
            statement.setInt(9, info.getState());
            statement.setString(10, info.getHeaderFilename());
            statement.setString(11, info.getSenderHost());
            statement.setString(12, info.getUserAgent());
            if (info.getRemoteMDNText() == null) {
                statement.setNull(13, Types.JAVA_OBJECT);
            } else {
                statement.setObject(13, info.getRemoteMDNText());
            }
            statement.executeUpdate();
        } catch (Exception e) {
            this.logger.severe("MDNAccessDB.initializeMDN: " + e.getMessage());
            Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (Exception e) {
                    this.logger.severe("MDNAccessDB.initializeMDN: " + e.getMessage());
                    Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
                }
            }
        }
    }

    /**Returns all file names of files that could be deleted for a passed message
     *info*/
    public List<String> getRawFilenamesToDelete(String messageId) {
        List<String> list = new ArrayList<String>();
        ResultSet result = null;
        PreparedStatement statement = null;
        try {
            String query = "SELECT * FROM mdn WHERE relatedmessageid=?";
            statement = this.runtimeConnection.prepareStatement(query);
            statement.setEscapeProcessing(true);
            statement.setString(1, messageId);
            result = statement.executeQuery();
            while (result.next()) {
                String rawFilename = result.getString("rawfilename");
                if (!result.wasNull()) {
                    list.add(rawFilename);
                }
                String headerFilename = result.getString("headerfilename");
                if (!result.wasNull()) {
                    list.add(headerFilename);
                }
            }
        } catch (Exception e) {
            this.logger.severe("MDNAccessDB.getRawFilenamesToDelete: " + e.getMessage());
            Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
        } finally {
            if (result != null) {
                try {
                    result.close();
                } catch (Exception e) {
                    this.logger.severe("MDNAccessDB.getRawFilenamesToDelete: " + e.getMessage());
                    Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (Exception e) {
                    this.logger.severe("MDNAccessDB.getRawFilenamesToDelete: " + e.getMessage());
                    Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
                }
            }
        }
        return (list);
    }
}
