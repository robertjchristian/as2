//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/message/AS2MessageParser.java,v 1.1 2012/04/18 14:10:30 heller Exp $
package de.mendelson.comm.as2.message;

import de.mendelson.comm.as2.AS2Exception;
import de.mendelson.comm.as2.notification.Notification;
import de.mendelson.util.security.cert.CertificateManager;
import de.mendelson.comm.as2.partner.Partner;
import de.mendelson.comm.as2.partner.PartnerAccessDB;
import de.mendelson.util.AS2Tools;
import de.mendelson.util.MecResourceBundle;
import de.mendelson.util.security.BCCryptoHelper;
import javax.mail.util.ByteArrayDataSource;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.activation.DataHandler;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import org.apache.commons.io.output.DeferredFileOutputStream;
import org.bouncycastle.cms.RecipientId;
import org.bouncycastle.cms.RecipientInformation;
import org.bouncycastle.cms.RecipientInformationStore;
import org.bouncycastle.cms.jcajce.JceKeyTransEnvelopedRecipient;
import org.bouncycastle.cms.jcajce.JceKeyTransRecipientId;
import org.bouncycastle.mail.smime.SMIMECompressed;
import org.bouncycastle.mail.smime.SMIMEEnveloped;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Analyzes and builds AS2 messages
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class AS2MessageParser {

    private Logger logger = null;
    /**Access to all certificates*/
    private CertificateManager certificateManagerSignature;
    private CertificateManager certificateManagerEncryption;
    private MecResourceBundle rb = null;
    private MecResourceBundle rbMessage = null;
    //DB connection
    private Connection configConnection = null;
    private Connection runtimeConnection = null;

    public AS2MessageParser() {
        //Load resourcebundle
        try {
            this.rb = (MecResourceBundle) ResourceBundle.getBundle(
                    ResourceBundleAS2MessageParser.class.getName());
            this.rbMessage = (MecResourceBundle) ResourceBundle.getBundle(
                    ResourceBundleAS2Message.class.getName());
        } //load up  resourcebundle
        catch (MissingResourceException e) {
            throw new RuntimeException("Oops..resource bundle " + e.getClassName() + " not found.");
        }
    }

    /**Passes the certificate manager to this class*/
    public void setCertificateManager(CertificateManager certificateManagerSignature, CertificateManager certificateManagerEncryption) {
        this.certificateManagerEncryption = certificateManagerEncryption;
        this.certificateManagerSignature = certificateManagerSignature;
    }

    /**Passes a db connection to this class*/
    public void setDBConnection(Connection configConnection, Connection runtimeConnection) {
        this.configConnection = configConnection;
        this.runtimeConnection = runtimeConnection;
    }

    /**Passes a logger to this class*/
    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    /**Unescapes the AS2-TO and AS2-FROM headers in receiver direction, related to
     * RFC 4130 section 6.2
     * @param identification as2-from or as2-to value to unescape
     * @return unescaped value
     */
    public static String unescapeFromToHeader(String identification) {
        StringBuilder builder = new StringBuilder();
        //remove quotes
        if (identification.startsWith("\"") && identification.endsWith("\"")) {
            identification = identification.substring(1, identification.length() - 1);
        }
        boolean inEscape = false;
        for (int i = 0; i < identification.length(); i++) {
            char singleChar = identification.charAt(i);
            if (singleChar == '\\') {
                if (inEscape) {
                    builder.append(singleChar);
                    inEscape = false;
                } else {
                    inEscape = true;
                }
            } else {
                builder.append(singleChar);
                inEscape = false;
            }

        }
        return (builder.toString());
    }

    /**Displays a bundle of byte arrays as hex string, for debug purpose only*/
    private String toHexDisplay(byte[] data) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < data.length; i++) {
            result.append(Integer.toString((data[i] & 0xff) + 0x100, 16).substring(1));
            result.append(" ");
        }
        return result.toString();
    }

    /**Uncompresses message data*/
    public byte[] decompressData(AS2MessageInfo info, byte[] data, String contentType) throws Exception {
        MimeBodyPart compressedPart = new MimeBodyPart();
        compressedPart.setDataHandler(new DataHandler(new ByteArrayDataSource(data, contentType)));
        compressedPart.setHeader("content-type", contentType);
        return (this.decompressData(info, new SMIMECompressed(compressedPart), compressedPart.getSize()));
    }

    /**Uncompresses message data*/
    public byte[] decompressData(AS2MessageInfo info, SMIMECompressed compressed, long compressedSize) throws Exception {
        byte[] decompressedData = compressed.getContent();
        info.setCompressionType(AS2Message.COMPRESSION_ZLIB);
        if (this.logger != null) {
            this.logger.log(Level.INFO, this.rb.getResourceString("data.compressed.expanded",
                    new Object[]{
                        info.getMessageId(), AS2Tools.getDataSizeDisplay(compressedSize),
                        AS2Tools.getDataSizeDisplay(decompressedData.length)
                    }), info);
        }
        return (decompressedData);
    }

    /**Decodes data by its content transfer encoding and returns it*/
    private byte[] decodeContentTransferEncoding(byte[] encodedData, String contentTransferEncoding) throws Exception {
        ByteArrayInputStream bais = new ByteArrayInputStream(encodedData);
        InputStream b64is = MimeUtility.decode(bais, contentTransferEncoding);
        byte[] tmp = new byte[encodedData.length];
        int n = b64is.read(tmp);
        byte[] res = new byte[n];
        System.arraycopy(tmp, 0, res, 0, n);
        return res;
    }

    /**If a content transfer encoding is set this will decode the data*/
    private byte[] processContentTransferEncoding(byte[] data, Properties header) throws Exception {
        if (!header.containsKey("content-transfer-encoding")) {
            //no content transfer encoding set: the AS2 default is "binary" in this case (NOT 7bit!), binary
            //content transfer encoding requires no decoding
            return (data);
        } else {
            String transferEncoding = header.getProperty("content-transfer-encoding");
            return (this.decodeContentTransferEncoding(data, transferEncoding));
        }
    }

    private AS2Message createFromMDNRequest(byte[] rawMessageData, Properties header, String contentType, AS2MDNInfo mdnInfo, MDNParser mdnParser) throws Exception {
        AS2MessageInfo relatedMessageInfo = new AS2MessageInfo();
        relatedMessageInfo.setMessageId(mdnInfo.getRelatedMessageId());
        //generate a new MDN id for this MDN if none is provided. message ids are not required for MDN in the RFC:
        //section 7.6 (Receipt Reply Considerations in HTTP POST) -->  "an MDN SHOULD have its own unique Message-ID header."
        if (mdnInfo.getMessageId() == null) {
            mdnInfo.setMessageId("<MDN_ON_" + mdnInfo.getRelatedMessageId() + ">");
        }
        MDNAccessDB mdnAccess = new MDNAccessDB(this.configConnection, this.runtimeConnection);
        MessageAccessDB messageAccess = new MessageAccessDB(this.configConnection, this.runtimeConnection);
        //check if related message id exists in db
        if (!messageAccess.messageIdExists(mdnInfo.getRelatedMessageId())) {
            //there is no way to log this persistent because the MDN is not related to any message: Just write the
            //incoming event to the log without binding it to a as message
            if (this.logger != null) {
                this.logger.log(Level.FINE, this.rb.getResourceString("mdn.incoming", mdnInfo.getMessageId()));
            }
            throw new Exception(this.rb.getResourceString("mdn.unexpected.messageid",
                    new Object[]{
                        mdnInfo.getMessageId(), mdnInfo.getRelatedMessageId()
                    }));
        }
        //do not add an MDN to a message that is already ok or stopped
        int messageState = messageAccess.getMessageState(mdnInfo.getRelatedMessageId());
        if (messageState != AS2Message.STATE_PENDING) {
            throw new Exception(this.rb.getResourceString("mdn.unexpected.state",
                    new Object[]{
                        mdnInfo.getMessageId(), mdnInfo.getRelatedMessageId()
                    }));
        }
        mdnAccess.initializeOrUpdateMDN(mdnInfo);
        if (this.logger != null) {
            this.logger.log(Level.FINE, this.rb.getResourceString("mdn.incoming", mdnInfo.getMessageId()), relatedMessageInfo);
        }
        relatedMessageInfo = messageAccess.getLastMessageEntry(mdnInfo.getRelatedMessageId());
        if (this.logger != null) {
            this.logger.log(Level.INFO, this.rb.getResourceString("mdn.answerto",
                    new Object[]{
                        mdnInfo.getMessageId(), mdnInfo.getRelatedMessageId()
                    }), relatedMessageInfo);
        }
        if (mdnParser.getDispositionState() != null) {
            //failure in processing message on remote AS2 server: log the failure and set transaction state
            if (mdnParser.getDispositionState().toLowerCase().indexOf("failed") >= 0 || mdnParser.getDispositionState().toLowerCase().indexOf("error") >= 0) {
                if (this.logger != null) {
                    this.logger.log(Level.SEVERE,
                            this.rb.getResourceString("mdn.state",
                            new Object[]{
                                mdnInfo.getMessageId(), mdnParser.getDispositionState()
                            }), relatedMessageInfo);
                    this.logger.log(Level.SEVERE,
                            this.rb.getResourceString("mdn.details",
                            new Object[]{
                                mdnInfo.getMessageId(), mdnParser.getMdnDetails()
                            }), relatedMessageInfo);
                }
                mdnInfo.setState(AS2Message.STATE_STOPPED);
                mdnInfo.setRemoteMDNText("[" + mdnParser.getDispositionState() + "] " + mdnParser.getMdnDetails());
                mdnAccess.initializeOrUpdateMDN(mdnInfo);
                relatedMessageInfo.setState(AS2Message.STATE_STOPPED);
                //update the related message in the database. This has to be performed because 
                //of the notification
                messageAccess.setMessageState(relatedMessageInfo.getMessageId(), AS2Message.STATE_STOPPED);
                messageAccess.initializeOrUpdateMessage(relatedMessageInfo);
            } else {
                //message has been processed: log the found state
                if (this.logger != null) {
                    this.logger.log(Level.FINE,
                            this.rb.getResourceString("mdn.state",
                            new Object[]{
                                mdnInfo.getMessageId(), mdnParser.getDispositionState()
                            }), relatedMessageInfo);
                    this.logger.log(Level.FINE,
                            this.rb.getResourceString("mdn.details",
                            new Object[]{
                                mdnInfo.getMessageId(), mdnParser.getMdnDetails()
                            }), relatedMessageInfo);
                }
                mdnInfo.setState(AS2Message.STATE_FINISHED);
                mdnInfo.setRemoteMDNText("[" + mdnParser.getDispositionState() + "] " + mdnParser.getMdnDetails());
                mdnAccess.initializeOrUpdateMDN(mdnInfo);
                //relatedMessageInfo.setState(AS2Message.STATE_FINISHED);
                messageAccess.initializeOrUpdateMessage(relatedMessageInfo);
            }
        }
        AS2Message message = new AS2Message(mdnInfo);
        message.setRawData(rawMessageData);
        //check for existing partners
        PartnerAccessDB partnerAccess = new PartnerAccessDB(this.configConnection, this.runtimeConnection);
        Partner sender = partnerAccess.getPartner(mdnInfo.getSenderId());
        Partner receiver = partnerAccess.getPartner(mdnInfo.getReceiverId());
        if (sender == null) {
            throw new AS2Exception(AS2Exception.UNKNOWN_TRADING_PARTNER_ERROR,
                    "Sender AS2 id " + mdnInfo.getSenderId() + " is unknown.", message);
        }
        if (receiver == null) {
            throw new AS2Exception(AS2Exception.UNKNOWN_TRADING_PARTNER_ERROR,
                    "Receiver AS2 id " + mdnInfo.getReceiverId() + " is unknown.", message);
        }
        if (!receiver.isLocalStation()) {
            throw new AS2Exception(AS2Exception.PROCESSING_ERROR,
                    "The receiver of the message (" + receiver.getAS2Identification() + ") is not defined as a local station.",
                    message);
        }
        message.setDecryptedRawData(rawMessageData);
        Part payloadPart = this.verifySignature(message, sender, contentType);
        //is it a MDN and everything performed well up to here? Then perform the MIC check
        if (mdnInfo.getState() == AS2Message.STATE_FINISHED) {
            this.checkMDNReceivedContentMIC(mdnInfo);
        }
        //this is a signed MDN
        if (message.getAS2Info().getSignType() != AS2Message.SIGNATURE_NONE) {
            this.writePayloadsToMessage(payloadPart, message, header);
        } else {
            //this is an unsigned mdn
            this.writePayloadsToMessage(message.getDecryptedRawData(), message, header);
        }
        return (message);
    }

    /**Analyzes and creates passed message data
     */
    public AS2Message createMessageFromRequest(byte[] rawMessageData, Properties header, String contentType) throws AS2Exception {
        AS2Message message = new AS2Message(new AS2MessageInfo());
        if (this.configConnection == null) {
            throw new AS2Exception(AS2Exception.PROCESSING_ERROR,
                    "AS2MessageParser: Pass a DB connection before calling createMessageFromRequest()", message);
        }
        try {
            //decode the content transfer encoding if set
            rawMessageData = this.processContentTransferEncoding(rawMessageData, header);
            //check if this is a MDN
            MDNParser mdnParser = new MDNParser();
            AS2MDNInfo mdnInfo = mdnParser.parseMDNData(rawMessageData, contentType);
            //its a MDN
            if (mdnInfo != null) {
                message.setAS2Info(mdnInfo);
                mdnInfo.initializeByRequestHeader(header);
                return (this.createFromMDNRequest(rawMessageData, header, contentType, mdnInfo, mdnParser));
            } else {
                AS2MessageInfo messageInfo = (AS2MessageInfo) message.getAS2Info();
                messageInfo.initializeByRequestHeader(header);
                //inbound AS2 message, no MDN
                //no futher processing if the message does not contain a message id
                if (messageInfo.getMessageId() == null) {
                    return (message);
                }
                //figure out if the MDN should be signed NOW. If an error occurs beyond this point
                //the info has to know if the returned MDN should be signed
                messageInfo.getDispositionNotificationOptions().setHeaderValue(
                        header.getProperty("disposition-notification-options"));                
                //check for existing partners
                PartnerAccessDB partnerAccess = new PartnerAccessDB(this.configConnection, this.runtimeConnection);
                Partner sender = partnerAccess.getPartner(messageInfo.getSenderId());
                Partner receiver = partnerAccess.getPartner(messageInfo.getReceiverId());
                if (sender == null) {
                    throw new AS2Exception(AS2Exception.UNKNOWN_TRADING_PARTNER_ERROR,
                            "Sender AS2 id " + messageInfo.getSenderId() + " is unknown.", message);
                }
                if (receiver == null) {
                    throw new AS2Exception(AS2Exception.UNKNOWN_TRADING_PARTNER_ERROR,
                            "Receiver AS2 id " + messageInfo.getReceiverId() + " is unknown.", message);
                }
                if (!receiver.isLocalStation()) {
                    throw new AS2Exception(AS2Exception.PROCESSING_ERROR,
                            "The receiver of the message (" + receiver.getAS2Identification() + ") is not defined as a local station.",
                            message);
                }
                MessageAccessDB messageAccess = new MessageAccessDB(this.configConnection, this.runtimeConnection);                
                //check if the message already exists
                AS2MessageInfo alreadyExistingInfo = this.messageAlreadyExists(messageAccess, messageInfo.getMessageId());
                if( alreadyExistingInfo != null ){
                    //perform notification: Resend detected, manual interaction might be required
                    Notification notification = new Notification(this.configConnection, this.runtimeConnection);
                    notification.sendResendDetected(messageInfo, alreadyExistingInfo, sender, receiver);
                    throw new AS2Exception(
                            AS2Exception.PROCESSING_ERROR,
                            "An AS2 message with the message id " + messageInfo.getMessageId()
                            + " has been already processed successfully by the system or is pending ("
                            + alreadyExistingInfo.getInitDate() + "). Please "
                            + " resubmit the message with a new message id instead or resending it if it should be processed again.",
                            new AS2Message(messageInfo));
                }
                messageAccess.initializeOrUpdateMessage(messageInfo);      
                if (this.logger != null) {
                    //do not log before because the logging process is related to an already created message in the transaction log
                    this.logger.log(Level.FINE, this.rb.getResourceString("msg.incoming",
                            new Object[]{
                                messageInfo.getMessageId(),
                                AS2Tools.getDataSizeDisplay(rawMessageData.length)
                            }), messageInfo);
                }
                //indicates if a sync or async mdn is requested
                messageInfo.setAsyncMDNURL(header.getProperty("receipt-delivery-option"));
                messageInfo.setRequestsSyncMDN(header.getProperty("receipt-delivery-option") == null || header.getProperty("receipt-delivery-option").trim().length() == 0);
                message.setRawData(rawMessageData);                
                byte[] decryptedData = this.decryptMessage(message, rawMessageData, contentType, sender, receiver);
                //may be already compressed here. Decompress first before going further
                if (this.contentTypeIndicatesCompression(contentType)) {
                    byte[] decompressed = this.decompressData((AS2MessageInfo) message.getAS2Info(), decryptedData, contentType);
                    message.setDecryptedRawData(decompressed);
                    //content type has changed now, get it from the decompressed data
                    ByteArrayInputStream memIn = new ByteArrayInputStream(decompressed);
                    MimeBodyPart tempPart = new MimeBodyPart(memIn);
                    memIn.close();
                    contentType = tempPart.getContentType();
                } else {
                    //check the MIME structure that is embedded in decryptedData for its content type
                    ByteArrayInputStream memIn = new ByteArrayInputStream(decryptedData);
                    MimeMessage possibleCompressedPart = new MimeMessage(Session.getInstance(System.getProperties()), memIn);
                    memIn.close();
                    if (this.contentTypeIndicatesCompression(possibleCompressedPart.getContentType())) {
                        long compressedSize = possibleCompressedPart.getSize();
                        byte[] decompressed = this.decompressData((AS2MessageInfo) message.getAS2Info(), new SMIMECompressed(possibleCompressedPart), compressedSize);
                        message.setDecryptedRawData(decompressed);
                    } else {
                        message.setDecryptedRawData(decryptedData);
                    }
                }
                decryptedData = null;
                Part payloadPart = this.verifySignature(message, sender, contentType);
                //decompress the data if it has been sent compressed, only possible for signed data
                if (message.getAS2Info().getSignType() != AS2Message.SIGNATURE_NONE) {
                    //signed message:
                    //http://tools.ietf.org/html/draft-ietf-ediint-compression-12
                    //4.1 MIC Calculation For Signed Message
                    //For any signed message, the MIC to be returned is calculated over
                    //the same data that was signed in the original message as per [AS1].
                    //The signed content will be a mime bodypart that contains either
                    //compressed or uncompressed data.                    
                    this.computeReceivedContentMIC(rawMessageData, message, payloadPart, contentType);
                    payloadPart = this.decompressData(payloadPart, message);
                    this.writePayloadsToMessage(payloadPart, message, header);
                } else {
                    //this is an unsigned message
                    this.writePayloadsToMessage(message.getDecryptedRawData(), message, header);
                    //unsigned message:
                    //http://tools.ietf.org/html/draft-ietf-ediint-compression-12
                    //4.2 MIC Calculation For Encrypted, Unsigned Message
                    //For encrypted, unsigned messages, the MIC to be returned is
                    //calculated over the uncompressed data content including all
                    //MIME header fields and any applied Content-Transfer-Encoding.

                    //http://tools.ietf.org/html/draft-ietf-ediint-compression-12
                    //4.3 MIC Calculation For Unencrypted, Unsigned Message
                    //For unsigned, unencrypted messages, the MIC is calculated
                    //over the uncompressed data content including all MIME header
                    //fields and any applied Content-Transfer-Encoding.                    
                    this.computeReceivedContentMIC(rawMessageData, message, payloadPart, contentType);
                }
                if (this.logger != null) {
                    this.logger.log(Level.INFO, this.rb.getResourceString("found.attachments",
                            new Object[]{messageInfo.getMessageId(), String.valueOf(message.getPayloadCount())}),
                            messageInfo);
                }
                return (message);
            }
        } catch (Exception e) {
            if (e instanceof AS2Exception) {
                throw (AS2Exception) e;
            } else {
                throw new AS2Exception(AS2Exception.PROCESSING_ERROR,
                        e.getMessage(), message);
            }
        }
    }

    /**
     * Sending implementations MUST be capable of configuring a maximum number
    of retries, and MUST stop retrying either when a successful send
    occurs or when the total retry number is reached.
     * @return
     */
    private AS2MessageInfo messageAlreadyExists(MessageAccessDB messageAccess, String messageId) {
        List<AS2MessageInfo> infos = messageAccess.getMessageOverview(messageId);
        if( infos != null && !infos.isEmpty()){
            return( infos.get(0));
        }
        return( null);
    }

    /**Checks if the received content mic matches the send message mic. The content mic field is optional if
     * the MDN is unsigned, see RFC 4130 section 7.4.3:
     * The "Received-content-MIC" extension field is set when the integrity of the received
     * message is verified. The MIC is the base64-encoded message-digest computed over the
     * received message with a hash function. This field is required for signed receipts but
     * optional for unsigned receipts.
     */
    private void checkMDNReceivedContentMIC(AS2MDNInfo mdnMessageInfo) throws AS2Exception {
        //ignore this check if the received content mic has not been set in the MDN and the MDN is unsigned
        if (mdnMessageInfo.getSignType() == AS2Message.SIGNATURE_NONE && mdnMessageInfo.getReceivedContentMIC() == null) {
            return;
        }
        MessageAccessDB messageAccess = new MessageAccessDB(this.configConnection, this.runtimeConnection);
        AS2MessageInfo relatedMessageInfo = messageAccess.getLastMessageEntry(mdnMessageInfo.getRelatedMessageId());
        BCCryptoHelper helper = new BCCryptoHelper();
        if (helper.micIsEqual(mdnMessageInfo.getReceivedContentMIC(), relatedMessageInfo.getReceivedContentMIC())) {
            if (this.logger != null) {
                this.logger.log(Level.INFO,
                        this.rb.getResourceString("contentmic.match",
                        new Object[]{
                            mdnMessageInfo.getMessageId()}), relatedMessageInfo);
            }
        } else {
            if (this.logger != null) {
                this.logger.log(Level.INFO,
                        this.rb.getResourceString("contentmic.failure",
                        new Object[]{
                            mdnMessageInfo.getMessageId(),
                            relatedMessageInfo.getReceivedContentMIC(),
                            mdnMessageInfo.getReceivedContentMIC()
                        }), relatedMessageInfo);
            }
            //uncomment for ERROR if mic does not match
//            throw new AS2Exception(AS2Exception.INTEGRITY_ERROR,
//                    this.rb.getResourceString("contentmic.failure",
//                    new Object[]{
//                        mdnMessageInfo.getMessageId(),
//                        relatedMessageInfo.getReceivedContentMIC(),
//                        mdnMessageInfo.getReceivedContentMIC()
//                    }), message);
        }
    }

    /**Writes a passed payload data to the passed message object. Could be called from either the MDN
     * processing or the message processing
     */
    public void writePayloadsToMessage(byte[] data, AS2Message message, Properties header) throws Exception {
        ByteArrayOutputStream payloadOut = new ByteArrayOutputStream();
        MimeMessage testMessage = new MimeMessage(Session.getInstance(System.getProperties()), new ByteArrayInputStream(data));
        //multiple attachments?
        if (testMessage.isMimeType("multipart/*")) {
            this.writePayloadsToMessage(testMessage, message, header);
            return;
        }
        InputStream payloadIn = null;
        AS2Info info = message.getAS2Info();
        if (info instanceof AS2MessageInfo
                && info.getSignType() == AS2Message.SIGNATURE_NONE
                && ((AS2MessageInfo) info).getCompressionType() == AS2Message.COMPRESSION_NONE) {
            payloadIn = new ByteArrayInputStream(data);
        } else if (testMessage.getSize() > 0) {
            payloadIn = testMessage.getInputStream();
        } else {
            payloadIn = new ByteArrayInputStream(data);
        }
        this.copyStreams(payloadIn, payloadOut);
        payloadOut.flush();
        payloadOut.close();
        byte[] payloadData = payloadOut.toByteArray();
        AS2Payload as2Payload = new AS2Payload();
        as2Payload.setData(payloadData);
        String contentIdHeader = header.getProperty("content-id");
        if (contentIdHeader != null) {
            as2Payload.setContentId(contentIdHeader);
        }
        String contentTypeHeader = header.getProperty("content-type");
        if (contentTypeHeader != null) {
            as2Payload.setContentType(contentTypeHeader);
        }
        try {
            as2Payload.setOriginalFilename(testMessage.getFileName());
        } catch (MessagingException e) {
            if (this.logger != null) {
                this.logger.log(Level.WARNING, this.rb.getResourceString("filename.extraction.error",
                        new Object[]{
                            info.getMessageId(),
                            e.getMessage(),}), info);
            }
        }
        if (as2Payload.getOriginalFilename() == null) {
            String filenameHeader = header.getProperty("content-disposition");
            if (filenameHeader != null) {
                //test part for convinience: extract file name
                MimeBodyPart filenamePart = new MimeBodyPart();
                filenamePart.setHeader("content-disposition", filenameHeader);
                try {
                    as2Payload.setOriginalFilename(filenamePart.getFileName());
                } catch (MessagingException e) {
                    if (this.logger != null) {
                        this.logger.log(Level.WARNING, this.rb.getResourceString("filename.extraction.error",
                                new Object[]{
                                    info.getMessageId(),
                                    e.getMessage(),}), info);
                    }
                }
            }
        }
        message.addPayload(as2Payload);
    }

    /**Writes a passed payload part to the passed message object. 
     */
    public void writePayloadsToMessage(Part payloadPart, AS2Message message, Properties header) throws Exception {
        List<Part> attachmentList = new ArrayList<Part>();
        AS2Info info = message.getAS2Info();
        if (!info.isMDN()) {
            AS2MessageInfo messageInfo = (AS2MessageInfo) message.getAS2Info();
            if (payloadPart.isMimeType("multipart/*")) {
                //check if it is a CEM
                if (payloadPart.getContentType().toLowerCase().contains("application/ediint-cert-exchange+xml")) {
                    messageInfo.setMessageType(AS2Message.MESSAGETYPE_CEM);
                    if (this.logger != null) {
                        this.logger.log(Level.FINE, this.rb.getResourceString("found.cem",
                                new Object[]{
                                    messageInfo.getMessageId(),
                                    message
                                }), info);
                    }
                }
                ByteArrayOutputStream mem = new ByteArrayOutputStream();
                payloadPart.writeTo(mem);
                mem.flush();
                mem.close();
                MimeMultipart multipart = new MimeMultipart(
                        new ByteArrayDataSource(mem.toByteArray(), payloadPart.getContentType()));
                //add all attachments to the message
                for (int i = 0; i < multipart.getCount(); i++) {
                    //its possible that one of the bodyparts is the signature (for compressed/signed messages), skip the signature
                    if (!multipart.getBodyPart(i).getContentType().toLowerCase().contains("pkcs7-signature")) {
                        attachmentList.add(multipart.getBodyPart(i));
                    }
                }
            } else {
                attachmentList.add(payloadPart);
            }
        } else {
            //its a MDN, write whole part
            attachmentList.add(payloadPart);
        }
        //write the parts
        for (Part attachmentPart : attachmentList) {
            ByteArrayOutputStream payloadOut = new ByteArrayOutputStream();
            InputStream payloadIn = attachmentPart.getInputStream();
            this.copyStreams(payloadIn, payloadOut);
            payloadOut.flush();
            payloadOut.close();
            byte[] data = payloadOut.toByteArray();
            AS2Payload as2Payload = new AS2Payload();
            as2Payload.setData(data);
            String[] contentIdHeader = attachmentPart.getHeader("content-id");
            if (contentIdHeader != null && contentIdHeader.length > 0) {
                as2Payload.setContentId(contentIdHeader[0]);
            }
            String[] contentTypeHeader = attachmentPart.getHeader("content-type");
            if (contentTypeHeader != null && contentTypeHeader.length > 0) {
                as2Payload.setContentType(contentTypeHeader[0]);
            }
            try {
                as2Payload.setOriginalFilename(payloadPart.getFileName());
            } catch (MessagingException e) {
                if (this.logger != null) {
                    this.logger.log(Level.WARNING, this.rb.getResourceString("filename.extraction.error",
                            new Object[]{
                                info.getMessageId(),
                                e.getMessage(),}), info);
                }
            }
            if (as2Payload.getOriginalFilename() == null) {
                String filenameheader = header.getProperty("content-disposition");
                if (filenameheader != null) {
                    //test part for convinience: extract file name
                    MimeBodyPart filenamePart = new MimeBodyPart();
                    filenamePart.setHeader("content-disposition", filenameheader);
                    try {
                        as2Payload.setOriginalFilename(filenamePart.getFileName());
                    } catch (MessagingException e) {
                        if (this.logger != null) {
                            this.logger.log(Level.WARNING, this.rb.getResourceString("filename.extraction.error",
                                    new Object[]{
                                        info.getMessageId(),
                                        e.getMessage(),}), info);
                        }
                    }
                }
            }
            message.addPayload(as2Payload);
        }
    }

    /**Computes the received content MIC and writes it to the message info object
     */
    public void computeReceivedContentMIC(byte[] rawMessageData, AS2Message message, Part partWithHeader, String contentType) throws Exception {
        AS2MessageInfo messageInfo = (AS2MessageInfo) message.getAS2Info();
        boolean encrypted = messageInfo.getEncryptionType() != AS2Message.ENCRYPTION_NONE;
        boolean signed = messageInfo.getSignType() != AS2Message.SIGNATURE_NONE;
        boolean compressed = messageInfo.getCompressionType() != AS2Message.COMPRESSION_NONE;
        BCCryptoHelper helper = new BCCryptoHelper();
        String sha1digestOID = helper.convertAlgorithmNameToOID(BCCryptoHelper.ALGORITHM_SHA1);
        //compute the MIC
        if (signed) {
            //compute the content-type for the signed part.
            //If the message was not encrypted the content-type should simply be taken from the header
            //else we have to look into the part
            String singedPartContentType = null;
            if (!encrypted) {
                singedPartContentType = contentType;
            } else {
                InputStream dataIn = message.getDecryptedRawDataInputStream();
                MimeBodyPart contentTypeTempPart = new MimeBodyPart(dataIn);
                dataIn.close();
                singedPartContentType = contentTypeTempPart.getContentType();
            }
            //ANY signed data
            //4.1 MIC Calculation For Signed Message
            //For any signed message, the MIC to be returned is calculated over
            //the same data that was signed in the original message as per [AS1].
            //The signed content will be a mime bodypart that contains either
            //compressed or uncompressed data.
            MimeBodyPart signedPart = new MimeBodyPart();
            signedPart.setDataHandler(new DataHandler(new ByteArrayDataSource(message.getDecryptedRawData(), contentType)));
            signedPart.setHeader("Content-Type", singedPartContentType);
            String digestOID = helper.getDigestAlgOIDFromSignature(signedPart);
            signedPart = null;
            String mic = helper.calculateMIC(partWithHeader, digestOID);
            String digestAlgorithmName = helper.convertOIDToAlgorithmName(digestOID);
            messageInfo.setReceivedContentMIC(mic + ", " + digestAlgorithmName);
        } else if (!signed && !compressed && !encrypted) {
            //uncompressed, unencrypted, unsigned: plaintext mic
            //http://tools.ietf.org/html/draft-ietf-ediint-compression-12
            //4.3 MIC Calculation For Unencrypted, Unsigned Message
            //For unsigned, unencrypted messages, the MIC is calculated
            //over the uncompressed data content including all MIME header
            //fields and any applied Content-Transfer-Encoding.
            String mic = helper.calculateMIC(rawMessageData, sha1digestOID);
            messageInfo.setReceivedContentMIC(mic + ", sha1");
        } else if (!signed && compressed && !encrypted) {
            //compressed, unencrypted, unsigned: uncompressed data mic
            //http://tools.ietf.org/html/draft-ietf-ediint-compression-12
            //4.3 MIC Calculation For Unencrypted, Unsigned Message
            //For unsigned, unencrypted messages, the MIC is calculated
            //over the uncompressed data content including all MIME header
            //fields and any applied Content-Transfer-Encoding.
            String mic = helper.calculateMIC(message.getDecryptedRawData(), sha1digestOID);
            messageInfo.setReceivedContentMIC(mic + ", sha1");
        } else if (!signed && encrypted) {
            //http://tools.ietf.org/html/draft-ietf-ediint-compression-12
            //4.2 MIC Calculation For Encrypted, Unsigned Message
            //For encrypted, unsigned messages, the MIC to be returned is
            //calculated over the uncompressed data content including all
            //MIME header fields and any applied Content-Transfer-Encoding.
            String mic = helper.calculateMIC(message.getDecryptedRawData(), sha1digestOID);
            messageInfo.setReceivedContentMIC(mic + ", sha1");
        } else {
            //this should never happen:
            String mic = helper.calculateMIC(partWithHeader, sha1digestOID);
            messageInfo.setReceivedContentMIC(mic + ", sha1");
        }
    }

    /**Returns a compressed part of this container if it exists, else null. If the container itself
     *is compressed it is returned.
     */
    public Part getCompressedEmbeddedPart(Part part) throws MessagingException, IOException {
        if (this.contentTypeIndicatesCompression(part.getContentType())) {
            return (part);
        }
        if (part.isMimeType("multipart/*")) {
            Multipart multiPart = (Multipart) part.getContent();
            int count = multiPart.getCount();
            for (int i = 0; i < count; i++) {
                BodyPart bodyPart = multiPart.getBodyPart(i);
                Part compressedEmbeddedPart = this.getCompressedEmbeddedPart(bodyPart);
                if (compressedEmbeddedPart != null) {
                    return (compressedEmbeddedPart);
                }
            }
        }
        return (null);
    }

    /**Returns the signed part of the passed data or null if the data is not detected to be signed*/
    public Part getSignedPart(byte[] data, String contentType) throws Exception {
        BCCryptoHelper helper = new BCCryptoHelper();
        MimeBodyPart possibleSignedPart = new MimeBodyPart();
        possibleSignedPart.setDataHandler(new DataHandler(new ByteArrayDataSource(data, contentType)));
        possibleSignedPart.setHeader("content-type", contentType);
        return (helper.getSignedEmbeddedPart(possibleSignedPart));
    }

    /**Verifies the signature of the passed signed part*/
    public MimeBodyPart verifySignedPart(Part signedPart, byte[] data, String contentType, X509Certificate certificate) throws Exception {
        BCCryptoHelper helper = new BCCryptoHelper();
        String signatureTransferEncoding = null;
        MimeMultipart checkPart = (MimeMultipart) signedPart.getContent();
        //it is sure that it is a signed part: set the type to multipart if the
        //parser has problems parsing it. Don't know why sometimes a parsing fails for
        //MimeBodyPart. This check looks if the parser is able to find more than one subpart
        if (checkPart.getCount() == 1) {
            MimeMultipart multipart = new MimeMultipart(
                    new ByteArrayDataSource(data, contentType));
            MimeMessage possibleSignedMessage = new MimeMessage(Session.getInstance(System.getProperties(), null));
            possibleSignedMessage.setContent(multipart, multipart.getContentType());
            possibleSignedMessage.saveChanges();
            //overwrite the formerly found signed part
            signedPart = helper.getSignedEmbeddedPart(possibleSignedMessage);
        }
        //get the content encoding of the signature
        MimeMultipart signedMultiPart = (MimeMultipart) signedPart.getContent();
        //body part 1 is always the signature
        String encodingHeader[] = signedMultiPart.getBodyPart(1).getHeader("Content-Transfer-Encoding");
        if (encodingHeader != null) {
            signatureTransferEncoding = encodingHeader[0];
        }
        return (helper.verify(signedPart, signatureTransferEncoding, certificate));
    }

    /**Verifies the signature of the passed message. If the transfer mode is unencrypted/unsigned, a new Bodypart will be constructed
     *@return the payload part, this is important to compute the MIC later
     */
    private Part verifySignature(AS2Message message, Partner sender, String contentType) throws Exception {
        if (this.certificateManagerSignature == null) {
            throw new AS2Exception(AS2Exception.PROCESSING_ERROR,
                    "AS2MessageParser.verifySignature: pass a certification manager for the signature before calling verifySignature()", message);
        }
        AS2Info as2Info = message.getAS2Info();
        if (!as2Info.isMDN()) {
            AS2MessageInfo messageInfo = (AS2MessageInfo) as2Info;
            if (messageInfo.getEncryptionType() != AS2Message.ENCRYPTION_NONE) {
                InputStream memIn = message.getDecryptedRawDataInputStream();
                MimeBodyPart testPart = new MimeBodyPart(memIn);
                memIn.close();
                contentType = testPart.getContentType();
            }
        }
        Part signedPart = this.getSignedPart(message.getDecryptedRawData(), contentType);
        //part is NOT signed but is defined to be signed
        if (signedPart == null) {
            as2Info.setSignType(AS2Message.SIGNATURE_NONE);
            if (as2Info.isMDN()) {
                this.logger.log(Level.INFO, this.rb.getResourceString("mdn.notsigned", as2Info.getMessageId()), as2Info);
                //MDN is not signed but should be signed
                if (sender.isSignedMDN()) {
                    this.logger.log(Level.SEVERE, this.rb.getResourceString("mdn.unsigned.error",
                            new Object[]{
                                as2Info.getMessageId(),
                                sender.getName(),}), as2Info);
                }
            } else {
                this.logger.log(Level.INFO, this.rb.getResourceString("msg.notsigned", as2Info.getMessageId()), as2Info);
            }
            if (!as2Info.isMDN() && sender.getSignType() != AS2Message.SIGNATURE_NONE) {
                throw new AS2Exception(AS2Exception.INSUFFICIENT_SECURITY_ERROR,
                        "Incoming messages from AS2 partner " + sender.getAS2Identification() + " are defined to be signed.",
                        message);
            }
            //if the message has been unsigned it is required to set a new datasource
            MimeBodyPart unsignedPart = new MimeBodyPart();
            unsignedPart.setDataHandler(new DataHandler(new ByteArrayDataSource(message.getDecryptedRawData(), contentType)));
            unsignedPart.setHeader("content-type", contentType);
            return (unsignedPart);
        } else {
            //it is definitly a signed mdn
            if (as2Info.isMDN()) {
                if (this.logger != null) {
                    this.logger.log(Level.INFO, this.rb.getResourceString("mdn.signed", as2Info.getMessageId()), as2Info);
                }
                as2Info.setSignType(this.getDigestFromSignature(signedPart));
                //MDN is signed but shouldn't be signed'
                if (!sender.isSignedMDN()) {
                    if (this.logger != null) {
                        this.logger.log(Level.WARNING, this.rb.getResourceString("mdn.signed.error",
                                new Object[]{
                                    as2Info.getMessageId(),
                                    sender.getName(),}), as2Info);
                    }
                }
            } else {
                //its no MDN, its a AS2 message
                if (this.logger != null) {
                    this.logger.log(Level.INFO, this.rb.getResourceString("msg.signed", as2Info.getMessageId()), as2Info);
                }
                int signDigest = this.getDigestFromSignature(signedPart);
                String digest = null;
                if (signDigest == AS2Message.SIGNATURE_SHA1) {
                    digest = "SHA1";
                } else if (signDigest == AS2Message.SIGNATURE_MD5) {
                    digest = "MD5";
                }
                as2Info.setSignType(signDigest);
                if (this.logger != null) {
                    this.logger.log(Level.INFO, this.rb.getResourceString("signature.analyzed.digest",
                            new Object[]{
                                as2Info.getMessageId(), digest
                            }), as2Info);
                }
            }
        }
        MimeBodyPart payloadPart = null;
        try {
            String signAlias = this.certificateManagerSignature.getAliasByFingerprint(sender.getSignFingerprintSHA1());
            payloadPart = this.verifySignedPartUsingAlias(message, signAlias, signedPart, contentType);
        } catch (AS2Exception e) {
            //retry to verify the signature with the second certificate if this is possible
            String secondAlias = this.certificateManagerSignature.getAliasByFingerprint(sender.getSignFingerprintSHA1(2));
            if (secondAlias != null) {
                payloadPart = this.verifySignedPartUsingAlias(message, secondAlias, signedPart, contentType);
            } else {
                throw e;
            }
        }
        return (payloadPart);
    }

    private MimeBodyPart verifySignedPartUsingAlias(AS2Message message, String alias, Part signedPart, String contentType) throws Exception {
        AS2Info info = message.getAS2Info();
        if (this.logger != null) {
            this.logger.log(Level.INFO, this.rb.getResourceString("signature.using.alias",
                    new Object[]{
                        info.getMessageId(), alias
                    }), info);
        }
        X509Certificate certificate = this.certificateManagerSignature.getX509Certificate(alias);
        MimeBodyPart payloadPart = null;
        try {
            payloadPart = this.verifySignedPart(signedPart, message.getDecryptedRawData(), contentType, certificate);
        } catch (Exception e) {
            if (this.logger != null) {
                this.logger.log(Level.INFO, this.rb.getResourceString("signature.failure",
                        new Object[]{
                            info.getMessageId(),
                            e.getMessage()
                        }), info);
            }
            throw new AS2Exception(AS2Exception.AUTHENTIFICATION_ERROR,
                    "Error verifying the senders digital signature: " + e.getMessage() + ".",
                    message);
        }
        if (this.logger != null) {
            this.logger.log(Level.INFO, this.rb.getResourceString("signature.ok", info.getMessageId()), info);
        }
        return (payloadPart);
    }


    /*Returns the digest of the signature, as constant of AS2Message
     */
    public int getDigestFromSignature(Part signedPart) throws Exception {
        BCCryptoHelper helper = new BCCryptoHelper();
        String digestOID = helper.getDigestAlgOIDFromSignature(signedPart);
        String as2Digest = helper.convertOIDToAlgorithmName(digestOID);
        if (as2Digest.equalsIgnoreCase(BCCryptoHelper.ALGORITHM_SHA1)) {
            return (AS2Message.SIGNATURE_SHA1);
        }
        if (as2Digest.equalsIgnoreCase(BCCryptoHelper.ALGORITHM_MD5)) {
            return (AS2Message.SIGNATURE_MD5);
        }
        //should never happen because unknown algorithms are thrown already by the conversion method
        return (-1);
    }

    /**Returns if the content type indicates a message encryption*/
    public boolean contentTypeIndicatesEncryption(String contentType) {
        return (contentType.toLowerCase().contains("application/pkcs7-mime"));
    }

    /**Returns if the content type indicates message compression*/
    public boolean contentTypeIndicatesCompression(String contentType) {
        return (contentType.toLowerCase().contains("compressed-data"));
    }

    /**Decrypts the data of a message with all given certificates etc
     * @param info MessageInfo, the encryption algorith will be stored in the encryption type of this info
     * @param rawMessageData encrypted data, will be decrypted
     * @param contentType contentType of the data
     * @param privateKey receivers private key
     * @param certificate receivers certificate
     */
    public byte[] decryptData(AS2Message message, byte[] data, String contentType, PrivateKey privateKeyReceiver, X509Certificate certificateReceiver, String receiverCryptAlias) throws Exception {
        AS2MessageInfo info = (AS2MessageInfo) message.getAS2Info();
        MimeBodyPart encryptedBody = new MimeBodyPart();
        encryptedBody.setHeader("content-type", contentType);
        encryptedBody.setDataHandler(new DataHandler(new ByteArrayDataSource(data, contentType)));
        RecipientId recipientId = new JceKeyTransRecipientId(certificateReceiver);
        SMIMEEnveloped enveloped = new SMIMEEnveloped(encryptedBody);
        BCCryptoHelper helper = new BCCryptoHelper();
        String algorithm = helper.convertOIDToAlgorithmName(enveloped.getEncryptionAlgOID());
        if (algorithm.equals(BCCryptoHelper.ALGORITHM_3DES)) {
            info.setEncryptionType(AS2Message.ENCRYPTION_3DES);
        } else if (algorithm.equals(BCCryptoHelper.ALGORITHM_DES)) {
            info.setEncryptionType(AS2Message.ENCRYPTION_DES);
        } else if (algorithm.equals(BCCryptoHelper.ALGORITHM_RC2)) {
            info.setEncryptionType(AS2Message.ENCRYPTION_RC2_UNKNOWN);
        } else if (algorithm.equals(BCCryptoHelper.ALGORITHM_AES_128)) {
            info.setEncryptionType(AS2Message.ENCRYPTION_AES_128);
        } else if (algorithm.equals(BCCryptoHelper.ALGORITHM_AES_192)) {
            info.setEncryptionType(AS2Message.ENCRYPTION_AES_192);
        } else if (algorithm.equals(BCCryptoHelper.ALGORITHM_AES_256)) {
            info.setEncryptionType(AS2Message.ENCRYPTION_AES_256);
        } else if (algorithm.equals(BCCryptoHelper.ALGORITHM_RC4)) {
            info.setEncryptionType(AS2Message.ENCRYPTION_RC4_UNKNOWN);
        } else {
            info.setEncryptionType(AS2Message.ENCRYPTION_UNKNOWN_ALGORITHM);
        }
        RecipientInformationStore recipients = enveloped.getRecipientInfos();
        enveloped = null;
        encryptedBody = null;
        RecipientInformation recipient = recipients.get(recipientId);
        if (recipient == null) {
            //give some details about the required and used cert for the decryption
            Collection recipientList = recipients.getRecipients();
            Iterator iterator = recipientList.iterator();
            while (iterator.hasNext()) {
                RecipientInformation recipientInfo = (RecipientInformation) iterator.next();
                if (this.logger != null) {
                    this.logger.log(Level.SEVERE, this.rb.getResourceString("decryption.inforequired",
                            new Object[]{
                                info.getMessageId(),
                                recipientInfo.getRID()
                            }),
                            info);
                }
            }
            if (this.logger != null) {
                this.logger.log(Level.SEVERE, this.rb.getResourceString("decryption.infoassigned",
                        new Object[]{
                            info.getMessageId(),
                            receiverCryptAlias,
                            recipientId
                        }),
                        info);
            }
            throw new AS2Exception(AS2Exception.AUTHENTIFICATION_ERROR,
                    "Error decrypting the message: Recipient certificate does not match.", message);
        }
        //Streamed decryption. Its also possible to use in memory decryption using getContent but that uses
        //far more memory.
        InputStream contentStream = recipient.getContentStream(
                new JceKeyTransEnvelopedRecipient(privateKeyReceiver).setProvider("BC")).getContentStream();
        //InputStream contentStream = recipient.getContentStream(privateKeyReceiver, "BC").getContentStream();
        //threshold set to 20 MB: if the data is less then 20MB perform the operaion in memory else stream to disk
        DeferredFileOutputStream decryptedOutput = new DeferredFileOutputStream(20 * 1024 * 1024, "as2decryptdata_", ".mem", null);
        this.copyStreams(contentStream, decryptedOutput);
        decryptedOutput.flush();
        decryptedOutput.close();
        contentStream.close();
        byte[] decryptedData = null;
        //size of the data was < than the threshold
        if (decryptedOutput.isInMemory()) {
            decryptedData = decryptedOutput.getData();
        } else {
            //data has been written to a temp file: reread and return
            ByteArrayOutputStream memOut = new ByteArrayOutputStream();
            decryptedOutput.writeTo(memOut);
            memOut.flush();
            memOut.close();
            //finally delete the temp file
            boolean deleted = decryptedOutput.getFile().delete();
            decryptedData = memOut.toByteArray();
        }
        if (this.logger != null) {
            this.logger.log(Level.INFO, this.rb.getResourceString("decryption.done.alias",
                    new Object[]{
                        info.getMessageId(),
                        receiverCryptAlias,
                        this.rbMessage.getResourceString("encryption." + info.getEncryptionType())
                    }),
                    info);
        }
        return (decryptedData);
    }

    /**Decrypts the passed data and returns it. Will return the original data
     *if it is not marked as encrypted
     */
    private byte[] decryptMessage(AS2Message message, byte[] data, String contentType, Partner sender, Partner receiver) throws AS2Exception {
        if (this.certificateManagerEncryption == null) {
            throw new AS2Exception(AS2Exception.PROCESSING_ERROR,
                    "AS2MessageParser.decryptMessage: pass a certification manager for the encryption before calling decryptMessage()", message);
        }
        try {
            AS2MessageInfo info = (AS2MessageInfo) message.getAS2Info();
            //first check if the message is decrypted.
            if (!this.contentTypeIndicatesEncryption(contentType) || this.contentTypeIndicatesCompression(contentType)) {

                if (this.logger != null) {
                    this.logger.log(Level.INFO, this.rb.getResourceString("msg.notencrypted", info.getMessageId()), info);
                }
                info.setEncryptionType(AS2Message.ENCRYPTION_NONE);
                //encryption expected?
                if (sender.getEncryptionType() != AS2Message.ENCRYPTION_NONE) {
                    throw new AS2Exception(AS2Exception.INSUFFICIENT_SECURITY_ERROR,
                            "Incoming messages from AS2 partner " + sender.getAS2Identification() + " are defined to be encrypted.",
                            message);
                }
                return (data);
            }
            if (this.logger != null) {
                this.logger.log(Level.INFO, this.rb.getResourceString("msg.encrypted", info.getMessageId()), info);
            }
            try {
                String cryptAlias = this.certificateManagerEncryption.getAliasByFingerprint(receiver.getCryptFingerprintSHA1());
                X509Certificate certificate = this.certificateManagerEncryption.getX509Certificate(cryptAlias);
                //receiver priv key
                PrivateKey privateKey = this.certificateManagerEncryption.getPrivateKey(cryptAlias);
                return (this.decryptData(message, data, contentType, privateKey, certificate, cryptAlias));
            } catch (Exception e) {
                //fallback: try second certificate if it exists
                String cryptAlias = this.certificateManagerEncryption.getAliasByFingerprint(receiver.getCryptFingerprintSHA1(2));
                if (cryptAlias != null) {
                    X509Certificate certificate = this.certificateManagerEncryption.getX509Certificate(cryptAlias);
                    //receiver priv key
                    PrivateKey privateKey = this.certificateManagerEncryption.getPrivateKey(cryptAlias);
                    return (this.decryptData(message, data, contentType, privateKey, certificate, cryptAlias));
                } else {
                    throw e;
                }
            }
        } catch (AS2Exception e) {
            throw e;
        } catch (Throwable e) {
            e.printStackTrace();
            throw new AS2Exception(AS2Exception.DECRYPTION_ERROR, e.getMessage(), message);
        }
    }

    /**Looks if the data is compressed and decompresses it if necessary
     */
    public Part decompressData(Part part, AS2Message message) throws Exception {
        Part compressedPart = this.getCompressedEmbeddedPart(part);
        if (compressedPart == null) {
            return (part);
        }
        SMIMECompressed compressed = null;
        if (compressedPart instanceof MimeBodyPart) {
            compressed = new SMIMECompressed((MimeBodyPart) compressedPart);
        } else {
            compressed = new SMIMECompressed((MimeMessage) compressedPart);
        }
        byte[] decompressedData = compressed.getContent();
        ((AS2MessageInfo) message.getAS2Info()).setCompressionType(AS2Message.COMPRESSION_ZLIB);
        if (this.logger != null) {
            this.logger.log(Level.INFO, this.rb.getResourceString("data.compressed.expanded",
                    new Object[]{
                        message.getAS2Info().getMessageId(), AS2Tools.getDataSizeDisplay(part.getSize()),
                        AS2Tools.getDataSizeDisplay(decompressedData.length)
                    }), message.getAS2Info());
        }
        ByteArrayInputStream memIn = new ByteArrayInputStream(decompressedData);
        MimeBodyPart uncompressedPayload = new MimeBodyPart(memIn);
        memIn.close();
        return (uncompressedPayload);
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
}
