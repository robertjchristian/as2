//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/server/AS2Server.java,v 1.1 2012/04/18 14:10:38 heller Exp $
package de.mendelson.comm.as2.server;

import de.mendelson.comm.as2.timing.MDNReceiptController;
import de.mendelson.Copyright;
import de.mendelson.comm.as2.AS2ServerVersion;
import de.mendelson.comm.as2.AS2ShutdownThread;
import de.mendelson.comm.as2.cert.CertificateCEMController;
import de.mendelson.util.security.cert.CertificateManager;
import de.mendelson.comm.as2.database.DBDriverManager;
import de.mendelson.comm.as2.database.DBServer;
import de.mendelson.comm.as2.log.DBLoggingHandler;
import de.mendelson.comm.as2.preferences.PreferencesAS2;
import de.mendelson.comm.as2.send.DirPollManager;
import de.mendelson.comm.as2.sendorder.SendOrderAccessDB;
import de.mendelson.comm.as2.sendorder.SendOrderReceiver;
import de.mendelson.comm.as2.timing.CertificateExpireController;
import de.mendelson.comm.as2.timing.MessageDeleteController;
import de.mendelson.comm.as2.timing.StatisticDeleteController;
import de.mendelson.util.MecResourceBundle;
import de.mendelson.util.clientserver.ClientServer;
import de.mendelson.util.clientserver.log.ClientServerLoggingHandler;
import de.mendelson.util.clientserver.user.DefaultPermissionDescription;
import de.mendelson.util.log.DailySubdirFileLoggingHandler;
import de.mendelson.util.rmi.MecRemote;
import de.mendelson.util.security.BCCryptoHelper;
import de.mendelson.util.security.cert.KeystoreStorage;
import de.mendelson.util.security.cert.KeystoreStorageImplFile;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.net.URL;
import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.sql.Connection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Hashtable;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mortbay.jetty.Server;
import org.mortbay.xml.XmlConfiguration;

/**
 * Class to start the AS2 server
 * @author  S.Heller
 * @version $Revision: 1.1 $
 * @since build 68
 */
public class AS2Server extends AbstractAS2Server implements AS2ServerMBean {

    public static final String SERVER_LOGGER_NAME = "de.mendelson.as2.server";
    private static int transactionCounter = 0;
    private static long rawDataSent = 0;
    private static long rawDataReceived = 0;
    /**Server start time in ms*/
    long startTime = System.currentTimeMillis();
    private Logger logger = Logger.getLogger(SERVER_LOGGER_NAME);
    /**Product preferences*/
    private PreferencesAS2 preferences = new PreferencesAS2();
    /**Registry to register RMI services*/
    private Registry registry = null;
    /**DB server that is used*/
    private DBServer dbServer = null;
    /**Localize the output
     */
    private MecResourceBundle rb = null;
    private DirPollManager pollManager = null;
    private CertificateManager certificateManager = null;
    //DB connection
    private Connection configConnection = null;
    private Connection runtimeConnection = null;
    /**Stores the RMI service locale to use the direct instanciation of a service instead
     * of RMI if RMI client and server are running in the same VM*/
    private static Hashtable<String, MecRemote> localRMIServiceMap = new Hashtable<String, MecRemote>();
    private ClientServer clientserver;
    private ClientServerSessionHandlerLocalhost clientServerSessionHandler = null;
    private boolean startHTTPServer = false;
    /**Sets if all clients may connect to this server or only clients from the servers host*/
    private boolean allowAllClients = false;

