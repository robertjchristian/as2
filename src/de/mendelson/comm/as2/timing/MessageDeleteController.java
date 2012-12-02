//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/timing/MessageDeleteController.java,v 1.1 2012/04/18 14:10:39 heller Exp $
package de.mendelson.comm.as2.timing;

import de.mendelson.comm.as2.clientserver.message.RefreshClientMessageOverviewList;
import de.mendelson.comm.as2.log.LogAccessDB;
import de.mendelson.comm.as2.message.AS2MessageInfo;
import de.mendelson.comm.as2.message.MessageAccessDB;
import de.mendelson.comm.as2.notification.Notification;
import de.mendelson.comm.as2.preferences.PreferencesAS2;
import de.mendelson.comm.as2.server.AS2Server;
import de.mendelson.util.MecResourceBundle;
import de.mendelson.util.clientserver.ClientServer;
import java.io.File;
import java.sql.Connection;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Controlles the timed deletion of AS2 entries from the log
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class MessageDeleteController {

    /**Logger to log inforamtion to*/
    private Logger logger = Logger.getLogger(AS2Server.SERVER_LOGGER_NAME);
    private PreferencesAS2 preferences = new PreferencesAS2();
    private MessageDeleteThread deleteThread;
    private ClientServer clientserver = null;
    private MecResourceBundle rb = null;
    private Connection configConnection;
    private Connection runtimeConnection;

    public MessageDeleteController(ClientServer clientserver, Connection configConnection,
            Connection runtimeConnection) {
        this.clientserver = clientserver;
        this.configConnection = configConnection;
        this.runtimeConnection = runtimeConnection;
        //Load default resourcebundle
        try {
            this.rb = (MecResourceBundle) ResourceBundle.getBundle(
                    ResourceBundleMessageDeleteController.class.getName());
        } //load up resourcebundle
        catch (MissingResourceException e) {
            throw new RuntimeException("Oops..resource bundle " + e.getClassName() + " not found.");
        }
    }

    /**Starts the embedded task that guards the log
     */
    public void startAutoDeleteControl() {
        this.deleteThread = new MessageDeleteThread(this.configConnection, this.runtimeConnection);
        Executors.newSingleThreadExecutor().submit(this.deleteThread);
    }

    /**Deletes a message entry from the log. Clears all files
     */
    public void deleteMessageFromLog(AS2MessageInfo info) {
        LogAccessDB logAccess = new LogAccessDB(this.configConnection, this.runtimeConnection);
        logAccess.deleteMessageLog(info.getMessageId());
        MessageAccessDB messageAccess = new MessageAccessDB(this.configConnection, this.runtimeConnection);
        try {
            //delete all raw files from the disk
            List<String> rawfilenames = messageAccess.getRawFilenamesToDelete(info);
            if (rawfilenames != null) {
                for (String rawfilename : rawfilenames) {
                    boolean success = new File(rawfilename).delete();
                    //did not work, schedule for delete after shutdown
                    if (!success) {
                        new File(rawfilename).deleteOnExit();
                    }
                }
            }
            messageAccess.deleteMessage(info);
        } catch (Exception e) {
            this.logger.severe("deleteMessageFromLog: " + e.getMessage());
            Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
        }
        if (this.clientserver != null) {
            this.clientserver.broadcastToClients(new RefreshClientMessageOverviewList());
        }
    }

    public class MessageDeleteThread implements Runnable {

        private boolean stopRequested = false;
        //wait this time between checks, once an hour
        private final long WAIT_TIME = TimeUnit.HOURS.toMillis(1);
        //DB connection
        private Connection configConnection;
        private Connection runtimeConnection;

        public MessageDeleteThread(Connection configConnection, Connection runtimeConnection) {
            this.configConnection = configConnection;
            this.runtimeConnection = runtimeConnection;
        }

        @Override
        public void run() {
            Thread.currentThread().setName("Contol auto as2 message delete");
            while (!stopRequested) {
                try {
                    Thread.sleep(WAIT_TIME);
                } catch (InterruptedException e) {
                    //nop
                }
                if (preferences.getBoolean(PreferencesAS2.AUTO_MSG_DELETE)) {
                    MessageAccessDB messageAccess = null;
                    try {
                        messageAccess = new MessageAccessDB(this.configConnection, this.runtimeConnection);
                        long olderThan = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(preferences.getInt(PreferencesAS2.AUTO_MSG_DELETE_OLDERTHAN));
                        List<AS2MessageInfo> overviewList = messageAccess.getMessagesOlderThan(olderThan, -1);
                        if (overviewList != null) {
                            for (AS2MessageInfo messageInfo:overviewList) {
                                if (preferences.getBoolean(PreferencesAS2.AUTO_MSG_DELETE_LOG)) {
                                    logger.fine(rb.getResourceString("autodelete",
                                            new Object[]{
                                                messageInfo.getMessageId(),
                                                String.valueOf(preferences.getInt(PreferencesAS2.AUTO_MSG_DELETE_OLDERTHAN))
                                            }));
                                }
                                deleteMessageFromLog(messageInfo);
                            }
                        }
                    } catch (Exception e) {
                        logger.severe("MessageDeleteThread: " + e.getMessage());
                        Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
                    }
                }
            }
        }
    }
}
