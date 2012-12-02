//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/server/AS2ServerRemoteImpl.java,v 1.1 2012/04/18 14:10:39 heller Exp $
package de.mendelson.comm.as2.server;

import de.mendelson.comm.as2.AS2Exception;
import de.mendelson.comm.as2.AS2ServerVersion;
import de.mendelson.comm.as2.cem.CEMReceiptController;
import de.mendelson.util.security.cert.CertificateManager;
import de.mendelson.comm.as2.clientserver.message.RefreshClientMessageOverviewList;
import de.mendelson.comm.as2.clientserver.message.RefreshTablePartnerData;
import de.mendelson.comm.as2.clientserver.serialize.CommandObject;
import de.mendelson.comm.as2.clientserver.serialize.CommandObjectIncomingMessage;
import de.mendelson.comm.as2.clientserver.serialize.CommandObjectServerInfo;
import de.mendelson.comm.as2.clientserver.serialize.CommandObjectShutdown;
import de.mendelson.comm.as2.message.AS2Info;
import de.mendelson.comm.as2.message.AS2MDNCreation;
import de.mendelson.comm.as2.message.AS2MDNInfo;
import de.mendelson.comm.as2.message.AS2Message;
import de.mendelson.comm.as2.message.AS2MessageInfo;
import de.mendelson.comm.as2.message.AS2MessageParser;
import de.mendelson.comm.as2.message.ExecuteShellCommand;
import de.mendelson.comm.as2.message.MDNAccessDB;
import de.mendelson.comm.as2.message.MessageAccessDB;
import de.mendelson.comm.as2.message.store.MessageStoreHandler;
import de.mendelson.comm.as2.notification.Notification;
import de.mendelson.comm.as2.partner.Partner;
import de.mendelson.comm.as2.partner.PartnerAccessDB;
import de.mendelson.comm.as2.partner.PartnerSystem;
import de.mendelson.comm.as2.partner.PartnerSystemAccessDB;
import de.mendelson.comm.as2.sendorder.SendOrderSender;
import de.mendelson.comm.as2.statistic.QuotaAccessDB;
import de.mendelson.util.MecResourceBundle;
import de.mendelson.util.clientserver.ClientServer;
import de.mendelson.util.rmi.MecRemote;
import de.mendelson.util.rmi.RMIPing;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.util.Enumeration;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletResponse;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software. Other product
 * and brand names are trademarks of their respective owners.
 */
/**
 * Class to be executed on the server site
 *
 * @author S.Heller
 * @version $Revision: 1.1 $
 * @since build 68
 */
public class AS2ServerRemoteImpl extends UnicastRemoteObject implements MecRemote {

    /**
     * Start time of this class, this is similar to the server startup time
     */
    private long startupTime = System.currentTimeMillis();
    /**
     * ResourceBundle to localize messages of the server
     */
    private MecResourceBundle rb = null;
    /**
     * Logger to log the requests to
     */
    private Logger logger = Logger.getLogger(AS2Server.SERVER_LOGGER_NAME);
    /**
     * Handles all certificates
     */
    private CertificateManager certificateManager;
    /**
     * Stores the messages
     */
    private MessageStoreHandler messageStoreHandler;
    private MessageAccessDB messageAccess;
    private MDNAccessDB mdnAccess;
    private ClientServer clientserver;
    //DB connection
    private Connection configConnection;
    private Connection runtimeConnection;