    /**Creates a new AS2 server and starts it
     */
    public AS2Server(boolean startHTTPServer, boolean allowAllClients) throws Exception {
        //Load default resourcebundle
        try {
            this.rb = (MecResourceBundle) ResourceBundle.getBundle(
                    ResourceBundleAS2Server.class.getName());
        } //load up  resourcebundle
        catch (MissingResourceException e) {
            throw new Exception("Oops..resource bundle " + e.getClassName() + " not found.");
        }
        this.startHTTPServer = startHTTPServer;
        this.allowAllClients = allowAllClients;
        AS2ServerResourceCheck resourceCheck = new AS2ServerResourceCheck();
        resourceCheck.performPortCheck();
        BCCryptoHelper helper = new BCCryptoHelper();
        boolean unlimitedStrengthInstalled = helper.performUnlimitedStrengthJurisdictionPolicyTest();
        if (!unlimitedStrengthInstalled) {
            this.logger.severe(this.rb.getResourceString("fatal.limited.strength"));
            System.exit(1);
        }
        ServerStartupSequence startup = new ServerStartupSequence(this.logger);
        startup.performWork();
        this.checkLock();
        int clientServerCommPort = this.preferences.getInt(PreferencesAS2.CLIENTSERVER_COMM_PORT);
        this.clientserver = new ClientServer(logger, clientServerCommPort);
        this.clientserver.setProductName(AS2ServerVersion.getFullProductName());
        this.setupClientServerSessionHandler();
        this.start();
        //start the partner poll threads
        this.pollManager.partnerConfigurationChanged();
    }

    @Override
    public void start() throws Exception {
        this.logger.info(rb.getResourceString("server.willstart", AS2ServerVersion.getFullProductName()));
        this.logger.info(Copyright.getCopyrightMessage());
        this.startDBServer();
        this.configConnection = DBDriverManager.getConnectionWithoutErrorHandling(DBDriverManager.DB_CONFIG, "localhost");
        this.runtimeConnection = DBDriverManager.getConnectionWithoutErrorHandling(DBDriverManager.DB_RUNTIME, "localhost");
        this.startHTTPServer();
        this.certificateManager = new CertificateManager(this.logger);
        KeystoreStorage storage = new KeystoreStorageImplFile("certificates.p12",
                this.preferences.get(PreferencesAS2.KEYSTORE_PASS).toCharArray(),
                BCCryptoHelper.KEYSTORE_PKCS12);
        this.certificateManager.loadKeystoreCertificates(storage);
        this.startSendOrderReceiver();
        this.setupLogger();
        this.registerRMI();
        //dont cache the DNS lookup for whole lifetime of the VM
        System.setProperty("networkaddress.cache.ttl", "86400");
        //start control threads
        MDNReceiptController receiptController = new MDNReceiptController(this.clientserver, this.configConnection, this.runtimeConnection);
        receiptController.startMDNCheck();
        MessageDeleteController logDeleteController = new MessageDeleteController(this.clientserver, this.configConnection, this.runtimeConnection);
        logDeleteController.startAutoDeleteControl();
        StatisticDeleteController statsDeleteController = new StatisticDeleteController(this.configConnection, this.runtimeConnection);
        statsDeleteController.startAutoDeleteControl();
        this.pollManager = new DirPollManager(this.certificateManager, this.configConnection,
                this.runtimeConnection, this.clientserver);
        this.clientServerSessionHandler.addServerProcessing(
                new AS2ServerProcessing(this.clientserver, this.pollManager,
                this.certificateManager, this.configConnection, this.runtimeConnection));
        CertificateExpireController expireController = new CertificateExpireController(this.certificateManager, this.configConnection, this.runtimeConnection);
        expireController.startCertExpireControl();
        CertificateCEMController cemController = new CertificateCEMController(this.clientserver, this.configConnection, this.runtimeConnection, this.certificateManager);
        Executors.newSingleThreadExecutor().submit(cemController);
        Runtime.getRuntime().addShutdownHook(new AS2ShutdownThread(this.dbServer));
        //listen for inbound client connects
        this.clientserver.start();
        this.logger.info(rb.getResourceString("server.started",
                String.valueOf(System.currentTimeMillis() - this.startTime)));
        //display the startup message on the console
        System.out.println(rb.getResourceString("server.started",
                String.valueOf(System.currentTimeMillis() - this.startTime)));
    }

