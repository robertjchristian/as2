//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/message/AS2MDNCreation.java,v 1.1 2012/04/18 14:10:30 heller Exp $
package de.mendelson.comm.as2.message;

import com.sun.mail.util.LineOutputStream;
import de.mendelson.comm.as2.AS2Exception;
import de.mendelson.comm.as2.AS2ServerVersion;
import de.mendelson.util.security.cert.CertificateManager;
import de.mendelson.comm.as2.partner.Partner;
import de.mendelson.util.MecResourceBundle;
import de.mendelson.util.security.BCCryptoHelper;
import java.io.ByteArrayOutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.activation.DataHandler;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import javax.mail.util.ByteArrayDataSource;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Packs a message with all necessary headers and attachments
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class AS2MDNCreation {

    private Logger logger = null;
    private MecResourceBundle rb = null;
    private CertificateManager certificateManager = null;

    public AS2MDNCreation(CertificateManager certificateManager) {
        //Load resourcebundle
        try {
            this.rb = (MecResourceBundle) ResourceBundle.getBundle(
                    ResourceBundleAS2MessagePacker.class.getName());
        } //load up resourcebundle
        catch (MissingResourceException e) {
            throw new RuntimeException("Oops..resource bundle " + e.getClassName() + " not found.");
        }
        this.certificateManager = certificateManager;
    }

    /**Build the header for the sync response and returns them
     */
    public Properties buildHeaderForSyncMDN(AS2Message message) {
        String ediintFeatures = "multiple-attachments, CEM";
        AS2MDNInfo info = (AS2MDNInfo) message.getAS2Info();
        Properties header = new Properties();
        header.setProperty("server", AS2ServerVersion.getUserAgent());
        header.setProperty("as2-version", "1.2");
        header.setProperty("ediint-features", ediintFeatures);
        header.setProperty("mime-version", "1.0");
        header.setProperty("message-id", "<" + info.getMessageId() + ">");
        DateFormat headerFormat = new SimpleDateFormat("EE, dd MMM yyyy HH:mm:ss zz");
        header.setProperty("date", headerFormat.format(new Date()));
        header.setProperty("connection", "close");
        if (info.getReceiverId() != null) {
            header.setProperty("as2-to", AS2Message.escapeFromToHeader(info.getReceiverId()));
        }
        if (info.getSenderId() != null) {
            header.setProperty("as2-from", AS2Message.escapeFromToHeader(info.getSenderId()));
        }
        header.setProperty("content-type", message.getContentType());
        header.setProperty("content-length", String.valueOf(message.getRawDataSize()));
        return (header);
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

    /**Creates an mdn that could be returned to the sender and indicates that
     *everything is ok
     */
    public AS2Message createMDNProcessed(AS2MessageInfo releatedMessageInfo, Partner mdnSender, String mdnReceiverAS2Id) throws Exception {
        return (this.createMDNProcessed(releatedMessageInfo, mdnSender, mdnReceiverAS2Id, MDNText.get(MDNText.RECEIVED, releatedMessageInfo.getMessageType())));
    }

    /**Creates an mdn that could be returned to the sender and indicates that
     *everything is ok
     */
    public AS2Message createMDNProcessed(AS2MessageInfo releatedMessageInfo, Partner mdnSender, String mdnReceiverAS2Id, String detailText) throws Exception {
        AS2Message mdn = this.createMDN(releatedMessageInfo, mdnSender, mdnSender.getAS2Identification(), mdnReceiverAS2Id, "processed", detailText);
        mdn.getAS2Info().setState(AS2Message.STATE_FINISHED);
        return (mdn);
    }

    /**Creates an mdn that could be returned to the sender and indicates an error
     *by processing the message
     */
    public AS2Message createMDNError(AS2Exception exception, String as2MessageSenderId, Partner as2MessageReceiver, String as2MessageReceiverId) throws Exception {
        AS2MessageInfo info = (AS2MessageInfo) exception.getAS2Message().getAS2Info();
        AS2Message mdn = this.createMDN(info, as2MessageReceiver, as2MessageReceiverId,
                as2MessageSenderId, "processed/error: " + exception.getErrorType(), MDNText.get(MDNText.ERROR, info.getMessageType()) + exception.getMessage());
        if (this.logger != null) {
            this.logger.log(Level.SEVERE, this.rb.getResourceString("mdn.details",
                    new Object[]{
                        info.getMessageId(),
                        exception.getMessage()
                    }), info);
        }
        mdn.getAS2Info().setState(AS2Message.STATE_STOPPED);
        return (mdn);
    }

    /**Creates a MDN to return. It may be confusing that the sender and the sender id is passed but the sender
     * is null if the partner with the sender id has not been found in the db
     *@param dispositionState State that will be written into the disposition header
     */
    private AS2Message createMDN(AS2MessageInfo relatedMessageInfo, Partner sender,
            String senderAS2Id, String receiverAS2Id, String dispositionState,
            String additionalText) throws Exception {
        AS2Message message = new AS2Message(new AS2MDNInfo());
        AS2MDNInfo info = (AS2MDNInfo) message.getAS2Info();
        info.setMessageId(UniqueId.createMessageId(senderAS2Id, receiverAS2Id));
        info.setSenderId(senderAS2Id);
        info.setReceiverId(receiverAS2Id);
        info.setRelatedMessageId(relatedMessageInfo.getMessageId());
        try {
            info.setSenderHost(InetAddress.getLocalHost().getCanonicalHostName());
        } catch (UnknownHostException e) {
            //nop
        }
        String contentTransferEncoding = "7bit";
        //String contentTransferEncoding = "base64";
        MimeMultipart multiPart = new MimeMultipart();
        multiPart.addBodyPart(this.createMDNNotesBody(additionalText, contentTransferEncoding));
        multiPart.addBodyPart(this.createMDNDispositionBody(relatedMessageInfo, dispositionState, contentTransferEncoding));
        multiPart.setSubType("report; report-type=disposition-notification");
        MimeMessage messagePart = new MimeMessage(Session.getInstance(System.getProperties(), null));
        messagePart.setContent(multiPart, MimeUtility.unfold(multiPart.getContentType()));
        messagePart.saveChanges();
        ByteArrayOutputStream memOutUnsigned = new ByteArrayOutputStream();
        //normally the content type header is folded (which is correct but some products are not able to parse this properly)
        //Now take the content-type, unfold it and write it
        Enumeration hdrLines = messagePart.getMatchingHeaderLines(new String[]{"Content-Type"});
        LineOutputStream los = new LineOutputStream(memOutUnsigned);
        while (hdrLines.hasMoreElements()) {
            //requires java mail API >= 1.4
            String nextHeaderLine = MimeUtility.unfold((String) hdrLines.nextElement());
            los.writeln(nextHeaderLine);
        }
        messagePart.writeTo(memOutUnsigned,
                new String[]{"Message-ID", "Mime-Version", "Content-Type"});
        memOutUnsigned.flush();
        memOutUnsigned.close();
        message.setDecryptedRawData(memOutUnsigned.toByteArray());
        //check if authentification of sender is ok, then sign if possible
        if (sender != null) {
            MimeMessage signedMessage = this.signMDN(messagePart, sender, message, relatedMessageInfo);
            message.setContentType(MimeUtility.unfold(signedMessage.getContentType()));
            ByteArrayOutputStream memOutSigned = new ByteArrayOutputStream();
            signedMessage.writeTo(memOutSigned,
                    new String[]{"Message-ID", "Mime-Version", "Content-Type"});
            memOutSigned.flush();
            memOutSigned.close();
            message.setRawData(memOutSigned.toByteArray());
        } //there occured an authentification error: the system was unable to authentificate the sender,
        //do not sign MDN
        else {
            ByteArrayOutputStream memOut = new ByteArrayOutputStream();
            messagePart.writeTo(memOut,
                    new String[]{"Message-ID", "Mime-Version", "Content-Type"});
            memOut.flush();
            memOut.close();
            message.getAS2Info().setSignType(AS2Message.SIGNATURE_NONE);
            message.setContentType(MimeUtility.unfold(messagePart.getContentType()));
            message.setRawData(memOut.toByteArray());
        }
        if (dispositionState.indexOf("error") >= 0) {
            if (this.logger != null) {
                this.logger.log(Level.SEVERE, this.rb.getResourceString("mdn.created",
                        new Object[]{
                            info.getMessageId(), dispositionState
                        }), info);
            }
        } else {
            if (this.logger != null) {
                this.logger.log(Level.FINE, this.rb.getResourceString("mdn.created",
                        new Object[]{
                            info.getMessageId(), dispositionState
                        }), info);
            }
        }
        return (message);
    }

    /**Its necessary to transmit additional notes
     */
    private MimeBodyPart createMDNNotesBody(String text, String contentTransferEncoding) throws MessagingException {
        MimeBodyPart body = new MimeBodyPart();
        body.setDataHandler(new DataHandler(new ByteArrayDataSource(text.getBytes(), "text/plain")));
        body.setHeader("Content-Type", "text/plain");
        body.setHeader("Content-Transfer-Encoding", contentTransferEncoding);
        return (body);
    }

    /**Creates the MDN body and returns it
     *
     */
    private MimeBodyPart createMDNDispositionBody(AS2MessageInfo relatedMessageInfo, String dispositionState,
            String contentTransferEncoding) throws MessagingException {
        MimeBodyPart body = new MimeBodyPart();
        StringBuilder buffer = new StringBuilder();
        buffer.append("Reporting-UA: ").append(AS2ServerVersion.getProductName()).append("\r\n");
        buffer.append("Original-Recipient: rfc822; ").append(relatedMessageInfo.getReceiverId()).append("\r\n");
        buffer.append("Final-Recipient: rfc822; ").append(relatedMessageInfo.getReceiverId()).append("\r\n");
        buffer.append("Original-Message-ID: <").append(relatedMessageInfo.getMessageId()).append(">\r\n");
        buffer.append("Disposition: automatic-action/MDN-sent-automatically; ").append(dispositionState).append("\r\n");
        if (relatedMessageInfo.getReceivedContentMIC() != null) {
            buffer.append("Received-Content-MIC: ").append(relatedMessageInfo.getReceivedContentMIC()).append("\r\n");
        }
        body.setDataHandler(new DataHandler(new ByteArrayDataSource(buffer.toString().getBytes(),
                "message/disposition-notification")));
        body.setHeader("Content-Transfer-Encoding", contentTransferEncoding);
        return (body);
    }

    /**Signs the passed mdn and returns it
     */
    private MimeMessage signMDN(MimeMessage mimeMessage, Partner sender, AS2Message as2Message, AS2MessageInfo relatedMessageInfo) throws Exception {
        if (relatedMessageInfo.getDispositionNotificationOptions().signMDN()) {
            int[] possibleAlgorithm = relatedMessageInfo.getDispositionNotificationOptions().getSignatureAlgorithm();
            boolean sha1Possible = false;
            boolean md5Possible = false;
            for (int i = 0; i < possibleAlgorithm.length; i++) {
                if (possibleAlgorithm[i] == AS2Message.SIGNATURE_MD5) {
                    md5Possible = true;
                } else if (possibleAlgorithm[i] == AS2Message.SIGNATURE_SHA1) {
                    sha1Possible = true;
                }
            }
            String digest = null;
            if (sha1Possible) {
                digest = "sha1";
                as2Message.getAS2Info().setSignType(AS2Message.SIGNATURE_SHA1);
            } else if (md5Possible) {
                digest = "md5";
                as2Message.getAS2Info().setSignType(AS2Message.SIGNATURE_MD5);
            }
            if (digest == null) {
                as2Message.getAS2Info().setSignType(AS2Message.SIGNATURE_NONE);
                if (this.logger != null) {
                    this.logger.log(Level.INFO, this.rb.getResourceString("mdn.notsigned",
                            new Object[]{
                                as2Message.getAS2Info().getMessageId(),}), as2Message.getAS2Info());
                }
                return (mimeMessage);
            }
            PrivateKey senderKey = this.certificateManager.getPrivateKeyByFingerprintSHA1(sender.getSignFingerprintSHA1());
            String senderSignAlias = this.certificateManager.getAliasByFingerprint(sender.getSignFingerprintSHA1());
            Certificate[] chain = this.certificateManager.getCertificateChain(senderSignAlias);
            BCCryptoHelper helper = new BCCryptoHelper();
            MimeMessage signedMimeMessage = helper.signToMessage(mimeMessage, chain, senderKey, digest.toUpperCase());
            if (this.logger != null) {
                this.logger.log(Level.INFO, this.rb.getResourceString("mdn.signed",
                        new Object[]{
                            as2Message.getAS2Info().getMessageId(), digest.toUpperCase()
                        }), as2Message.getAS2Info());
            }
            return (signedMimeMessage);
        } else {
            as2Message.getAS2Info().setSignType(AS2Message.SIGNATURE_NONE);
            if (this.logger != null) {
                this.logger.log(Level.INFO, this.rb.getResourceString("mdn.notsigned",
                        new Object[]{
                            as2Message.getAS2Info().getMessageId(),}), as2Message.getAS2Info());
            }
            return (mimeMessage);
        }
    }

    /**
     * @param logger the logger to set. If no logger is passed to this class there will be no logging
     */
    public void setLogger(Logger logger) {
        this.logger = logger;
    }
}