    /**
     * Implementation of the mendelson business integration remote interface
     *
     * @param communicationServer Server that handles the communication requests
     * @param convertingServer Server that handles the converting requests
     * @param serverLog Log into the DB/file
     * @param tRFCListener Listener that listens on tRFC server events (incoming
     * idocs, state change)
     * @param dbServer used database server
     */
    public AS2ServerRemoteImpl(ClientServer clientserver,
            CertificateManager certificateManager,
            Connection configConnection,
            Connection runtimeConnection) throws RemoteException, MissingResourceException {
        this.clientserver = clientserver;
        this.certificateManager = certificateManager;
        this.configConnection = configConnection;
        this.runtimeConnection = runtimeConnection;
        //Load default resourcebundle
        this.rb = (MecResourceBundle) ResourceBundle.getBundle(
                ResourceBundleAS2ServerRemoteImpl.class.getName());
        this.messageStoreHandler = new MessageStoreHandler(this.configConnection, this.runtimeConnection);
        clientserver.broadcastToClients(new RefreshTablePartnerData());
        this.messageAccess = new MessageAccessDB(this.configConnection, this.runtimeConnection);
        this.mdnAccess = new MDNAccessDB(this.configConnection, this.runtimeConnection);
    }

    /**
     * Executes a command object given by the client
     *
     * @param commandObject Object to be executed
     */
    @Override
    public Object execute(Object commandObject) throws RemoteException {
        return (this.computeServerSite(commandObject));
    }

