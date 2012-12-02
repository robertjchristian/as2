//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/database/DBServer.java,v 1.1 2012/04/18 14:10:29 heller Exp $
package de.mendelson.comm.as2.database;

import de.mendelson.comm.as2.AS2ServerVersion;
import de.mendelson.comm.as2.preferences.PreferencesAS2;
import de.mendelson.comm.as2.server.AS2Server;
import de.mendelson.comm.as2.server.UpgradeRequiredException;
import de.mendelson.util.MecResourceBundle;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import org.hsqldb.Server;
import org.hsqldb.persist.HsqlProperties;
import org.hsqldb.server.ServerConstants;
/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */

/**
 * Class to start a dedicated SQL database server
 * @author S.Heller
 * @version $Revision: 1.1 $
 * @since build 70
 */
public class DBServer {

    /**Resourcebundle to localize messages of the DB server*/
    private static MecResourceBundle rb;
    /**Log messages*/
    private Logger logger = Logger.getLogger(AS2Server.SERVER_LOGGER_NAME);
    /**Database object*/
    private Server server = null;
    private PreferencesAS2 preferences = new PreferencesAS2();

    static {
        try {
            rb = (MecResourceBundle) ResourceBundle.getBundle(
                    ResourceBundleDBServer.class.getName());
        } //load up resourcebundle
        catch (MissingResourceException e) {
            throw new RuntimeException("Oops..resource bundle " + e.getClassName() + " not found.");
        }
    }

    /**Start a dedicated database server
     */
    public DBServer() throws Exception {
        //split up database if its an older version with a single DB
        this.createDeprecatedCheck();
        //check if hsqldb 2.x is used or an older version
        this.checkDBUpgradeRequired();
    }

    private void checkDBUpgradeRequired() throws UpgradeRequiredException, Exception {
        File propertiesFileConfig = new File(DBDriverManager.getDBName(DBDriverManager.DB_CONFIG) + ".properties");
        File propertiesFileRuntime = new File(DBDriverManager.getDBName(DBDriverManager.DB_RUNTIME) + ".properties");
        String versionConfig = "";
        String versionRuntime = "";
        if (propertiesFileConfig.exists()) {            
            Properties dbProperties = new Properties();
            FileInputStream inStream = null;
            try {
                inStream = new FileInputStream(propertiesFileConfig.getAbsolutePath());
                dbProperties.load(inStream);
            } finally {
                if (inStream != null) {
                    inStream.close();
                }
            }
            versionConfig = dbProperties.getProperty("version");
        }
        if (propertiesFileRuntime.exists()) {
            Properties dbProperties = new Properties();
            FileInputStream inStream = null;
            try {
                inStream = new FileInputStream(propertiesFileRuntime.getAbsolutePath());
                dbProperties.load(inStream);
            } finally {
                if (inStream != null) {
                    inStream.close();
                }
            }
            versionRuntime = dbProperties.getProperty("version");
        }        
        if (versionConfig.startsWith("1") || versionRuntime.startsWith("1")) {
            throw new UpgradeRequiredException(this.rb.getResourceString("upgrade.required"));
        }
    }
    
    
    /**Starts an internal DB server with default parameter*/
    private void startDBServer() throws Exception {
        this.server = new Server();
        //start an internal server
        int dbPort = this.preferences.getInt(PreferencesAS2.SERVER_DB_PORT);
        this.server.setPort(dbPort);
        this.server.setDatabasePath(0, DBDriverManager.getDBName(DBDriverManager.DB_CONFIG));
        this.server.setDatabaseName(0, DBDriverManager.getDBAlias(DBDriverManager.DB_CONFIG));
        this.server.setDatabasePath(1, DBDriverManager.getDBName(DBDriverManager.DB_RUNTIME));
        this.server.setDatabaseName(1, DBDriverManager.getDBAlias(DBDriverManager.DB_RUNTIME));
        HsqlProperties hsqlProperties = new HsqlProperties();
        hsqlProperties.setProperty("hsqldb.cache_file_scale", 128);
        hsqlProperties.setProperty("hsqldb.write_delay", false);
        hsqlProperties.setProperty("hsqldb.write_delay_millis", 0);
        this.server.setProperties(hsqlProperties);
        this.server.setLogWriter(null);
        this.server.start();
    }