    private void setupLogger() {
        this.logger.setUseParentHandlers(false);
        //send the log info to the attached clients of the client-server framework
        this.logger.addHandler(new ClientServerLoggingHandler(this.clientServerSessionHandler));
        this.logger.addHandler(new DBLoggingHandler());
        //add file logger that logs in a daily subdir
        this.logger.addHandler(new DailySubdirFileLoggingHandler(new File(this.preferences.get(PreferencesAS2.DIR_LOG)), "as2.log"));
        this.logger.setLevel(Level.ALL);
    }

    private void setupClientServerSessionHandler() {
        //set up session handler for incoming client requests
        this.clientServerSessionHandler = new ClientServerSessionHandlerLocalhost(this.logger,
                new String[]{AS2ServerVersion.getFullProductName()}, this.allowAllClients);
        this.clientserver.setSessionHandler(this.clientServerSessionHandler);
        this.clientServerSessionHandler.setProductName(AS2ServerVersion.getProductName());
        this.clientServerSessionHandler.setPermissionDescription(new DefaultPermissionDescription());
    }

    @Override
    public Logger getLogger() {
        return (this.logger);
    }

    /**register a local service to the mbi server. This will skip the remote RMI
     * if the request is a local request. That means: no serialization, more performance
     *
     * @param service Service name ot register
     * @param remote Remote implementation
     */
    public static void registerLocalRMI(String service, MecRemote remote) {
        AS2Server.localRMIServiceMap.put(service, remote);
    }

    /**Returns an instance of the requested service if this has been registered to the AS2 server
     * If it has not been registered, null is returned and the RMI way will be used for the client-server
     * connection
     */
    public static MecRemote lookupLocalRMI(String service) {
        return (AS2Server.localRMIServiceMap.get(service));
    }

