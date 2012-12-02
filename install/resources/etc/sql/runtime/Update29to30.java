//$Header: /as2/sqlscript/runtime/Update29to30.java 2     11.11.11 12:14 Heller $
package sqlscript.runtime;

import de.mendelson.comm.as2.database.IUpdater;
import de.mendelson.comm.as2.preferences.PreferencesAS2;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.cert.Certificate;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Enumeration;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 *
 * Update the database from version 29 to version 30
 * @author S.Heller
 * @version $Revision: 2 $
 * @since build 128
 */
public class Update29to30 implements IUpdater {

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
        Statement statement = connection.createStatement();
        statement.execute("ALTER TABLE certificates ADD COLUMN fingerprintsha1 VARCHAR(255)");
        ResultSet result = statement.executeQuery("SELECT * FROM certificates");
        while (result.next()) {
            int partnerId = result.getInt("partnerid");
            int category = result.getInt("category");
            int prio = result.getInt("prio");
            String serial = result.getString("serialid");
            this.updateCertEntry(connection, partnerId, category, prio, serial);
        }
        result.close();
        statement.execute("ALTER TABLE certificates DROP COLUMN serialid");
        statement.close();
        this.success = true;
    }

    /**Inserts a new entry into the certificate table*/
    private void updateCertEntry(Connection connection, int partnerId, int category, int prio, String serial) {
        try {
            KeyStore keystore = KeyStore.getInstance("PKCS12", "BC");
            PreferencesAS2 preferences = new PreferencesAS2();
            char[] keystorePass = preferences.get(PreferencesAS2.KEYSTORE_PASS).toCharArray();
            this.loadKeyStore(keystore, "certificates.p12", keystorePass);
            X509Certificate cert = this.getCertificateBySerial(keystore, serial);
            if (cert == null) {
                System.out.println("WARNING: Certificate with serial \"" + serial + "\" NOT found in the underlaying keystore. Please visit the partner settings for the partner \"" + partnerId + "\".");
            } else {
                PreparedStatement statement = connection.prepareStatement(
                        "UPDATE certificates SET fingerprintsha1=? WHERE partnerid=? AND category=? AND prio=? AND serialid=?");
                statement.setString(1, this.getFingerPrintSHA1(cert));
                statement.setInt(2, partnerId);
                statement.setInt(3, category);
                statement.setInt(4, prio);
                statement.setString(5, serial);
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
    private void loadKeyStore(KeyStore keystoreInstance,
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
    private X509Certificate convertToX509Certificate(Certificate certificate)
            throws CertificateException, IOException {
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        ByteArrayInputStream inStream =
                new ByteArrayInputStream(certificate.getEncoded());
        X509Certificate cert = (X509Certificate) factory.generateCertificate(inStream);
        inStream.close();
        return (cert);
    }

    private X509Certificate getCertificateBySerial(KeyStore keystore, String serial) throws Exception {
        Enumeration<String> enumeration = keystore.aliases();
        while (enumeration.hasMoreElements()) {
            String alias = enumeration.nextElement();
            X509Certificate testCert = this.convertToX509Certificate(keystore.getCertificate(alias));
            if (testCert.getSerialNumber().toString().equals(serial)) {
                return (testCert);
            }
        }
        return (null);
    }

    /**@param digest to create the hash value, please use SHA1 or MD5 only
     * 
     */
    private String getFingerPrintSHA1(X509Certificate certificate) throws Exception {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA1");
        byte[] bytes = messageDigest.digest(certificate.getEncoded());
        StringBuilder fingerprint = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            if (i > 0) {
                fingerprint.append(":");
            }
            String singleByte = Integer.toHexString(bytes[i] & 0xFF).toUpperCase();
            if (singleByte.length() == 0) {
                fingerprint.append("00");
            } else if (singleByte.length() == 1) {
                fingerprint.append("0");
            }
            fingerprint.append(singleByte);
        }
        return fingerprint.toString();
    }
}
