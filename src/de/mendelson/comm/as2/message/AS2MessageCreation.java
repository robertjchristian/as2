//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/message/AS2MessageCreation.java,v 1.1 2012/04/18 14:10:30 heller Exp $
package de.mendelson.comm.as2.message;

import com.sun.mail.util.LineOutputStream;
import de.mendelson.util.security.cert.CertificateManager;
import de.mendelson.comm.as2.partner.Partner;
import de.mendelson.util.AS2Tools;
import de.mendelson.util.MecResourceBundle;
import de.mendelson.util.security.BCCryptoHelper;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.activation.DataHandler;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import javax.mail.util.ByteArrayDataSource;
import org.apache.commons.io.output.DeferredFileOutputStream;
import org.bouncycastle.cms.CMSEnvelopedDataGenerator;
import org.bouncycastle.cms.CMSEnvelopedDataStreamGenerator;
import org.bouncycastle.mail.smime.SMIMECompressedGenerator;
import org.bouncycastle.mail.smime.SMIMEException;

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
public class AS2MessageCreation {

    private Logger logger = null;
    private MecResourceBundle rb = null;
    private MecResourceBundle rbMessage = null;
    private CertificateManager signatureCertManager = null;
    private CertificateManager encryptionCertManager = null;
    //Database connection
    private Connection runtimeConnection;
    private Connection configConnection;

    public AS2MessageCreation(CertificateManager signatureCertManager, CertificateManager encryptionCertManager) {
        //Load resourcebundle
        try {
            this.rb = (MecResourceBundle) ResourceBundle.getBundle(
                    ResourceBundleAS2MessagePacker.class.getName());
            this.rbMessage = (MecResourceBundle) ResourceBundle.getBundle(
                    ResourceBundleAS2Message.class.getName());
        } //load up  resourcebundle
        catch (MissingResourceException e) {
            throw new RuntimeException("Oops..resource bundle " + e.getClassName() + " not found.");
        }
        this.signatureCertManager = signatureCertManager;
        this.encryptionCertManager = encryptionCertManager;
    }

    /**Passes a logger to this creation class*/
    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    /**Passes a database connection to this class to allow logging functionality*/
    public void setServerResources(Connection configConnection, Connection runtimeConnection) {
        this.runtimeConnection = runtimeConnection;
        this.configConnection = configConnection;
    }

