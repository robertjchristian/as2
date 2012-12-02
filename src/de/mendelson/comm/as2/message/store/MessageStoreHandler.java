//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/message/store/MessageStoreHandler.java,v 1.1 2012/04/18 14:10:31 heller Exp $
package de.mendelson.comm.as2.message.store;

import de.mendelson.comm.as2.AS2ServerVersion;
import de.mendelson.comm.as2.message.AS2Info;
import de.mendelson.comm.as2.message.AS2Message;
import de.mendelson.comm.as2.message.AS2MessageInfo;
import de.mendelson.comm.as2.message.AS2Payload;
import de.mendelson.comm.as2.message.MessageAccessDB;
import de.mendelson.comm.as2.partner.Partner;
import de.mendelson.comm.as2.partner.PartnerAccessDB;
import de.mendelson.comm.as2.preferences.PreferencesAS2;
import de.mendelson.comm.as2.server.AS2Server;
import de.mendelson.util.MecResourceBundle;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
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
 * Stores messages in specified directories
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class MessageStoreHandler {

    /**products preferences*/
    private PreferencesAS2 preferences = new PreferencesAS2();
    private Logger logger = Logger.getLogger(AS2Server.SERVER_LOGGER_NAME);
    /**localize the output*/
    private MecResourceBundle rb = null;
    private final String CRLF = new String(new byte[]{0x0d, 0x0a});
    private Connection configConnection;
    private Connection runtimeConnection;

    public MessageStoreHandler(Connection configConnection, Connection runtimeConnection) {
        this.configConnection = configConnection;
        this.runtimeConnection = runtimeConnection;
        //Load resourcebundle
        try {
            this.rb = (MecResourceBundle) ResourceBundle.getBundle(
                    ResourceBundleMessageStoreHandler.class.getName());
        } //load up  resourcebundle
        catch (MissingResourceException e) {
            throw new RuntimeException("Oops..resource bundle " + e.getClassName() + " not found.");
        }
    }

    /**Stores incoming data for the server without analyzing it, raw
     *Returns the raw filename and the header filename
     */
    public String[] storeRawIncomingData(byte[] data, Properties header, String remoteHost) throws IOException {
        String[] filenames = new String[2];
        File inRawDir = new File(new File(this.preferences.get(PreferencesAS2.DIR_MSG)).getAbsolutePath() + File.separator + "_rawincoming");
        //ensure the directory exists
        if (!inRawDir.exists()) {
            boolean created = inRawDir.mkdirs();
            if (!created) {
                this.logger.warning(this.rb.getResourceString("dir.createerror",
                        inRawDir.getAbsolutePath()));
            }
        }
        DateFormat format = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        StringBuilder rawFileName = new StringBuilder();
        rawFileName.append(inRawDir.getAbsolutePath()).append(File.separator).append(format.format(new Date())).append("_");
        if (remoteHost != null) {
            rawFileName.append(remoteHost);
        } else {
            rawFileName.append("unknownhost");
        }
        rawFileName.append(".as2");
        File rawDataFile = new File(rawFileName.toString());
        //write raw data
        FileOutputStream outStream = new FileOutputStream(rawDataFile);
        ByteArrayInputStream inStream = new ByteArrayInputStream(data);
        this.copyStreams(inStream, outStream);
        outStream.flush();
        outStream.close();
        inStream.close();
        //write header
        File headerFile = new File(rawDataFile.getAbsolutePath() + ".header");
        FileOutputStream outStreamHeader = new FileOutputStream(headerFile);
        Enumeration enumeration = header.keys();
        while (enumeration.hasMoreElements()) {
            String key = (String) enumeration.nextElement();
            outStreamHeader.write((key + " = " + header.getProperty(key) + CRLF).getBytes());
        }
        outStreamHeader.flush();
        outStreamHeader.close();
        filenames[0] = rawDataFile.getAbsolutePath();
        filenames[1] = headerFile.getAbsolutePath();
        return (filenames);
    }

    /**Copies all data from one stream to another*/
    private void copyStreams(InputStream in, OutputStream out) throws IOException {
        BufferedInputStream inStream = new BufferedInputStream(in);
        BufferedOutputStream outStream = new BufferedOutputStream(out);
        //copy the contents to an output stream
        byte[] buffer = new byte[2048];
        int read = 0;
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

    /**If a message state is OK the payload has to be moved to the right directory
     * @param messageType could be a normal EDI message or a CEM
     */
    public void movePayloadToInbox(int messageType, String messageId, Partner localstation, Partner senderstation) throws Exception {
        StringBuilder inBoxDirPath = new StringBuilder();
        inBoxDirPath.append(localstation.getMessagePath(this.preferences.get(PreferencesAS2.DIR_MSG)));
        inBoxDirPath.append(File.separator);
        if (messageType == AS2Message.MESSAGETYPE_AS2) {
            inBoxDirPath.append("inbox");
        } else if (messageType == AS2Message.MESSAGETYPE_CEM) {
            inBoxDirPath.append("certificates");
        }
        if (this.preferences.getBoolean(PreferencesAS2.RECEIPT_PARTNER_SUBDIR)) {
            inBoxDirPath.append(File.separator);
            inBoxDirPath.append(convertToValidFilename(senderstation.getName()));
        }
        //store incoming message
        File inboxDir = new File(inBoxDirPath.toString());
        //ensure the directory exists
        if (!inboxDir.exists()) {
            boolean created = inboxDir.mkdirs();
            if (!created) {
                this.logger.warning(this.rb.getResourceString("dir.createerror",
                        inboxDir.getAbsolutePath()));
            }
        }
        //load message overview from database
        MessageAccessDB messageAccess = new MessageAccessDB(this.configConnection, this.runtimeConnection);
        List<AS2Payload> payloadList = messageAccess.getPayload(messageId);
        AS2MessageInfo messageInfo = messageAccess.getLastMessageEntry(messageId);
        if (payloadList != null) {
            for (int i = 0; i < payloadList.size(); i++) {
                String payloadFilename = payloadList.get(i).getPayloadFilename();
                if (payloadFilename == null) {
                    continue;
                }
                //source where to copy from
                File inFile = new File(payloadFilename);
                //is it defined to keep the original filename for messages from this sender?
                if (senderstation.getKeepOriginalFilenameOnReceipt() && payloadList.get(i).getOriginalFilename() != null && payloadList.get(i).getOriginalFilename().length() > 0) {
                    payloadFilename = payloadList.get(i).getOriginalFilename();
                }
                //is it a CEM? Take the content id as filename and add an extension
                if (messageInfo.getMessageType() == AS2Message.MESSAGETYPE_CEM && payloadList.get(i).getContentId() != null) {
                    payloadFilename = payloadFilename + "_" + convertToValidFilename(payloadList.get(i).getContentId());
                    if (payloadList.get(i).getContentType() != null) {
                        if (payloadList.get(i).getContentType().toLowerCase().contains("ediint-cert-exchange+xml")) {
                            payloadFilename = payloadFilename + ".xml";
                        } else {
                            payloadFilename = payloadFilename + ".p7c";
                        }
                    }
                }

                StringBuilder outFilename = new StringBuilder();
                outFilename.append(inboxDir.getAbsolutePath());
                outFilename.append(File.separator);
                outFilename.append(new File(payloadFilename).getName());
                File outFile = new File(outFilename.toString());
                inFile.renameTo(outFile);
                payloadList.get(i).setPayloadFilename(outFilename.toString());
                this.logger.log(Level.FINE, this.rb.getResourceString("comm.success",
                        new Object[]{
                            messageInfo.getMessageId(),
                            String.valueOf(i + 1),
                            outFilename.toString()
                        }), messageInfo);
            }
            messageAccess.insertPayload(messageId, payloadList);
        }
    }

    /**Stores an incoming message payload to the right partners mailbox, the decrypted message to the raw directory
     *The filenames of the files where the data has been stored in is written to the message object
     */
    public void storeParsedIncomingMessage(AS2Message message, Partner localstation) throws Exception {
        //do not store signals payload in pending dir
        if (!message.getAS2Info().isMDN()) {
            StringBuilder inBoxDirPath = new StringBuilder();
            inBoxDirPath.append(localstation.getMessagePath(this.preferences.get(PreferencesAS2.DIR_MSG)));
            inBoxDirPath.append(File.separator);
            inBoxDirPath.append("inbox");
            //store incoming message
            File inboxDir = new File(inBoxDirPath.toString());
            //ensure the directory exists
            if (!inboxDir.exists()) {
                boolean created = inboxDir.mkdirs();
                if (!created) {
                    this.logger.warning(this.rb.getResourceString("dir.createerror",
                            inboxDir.getAbsolutePath()));
                }
            }
            //store the payload to the pending directory. It resists there as long as no positive MDN comes in
            File pendingDir = new File(inboxDir.getAbsolutePath() + File.separator + "pending");
            if (!pendingDir.exists()) {
                boolean created = pendingDir.mkdirs();
                if (!created) {
                    this.logger.warning(this.rb.getResourceString("dir.createerror",
                            pendingDir.getAbsolutePath()));
                }
            }
            for (int i = 0; i < message.getPayloadCount(); i++) {
                AS2Payload payload = message.getPayload(i);
                StringBuilder pendingFilename = new StringBuilder();
                pendingFilename.append(pendingDir.getAbsolutePath());
                pendingFilename.append(File.separator);
                pendingFilename.append(MessageStoreHandler.convertToValidFilename(message.getAS2Info().getMessageId()));
                if (message.getPayloadCount() > 1) {
                    pendingFilename.append("_").append(String.valueOf(i));
                }
                File pendingFile = new File(pendingFilename.toString());
                payload.writeTo(pendingFile);
                payload.setPayloadFilename(pendingFile.getAbsolutePath());
            }
            MessageAccessDB messageAccess = new MessageAccessDB(this.configConnection, this.runtimeConnection);
            messageAccess.insertPayload(message.getAS2Info().getMessageId(), message.getPayloads());
            File decryptedRawFile = new File(message.getAS2Info().getRawFilename() + ".decrypted");
            FileOutputStream outStream = new FileOutputStream(decryptedRawFile);
            ByteArrayInputStream inStream = new ByteArrayInputStream(message.getDecryptedRawData());
            this.copyStreams(inStream, outStream);
            outStream.flush();
            outStream.close();
            ((AS2MessageInfo) message.getAS2Info()).setRawFilenameDecrypted(decryptedRawFile.getAbsolutePath());
        }
    }

    /**Stores the message if an error occured during creation
     *or sending the message
     */
    public void storeSentErrorMessage(AS2Message message, Partner localstation, Partner receiver) throws Exception {
        DateFormat format = new SimpleDateFormat("yyyyMMdd");
        StringBuilder errorDirName = new StringBuilder();
        errorDirName.append(new File(this.preferences.get(PreferencesAS2.DIR_MSG)).getAbsolutePath());
        errorDirName.append(File.separator);
        errorDirName.append(convertToValidFilename(receiver.getName())).append(File.separator).append("error");
        errorDirName.append(File.separator).append(convertToValidFilename(localstation.getName()));
        errorDirName.append(File.separator).append(format.format(new Date()));
        //store sent message
        File errorDir = new File(errorDirName.toString());
        //ensure the directory exists
        if (!errorDir.exists()) {
            boolean created = errorDir.mkdirs();
            if (!created) {
                this.logger.warning(this.rb.getResourceString("dir.createerror",
                        errorDir.getAbsolutePath()));
            }
        }
        //write out the payload(s)
        for (int i = 0; i < message.getPayloadCount(); i++) {
            File payloadFile = File.createTempFile("AS2Message", ".as2", errorDir);
            message.getPayload(i).writeTo(payloadFile);
            message.getPayload(i).setPayloadFilename(payloadFile.getAbsolutePath());
            this.logger.log(Level.SEVERE, this.rb.getResourceString("message.error.stored",
                    new Object[]{
                        message.getAS2Info().getMessageId(),
                        payloadFile.getAbsolutePath()
                    }), message.getAS2Info());
        }
        //write raw file to error/raw
        File errorRawDir = new File(errorDir.getAbsolutePath() + File.separator + "raw");
        //ensure the directory exists
        if (!errorRawDir.exists()) {
            boolean created = errorRawDir.mkdirs();
            if (!created) {
                this.logger.warning(this.rb.getResourceString("dir.createerror",
                        errorRawDir.getAbsolutePath()));
            }
        }
        File errorRawFile = File.createTempFile("error", ".raw", errorRawDir);
        message.writeRawDecryptedTo(errorRawFile);
        this.logger.log(Level.SEVERE, this.rb.getResourceString("message.error.raw.stored",
                new Object[]{
                    message.getAS2Info().getMessageId(), errorRawFile.getAbsolutePath()
                }), message.getAS2Info());
        MessageAccessDB messageAccess = new MessageAccessDB(this.configConnection, this.runtimeConnection);
        if (!message.getAS2Info().isMDN()) {
            AS2MessageInfo messageInfo = (AS2MessageInfo) message.getAS2Info();
            messageInfo.setRawFilenameDecrypted(errorRawFile.getAbsolutePath());
            //update the filenames in the db            
            messageAccess.updateFilenames(messageInfo);
        }
        messageAccess.insertPayload(message.getAS2Info().getMessageId(), message.getPayloads());
    }

    /**Stores an outgoing message in a sent directory
     */
    public void storeSentMessage(AS2Message message, Partner localstation, Partner receiver, Properties header) throws Exception {
        DateFormat format = new SimpleDateFormat("yyyyMMdd");
        String receiverName = "unidentified";
        if (receiver != null) {
            receiverName = convertToValidFilename(receiver.getName());
        }
        String localStationName = "unknown";
        if (localstation != null) {
            localStationName = convertToValidFilename(localstation.getName());
        }
        //store sent message
        File sentDir = new File(new File(this.preferences.get(PreferencesAS2.DIR_MSG)).getAbsolutePath() + File.separator + receiverName + File.separator + "sent" + File.separator + localStationName + File.separator + format.format(new Date()));
        //ensure the directory exists
        if (!sentDir.exists()) {
            boolean created = sentDir.mkdirs();
            if (!created) {
                this.logger.warning(this.rb.getResourceString("dir.createerror",
                        sentDir.getAbsolutePath()));
            }
        }
        AS2Info as2Info = message.getAS2Info();
        String requestType = "";
        if (as2Info.isMDN()) {
            requestType = "_MDN";
        }
        StringBuilder rawFilename = new StringBuilder();
        rawFilename.append(sentDir.getAbsolutePath());
        rawFilename.append(File.separator);
        rawFilename.append(MessageStoreHandler.convertToValidFilename(as2Info.getMessageId()));
        rawFilename.append(requestType);
        rawFilename.append(".as2");
        File headerFile = new File(rawFilename.toString() + ".header");
        FileOutputStream outStream = new FileOutputStream(headerFile);
        Enumeration enumeration = header.keys();
        while (enumeration.hasMoreElements()) {
            String key = (String) enumeration.nextElement();
            outStream.write((key + " = " + header.getProperty(key) + CRLF).getBytes());
        }
        outStream.close();
        outStream.flush();
        as2Info.setHeaderFilename(headerFile.getAbsolutePath());
        File rawFile = new File(rawFilename.toString());
        outStream = new FileOutputStream(rawFile);
        ByteArrayInputStream inStream = new ByteArrayInputStream(message.getDecryptedRawData());
        this.copyStreams(inStream, outStream);
        inStream.close();
        outStream.flush();
        outStream.close();
        byte[] contentSource = null;
        if (as2Info.isMDN()) {
            contentSource = message.getRawData();
        } else {
            contentSource = message.getDecryptedRawData();
        }
        File rawFileDecrypted = new File(rawFilename.toString() + ".decrypted");
        outStream = new FileOutputStream(rawFileDecrypted);
        inStream = new ByteArrayInputStream(contentSource);
        this.copyStreams(inStream, outStream);
        inStream.close();
        outStream.flush();
        outStream.close();
        for (int i = 0; i < message.getPayloadCount(); i++) {
            StringBuilder payloadFilename = new StringBuilder();
            payloadFilename.append(sentDir.getAbsolutePath()).append(File.separator);
            String originalFilename = message.getPayload(i).getOriginalFilename();
            if (originalFilename == null) {
                originalFilename = "unknown";
            }
            payloadFilename.append(MessageStoreHandler.convertToValidFilename(as2Info.getMessageId()));
            payloadFilename.append(".payload");
            File payloadFile = new File(payloadFilename.toString());
            message.getPayload(i).writeTo(payloadFile);
            message.getPayload(i).setPayloadFilename(payloadFile.getAbsolutePath());
        }
        //set all filenames to the message object
        as2Info.setRawFilename(rawFile.getAbsolutePath());
        as2Info.setHeaderFilename(headerFile.getAbsolutePath());
        //update the filenames in the db
        MessageAccessDB messageAccess = new MessageAccessDB(this.configConnection, this.runtimeConnection);
        if (!as2Info.isMDN()) {
            AS2MessageInfo messageInfo = (AS2MessageInfo) as2Info;
            messageInfo.setRawFilenameDecrypted(rawFileDecrypted.getAbsolutePath());
            messageAccess.updateFilenames(messageInfo);
        }
        messageAccess.insertPayload(message.getAS2Info().getMessageId(), message.getPayloads());
    }

    /**Converts a suggested filename to a valid filename. This may be necessary if as2 ids contain chars that are not allowed in
     *the current file system
     */
    public static String convertToValidFilename(String filename) {
        File file = new File(filename);
        //no trouble, file already exists on the file system
        if (file.exists()) {
            return (filename);
        }
        //seems not to be a valid filename, replace some chars
        StringBuilder buffer = new StringBuilder();
        for (int i = 0, length = filename.length(); i < length; i++) {
            char c = filename.charAt(i);
            int type = Character.getType(c);
            if (c == '@' || type == Character.DECIMAL_DIGIT_NUMBER || type == Character.LETTER_NUMBER || type == Character.LOWERCASE_LETTER || type == Character.OTHER_LETTER || type == Character.OTHER_NUMBER || type == Character.TITLECASE_LETTER || type == Character.UPPERCASE_LETTER) {
                buffer.append(c);
            } else {
                buffer.append('_');
            }
        }
        return (buffer.toString());
    }

    /**Stores the status information for outbound transactions in a file*/
    public void writeOutboundStatusFile(AS2MessageInfo messageInfo) throws Exception {
        //ignore the write process if this is not requested in the preferences
        if (!this.preferences.getBoolean(PreferencesAS2.WRITE_OUTBOUND_STATUS_FILE)) {
            return;
        }
        PartnerAccessDB partnerAccessDB = new PartnerAccessDB(this.configConnection, this.configConnection);
        Partner sender = partnerAccessDB.getPartner(messageInfo.getSenderId());
        Partner receiver = partnerAccessDB.getPartner(messageInfo.getReceiverId());
        MessageAccessDB access = new MessageAccessDB(this.configConnection, this.runtimeConnection);
        List<AS2Payload> payload = access.getPayload(messageInfo.getMessageId());
        //deal with the status directory
        File statusDir = new File("outboundstatus");
        //ensure the directory exists
        if (!statusDir.exists()) {
            boolean created = statusDir.mkdirs();
            if (!created) {
                this.logger.warning(this.rb.getResourceString("dir.createerror",
                        statusDir.getAbsolutePath()));
            }
        }
        StringBuilder rawFilename = new StringBuilder();
        rawFilename.append(statusDir.getAbsolutePath());
        rawFilename.append(File.separator);
        for (int i = 0; i < payload.size(); i++) {
            rawFilename.append(payload.get(i).getOriginalFilename());
            rawFilename.append("_");
        }
        rawFilename.append(messageInfo.getMessageId());
        rawFilename.append(".sent.state");
        File statusFile = new File(rawFilename.toString());
        FileOutputStream outStream = new FileOutputStream(statusFile);
        outStream.write("product=".getBytes());
        outStream.write(AS2ServerVersion.getProductName().getBytes());
        outStream.write(" ".getBytes());
        outStream.write(AS2ServerVersion.getVersion().getBytes());
        outStream.write(" ".getBytes());
        outStream.write(AS2ServerVersion.getBuild().getBytes());
        outStream.write("\n".getBytes());
        for (int i = 0; i < payload.size(); i++) {
            String originalFileKey = "originalfile." + i + "=";
            outStream.write(originalFileKey.getBytes());
            outStream.write(payload.get(i).getOriginalFilename().getBytes());
            outStream.write("\n".getBytes());
        }
        outStream.write("messageid=".getBytes());
        outStream.write(messageInfo.getMessageId().getBytes());
        outStream.write("\n".getBytes());
        outStream.write("sender=".getBytes());
        outStream.write(sender.getName().getBytes());
        outStream.write("\n".getBytes());
        outStream.write("senderAS2Id=".getBytes());
        outStream.write(sender.getAS2Identification().getBytes());
        outStream.write("\n".getBytes());
        outStream.write("receiver=".getBytes());
        outStream.write(receiver.getName().getBytes());
        outStream.write("\n".getBytes());
        outStream.write("receiverAS2Id=".getBytes());
        outStream.write(receiver.getAS2Identification().getBytes());
        outStream.write("\n".getBytes());
        outStream.write("state=".getBytes());
        if (messageInfo.getState() == AS2Message.STATE_FINISHED) {
            outStream.write("OK".getBytes());
        } else {
            outStream.write("ERROR".getBytes());
        }
        outStream.flush();
        outStream.close();
        this.logger.log(Level.FINE, this.rb.getResourceString("outboundstatus.written",
                new Object[]{
                    messageInfo.getMessageId(),
                    statusFile.getAbsolutePath()
                }), messageInfo);
    }
}