    /**
     * A communicatoin connection indicates that a new message arrived
     */
    private CommandObjectIncomingMessage newMessageArrived(CommandObjectIncomingMessage requestObject) throws Throwable {
        //is this an AS2 request? It should have a as2-to and as2-from header
        if (requestObject.getHeader().getProperty("as2-to") == null) {
            this.logger.log(Level.SEVERE, this.rb.getResourceString("invalid.request.to"));
            requestObject.setMDNData(null);
            requestObject.setHttpReturnCode(HttpServletResponse.SC_BAD_REQUEST);
            return (requestObject);
        }
        if (requestObject.getHeader().getProperty("as2-from") == null) {
            this.logger.log(Level.SEVERE, this.rb.getResourceString("invalid.request.from"));
            requestObject.setMDNData(null);
            requestObject.setHttpReturnCode(HttpServletResponse.SC_BAD_REQUEST);
            return (requestObject);
        }
        AS2MessageParser parser = new AS2MessageParser();
        parser.setCertificateManager(this.certificateManager, this.certificateManager);
        parser.setDBConnection(this.configConnection, this.runtimeConnection);
        parser.setLogger(this.logger);
        byte[] incomingMessageData = this.readFile(requestObject.getMessageDataFilename());
        //store raw incoming message. If the message partners are identified successfully
        //the raw data is also written to the partner dir/raw
        String[] rawFiles = this.messageStoreHandler.storeRawIncomingData(
                incomingMessageData, requestObject.getHeader(),
                requestObject.getRemoteHost());
        String rawIncomingFile = rawFiles[0];
        String rawIncomingFileHeader = rawFiles[1];
        AS2Message message = null;
        try {
            //this will throw an exception if any of the partners are unknown or the local station
            //is not the receiver or the content MIC does not match. Anyway every message should be logged
            message = parser.createMessageFromRequest(incomingMessageData,
                    requestObject.getHeader(), requestObject.getContentType());
            message.getAS2Info().setRawFilename(rawIncomingFile);
            message.getAS2Info().setHeaderFilename(rawIncomingFileHeader);
            message.getAS2Info().setSenderHost(requestObject.getRemoteHost());
            message.getAS2Info().setDirection(AS2MessageInfo.DIRECTION_IN);
            //found a message without message id: stop processing
            if (!message.isMDN() && message.getAS2Info().getMessageId() == null) {
                this.logger.log(Level.SEVERE, this.rb.getResourceString("invalid.request.messageid"));
                requestObject.setMDNData(null);
                requestObject.setHttpReturnCode(HttpServletResponse.SC_BAD_REQUEST);
                return (requestObject);
            }
            //its a CEM: check data integrity before returning an MDN
            if (!message.isMDN()) {
                AS2MessageInfo messageInfo = (AS2MessageInfo) message.getAS2Info();
                if (messageInfo.getMessageType() == AS2Message.MESSAGETYPE_CEM) {
                    CEMReceiptController cemReceipt = new CEMReceiptController(
                            this.clientserver, this.configConnection, this.runtimeConnection,
                            this.certificateManager);
                    cemReceipt.checkInboundCEM(message);
                }
                this.messageAccess.initializeOrUpdateMessage(messageInfo);
            } else {
                this.mdnAccess.initializeOrUpdateMDN((AS2MDNInfo) message.getAS2Info());
            }
            //inbound message was an sync or async MDN
            if (message.isMDN()) {
                AS2MDNInfo mdnInfo = (AS2MDNInfo) message.getAS2Info();
                this.messageAccess.setMessageState(mdnInfo.getRelatedMessageId(),
                        mdnInfo.getState());
                //ASYNC/SYNC MDN received: insert an entry into the statistic table that a message has been sent
                QuotaAccessDB.incSentMessages(this.configConnection, this.runtimeConnection,
                        mdnInfo.getReceiverId(),
                        mdnInfo.getSenderId(), mdnInfo.getState(), mdnInfo.getRelatedMessageId());
            }
            this.updatePartnerSystemInfo(requestObject.getHeader());
            this.clientserver.broadcastToClients(new RefreshClientMessageOverviewList());
        } catch (AS2Exception e) {
            //exec on MDN send makes no sense here because no valid filename exists
            AS2Info as2Info = e.getAS2Message().getAS2Info();
            as2Info.setRawFilename(rawIncomingFile);
            as2Info.setHeaderFilename(rawIncomingFileHeader);
            as2Info.setState(AS2Message.STATE_STOPPED);
            as2Info.setDirection(AS2MessageInfo.DIRECTION_IN);
            as2Info.setSenderHost(requestObject.getRemoteHost());
            if (!as2Info.isMDN()) {
                AS2MessageInfo as2MessageInfo = (AS2MessageInfo) as2Info;
                if (as2MessageInfo.getSenderId() != null && as2MessageInfo.getReceiverId() != null) {
                    //this has to be performed because of the notification                    
                    this.messageAccess.initializeOrUpdateMessage(as2MessageInfo);
                    this.messageAccess.setMessageState(as2MessageInfo.getMessageId(), AS2Message.STATE_STOPPED);
                    if (((AS2MessageInfo) as2Info).requestsSyncMDN()) {
                        //SYNC MDN received with error: insert an entry into the statistic table that a message has been sent
                        QuotaAccessDB.incReceivedMessages(this.configConnection,
                                this.runtimeConnection,
                                as2Info.getReceiverId(),
                                as2Info.getSenderId(),
                                as2Info.getState(),
                                as2Info.getMessageId());
                    }
                }
                throw e;
            } else {
                AS2MDNInfo mdnInfo = (AS2MDNInfo) as2Info;
                //if its a MDN set the state of the whole transaction
                AS2MessageInfo relatedMessageInfo = this.messageAccess.getLastMessageEntry(mdnInfo.getRelatedMessageId());
                if (relatedMessageInfo != null) {
                    relatedMessageInfo.setState(AS2Message.STATE_STOPPED);
                    mdnInfo.setState(AS2Message.STATE_STOPPED);
                    this.mdnAccess.initializeOrUpdateMDN(mdnInfo);
                    this.messageAccess.setMessageState(mdnInfo.getRelatedMessageId(), AS2Message.STATE_STOPPED);
                    //content MIC does not match or simular error
                    ExecuteShellCommand executeCommand = new ExecuteShellCommand(this.configConnection, this.runtimeConnection);
                    //execute on sync MDN processing error
                    executeCommand.executeShellCommandOnSend(relatedMessageInfo, mdnInfo);
                    //write status file                    
                    MessageStoreHandler handler = new MessageStoreHandler(this.configConnection, this.runtimeConnection);
                    handler.writeOutboundStatusFile(relatedMessageInfo);
                    this.logger.log(Level.SEVERE, e.getMessage(), as2Info);
                }
            }
            this.clientserver.broadcastToClients(new RefreshClientMessageOverviewList());
            //dont't thow an exception here if this is an MDN already, a thrown Exception
            //will result in another MDN!
            if (as2Info.isMDN()) {
                //its a MDN
                AS2MDNInfo mdnInfo = (AS2MDNInfo) as2Info;
                //there is no related message because the original message id of the received MDN does not reference a message?
                AS2MessageInfo originalMessageInfo = this.messageAccess.getLastMessageEntry(mdnInfo.getRelatedMessageId());
                if (originalMessageInfo == null) {
                    this.logger.log(Level.SEVERE, e.getMessage());
                } else {
                }
                //an exception occured in processing an inbound MDN, signal back an error to the sender by HTTP code.
                // This will only work for ASYNC MDN because there is a logical problem in sync MDN processing:
                //If a sync mdn could not processed it is impossible to signal this back -> sender and receiver
                //will have different states of processing. Another reason to use ASYNC MDN instead of SYNC MDN
                requestObject.setHttpReturnCode(HttpServletResponse.SC_BAD_REQUEST);
                return (requestObject);
            }
        }
        AS2Info as2Info = message.getAS2Info();
        PartnerAccessDB access = new PartnerAccessDB(this.configConnection, this.runtimeConnection);
        Partner messageSender = access.getPartner(as2Info.getSenderId());
        Partner messageReceiver = access.getPartner(as2Info.getReceiverId());
        this.messageStoreHandler.storeParsedIncomingMessage(message, messageReceiver);
        if (!as2Info.isMDN()) {
            this.messageAccess.updateFilenames((AS2MessageInfo) as2Info);
            this.clientserver.broadcastToClients(new RefreshClientMessageOverviewList());
        }
        //process MDN
        if (message.isMDN()) {
            AS2MDNInfo mdnInfo = (AS2MDNInfo) message.getAS2Info();
            AS2MessageInfo originalMessageInfo = this.messageAccess.getLastMessageEntry(mdnInfo.getRelatedMessageId());
            ExecuteShellCommand executeCommand = new ExecuteShellCommand(this.configConnection, this.runtimeConnection);
            executeCommand.executeShellCommandOnSend(originalMessageInfo, mdnInfo);
            //write status file
            MessageStoreHandler handler = new MessageStoreHandler(this.configConnection, this.runtimeConnection);
            handler.writeOutboundStatusFile(originalMessageInfo);
        }
        //don't answer on signals or store them
        if (!as2Info.isMDN()) {
            AS2MessageInfo messageInfo = (AS2MessageInfo) message.getAS2Info();
            Partner mdnSender = messageReceiver;
            Partner mdnReceiver = messageSender;
            AS2MDNCreation mdnCreation = new AS2MDNCreation(this.certificateManager);
            mdnCreation.setLogger(this.logger);
            //create the MDN that the message has been received; state "processed"
            AS2Message mdn = mdnCreation.createMDNProcessed(messageInfo, mdnSender, mdnReceiver.getAS2Identification());
            AS2MessageInfo as2RelatedMessageInfo = this.messageAccess.getLastMessageEntry(((AS2MDNInfo) mdn.getAS2Info()).getRelatedMessageId());
            if (messageInfo.requestsSyncMDN()) {
                requestObject.setContentType(mdn.getContentType());
                requestObject.setMDNData(mdn.getRawData());
                //build up the header for the sync response
                Properties header = mdnCreation.buildHeaderForSyncMDN(mdn);
                requestObject.setHeader(header);
                this.messageStoreHandler.storeSentMessage(mdn, mdnSender, mdnReceiver, header);
                this.mdnAccess.initializeOrUpdateMDN((AS2MDNInfo) mdn.getAS2Info());
                //MBean counter: inc the sent data size, this is for sync success MDN
                AS2Server.incRawSentData(this.computeRawHeaderSize(header) + mdn.getRawDataSize());
                this.logger.log(Level.INFO,
                        this.rb.getResourceString("sync.mdn.sent",
                        new Object[]{
                            mdn.getAS2Info().getMessageId(),
                            ((AS2MDNInfo) mdn.getAS2Info()).getRelatedMessageId()
                        }), mdn.getAS2Info());
                //SYNC MDN sent with state "processed": insert an entry into the statistic table that a message has been received
                QuotaAccessDB.incReceivedMessages(this.configConnection, this.runtimeConnection, messageReceiver,
                        messageSender,
                        mdn.getAS2Info().getState(),
                        ((AS2MDNInfo) mdn.getAS2Info()).getRelatedMessageId());
                //on sync MDN the command object is sent back to the servlet, store the payload already as good here
                if (mdn.getAS2Info().getState() == AS2Message.STATE_FINISHED) {
                    this.messageStoreHandler.movePayloadToInbox(messageInfo.getMessageType(),
                            ((AS2MDNInfo) mdn.getAS2Info()).getRelatedMessageId(),
                            messageReceiver, messageSender);
                    //dont execute the command after receipt for CEM
                    if (as2RelatedMessageInfo.getMessageType() == AS2Message.MESSAGETYPE_CEM) {
                        CEMReceiptController cemReceipt = new CEMReceiptController(this.clientserver,
                                this.configConnection, this.runtimeConnection, this.certificateManager);
                        cemReceipt.processInboundCEM(as2RelatedMessageInfo);
                    } else {
                        ExecuteShellCommand executeCommand = new ExecuteShellCommand(this.configConnection, this.runtimeConnection);
                        executeCommand.executeShellCommandOnReceipt(messageSender, messageReceiver, as2RelatedMessageInfo);
                    }
                }
                this.messageAccess.setMessageState(((AS2MDNInfo) mdn.getAS2Info()).getRelatedMessageId(), mdn.getAS2Info().getState());
                this.clientserver.broadcastToClients(new RefreshClientMessageOverviewList());
            } else {
                //async MDN requested, dont send MDN in this case
                //process the CEM request if it requires async MDN
                if (as2RelatedMessageInfo.getMessageType() == AS2Message.MESSAGETYPE_CEM) {
                    CEMReceiptController cemReceipt = new CEMReceiptController(this.clientserver,
                            this.configConnection, this.runtimeConnection, this.certificateManager);
                    cemReceipt.processInboundCEM(as2RelatedMessageInfo);
                }
                requestObject.setMDNData(null);
                //async back to sender
                this.addSendOrder(mdn, messageSender, messageReceiver);
            }
        }
        return (requestObject);
    }

