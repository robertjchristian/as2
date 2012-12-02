//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/notification/Notification.java,v 1.1 2012/04/18 14:10:31 heller Exp $
package de.mendelson.comm.as2.notification;

import de.mendelson.comm.as2.AS2ServerVersion;
import de.mendelson.util.security.cert.CertificateManager;
import de.mendelson.util.security.cert.KeystoreCertificate;
import de.mendelson.comm.as2.log.LogAccessDB;
import de.mendelson.comm.as2.log.LogEntry;
import de.mendelson.comm.as2.message.AS2MessageInfo;
import de.mendelson.comm.as2.message.MessageAccessDB;
import de.mendelson.comm.as2.partner.Partner;
import de.mendelson.comm.as2.partner.PartnerAccessDB;
import de.mendelson.comm.as2.partner.PartnerCertificateInformation;
import de.mendelson.comm.as2.server.AS2Server;
import de.mendelson.comm.as2.statistic.QuotaAccessDB;
import de.mendelson.comm.as2.statistic.StatisticOverviewEntry;
import de.mendelson.util.MecResourceBundle;
import java.io.File;
import java.net.InetAddress;
import java.sql.Connection;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Performs the notification for an event
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class Notification {

    private String templateDir = "notificationtemplates" + File.separator;
    /**Stores the connection data and notification eMail*/
    private NotificationData data;
    private Logger logger = Logger.getLogger(AS2Server.SERVER_LOGGER_NAME);
    /**localize your output*/
    private MecResourceBundle rb = null;
    //DB connection
    private Connection configConnection;
    private Connection runtimeConnection;

    /**Will not perform a lookup in the db but take the passed notification data object
     */
    public Notification(NotificationData data, Connection configConnection, Connection runtimeConnection) {
        this.configConnection = configConnection;
        this.runtimeConnection = runtimeConnection;
        //Load resourcebundle
        try {
            this.rb = (MecResourceBundle) ResourceBundle.getBundle(
                    ResourceBundleNotification.class.getName());
        } //load up  resourcebundle
        catch (MissingResourceException e) {
            throw new RuntimeException("Oops..resource bundle " + e.getClassName() + " not found.");
        }
        this.data = data;
    }

    /**Constructure without notification data, will perform a lookup in the db*/
    public Notification(Connection configConnection, Connection runtimeConnection) {
        this.configConnection = configConnection;
        this.runtimeConnection = runtimeConnection;
        //Load resourcebundle
        try {
            this.rb = (MecResourceBundle) ResourceBundle.getBundle(
                    ResourceBundleNotification.class.getName());
        } //load up  resourcebundle
        catch (MissingResourceException e) {
            throw new RuntimeException("Oops..resource bundle " + e.getClassName() + " not found.");
        }
        //figure out the notification data
        NotificationAccessDB access = new NotificationAccessDB(this.configConnection);
        this.data = access.getNotificationData();
    }

    public void sendResendDetected(AS2MessageInfo newMessageInfo, AS2MessageInfo alreadyExistingMessageInfo,
            Partner sender, Partner receiver) throws Exception{
        //do only notify if this is requested by the config
        if (!this.data.notifyResendDetected()) {
            return;
        }
        String filename = this.getLocalizedTemplateFilename("template_notification_resend_detected");
        Properties replacement = new Properties();
        replacement.setProperty("${PRODUCTNAME}", AS2ServerVersion.getProductName());
        replacement.setProperty("${HOST}", InetAddress.getLocalHost().getHostName());
        replacement.setProperty("${EXISTING_MESSAGE_INIT_TIME}", alreadyExistingMessageInfo.getInitDate().toString());
        replacement.setProperty("${MESSAGEID}", newMessageInfo.getMessageId());
        replacement.setProperty("${SENDER}", sender.getName());
        replacement.setProperty("${RECEIVER}", receiver.getName());
        NotificationMail mail = new NotificationMail();
        mail.read(filename, replacement);
        this.sendMail(mail);
        this.logger.fine(this.rb.getResourceString("misc.message.send", this.data.getNotificationMail()));
    }
    
    
    /**The system has imported a new certificate into the SSL keystore. The SSL connector
     * has to be restarted before these changes are taken*/
    public void sendSSLCertificateAddedByCEM(Partner partner, KeystoreCertificate cert) throws Exception {
        //do only notify if this is requested by the config
        if (!this.data.notifyCEM()) {
            return;
        }
        String filename = this.getLocalizedTemplateFilename("template_notification_cem_ssl_cert_added");
        Properties replacement = new Properties();
        replacement.setProperty("${PRODUCTNAME}", AS2ServerVersion.getProductName());
        replacement.setProperty("${HOST}", InetAddress.getLocalHost().getHostName());
        replacement.setProperty("${PARTNER}", partner.getName());
        StringBuilder techInfo = new StringBuilder();
        String alias = cert.getAlias();
        techInfo.append(alias).append(":\n");
        for (int i = 0; i < alias.length() + 1; i++) {
            techInfo.append("-");
        }
        techInfo.append("\n");
        techInfo.append(cert.getInfo());
        techInfo.append("\n");
        replacement.setProperty("${CERTIFICATETECHDETAILS}", techInfo.toString());
        NotificationMail mail = new NotificationMail();
        mail.read(filename, replacement);
        this.sendMail(mail);
        this.logger.fine(this.rb.getResourceString("misc.message.send", this.data.getNotificationMail()));
    }

    /**Sends a notification if the send quota has been exceeded
     * 
     * @param partner
     */
    public void sendPartnerSendQuotaExceeded(Partner localStation, Partner partner) {
        StatisticOverviewEntry entry = null;
        try {
            QuotaAccessDB access = new QuotaAccessDB(this.configConnection, this.runtimeConnection );
            entry = access.getStatisticOverview(
                    localStation.getAS2Identification(), partner.getAS2Identification());
            if (partner.isNotifySendEnabled() && partner.getNotifySend() == entry.getSendMessageCount()) {
                String filename = this.getLocalizedTemplateFilename("template_notification_sendquota_exceeded");
                Properties replacement = new Properties();
                replacement.setProperty("${PRODUCTNAME}", AS2ServerVersion.getProductName());
                replacement.setProperty("${HOST}", InetAddress.getLocalHost().getHostName());
                replacement.setProperty("${PARTNER}", partner.getName());
                replacement.setProperty("${QUOTA}", String.valueOf(partner.getNotifySend()));
                NotificationMail mail = new NotificationMail();
                mail.read(filename, replacement);
                this.sendMail(mail);
                this.logger.fine(this.rb.getResourceString("quota.send.message.send", this.data.getNotificationMail()));
            }
        } catch (Exception e) {
            return;
        }
    }

    /**Sends a notification if the receive quota has been exceeded
     * 
     * @param partner
     */
    public void sendPartnerReceiveQuotaExceeded(Partner localStation, Partner partner) {
        StatisticOverviewEntry entry = null;
        try {
            QuotaAccessDB access = new QuotaAccessDB(this.configConnection, this.runtimeConnection );
            entry = access.getStatisticOverview(
                    localStation.getAS2Identification(), partner.getAS2Identification());
            if (partner.isNotifyReceiveEnabled() && partner.getNotifyReceive() == entry.getReceivedMessageCount()) {
                String filename = this.getLocalizedTemplateFilename("template_notification_receivequota_exceeded");
                Properties replacement = new Properties();
                replacement.setProperty("${PRODUCTNAME}", AS2ServerVersion.getProductName());
                replacement.setProperty("${HOST}", InetAddress.getLocalHost().getHostName());
                replacement.setProperty("${PARTNER}", partner.getName());
                replacement.setProperty("${QUOTA}", String.valueOf(partner.getNotifyReceive()));
                NotificationMail mail = new NotificationMail();
                mail.read(filename, replacement);
                this.sendMail(mail);
                this.logger.fine(this.rb.getResourceString("quota.receive.message.send", this.data.getNotificationMail()));
            }
        } catch (Exception e) {
            return;
        }
    }

    /**Convinience method to send a notification to the administrator that something unexpected happened to the system*/
    public static void systemFailure(Connection configConnection, Connection runtimeConnection, Throwable exception) {
        Notification notification = new Notification(configConnection, runtimeConnection);
        notification.sendSystemFailure(exception);
    }

    /**Sends a notification to the administrator that something unexpected happened to the system*/
    public void sendSystemFailure(Throwable exception) {
        //do only notify if this is requested by the config
        if (!this.data.notifySystemFailure()) {
            return;
        }
        try {
            String filename = this.getLocalizedTemplateFilename("template_notification_systemproblem");
            Properties replacement = new Properties();
            replacement.setProperty("${PRODUCTNAME}", AS2ServerVersion.getProductName());
            replacement.setProperty("${HOST}", InetAddress.getLocalHost().getHostName());
            replacement.setProperty("${MESSAGE}", exception.getClass().getName()
                    + ": " + exception.getMessage());
            StackTraceElement[] trace = exception.getStackTrace();
            StringBuilder builder = new StringBuilder();
            for (StackTraceElement element : trace) {
                builder.append(element.toString());
                builder.append("\n");
            }
            replacement.setProperty("${DETAILS}", builder.toString());
            NotificationMail mail = new NotificationMail();
            mail.read(filename, replacement);
            this.sendMail(mail);
            this.logger.fine(this.rb.getResourceString("misc.message.send", this.data.getNotificationMail()));
        } catch (Exception e) {
            return;
        }
    }

    /**Sends a notification if the receive quota has been exceeded
     * 
     * @param partner
     */
    public void sendPartnerSendReceiveQuotaExceeded(Partner localStation, Partner partner) {
        StatisticOverviewEntry entry = null;
        try {
            QuotaAccessDB access = new QuotaAccessDB(this.configConnection, this.runtimeConnection );
            entry = access.getStatisticOverview(
                    localStation.getAS2Identification(), partner.getAS2Identification());
            if (partner.isNotifySendReceiveEnabled() && partner.getNotifySendReceive() == entry.getSendMessageCount() + entry.getReceivedMessageCount()) {
                String filename = this.getLocalizedTemplateFilename("template_notification_sendreceivequota_exceeded");
                Properties replacement = new Properties();
                replacement.setProperty("${PRODUCTNAME}", AS2ServerVersion.getProductName());
                replacement.setProperty("${HOST}", InetAddress.getLocalHost().getHostName());
                replacement.setProperty("${PARTNER}", partner.getName());
                replacement.setProperty("${QUOTA}", String.valueOf(partner.getNotifyReceive()));
                NotificationMail mail = new NotificationMail();
                mail.read(filename, replacement);
                this.sendMail(mail);
                this.logger.fine(this.rb.getResourceString("quota.sendreceive.message.send", this.data.getNotificationMail()));
            }
        } catch (Exception e) {
            return;
        }
    }

    /**Adds a _de _fr etc to the template name and returns it*/
    private String getLocalizedTemplateFilename(String templateName) {
        String language = Locale.getDefault().getLanguage();
        //select language specific template
        if (new File(this.templateDir + templateName + "_" + language).exists()) {
            templateName = this.templateDir + templateName + "_" + language;
        } else {
            templateName = this.templateDir + templateName;
        }
        return (templateName);
    }

    /**Sends a test notification
     * 
     */
    public void sendTest() throws Exception {
        String filename = this.getLocalizedTemplateFilename("template_notification_test");
        Properties replacement = new Properties();
        replacement.setProperty("${PRODUCTNAME}", AS2ServerVersion.getProductName());
        replacement.setProperty("${HOST}", InetAddress.getLocalHost().getHostName());
        replacement.setProperty("${USER}", System.getProperty("user.name"));
        NotificationMail mail = new NotificationMail();
        mail.read(filename, replacement);
        this.sendMail(mail);
        this.logger.fine(this.rb.getResourceString("test.message.send", this.data.getNotificationMail()));
    }

    /**Sends an email that a certification will expire*/
    public void sendCertificateWillExpire(KeystoreCertificate certificate, int expireDuration) throws Exception {
        //do only notify if this is requested by the config
        if (!this.data.notifyCertExpire()) {
            return;
        }
        String templateName = "template_notification_cert_expire";
        if (expireDuration <= 0) {
            templateName = templateName + "d";
        }
        String filename = this.getLocalizedTemplateFilename(templateName);
        Properties replacement = new Properties();
        replacement.setProperty("${PRODUCTNAME}", AS2ServerVersion.getProductName());
        replacement.setProperty("${HOST}", InetAddress.getLocalHost().getHostName());
        if (expireDuration >= 0) {
            replacement.setProperty("${DURATION}", String.valueOf(expireDuration));
        }
        replacement.setProperty("${ALIAS}", certificate.getAlias());
        NotificationMail mail = new NotificationMail();
        mail.read(filename, replacement);
        this.sendMail(mail);
        this.logger.fine(this.rb.getResourceString("cert.message.send",
                new Object[]{this.data.getNotificationMail(),
                    certificate.getAlias()
                }));
    }

    /**Sends an email that a CEM has been received*/
    public void sendCEMRequestReceived(Partner initiator) throws Exception {
        //do only notify if this is requested by the config
        if (!this.data.notifyCEM()) {
            return;
        }
        String filename = this.getLocalizedTemplateFilename("template_notification_cem_request_received");
        Properties replacement = new Properties();
        replacement.setProperty("${PRODUCTNAME}", AS2ServerVersion.getProductName());
        replacement.setProperty("${HOST}", InetAddress.getLocalHost().getHostName());
        replacement.setProperty("${PARTNER}", initiator.getName());
        NotificationMail mail = new NotificationMail();
        mail.read(filename, replacement);
        this.sendMail(mail);
        this.logger.fine(this.rb.getResourceString("misc.message.send",
                this.data.getNotificationMail()));
    }

    /**Sends an email that a CEM has been received
     */
    public void sendCertificateChangedByCEM(CertificateManager manager, Partner partner, int category) throws Exception {
        //do only notify if this is requested by the config
        if (!this.data.notifyCEM()) {
            return;
        }
        String filename = this.getLocalizedTemplateFilename("template_notification_cem_cert_changed");
        Properties replacement = new Properties();
        replacement.setProperty("${PRODUCTNAME}", AS2ServerVersion.getProductName());
        replacement.setProperty("${HOST}", InetAddress.getLocalHost().getHostName());
        String description = partner.getPartnerCertificateInformationList().getCertificatePurposeDescription(
                manager, partner, category);
        replacement.setProperty("${CERTIFICATEDESCRIPTION}", description);
        StringBuilder techInfo = new StringBuilder();
        PartnerCertificateInformation info1 = partner.getCertificateInformation(category, 1);
        PartnerCertificateInformation info2 = partner.getCertificateInformation(category, 2);
        if (info1 != null) {
            KeystoreCertificate certificate = manager.getKeystoreCertificateByFingerprintSHA1(info1.getFingerprintSHA1());
            if (certificate != null) {
                String alias = certificate.getAlias();
                techInfo.append(alias).append(":\n");
                for (int i = 0; i < alias.length() + 1; i++) {
                    techInfo.append("-");
                }
                techInfo.append("\n");
                techInfo.append(certificate.getInfo());
                techInfo.append("\n");
            }
        }
        if (info2 != null) {
            KeystoreCertificate certificate = manager.getKeystoreCertificateByFingerprintSHA1(info2.getFingerprintSHA1());
            if (certificate != null) {
                String alias = certificate.getAlias();
                techInfo.append(alias).append(":\n");
                for (int i = 0; i < alias.length() + 1; i++) {
                    techInfo.append("-");
                }
                techInfo.append("\n");
                techInfo.append(certificate.getInfo());
                techInfo.append("\n");
            }
        }
        replacement.setProperty("${CERTIFICATETECHDETAILS}", techInfo.toString());
        NotificationMail mail = new NotificationMail();
        mail.read(filename, replacement);
        this.sendMail(mail);
        this.logger.fine(this.rb.getResourceString("misc.message.send",
                this.data.getNotificationMail()));
    }

    /**Returns the message info object a a passed message id from the database*/
    private AS2MessageInfo getMessageInfo(String messageId) throws Exception {
        MessageAccessDB messageAccess 
                = new MessageAccessDB(this.configConnection, this.runtimeConnection);
        AS2MessageInfo info = messageAccess.getLastMessageEntry(messageId);
        if (info == null) {
            throw new Exception("No message entry found for " + messageId);
        }
        return (info);
    }

    /**Sends an email that an error occured in a transaction
     */
    public void sendTransactionError(String messageId) {
        //do only notify if this is requested by the config
        if (!this.data.notifyTransactionError()) {
            return;
        }
        AS2MessageInfo info = null;
        try {
            //get additional properties for the notification eMail
            String senderName = "Unknown";
            String receiverName = "Unknown";
            info = this.getMessageInfo(messageId);
            //lookup partner            
            PartnerAccessDB partnerAccess = new PartnerAccessDB(this.configConnection, this.runtimeConnection);
            if (info.getSenderId() != null) {
                Partner sender = partnerAccess.getPartner(info.getSenderId());
                if (sender != null) {
                    senderName = sender.getName();
                }
            }
            if (info.getReceiverId() != null) {
                Partner receiver = partnerAccess.getPartner(info.getReceiverId());
                if (receiver != null) {
                    receiverName = receiver.getName();
                }
            }
            LogAccessDB logAccess = new LogAccessDB(this.configConnection, this.runtimeConnection);
            StringBuilder log = new StringBuilder();
            LogEntry[] entries = logAccess.getLog(messageId);
            DateFormat format = DateFormat.getDateTimeInstance();
            for (LogEntry entry : entries) {
                if (log.length() > 0) {
                    log.append("\n");
                }
                log.append("[").append(format.format(new Date(entry.getMillis()))).append("] ");
                log.append(entry.getMessage());
            }
            String filename = this.getLocalizedTemplateFilename("template_notification_transaction_error");
            Properties replacement = new Properties();
            replacement.setProperty("${PRODUCTNAME}", AS2ServerVersion.getProductName());
            replacement.setProperty("${HOST}", InetAddress.getLocalHost().getHostName());
            replacement.setProperty("${MESSAGEID}", messageId);
            replacement.setProperty("${SENDER}", senderName);
            replacement.setProperty("${RECEIVER}", receiverName);
            replacement.setProperty("${LOG}", log.toString());
            if( info.getSubject() != null ){
                replacement.setProperty("${SUBJECT}", info.getSubject());
            }else{
                replacement.setProperty("${SUBJECT}", "");
            }
            NotificationMail mail = new NotificationMail();
            mail.read(filename, replacement);
            this.sendMail(mail);
            this.logger.log(Level.FINE, this.rb.getResourceString("transaction.message.send",
                    new Object[]{
                        info.getMessageId(),
                        this.data.getNotificationMail(),}), info);
        } catch (Exception e) {
            if (info == null) {
                this.logger.severe(this.rb.getResourceString("transaction.message.send.error",
                        new Object[]{
                            messageId,
                            this.data.getNotificationMail(),
                            e.getMessage()
                        }));
            } else {
                e.printStackTrace();
                this.logger.log(Level.SEVERE, this.rb.getResourceString("transaction.message.send.error",
                        new Object[]{
                            messageId,
                            this.data.getNotificationMail(),
                            e.getMessage()
                        }), info);
            }
        }
    }

    /**Returns the default session for the mail send process
     */
    private Session getDefaultSession() {
        Properties properties = new Properties();
        properties.put("mail.smtp.host", this.data.getMailServer());
        properties.put("mail.smtp.port", String.valueOf(this.data.getMailServerPort()));
        properties.put("mail.transport.protocol", "smtp");
        Session session = null;
        if (this.data.isUseSMTHAuth()) {
            properties.put("mail.smtp.auth", "true");
            session = Session.getInstance(properties,
                    new SendMailAuthenticator(this.data.getSmtpUser(),
                    String.valueOf(this.data.getSmtpPass())));
        } else {
            session = Session.getInstance(properties, null);
        }
        return (session);
    }

    @SuppressWarnings("static-access")
    private void sendMail(NotificationMail mail) throws Exception {
        Session session = this.getDefaultSession();
        // construct the message
        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(data.getReplyTo()));
        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(data.getNotificationMail(), false));
        msg.setSubject(mail.getSubject());
        msg.setText(mail.getBody());
        msg.setSentDate(new Date());
        msg.setHeader("X-Mailer", AS2ServerVersion.getProductName());
        // send the message
        Transport transport = null;
        try {
            transport = session.getTransport("smtp");
            transport.send(msg);
        } catch (SendFailedException sendFailedException) {
            Address failedAddresses[] = sendFailedException.getInvalidAddresses();
            StringBuilder errorMessage = new StringBuilder();
            if (failedAddresses != null) {
                errorMessage.append("The following mail addresses are invalid:").append("\n");
                for (Address address : failedAddresses) {
                    errorMessage.append(address.toString()).append("\n");
                }
            }
            Address validUnsentAddresses[] = sendFailedException.getValidUnsentAddresses();
            if (validUnsentAddresses != null) {
                errorMessage.append("No mail has been sent to the following valid addresses:").append("\n");
                for (Address address : validUnsentAddresses) {
                    errorMessage.append(address.toString()).append("\n");
                }
            }
            throw (sendFailedException);
        } finally {
            transport.close();
        }
    }

    /**Used for the SMTP authentication, this is required by some mail servers*/
    private static class SendMailAuthenticator extends Authenticator {

        private String user;
        private String password;

        public SendMailAuthenticator(String user, String password) {
            this.user = user;
            this.password = password;
        }

        @Override
        public PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(
                    this.user, this.password);
        }
    }
}
