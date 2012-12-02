//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/database/SQLScriptExecutor.java,v 1.1 2012/04/18 14:10:29 heller Exp $
package de.mendelson.comm.as2.database;

import de.mendelson.comm.as2.AS2ServerVersion;
import de.mendelson.comm.as2.server.AS2Server;
import de.mendelson.util.ConsoleProgressBar;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */

/**
 * Class that can execute SQL scripts or execute predefined
 * commands which are assigned to scripts
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class SQLScriptExecutor {

   private Logger logger = Logger.getLogger(AS2Server.SERVER_LOGGER_NAME);
    /**Directory where the SQL scripts are found*/
    public static final String SCRIPT_RESOURCE_CONFIG = "/sqlscript/config/";
    public static final String SCRIPT_RESOURCE_RUNTIME = "/sqlscript/runtime/";

    /** Creates new SQLScriptExecutor */
    public SQLScriptExecutor() {
    }

    /**creates a new database
     * @param connection connection to the database
     * @param RESOURCE resource type as defined in the class 
     */
    public void create(Connection connection, final String RESOURCE, int version) throws Exception {
        PreparedStatement statement = null;
        try {
            this.executeScript(connection, RESOURCE + "CREATE.sql");
            //request all connections from the database to store them
            statement = connection.prepareStatement(
                    "INSERT INTO version(actualVersion,updateDate,updateComment)" + "VALUES(?,?,?)");
            statement.setEscapeProcessing(true);
            //fill in values
            statement.setInt(1, version);
            statement.setDate(2, new java.sql.Date(System.currentTimeMillis()));
            statement.setString(3, AS2ServerVersion.getProductName() 
                    + " " + AS2ServerVersion.getVersion() + " " 
                    + AS2ServerVersion.getBuild() + ": initial installation");
            statement.executeUpdate();
            statement.close();
        } finally {
            if (statement != null) {
                statement.close();
            }
        }
    }

    /**Checks if the resource exist
     * @param resource Resource to check for existence
     */
    public boolean resourceExists(String resource) {
        InputStream is = SQLScriptExecutor.class.getResourceAsStream(resource);
        try {
            return (is != null);
        } finally {
            if (is != null) {
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (Exception e) {
                    //nop
                }
            }
        }
    }

    /**Executes a SQL script to make changes to the database
     * @param resource FULL Resource of the sql script, e.g. "/sqlscript/config/script.sql"
     * @param connection connection to the database
     *@return true if everything worked fine
     */
    public void executeScript(Connection connection, String resource) throws Exception {
        InputStream is = null;
        try {
            is = SQLScriptExecutor.class.getResourceAsStream(resource);
            if (is == null) {
                String text = "SQLScriptExecutor: Resource " + resource + " not found";
                throw new Exception(text);
            }
            this.executeScript(connection, is);
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    /**Executes a SQL script to make changes to the database
     * @param resource FULL Resource of the sql script, e.g. "/sqlscript/config/script.sql"
     * @param connection connection to the database
     *@return true if everything worked fine
     */
    private void executeScript(Connection connection, InputStream is) throws Exception {
        ConsoleProgressBar.print(0f);
        List<String> queryList = new ArrayList<String>();
        String line = "";
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        while (line != null) {
            line = reader.readLine();
            if (line != null && line.trim().length() > 0 && (!line.startsWith("#"))) {
                queryList.add(line);
            }
        }
        Statement statement = null;
        String lastQuery = "";
        try {
            int counter = 0;
            statement = connection.createStatement();
            for (String query : queryList) {
                counter++;
                lastQuery = query;
                float percent = ((float) counter / (float) queryList.size()) * 100f;
                ConsoleProgressBar.print(percent);
                statement.executeUpdate(query);
            }
        } finally {
            if (statement != null) {
                statement.close();
            }
            System.out.println();
        }
    }
}