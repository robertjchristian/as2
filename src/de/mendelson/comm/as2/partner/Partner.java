//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/partner/Partner.java,v 1.1 2012/04/18 14:10:32 heller Exp $
package de.mendelson.comm.as2.partner;

import de.mendelson.util.security.cert.CertificateManager;
import de.mendelson.comm.as2.message.AS2Message;
import de.mendelson.comm.as2.message.store.MessageStoreHandler;
import de.mendelson.comm.as2.send.HttpConnectionParameter;
import de.mendelson.util.security.cert.KeystoreCertificate;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */

/**
 * Stores all information about a business partner
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class Partner implements Serializable, Comparable {

    /**Unique id in the database for this partner
     */
    private int dbId = -1;
    private boolean localStation = false;
    /**Found identification in the message*/
    private String as2Identification;
    /**Name of the partner, defined in the PIP server
     */
    private String name;
    private PartnerCertificateInformationList partnerCertificateList = new PartnerCertificateInformationList();
    private int encryptionType = AS2Message.ENCRYPTION_3DES;
    private int signType = AS2Message.SIGNATURE_SHA1;
    private String email = "sender@as2server.com";
    private String url = this.getDefaultURL();
    private String subject = "AS2 message";
    private String contentType = "application/EDI-Consent";
    private String mdnURL = this.getDefaultURL();
    private boolean syncMDN = true;
    private boolean keepFilenameOnReceipt = false;
    private String[] pollIgnoreList = null;
    /**Direcrtory poll interval in seconds*/
    private int pollInterval = 10;
    /**Max number of files to pick up for a single poll process*/
    private int maxPollFiles = 100;
    /**Compression type for this partner, used if you send messages to the partner*/
    private int compressionType = AS2Message.COMPRESSION_NONE;
    /**MDNs to this partner should be signed?*/
    private boolean signedMDN = true;
    /**Command that should be executed on message receipt for this partner
     */
    private String commandOnReceipt = "c:/mendelson/mbi/SendToMBI.exe -file ${filename} -media as2";
    /**Indicates if the command should be executed on receipt of a message
     */
    private boolean useCommandOnReceipt = false;
    /**Stores an async MDN HTTP authentication if requested
     */
    private HTTPAuthentication authenticationAsyncMDN = new HTTPAuthentication();
    /**Stores a send data HTTP authentication if requested
     */
    private HTTPAuthentication authentication = new HTTPAuthentication();
    /**Comment for a partner, this is not relevant for the as2 communication*/
    private String comment = null;
    private int notifySend = 0;
    private int notifyReceive = 0;
    private int notifySendReceive = 0;
    private boolean notifySendEnabled = false;
    private boolean notifyReceiveEnabled = false;
    private boolean notifySendReceiveEnabled = false;
    /**Command that should be executed on message send for this partner / error
     */
    private String commandOnSendError = "c:/mendelson/mbi/SendToLog.exe -tid ${filename} -title \"AS2 sending process failed\" -details \"The AS2 message has NOT been sent:\\n${log}\" -state ERROR";
    /**Indicates if the command should be executed on send of a message / error
     */
    private boolean useCommandOnSendError = false;
    /**Command that should be executed on message send for this partner / success
     */
    private String commandOnSendSuccess = "c:/mendelson/mbi/SendToLog.exe -tid ${filename} -title \"AS2 message sent\" -details \"Success.\\nThe AS2 message has been sent:\\n${log}\" -state SUCCESS";
    /**Indicates if the command should be executed on send of a message / success
     */
    private boolean useCommandOnSendSuccess = false;
    private int contentTransferEncoding = AS2Message.CONTENT_TRANSFER_ENCODING_BINARY;
    /**Partner specific http headers*/
    private List<PartnerHttpHeader> httpHeader = new ArrayList<PartnerHttpHeader>();
    /**http protocol version for this partner
     */
    private String httpProtocolVersion = HttpConnectionParameter.HTTP_1_1;

    public Partner() {
    }

    /**Deletes http headers that contain only of a value but no key*/
    public void deleteEmptyHttpHeader() {
        for (int i = this.httpHeader.size() - 1; i >= 0; i--) {
            PartnerHttpHeader header = this.httpHeader.get(i);
            if (header.getKey() == null || header.getKey().trim().length() == 0) {
                this.httpHeader.remove(i);
            }
        }
    }

    /**Returns a list of file patterns that will be ignored by the dir poll manager*/
    public String[] getPollIgnoreList() {
        return (this.pollIgnoreList);
    }

    /**Pass the poll ignore list to the partner*/
    public void setPollIgnoreList(String[] pollIgnoreList) {
        this.pollIgnoreList = pollIgnoreList;
    }

    /**Expected is a comma separated list of poll ignores*/
    public void setPollIgnoreListString(String pollIgnoreStr) {
        if (pollIgnoreStr == null) {
            this.pollIgnoreList = null;
            return;
        }
        StringTokenizer tokenizer = new StringTokenizer(pollIgnoreStr, ",");
        this.pollIgnoreList = new String[tokenizer.countTokens()];
        for (int i = 0; tokenizer.hasMoreTokens(); i++) {
            this.pollIgnoreList[i] = tokenizer.nextToken();
        }
    }

    /**Returns a String that contains a comma separated list of dir poll manager ignore patterns*/
    public String getPollIgnoreListAsString() {
        if (this.pollIgnoreList == null) {
            return (null);
        }
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < this.pollIgnoreList.length; i++) {
            buffer.append(this.pollIgnoreList[i]);
            if (i + 1 < this.pollIgnoreList.length) {
                buffer.append(",");
            }
        }
        return (buffer.toString());
    }

    /**Returns the default URL where to connect to
     */
    public String getDefaultURL() {
        return ("http://as2.mendelson-e-c.com:8080/as2/HttpReceiver");
    }

    /**Returns the path on the harddisk where the messages are stored in*/
    public String getMessagePath(String messageDir) {
        StringBuilder messagePath = new StringBuilder();
        messagePath.append(new File(messageDir).getAbsolutePath());
        messagePath.append(File.separator);
        messagePath.append(MessageStoreHandler.convertToValidFilename(this.getName()));
        return (messagePath.toString());
    }

    public int getDBId() {
        return dbId;
    }

    public void setDBId(int dbId) {
        this.dbId = dbId;
    }

    public boolean isLocalStation() {
        return localStation;
    }

    public void setLocalStation(boolean localStation) {
        this.localStation = localStation;
    }

    public String getAS2Identification() {
        return as2Identification;
    }

    public void setAS2Identification(String as2Identification) {
        this.as2Identification = as2Identification;
    }

    public String getName() {
        if (this.name == null) {
            return (this.getAS2Identification());
        }
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PartnerCertificateInformation getCertificateInformation(int category, int prio) {
        return (this.partnerCertificateList.getPartnerCertificate(category, prio));
    }

    /**Sets a single cert information to the partner, overwriting any existing with the same status, priority and type
     */
    public void setCertificateInformation(PartnerCertificateInformation information) {
        this.partnerCertificateList.setCertificateInformation(information);
    }

    /**Sets a new sign cert to this partner. This is a convenience method to set a cert info of
     * the category SIGN, overwriting prio 1 and set the new cert to accepted
     */
    public void setSignFingerprintSHA1(String fingerprintSHA1) {
        PartnerCertificateInformation signInfo = new PartnerCertificateInformation(
                fingerprintSHA1,
                PartnerCertificateInformation.CATEGORY_SIGN);
        this.setCertificateInformation(signInfo);
    }

    /**Sets a new encryption cert to this partner. This is a convenience method to set a cert info of
     * the category CRYPT, overwriting prio 1 and set the new cert to accepted
     */
    public void setCryptFingerprintSHA1(String fingerprintSHA1) {
        PartnerCertificateInformation cryptInfo = new PartnerCertificateInformation(
                fingerprintSHA1,
                PartnerCertificateInformation.CATEGORY_CRYPT);
        this.setCertificateInformation(cryptInfo);
    }

    /**Returns the alias used for signing messages. This returns the cert
     * with prio 1, status accepted, category SIGN
     */
    public String getSignFingerprintSHA1() {
        return (this.getSignFingerprintSHA1(1));
    }

    /**Returns the fingerprint (SHA1) used for signing messages. This returns the cert
     * with prio "prio", status accepted, category SIGN
     */
    public String getSignFingerprintSHA1(int prio) {
        PartnerCertificateInformation signInfo = this.getCertificateInformation(PartnerCertificateInformation.CATEGORY_SIGN, prio);
        if (signInfo != null) {
            return (signInfo.getFingerprintSHA1());
        } else {
            return (null);
        }
    }

    /**Returns the cert used for signing messages. This returns the cert
     * with prio 1, status accepted, category SIGN
     */
    public String getCryptFingerprintSHA1() {
        return (this.getCryptFingerprintSHA1(1));
    }

    /**Returns the alias used for signing messages. This returns the cert
     * with prio "prio", status accepted, category SIGN
     */
    public String getCryptFingerprintSHA1(int prio) {
        PartnerCertificateInformation cryptInfo = this.getCertificateInformation(PartnerCertificateInformation.CATEGORY_CRYPT, prio);
        if (cryptInfo != null) {
            return (cryptInfo.getFingerprintSHA1());
        } else {
            return (null);
        }
    }

    /**Overwrite the existing cert list*/
    public void setPartnerCertificateInformationList(PartnerCertificateInformationList list) {
        this.partnerCertificateList = list;
    }

    /**Returns the existing cert list*/
    public PartnerCertificateInformationList getPartnerCertificateInformationList() {
        return (this.partnerCertificateList);
    }

    public int getEncryptionType() {
        return encryptionType;
    }

    public void setEncryptionType(int encryptionType) {
        this.encryptionType = encryptionType;
    }

    public int getSignType() {
        return signType;
    }

    public void setSignType(int signType) {
        this.signType = signType;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getURL() {
        return url;
    }

    public void setURL(String url) {
        this.url = url;
    }

    public String getMdnURL() {
        return mdnURL;
    }

    public void setMdnURL(String mdnURL) {
        this.mdnURL = mdnURL;
    }

    @Override
    public String toString() {
        if (this.name != null) {
            return (this.name);
        }
        return (this.as2Identification);
    }

    /**Overwrite the equal method of object
     *@param anObject object ot compare
     */
    @Override
    public boolean equals(Object anObject) {
        if (anObject == this) {
            return (true);
        }
        if (anObject != null && anObject instanceof Partner) {
            Partner partner = (Partner) anObject;
            return (partner.dbId == this.dbId);
        }
        return (false);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + (this.localStation ? 1 : 0);
        hash = 29 * hash + (this.as2Identification != null ? this.as2Identification.hashCode() : 0);
        hash = 29 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 29 * hash + this.encryptionType;
        hash = 29 * hash + this.signType;
        return hash;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public boolean isSyncMDN() {
        return syncMDN;
    }

    public void setSyncMDN(boolean syncMDN) {
        this.syncMDN = syncMDN;
    }

    public int getPollInterval() {
        return (this.pollInterval);
    }

    public void setPollInterval(int pollInterval) {
        if (pollInterval > 0) {
            this.pollInterval = pollInterval;
        }
    }

    public int getCompressionType() {
        return (this.compressionType);
    }

    public void setCompressionType(int compressionType) {
        this.compressionType = compressionType;
    }

    public boolean isSignedMDN() {
        return signedMDN;
    }

    public void setSignedMDN(boolean signedMDN) {
        this.signedMDN = signedMDN;
    }

    public String getCommandOnReceipt() {
        return commandOnReceipt;
    }

    public void setCommandOnReceipt(String commandOnReceipt) {
        this.commandOnReceipt = commandOnReceipt;
    }

    public boolean useCommandOnReceipt() {
        return useCommandOnReceipt;
    }

    public void setUseCommandOnReceipt(boolean useCommandOnReceipt) {
        this.useCommandOnReceipt = useCommandOnReceipt;
    }

    public HTTPAuthentication getAuthenticationAsyncMDN() {
        return authenticationAsyncMDN;
    }

    public void setAuthenticationAsyncMDN(HTTPAuthentication authenticationAsyncMDN) {
        this.authenticationAsyncMDN = authenticationAsyncMDN;
    }

    public HTTPAuthentication getAuthentication() {
        return authentication;
    }

    public void setAuthentication(HTTPAuthentication authentication) {
        this.authentication = authentication;
    }

    public void setKeepOriginalFilenameOnReceipt(boolean keepFilenameOnReceipt) {
        this.keepFilenameOnReceipt = keepFilenameOnReceipt;
    }

    public boolean getKeepOriginalFilenameOnReceipt() {
        return (this.keepFilenameOnReceipt);
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        if (comment == null || comment.length() == 0) {
            this.comment = null;
        } else {
            this.comment = comment;
        }
    }

    public int compare(Object one, Object two) {
        Partner obj1 = (Partner) one;
        Partner obj2 = (Partner) two;
        return (obj1.getName().compareToIgnoreCase(obj2.getName()));
    }

    @Override
    public int compareTo(Object obj) {
        Partner partner = (Partner) obj;
        return (this.name.compareToIgnoreCase(partner.name));
    }

    public int getNotifySend() {
        return notifySend;
    }

    public void setNotifySend(int notifySend) {
        this.notifySend = notifySend;
    }

    public int getNotifyReceive() {
        return notifyReceive;
    }

    public void setNotifyReceive(int notifyReceive) {
        this.notifyReceive = notifyReceive;
    }

    public int getNotifySendReceive() {
        return notifySendReceive;
    }

    public void setNotifySendReceive(int notifySendReceive) {
        this.notifySendReceive = notifySendReceive;
    }

    public boolean isNotifySendEnabled() {
        return notifySendEnabled;
    }

    public void setNotifySendEnabled(boolean notifySendEnabled) {
        this.notifySendEnabled = notifySendEnabled;
    }

    public boolean isNotifyReceiveEnabled() {
        return notifyReceiveEnabled;
    }

    public void setNotifyReceiveEnabled(boolean notifyReceiveEnabled) {
        this.notifyReceiveEnabled = notifyReceiveEnabled;
    }

    public boolean isNotifySendReceiveEnabled() {
        return notifySendReceiveEnabled;
    }

    public void setNotifySendReceiveEnabled(boolean notifySendReceiveEnabled) {
        this.notifySendReceiveEnabled = notifySendReceiveEnabled;
    }

    public String getCommandOnSendError() {
        return commandOnSendError;
    }

    public void setCommandOnSendError(String commandOnSendError) {
        this.commandOnSendError = commandOnSendError;
    }

    public boolean useCommandOnSendError() {
        return useCommandOnSendError;
    }

    public void setUseCommandOnSendError(boolean useCommandOnSendError) {
        this.useCommandOnSendError = useCommandOnSendError;
    }

    public String getCommandOnSendSuccess() {
        return commandOnSendSuccess;
    }

    public void setCommandOnSendSuccess(String commandOnSendSuccess) {
        this.commandOnSendSuccess = commandOnSendSuccess;
    }

    public boolean useCommandOnSendSuccess() {
        return useCommandOnSendSuccess;
    }

    public void setUseCommandOnSendSuccess(boolean useCommandOnSendSuccess) {
        this.useCommandOnSendSuccess = useCommandOnSendSuccess;
    }

    public String getDebugDisplay() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("Name:\t\t").append(this.getName());
        buffer.append(" (local station: ").append(this.isLocalStation()).append(")\n");
        buffer.append("AS2 id:\t\t").append(this.getAS2Identification()).append("\n");
        buffer.append("UseCommandOnSendError:\t\t").append(this.useCommandOnSendError).append("\n");
        buffer.append("CommandOnSendError:\t\t").append(this.getCommandOnSendError()).append("\n");
        buffer.append("UseCommandOnSendSuccess:\t\t").append(this.useCommandOnSendSuccess).append("\n");
        buffer.append("CommandOnSendSuccess:\t\t").append(this.getCommandOnSendSuccess()).append("\n");
        buffer.append("Max poll files:\t\t").append(this.getMaxPollFiles()).append("\n");
        return (buffer.toString());
    }

    /**
     * @return the contentTransferEncoding
     */
    public int getContentTransferEncoding() {
        return contentTransferEncoding;
    }

    /**
     * @param contentTransferEncoding the contentTransferEncoding to set
     */
    public void setContentTransferEncoding(int contentTransferEncoding) {
        this.contentTransferEncoding = contentTransferEncoding;
    }

    /**Serializes this partner to XML
     * @param level level in the XML hierarchie for the xml beautifying
     */
    public String toXML(CertificateManager manager, int level) {
        String offset = "";
        for (int i = 0; i < level; i++) {
            offset += "\t";
        }
        StringBuilder builder = new StringBuilder();
        builder.append(offset).append("<partner>\n");
        builder.append(offset).append("\t<name>").append(this.toCDATA(this.name)).append("</name>\n");
        builder.append(offset).append("\t<as2ident>").append(this.toCDATA(this.as2Identification)).append("</as2ident>\n");
        builder.append(offset).append("\t<usecommandonreceipt>").append(String.valueOf(this.useCommandOnReceipt)).append("</usecommandonreceipt>\n");
        if (this.commandOnReceipt != null) {
            builder.append(offset).append("\t<commandonreceipt>").append(this.toCDATA(this.commandOnReceipt)).append("</commandonreceipt>\n");
        }
        builder.append(offset).append("\t<usecommandonsenderror>").append(String.valueOf(this.useCommandOnSendError)).append("</usecommandonsenderror>\n");
        if (this.commandOnSendError != null) {
            builder.append(offset).append("\t<commandonsenderror>").append(this.toCDATA(this.commandOnSendError)).append("</commandonsenderror>\n");
        }
        builder.append(offset).append("\t<usecommandonsendsuccess>").append(String.valueOf(this.useCommandOnSendSuccess)).append("</usecommandonsendsuccess>\n");
        if (this.commandOnSendSuccess != null) {
            builder.append(offset).append("\t<commandonsendsuccess>").append(this.toCDATA(this.commandOnSendSuccess)).append("</commandonsendsuccess>\n");
        }
        if (this.comment != null) {
            builder.append(offset).append("\t<comment>").append(this.toCDATA(this.comment)).append("</comment>\n");
        }
        builder.append(offset).append("\t<contenttype>").append(this.toCDATA(this.contentType)).append("</contenttype>\n");
        //no longer used but for compatibility of older versions: write down the crypt alias
        PartnerCertificateInformation cryptInfo = this.getCertificateInformation(PartnerCertificateInformation.CATEGORY_CRYPT, 1);
        if (cryptInfo != null) {
            String cryptAlias = manager.getAliasByFingerprint(this.getCryptFingerprintSHA1());
            builder.append(offset).append("\t<cryptalias>").append(this.toCDATA(cryptAlias)).append("</cryptalias>\n");
        }
        builder.append(offset).append("\t<email>").append(this.toCDATA(this.email)).append("</email>\n");
        builder.append(offset).append("\t<mdnurl>").append(this.toCDATA(this.mdnURL)).append("</mdnurl>\n");
        //no longer used but for compatibility of older versions: write down the sign alias
        String signAlias = manager.getAliasByFingerprint(this.getSignFingerprintSHA1());
        if (signAlias != null) {
            builder.append(offset).append("\t<signalias>").append(this.toCDATA(signAlias)).append("</signalias>\n");
        }
        builder.append(offset).append("\t<subject>").append(this.toCDATA(this.subject)).append("</subject>\n");
        builder.append(offset).append("\t<url>").append(this.toCDATA(this.url)).append("</url>\n");
        builder.append(offset).append("\t<compression>").append(String.valueOf(this.compressionType)).append("</compression>\n");
        builder.append(offset).append("\t<transferencoding>").append(this.contentTransferEncoding).append("</transferencoding>\n");
        builder.append(offset).append("\t<encryptiontype>").append(String.valueOf(this.encryptionType)).append("</encryptiontype>\n");
        builder.append(offset).append("\t<keepfilename>").append(String.valueOf(this.keepFilenameOnReceipt)).append("</keepfilename>\n");
        builder.append(offset).append("\t<localstation>").append(String.valueOf(this.localStation)).append("</localstation>\n");
        builder.append(offset).append("\t<notifyreceive>").append(String.valueOf(this.notifyReceive)).append("</notifyreceive>\n");
        builder.append(offset).append("\t<notifyreceiveenabled>").append(String.valueOf(this.notifyReceiveEnabled)).append("</notifyreceiveenabled>\n");
        builder.append(offset).append("\t<notifysend>").append(String.valueOf(this.notifySend)).append("</notifysend>\n");
        builder.append(offset).append("\t<notifysendenabled>").append(String.valueOf(this.notifySendEnabled)).append("</notifysendenabled>\n");
        builder.append(offset).append("\t<notifysendreceiveenabled>").append(String.valueOf(this.notifySendReceiveEnabled)).append("</notifysendreceiveenabled>\n");
        builder.append(offset).append("\t<pollinterval>").append(String.valueOf(this.pollInterval)).append("</pollinterval>\n");
        if (this.pollIgnoreList != null) {
            builder.append(offset).append("\t<pollignorelist>").append(this.toCDATA(this.getPollIgnoreListAsString())).append("</pollignorelist>\n");
        }
        builder.append(offset).append("\t<signtype>").append(String.valueOf(this.signType)).append("</signtype>\n");
        builder.append(offset).append("\t<signedmdn>").append(String.valueOf(this.signedMDN)).append("</signedmdn>\n");
        builder.append(offset).append("\t<syncmdn>").append(String.valueOf(this.syncMDN)).append("</syncmdn>\n");
        if (this.authentication != null) {
            builder.append(this.authentication.toXML(level + 1, "standard"));
        }
        if (this.authenticationAsyncMDN != null) {
            builder.append(this.authenticationAsyncMDN.toXML(level + 1, "asyncmdn"));
        }
        if (this.httpHeader != null && this.httpHeader.size() > 0) {
            for (PartnerHttpHeader singleHeader : this.httpHeader) {
                builder.append("\t<httpheader>\n");
                builder.append("\t\t<key>").append(this.toCDATA(singleHeader.getKey())).append("</key>\n");
                builder.append("\t\t<value>").append(this.toCDATA(singleHeader.getValue())).append("</value>\n");
                builder.append("\t</httpheader>\n");
            }
        }
        builder.append(offset).append("</partner>\n");
        return (builder.toString());
    }

    /**Adds a cdata indicator to xml data*/
    private String toCDATA(String data) {
        return ("<![CDATA[" + data + "]]>");
    }

    /**Deserializes a partner from an XML node*/
    public static Partner fromXML(CertificateManager manager, Element element) {
        Partner partner = new Partner();
        NodeList propertiesNodeList = element.getChildNodes();
        for (int i = 0; i < propertiesNodeList.getLength(); i++) {
            if (propertiesNodeList.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element property = (Element) propertiesNodeList.item(i);
                String key = property.getTagName();
                String value = property.getTextContent();
                if (key.equals("name")) {
                    partner.setName(value);
                } else if (key.equals("as2ident")) {
                    partner.setAS2Identification(value);
                } else if (key.equals("usecommandonreceipt")) {
                    partner.setUseCommandOnReceipt(value.equalsIgnoreCase("true"));
                } else if (key.equals("commandonreceipt")) {
                    partner.setCommandOnReceipt(value);
                } else if (key.equals("usecommandonsenderror")) {
                    partner.setUseCommandOnSendError(value.equalsIgnoreCase("true"));
                } else if (key.equals("commandonsenderror")) {
                    partner.setCommandOnSendError(value);
                } else if (key.equals("usecommandonsendsuccess")) {
                    partner.setUseCommandOnSendSuccess(value.equalsIgnoreCase("true"));
                } else if (key.equals("commandonsendsuccess")) {
                    partner.setCommandOnSendSuccess(value);
                } else if (key.equals("comment")) {
                    partner.setComment(value);
                } else if (key.equals("contenttype")) {
                    partner.setContentType(value);
                } else if (key.equals("cryptalias")) {
                    KeystoreCertificate certificate = manager.getKeystoreCertificate(value);
                    if (certificate != null) {
                        partner.setCryptFingerprintSHA1(manager.getKeystoreCertificate(value).getFingerPrintSHA1());
                    }
                } else if (key.equals("email")) {
                    partner.setEmail(value);
                } else if (key.equals("mdnurl")) {
                    partner.setMdnURL(value);
                } else if (key.equals("signalias")) {
                    KeystoreCertificate certificate = manager.getKeystoreCertificate(value);
                    if (certificate != null) {
                        partner.setSignFingerprintSHA1(manager.getKeystoreCertificate(value).getFingerPrintSHA1());
                    }
                } else if (key.equals("subject")) {
                    partner.setSubject(value);
                } else if (key.equals("url")) {
                    partner.setURL(value);
                } else if (key.equals("compression")) {
                    partner.setCompressionType(Integer.valueOf(value).intValue());
                } else if (key.equals("transferencoding")) {
                    partner.setContentTransferEncoding(Integer.valueOf(value).intValue());
                } else if (key.equals("encryptiontype")) {
                    partner.setEncryptionType(Integer.valueOf(value).intValue());
                } else if (key.equals("keepfilename")) {
                    partner.setKeepOriginalFilenameOnReceipt(value.equalsIgnoreCase("true"));
                } else if (key.equals("localstation")) {
                    partner.setLocalStation(value.equalsIgnoreCase("true"));
                } else if (key.equals("notifyreceive")) {
                    partner.setNotifyReceive(Integer.valueOf(value).intValue());
                } else if (key.equals("notifyreceiveenabled")) {
                    partner.setNotifyReceiveEnabled(value.equalsIgnoreCase("true"));
                } else if (key.equals("notifysend")) {
                    partner.setNotifySend(Integer.valueOf(value).intValue());
                } else if (key.equals("notifysendenabled")) {
                    partner.setNotifySendEnabled(value.equalsIgnoreCase("true"));
                } else if (key.equals("notifysendreceiveenabled")) {
                    partner.setNotifySendReceiveEnabled(value.equalsIgnoreCase("true"));
                } else if (key.equals("pollinterval")) {
                    partner.setPollInterval(Integer.valueOf(value).intValue());
                } else if (key.equals("pollignorelist")) {
                    partner.setPollIgnoreListString(value);
                } else if (key.equals("signtype")) {
                    partner.setSignType(Integer.valueOf(value).intValue());
                } else if (key.equals("signedmdn")) {
                    partner.setSignedMDN(value.equalsIgnoreCase("true"));
                } else if (key.equals("syncmdn")) {
                    partner.setSyncMDN(value.equalsIgnoreCase("true"));
                } else if (key.equals("httpheader")) {
                    NodeList httpHeaderNodeList = property.getChildNodes();
                    PartnerHttpHeader httpHeader = new PartnerHttpHeader();
                    for (int ii = 0; ii < httpHeaderNodeList.getLength(); ii++) {
                        if (httpHeaderNodeList.item(ii).getNodeType() == Node.ELEMENT_NODE) {
                            Element headerProperty = (Element) httpHeaderNodeList.item(ii);
                            String httpHeaderKey = headerProperty.getTagName();
                            String httpHeaderValue = headerProperty.getTextContent();
                            if (httpHeaderKey.equalsIgnoreCase("key")) {
                                httpHeader.setKey(httpHeaderValue);
                            } else if (httpHeaderKey.equalsIgnoreCase("value")) {
                                httpHeader.setValue(httpHeaderValue);
                            }
                        }
                    }
                    if (httpHeader.getKey() != null && httpHeader.getKey().trim().length() > 0
                            && httpHeader.getValue() != null && httpHeader.getValue().trim().length() > 0) {
                        partner.addHttpHeader(httpHeader);
                    }
                }
            }
        }
        //deserialize the HTTP authentications
        NodeList authenticationNodeList = element.getElementsByTagName("httpauthentication");
        for (int i = 0; i < authenticationNodeList.getLength(); i++) {
            Element authenticationElement = (Element) authenticationNodeList.item(i);
            if (!authenticationElement.hasAttribute("type")) {
                continue;
            }
            if (authenticationElement.getAttribute("type").equalsIgnoreCase("standard")) {
                partner.setAuthentication(HTTPAuthentication.fromXML(authenticationElement));
            } else if (authenticationElement.getAttribute("type").equalsIgnoreCase("asyncmdn")) {
                partner.setAuthenticationAsyncMDN(HTTPAuthentication.fromXML(authenticationElement));
            }
        }
        return (partner);
    }

    /**
     * @return the httpHeader or null if it doesnt exist
     */
    public PartnerHttpHeader getHttpHeader(String key) {
        PartnerHttpHeader searchHeader = new PartnerHttpHeader();
        searchHeader.setKey(key);
        int index = this.httpHeader.indexOf(searchHeader);
        if (index >= 0) {
            return (this.httpHeader.get(index));
        }
        return (null);
    }

    /**Returns all http headers that are not listed in the passed list*/
    public List<PartnerHttpHeader> getAllNonListedHttpHeader(List<String> keyList) {
        List<PartnerHttpHeader> nonListedHeaders = new ArrayList<PartnerHttpHeader>();
        for (PartnerHttpHeader testHeader : this.httpHeader) {
            if (!keyList.contains(testHeader.getKey())) {
                nonListedHeaders.add(testHeader);
            }
        }
        return (nonListedHeaders);
    }

    /**Returns all http headers of this partner*/
    public List<PartnerHttpHeader> getHttpHeader() {
        return (this.httpHeader);
    }

    /**
     * @param httpHeader the httpHeader to set
     */
    public void addHttpHeader(PartnerHttpHeader httpHeader) {
        this.httpHeader.add(httpHeader);
    }

    /**
     * @return the httpProtocolVersion
     */
    public String getHttpProtocolVersion() {
        return httpProtocolVersion;
    }

    /**
     * @param httpProtocolVersion the httpProtocolVersion to set
     */
    public void setHttpProtocolVersion(String httpProtocolVersion) {
        if (httpProtocolVersion == null
                || (!httpProtocolVersion.equals(HttpConnectionParameter.HTTP_1_0)
                && !httpProtocolVersion.equals(HttpConnectionParameter.HTTP_1_1))) {
            throw new IllegalArgumentException("Partner.setHttpProtocolVersion(): Allowed values are \""
                    + HttpConnectionParameter.HTTP_1_0 + "\" and \"" + HttpConnectionParameter.HTTP_1_1 + "\".");
        }
        this.httpProtocolVersion = httpProtocolVersion;
    }

    /**
     * @return the maxPollFiles
     */
    public int getMaxPollFiles() {
        return maxPollFiles;
    }

    /**
     * @param maxPollFiles the maxPollFiles to set
     */
    public void setMaxPollFiles(int maxPollFiles) {
        this.maxPollFiles = maxPollFiles;
    }
}
