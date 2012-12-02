//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/sendorder/SendOrderReceiver.java,v 1.1 2012/04/18 14:10:38 heller Exp $
package de.mendelson.comm.as2.sendorder;

import de.mendelson.comm.as2.clientserver.message.RefreshClientMessageOverviewList;
import de.mendelson.comm.as2.message.AS2MDNInfo;
import de.mendelson.comm.as2.message.AS2Message;
import de.mendelson.comm.as2.message.AS2MessageInfo;
import de.mendelson.comm.as2.message.ExecuteShellCommand;
import de.mendelson.comm.as2.message.MessageAccessDB;
import de.mendelson.comm.as2.message.store.MessageStoreHandler;
import de.mendelson.comm.as2.preferences.PreferencesAS2;
import de.mendelson.comm.as2.send.HttpConnectionParameter;
import de.mendelson.comm.as2.send.MessageHttpUploader;
import de.mendelson.comm.as2.send.NoConnectionException;
import de.mendelson.comm.as2.server.AS2Server;
import de.mendelson.util.MecResourceBundle;
import de.mendelson.util.clientserver.ClientServer;
import java.sql.Connection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
 * Receiver class that enqueues send orders
 *
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class SendOrderReceiver implements Runnable {

    private Logger logger = Logger.getLogger(AS2Server.SERVER_LOGGER_NAME);
    private MecResourceBundle rb;
    private Connection configConnection;
    private Connection runtimeConnection;
    private SendOrderAccessDB sendOrderAccess;
    private final int MAX_RETRY_COUNT = 10;
    private final long RETRY_WAIT_TIME = TimeUnit.SECONDS.toMillis(30);
    /**
     * Thread will stop if this is no longer set
     */
    private boolean runPermission = true;
    /**
     * Needed for refresh
     */
    private ClientServer clientserver = null;
    /**
     * Server preferences
     */
    private PreferencesAS2 preferences = new PreferencesAS2();
    /**
     * Handles messages storage
     */
    private MessageStoreHandler messageStoreHandler;
    private MessageAccessDB messageAccess;

    public SendOrderReceiver(Connection configConnection, Connection runtimeConnection,
            ClientServer clientserver) {
        //Load default resourcebundle
        try {
            this.rb = (MecResourceBundle) ResourceBundle.getBundle(
                    ResourceBundleSendOrderReceiver.class.getName());
        } //load up resourcebundle
        catch (MissingResourceException e) {
            throw new RuntimeException("Oops..resource bundle " + e.getClassName() + " not found.");
        }
        this.configConnection = configConnection;
        this.runtimeConnection = runtimeConnection;
        this.sendOrderAccess = new SendOrderAccessDB(this.configConnection, this.runtimeConnection);
        this.messageAccess = new MessageAccessDB(this.configConnection, this.runtimeConnection);
        this.messageStoreHandler = new MessageStoreHandler(this.configConnection, this.runtimeConnection);
        this.clientserver = clientserver;
    }

    /**
     * Stops the listener
     */
    public void stopReceiver() {
        this.runPermission = false;
    }

    @Override
    public void run() {

        //Max number of outbound connections. All other connection attempts are scheduled in a queue
        ExecutorService fixedTheadExecutor = Executors.newFixedThreadPool(5);
        //listen until stop is requested
        while (this.runPermission) {
            //listen on the queue for new awaiting send orders
            List<SendOrder> waitingOrders = this.sendOrderAccess.getNext(5);
            for (SendOrder order : waitingOrders) {
                final SendOrder finalOrder = order;
                fixedTheadExecutor.execute(new Runnable() {

                    @Override
                    public void run() {
                        processOrder(finalOrder);
                    }
                });

            }
            //wait some time before looking for new messages
            try {
                Thread.sleep(TimeUnit.MILLISECONDS.toMillis(250));
            } catch (InterruptedException e) {
                //NOP
            }
        }
    }

    private void processOrder(SendOrder order) {
        try {
            boolean processingAllowed = true;
            //before performing the send there has to be checked if the send process is still valid. The orders
            //are queued, between scheduling and processing the orders the transmission time could expire
            //or the user could cancel it
            if (order.getMessage().isMDN()) {
                //if the MDN state is on failure then the related transmission is on failure state, too - 
                //checking this makes no sense here
                AS2MDNInfo mdnInfo = (AS2MDNInfo) order.getMessage().getAS2Info();
                AS2MessageInfo relatedMessageInfo = messageAccess.getLastMessageEntry(mdnInfo.getRelatedMessageId());
                if (relatedMessageInfo == null) {
                    processingAllowed = false;
                }
            } else {
                AS2MessageInfo messageInfo = (AS2MessageInfo) order.getMessage().getAS2Info();
                if (messageInfo.getMessageType() == AS2Message.MESSAGETYPE_AS2) {
                    //update the message info from the database
                    messageInfo = messageAccess.getLastMessageEntry(messageInfo.getMessageId());
                    if (messageInfo == null || messageInfo.getState() == AS2Message.STATE_STOPPED) {
                        processingAllowed = false;
                    }
                } else if (messageInfo.getMessageType() == AS2Message.MESSAGETYPE_CEM) {
                    processingAllowed = true;
                }
            }
            if (processingAllowed) {
                MessageHttpUploader messageUploader = new MessageHttpUploader();
                messageUploader.setLogger(this.logger);
                messageUploader.setAbstractServer(this.clientserver);
                messageUploader.setDBConnection(this.configConnection, this.runtimeConnection);
                //configure the connection parameters
                HttpConnectionParameter connectionParameter = new HttpConnectionParameter();
                connectionParameter.setConnectionTimeoutMillis(this.preferences.getInt(PreferencesAS2.HTTP_SEND_TIMEOUT));
                connectionParameter.setHttpProtocolVersion(order.getReceiver().getHttpProtocolVersion());
                connectionParameter.setProxy(messageUploader.createProxyObjectFromPreferences());
                connectionParameter.setUseExpectContinue(true);
                Properties requestHeader = messageUploader.upload(connectionParameter, order.getMessage(), order.getSender(), order.getReceiver());
                //set error or finish state, remember that this send order could be
                //also an MDN if async MDN is requested
                if (order.getMessage().isMDN()) {
                    AS2MDNInfo mdnInfo = (AS2MDNInfo) order.getMessage().getAS2Info();
                    if (mdnInfo.getState() == AS2Message.STATE_FINISHED) {
                        AS2MessageInfo relatedMessageInfo = messageAccess.getLastMessageEntry(mdnInfo.getRelatedMessageId());
                        this.messageStoreHandler.movePayloadToInbox(relatedMessageInfo.getMessageType(), mdnInfo.getRelatedMessageId(),
                                order.getSender(), order.getReceiver());
                        //execute a shell command after send SUCCESS
                        ExecuteShellCommand executeCommand = new ExecuteShellCommand(this.configConnection, this.runtimeConnection);
                        //switch sender and receiver because its the MDN sender that is requested, not the message sender
                        executeCommand.executeShellCommandOnReceipt(order.getReceiver(), order.getSender(), relatedMessageInfo);
                    }
                    //set the transaction state to the MDN state
                    messageAccess.setMessageState(mdnInfo.getRelatedMessageId(), mdnInfo.getState());
                } else {
                    //its a AS2 message that has been sent
                    AS2MessageInfo messageInfo = (AS2MessageInfo) order.getMessage().getAS2Info();
                    messageAccess.updateFilenames(messageInfo);
                    messageAccess.setMessageSendDate(messageInfo);
                    if (!messageInfo.requestsSyncMDN()) {
                        long endTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(preferences.getInt(PreferencesAS2.ASYNC_MDN_TIMEOUT));
                        DateFormat format = SimpleDateFormat.getDateTimeInstance(DateFormat.SHORT,
                                DateFormat.MEDIUM);
                        logger.log(Level.INFO, rb.getResourceString("async.mdn.wait",
                                new Object[]{
                                    order.getMessage().getAS2Info().getMessageId(),
                                    format.format(endTime)
                                }), messageInfo);
                    }
                }
            }
            //even if a processing was not possible: delete the sendorder
            this.sendOrderAccess.delete(order.getDbId());
            this.clientserver.broadcastToClients(new RefreshClientMessageOverviewList());
        } catch (NoConnectionException e) {
            int retryCount = order.incRetryCount();
            //to many retries: cancel the transaction
            if (retryCount > this.MAX_RETRY_COUNT) {
                logger.log(Level.SEVERE, e.getMessage(), order.getMessage().getAS2Info());
                logger.log(Level.SEVERE, rb.getResourceString("max.retry.reached",
                        new Object[]{
                            order.getMessage().getAS2Info().getMessageId(),}), order.getMessage().getAS2Info());
                this.processUploadError(order);
            } else {
                logger.log(Level.WARNING, e.getMessage(), order.getMessage().getAS2Info());
                logger.log(Level.WARNING, rb.getResourceString("retry",
                        new Object[]{
                            order.getMessage().getAS2Info().getMessageId(),
                            String.valueOf((this.RETRY_WAIT_TIME / 1000)),
                            String.valueOf(retryCount),
                            String.valueOf(this.MAX_RETRY_COUNT)
                        }), order.getMessage().getAS2Info());
                this.sendOrderToRetry(order);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.log(Level.SEVERE, e.getMessage(), order.getMessage().getAS2Info());
            this.processUploadError(order);
        }
    }

    /**
     * Update the order in the queue - with a new nextexecution time
     */
    private void sendOrderToRetry(SendOrder order) {
        SendOrderSender sender = null;
        sender = new SendOrderSender(this.configConnection, this.runtimeConnection);
        sender.resend(order, System.currentTimeMillis() + this.RETRY_WAIT_TIME);
    }

    /**
     * The upload process of the data failed. Set the message state, execute the
     * command, ..
     */
    private void processUploadError(SendOrder order) {
        try {
            //stores
            this.messageStoreHandler.storeSentErrorMessage(
                    order.getMessage(), order.getSender(), order.getReceiver());
            if (!order.getMessage().isMDN()) {
                //message upload failure
                messageAccess.setMessageState(order.getMessage().getAS2Info().getMessageId(),
                        AS2Message.STATE_STOPPED);
                //its important to set the state in the message info, too. An event exec is not performed
                //for pending messages
                order.getMessage().getAS2Info().setState(AS2Message.STATE_STOPPED);
                messageAccess.updateFilenames((AS2MessageInfo) order.getMessage().getAS2Info());
                //execute a shell command after send ERROR if this is configured (sync MDN, async MDN)
                ExecuteShellCommand executor = new ExecuteShellCommand(this.configConnection, this.runtimeConnection);
                executor.executeShellCommandOnSend((AS2MessageInfo) order.getMessage().getAS2Info(), null);
                //write status file
                this.messageStoreHandler.writeOutboundStatusFile((AS2MessageInfo) order.getMessage().getAS2Info());
            } else {
                //MDN send failure, e.g. wrong URL for async MDN in message
                messageAccess.setMessageState(((AS2MDNInfo) order.getMessage().getAS2Info()).getRelatedMessageId(),
                        AS2Message.STATE_STOPPED);
            }
            this.sendOrderAccess.delete(order.getDbId());
            this.clientserver.broadcastToClients(new RefreshClientMessageOverviewList());
        } catch (Exception ee) {
            ee.printStackTrace();
            logger.log(Level.SEVERE, "SendOrderReceiver.processUploadError(): " + ee.getMessage(),
                    order.getMessage().getAS2Info());
            this.messageAccess.setMessageState(order.getMessage().getAS2Info().getMessageId(), AS2Message.STATE_STOPPED);
        }
    }
}