    public void startup() throws Exception {
        this.startDBServer();
        try {
            this.createCheck();
        } catch (Exception e) {
            this.logger.warning(e.getMessage());
        }
        try {
            DBServer.defragDB(DBDriverManager.DB_CONFIG);
        } catch (Exception e) {
            this.logger.warning(e.getMessage());
        }
        try {
            DBServer.defragDB(DBDriverManager.DB_RUNTIME);
        } catch (Exception e) {
            this.logger.warning(e.getMessage());
        }
        try {
            Connection configConnection = DBDriverManager.getLocalConnection(DBDriverManager.DB_CONFIG);
            if (configConnection == null) {
                return;
            }
            Statement statement = configConnection.createStatement();
            statement.execute("SET FILES SCRIPT FORMAT COMPRESSED");
            statement.close();
            //check if a DB update is necessary. If so, update the DB
            this.updateDB(configConnection, DBDriverManager.DB_CONFIG);
            configConnection.close();
            Connection runtimeConnection = DBDriverManager.getLocalConnection(DBDriverManager.DB_RUNTIME);
            if (runtimeConnection == null) {
                return;
            }
            statement = runtimeConnection.createStatement();
            statement.execute("SET FILES SCRIPT FORMAT COMPRESSED");
            statement.close();
            //check if a runtime DB update is necessary. If so, update the runtime DB
            this.updateDB(runtimeConnection, DBDriverManager.DB_RUNTIME);
            DatabaseMetaData data = runtimeConnection.getMetaData();
            this.logger.info(rb.getResourceString("server.started",
                    new Object[]{data.getDatabaseProductName() + " " + data.getDatabaseProductVersion()}));
            runtimeConnection.close();
        } catch (Exception e) {
            this.logger.severe("DBServer.startup: " + e.getMessage());
        }
        DBDriverManager.setupConnectionPool();
    }

    /**Performs a defragementation of the passed database. This is necessary to keep the database files small
     *
     */
    public static void defragDB(final int DB_TYPE) throws Exception {
        Connection connection = null;
        Statement statement = null;
        try {
            connection = DBDriverManager.getConnectionWithoutErrorHandling(DB_TYPE, "localhost");
            statement = connection.createStatement();
            statement.execute("CHECKPOINT DEFRAG");
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (Exception e) {
                Logger.getLogger(AS2Server.SERVER_LOGGER_NAME).warning(e.getMessage());
            }
            try {
                if (statement != null) {
                    connection.close();
                }
            } catch (Exception e) {
                Logger.getLogger(AS2Server.SERVER_LOGGER_NAME).warning(e.getMessage());
            }
        }
    }

    /**Check if db exists and create a new one
     * if it doesnt exist
     */
    private void createCheck() {
        if (!this.databaseExists(DBDriverManager.DB_CONFIG)) {
            //new installation
            DBDriverManager.createDatabase(DBDriverManager.DB_CONFIG);
        }
        if (!this.databaseExists(DBDriverManager.DB_RUNTIME)) {
            //new installation
            DBDriverManager.createDatabase(DBDriverManager.DB_RUNTIME);
        }
    }

