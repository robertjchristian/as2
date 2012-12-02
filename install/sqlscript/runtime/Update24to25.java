//$Header: /as2/sqlscript/runtime/Update24to25.java 2     11.11.11 12:14 Heller $
package sqlscript.runtime;

import de.mendelson.comm.as2.database.IUpdater;
import de.mendelson.comm.as2.preferences.PreferencesAS2;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 *
 * Update the database from version 24 to version 25
 * @author S.Heller
 * @version $Revision: 2 $
 * @since build 128
 */
public class Update24to25 implements IUpdater {

    private final int CATEGORY_CRYPT = 1;
    private final int CATEGORY_SIGN = 2;
    private final int CATEGORY_SSL = 3;
    /**Store if this was a successfully operation*/
    private boolean success = false;

    /** Return if the update was successfully */
    @Override
    public boolean updateWasSuccessfully() {
        return (this.success);
    }

    /** Starts the update process */
    @Override
    public void startUpdate(Connection connection) throws Exception {
        //transfer the keys to the new certificate table
        //create the new table
        Statement statement = connection.createStatement();
        statement.execute("CREATE TABLE certificates(id INTEGER IDENTITY PRIMARY KEY,partnerid INTEGER,serialid VARCHAR(255),prio INT DEFAULT 0 NOT NULL,category INTEGER,FOREIGN KEY(partnerid)REFERENCES partner(id))");
        ResultSet result = statement.executeQuery("SELECT * FROM partner");
        while (result.next()) {
            String alias = result.getString("cryptalias");
            this.insertCertEntry(connection, result.getInt("id"), alias, CATEGORY_CRYPT);
            alias = result.getString("signalias");
            this.insertCertEntry(connection, result.getInt("id"), alias, CATEGORY_SIGN);
        }
        result.close();
        //modify the partner table
        statement.execute("ALTER TABLE partner DROP COLUMN cryptalias");
        statement.execute("ALTER TABLE partner DROP COLUMN signalias");
        //modify the message table, now there are the message types CEM and standard AS2 message
        statement.execute("ALTER TABLE messages ADD COLUMN messagetype INT DEFAULT 1 NOT NULL");
        //modify the payload table, store content type and content id of the payloads
        statement.execute("ALTER TABLE payload ADD COLUMN contentid VARCHAR(255)");
        statement.execute("ALTER TABLE payload ADD COLUMN contenttype VARCHAR(255)");
        //create the cem table
        statement.execute("CREATE TABLE cem(id INTEGER IDENTITY PRIMARY KEY,initiatoras2id VARCHAR(255),receiveras2id VARCHAR(255),requestid VARCHAR(255),requestmessageid VARCHAR(255),responsemessageid VARCHAR(255),respondbydate BIGINT,requestmessageoriginated BIGINT,responsemessageoriginated BIGINT,category INTEGER,cemstate INTEGER,serialid VARCHAR(255),issuername VARCHAR(255),processed INT DEFAULT 0 NOT NULL,processdate BIGINT,reasonforrejection OBJECT)");
        //add a cem notification possibility
        statement.execute("ALTER TABLE notification ADD COLUMN notifycem INTEGER DEFAULT 0 NOT NULL");
        statement.execute("ALTER TABLE notification ADD COLUMN usesmtpauth INTEGER DEFAULT 0 NOT NULL");
        statement.execute("ALTER TABLE notification ADD COLUMN smtpauthuser VARCHAR(255)");
        statement.execute("ALTER TABLE notification ADD COLUMN smtpauthpass VARCHAR(255)");
        //add partner info table
        statement.execute("CREATE TABLE partnersystem(id INTEGER IDENTITY PRIMARY KEY,partnerid INTEGER,as2version VARCHAR(10),productname VARCHAR(255),compression INTEGER DEFAULT 0 NOT NULL,ma INTEGER DEFAULT 0 NOT NULL,cem INTEGER DEFAULT 0 NOT NULL,FOREIGN KEY(partnerid)REFERENCES partner(id))");
        statement.close();
        this.success = true;
    }

    /**Inserts a new entry into the certificate table*/
    private void insertCertEntry(Connection connection, int partnerId, String alias, int category) {
        try {
            KeyStore keystore = KeyStore.getInstance("PKCS12", "BC");
            PreferencesAS2 preferences = new PreferencesAS2();
            char[] keystorePass = preferences.get(PreferencesAS2.KEYSTORE_PASS).toCharArray();
            this.loadKeyStore(keystore, "certificates.p12", keystorePass);
            Certificate cert = keystore.getCertificate(alias);
            if (cert == null) {
                System.out.println("WARNING: Certificate with alias \"" + alias + "\" NOT found in the underlaying keystore. Please visit the partner settings for the partner \"" + partnerId + "\".");
            } else {
                X509Certificate certificate = this.convertToX509Certificate(cert);
                String serial = certificate.getSerialNumber().toString();
                PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO certificates(partnerid,serialid,prio,category)VALUES(?,?,?,?)");
                statement.setInt(1, partnerId);
                statement.setString(2, serial);
                statement.setInt(3, 1);
                statement.setInt(4, category);
                statement.execute();
                statement.close();
            }
        } catch (Exception e) {
            System.out.println("WARNING: " + e.getMessage());
        }
    }

    /**Loads a keystore and returns it. The passed keystore has to be created
     *first by the security provider, e.g. using the code
     *KeyStore.getInstance(<keystoretype>, <provider>);
     *If the passed filename does not exist a new, empty keystore will be created
     */
    public void loadKeyStore(KeyStore keystoreInstance,
            String filename, char[] keystorePass) throws Exception {
        File inFile = new File(filename);
        if (inFile.exists()) {
            FileInputStream inStream = new FileInputStream(inFile);
            keystoreInstance.load(inStream, keystorePass);
            inStream.close();
        } else {
            keystoreInstance.load(null, null);
        }
    }

    /**Converts the passed certificate to an X509 certificate. Mainly it is already
     *in this format.
     */
    public final X509Certificate convertToX509Certificate(Certificate certificate)
            throws CertificateException, IOException {
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        ByteArrayInputStream inStream =
                new ByteArrayInputStream(certificate.getEncoded());
        X509Certificate cert = (X509Certificate) factory.generateCertificate(inStream);
        inStream.close();
        return (cert);
    }
}


