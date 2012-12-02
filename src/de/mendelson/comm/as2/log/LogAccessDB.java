//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/log/LogAccessDB.java,v 1.1 2012/04/18 14:10:30 heller Exp $
package de.mendelson.comm.as2.log;

import de.mendelson.comm.as2.notification.Notification;
import de.mendelson.comm.as2.server.AS2Server;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
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
 * Access to the PIP log that stores log messages for every PIP
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class LogAccessDB {

    private int LEVEL_FINE = 3;
    private int LEVEL_SEVERE = 2;
    private int LEVEL_WARNING = 1;
    private int LEVEL_INFO = 0;
    /**Logger to log inforamtion to*/
    private Logger logger = Logger.getLogger(AS2Server.SERVER_LOGGER_NAME);
    /**Connection to the database*/
    private Connection runtimeConnection;
    private Connection configConnection;

    /**
     *@param host host to connect to
     */
    public LogAccessDB(Connection configConnection, Connection runtimeConnection) {
        this.runtimeConnection = runtimeConnection;
        this.configConnection = configConnection;
    }

    private int convertLevel(Level level) {
        if (level.equals(Level.WARNING)) {
            return (this.LEVEL_WARNING);
        }
        if (level.equals(Level.SEVERE)) {
            return (this.LEVEL_SEVERE);
        }
        if (level.equals(Level.FINE)) {
            return (this.LEVEL_FINE);
        }
        return (this.LEVEL_INFO);
    }

    private Level convertLevel(int level) {
        if (level == this.LEVEL_WARNING) {
            return (Level.WARNING);
        }
        if (level == this.LEVEL_SEVERE) {
            return (Level.SEVERE);
        }
        if (level == this.LEVEL_FINE) {
            return (Level.FINE);
        }
        return (Level.INFO);
    }

    /**Adds a log line to the db*/
    public void log(Level level, long millis, String message, String messageId) {
        if (message == null) {
            return;
        }
        try {
            PreparedStatement statement = this.runtimeConnection.prepareStatement(
                    "INSERT INTO messagelog(timestamp,messageid,loglevel,details)VALUES(?,?,?,?)");
            statement.setEscapeProcessing(true);
            statement.setTimestamp(1, new Timestamp(millis));
            statement.setString(2, messageId);
            statement.setInt(3, this.convertLevel(level));
            if (message == null) {
                statement.setNull(4, Types.JAVA_OBJECT);
            } else {
                statement.setObject(4, message);
            }
            statement.execute();
            statement.close();
        } catch (Exception e) {
            this.logger.severe("LogAccessDB.log: " + e.getMessage());
            Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
            e.printStackTrace();
        }
    }

    /**Returns the whole log of a single instance
     */
    public LogEntry[] getLog(String messageId) {
        List<LogEntry> list = new ArrayList<LogEntry>();
        try {
            PreparedStatement statement = this.runtimeConnection.prepareStatement("SELECT * FROM messagelog WHERE messageid=? ORDER BY timestamp");
            statement.setString(1, messageId);
            ResultSet result = statement.executeQuery();
            while (result.next()) {
                LogEntry entry = new LogEntry();
                entry.setLevel(this.convertLevel(result.getInt("loglevel")));
                Object detailsObj = result.getObject("details");
                if (!result.wasNull() ){
                    if( detailsObj instanceof String){
                        entry.setMessage((String)detailsObj);
                    }else if( detailsObj instanceof byte[]){
                        //just for compatibility reasons for an update to hsqldb 2.x
                        entry.setMessage(new String((byte[])detailsObj));
                    }
                }
                entry.setMessageId(messageId);
                entry.setMillis(result.getTimestamp("timestamp").getTime());
                list.add(entry);
            }
            statement.close();
        } catch (Exception e) {
            this.logger.severe("LogAccessDB.getLog: " + e.getMessage());
            Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
        }
        LogEntry[] logArray = new LogEntry[list.size()];
        list.toArray(logArray);
        return (logArray);
    }

    /**Deletes all information from the table messagelog
     *regarding the passed message instance
     */
    public void deleteMessageLog(String messageId) {
        Statement statement = null;
        try {
            statement = this.runtimeConnection.createStatement();
            statement.setEscapeProcessing(true);
            //check if the number of entires have changed since last request
            String query = null;
            if (messageId != null) {
                query = "DELETE FROM messagelog WHERE messageid='" + messageId + "'";
            } else {
                query = "DELETE FROM messagelog WHERE messageid IS NULL";
            }
            statement.execute(query);
        } catch (Exception e) {
            this.logger.severe("deleteMessageLog: " + e.getMessage());
            Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (Exception e) {
                    this.logger.severe("deleteMessageLog: " + e.getMessage());
                    Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
                }
            }
        }
    }
}
