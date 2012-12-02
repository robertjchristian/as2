//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/cem/CEMReceiptController.java,v 1.1 2012/04/18 14:10:17 heller Exp $
package de.mendelson.comm.as2.cem;

import de.mendelson.comm.as2.AS2Exception;
import de.mendelson.comm.as2.cem.messages.CertificateReference;
import de.mendelson.comm.as2.cem.messages.EDIINTCertificateExchangeRequest;
import de.mendelson.comm.as2.cem.messages.EDIINTCertificateExchangeResponse;
import de.mendelson.comm.as2.cem.messages.TradingPartnerInfo;
import de.mendelson.comm.as2.cem.messages.TrustRequest;
import de.mendelson.comm.as2.cem.messages.TrustResponse;
import de.mendelson.comm.as2.cert.CertificateAccessDB;
import de.mendelson.comm.as2.clientserver.message.RefreshClientCEMDisplay;
import de.mendelson.util.security.cert.CertificateManager;
import de.mendelson.comm.as2.message.AS2Message;
import de.mendelson.comm.as2.message.AS2MessageCreation;
import de.mendelson.comm.as2.message.AS2MessageInfo;
import de.mendelson.comm.as2.message.AS2Payload;
import de.mendelson.comm.as2.message.MessageAccessDB;
import de.mendelson.comm.as2.notification.Notification;
import de.mendelson.comm.as2.partner.Partner;
import de.mendelson.comm.as2.partner.PartnerAccessDB;
import de.mendelson.comm.as2.preferences.PreferencesAS2;
import de.mendelson.comm.as2.sendorder.SendOrder;
import de.mendelson.comm.as2.sendorder.SendOrderSender;
import de.mendelson.comm.as2.server.AS2Server;
import de.mendelson.util.AS2Tools;
import de.mendelson.util.MecResourceBundle;
import de.mendelson.util.XPathHelper;
import de.mendelson.util.clientserver.ClientServer;
import de.mendelson.util.security.BCCryptoHelper;
import de.mendelson.util.security.KeyStoreUtil;
import de.mendelson.util.security.cert.KeystoreCertificate;
import de.mendelson.util.security.cert.KeystoreStorageImplFile;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.security.Provider;
import java.security.Security;
import java.security.cert.Certificate;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Task that checks for inbound CEM requests and performs the required steps like
 *
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class CEMReceiptController {

    private Logger logger = Logger.getLogger(AS2Server.SERVER_LOGGER_NAME);
    private Connection configConnection;
    private Connection runtimeConnection;
    private CertificateManager certificateManager;
    private MecResourceBundle rb;
    private PreferencesAS2 preferences = new PreferencesAS2();
    private ClientServer clientServer;

    public CEMReceiptController(ClientServer clientServer, Connection configConnection,
            Connection runtimeConnection,
            CertificateManager certificateManager) {
        //load resource bundle
        try {
            this.rb = (MecResourceBundle) ResourceBundle.getBundle(
                    ResourceBundleCEM.class.getName());
        } catch (MissingResourceException e) {
            throw new RuntimeException("Oops..resource bundle " + e.getClassName() + " not found.");
        }
        this.configConnection = configConnection;
        this.runtimeConnection = runtimeConnection;
        this.certificateManager = certificateManager;
        this.clientServer = clientServer;
    }

    /**Checks a XML file against a W3C Schema and throws an exception if anything happens*/
    private void checkAgainstSchema(AS2Message message, File schemaFile, byte[] xmlData) throws Exception {
        //create a new W3C Schema instance
        SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
        Schema schema = factory.newSchema(schemaFile);
        Validator validator = schema.newValidator();
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true);
        DocumentBuilder builder = domFactory.newDocumentBuilder();
        ByteArrayInputStream memIn = new ByteArrayInputStream(xmlData);
        DOMSource source = new DOMSource(builder.parse(memIn));
        validator.validate(source);
        AS2MessageInfo info = (AS2MessageInfo) message.getAS2Info();
        this.logger.log(Level.INFO, this.rb.getResourceString("cem.validated.schema",
                new Object[]{
                    info.getMessageId()
                }), info);
        memIn.close();
    }

    /**Checks an inbound CEM and throws an error if anything goes wrong. This has to be done before
     * the MDN is sent. It will parse the xml description and see if all attachments are referenced etc
     */
    public void checkInboundCEM(AS2Message message) throws AS2Exception {
        AS2MessageInfo info = (AS2MessageInfo) message.getAS2Info();
        try {
            //check if a description file is part of the request. For compatibility reasons the server
            //is checking the payload by its content type only if the number of payloads are > 1. If there is
            //only one payload it is assumed that the ONE payload is the description.
            AS2Payload description = this.getPayloadByContentType(message.getPayloads(), "ediint-cert-exchange+xml");
            if (description == null) {
                if (message.getPayloadCount() == 1) {
                    description = message.getPayload(0);
                } else {
                    //do not localize, this will appear in the MDN
                    throw new Exception("CEM is in wrong structure: missing ediint-cert-exchange xml.");
                }
            }
            File cemSchema = new File("cem.xsd");
            this.checkAgainstSchema(message, cemSchema, description.getData());
            //parse the XML data to check if the content is in right structure and all attachments are present
            ByteArrayInputStream inStream = new ByteArrayInputStream(description.getData());
            XPathHelper helper = new XPathHelper(inStream);
            helper.addNamespace("x", "urn:ietf:params:xml:ns:ediintcertificateexchange");
            helper.addNamespace("ds", "http://www.w3.org/2000/09/xmldsig#");
            if (helper.getNodeCount("/x:EDIINTCertificateExchangeRequest") == 1) {
                this.logger.log(Level.INFO, this.rb.getResourceString("cemtype.request",
                        new Object[]{
                            info.getMessageId()
                        }), info);
                EDIINTCertificateExchangeRequest request = EDIINTCertificateExchangeRequest.parse(description.getData());
                if (!request.getTradingPartnerInfo().getSenderAS2Id().equals(info.getSenderId())) {
                    //do not localize, will be returned in an MDN
                    throw new Exception("CEM request sender AS2 id (" + request.getTradingPartnerInfo().getSenderAS2Id() + ") is not the same as the message sender AS2 id (" + info.getSenderId() + ")");
                }
                //check if all referenced content ids are available as attachments
                List<TrustRequest> requestList = request.getTrustRequestList();
                for (TrustRequest trustRequest : requestList) {
                    String contentId = trustRequest.getEndEntity().getContentId();
                    AS2Payload referencedPayload = this.getPayloadByContentId(message.getPayloads(), contentId);
                    if (referencedPayload == null) {
                        throw new Exception("The CEM request references an attached certificate with the content id " + contentId + " which is not part of the message.");
                    }
                }
            } else if (helper.getNodeCount("/x:EDIINTCertificateExchangeResponse") == 1) {
                this.logger.log(Level.INFO, this.rb.getResourceString("cemtype.response", new Object[]{
                            info.getMessageId()
                        }), info);
                EDIINTCertificateExchangeResponse response = EDIINTCertificateExchangeResponse.parse(description.getData());
                CEMAccessDB cemAccess = new CEMAccessDB(this.configConnection, this.runtimeConnection);
                if (!cemAccess.requestExists(response.getRequestId())) {
                    //do not loalize, will be returned in an MDN
                    throw new Exception("Related CEM request with requestId " + response.getRequestId() + " does not exist.");
                } else {
                    this.logger.log(Level.INFO, this.rb.getResourceString("cem.response.relatedrequest.found", response.getRequestId()), info);
                }
                if (!response.getTradingPartnerInfo().getSenderAS2Id().equals(info.getSenderId())) {
                    //do not loalize, will be returned in an MDN
                    throw new Exception("CEM request sender AS2 id (" + response.getTradingPartnerInfo().getSenderAS2Id() + ") is not the same as the message sender AS2 id (" + info.getSenderId() + ")");
                }
            } else {
                //no idea what this request is about
                throw new AS2Exception(AS2Exception.PROCESSING_ERROR, "The inbound CEM message is neither a certificate request or a certificate response - unable to process it", message);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new AS2Exception(AS2Exception.PROCESSING_ERROR, e.getMessage(), message);
        }
    }

    /**Gets an inbound CEM and processes it*/
    public void processInboundCEM(AS2MessageInfo messageInfo) throws Throwable {
        List<AS2Payload> payloads = this.getPayloads(messageInfo);
        AS2Payload description = this.getPayloadByContentType(payloads, "ediint-cert-exchange+xml");
        //check if it is a request or a response
        ByteArrayInputStream inStream = new ByteArrayInputStream(description.getData());
        XPathHelper helper = new XPathHelper(inStream);
        helper.addNamespace("x", "urn:ietf:params:xml:ns:ediintcertificateexchange");
        helper.addNamespace("ds", "http://www.w3.org/2000/09/xmldsig#");
        if (helper.getNodeCount("/x:EDIINTCertificateExchangeRequest") == 1) {
            this.processInboundCEMRequest(messageInfo, payloads, description);
        } else if (helper.getNodeCount("/x:EDIINTCertificateExchangeResponse") == 1) {
            this.processInboundCEMResponse(messageInfo, description);
        }
    }

    /**Processes an inbound CEM request and answers a CEM response*/
    private void processInboundCEMRequest(AS2MessageInfo info, List<AS2Payload> payloads, AS2Payload description) throws Throwable {
        PartnerAccessDB partnerAccess 
                = new PartnerAccessDB(this.configConnection,
                        this.runtimeConnection);
        Partner initiator = partnerAccess.getPartner(info.getSenderId());
        Partner receiver = partnerAccess.getPartner(info.getReceiverId());
        EDIINTCertificateExchangeRequest request = EDIINTCertificateExchangeRequest.parse(description.getData());
        //auto import the attached certificates into the right keystore: SSL to the SSL keystore,
        //encryption and signature to the enc/singnature keystore
        List<TrustResponse> trustResponses = this.importCertificates(initiator, info, request, payloads);
        EDIINTCertificateExchangeResponse response = new EDIINTCertificateExchangeResponse();
        response.setRequestId(request.getRequestId());
        TradingPartnerInfo partnerInfo = new TradingPartnerInfo();
        partnerInfo.setSenderAS2Id(info.getReceiverId());
        response.setTradingPartnerInfo(partnerInfo);
        for (TrustResponse trustResponse : trustResponses) {
            response.addTrustResponse(trustResponse);
        }
        //enter the request to the CEM table in the db
        CEMAccessDB cemAccess = new CEMAccessDB(this.configConnection, this.runtimeConnection);
        cemAccess.insertRequest(info, initiator, receiver, request);
        if (this.clientServer != null) {
            this.clientServer.broadcastToClients(new RefreshClientCEMDisplay());
        }
        //do not insert any new certificate and reject the request if the CEM is disabled in the
        //preferences
        boolean cemEnabled = this.preferences.getBoolean(PreferencesAS2.CEM);
        if (cemEnabled) {
            //for sign or SSL certificates the initiator may use them immediatly after a accept response
            //OR after the respondBy date. Insert the certificate to the partner now with prio2 if its the receiver
            List<TrustRequest> requestList = request.getTrustRequestList();
            CertificateAccessDB certificateAccess 
                    = new CertificateAccessDB(this.configConnection, this.runtimeConnection);
            for (TrustRequest trustRequest : requestList) {
                if (trustRequest.isCertUsageSignature()) {
                    //cert should be imported now
                    KeystoreCertificate referencedCert = this.certificateManager.getKeystoreCertificateByIssuerAndSerial(
                            trustRequest.getEndEntity().getIssuerName(), trustRequest.getEndEntity().getSerialNumber());
                    if (referencedCert == null) {
                        throw new Exception("Certificate with issuer " + trustRequest.getEndEntity().getIssuerName()
                                + " and serial " + trustRequest.getEndEntity().getSerialNumber() + " not found");
                    }
                    initiator.getPartnerCertificateInformationList().
                            insertNewCertificate(referencedCert.getFingerPrintSHA1(),
                            CEMEntry.CATEGORY_SIGN, 2);
                    certificateAccess.storePartnerCertificateInformationList(initiator);
                    this.logger.fine(initiator.getPartnerCertificateInformationList().getCertificatePurposeDescription(this.certificateManager, initiator, CEMEntry.CATEGORY_SIGN));
                }
            }
        } else {
            AS2Message errorMessage = new AS2Message(info);
            //the user has disabled the CEM support
            throw new AS2Exception(AS2Exception.PROCESSING_ERROR, "The CEM support of this AS2 system is disabled in the system configuration by the user",
                    errorMessage);
        }
        //now send the response and insert the response data into the database
        this.sendResponse(info, info.getReceiverId(), info.getSenderId(), response);
        //send a CEM notification if this is requested in the config
        Notification notification = new Notification(this.configConnection, this.runtimeConnection);
        try {
            notification.sendCEMRequestReceived(initiator);
        } catch (Exception e) {
            logger.severe("CEMReceiptController: " + e.getMessage());
            Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
        }
    }

    /**Processes the inbound CEM response*/
    private void processInboundCEMResponse(AS2MessageInfo info, AS2Payload description) throws Exception {
        EDIINTCertificateExchangeResponse response = EDIINTCertificateExchangeResponse.parse(description.getData());
        //insert the response into the database
        CEMAccessDB cemAccess = new CEMAccessDB(this.configConnection, this.runtimeConnection);
        //insert the request data into the certificate database
        PartnerAccessDB partnerAccess 
                = new PartnerAccessDB(this.configConnection,
                        this.runtimeConnection);
        Partner receiver = partnerAccess.getPartner(info.getSenderId());
        Partner initiator = partnerAccess.getPartner(info.getReceiverId());
        cemAccess.insertResponse(info, initiator, receiver, response);
        if (this.clientServer != null) {
            this.clientServer.broadcastToClients(new RefreshClientCEMDisplay());
        }
    }

    /**Returns the payloads that are assigned to the passed message info*/
    private List<AS2Payload> getPayloads(AS2MessageInfo info) throws Exception {
        MessageAccessDB messageAccess 
                = new MessageAccessDB(this.configConnection, this.runtimeConnection);
        List<AS2Payload> payloads = messageAccess.getPayload(info.getMessageId());
        for (AS2Payload payload : payloads) {
            payload.loadDataFromPayloadFile();
        }
        return (payloads);
    }

    /**Checks the content types of the passed payload and returns the first found payload*/
    private AS2Payload getPayloadByContentType(List<AS2Payload> payloads, String contentType) {
        for (AS2Payload payload : payloads) {
            if (payload.getContentType() != null && contentType != null
                    && payload.getContentType().toLowerCase().contains(contentType.toLowerCase())) {
                return (payload);
            }
        }
        return (null);
    }

    /**Checks the content ids of the passed payload and returns the first found payload*/
    private AS2Payload getPayloadByContentId(List<AS2Payload> payloads, String contentId) {
        for (AS2Payload payload : payloads) {
            if (payload.getContentId() != null && contentId != null
                    && payload.getContentId().toLowerCase().contains(contentId.toLowerCase())) {
                return (payload);
            }
        }
        return (null);
    }

    /**Sends the respose of a CEM and iserts the response into the database*/
    private void sendResponse(AS2MessageInfo requestInfo, String senderId, String receiverId, EDIINTCertificateExchangeResponse response) throws Exception {
        PartnerAccessDB partnerAccess 
                = new PartnerAccessDB(this.configConnection,
                        this.runtimeConnection);
        Partner sender = partnerAccess.getPartner(senderId);
        Partner receiver = partnerAccess.getPartner(receiverId);
        AS2MessageCreation creation = new AS2MessageCreation(this.certificateManager, this.certificateManager);
        //store the payload
        File payloadFile = AS2Tools.createTempFile("AS2Response", ".xml");
        FileOutputStream fileOut = new FileOutputStream(payloadFile);
        OutputStreamWriter writer = new OutputStreamWriter(fileOut, "UTF-8");
        writer.write(response.toXML());
        writer.flush();
        writer.close();
        AS2Payload descriptionXML = new AS2Payload();
        descriptionXML.setContentType("application/ediint-cert-exchange+xml");
        descriptionXML.setPayloadFilename(payloadFile.getAbsolutePath());
        descriptionXML.loadDataFromPayloadFile();
        AS2Message message = creation.createMessage(sender, receiver, new AS2Payload[]{descriptionXML}, AS2Message.MESSAGETYPE_CEM);
        this.logger.log(Level.INFO, this.rb.getResourceString("cem.response.prepared",
                new Object[]{
                    requestInfo.getMessageId(), response.getRequestId()
                }), requestInfo);
        SendOrder order = new SendOrder();
        order.setReceiver(receiver);
        order.setMessage(message);
        order.setSender(sender);        
        SendOrderSender orderSender = new SendOrderSender(this.configConnection, this.runtimeConnection);
        orderSender.send(order);        
        CEMAccessDB cemAccess = new CEMAccessDB(this.configConnection, this.runtimeConnection);
        cemAccess.insertResponse((AS2MessageInfo) message.getAS2Info(), receiver, sender, response);
        if (this.clientServer != null) {
            this.clientServer.broadcastToClients(new RefreshClientCEMDisplay());
        }
    }

    /**Imports a single certificate in the keystore that is wrapped by the passed certificate manager
     * Returns if the import has been skipped (it already exists) or if the import has been performed
     */
    private boolean importSingleCertificate(AS2MessageInfo info, CertificateManager keystoreManager, Certificate cert) throws Throwable {
        KeyStoreUtil util = new KeyStoreUtil();
        boolean imported = false;
        //check if the cert already exists
        String importAlias = util.getCertificateAlias(keystoreManager.getKeystore(), util.convertToX509Certificate(cert));
        if (importAlias != null) {
            this.logger.log(Level.WARNING, this.rb.getResourceString("cert.already.imported",
                    new Object[]{
                        info.getMessageId(),
                        importAlias
                    }), info);
        } else {
            //import the new alias
            Provider provBC = Security.getProvider("BC");
            importAlias = util.importX509Certificate(keystoreManager.getKeystore(), util.convertToX509Certificate(cert), provBC);
            keystoreManager.saveKeystore();
            this.logger.log(Level.FINE, this.rb.getResourceString("cert.imported.success",
                    new Object[]{
                        info.getMessageId(),
                        importAlias
                    }), info);
            imported = true;
        }
        return (imported);
    }

    /**Auto imports the CEM request certificates into the encryption/sign keystore if they dont exist so far*/
    private List<TrustResponse> importCertificates(Partner initiator, AS2MessageInfo info, EDIINTCertificateExchangeRequest request, List<AS2Payload> payloads) throws Throwable {
        KeyStoreUtil util = new KeyStoreUtil();
        Provider provBC = Security.getProvider("BC");
        List<TrustResponse> trustResponseList = new ArrayList<TrustResponse>();
        List<TrustRequest> trustRequestList = request.getTrustRequestList();
        //do reject the request if the CEM is disabled in the
        //preferences
        boolean cemEnabled = this.preferences.getBoolean(PreferencesAS2.CEM);
        for (TrustRequest trustRequest : trustRequestList) {
            //read certificates from the payloads
            AS2Payload certPayload = this.getPayloadByContentId(payloads, trustRequest.getEndEntity().getContentId());
            FileInputStream inStream = new FileInputStream(certPayload.getPayloadFilename());
            Collection<? extends Certificate> certList = util.readCertificates(inStream, provBC);
            inStream.close();
            TrustResponse trustResponse = new TrustResponse();
            if (cemEnabled) {
                trustResponse.setState(TrustResponse.STATUS_ACCEPTED_STR);
                try {
                    //import the cert into the encryption/signature keystore
                    for (Certificate cert : certList) {
                        if (trustRequest.isCertUsageEncryption() || trustRequest.isCertUsageSignature()) {
                            boolean imported = this.importSingleCertificate(info, this.certificateManager, cert);
                        }
                        if (trustRequest.isCertUsageSSL()) {
                            //import the certificate into the SSL keystore
                            CertificateManager sslManager = new CertificateManager(this.logger);
                            String keystoreFile = new File(this.preferences.get(PreferencesAS2.KEYSTORE_HTTPS_SEND)).getAbsolutePath();
                            KeystoreStorageImplFile storage = new KeystoreStorageImplFile(keystoreFile,
                                    this.preferences.get(PreferencesAS2.KEYSTORE_HTTPS_SEND_PASS).toCharArray(), BCCryptoHelper.KEYSTORE_JKS);
                            sslManager.loadKeystoreCertificates(storage);
                            boolean imported = this.importSingleCertificate(info, sslManager, cert);
                            //notify the user that a SSL certificate has been changed an the SSL connector must be resetted
                            if (imported) {
                                String importAlias = util.getCertificateAlias(sslManager.getKeystore(), util.convertToX509Certificate(cert));
                                Notification notification = new Notification(this.configConnection, this.runtimeConnection);
                                notification.sendSSLCertificateAddedByCEM(initiator, sslManager.getKeystoreCertificate(importAlias));
                            }
                        }
                    }
                } catch (Exception e) {
                    //if the import fails the trust response state should be set to REJECTED with an error message
                    trustResponse.setState(TrustResponse.STATUS_REJECTED_STR);
                    String rejectionReason = "Failure in certificate import process: " + e.getMessage();
                    this.logger.warning(rejectionReason);
                    trustResponse.setReasonForRejection(rejectionReason);
                }
            } else {
                //cem is disabled
                trustResponse.setState(TrustResponse.STATUS_REJECTED_STR);
                //do not localize the reason, its part of the response
                trustResponse.setReasonForRejection("CEM has been disabled in the system settings.");
            }
            //ensure to really return the same issuer name as requested if the other side performs a simple string compare on it
            CertificateReference certificateReference = new CertificateReference();
            certificateReference.setCertficiate(trustRequest.getEndEntity().getIssuerName(), trustRequest.getEndEntity().getSerialNumber());
            trustResponse.setCertificateReference(certificateReference);
            trustResponseList.add(trustResponse);
        }
        this.certificateManager.rereadKeystoreCertificates();
        return (trustResponseList);
    }
}
