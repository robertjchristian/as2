//$Header: /cvsroot-fuse/mec-as2/39/mendelson/util/security/KeyStoreUtil.java,v 1.1 2012/04/18 14:10:45 heller Exp $
package de.mendelson.util.security;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.cert.Certificate;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.Provider;
import java.security.cert.CertPath;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.security.auth.x500.X500Principal;
import org.bouncycastle.openssl.PEMReader;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Utility class to handle java keyStore issues
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class KeyStoreUtil {

    /**Saves the passed keystore
     *@param keystorePass Password for the keystore
     *@param filename Filename where to save the keystore to
     */
    public void saveKeyStore(KeyStore keystore, char[] keystorePass, String filename) throws Exception {
        OutputStream out = null;
        try {
            out = new FileOutputStream(filename);
            keystore.store(out, keystorePass);
        } finally {
            if (out != null) {
                out.close();
            }
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
        FileInputStream inStream = null;
        try {
            if (inFile.exists()) {
                inStream = new FileInputStream(inFile);
                keystoreInstance.load(inStream, keystorePass);
            } else {
                keystoreInstance.load(null, null);
            }
        } finally {
            if (inStream != null) {
                inStream.close();
            }
        }
    }

    /**Renames an entry in the keystore
     *@param keyStore Keystore to read the keys from
     *@param oldAlias Old alias to rename
     *@param newAlias New alias to rename
     *@param keyPassword Password of the key, not used for keystores of format
     *PKCS#12, for these types of keystores just pass null.
     *
     */
    public void renameEntry(KeyStore keyStore, String oldAlias, String newAlias,
            char[] keyPassword)
            throws Exception {
        if (keyPassword == null) {
            keyPassword = "dummy".toCharArray();
        }
        //copy operation
        if (keyStore.isKeyEntry(oldAlias)) {
            Key key = keyStore.getKey(oldAlias, keyPassword);
            Certificate[] certs = keyStore.getCertificateChain(oldAlias);
            keyStore.setKeyEntry(newAlias, key, keyPassword, certs);
        } else {
            Certificate cert = keyStore.getCertificate(oldAlias);
            keyStore.setCertificateEntry(newAlias, cert);
        }
        //delete operation
        keyStore.deleteEntry(oldAlias);
    }

    /**Imports a X509 certificate into the passed keystore using a special provider
     *e.g. for the  use of BouncyCastle Provider use the code
     *Provider provBC = Security.getProvider("BC");
     *
     *@param keystore Keystore to import the certificate to
     *@param certStream Stream to access the cert data from
     *@param alias Aslias to use in the keystore
     */
    public void importX509Certificate(KeyStore keystore, InputStream certStream,
            String alias, Provider provider) throws Exception {
        X509Certificate cert = this.readCertificate(certStream, provider);
        keystore.setCertificateEntry(alias, cert);
    }

    /**Checks if the passed certificate is stored in the keystore and returns its alias. Returns
     * null if the cert is not in the keystore
     */
    public String getCertificateAlias(KeyStore keystore, X509Certificate cert) throws Exception {
        Enumeration enumeration = keystore.aliases();
        while (enumeration.hasMoreElements()) {
            String certAlias = (String) enumeration.nextElement();
            X509Certificate checkCert = this.convertToX509Certificate(keystore.getCertificate(certAlias));
            if (checkCert.getSerialNumber().equals(cert.getSerialNumber())
                    && checkCert.getNotAfter().equals(cert.getNotAfter())
                    && checkCert.getNotBefore().equals(cert.getNotBefore())) {
                return (certAlias);
            }
        }
        return (null);
    }

    /**Imports a X509 certificate into the passed keystore using a special provider
     *e.g. for the  use of BouncyCastle Provider use the code
     *Provider provBC = Security.getProvider("BC");
     *
     *@param keystore Keystore to import the certificate to
     *@param certStream Stream to access the cert data from
     *@param alias Aslias to use in the keystore
     */
    public String importX509Certificate(KeyStore keystore, X509Certificate cert, Provider provider) throws Exception {
        //dont import the certificate if it already exists!
        if (this.getCertificateAlias(keystore, cert) != null) {
            return (this.getCertificateAlias(keystore, cert));
        }
        String alias = this.getProposalCertificateAliasForImport(cert);
        alias = this.ensureUniqueAliasName(keystore, alias);
        keystore.setCertificateEntry(alias, cert);
        return (alias);
    }

    /**Checks that an alias for an import is unique in this keystore*/
    public String ensureUniqueAliasName(KeyStore keystore, String alias) throws Exception {
        int counter = 1;
        String newAlias = alias;
        //add a number to the alias if it already exists with this name
        while (keystore.containsAlias(newAlias)) {
            newAlias = alias + counter;
            counter++;
        }
        alias = newAlias;
        return (alias);
    }

    /**Checks the principal of a certificate and returns the proposed alias name
     */
    public String getProposalCertificateAliasForImport(X509Certificate cert) {
        X500Principal principal = cert.getSubjectX500Principal();
        StringTokenizer tokenizer = new StringTokenizer(principal.getName(X500Principal.RFC2253), ",");
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken().trim();
            if (token.startsWith("CN=")) {
                return (token.substring(3));
            }
        }
        //fallback: return a common name. Please check if this alias exists before importing the certificate
        return ("certificate");
    }

    /**Reads a chain of certificates from the passed stream*/
    public Collection<? extends Certificate> readCertificates(InputStream certStream, Provider provider) throws CertificateException {
        CertificateFactory factory;
        try {
            if (provider != null) {
                factory = CertificateFactory.getInstance("X.509", provider);
                return (factory.generateCertificates(certStream));
            } //Let the default provider parsing the certificate
            else {
                factory = CertificateFactory.getInstance("X.509");
                return (factory.generateCertificates(certStream));
            }
        } catch (Exception e) {
            throw new CertificateException("Not a certificate or unsupported encoding.");
        }
    }

    /**Reads a certificate from a stream and returns it
     */
    public X509Certificate readCertificate(InputStream certStream, Provider provider) throws CertificateException {
        CertificateFactory factory;
        X509Certificate cert = null;
        try {
            if (provider != null) {
                factory = CertificateFactory.getInstance("X.509", provider);
                cert = (X509Certificate) factory.generateCertificate(certStream);
            }
            //Let the default provider parsing the certificate
            if (provider == null || cert == null) {
                factory = CertificateFactory.getInstance("X.509");
                cert = (X509Certificate) factory.generateCertificate(certStream);
            }
            //still no success, perhaps PEM encoding? Start the PEM reader and see if it could read the cert
            if (cert == null) {
                PEMReader pemReader = new PEMReader(new InputStreamReader(certStream));
                cert = (X509Certificate) pemReader.readObject();
            }
        } catch (Exception e) {
            throw new CertificateException("Not a certificate or unsupported encoding.");
        }
        if (cert != null) {
            return (cert);
        } else {
            throw new CertificateException("Not a certificate or unsupported encoding.");
        }
    }

    /**Imports a X509 certificate into the passed keystore using a special provider
     *e.g. for the  use of BouncyCastle Provider use the code
     *Provider provBC = Security.getProvider("BC");
     *
     *@param keystore Keystore to import the certificate to
     *@param certificateFilename filename to read the certificate from
     *@param alias Aslias to use in the keystore
     */
    public void importX509Certificate(KeyStore keystore, String certificateFilename,
            String alias, Provider provider) throws Exception {
        InputStream inCert = new FileInputStream(certificateFilename);
        this.importX509Certificate(keystore, inCert, alias, provider);
        inCert.close();
    }

    /**Imports a X509 certificate into the passed keystore
     *@param keystore Keystore to import the certificate to
     *@param certificateFilename filename to read the certificate from
     *@param alias Aslias to use in the keystore
     */
    public void importX509Certificate(KeyStore keystore, String certificateFilename,
            String alias) throws Exception {
        InputStream inCert = new FileInputStream(certificateFilename);
        this.importX509Certificate(keystore, inCert, alias, null);
        inCert.close();
    }

    /**
     * Attempt to order the supplied array of X.509 certificates in issued to
     * to issued from order.
     * @param certs The X.509 certificates to order
     * @return The ordered X.509 certificates
     */
    public X509Certificate[] orderX509CertChain(X509Certificate[] certs) {
        int ordered = 0;
        X509Certificate[] tmpCerts = (X509Certificate[]) certs.clone();
        X509Certificate[] orderedCerts = new X509Certificate[certs.length];
        X509Certificate issuerCertificate = null;

        // Find the root issuer (ie certificate where issuer is the same
        // as subject)
        for (int i = 0; i < tmpCerts.length; i++) {
            X509Certificate singleCertificate = tmpCerts[i];
            if (singleCertificate.getIssuerDN().equals(singleCertificate.getSubjectDN())) {
                issuerCertificate = singleCertificate;
                orderedCerts[ordered] = issuerCertificate;
                ordered++;
            }
        }
        // Couldn't find a root issuer so just return the un-ordered array
        if (issuerCertificate == null) {
            return certs;
        }
        // Keep making passes through the array of certificates looking for the
        // next certificate in the chain until the links run out
        while (true) {
            boolean foundNext = false;
            for (int i = 0; i < tmpCerts.length; i++) {
                X509Certificate singleCertificate = tmpCerts[i];

                // Is this certificate the next in the chain?
                if (singleCertificate.getIssuerDN().equals(issuerCertificate.getSubjectDN()) && singleCertificate != issuerCertificate) {
                    // Yes
                    issuerCertificate = singleCertificate;
                    orderedCerts[ordered] = issuerCertificate;
                    ordered++;
                    foundNext = true;
                    break;
                }
            }
            if (!foundNext) {
                break;
            }
        }
        // Resize array
        tmpCerts = new X509Certificate[ordered];
        System.arraycopy(orderedCerts, 0, tmpCerts, 0, ordered);
        // Reverse the order of the array
        orderedCerts = new X509Certificate[ordered];
        for (int i = 0; i < ordered; i++) {
            orderedCerts[i] = tmpCerts[tmpCerts.length - 1 - i];
        }
        return orderedCerts;
    }

    /**Exports an X.509 certificate from a passed keystore, encoding is PKCS7
     *@returns the certificate
     */
    public File[] exportX509CertificatePKCS7(KeyStore keystore, String alias,
            String baseFilename) throws Exception {
        byte[] certificate = this.exportX509Certificate(keystore, alias, "PKCS7");
        File file = new File(baseFilename);
        if (certificate != null) {
            FileOutputStream outStream = new FileOutputStream(file);
            ByteArrayInputStream inStream = new ByteArrayInputStream(certificate);
            this.copyStreams(inStream, outStream);
            inStream.close();
            outStream.flush();
            outStream.close();
        }
        return (new File[]{file});
    }

    /**Converts a x.509 certificate to PEM format which is printable,
     *BASE64 encoded.
     */
    public String convertX509CertificateToPEM(X509Certificate certificate)
            throws CertificateEncodingException {
        // Get Base 64 encoding of certificate
        String fullEncoded = Base64.encode(certificate.getEncoded());

        // Certificate encodng is bounded by a header and footer
        String header = "-----BEGIN CERTIFICATE-----\n";
        String footer = "-----END CERTIFICATE-----\n";

        StringBuilder pemBuffer = new StringBuilder();
        pemBuffer.append(header);
        pemBuffer.append(fullEncoded);
        pemBuffer.append(footer);
        return (pemBuffer.toString());
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

    /**Converts an array x.509 certificate to pkcs#7 format*/
    public byte[] convertX509CertificateToPKCS7(X509Certificate[] certificates) throws Exception {
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        List<X509Certificate> certList = new ArrayList<X509Certificate>();
        certList.addAll(Arrays.asList(certificates));
        CertPath certPath = factory.generateCertPath(certList);
        return (certPath.getEncoded("PKCS7"));
    }

    /**Exports an X.509 certificate from a passed keystore, encoding is "DER", "PEM", "PKCS7"
     *@returns the certificate
     */
    public byte[] exportX509Certificate(KeyStore keystore, String alias, String encoding) throws Exception {
        if (keystore.isKeyEntry(alias)) {
            Certificate[] certificates = keystore.getCertificateChain(alias);
            X509Certificate[] x509Certificates = new X509Certificate[certificates.length];
            for (int i = 0; i < certificates.length; i++) {
                x509Certificates[i] = this.convertToX509Certificate(certificates[i]);
            }
            x509Certificates = this.orderX509CertChain(x509Certificates);
            X509Certificate singleCertificate = x509Certificates[0];
            //write certificate to file
            if (encoding.equals("DER")) {
                byte[] encoded = singleCertificate.getEncoded();
                return (encoded);
            } else if (encoding.equals("PEM")) {
                return (this.convertX509CertificateToPEM(singleCertificate).getBytes());
            } else if (encoding.equals("PKCS7")) {
                return (this.convertX509CertificateToPKCS7(x509Certificates));
            } else {
                throw new IllegalArgumentException("exportX509Certificate: Unsupported encoding " + encoding);
            }
        }
        if (keystore.isCertificateEntry(alias)) {
            Certificate certificate = keystore.getCertificate(alias);
            X509Certificate x509Certificate = this.convertToX509Certificate(certificate);
            //write certificate to file
            if (encoding.equals("DER")) {
                byte[] encoded = x509Certificate.getEncoded();
                return (encoded);
            } else if (encoding.equals("PEM")) {
                String encoded = this.convertX509CertificateToPEM(x509Certificate);
                return (encoded.getBytes());
            } else if (encoding.equals("PKCS7")) {
                return (this.convertX509CertificateToPKCS7(new X509Certificate[]{x509Certificate}));
            } else {
                throw new IllegalArgumentException("exportX509Certificate: Unsupported encoding " + encoding);
            }
        }
        return (null);
    }

    /**Copies all data from one stream to another*/
    private void copyStreams(InputStream in, OutputStream out)
            throws IOException {
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

    /**Exports an X.509 certificate from a passed keystore, encoding is ASN.1 DER
     *@returns the certificate
     */
    public File[] exportX509CertificateDER(KeyStore keystore, String alias,
            String baseFilename) throws Exception {
        byte[] certificate = this.exportX509Certificate(keystore, alias, "DER");
        File file = new File(baseFilename);
        if (certificate != null) {
            FileOutputStream outStream = new FileOutputStream(file);
            ByteArrayInputStream inStream = new ByteArrayInputStream(certificate);
            this.copyStreams(inStream, outStream);
            inStream.close();
            outStream.flush();
            outStream.close();
        }
        return (new File[]{file});
    }

    /**Exports an X.509 certificate from a passed keystore, encoding is PEM
     *@returns the certificate
     */
    public File[] exportX509CertificatePEM(KeyStore keystore, String alias,
            String baseFilename) throws Exception {
        byte[] certificate = this.exportX509Certificate(keystore, alias, "PEM");
        File file = new File(baseFilename);
        if (certificate != null) {
            FileOutputStream outStream = new FileOutputStream(file);
            ByteArrayInputStream inStream = new ByteArrayInputStream(certificate);
            this.copyStreams(inStream, outStream);
            inStream.close();
            outStream.flush();
            outStream.close();
        }
        return (new File[]{file});
    }

    /**Extracts the private key from a passed keystore and stores it in ASN.1 encoding as defined
     *in the PKCS#8 standard
     *@param keystore keystore that contains the private key
     *@param keystorePass Password for the keystore
     *@param alias Alias the keystore holds the private key with
     */
    public void extractPrivateKeyToPKCS8(KeyStore keystore, char[] keystorePass, String alias, File outFile)
            throws Exception {
        if (!keystore.isKeyEntry(alias)) {
            throw new Exception(
                    "The keystore does not contain the private key with the alias " + alias);
        }
        Key privateKey = keystore.getKey(alias, keystorePass);
        if (privateKey != null) {
            PKCS8EncodedKeySpec pkcs8 = new PKCS8EncodedKeySpec(privateKey.getEncoded());
            OutputStream os = new FileOutputStream(outFile);
            os.write(pkcs8.getEncoded());
            os.flush();
            os.close();
        }
    }

    /**Returns a map that contains all certificates of the passed keystore
     */
    public HashMap<String, Certificate> getCertificatesFromKeystore(KeyStore keystore) throws GeneralSecurityException {
        HashMap<String, Certificate> certMap = new HashMap<String, Certificate>();
        Enumeration enumeration = keystore.aliases();
        while (enumeration.hasMoreElements()) {
            String certAlias = (String) enumeration.nextElement();
            certMap.put(certAlias, keystore.getCertificate(certAlias));
        }
        return (certMap);
    }

    /**Returns a list of aliases for a specified keystore, vector of string because this may be used for GUI lists
     */
    public Vector<String> getKeyAliases(KeyStore keystore) throws KeyStoreException {
        Enumeration enumeration = keystore.aliases();
        Vector<String> keyList = new Vector<String>();
        while (enumeration.hasMoreElements()) {
            String alias = (String) enumeration.nextElement();
            if (keystore.isKeyEntry(alias)) {
                keyList.add(alias);
            }
        }
        return (keyList);
    }

    /**Returns a list of aliases for a specified keystore, vector of string because this may be used for GUI lists
     */
    public Vector<String> getNonKeyAliases(KeyStore keystore) throws KeyStoreException {
        Enumeration enumeration = keystore.aliases();
        Vector<String> nonkeyList = new Vector<String>();
        while (enumeration.hasMoreElements()) {
            String alias = (String) enumeration.nextElement();
            if (!keystore.isKeyEntry(alias)) {
                nonkeyList.add(alias);
            }
        }
        return (nonkeyList);
    }
}
