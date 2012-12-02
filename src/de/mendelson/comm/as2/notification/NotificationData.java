//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/notification/NotificationData.java,v 1.1 2012/04/18 14:10:31 heller Exp $
package de.mendelson.comm.as2.notification;

import java.io.Serializable;
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
 * Implementation of a server log for the as2 server database
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class NotificationData implements Serializable{

    private String notificationMail = null;
    private String mailServer = null;
    private int mailServerPort = 25;
    private String accountName = null;
    private char[] accountPassword = null;
    private boolean notifyCertExpire = false;
    private boolean notifyTransactionError = false;
    private boolean notifyCEM = false;
    private boolean notifySystemFailure = false;
    //TODO CONFIGURABLE!!!
    private boolean notifyResendDetected = true;
    /**Makes no sense but some mail servers require this to be a valid email from the same host to prevent SPAM sending*/
    private String replyTo = null;

    private boolean useSMTHAuth = false;
    private String smtpUser = null;
    private char[] smtpPass = null;

    public String getNotificationMail() {
        return notificationMail;
    }

    public void setNotificationMail(String notificationMail) {
        this.notificationMail = notificationMail;
    }

    public String getMailServer() {
        return mailServer;
    }

    public void setMailServer(String mailServer) {
        this.mailServer = mailServer;
    }

    public int getMailServerPort() {
        return mailServerPort;
    }

    public void setMailServerPort(int mailServerPort) {
        this.mailServerPort = mailServerPort;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public char[] getAccountPassword() {
        return accountPassword;
    }

    public void setAccountPassword(char[] accountPassword) {
        this.accountPassword = accountPassword;
    }

    public boolean notifyCertExpire() {
        return notifyCertExpire;
    }

    public void setNotifyCertExpire(boolean notifyCertExpire) {
        this.notifyCertExpire = notifyCertExpire;
    }

    public boolean notifyTransactionError() {
        return notifyTransactionError;
    }

    public void setNotifyTransactionError(boolean notifyTransactionError) {
        this.notifyTransactionError = notifyTransactionError;
    }

    public String getReplyTo() {
        return replyTo;
    }

    public void setReplyTo(String replyTo) {
        this.replyTo = replyTo;
    }

    /**Serializes this notification data object to XML
     * @param level level in the XML hierarchie for the xml beatifying
     */
    public String toXML(int level) {
        StringBuilder builder = new StringBuilder();
        String offset = "";
        for (int i = 0; i < level; i++) {
            offset += "\t";
        }
        builder.append(offset).append("<notification>\n");
        builder.append(offset).append("\t<accountname>").append(this.toCDATA(this.accountName)).append("</accountname>\n");
        builder.append(offset).append("\t<accountpass>").append(this.toCDATA(String.valueOf(this.accountPassword))).append("</accountpass>\n");
        builder.append(offset).append("\t<mailserver>").append(this.toCDATA(this.mailServer)).append("</mailserver>\n");
        builder.append(offset).append("\t<mailserverport>").append(this.mailServerPort).append("</mailserverport>\n");
        builder.append(offset).append("\t<notificationmail>").append(this.toCDATA(this.notificationMail)).append("</notificationmail>\n");
        builder.append(offset).append("\t<notifycertexpire>").append(String.valueOf(this.notifyCertExpire)).append("</notifycertexpire>\n");
        builder.append(offset).append("\t<notifytransactionerror>").append(String.valueOf(this.notifyTransactionError)).append("</notifytransactionerror>\n");
        builder.append(offset).append("\t<notifysystemfailure>").append(String.valueOf(this.notifySystemFailure)).append("</notifysystemfailure>\n");
        builder.append(offset).append("\t<notifycem>").append(String.valueOf(this.notifyCEM)).append("</notifycem>\n");
        builder.append(offset).append("\t<replyto>").append(this.toCDATA(this.replyTo)).append("</replyto>\n");
        builder.append(offset).append("</notification>\n");
        return (builder.toString());
    }

    /**Adds a cdata indicator to xml data*/
    private String toCDATA(String data) {
        return ("<![CDATA[" + data + "]]>");
    }

    /**Deserializes a notification from an XML node*/
    public static NotificationData fromXML(Element element) {
        NotificationData notification = new NotificationData();
        NodeList notificationNodeList = element.getChildNodes();
        for (int i = 0; i < notificationNodeList.getLength(); i++) {
            if (notificationNodeList.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element property = (Element) notificationNodeList.item(i);
                String key = property.getTagName();
                String value = property.getTextContent();
                if (key.equals("accountname")) {
                    notification.setAccountName(value);
                } else if (key.equals("accountpass")) {
                    notification.setAccountPassword(value.toCharArray());
                } else if (key.equals("mailserver")) {
                    notification.setMailServer(value);
                } else if (key.equals("mailserverport")) {
                    notification.setMailServerPort(Integer.valueOf(value).intValue());
                } else if (key.equals("notificationmail")) {
                    notification.setNotificationMail(value);
                } else if (key.equals("notifycertexpire")) {
                    notification.setNotifyCertExpire(value.equalsIgnoreCase("true"));
                } else if (key.equals("notifytransactionerror")) {
                    notification.setNotifyTransactionError(value.equalsIgnoreCase("true"));
                }else if (key.equals("notifysystemfailure")) {
                    notification.setNotifySystemFailure(value.equalsIgnoreCase("true"));
                }else if (key.equals("notifycem")) {
                    notification.setNotifyCEM(value.equalsIgnoreCase("true"));
                } else if (key.equals("replyto")) {
                    notification.setReplyTo(value);
                }
            }
        }
        return (notification);
    }

    /**
     * @return the notifyCEM
     */
    public boolean notifyCEM() {
        return notifyCEM;
    }

    /**
     * @param notifyCEM the notifyCEM to set
     */
    public void setNotifyCEM(boolean notifyCEM) {
        this.notifyCEM = notifyCEM;
    }

    /**
     * @return the useSMTHAuth
     */
    public boolean isUseSMTHAuth() {
        return useSMTHAuth;
    }

    /**
     * @param useSMTHAuth the useSMTHAuth to set
     */
    public void setUseSMTHAuth(boolean useSMTHAuth) {
        this.useSMTHAuth = useSMTHAuth;
    }

    /**
     * @return the smtpUser
     */
    public String getSmtpUser() {
        return smtpUser;
    }

    /**
     * @param smtpUser the smtpUser to set
     */
    public void setSmtpUser(String smtpUser) {
        this.smtpUser = smtpUser;
    }

    /**
     * @return the smtpPass
     */
    public char[] getSmtpPass() {
        return smtpPass;
    }

    /**
     * @param smtpPass the smtpPass to set
     */
    public void setSmtpPass(char[] smtpPass) {
        this.smtpPass = smtpPass;
    }

    /**
     * @return the notifySystemFailure
     */
    public boolean notifySystemFailure() {
        return notifySystemFailure;
    }

    /**
     * @param notifySystemFailure the notifySystemFailure to set
     */
    public void setNotifySystemFailure(boolean notifySystemFailure) {
        this.notifySystemFailure = notifySystemFailure;
    }

    /**
     * @return the notifyResendDetected
     */
    public boolean notifyResendDetected() {
        return( this.notifyResendDetected);
    }

    /**
     * @param notifyResendDetected the notifyResendDetected to set
     */
    public void setNotifyResendDetected(boolean notifyResendDetected) {
        this.notifyResendDetected = notifyResendDetected;
    }

}