    /**
     * Compute the header upload size for the jmx interface
     */
    private long computeRawHeaderSize(Properties header) {
        long size = 0;
        Enumeration enumeration = header.propertyNames();
        while (enumeration.hasMoreElements()) {
            String key = (String) enumeration.nextElement();
            //key + "="
            size += key.length() + 1;
            //value + LF
            size += header.getProperty(key).length();
        }
        return (size);
    }

    /**
     * Returns some server info values
     */
    private CommandObject fillInServerInfo(CommandObjectServerInfo infoRequest) {
        infoRequest.setProperty(CommandObjectServerInfo.SERVER_START_TIME, String.valueOf(this.startupTime));
        infoRequest.setProperty(CommandObjectServerInfo.SERVER_PRODUCT_NAME, AS2ServerVersion.getProductName());
        infoRequest.setProperty(CommandObjectServerInfo.SERVER_VERSION, AS2ServerVersion.getVersion());
        infoRequest.setProperty(CommandObjectServerInfo.SERVER_BUILD, AS2ServerVersion.getBuild());
        return (infoRequest);
    }

    /**
     * Looks for the type of the command Object and executes it.
     *
     * @param object Object that contains all informations to execute
     */
    private Object computeServerSite(Object object) {
        try {
            //server shutdown requested by external request
            if (object instanceof CommandObjectShutdown) {
                CommandObjectShutdown shutdown = (CommandObjectShutdown) object;
                //log some information about who tried this
                this.logger.severe(this.rb.getResourceString("server.shutdown",
                        new Object[]{
                            shutdown.getClientUser(),
                            shutdown.getClientName() + ":" + shutdown.getClientIP()
                        }));
                System.exit(0);
            }
            //signal from a communication component, e.g. the http servlet
            if (object instanceof CommandObjectIncomingMessage) {
                try {
                    CommandObjectIncomingMessage commandObjectIncoming = (CommandObjectIncomingMessage) object;
                    //inc the sent data size, this is for sync error MDN
                    long size = 0;
                    if (commandObjectIncoming.getHeader() != null) {
                        size += this.computeRawHeaderSize(commandObjectIncoming.getHeader());
                    }
                    if (commandObjectIncoming.getMessageDataFilename() != null) {
                        size += new File(commandObjectIncoming.getMessageDataFilename()).length();
                    }
                    //MBean counter for received data size
                    AS2Server.incRawReceivedData(size);
                    object = this.newMessageArrived(commandObjectIncoming);
                } catch (AS2Exception as2Exception) {
                    PartnerAccessDB partnerAccess = new PartnerAccessDB(this.configConnection,
                            this.runtimeConnection);
                    String foundSenderId = as2Exception.getAS2Message().getAS2Info().getSenderId();
                    String foundReceiverId = as2Exception.getAS2Message().getAS2Info().getReceiverId();
                    Partner as2MessageReceiver = partnerAccess.getPartner(foundReceiverId);
                    AS2MDNCreation mdnCreation = new AS2MDNCreation(this.certificateManager);
                    mdnCreation.setLogger(this.logger);
                    AS2Message mdn = mdnCreation.createMDNError(as2Exception, foundSenderId, as2MessageReceiver, foundReceiverId);
                    AS2MDNInfo mdnInfo = (AS2MDNInfo) mdn.getAS2Info();
                    AS2MessageInfo messageInfo = (AS2MessageInfo) as2Exception.getAS2Message().getAS2Info();
                    //sync error MDN
                    if (messageInfo.requestsSyncMDN()) {
                        CommandObjectIncomingMessage mdnObject = new CommandObjectIncomingMessage();
                        mdnObject.setContentType(mdn.getContentType());
                        mdnObject.setMDNData(mdn.getRawData());
                        //build up the header for the sync response
                        Properties header = mdnCreation.buildHeaderForSyncMDN(mdn);
                        mdnObject.setHeader(header);
                        //MBean counter: inc the sent data size, this is for sync error MDN
                        AS2Server.incRawSentData(this.computeRawHeaderSize(header) + mdn.getRawDataSize());
                        Partner mdnReceiver = partnerAccess.getPartner(mdnInfo.getReceiverId());
                        Partner mdnSender = partnerAccess.getPartner(mdnInfo.getSenderId());
                        this.messageStoreHandler.storeSentMessage(mdn, mdnSender, mdnReceiver, header);
                        this.mdnAccess.initializeOrUpdateMDN(mdnInfo);
                        this.logger.log(Level.INFO,
                                this.rb.getResourceString("sync.mdn.sent",
                                new Object[]{
                                    mdnInfo.getMessageId(),
                                    mdnInfo.getRelatedMessageId()
                                }), mdnInfo);
                        this.messageAccess.setMessageState(mdnInfo.getRelatedMessageId(), AS2Message.STATE_STOPPED);
                        this.clientserver.broadcastToClients(new RefreshClientMessageOverviewList());
                        return (mdnObject);
                    } //async error MDN
                    else {
                        Partner messageReceiver = partnerAccess.getPartner(mdnInfo.getReceiverId());
                        Partner messageSender = partnerAccess.getPartner(mdnInfo.getSenderId());
                        //async back to sender. There are ALWAYS required partners for the send order even if the as2 ids 
                        //are not founnd because the partners are required for the async MDN receipt URL and a well structured MDN
                        if (messageReceiver == null) {
                            messageReceiver = new Partner();
                            messageReceiver.setAS2Identification(mdnInfo.getReceiverId());
                            messageReceiver.setMdnURL(messageInfo.getAsyncMDNURL());
                        }
                        if (messageSender == null) {
                            messageSender = new Partner();
                            messageSender.setAS2Identification(mdnInfo.getSenderId());
                        }
                        this.addSendOrder(mdn, messageReceiver, messageSender);
                    }
                }
            } else if (object instanceof CommandObjectServerInfo) {
                //external process requests some information about the server
                this.fillInServerInfo((CommandObjectServerInfo) object);
            }
        } catch (Throwable e) {
            e.printStackTrace();
            this.logger.severe("AS2ServerRemoteImpl: " + e.getClass().getName() + " " + e.getMessage());
            Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
        }
        return (object);
    }

