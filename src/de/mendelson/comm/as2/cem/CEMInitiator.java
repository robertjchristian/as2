//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/cem/CEMInitiator.java,v 1.1 2012/04/18 14:10:17 heller Exp $
package de.mendelson.comm.as2.cem;

import de.mendelson.comm.as2.cem.messages.EDIINTCertificateExchangeRequest;
import de.mendelson.comm.as2.cem.messages.EndEntity;
import de.mendelson.comm.as2.cem.messages.TradingPartnerInfo;
import de.mendelson.comm.as2.cem.messages.TrustRequest;
import de.mendelson.comm.as2.cert.CertificateAccessDB;
import de.mendelson.util.security.cert.CertificateManager;
import de.mendelson.util.security.cert.KeystoreCertificate;
import de.mendelson.comm.as2.message.AS2Message;
import de.mendelson.comm.as2.message.AS2MessageCreation;
import de.mendelson.comm.as2.message.AS2MessageInfo;
import de.mendelson.comm.as2.message.AS2Payload;
import de.mendelson.comm.as2.message.UniqueId;
import de.mendelson.comm.as2.partner.Partner;
import de.mendelson.comm.as2.sendorder.SendOrder;
import de.mendelson.comm.as2.sendorder.SendOrderSender;
import de.mendelson.comm.as2.server.AS2Server;
import de.mendelson.util.AS2Tools;
import de.mendelson.util.security.KeyStoreUtil;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.util.Date;
import java.util.logging.Logger;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Initiates a CEM request
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class CEMInitiator {

    /**Logger to log inforamtion to*/
    private Logger logger = Logger.getLogger(AS2Server.SERVER_LOGGER_NAME);
    /**Connection to the database*/
    private Connection configConnection = null;
    private Connection runtimeConnection = null;
    /**Stores the certificates*/
    private CertificateManager certificateManagerEncSign;
    /**Partner access*/
    private CertificateAccessDB certificateAccess;

    /** Creates new message I/O log and connects to localhost
     *@param host host to connect to
     */
    public CEMInitiator(Connection configConnection, Connection runtimeConnection, CertificateManager certificateManagerEncSign) {
        this.configConnection = configConnection;
        this.runtimeConnection = runtimeConnection;
        this.certificateManagerEncSign = certificateManagerEncSign;
        this.certificateAccess = new CertificateAccessDB(this.configConnection, this.runtimeConnection);
    }

    /**Sends the request to the partner*/
    public void sendRequest(Partner initiator, Partner receiver, KeystoreCertificate certificate,
            boolean encryptionUsage, boolean signatureUsage, boolean sslUsage, Date respondByDate)
            throws Exception {
        EDIINTCertificateExchangeRequest request = new EDIINTCertificateExchangeRequest();
        String requestId = UniqueId.createId();
        String requestContentId = UniqueId.createId();
        String certContentId = UniqueId.createId();
        request.setRequestId(requestId);
        TradingPartnerInfo partnerInfo = new TradingPartnerInfo();
        partnerInfo.setSenderAS2Id(initiator.getAS2Identification());
        request.setTradingPartnerInfo(partnerInfo);
        EndEntity endEntity = new EndEntity();
        endEntity.setContentId(certContentId);
        endEntity.setIssuerName(certificate.getIssuerDN());
        endEntity.setSerialNumber(certificate.getSerialNumberDEC());
        TrustRequest trustRequest = new TrustRequest();
        trustRequest.setResponseURL(initiator.getMdnURL());
        trustRequest.setRespondByDate(respondByDate);
        trustRequest.setCertUsageEncryption(encryptionUsage);
        trustRequest.setCertUsageSSL(sslUsage);
        trustRequest.setCertUsageSignature(signatureUsage);
        trustRequest.setEndEntity(endEntity);
        request.addTrustRequest(trustRequest);
        //export the certificate to a file and create a payload
        File certFile = this.exportCertificate(certificate, certContentId);
        AS2Payload[] payloads = new AS2Payload[2];
        File descriptionFile = this.storeRequest(request);
        //build up the XML description as payload
        AS2Payload payloadXML = new AS2Payload();
        payloadXML.setPayloadFilename(descriptionFile.getAbsolutePath());
        payloadXML.loadDataFromPayloadFile();
        payloadXML.setContentId(requestContentId);
        payloadXML.setContentType("application/ediint-cert-exchange+xml");
        payloads[0] = payloadXML;
        //build up the certificate as payload
        AS2Payload payloadCert = new AS2Payload();
        payloadCert.setPayloadFilename(certFile.getAbsolutePath());
        payloadCert.loadDataFromPayloadFile();
        payloadCert.setContentId(certContentId);
        payloadCert.setContentType("application/pkcs7-mime; smime-type=certs-only");
        payloads[1] = payloadCert;
        //send the message
        AS2MessageCreation creation = new AS2MessageCreation(this.certificateManagerEncSign, this.certificateManagerEncSign);
        AS2Message message = creation.createMessage(initiator, receiver, payloads, AS2Message.MESSAGETYPE_CEM);        
        SendOrder order = new SendOrder();
        order.setReceiver(receiver);
        order.setMessage(message);
        order.setSender(initiator);        
        SendOrderSender orderSender = new SendOrderSender(this.configConnection, this.runtimeConnection);
        orderSender.send(order);
        //set the certificates as fallback to the partner
        if (encryptionUsage) {
            this.setCertificateToPartner(initiator, certificate, CEMEntry.CATEGORY_CRYPT, 2);
        }
        if (signatureUsage) {
            this.setCertificateToPartner(initiator, certificate, CEMEntry.CATEGORY_SIGN, 2);
        }
        //enter the request to the CEM table in the db
        CEMAccessDB cemAccess = new CEMAccessDB(this.configConnection, this.runtimeConnection);
        cemAccess.insertRequest((AS2MessageInfo) message.getAS2Info(), initiator, receiver, request);
    }

    /**Sets a certificate to a partner*/
    private void setCertificateToPartner(Partner partner, KeystoreCertificate certificate, int category, int prio) {
        partner.getPartnerCertificateInformationList().insertNewCertificate(certificate.getFingerPrintSHA1(), category, prio);
        this.certificateAccess.storePartnerCertificateInformationList(partner);
        //display the changes in the certificates for the user in the log
        this.logger.fine(partner.getPartnerCertificateInformationList().getCertificatePurposeDescription(this.certificateManagerEncSign, partner, category));
    }

    private File exportCertificate(KeystoreCertificate certificate, String certContentId)
            throws Exception {
        KeyStoreUtil util = new KeyStoreUtil();
        String tempDir = System.getProperty("java.io.tmpdir");
        File[] exportFile = util.exportX509CertificatePKCS7(this.certificateManagerEncSign.getKeystore(),
                certificate.getAlias(), tempDir + certContentId + ".p7c");
        return (exportFile[0]);
    }

    private File storeRequest(EDIINTCertificateExchangeRequest request) throws Exception {
        File descriptionFile = AS2Tools.createTempFile("request", ".xml");
        OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(descriptionFile), "UTF-8");
        writer.write(request.toXML());
        writer.flush();
        writer.close();
        return (descriptionFile);
    }
}
