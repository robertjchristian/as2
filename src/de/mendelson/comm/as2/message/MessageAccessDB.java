//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/message/MessageAccessDB.java,v 1.1 2012/04/18 14:10:30 heller Exp $
package de.mendelson.comm.as2.message;

import de.mendelson.comm.as2.notification.Notification;
import de.mendelson.comm.as2.partner.Partner;
import de.mendelson.comm.as2.server.AS2Server;
import de.mendelson.comm.as2.statistic.ServerInteroperabilityAccessDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software. Other product
 * and brand names are trademarks of their respective owners.
 */
/**
 * Implementation of a server log for the as2 server database
 *
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class MessageAccessDB {

    /**
     * Logger to log inforamtion to
     */
    private Logger logger = Logger.getLogger(AS2Server.SERVER_LOGGER_NAME);
    /**
     * Connection to the database
     */
    private Connection runtimeConnection = null;
    private Connection configConnection = null;

    /**
     * Creates new message I/O log and connects to localhost
     *
     * @param host host to connect to
     */
    public MessageAccessDB(Connection configConnection, Connection runtimeConnection) {
        this.runtimeConnection = runtimeConnection;
        this.configConnection = configConnection;
    }

    /**
     * Returns the state of the latest passed message. Will return pending state
     * if the messageid does not exist
     */
    public int getMessageState(String messageId) {
        int state = AS2Message.STATE_PENDING;
        try {
            //desc because the latest message should be first in resultset
            PreparedStatement statement = this.runtimeConnection.prepareStatement(
                    "SELECT state FROM messages WHERE messageid=? ORDER BY initdate DESC");
            statement.setString(1, messageId);
            ResultSet result = statement.executeQuery();
            if (result.next()) {
                state = result.getInt("state");
            }
            result.close();
            statement.close();
        } catch (Exception e) {
            this.logger.severe("getMessageState: " + e.getMessage());
            Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
        }
        return (state);
    }

    /**
     * Sets the corresponding message status to the new value. This will change
     * the state in any case without any check
     *
     * @param state one of the staes defined in the class AS2Message
     */
    public void setMessageState(String messageId, int fromState, int toState) {
        PreparedStatement statement = null;
        //perform the db update
        try {
            statement = this.runtimeConnection.prepareStatement(
                    "UPDATE messages SET state=? WHERE state=? AND messageid=?");
            statement.setInt(1, toState);
            statement.setInt(2, fromState);
            statement.setString(3, messageId);
            int rows = statement.executeUpdate();
        } catch (Exception e) {
            this.logger.severe("MessageAccessDB.setMessageState: " + e.getMessage());
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
        //perform the notification
        if (toState == AS2Message.STATE_STOPPED) {
            Notification notification = new Notification(this.configConnection, this.runtimeConnection);
            try {
                notification.sendTransactionError(messageId);
            } catch (Exception e) {
                this.logger.severe("MessageAccessDB.setMessageState: " + e.getMessage());
                Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
            }
        }
    }

    /**
     * Sets the corresponding message status to the new value. This will have
     * only effects if the actual message state is "pending". "Stopped" and
     * "finished" are states that MUST not be changed.
     *
     * @param newState one of the staes defined in the class AS2Message
     */
    public void setMessageState(String messageId, int newState) {
        int oldState = this.getMessageState(messageId);
        //keep red state and keep green state - only the pending state may be changed
        if (oldState != AS2Message.STATE_PENDING) {
            return;
        }
        this.setMessageState(messageId, oldState, newState);
        //store the entry in the interoperability statistic
        ServerInteroperabilityAccessDB statisticAccess = new ServerInteroperabilityAccessDB(this.configConnection, this.runtimeConnection);
        statisticAccess.addEntry(messageId);
    }

    /**
     * Returns information about the payload of a special message
     */
    public List<AS2Payload> getPayload(String messageId) {
        List<AS2Payload> payloadList = new ArrayList<AS2Payload>();
        ResultSet result = null;
        PreparedStatement statement = null;
        try {
            statement = this.runtimeConnection.prepareStatement("SELECT * FROM payload WHERE messageid=?");
            statement.setString(1, messageId);
            result = statement.executeQuery();
            while (result.next()) {
                AS2Payload payload = new AS2Payload();
                payload.setPayloadFilename(result.getString("payloadfilename"));
                payload.setOriginalFilename(result.getString("originalfilename"));
                payload.setContentId(result.getString("contentid"));
                payload.setContentType(result.getString("contenttype"));
                payloadList.add(payload);
            }
        } catch (Exception e) {
            this.logger.severe("MessageAccessDB.getPayload: " + e.getMessage());
            Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
        } finally {
            if (result != null) {
                try {
                    result.close();
                } catch (Exception e) {
                    //nop
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (Exception e) {
                    //nop
                }
            }
        }
        return (payloadList);
    }

    /**
     * Returns all detail rows from the datase
     */
    public List<AS2Info> getMessageDetails(String messageId) {
        List<AS2Info> messageList = new ArrayList<AS2Info>();
        messageList.addAll(this.getMessageOverview(messageId));
        MDNAccessDB mdnAccess = new MDNAccessDB(this.configConnection, this.runtimeConnection);
        messageList.addAll(mdnAccess.getMDN(messageId));
        return (messageList);
    }

    /**
     * Checks if a passed message id exists
     */
    public boolean messageIdExists(String messageId) {
        AS2MessageInfo info = this.getLastMessageEntry(messageId);
        return (info != null);
    }

    /**
     * Reads information about a specific messageid from the data base, returns
     * the latest message of this id
     */
    public AS2MessageInfo getLastMessageEntry(String messageId) {
        ResultSet result = null;
        PreparedStatement statement = null;
        try {
            //desc because we need the latest
            statement = this.runtimeConnection.prepareStatement("SELECT * FROM messages WHERE messageid=? ORDER BY initdate DESC");
            statement.setString(1, messageId);
            result = statement.executeQuery();
            if (result.next()) {
                AS2MessageInfo info = new AS2MessageInfo();
                info.setInitDate(result.getTimestamp("initdate"));
                info.setEncryptionType(result.getInt("encryption"));
                info.setDirection(result.getInt("direction"));
                info.setMessageType(result.getInt("messagetype"));
                info.setMessageId(result.getString("messageid"));
                info.setRawFilename(result.getString("rawfilename"));
                info.setReceiverId(result.getString("receiverid"));
                info.setSenderId(result.getString("senderid"));
                info.setSignType(result.getInt("signature"));
                info.setState(result.getInt("state"));
                info.setRequestsSyncMDN(result.getInt("syncmdn") == 1);
                info.setHeaderFilename(result.getString("headerfilename"));
                info.setRawFilenameDecrypted(result.getString("rawdecryptedfilename"));
                info.setSenderHost(result.getString("senderhost"));
                info.setUserAgent(result.getString("useragent"));
                info.setReceivedContentMIC(result.getString("contentmic"));
                info.setCompressionType(result.getInt("compression"));
                info.setAsyncMDNURL(result.getString("asyncmdnurl"));
                info.setSubject(result.getString("subject"));
                info.setResendCounter(result.getInt("resendcounter"));
                return (info);
            }
        } catch (Exception e) {
            this.logger.severe("MessageAccessDB.getLastMessageEntry: " + e.getMessage());
            Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
            return (null);
        } finally {
            if (result != null) {
                try {
                    result.close();
                } catch (Exception e) {
                    this.logger.severe("MessageAccessDB.getLastMessageEntry: " + e.getMessage());
                    Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (Exception e) {
                    this.logger.severe("MessageAccessDB.getLastMessageEntry: " + e.getMessage());
                    Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
                }
            }
        }
        return (null);
    }

    /**
     * Returns all overview rows from the datase
     */
    public List<AS2MessageInfo> getMessageOverview(String messageId) {
        List<AS2MessageInfo> messageList = new ArrayList<AS2MessageInfo>();
        ResultSet result = null;
        PreparedStatement statement = null;
        try {
            statement = this.runtimeConnection.prepareStatement("SELECT * FROM messages WHERE messageid=? ORDER BY initdate ASC");
            statement.setString(1, messageId);
            result = statement.executeQuery();
            while (result.next()) {
                AS2MessageInfo info = new AS2MessageInfo();
                info.setInitDate(result.getTimestamp("initdate"));
                info.setEncryptionType(result.getInt("encryption"));
                info.setDirection(result.getInt("direction"));
                info.setMessageType(result.getInt("messagetype"));
                info.setMessageId(result.getString("messageid"));
                info.setRawFilename(result.getString("rawfilename"));
                info.setReceiverId(result.getString("receiverid"));
                info.setSenderId(result.getString("senderid"));
                info.setSignType(result.getInt("signature"));
                info.setState(result.getInt("state"));
                info.setRequestsSyncMDN(result.getInt("syncmdn") == 1);
                info.setHeaderFilename(result.getString("headerfilename"));
                info.setRawFilenameDecrypted(result.getString("rawdecryptedfilename"));
                info.setSenderHost(result.getString("senderhost"));
                info.setUserAgent(result.getString("useragent"));
                info.setReceivedContentMIC(result.getString("contentmic"));
                info.setCompressionType(result.getInt("compression"));
                info.setAsyncMDNURL(result.getString("asyncmdnurl"));
                info.setSubject(result.getString("subject"));
                info.setResendCounter(result.getInt("resendcounter"));
                messageList.add(info);
            }
        } catch (Exception e) {
            this.logger.severe("MessageAccessDB.getMessageOverview: " + e.getMessage());
            Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
            return (null);
        } finally {
            if (result != null) {
                try {
                    result.close();
                } catch (Exception e) {
                    this.logger.severe("MessageAccessDB.getMessageOverview: " + e.getMessage());
                    Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (Exception e) {
                    this.logger.severe("MessageAccessDB.getMessageOverview: " + e.getMessage());
                    Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
                }
            }
        }
        return (messageList);
    }

    /**
     * Returns all overview rows from the datase
     */
    public List<AS2MessageInfo> getMessageOverview(MessageOverviewFilter filter) {
        List<AS2MessageInfo> messageList = new ArrayList<AS2MessageInfo>();
        ResultSet result = null;
        PreparedStatement statement = null;
        try {
            List<Object> parameterList = new ArrayList<Object>();
            StringBuilder queryCondition = new StringBuilder();
            if (filter.getShowPartner() != null) {
                Partner partner = filter.getShowPartner();
                if (queryCondition.length() == 0) {
                    queryCondition.append(" WHERE");
                } else {
                    queryCondition.append(" AND");
                }
                queryCondition.append("(senderid=? OR receiverid=?)");
                parameterList.add(partner.getAS2Identification());
                parameterList.add(partner.getAS2Identification());
            }
            if (filter.getShowLocalStation() != null) {
                Partner localStation = filter.getShowLocalStation();
                if (queryCondition.length() == 0) {
                    queryCondition.append(" WHERE");
                } else {
                    queryCondition.append(" AND");
                }
                queryCondition.append("(senderid=? OR receiverid=?)");
                parameterList.add(localStation.getAS2Identification());
                parameterList.add(localStation.getAS2Identification());
            }
            if (!filter.isShowFinished()) {
                if (queryCondition.length() == 0) {
                    queryCondition.append(" WHERE");
                } else {
                    queryCondition.append(" AND");
                }
                queryCondition.append(" state <>?");
                parameterList.add(Integer.valueOf(AS2Message.STATE_FINISHED));
            }
            if (!filter.isShowPending()) {
                if (queryCondition.length() == 0) {
                    queryCondition.append(" WHERE");
                } else {
                    queryCondition.append(" AND");
                }
                queryCondition.append(" state <>?");
                parameterList.add(Integer.valueOf(AS2Message.STATE_PENDING));
            }
            if (!filter.isShowStopped()) {
                if (queryCondition.length() == 0) {
                    queryCondition.append(" WHERE");
                } else {
                    queryCondition.append(" AND");
                }
                queryCondition.append(" state <>?");
                parameterList.add(Integer.valueOf(AS2Message.STATE_STOPPED));
            }
            if (filter.getShowDirection() != MessageOverviewFilter.DIRECTION_ALL) {
                if (queryCondition.length() == 0) {
                    queryCondition.append(" WHERE");
                } else {
                    queryCondition.append(" AND");
                }
                queryCondition.append(" direction=?");
                parameterList.add(Integer.valueOf(filter.getShowDirection()));
            }
            if (filter.getShowMessageType() != MessageOverviewFilter.MESSAGETYPE_ALL) {
                if (queryCondition.length() == 0) {
                    queryCondition.append(" WHERE");
                } else {
                    queryCondition.append(" AND");
                }
                queryCondition.append(" messagetype=?");
                parameterList.add(Integer.valueOf(filter.getShowMessageType()));
            }
            String query = "SELECT * FROM messages" + queryCondition.toString() + " ORDER BY initdate ASC";
            statement = this.runtimeConnection.prepareStatement(query);
            for (int i = 0; i < parameterList.size(); i++) {
                if (parameterList.get(i) instanceof Integer) {
                    statement.setInt(i + 1, ((Integer) parameterList.get(i)).intValue());
                } else {
                    statement.setString(i + 1, (String) parameterList.get(i));
                }
            }
            result = statement.executeQuery();
            while (result.next()) {
                AS2MessageInfo info = new AS2MessageInfo();
                info.setInitDate(result.getTimestamp("initdate"));
                info.setEncryptionType(result.getInt("encryption"));
                info.setDirection(result.getInt("direction"));
                info.setMessageType(result.getInt("messagetype"));
                info.setMessageId(result.getString("messageid"));
                info.setRawFilename(result.getString("rawfilename"));
                info.setReceiverId(result.getString("receiverid"));
                info.setSenderId(result.getString("senderid"));
                info.setSignType(result.getInt("signature"));
                info.setState(result.getInt("state"));
                info.setRequestsSyncMDN(result.getInt("syncmdn") == 1);
                info.setHeaderFilename(result.getString("headerfilename"));
                info.setRawFilenameDecrypted(result.getString("rawdecryptedfilename"));
                info.setSenderHost(result.getString("senderhost"));
                info.setUserAgent(result.getString("useragent"));
                info.setReceivedContentMIC(result.getString("contentmic"));
                info.setCompressionType(result.getInt("compression"));
                info.setAsyncMDNURL(result.getString("asyncmdnurl"));
                info.setSubject(result.getString("subject"));
                info.setResendCounter(result.getInt("resendcounter"));
                messageList.add(info);
            }
        } catch (Exception e) {
            this.logger.severe("MessageAccessDB.getMessageOverview: " + e.getMessage());
            Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
        } finally {
            if (result != null) {
                try {
                    result.close();
                } catch (Exception e) {
                    this.logger.severe("MessageAccessDB.getMessageOverview: " + e.getMessage());
                    Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (Exception e) {
                    this.logger.severe("MessageAccessDB.getMessageOverview: " + e.getMessage());
                    Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
                }
            }
        }
        return (messageList);
    }

    /**
     * Returns all file names of files that could be deleted for a passed
     * message
     *info
     */
    public List<String> getRawFilenamesToDelete(AS2MessageInfo info) {
        List<String> list = new ArrayList<String>();
        ResultSet result = null;
        PreparedStatement statement = null;
        try {
            String query = "SELECT * FROM messages WHERE messageid=?";
            statement = this.runtimeConnection.prepareStatement(query);
            statement.setString(1, info.getMessageId());
            result = statement.executeQuery();
            while (result.next()) {
                String rawFilename = result.getString("rawfilename");
                if (!result.wasNull()) {
                    list.add(rawFilename);
                }
                String rawFilenameDecrypted = result.getString("rawdecryptedfilename");
                if (!result.wasNull()) {
                    list.add(rawFilenameDecrypted);
                }
                String headerFilename = result.getString("headerfilename");
                if (!result.wasNull()) {
                    list.add(headerFilename);
                }
            }
        } catch (Exception e) {
            this.logger.severe("MessageAccessDB.getRawFilenamesToDelete: " + e.getMessage());
            Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
        } finally {
            if (result != null) {
                try {
                    result.close();
                } catch (Exception e) {
                    this.logger.severe("MessageAccessDB.getRawFilenamesToDelete: " + e.getMessage());
                    Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (Exception e) {
                    this.logger.severe("MessageAccessDB.getRawFilenamesToDelete: " + e.getMessage());
                    Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
                }
            }
        }
        MDNAccessDB mdnAccess = new MDNAccessDB(this.configConnection, this.runtimeConnection);
        list.addAll(mdnAccess.getRawFilenamesToDelete(info.getMessageId()));
        return (list);
    }

    /**
     * Deletes messages and MDNs of the passed id
     */
    public void deleteMessage(String messageId) {
        PreparedStatement statement = null;
        try {
            if (messageId != null) {
                statement = this.runtimeConnection.prepareStatement("DELETE FROM mdn WHERE relatedmessageid=?");
                statement.setString(1, messageId);
                statement.execute();
                statement.close();
                statement = this.runtimeConnection.prepareStatement("DELETE FROM payload WHERE messageid=?");
                statement.setString(1, messageId);
                statement.execute();
                statement.close();
                statement = this.runtimeConnection.prepareStatement("DELETE FROM messages WHERE messageid=?");
                statement.setString(1, messageId);
                statement.execute();
            } else {
                statement = this.runtimeConnection.prepareStatement("DELETE FROM payload WHERE messageid IS NULL");
                statement.execute();
                statement.close();
                statement = this.runtimeConnection.prepareStatement("DELETE FROM messages WHERE messageid IS NULL");
                statement.execute();
            }
        } catch (SQLException e) {
            this.logger.severe("MessageAccessDB.deleteMessage: " + e.getMessage());
            Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (Exception e) {
                    this.logger.severe("MessageAccessDB.deleteMessage: " + e.getMessage());
                    Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
                }
            }
        }
    }

    /**
     * Deletes messages and MDNs of the passed id
     */
    public void deleteMessage(AS2MessageInfo info) {
        this.deleteMessage(info.getMessageId());
    }

    /**
     * Updates a message entry in the database, only the filenames
     */
    public void setMessageSendDate(AS2MessageInfo info) {
        PreparedStatement statement = null;
        try {
            statement = this.runtimeConnection.prepareStatement(
                    "UPDATE messages SET senddate=? WHERE messageid=?");
            statement.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            //WHERE
            statement.setString(2, info.getMessageId());
            statement.executeUpdate();
        } catch (SQLException e) {
            this.logger.severe("MessageAccessDB.setMessageSendDate: " + e.getMessage());
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

    /**
     * Updates a message entry in the database, only the filenames
     */
    public void updateFilenames(AS2MessageInfo info) {
        PreparedStatement statement = null;
        try {
            statement = this.runtimeConnection.prepareStatement(
                    "UPDATE messages SET rawfilename=?,headerfilename=?,rawdecryptedfilename=? WHERE messageid=?");
            statement.setString(1, info.getRawFilename());
            statement.setString(2, info.getHeaderFilename());
            statement.setString(3, info.getRawFilenameDecrypted());
            //WHERE
            statement.setString(4, info.getMessageId());
            statement.executeUpdate();
        } catch (SQLException e) {
            this.logger.severe("MessageAccessDB.updateFilenames: " + e.getMessage());
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

    public void insertPayload(String messageId, AS2Payload[] payload) {
        if (payload == null) {
            return;
        }
        this.insertPayload(messageId, Arrays.asList(payload));
    }

    /**
     * Writes the payload and original filenames to the database, deleting all
     * entries first (only if a payload has been passed)
     */
    public void insertPayload(String messageId, List<AS2Payload> payload) {
        if (payload == null || payload.isEmpty()) {
            return;
        }
        PreparedStatement statement = null;
        try {
            statement = this.runtimeConnection.prepareStatement("DELETE FROM payload WHERE messageid=?");
            statement.setString(1, messageId);
            statement.execute();
            statement.close();
            //insert
            for (int i = 0; i < payload.size(); i++) {
                statement = this.runtimeConnection.prepareStatement("INSERT INTO payload(messageid,originalfilename,payloadfilename,contentid,contenttype)VALUES(?,?,?,?,?)");
                statement.setString(1, messageId);
                statement.setString(2, payload.get(i).getOriginalFilename());
                statement.setString(3, payload.get(i).getPayloadFilename());
                statement.setString(4, payload.get(i).getContentId());
                statement.setString(5, payload.get(i).getContentType());
                statement.execute();
                statement.close();
            }

        } catch (SQLException e) {
            this.logger.severe("MessageAccessDB.insertPayload: " + e.getMessage());
            Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (Exception e) {
                    this.logger.severe("MessageAccessDB.insertPayload: " + e.getMessage());
                    Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
                }
            }
        }
    }

    /**
     * Initializes or updates a messages in the database. If the message id
     * already exists it is updated
     *
     * @param message
     */
    public void initializeOrUpdateMessage(AS2MessageInfo info) {
        AS2MessageInfo testInfo = this.getLastMessageEntry(info.getMessageId());
        if (testInfo == null) {
            this.initializeMessage(info);
        } else {
            this.updateMessage(info);
        }
    }

    /**
     * Initializes a messages in the database.
     */
    private void initializeMessage(AS2MessageInfo info) {
        PreparedStatement statement = null;
        try {
            statement = this.runtimeConnection.prepareStatement(
                    "INSERT INTO messages(initdate,encryption,direction,messageid,rawfilename,receiverid,senderid,"
                    + "signature,state,syncmdn,headerfilename,rawdecryptedfilename,senderhost,useragent,"
                    + "contentmic,compression,messagetype,asyncmdnurl,subject)VALUES("
                    + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
            statement.setTimestamp(1, new java.sql.Timestamp(info.getInitDate().getTime()));
            statement.setInt(2, info.getEncryptionType());
            statement.setInt(3, info.getDirection());
            statement.setString(4, info.getMessageId());
            statement.setString(5, info.getRawFilename());
            statement.setString(6, info.getReceiverId());
            statement.setString(7, info.getSenderId());
            statement.setInt(8, info.getSignType());
            statement.setInt(9, info.getState());
            statement.setInt(10, info.requestsSyncMDN() ? 1 : 0);
            statement.setString(11, info.getHeaderFilename());
            statement.setString(12, info.getRawFilenameDecrypted());
            statement.setString(13, info.getSenderHost());
            statement.setString(14, info.getUserAgent());
            statement.setString(15, info.getReceivedContentMIC());
            statement.setInt(16, info.getCompressionType());
            statement.setInt(17, info.getMessageType());
            statement.setString(18, info.getAsyncMDNURL());
            statement.setString(19, info.getSubject());
            statement.executeUpdate();
            //insert payload and inc transaction counter
            AS2Message message = new AS2Message(info);
            this.insertPayload(info.getMessageId(), message.getPayloads());
            AS2Server.incTransactionCounter();
        } catch (SQLException e) {
            this.logger.severe("MessageAccessDB.initializeMessage: " + e.getMessage());
            Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (Exception e) {
                    this.logger.severe("MessageAccessDB.initializeMessage: " + e.getMessage());
                    Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
                }
            }
        }
    }

    /**
     * Updates the subject of a message
     */
    public void updateSubject(AS2MessageInfo info) {
        PreparedStatement statement = null;
        try {
            statement = this.runtimeConnection.prepareStatement(
                    "UPDATE messages SET subject=? WHERE messageid=?");
            statement.setString(1, info.getSubject());
            //condition
            statement.setString(2, info.getMessageId());
            statement.executeUpdate();
        } catch (SQLException e) {
            this.logger.severe("MessageAccessDB.updateSubject: " + e.getMessage());
            Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (Exception e) {
                    this.logger.severe("MessageAccessDB.updateSubject: " + e.getMessage());
                    Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
                }
            }
        }
    }

    public void updateResendCounter(AS2MessageInfo info) {
        PreparedStatement statement = null;
        try {
            statement = this.runtimeConnection.prepareStatement(
                    "UPDATE messages SET resendcounter=? WHERE messageid=?");
            statement.setInt(1, info.getResendCounter());
            //condition
            statement.setString(2, info.getMessageId());
            statement.executeUpdate();
        } catch (SQLException e) {
            this.logger.severe("MessageAccessDB.updateResendCounter: " + e.getMessage());
            Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (Exception e) {
                    this.logger.severe("MessageAccessDB.updateResendCounter: " + e.getMessage());
                    Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
                }
            }
        }
    }

    /**
     * Inserts a new message entry into the database
     */
    private void updateMessage(AS2MessageInfo info) {
        PreparedStatement statement = null;
        try {
            statement = this.runtimeConnection.prepareStatement(
                    "UPDATE messages SET encryption=?,direction=?,rawfilename=?,receiverid=?,"
                    + "senderid=?,signature=?,state=?,syncmdn=?,headerfilename=?,useragent=?,"
                    + "rawdecryptedfilename=?,senderhost=?,"
                    + "contentmic=?,compression=?,messagetype=?,asyncmdnurl=?,subject=?"
                    + " WHERE messageid=?");
            statement.setInt(1, info.getEncryptionType());
            statement.setInt(2, info.getDirection());
            statement.setString(3, info.getRawFilename());
            statement.setString(4, info.getReceiverId());
            statement.setString(5, info.getSenderId());
            statement.setInt(6, info.getSignType());
            statement.setInt(7, info.getState());
            statement.setInt(8, info.requestsSyncMDN() ? 1 : 0);
            statement.setString(9, info.getHeaderFilename());
            statement.setString(10, info.getUserAgent());
            statement.setString(11, info.getRawFilenameDecrypted());
            statement.setString(12, info.getSenderHost());
            statement.setString(13, info.getReceivedContentMIC());
            statement.setInt(14, info.getCompressionType());
            statement.setInt(15, info.getMessageType());
            statement.setString(16, info.getAsyncMDNURL());
            statement.setString(17, info.getSubject());
            //condition
            statement.setString(18, info.getMessageId());
            statement.executeUpdate();
            //insert payload and inc transaction counter
            AS2Message message = new AS2Message(info);
            this.insertPayload(info.getMessageId(), message.getPayloads());
            AS2Server.incTransactionCounter();

        } catch (SQLException e) {
            this.logger.severe("MessageAccessDB.updateMessage: " + e.getMessage());
            Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (Exception e) {
                    this.logger.severe("MessageAccessDB.updateMessage: " + e.getMessage());
                    Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
                }
            }
        }
    }

    /**
     * Returns a list of all messages that are older than the passed timestamp
     *
     * @param state pass -1 for any state else only messages of the requested
     * state are returned
     */
    public List<AS2MessageInfo> getMessagesSendOlderThan(long initTimestamp) {
        List<AS2MessageInfo> messageList = new ArrayList<AS2MessageInfo>();
        ResultSet result = null;
        PreparedStatement statement = null;
        try {
            String query = "SELECT * FROM messages WHERE (senddate IS NOT NULL) AND senddate<? AND state=?";
            statement = this.runtimeConnection.prepareStatement(query);
            statement.setTimestamp(1, new java.sql.Timestamp(initTimestamp));
            statement.setInt(2, AS2Message.STATE_PENDING);
            result = statement.executeQuery();
            while (result.next()) {
                AS2MessageInfo info = new AS2MessageInfo();
                info.setInitDate(result.getTimestamp("initdate"));
                info.setEncryptionType(result.getInt("encryption"));
                info.setDirection(result.getInt("direction"));
                info.setMessageType(result.getInt("messagetype"));
                info.setMessageId(result.getString("messageid"));
                info.setRawFilename(result.getString("rawfilename"));
                info.setReceiverId(result.getString("receiverid"));
                info.setSenderId(result.getString("senderid"));
                info.setSignType(result.getInt("signature"));
                info.setState(result.getInt("state"));
                info.setRequestsSyncMDN(result.getInt("syncmdn") == 1);
                info.setHeaderFilename(result.getString("headerfilename"));
                info.setRawFilenameDecrypted(result.getString("rawdecryptedfilename"));
                info.setSenderHost(result.getString("senderhost"));
                info.setUserAgent(result.getString("useragent"));
                info.setReceivedContentMIC(result.getString("contentmic"));
                info.setCompressionType(result.getInt("compression"));
                info.setAsyncMDNURL(result.getString("asyncmdnurl"));
                info.setSubject(result.getString("subject"));
                info.setResendCounter(result.getInt("resendcounter"));
                messageList.add(info);
            }
        } catch (Exception e) {
            this.logger.severe("MessageAccessDB.getMessagesSendOlderThan: " + e.getMessage());
            Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
        } finally {
            if (result != null) {
                try {
                    result.close();
                } catch (Exception e) {
                    this.logger.severe("MessageAccessDB.getMessagesSendOlderThan: " + e.getMessage());
                    Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (Exception e) {
                    this.logger.severe("MessageAccessDB.getMessagesSendOlderThan: " + e.getMessage());
                    Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
                }
            }
        }
        return (messageList);
    }

    /**
     * Returns a list of all messages that are older than the passed timestamp
     *
     * @param state pass -1 for any state else only messages of the requested
     * state are returned
     */
    public List<AS2MessageInfo> getMessagesOlderThan(long initTimestamp, int state) {
        List<AS2MessageInfo> messageList = new ArrayList<AS2MessageInfo>();
        ResultSet result = null;
        PreparedStatement statement = null;
        try {
            String query = "SELECT * FROM messages WHERE initdate < ?";
            if (state != -1) {
                query = query + " AND state=" + state;
            }
            statement = this.runtimeConnection.prepareStatement(query);
            statement.setTimestamp(1, new java.sql.Timestamp(initTimestamp));
            result = statement.executeQuery();
            while (result.next()) {
                AS2MessageInfo info = new AS2MessageInfo();
                info.setInitDate(result.getTimestamp("initdate"));
                info.setEncryptionType(result.getInt("encryption"));
                info.setDirection(result.getInt("direction"));
                info.setMessageType(result.getInt("messagetype"));
                info.setMessageId(result.getString("messageid"));
                info.setRawFilename(result.getString("rawfilename"));
                info.setReceiverId(result.getString("receiverid"));
                info.setSenderId(result.getString("senderid"));
                info.setSignType(result.getInt("signature"));
                info.setState(result.getInt("state"));
                info.setRequestsSyncMDN(result.getInt("syncmdn") == 1);
                info.setHeaderFilename(result.getString("headerfilename"));
                info.setRawFilenameDecrypted(result.getString("rawdecryptedfilename"));
                info.setSenderHost(result.getString("senderhost"));
                info.setUserAgent(result.getString("useragent"));
                info.setReceivedContentMIC(result.getString("contentmic"));
                info.setCompressionType(result.getInt("compression"));
                info.setAsyncMDNURL(result.getString("asyncmdnurl"));
                info.setSubject(result.getString("subject"));
                info.setResendCounter(result.getInt("resendcounter"));
                messageList.add(info);
            }
            statement.close();
        } catch (Exception e) {
            this.logger.severe("MessageAccessDB.getMessagesOlderThan: " + e.getMessage());
            Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
        } finally {
            if (result != null) {
                try {
                    result.close();
                } catch (Exception e) {
                    this.logger.severe("MessageAccessDB.getMessagesOlderThan: " + e.getMessage());
                    Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (Exception e) {
                    this.logger.severe("MessageAccessDB.getMessagesOlderThan: " + e.getMessage());
                    Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
                }
            }
        }
        return (messageList);
    }
}