    /**
     * Ping the server, sometimes a service is bound somewhere but this method
     * indicates perfectly that the RIGHT server is running
     */
    @Override
    public RMIPing ping() throws RemoteException {
        return (new RMIPing());
    }

    /**
     * Adds a message send order to the queue, this could also include an MDN
     *
     */
    private void addSendOrder(AS2Message message, Partner receiver, Partner sender) throws Exception {
        de.mendelson.comm.as2.sendorder.SendOrder order = new de.mendelson.comm.as2.sendorder.SendOrder();
        order.setReceiver(receiver);
        order.setMessage(message);
        order.setSender(sender);
        SendOrderSender orderSender = new SendOrderSender(this.configConnection, this.runtimeConnection);
        orderSender.send(order);
        this.clientserver.broadcastToClients(new RefreshClientMessageOverviewList());
    }

    /**
     * Reads a file from the disk and returns its content as byte array
     */
    private byte[] readFile(String filename) throws Exception {
        FileInputStream inStream = new FileInputStream(filename);
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        this.copyStreams(inStream, outStream);
        inStream.close();
        outStream.flush();
        outStream.close();
        return (outStream.toByteArray());
    }

    /**
     * Copies all data from one stream to another
     */
    private void copyStreams(InputStream in, OutputStream out)
            throws IOException {
        BufferedInputStream inStream = new BufferedInputStream(in);
        BufferedOutputStream outStream = new BufferedOutputStream(out);
        //copy the contents to an output stream
        byte[] buffer = new byte[1024];
        int read = 1024;
        //a read of 0 must be allowed, sometimes it takes time to
        //extract data from the input
        while (read != -1) {
            read = inStream.read(buffer);
            if (read > 0) {
                outStream.write(buffer, 0, read);
            }
        }
        outStream.flush();
    }

