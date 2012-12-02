//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/send/DirPollManager.java,v 1.1 2012/04/18 14:10:35 heller Exp $
package de.mendelson.comm.as2.send;

import de.mendelson.comm.as2.clientserver.message.RefreshClientMessageOverviewList;
import de.mendelson.util.security.cert.CertificateManager;
import de.mendelson.comm.as2.message.AS2Message;
import de.mendelson.comm.as2.message.store.MessageStoreHandler;
import de.mendelson.comm.as2.notification.Notification;
import de.mendelson.comm.as2.partner.Partner;
import de.mendelson.comm.as2.partner.PartnerAccessDB;
import de.mendelson.comm.as2.preferences.PreferencesAS2;
import de.mendelson.comm.as2.sendorder.SendOrderSender;
import de.mendelson.comm.as2.server.AS2Server;
import de.mendelson.util.FileFilterRegexpMatch;
import de.mendelson.util.MecResourceBundle;
import de.mendelson.util.clientserver.ClientServer;
import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software. Other product
 * and brand names are trademarks of their respective owners.
 */

/**
 * Manager that polls the outbox directories of the partners, creates messages
 * and sends them
 *
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class DirPollManager {

    private Logger logger = Logger.getLogger(AS2Server.SERVER_LOGGER_NAME);
    private PreferencesAS2 preferences = new PreferencesAS2();
    private CertificateManager certificateManager;
    /**
     * Stores all poll threads key: partner DB id, value: pollThread
     */
    private Map<String, DirPollThread> mapPollThread = new Hashtable<String, DirPollThread>();
    /**
     * Localize the GUI
     */
    private MecResourceBundle rb = null;
    //DB connection
    private Connection configConnection;
    private Connection runtimeConnection;
    private ClientServer clientserver;

    public DirPollManager(CertificateManager certificateManager, Connection configConnection, Connection runtimeConnection,
            ClientServer clientserver) {
        this.configConnection = configConnection;
        this.runtimeConnection = runtimeConnection;
        this.clientserver = clientserver;
        //Load default resourcebundle
        try {
            this.rb = (MecResourceBundle) ResourceBundle.getBundle(
                    ResourceBundleDirPollManager.class.getName());
        } //load up resourcebundle
        catch (MissingResourceException e) {
            throw new RuntimeException("Oops..resource bundle " + e.getClassName() + " not found.");
        }
        this.certificateManager = certificateManager;
        this.logger.info(this.rb.getResourceString("manager.started"));
    }

    /**
     * Indicates that the partner configuration has been changed: This should
     * stop now unued tasks and start other
     */
    public void partnerConfigurationChanged() {
        Partner[] partner = null;
        Partner[] localStations = null;
        PartnerAccessDB access = new PartnerAccessDB(this.configConnection, this.runtimeConnection);
        partner = access.getPartner();
        localStations = access.getLocalStations();
        if (partner == null) {
            this.logger.severe("partnerConfigurationChanged: Unable to load partner");
            return;
        }
        for (Partner sender : localStations) {
            for (Partner receiver : partner) {
                String id = sender.getDBId() + "_" + receiver.getDBId();
                //add partner task if it does not exist so far
                if (!this.mapPollThread.containsKey(id) && !receiver.isLocalStation()) {
                    this.addPartnerPollThread(sender, receiver);
                } else if (this.mapPollThread.containsKey(id)) {
                    DirPollThread thread = (DirPollThread) this.mapPollThread.get(id);
                    if (!receiver.isLocalStation()) {
                        //task exists: update its information
                        thread.setRelationShip(sender, receiver);
                    } else {
                        //its a local station now: stop the task and remove it
                        thread.requestStop();
                        this.mapPollThread.remove(id);
                    }
                }
            }
        }
        //still running task that is not in the configuration any more: stop and remove
        List<String> idList = new ArrayList<String>();
        Iterator iterator = this.mapPollThread.keySet().iterator();
        while (iterator.hasNext()) {
            idList.add((String) iterator.next());
        }
        for (String id : idList) {
            boolean idFound = false;
            for (Partner sender : localStations) {
                for (Partner receiver : partner) {
                    String relationShipId = sender.getDBId() + "_" + receiver.getDBId();
                    if (id.equals(relationShipId)) {
                        idFound = true;
                    }
                }
            }
            //old still running taks, has been deleted in the config: stop and remove
            if (!idFound) {
                DirPollThread thread = this.mapPollThread.get(id);
                thread.requestStop();
                this.mapPollThread.remove(id);
            }
        }
    }

    /**
     * Adds a new partner to the poll thread list
     *
     */
    private void addPartnerPollThread(Partner localStation, Partner partner) {
        DirPollThread thread = new DirPollThread(this.configConnection, this.runtimeConnection);
        thread.setRelationShip(localStation, partner);
        this.mapPollThread.put(localStation.getDBId() + "_" + partner.getDBId(), thread);
        Executors.newSingleThreadExecutor().submit(thread);
        String pollIgnoreList = partner.getPollIgnoreListAsString();
        if (pollIgnoreList == null) {
            pollIgnoreList = "--";
        }
        logger.info(rb.getResourceString("poll.started", new Object[]{
                    localStation.getName(), partner.getName(), pollIgnoreList, partner.getPollInterval()
                }));
    }

    /**
     * Worker class that listens on the queue and performs a http send if a send
     * order has been found
     */
    public class DirPollThread implements Runnable {

        /**
         * Polls all 10s by default
         */
        private long pollInterval = TimeUnit.SECONDS.toMillis(10);
        private boolean stopRequested = false;
        private Partner receiver = null;
        private Partner sender = null;
        private Connection configConnection;
        private Connection runtimeConnection;

        public DirPollThread(Connection configConnection, Connection runtimeConnection) {
            this.configConnection = configConnection;
            this.runtimeConnection = runtimeConnection;
        }

        /**
         * Asks the thread to stiop
         */
        public void requestStop() {
            logger.info(rb.getResourceString("poll.stopped",
                    new Object[]{this.sender.getName(), this.receiver.getName()}));
            this.stopRequested = true;
        }

        /**
         * Extracts the right directory to poll for the passed partner
         */
        public synchronized void setRelationShip(Partner newSender, Partner newReceiver) {
            //partner renamed, this results in a new poll directory
            if ((this.receiver != null && this.sender != null)) {
                if (!this.receiver.getName().equals(newReceiver.getName()) || !this.sender.getName().equals(newSender.getName())) {
                    logger.info(rb.getResourceString("poll.stopped",
                            new Object[]{
                                this.sender.getName(), this.receiver.getName()
                            }));
                    String pollIgnoreList = newReceiver.getPollIgnoreListAsString();
                    if (pollIgnoreList == null) {
                        pollIgnoreList = "--";
                    }
                    logger.info(rb.getResourceString("poll.started", new Object[]{
                                newSender.getName(), newReceiver.getName(), pollIgnoreList, newReceiver.getPollInterval()
                            }));
                }
            }
            this.receiver = newReceiver;
            this.sender = newSender;
            this.pollInterval = newReceiver.getPollInterval() * TimeUnit.SECONDS.toMillis(1);
        }

        /**
         * Runs this thread
         */
        @Override
        public void run() {
            //allow to process 3 files threaded
            ExecutorService fixedTheadExecutor = Executors.newFixedThreadPool(3);
            while (!stopRequested) {
                try {
                    Thread.sleep(this.pollInterval);
                } catch (InterruptedException e) {
                    //nop
                }
                StringBuilder outboxDirName = new StringBuilder();
                outboxDirName.append(new File(preferences.get(PreferencesAS2.DIR_MSG)).getAbsolutePath());
                outboxDirName.append(File.separator);
                outboxDirName.append(MessageStoreHandler.convertToValidFilename(this.receiver.getName()));
                outboxDirName.append(File.separator);
                outboxDirName.append("outbox");
                outboxDirName.append(File.separator);
                outboxDirName.append(MessageStoreHandler.convertToValidFilename(this.sender.getName()));
                outboxDirName.append(File.separator);
                File outboxDir = new File(outboxDirName.toString());
                if (!outboxDir.exists()) {
                    outboxDir.mkdirs();
                }
                FileFilterRegexpMatch fileFilter = new FileFilterRegexpMatch();
                if (this.receiver.getPollIgnoreList() != null) {
                    for (String ignoreEntry : this.receiver.getPollIgnoreList()) {
                        fileFilter.addNonMatchingPattern(ignoreEntry);
                    }
                }
                File[] files = outboxDir.listFiles(fileFilter);
                Arrays.sort(files, new ComparatorFiledateOldestFirst());
                List<Callable<Boolean>> tasks = new ArrayList<Callable<Boolean>>();
                int fileCounter = 0;
                for (File file : files) {
                    if (file.isDirectory()) {
                        continue;
                    }
                    if (!file.canWrite()) {
                        logger.warning(rb.getResourceString("warning.ro", file.getAbsolutePath()));
                        continue;
                    }
                    if (!this.renameIsPossible(file)) {
                        logger.warning(rb.getResourceString("warning.notcomplete", file.getAbsolutePath()));
                        continue;
                    }
                    final File finalFile = file;
                    Callable<Boolean> singleTask = new Callable<Boolean>() {

                        @Override
                        public Boolean call() {
                            processFile(finalFile);
                            return (Boolean.TRUE);
                        }
                    };
                    tasks.add(singleTask);
                    fileCounter++;
                    //take a defined max number of files per poll process only
                    if (fileCounter == this.receiver.getMaxPollFiles()) {
                        break;
                    }
                }
                //wait for all threads to be finished
                try {
                    fixedTheadExecutor.invokeAll(tasks);
                } catch (InterruptedException e) {
                    //nop
                }
            }
        }

        /**
         * Checks if the passed file could be renamed. If this is not possible,
         * the file is still used as stream target and should not be touched
         * (works actually only on windows but does not lead to problems for
         * other OS)
         *
         * @param file
         * @return
         */
        private boolean renameIsPossible(File file) {
            File newFile = new File(file.getAbsolutePath() + "x");
            boolean renamePossible = file.renameTo(newFile);
            boolean renameBackPossible = newFile.renameTo(file);
            return (renamePossible && renameBackPossible);
        }

        /**
         * Processes a single, found file
         */
        private void processFile(File file) {
            try {
                logger.fine(rb.getResourceString("processing.file",
                        new Object[]{
                            file.getName(),
                            this.sender.getName(),
                            this.receiver.getName()
                        }));
                SendOrderSender orderSender = new SendOrderSender(this.configConnection, this.runtimeConnection);
                AS2Message message = orderSender.send(certificateManager, this.sender, this.receiver, file);
                clientserver.broadcastToClients(new RefreshClientMessageOverviewList());

                boolean deleted = file.delete();
                if (deleted) {
                    logger.log(Level.INFO,
                            rb.getResourceString("messagefile.deleted",
                            new Object[]{
                                message.getAS2Info().getMessageId(),
                                file.getName(),}),
                            message.getAS2Info());
                }
            } catch (Throwable e) {
                String message = rb.getResourceString("processing.file.error",
                        new Object[]{file.getName(), this.sender, this.receiver, e.getMessage()});
                logger.severe(message);
                Exception exception = new Exception(message, e);
                Notification.systemFailure(this.configConnection, this.runtimeConnection, exception);
            }
        }
    }
}
