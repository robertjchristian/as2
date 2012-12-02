//$Header: /as2/sqlscript/runtime/Update21to22.java 2     11.11.11 12:14 Heller $
package sqlscript.runtime;

import de.mendelson.comm.as2.database.IUpdater;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
 * Update the database from version21 to version 22: create secondary keys
 * @author S.Heller
 * @version $Revision: 2 $
 * @since build 128
 */
public class Update21to22 implements IUpdater {

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
        //set new primary key for messages table
        Statement statement = connection.createStatement();
        ResultSet result = statement.executeQuery("SELECT messageid,COUNT(messageid)AS idcount FROM messages GROUP BY messageid HAVING(COUNT(messageid)>1)");
        ArrayList<String> messageIdList = new ArrayList<String>();
        while (result.next()) {
            messageIdList.add(result.getString("messageid"));
        }
        result.close();
        statement.close();
        this.deleteDuplicateMessages(connection, messageIdList);
        statement = connection.createStatement();
        statement.executeQuery("ALTER TABLE messages DROP COLUMN id");
        statement.executeQuery("ALTER TABLE messages ADD PRIMARY KEY(messageid)");
        statement.close();
        statement = connection.createStatement();
        //find all unreferenced messagelog entries and delete them, then create a secondary key on the table
        result = statement.executeQuery("SELECT messagelog.messageid FROM messagelog LEFT OUTER JOIN messages ON (messagelog.messageid = messages.messageid) WHERE messages.messageid IS NULL");
        messageIdList.clear();
        while (result.next()) {
            messageIdList.add(result.getString("messageid"));
        }
        result.close();
        statement.close();
        String[] idsToDelete = new String[messageIdList.size()];
        messageIdList.toArray(idsToDelete);
        int position = 0;
        String[] splittedIds = null;
        //do not use more than 20 transaction ids per delete attempt, the transaction id could be very long
        //and this will result in a huge sql statement
        int splitSize = 20;
        while (position != idsToDelete.length) {
            int splitLength = Math.min(splitSize, idsToDelete.length - position);
            splittedIds = new String[splitLength];
            System.arraycopy(idsToDelete, position, splittedIds, 0, splitLength);
            this.deleteMessageLogEntries(connection, splittedIds);
            position += splitLength;
        }
        //add the secondary key
        statement = connection.createStatement();
        statement.execute("ALTER TABLE messagelog ADD FOREIGN KEY(messageid)REFERENCES messages(messageid)");
        statement.close();
        statement = connection.createStatement();
        ////find all unreferenced payload entries and delete them, then create a secondary key on the table
        result = statement.executeQuery("SELECT payload.messageid FROM payload LEFT OUTER JOIN messages ON (payload.messageid = messages.messageid) WHERE messages.messageid IS NULL");
        ArrayList<String> payloadIdList = new ArrayList<String>();
        while (result.next()) {
            payloadIdList.add(result.getString("messageid"));
        }
        result.close();
        statement.close();
        idsToDelete = new String[payloadIdList.size()];
        payloadIdList.toArray(idsToDelete);
        position = 0;
        splittedIds = null;
        //do not use more than 20 transaction ids per delete attempt, the transaction id could be very long
        //and this will result in a huge sql statement
        splitSize = 20;
        while (position != idsToDelete.length) {
            int splitLength = Math.min(splitSize, idsToDelete.length - position);
            splittedIds = new String[splitLength];
            System.arraycopy(idsToDelete, position, splittedIds, 0, splitLength);
            this.deletePayloadEntries(connection, splittedIds);
            position += splitLength;
        }
        //add the secondary key
        statement = connection.createStatement();
        statement.executeQuery("ALTER TABLE payload ADD COLUMN id INTEGER IDENTITY PRIMARY KEY");
        statement.execute("ALTER TABLE payload ADD FOREIGN KEY(messageid)REFERENCES messages(messageid)");
        statement.close();
        this.success = true;
    }

    /**Deletes duplicate messageid entries from the database*/
    private void deleteDuplicateMessages(Connection connection, ArrayList<String> messageIdList) throws SQLException {
        for (String messageId : messageIdList) {
            PreparedStatement statement = null;
            statement = connection.prepareStatement("DELETE FROM messages WHERE messageid=?");
            statement.setString(1, messageId);
            statement.execute();
            statement.close();
        }
    }

    private void deleteMessageLogEntries(Connection connection, String[] transactionsToDelete) throws Exception {
        if (transactionsToDelete == null || transactionsToDelete.length == 0) {
            return;
        }
        PreparedStatement statement = null;
        try {
            StringBuilder condition = new StringBuilder("WHERE messageid IN(");
            for (int i = 0; i < transactionsToDelete.length; i++) {
                if (i > 0) {
                    condition.append(",");
                }
                condition.append("?");
            }
            condition.append(")");
            statement = connection.prepareStatement("DELETE FROM messagelog " + condition.toString());
            statement.setEscapeProcessing(true);
            for (int i = 0; i < transactionsToDelete.length; i++) {
                statement.setString(i + 1, transactionsToDelete[i]);
            }
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void deletePayloadEntries(Connection connection, String[] transactionsToDelete) throws Exception {
        if (transactionsToDelete == null || transactionsToDelete.length == 0) {
            return;
        }
        PreparedStatement statement = null;
        try {
            StringBuilder condition = new StringBuilder("WHERE messageid IN(");
            for (int i = 0; i < transactionsToDelete.length; i++) {
                if (i > 0) {
                    condition.append(",");
                }
                condition.append("?");
            }
            condition.append(")");
            statement = connection.prepareStatement("DELETE FROM payload " + condition.toString());
            statement.setEscapeProcessing(true);
            for (int i = 0; i < transactionsToDelete.length; i++) {
                statement.setString(i + 1, transactionsToDelete[i]);
            }
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