    /**
     * Updates the system information for a partner
     */
    private void updatePartnerSystemInfo(Properties header) {
        try {
            PartnerAccessDB access = new PartnerAccessDB(this.configConnection, this.runtimeConnection);
            Partner messageSender = access.getPartner(AS2MessageParser.unescapeFromToHeader(header.getProperty("as2-from")));
            if (messageSender != null) {
                PartnerSystem partnerSystem = new PartnerSystem();
                partnerSystem.setPartner(messageSender);
                if (header.getProperty("server") != null) {
                    partnerSystem.setProductName(header.getProperty("server"));
                } else if (header.getProperty("user-agent") != null) {
                    partnerSystem.setProductName(header.getProperty("user-agent"));
                }
                String version = header.getProperty("as2-version");
                if (version != null) {
                    partnerSystem.setAs2Version(version);
                    partnerSystem.setCompression(!version.equals("1.0"));
                }
                String optionalProfiles = header.getProperty("ediint-features");
                if (optionalProfiles != null) {
                    partnerSystem.setMa(optionalProfiles.contains("multiple-attachments"));
                    partnerSystem.setCEM(optionalProfiles.contains("CEM"));
                }
                PartnerSystemAccessDB systemAccess = new PartnerSystemAccessDB(this.configConnection,
                        this.runtimeConnection);
                systemAccess.insertOrUpdatePartnerSystem(partnerSystem);
            }
        } //this feature is really NOT that important to stop an inbound message
        catch (Exception e) {
            this.logger.warning("updatePartnerSystemInfo: " + e);
        }
    }
}
