//$Header: /as2/sqlscript/runtime/Update26to27.java 2     11.11.11 12:14 Heller $
package sqlscript.runtime;

import de.mendelson.comm.as2.database.IUpdater;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 *
 * Update the database from version 26 to version 27
 * @author S.Heller
 * @version $Revision: 2 $
 * @since build 128
 */
public class Update26to27 implements IUpdater {

    /**Store if this was a successfully operation*/
    private boolean success = false;

    /** Return if the update was successfully */
    @Override
    public boolean updateWasSuccessfully() {
        return (this.success);
    }

    /** Starts the update process */
    @Override
    public void startUpdate(Connection connection) throws Exception {
        //transfer the mdns to the new mdn table
        //create the new table
        Statement statement = connection.createStatement();
        statement.execute("CREATE CACHED TABLE mdn(messageid VARCHAR(255) PRIMARY KEY,relatedmessageid VARCHAR(255),messagedate TIMESTAMP,direction INTEGER,rawfilename VARCHAR(512), state INTEGER, signature INTEGER, senderid VARCHAR(255),receiverid VARCHAR(255), headerfilename VARCHAR(512),senderhost VARCHAR(255),useragent VARCHAR(255),mdntext OBJECT,FOREIGN KEY(relatedmessageid)REFERENCES messages(messageid))");
        statement.execute("CREATE INDEX idx_mdn_messagedate ON mdn(messagedate)");
        //now move the mdns to the new table
        ResultSet result = statement.executeQuery("SELECT * FROM messages WHERE relatedmessageid IS NOT NULL");
        ArrayList<String> relatedList = new ArrayList<String>();
        while (result.next()) {
            String relatedMessageId = result.getString("relatedmessageid");
            //check id this MDN is linked
            if (this.messageExists(connection, relatedMessageId)) {
                //build up insert statement
                PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO mdn(messageid,relatedmessageid,messagedate,direction,rawfilename,state,signature,senderid,receiverid,headerfilename,senderhost,useragent)VALUES"
                        + "(?,?,?,?,?,?,?,?,?,?,?,?)");
                insertStatement.setString(1, result.getString("messageid"));
                insertStatement.setString(2, result.getString("relatedmessageid"));
                insertStatement.setTimestamp(3, result.getTimestamp("messagedate"));
                insertStatement.setInt(4, result.getInt("direction"));
                insertStatement.setString(5, result.getString("rawfilename"));
                insertStatement.setInt(6, result.getInt("state"));
                insertStatement.setInt(7, result.getInt("signature"));
                insertStatement.setString(8, result.getString("senderid"));
                insertStatement.setString(9, result.getString("receiverid"));
                insertStatement.setString(10, result.getString("headerfilename"));
                insertStatement.setString(11, result.getString("senderhost"));
                insertStatement.setString(12, result.getString("useragent"));
                insertStatement.execute();
                insertStatement.close();
            }
            relatedList.add(relatedMessageId);
        }
        result.close();
        for (String messageId : relatedList) {
            PreparedStatement deleteStatement = connection.prepareStatement("DELETE FROM messages WHERE relatedmessageid=?");
            deleteStatement.setString(1, messageId);
            deleteStatement.execute();
            deleteStatement.close();
        }
        statement.execute("ALTER TABLE messages DROP COLUMN relatedmessageid");
        statement.execute("ALTER TABLE messages ADD COLUMN asyncmdnurl VARCHAR(512)");
        statement.execute("ALTER TABLE messages DROP COLUMN statefilename");
        statement.close();
        this.success = true;
    }

    /**Does the message exist?*/
    private boolean messageExists(Connection connection, String messageId) throws Exception {
        ResultSet result = null;
        PreparedStatement statement = null;
        try {
            //desc because we need the latest
            statement = connection.prepareStatement("SELECT COUNT(1) AS counter FROM messages WHERE messageid=? AND relatedmessageid IS NULL");
            statement.setEscapeProcessing(true);
            statement.setString(1, messageId);
            result = statement.executeQuery();
            if (result.next()) {
                int counter = result.getInt("counter");
                return (counter > 0);
            }
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
        return (false);
    }
}


