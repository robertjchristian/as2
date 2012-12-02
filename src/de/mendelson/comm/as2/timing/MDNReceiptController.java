//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/timing/MDNReceiptController.java,v 1.1 2012/04/18 14:10:39 heller Exp $
package de.mendelson.comm.as2.timing;

import de.mendelson.comm.as2.clientserver.message.RefreshClientMessageOverviewList;
import de.mendelson.comm.as2.message.AS2Message;
import de.mendelson.comm.as2.message.AS2MessageInfo;
import de.mendelson.comm.as2.message.ExecuteShellCommand;
import de.mendelson.comm.as2.message.MessageAccessDB;
import de.mendelson.comm.as2.message.store.MessageStoreHandler;
import de.mendelson.comm.as2.notification.Notification;
import de.mendelson.comm.as2.preferences.PreferencesAS2;
import de.mendelson.comm.as2.server.AS2Server;
import de.mendelson.util.MecResourceBundle;
import de.mendelson.util.clientserver.ClientServer;
import java.sql.Connection;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
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
 * Controlles the timed deletion of as2 entries from the log
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class MDNReceiptController {

    /**Logger to log inforamtion to*/
    private Logger logger = Logger.getLogger(AS2Server.SERVER_LOGGER_NAME);
    private PreferencesAS2 preferences = new PreferencesAS2();
    private MDNCheckThread checkThread;
    /**server for client-server communication*/
    private ClientServer clientserver = null;
    private MecResourceBundle rb = null;
    private Connection configConnection;
    private Connection runtimeConnection;

    public MDNReceiptController(ClientServer clientserver, Connection configConnection,
            Connection runtimeConnection) {
        this.configConnection = configConnection;
        this.runtimeConnection = runtimeConnection;
        this.clientserver = clientserver;
        //Load default resourcebundle
        try {
            this.rb = (MecResourceBundle) ResourceBundle.getBundle(
                    ResourceBundleMDNReceipt.class.getName());
        } //load up resourcebundle
        catch (MissingResourceException e) {
            throw new RuntimeException("Oops..resource bundle " + e.getClassName() + " not found.");
        }
    }

    /**Starts the embedded task that guards the log
     */
    public void startMDNCheck() {
        this.checkThread = new MDNCheckThread(this.configConnection, this.runtimeConnection);
        Executors.newSingleThreadExecutor().submit(this.checkThread);
    }

    public class MDNCheckThread implements Runnable {
        //wait this time between checks

        private boolean stopRequested = false;
        private final long WAIT_TIME = TimeUnit.MINUTES.toMillis(1);
        private MessageAccessDB messageAccess;
        private Connection configConnection;
        private Connection runtimeConnection;

        public MDNCheckThread(Connection configConnection, Connection runtimeConnection) {
            this.configConnection = configConnection;
            this.runtimeConnection = runtimeConnection;
            try {
                this.messageAccess = new MessageAccessDB(this.configConnection, this.runtimeConnection);
            } catch (Exception e) {
                logger.severe("MDNCheckThread: " + e.getMessage());
                Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
            }
        }

        @Override
        public void run() {
            Thread.currentThread().setName("Check MDN receipt");
            while (!stopRequested) {
                try {
                    Thread.sleep(WAIT_TIME);
                } catch (InterruptedException e) {
                    //nop
                }
                long olderThan = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(preferences.getInt(PreferencesAS2.ASYNC_MDN_TIMEOUT));
                List<AS2MessageInfo> overviewList = this.messageAccess.getMessagesSendOlderThan(olderThan);
                if (overviewList != null) {
                    for (AS2MessageInfo messageInfo : overviewList) {
                        try {
                            logger.log(Level.SEVERE, rb.getResourceString("expired", messageInfo.getMessageId()), messageInfo);
                            //a message id may have more then one entry if the sender implemented a resend mechanism
                            messageAccess.setMessageState(messageInfo.getMessageId(), AS2Message.STATE_PENDING, AS2Message.STATE_STOPPED);
                            messageInfo.setState(AS2Message.STATE_STOPPED);
                            //execute system command after send, will always execute the error command
                            ExecuteShellCommand executor = new ExecuteShellCommand(this.configConnection, this.runtimeConnection);
                            executor.executeShellCommandOnSend(messageInfo, null);
                            //write status file
                            MessageStoreHandler handler = new MessageStoreHandler(this.configConnection, this.runtimeConnection);
                            handler.writeOutboundStatusFile(messageInfo);
                        } catch (Exception e) {
                            //this thread may not stop on error!
                            logger.severe(Thread.currentThread().getName() + ": " + e.getMessage());
                            Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
                        }
                    }
                    if (overviewList.size() > 0) {
                        clientserver.broadcastToClients(new RefreshClientMessageOverviewList());
                    }
                }
            }
        }
    }
}