    /**Escapes the AS2-TO and AS2-FROM headers in sending direction, related to
     * RFC 4130 section 6.2
     * @param identification as2-from or as2-to value to escape
     * @return escaped value
     */
    public static String escapeFromToHeader(String identification) {
        boolean containsBlank = false;
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < identification.length(); i++) {
            char singleChar = identification.charAt(i);
            if (singleChar == ' ') {
                containsBlank = true;
            } else if (singleChar == '"') {
                builder.append("\\");
            } else if (singleChar == '\\') {
                builder.append("\\");
            }
            builder.append(singleChar);
        }
        //quote the value if it contains blanks
        if (containsBlank) {
            builder.insert(0, "\"");
            builder.append("\"");
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

    /**Prepares the message if it contains no MIME structure*/
    public AS2Message createMessageNoMIME(AS2Message message, Partner receiver) throws Exception {
        AS2MessageInfo info = (AS2MessageInfo) message.getAS2Info();
        BCCryptoHelper cryptoHelper = new BCCryptoHelper();
        //payload content type.
        message.setContentType(receiver.getContentType());
        message.setRawData(message.getPayload(0).getData());
        message.setDecryptedRawData(message.getPayload(0).getData());
        if (this.logger != null) {
            this.logger.log(Level.INFO, this.rb.getResourceString("message.notsigned",
                    new Object[]{
                        info.getMessageId()
                    }), info);
        }
        //compute content mic. Use sha1 as hash alg.
        String digestOID = cryptoHelper.convertAlgorithmNameToOID(BCCryptoHelper.ALGORITHM_SHA1);
        String mic = cryptoHelper.calculateMIC(message.getPayload(0).getData(), digestOID);
        info.setReceivedContentMIC(mic + ", sha1");
        //add compression
        if (receiver.getCompressionType() == AS2Message.COMPRESSION_ZLIB) {
            info.setCompressionType(AS2Message.COMPRESSION_ZLIB);
            int uncompressedSize = message.getDecryptedRawData().length;
            MimeBodyPart bodyPart = this.compressPayload(receiver, message.getDecryptedRawData(), receiver.getContentType());
            int compressedSize = bodyPart.getSize();
            //sometimes size() is unable to determine the size of the compressed body part and will return -1. Dont log the
            //compression ratio in this case.
            if (compressedSize == -1) {
                if (this.logger != null) {
                    this.logger.log(Level.INFO, this.rb.getResourceString("message.compressed.unknownratio",
                            new Object[]{
                                info.getMessageId()
                            }), info);
                }
            } else {
                if (this.logger != null) {
                    this.logger.log(Level.INFO, this.rb.getResourceString("message.compressed",
                            new Object[]{
                                info.getMessageId(), AS2Tools.getDataSizeDisplay(uncompressedSize),
                                AS2Tools.getDataSizeDisplay(compressedSize)
                            }), info);
                }
            }
            //write compressed data into the message array
            ByteArrayOutputStream bodyOutStream = new ByteArrayOutputStream();
            bodyPart.writeTo(bodyOutStream);
            bodyOutStream.flush();
            bodyOutStream.close();
            message.setDecryptedRawData(bodyOutStream.toByteArray());
        }
        //no encryption
        if (info.getEncryptionType() == AS2Message.ENCRYPTION_NONE) {
            if (this.logger != null) {
                this.logger.log(Level.INFO, this.rb.getResourceString("message.notencrypted",
                        new Object[]{
                            info.getMessageId()
                        }), info);
            }
            message.setRawData(message.getDecryptedRawData());
        } else {
            //encrypt the message raw data
            String cryptAlias = this.encryptionCertManager.getAliasByFingerprint(receiver.getCryptFingerprintSHA1());
            this.encryptDataToMessage(message, cryptAlias, info.getEncryptionType(), receiver);
        }
        return (message);
    }

    /**Enwrapps the data into a signed MIME message structure and returns it*/
    private void enwrappInMessageAndSign(AS2Message message, Part contentPart, Partner sender, Partner receiver) throws Exception {
        AS2MessageInfo info = (AS2MessageInfo) message.getAS2Info();
        MimeMessage messagePart = new MimeMessage(Session.getInstance(System.getProperties(), null));
        //sign message if this is requested
        if (info.getSignType() != AS2Message.SIGNATURE_NONE) {
            MimeMultipart signedPart = this.signContentPart(contentPart, sender, receiver);
            if (this.logger != null) {
                String signAlias = this.signatureCertManager.getAliasByFingerprint(sender.getSignFingerprintSHA1());
                this.logger.log(Level.INFO, this.rb.getResourceString("message.signed",
                        new Object[]{
                            info.getMessageId(), signAlias,
                            this.rbMessage.getResourceString("signature." + receiver.getSignType())
                        }), info);
            }
            messagePart.setContent(signedPart);
            messagePart.saveChanges();
        } else {
            //unsigned message
            if (contentPart instanceof MimeBodyPart) {
                MimeMultipart unsignedPart = new MimeMultipart();
                unsignedPart.addBodyPart((MimeBodyPart) contentPart);
                if (this.logger != null) {
                    this.logger.log(Level.INFO, this.rb.getResourceString("message.notsigned",
                            new Object[]{
                                info.getMessageId()
                            }), info);
                }
                messagePart.setContent(unsignedPart);
            } else if (contentPart instanceof MimeMultipart) {
                messagePart.setContent((MimeMultipart) contentPart);
            } else if (contentPart instanceof MimeMessage) {
                messagePart = (MimeMessage) contentPart;
            } else {
                throw new IllegalArgumentException("enwrappInMessageAndSign: Unable to set the content of a "
                        + contentPart.getClass().getName());
            }
            messagePart.saveChanges();
        }
        //store signed or unsigned data
        ByteArrayOutputStream signedOut = new ByteArrayOutputStream();
        //normally the content type header is folded (which is correct but some products are not able to parse this properly)
        //Now take the content-type, unfold it and write it
        Enumeration headerLines = messagePart.getMatchingHeaderLines(new String[]{"Content-Type"});
        LineOutputStream los = new LineOutputStream(signedOut);
        while (headerLines.hasMoreElements()) {
            //requires java mail API >= 1.4
            String nextHeaderLine = MimeUtility.unfold((String) headerLines.nextElement());
            //write the line only if the as2 message is encrypted. If the as2 message is unencrypted this header is added later
            //in the class MessageHttpUploader
            if (info.getEncryptionType() != AS2Message.ENCRYPTION_NONE) {
                los.writeln(nextHeaderLine);
            }
            //store the content line in the as2 message object, this value is required later in MessageHttpUploader
            message.setContentType(nextHeaderLine.substring(nextHeaderLine.indexOf(':') + 1));
        }
        messagePart.writeTo(signedOut,
                new String[]{"Message-ID", "Mime-Version", "Content-Type"});
        signedOut.flush();
        signedOut.close();
        message.setDecryptedRawData(signedOut.toByteArray());
    }

    /**Builds up a new message from the passed message parts
     */
    public AS2Message createMessage(Partner sender, Partner receiver, File[] payloadFiles) throws Exception {
        return (this.createMessage(sender, receiver, payloadFiles, AS2Message.MESSAGETYPE_AS2));
    }

    /**Builds up a new message from the passed message parts
     * @param messageType one of the message types definfed in the class AS2Message
     */
    public AS2Message createMessage(Partner sender, Partner receiver, File[] payloadFiles, int messageType) throws Exception {
        //create payloads from the payload files
        AS2Payload[] payloads = new AS2Payload[payloadFiles.length];
        for (int i = 0; i < payloadFiles.length; i++) {
            File payloadFile = payloadFiles[i];
            InputStream inStream = new FileInputStream(payloadFile);
            ByteArrayOutputStream payloadOut = new ByteArrayOutputStream();
            this.copyStreams(inStream, payloadOut);
            inStream.close();
            payloadOut.flush();
            payloadOut.close();
            //add payload
            AS2Payload payload = new AS2Payload();
            payload.setData(payloadOut.toByteArray());
            payload.setOriginalFilename(payloadFile.getName().replace(' ', '_'));
            payloads[i] = payload;
        }
        return (this.createMessage(sender, receiver, payloads, messageType));
    }

    /**Builds up a new message from the passed message parts
     * @param messageType one of the message types definfed in the class AS2Message
     */
    public AS2Message createMessage(Partner sender, Partner receiver, AS2Payload[] payloads, int messageType) throws Exception {        
        return( this.createMessage(sender, receiver, payloads, messageType, null ));
    }
    
    
    /**Builds up a new message from the passed message parts
     * @param messageType one of the message types definfed in the class AS2Message
     */
    public AS2Message createMessage(Partner sender, Partner receiver, AS2Payload[] payloads, int messageType,
            String messageId ) throws Exception {
        if( messageId == null ){
            messageId = UniqueId.createMessageId(sender.getAS2Identification(), receiver.getAS2Identification());
        }
        BCCryptoHelper cryptoHelper = new BCCryptoHelper();
        AS2MessageInfo info = new AS2MessageInfo();
        info.setMessageType(messageType);
        info.setSenderId(sender.getAS2Identification());
        info.setReceiverId(receiver.getAS2Identification());
        info.setSenderEMail(sender.getEmail());
        info.setMessageId(messageId);
        info.setDirection(AS2MessageInfo.DIRECTION_OUT);
        info.setSignType(receiver.getSignType());
        info.setEncryptionType(receiver.getEncryptionType());
        info.setRequestsSyncMDN(receiver.isSyncMDN());
        if (!receiver.isSyncMDN()) {
            info.setAsyncMDNURL(sender.getMdnURL());
        }
        info.setSubject(receiver.getSubject());
        try {
            info.setSenderHost(InetAddress.getLocalHost().getCanonicalHostName());
        } catch (UnknownHostException e) {
            //nop
        }
        //create message object to return
        AS2Message message = new AS2Message(info);
        //stores all the available body parts that have been prepared
        List<MimeBodyPart> contentPartList = new ArrayList<MimeBodyPart>();
        for (AS2Payload as2Payload : payloads) {
            //add payload
            message.addPayload(as2Payload);
            if (this.runtimeConnection != null) {
                MessageAccessDB messageAccess = new MessageAccessDB(this.configConnection, this.runtimeConnection);
                messageAccess.initializeOrUpdateMessage(info);
            }
            //no MIME message: single payload, unsigned, no CEM
            if (info.getSignType() == AS2Message.SIGNATURE_NONE && payloads.length == 1
                    && info.getMessageType() != AS2Message.MESSAGETYPE_CEM) {
                return (this.createMessageNoMIME(message, receiver));
            }
            //MIME message
            MimeBodyPart bodyPart = new MimeBodyPart();
            String contentType = null;
            if (as2Payload.getContentType() == null) {
                contentType = receiver.getContentType();
            } else {
                contentType = as2Payload.getContentType();
            }
            bodyPart.setDataHandler(new DataHandler(new ByteArrayDataSource(as2Payload.getData(), contentType)));
            bodyPart.addHeader("Content-Type", contentType);
            if (as2Payload.getContentId() != null) {
                bodyPart.addHeader("Content-ID", as2Payload.getContentId());
            }
            if (receiver.getContentTransferEncoding() == AS2Message.CONTENT_TRANSFER_ENCODING_BASE64) {
                bodyPart.addHeader("Content-Transfer-Encoding", "base64");
            } else {
                bodyPart.addHeader("Content-Transfer-Encoding", "binary");
            }
            //prepare filename to not violate the MIME header rules
            if (as2Payload.getOriginalFilename() == null) {
                as2Payload.setOriginalFilename(new File(as2Payload.getPayloadFilename()).getName());
            }
            String newFilename = as2Payload.getOriginalFilename().replace(' ', '_');
            newFilename = newFilename.replace('@', '_');
            newFilename = newFilename.replace(':', '_');
            newFilename = newFilename.replace(';', '_');
            newFilename = newFilename.replace('(', '_');
            newFilename = newFilename.replace(')', '_');
            bodyPart.addHeader("Content-Disposition", "attachment; filename=" + newFilename);
            contentPartList.add(bodyPart);
        }
        Part contentPart = null;
        //sigle attachment? No CEM? Every CEM is in a multipart/related container
        if (contentPartList.size() == 1 && info.getMessageType() != AS2Message.MESSAGETYPE_CEM) {
            contentPart = contentPartList.get(0);
        } else {
            //build up a new MimeMultipart container for the multiple attachments, content-type
            //is "multipart/related"
            MimeMultipart multipart = null;
            //CEM messages are always in a multipart container (even the response which contains only a single
            //payload) with the subtype "application/ediint-cert-exchange+xml".
            if (info.getMessageType() == AS2Message.MESSAGETYPE_CEM) {
                multipart = new MimeMultipart("related; type=\"application/ediint-cert-exchange+xml\"");
            } else {
                multipart = new MimeMultipart("related");
            }
            for (MimeBodyPart bodyPart : contentPartList) {
                multipart.addBodyPart(bodyPart);
            }
            contentPart = new MimeMessage(Session.getInstance(System.getProperties(), null));
            contentPart.setContent(multipart, multipart.getContentType());
            ((MimeMessage) contentPart).saveChanges();
        }
        //should the content be compressed and enwrapped or just enwrapped?
        if (receiver.getCompressionType() == AS2Message.COMPRESSION_ZLIB) {
            info.setCompressionType(AS2Message.COMPRESSION_ZLIB);
            int uncompressedSize = contentPart.getSize();
            contentPart = this.compressPayload(receiver, contentPart);
            int compressedSize = contentPart.getSize();
            //sometimes size() is unable to determine the size of the compressed body part and will return -1. Dont log the
            //compression ratio in this case.
            if (uncompressedSize == -1 || compressedSize == -1) {
                if (this.logger != null) {
                    this.logger.log(Level.INFO, this.rb.getResourceString("message.compressed.unknownratio",
                            new Object[]{
                                info.getMessageId()
                            }), info);
                }
            } else {
                if (this.logger != null) {
                    this.logger.log(Level.INFO, this.rb.getResourceString("message.compressed",
                            new Object[]{
                                info.getMessageId(), AS2Tools.getDataSizeDisplay(uncompressedSize),
                                AS2Tools.getDataSizeDisplay(compressedSize)
                            }), info);
                }
            }
        }
        //compute content mic. Try to use sign digest as hash alg. For unsigned messages take sha-1
        String digestOID = null;
        if (info.getSignType() == AS2Message.SIGNATURE_MD5) {
            digestOID = cryptoHelper.convertAlgorithmNameToOID(BCCryptoHelper.ALGORITHM_MD5);
        } else {
            digestOID = cryptoHelper.convertAlgorithmNameToOID(BCCryptoHelper.ALGORITHM_SHA1);
        }
        String mic = cryptoHelper.calculateMIC(contentPart, digestOID);
        if (info.getSignType() == AS2Message.SIGNATURE_MD5) {
            info.setReceivedContentMIC(mic + ", md5");
        } else {
            info.setReceivedContentMIC(mic + ", sha1");
        }
        this.enwrappInMessageAndSign(message, contentPart, sender, receiver);
        //encryption requested for the receiver?
        if (info.getEncryptionType() != AS2Message.ENCRYPTION_NONE) {
            String cryptAlias = this.encryptionCertManager.getAliasByFingerprint(receiver.getCryptFingerprintSHA1());
            this.encryptDataToMessage(message, cryptAlias, info.getEncryptionType(), receiver);
        } else {
            message.setRawData(message.getDecryptedRawData());
            if (this.logger != null) {
                this.logger.log(Level.INFO, this.rb.getResourceString("message.notencrypted",
                        new Object[]{
                            info.getMessageId()
                        }), info);
            }
        }
        return (message);
    }

    /**Encrypts a byte array and returns it*/
    private void encryptDataToMessage(AS2Message message, String receiverCryptAlias, int encryptionType, Partner receiver) throws Exception {
        AS2MessageInfo info = (AS2MessageInfo) message.getAS2Info();
        BCCryptoHelper cryptoHelper = new BCCryptoHelper();
        X509Certificate certificate = this.encryptionCertManager.getX509Certificate(receiverCryptAlias);
        CMSEnvelopedDataStreamGenerator dataGenerator = new CMSEnvelopedDataStreamGenerator();
        dataGenerator.addKeyTransRecipient(certificate);
        DeferredFileOutputStream encryptedOutput = new DeferredFileOutputStream(1024 * 1024, "as2encryptdata_", ".mem", null);
        OutputStream out = null;
        if (encryptionType == AS2Message.ENCRYPTION_3DES) {
            out = dataGenerator.open(encryptedOutput, CMSEnvelopedDataGenerator.DES_EDE3_CBC, "BC");
        } else if (encryptionType == AS2Message.ENCRYPTION_DES) {
            out = dataGenerator.open(encryptedOutput, cryptoHelper.convertAlgorithmNameToOID(BCCryptoHelper.ALGORITHM_DES), 56, "BC");
        } else if (encryptionType == AS2Message.ENCRYPTION_RC2_40) {
            out = dataGenerator.open(encryptedOutput, CMSEnvelopedDataGenerator.RC2_CBC, 40, "BC");
        } else if (encryptionType == AS2Message.ENCRYPTION_RC2_64) {
            out = dataGenerator.open(encryptedOutput, CMSEnvelopedDataGenerator.RC2_CBC, 64, "BC");
        } else if (encryptionType == AS2Message.ENCRYPTION_RC2_128) {
            out = dataGenerator.open(encryptedOutput, CMSEnvelopedDataGenerator.RC2_CBC, 128, "BC");
        } else if (encryptionType == AS2Message.ENCRYPTION_RC2_196) {
            out = dataGenerator.open(encryptedOutput, CMSEnvelopedDataGenerator.RC2_CBC, 196, "BC");
        } else if (encryptionType == AS2Message.ENCRYPTION_AES_128) {
            out = dataGenerator.open(encryptedOutput, CMSEnvelopedDataGenerator.AES128_CBC, "BC");
        } else if (encryptionType == AS2Message.ENCRYPTION_AES_192) {
            out = dataGenerator.open(encryptedOutput, CMSEnvelopedDataGenerator.AES192_CBC, "BC");
        } else if (encryptionType == AS2Message.ENCRYPTION_AES_256) {
            out = dataGenerator.open(encryptedOutput, CMSEnvelopedDataGenerator.AES256_CBC, "BC");
        } else if (encryptionType == AS2Message.ENCRYPTION_RC4_40) {
            out = dataGenerator.open(encryptedOutput, cryptoHelper.convertAlgorithmNameToOID(BCCryptoHelper.ALGORITHM_RC4), 40, "BC");
        } else if (encryptionType == AS2Message.ENCRYPTION_RC4_56) {
            out = dataGenerator.open(encryptedOutput, cryptoHelper.convertAlgorithmNameToOID(BCCryptoHelper.ALGORITHM_RC4), 56, "BC");
        } else if (encryptionType == AS2Message.ENCRYPTION_RC4_128) {
            out = dataGenerator.open(encryptedOutput, cryptoHelper.convertAlgorithmNameToOID(BCCryptoHelper.ALGORITHM_RC4), 128, "BC");
        }
        if (out == null) {
            throw new Exception("Internal failure: unsupported encryption type " + encryptionType);
        }
        out.write(message.getDecryptedRawData());
        out.close();
        encryptedOutput.close();
        //size of the data was < than the threshold
        if (encryptedOutput.isInMemory()) {
            message.setRawData(encryptedOutput.getData());
        } else {
            //data has been written to a temp file: reread and return
            ByteArrayOutputStream memOut = new ByteArrayOutputStream();
            encryptedOutput.writeTo(memOut);
            memOut.flush();
            memOut.close();
            //finally delete the temp file
            boolean deleted = encryptedOutput.getFile().delete();
            message.setRawData(memOut.toByteArray());
        }
        if (this.logger != null) {
            String cryptAlias = this.encryptionCertManager.getAliasByFingerprint(receiver.getCryptFingerprintSHA1());
            this.logger.log(Level.INFO, this.rb.getResourceString("message.encrypted",
                    new Object[]{
                        info.getMessageId(), cryptAlias,
                        this.rbMessage.getResourceString("encryption." + receiver.getEncryptionType())
                    }), info);
        }
    }

    /**Compresses the payload using the ZLIB algorithm
     */
    private MimeBodyPart compressPayload(Partner receiver, byte[] data, String contentType) throws SMIMEException, MessagingException {
        MimeBodyPart bodyPart = new MimeBodyPart();
        bodyPart.setDataHandler(new DataHandler(new ByteArrayDataSource(data, contentType)));
        bodyPart.addHeader("Content-Type", contentType);
        if (receiver.getContentTransferEncoding() == AS2Message.CONTENT_TRANSFER_ENCODING_BASE64) {
            bodyPart.addHeader("Content-Transfer-Encoding", "base64");
        } else {
            bodyPart.addHeader("Content-Transfer-Encoding", "binary");
        }
        SMIMECompressedGenerator generator = new SMIMECompressedGenerator();
        if (receiver.getContentTransferEncoding() == AS2Message.CONTENT_TRANSFER_ENCODING_BASE64) {
            generator.setContentTransferEncoding("base64");
        } else {
            generator.setContentTransferEncoding("binary");
        }
        return (generator.generate(bodyPart, SMIMECompressedGenerator.ZLIB));
    }

    /**Compresses the payload using the ZLIB algorithm
     */
    private MimeBodyPart compressPayload(Partner receiver, Part contentPart) throws SMIMEException {
        SMIMECompressedGenerator generator = new SMIMECompressedGenerator();
        if (receiver.getContentTransferEncoding() == AS2Message.CONTENT_TRANSFER_ENCODING_BASE64) {
            generator.setContentTransferEncoding("base64");
        } else {
            generator.setContentTransferEncoding("binary");
        }
        if (contentPart instanceof MimeBodyPart) {
            return (generator.generate((MimeBodyPart) contentPart, SMIMECompressedGenerator.ZLIB));
        } else if (contentPart instanceof MimeMessage) {
            return (generator.generate((MimeMessage) contentPart, SMIMECompressedGenerator.ZLIB));
        } else {
            throw new IllegalArgumentException("compressPayload: Unable to compress a Part of class " + contentPart.getClass().getName());
        }
    }

    /**Signs the passed data and returns it
     */
    private MimeMultipart signContentPart(Part part, Partner sender, Partner receiver) throws Exception {
        MimeMultipart signedPart = null;
        if (part instanceof MimeBodyPart) {
            signedPart = this.signContent((MimeBodyPart) part, sender, receiver);
        } else if (part instanceof MimeMessage) {
            signedPart = this.signContent((MimeMessage) part, sender, receiver);
        } else {
            throw new IllegalArgumentException("signContentPart: unable to sign a " + part.getClass().getName() + ".");
        }
        return (signedPart);
    }

    /**Signs the passed data and returns it
     */
    private MimeMultipart signContent(MimeMessage message, Partner sender, Partner receiver) throws Exception {
        PrivateKey senderKey = this.signatureCertManager.getPrivateKeyByFingerprintSHA1(sender.getSignFingerprintSHA1());
        if (senderKey == null) {
            throw new Exception("AS2MessageCreation.signContent: Key with serial " + sender.getSignFingerprintSHA1()
                    + " does not exist in the keystore.");
        }
        String senderSignAlias = this.signatureCertManager.getAliasByFingerprint(sender.getSignFingerprintSHA1());
        Certificate[] chain = this.signatureCertManager.getCertificateChain(senderSignAlias);
        String digest = null;
        if (receiver.getSignType() == AS2Message.SIGNATURE_SHA1) {
            digest = "sha1";
        } else if (receiver.getSignType() == AS2Message.SIGNATURE_MD5) {
            digest = "md5";
        } else {
            throw new Exception("Internal failure: Unsupported sign type " + receiver.getSignType());
        }
        BCCryptoHelper helper = new BCCryptoHelper();
        return (helper.sign(message, chain, senderKey, digest));
    }

    /**Signs the passed message and returns it
     */
    private MimeMultipart signContent(MimeBodyPart body, Partner sender, Partner receiver) throws Exception {
        PrivateKey senderKey = this.signatureCertManager.getPrivateKeyByFingerprintSHA1(sender.getSignFingerprintSHA1());
        String senderSignAlias = this.signatureCertManager.getAliasByFingerprint(sender.getSignFingerprintSHA1());
        Certificate[] chain = this.signatureCertManager.getCertificateChain(senderSignAlias);
        String digest = null;
        if (receiver.getSignType() == AS2Message.SIGNATURE_SHA1) {
            digest = "sha1";
        } else if (receiver.getSignType() == AS2Message.SIGNATURE_MD5) {
            digest = "md5";
        } else {
            throw new Exception("Internal failure: Unsupported sign type " + receiver.getSignType());
        }
        BCCryptoHelper helper = new BCCryptoHelper();
        return (helper.sign(body, chain, senderKey, digest));
    }

    /**Copies all data from one stream to another*/
    private void copyStreams(InputStream in, OutputStream out) throws IOException {
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
}
