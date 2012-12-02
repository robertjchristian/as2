//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/importexport/ConfigurationImport.java,v 1.1 2012/04/18 14:10:30 heller Exp $
package de.mendelson.comm.as2.importexport;

import de.mendelson.util.security.cert.CertificateManager;
import de.mendelson.comm.as2.notification.NotificationAccessDB;
import de.mendelson.comm.as2.notification.NotificationData;
import de.mendelson.comm.as2.partner.Partner;
import de.mendelson.comm.as2.partner.PartnerAccessDB;
import de.mendelson.comm.as2.preferences.PreferencesAS2;
import de.mendelson.comm.as2.server.AS2Server;
import de.mendelson.util.security.BCCryptoHelper;
import de.mendelson.util.security.cert.KeystoreStorage;
import de.mendelson.util.security.cert.KeystoreStorageImplFile;
import java.io.InputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Exports configuration data to a file
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class ConfigurationImport {

    private Connection configConnection;
    private Connection runtimeConnection;
    private PreferencesAS2 preferences = new PreferencesAS2();
    private CertificateManager certificateManager;
    private Logger logger = Logger.getLogger(AS2Server.SERVER_LOGGER_NAME);

    /**@param connection database connection to use to write the config*/
    public ConfigurationImport(Connection configConnection, Connection runtimeConnection) {
        this.configConnection = configConnection;
        this.runtimeConnection = runtimeConnection;
        this.certificateManager = new CertificateManager(this.logger);
        try {
            KeystoreStorage storage = new KeystoreStorageImplFile("certificates.p12",
                    this.preferences.get(PreferencesAS2.KEYSTORE_PASS).toCharArray(),
                    BCCryptoHelper.KEYSTORE_PKCS12);
            this.certificateManager.loadKeystoreCertificates(storage);
        } catch (Exception e) {
            this.logger.severe(e.getMessage());
        }
    }

    /**parses a xml stream and returns a DOM document*/
    public Document parseStream(InputStream inStream) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource source = new InputSource(inStream);
        Document document = builder.parse(source);
        return (document);
    }

    /**Finally performs the import of the data*/
    public void importData(InputStream inStream, List<Partner> partnerListToImport, boolean importNotification, boolean importServerSettings) throws Exception {
        Element rootElement = this.getRootElement(inStream);
        if (importNotification) {
            NotificationData notification = this.readNotificationData(rootElement);
            NotificationAccessDB access = new NotificationAccessDB(this.configConnection);
            access.updateNotification(notification);
        }
        //insert or update the partners, that depends on the AS2 id
        if (partnerListToImport != null && partnerListToImport.size() > 0) {
            PartnerAccessDB partnerAccess = new PartnerAccessDB(this.configConnection, this.runtimeConnection);
            for (Partner newPartner : partnerListToImport) {
                Partner existingPartner = partnerAccess.getPartner(newPartner.getAS2Identification());
                if (existingPartner != null) {
                    newPartner.setDBId(existingPartner.getDBId());
                    partnerAccess.updatePartner(newPartner);
                } else {
                    partnerAccess.insertPartner(newPartner);
                }
            }
        }
        if (importServerSettings) {
            this.importServerSettings(rootElement);
        }
    }

    private void importServerSettings(Element parent) {
        String[] importPrefs = new String[]{
            PreferencesAS2.ASYNC_MDN_TIMEOUT,
            PreferencesAS2.AUTH_PROXY_PASS,
            PreferencesAS2.AUTH_PROXY_USE,
            PreferencesAS2.AUTH_PROXY_USER,
            PreferencesAS2.AUTO_MSG_DELETE,
            PreferencesAS2.AUTO_MSG_DELETE_LOG,
            PreferencesAS2.AUTO_MSG_DELETE_OLDERTHAN,
            PreferencesAS2.DIR_MSG,
            PreferencesAS2.HTTP_SEND_TIMEOUT,
            PreferencesAS2.KEYSTORE_HTTPS_SEND,
            PreferencesAS2.KEYSTORE_HTTPS_SEND_PASS,
            PreferencesAS2.KEYSTORE_PASS,
            PreferencesAS2.LANGUAGE,
            PreferencesAS2.PROXY_HOST,
            PreferencesAS2.PROXY_PORT,
            PreferencesAS2.PROXY_USE,
            PreferencesAS2.RECEIPT_PARTNER_SUBDIR
        };
        List<String> prefsList = Arrays.asList(importPrefs);
        NodeList propertiesNodeList = parent.getChildNodes();
        for (int i = 0; i < propertiesNodeList.getLength(); i++) {
            if (propertiesNodeList.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element property = (Element) propertiesNodeList.item(i);
                String key = property.getTagName();
                if (prefsList.contains(key)) {
                    String value = property.getTextContent();
                    preferences.put(key, value);
                }
            }
        }
    }

    /**Parses the XML file and returns its root element*/
    private Element getRootElement(InputStream inStream) throws Exception {
        Document document = this.parseStream(inStream);
        Element rootElement = document.getDocumentElement();
        return (rootElement);
    }

    /**Reads the notificatoin data from a partner, may return null*/
    private NotificationData readNotificationData(Element parent) {
        NotificationData notification = null;
        NodeList notifcationList = parent.getElementsByTagName("notification");
        for (int i = 0; i < notifcationList.getLength(); i++) {
            Element notificationElement = (Element) notifcationList.item(i);
            notification = NotificationData.fromXML(notificationElement);
        }
        return (notification);
    }

    /**Reads all partners of the import document*/
    public List<Partner> readPartner(InputStream inStream) throws Exception {
        Element rootElement = this.getRootElement(inStream);
        List<Partner> partnerList = this.readPartner(rootElement);
        return (partnerList);
    }

    /**Reads all partners of the import document*/
    private List<Partner> readPartner(Element parent) {
        List<Partner> partnerList = new ArrayList<Partner>();
        NodeList partnerElementList = parent.getElementsByTagName("partner");
        for (int i = 0; i < partnerElementList.getLength(); i++) {
            Element partnerElement = (Element) partnerElementList.item(i);
            Partner partner = Partner.fromXML(this.certificateManager, partnerElement);
            partnerList.add(partner);
        }
        return (partnerList);
    }
}