    /**Checks for a lock to prevent starting the server several times on the same machine
     * 
     */
    private void checkLock() {
        //check if lock file exists, if it exists cancel!
        File lockFile = new File(AS2ServerVersion.getProductName().replace(' ', '_') + ".lock");
        if (lockFile.exists()) {
            DateFormat format = SimpleDateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM);
            this.logger.severe(rb.getResourceString("server.already.running",
                    new Object[]{
                        lockFile.getAbsolutePath(),
                        format.format(new java.util.Date(lockFile.lastModified()))
                    }));
            throw new RuntimeException(rb.getResourceString("server.already.running",
                    new Object[]{
                        lockFile.getAbsolutePath(),
                        format.format(new java.util.Date(lockFile.lastModified()))
                    }));
        } else {
            try {
                FileWriter writer = new FileWriter(lockFile);
                writer.write("");
                writer.flush();
                writer.close();
            } catch (Exception e) {
                this.logger.severe("checkLock: " + e.getMessage());
                System.exit(1);
            }
        }
    }

    private void startSendOrderReceiver() throws Exception {
        //reset the send order state of available send orders back to waiting
        SendOrderAccessDB sendOrderAccess = new SendOrderAccessDB(this.configConnection, this.runtimeConnection);
        sendOrderAccess.resetAllToWaiting();
        SendOrderReceiver receiver = new SendOrderReceiver(this.configConnection, this.runtimeConnection,
                this.clientserver);
        Executors.newSingleThreadExecutor().submit(receiver);
    }

    private Server startHTTPServer() throws Exception {
        //start the HTTP server if this is requested
        if (this.startHTTPServer) {
            System.setProperty("jetty.home", new File("jetty").getAbsolutePath());
            URL serviceConfig = new File("./jetty/etc/jetty.xml").toURI().toURL();
            XmlConfiguration xmlConfiguration = new XmlConfiguration(serviceConfig);
            org.mortbay.jetty.Server server = new org.mortbay.jetty.Server();
            xmlConfiguration.configure(server);
            server.start();
            return (server);
        } else {
            this.logger.info(rb.getResourceString("server.nohttp"));
        }
        return (null);
    }

    /**Starts the database server*/
    private void startDBServer() throws Exception {
        //start the database server
        this.dbServer = new DBServer();
        this.dbServer.startup();
        while (true) {
            try {
                Connection testConnection = DBDriverManager.getConnectionWithoutErrorHandling(DBDriverManager.DB_CONFIG, "localhost");
                testConnection.close();
                break;
            } catch (Throwable e) {
                try {
                    Thread.sleep(250);
                } catch (InterruptedException ex) {
                    //nop
                }
            }
        }
        return;
    }

    /**Registers the RMI services an lists them
     */
    private void registerRMI() throws Exception {
        AS2ServerRemoteImpl remote = new AS2ServerRemoteImpl(this.clientserver, this.certificateManager,
                this.configConnection, this.runtimeConnection);
        //register local services
        AS2Server.registerLocalRMI(this.preferences.get(PreferencesAS2.SERVER_RMI_SERVICE), remote);
        int rmiPort = this.preferences.getInt(PreferencesAS2.SERVER_RMI_PORT);
        try {
            //check for existing registry and add service to this port, will throw an exception
            //if no registry has been found
            this.registry = LocateRegistry.getRegistry(rmiPort);
            //this rebind attempt should throw an exception if there is no registry so far. If there is a registry
            //of an other process we will use it. This will result in some trouble if the other process is shut down...
            registry.rebind(this.preferences.get(PreferencesAS2.SERVER_RMI_SERVICE),
                    remote);
            this.logger.info("Existing RMI registry found at port " + rmiPort + ", binding services there.");
            String[] services = this.registry.list();
            this.logger.info("Services bound, the following services are available now:");
            for (int i = 0; i < services.length; i++) {
                this.logger.info(services[i]);
            }
        } catch (ConnectException e) {
            //ok, there is no registry running on this machine
            this.registry = LocateRegistry.createRegistry(rmiPort);
            registry.rebind(this.preferences.get(PreferencesAS2.SERVER_RMI_SERVICE),
                    remote);
        } catch (RemoteException e) {
            this.logger.info("A running registry was found at port " + rmiPort + ". To allow " + AS2ServerVersion.getProductNameShortcut() + " to use this registry you have to add the " + AS2ServerVersion.getProductNameShortcut() + " jar to the CLASSPATH" + " environment variable of the registry hosting program.");
            this.logger.severe("Detailed exception information:");
            this.logger.severe(e.getMessage());
            System.exit(-1);
        }
    }

    @Override
    public int getPort() {
        return (this.preferences.getInt(PreferencesAS2.CLIENTSERVER_COMM_PORT));
    }

    /**MBean interface*/
    @Override
    public long getUsedMemoryInBytes() {
        return (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
    }

    /**MBean interface*/
    @Override
    public long getTotalMemoryInBytes() {
        return (Runtime.getRuntime().totalMemory());
    }

    /**MBean interface*/
    @Override
    public String getServerVersion() {
        return (AS2ServerVersion.getProductName() + " " + AS2ServerVersion.getVersion() + " " + AS2ServerVersion.getBuild());
    }

    /**MBean interface*/
    @Override
    public long getUptimeInMS() {
        return (System.currentTimeMillis() - this.startTime);
    }

    @Override
    public long getRawDataSentInBytesInUptime() {
        return (rawDataSent);
    }

    @Override
    public long getRawDataReceivedInBytesInUptime() {
        return (rawDataReceived);
    }

    @Override
    public long getTransactionCountInUptime() {
        return (transactionCounter);
    }

    public static synchronized void incTransactionCounter() {
        transactionCounter++;
    }

    public static synchronized void incRawSentData(long size) {
        rawDataSent += size;
    }

    public static synchronized void incRawReceivedData(long size) {
        rawDataReceived += size;
    }

    /**Deletes the lock file*/
    public static void deleteLockFile() {
        File lockFile = new File(AS2ServerVersion.getProductName().replace(' ', '_') + ".lock");
        lockFile.delete();
    }
}