    /**Returns if the passed database type exists*/
    private boolean databaseExists(int databaseType) {
        String TABLE_NAME = "TABLE_NAME";
        String[] TABLE_TYPES = {"TABLE"};
        boolean databaseFound = false;
        Connection connection = null;
        try {
            connection = DBDriverManager.getConnectionWithoutErrorHandling(databaseType, "localhost");
            if (connection != null) {
                DatabaseMetaData metadata = connection.getMetaData();
                ResultSet tableResultRuntime = metadata.getTables(null, null, null, TABLE_TYPES);
                while (tableResultRuntime.next()) {
                    if (tableResultRuntime.getString(TABLE_NAME).equalsIgnoreCase("version")) {
                        databaseFound = true;
                    }
                }
                connection.close();
            }
        } catch (Exception e) {
            return (databaseFound);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (Exception e) {
                    //nop
                }
            }
        }
        return (databaseFound);
    }

    private int getActualDBVersion(Connection connection) {
        Statement statement = null;
        int foundVersion = -1;
        ResultSet result = null;
        try {
            statement = connection.createStatement();
            statement.setEscapeProcessing(true);
            result = statement.executeQuery("SELECT MAX(actualversion) AS maxversion FROM version");
            if (result.next()) {
                //value is always in the first column
                foundVersion = result.getInt("maxversion");
            }
        } catch (SQLException e) {
            Logger.getLogger(AS2Server.SERVER_LOGGER_NAME).warning(e.getMessage());
        } finally {
            if (result != null) {
                try {
                    result.close();
                } catch (SQLException ex) {
                    Logger.getLogger(AS2Server.SERVER_LOGGER_NAME).warning(ex.getMessage());
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ex) {
                    Logger.getLogger(AS2Server.SERVER_LOGGER_NAME).warning(ex.getMessage());
                }
            }
        }
        return (foundVersion);
    }

    /**Update the database if this is necessary.
     *@param connection connection to the database
     *@param DB_TYPE of the database that should be created, as defined in this class MecDriverManager
     */
    private void updateDB(Connection connection, final int DB_TYPE) {
        int requiredDBVersion = -1;
        String dbName = null;
        if (DB_TYPE == DBDriverManager.DB_CONFIG) {
            dbName = "config";
            requiredDBVersion = AS2ServerVersion.getRequiredDBVersionConfig();
        } else if (DB_TYPE == DBDriverManager.DB_RUNTIME) {
            dbName = "runtime";
            requiredDBVersion = AS2ServerVersion.getRequiredDBVersionRuntime();
        } else if (DB_TYPE != DBDriverManager.DB_DEPRICATED) {
            throw new RuntimeException("Unknown DB type requested in DBServer:updateDB.");
        }
        int foundVersion = this.getActualDBVersion(connection);
        //check if this is smaller than the required version!
        if (foundVersion != -1 && foundVersion < requiredDBVersion) {
            this.logger.info(rb.getResourceString("update.versioninfo",
                    new Object[]{
                        String.valueOf(foundVersion),
                        String.valueOf(requiredDBVersion)
                    }));
            this.logger.info(rb.getResourceString("update.progress"));
            for (int i = foundVersion; i < requiredDBVersion; i++) {
                this.logger.info(rb.getResourceString("update.progress.version.start",
                        new Object[]{String.valueOf(i + 1), dbName}));
                if (!this.startDBUpdate(i, connection, DB_TYPE)) {
                    this.logger.info(rb.getResourceString("update.error",
                            new Object[]{String.valueOf(i), String.valueOf(i + 1)}));
                    System.exit(-1);
                }
                //set new version to the database
                this.setNewDBVersion(connection, i + 1);
                int newActualVersion = this.getActualDBVersion(connection);
                this.logger.info(rb.getResourceString("update.progress.version.end",
                        new Object[]{String.valueOf(newActualVersion), dbName}));
            }
            this.logger.info((rb.getResourceString("update.successfully", dbName)));
        }
    }

    /**Sets the new DB version to the passed number if the update was
     *successfully
     *@param connection DB connection to use
     *@param version new DB version the update has updated to
     */
    private void setNewDBVersion(Connection connection, int version) {
        try {
            //request all connections from the database to store them
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO version(actualVersion,updateDate,updateComment)VALUES(?,?,?)");
            statement.setEscapeProcessing(true);
            //fill in values
            statement.setInt(1, version);
            statement.setDate(2, new java.sql.Date(System.currentTimeMillis()));
            statement.setString(3, "by " + AS2ServerVersion.getFullProductName() + " auto updater");
            statement.execute();
            statement.close();
        } catch (SQLException e) {
            this.logger.warning("DBServer.setNewDBVersion: " + e);
        }
    }

    /**Sends a shutdown signal to the DB*/
    public void shutdown() {
        try {
            Connection configConnection = DBDriverManager.getConnectionWithoutErrorHandling(DBDriverManager.DB_CONFIG, "localhost");
            Connection runtimeConnection = DBDriverManager.getConnection(DBDriverManager.DB_RUNTIME, "localhost");
            configConnection.createStatement().execute("SHUTDOWN");
            configConnection.close();
            System.out.println("DB server: config DB shutdown complete.");
            runtimeConnection.createStatement().execute("SHUTDOWN");
            runtimeConnection.close();
            System.out.println("DB server: runtime DB shutdown complete.");
        } catch (Exception e) {
            System.out.println("DB server shutdown: " + e.getMessage());
        }
        try {
            this.server.signalCloseAllServerConnections();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        try {
            DBDriverManager.shutdownConnectionPool();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        this.server.shutdown();
        while (this.server.getState() != ServerConstants.SERVER_STATE_SHUTDOWN) {
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
            }
        }
        String shutdownMessage = "DB server: shut down complete.";
        System.out.println(shutdownMessage);
    }

    /**Start the DB update from the startVersion to the startVersion+1
     *@param startVersion Start version
     *@param connection Connection to use for the update
     *@return true if the update was successful
     *@param DB_TYPE of the database that should be created, as defined in this class MecDriverManager
     */
    private boolean startDBUpdate(int startVersion, Connection connection, final int DB_TYPE) {
        boolean updatePerformed = false;
        String updateResource = null;

        if (DB_TYPE == DBDriverManager.DB_CONFIG) {
            updateResource = SQLScriptExecutor.SCRIPT_RESOURCE_CONFIG;
        } else if (DB_TYPE == DBDriverManager.DB_RUNTIME) {
            updateResource = SQLScriptExecutor.SCRIPT_RESOURCE_RUNTIME;
        } else if (DB_TYPE != DBDriverManager.DB_DEPRICATED) {
            throw new RuntimeException("Unknown DB type requested in DBServer.");
        }
        //sql file to execute for the update process
        String sqlResource = updateResource + "update" + startVersion + "to" + (startVersion + 1) + ".sql";
        SQLScriptExecutor executor = new SQLScriptExecutor();
        try {
            //defrag the DB
            DBServer.defragDB(DB_TYPE);
            if (executor.resourceExists(sqlResource)) {
                executor.executeScript(connection, sqlResource);
                updatePerformed = true;
            }
            //check if a java file should be executed that changes something in
            //the database, too
            String javaUpdateClass = updateResource.replace('/', '.') + "Update" + startVersion + "to" + (startVersion + 1);
            if (javaUpdateClass.startsWith(".")) {
                javaUpdateClass = javaUpdateClass.substring(1);
            }
            Class cl = Class.forName(javaUpdateClass);
            IUpdater updater = (IUpdater) cl.newInstance();
            updater.startUpdate(connection);
            if (!updater.updateWasSuccessfully()) {
                throw new Exception("Update failed.");
            }
        } catch (ClassNotFoundException e) {
            //ignore if update is already ok
            if (!updatePerformed) {
                this.logger.info("DBServer.startDBUpdate (ClassNotFoundException):" + e);
                this.logger.info(rb.getResourceString("update.notfound",
                        new Object[]{String.valueOf(startVersion),
                            String.valueOf(startVersion + 1),
                            updateResource
                        }));
                return (false);
            } else {
                return (true);
            }
        } catch (Throwable e) {
            this.logger.warning(e.getMessage());
            return (false);
        }
        return (true);
    }

    /**Split up the DB into a config and a runtime database if this is an AS version where only a single database
     * exists (< end of 2011)
     */
    private void createDeprecatedCheck() throws Exception {
        File deprecatedFile = new File(DBDriverManager.getDBName(DBDriverManager.DB_DEPRICATED) + ".script");
        File configFile = new File(DBDriverManager.getDBName(DBDriverManager.DB_CONFIG) + ".script");
        File runtimeFile = new File(DBDriverManager.getDBName(DBDriverManager.DB_RUNTIME) + ".script");
        //create new Database
        if (deprecatedFile.exists() && !configFile.exists() && !runtimeFile.exists()) {
            this.logger.info("Performing database split into config/runtime database.");
            //update issue, performed on 11/2011: split up deprecated database
            this.copyDeprecatedDatabaseTo(DBDriverManager.getDBName(DBDriverManager.DB_CONFIG));
            this.copyDeprecatedDatabaseTo(DBDriverManager.getDBName(DBDriverManager.DB_RUNTIME));
            this.logger.info("Database structure splitted.");
        }
    }

    /**Splits up the depricated database into 2 separate databases. The version of these splitted databases could be any from
     *0 to 50.*/
    private void copyDeprecatedDatabaseTo(String targetBase) throws IOException {
        String sourceBase = DBDriverManager.getDBName(DBDriverManager.DB_DEPRICATED);
        this.copyFile(sourceBase + ".backup", targetBase + ".backup");
        this.copyFile(sourceBase + ".data", targetBase + ".data");
        this.copyFile(sourceBase + ".properties", targetBase + ".properties");
        this.copyFile(sourceBase + ".script", targetBase + ".script");
    }

    /**Copies the contents from one stream to the other
     */
    public void copyStreams(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[4096];
        int read = 0;
        while (read != -1) {
            read = input.read(buffer);
            if (read > 0) {
                output.write(buffer, 0, read);
                output.flush();
            }
        }
    }

    private void copyFile(String source, String target) throws IOException {
        File sourceFile = new File(source);
        if (!sourceFile.exists()) {
            return;
        }
        FileInputStream inStream = new FileInputStream(sourceFile);
        FileOutputStream outStream = new FileOutputStream(target);
        this.copyStreams(inStream, outStream);
        inStream.close();
        outStream.close();
    }
}
