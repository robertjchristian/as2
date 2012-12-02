//$Header: /cvsroot-fuse/mec-as2/39/mendelson/util/security/cert/CertificateManager.java,v 1.1 2012/04/18 14:10:47 heller Exp $
package de.mendelson.util.security.cert;

import de.mendelson.util.MecResourceBundle;
import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.Iterator;
import java.util.logging.Logger;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import javax.security.auth.x500.X500Principal;
/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */

/**
 * Helper class to store
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class CertificateManager {

    private Logger logger = null;
    private final List<KeystoreCertificate> keyStoreCertificateList = Collections.synchronizedList(new ArrayList<KeystoreCertificate>());
    private MecResourceBundle rb = null;
    private KeystoreStorage storage = null;

    public CertificateManager(Logger logger) {
        this.logger = logger;
        //load resource bundle
        try {
            this.rb = (MecResourceBundle) ResourceBundle.getBundle(
                    ResourceBundleCertificateManager.class.getName());
        } catch (MissingResourceException e) {
            throw new RuntimeException("Oops..resource bundle " + e.getClassName() + " not found.");
        }
    }

    public CertificateManager() {
        this(null);
    }

    /**Returns the cert alias that is assigned to the cert/key of the passed fingerprint (SHA1)*/
    public String getAliasByFingerprint(byte[] fingerprintSHA1) {
        KeystoreCertificate cert = this.getKeystoreCertificateByFingerprintSHA1(fingerprintSHA1);
        //fingerprint not found
        if (cert == null) {
            return (null);
        } else {
            return (cert.getAlias());
        }
    }

    /**Returns the cert alias that is assigned to the cert/key of the passed fingerprint (SHA1)*/
    public String getAliasByFingerprint(String fingerprintSHA1) {
        KeystoreCertificate cert = this.getKeystoreCertificateByFingerprintSHA1(fingerprintSHA1);
        //fingerprint not found
        if (cert == null) {
            return (null);
        } else {
            return (cert.getAlias());
        }
    }

    /**Returns the certificate chain for a special alias
     */
    public Certificate[] getCertificateChain(String alias) throws Exception {
        Certificate[] chain = this.storage.getCertificateChain(alias);
        return (chain);
    }

    /**Returns the X509 certificate assigned to the passed alias
     */
    public X509Certificate getX509Certificate(String alias) throws Exception {
        X509Certificate certificate = this.storage.getCertificate(alias);
        if (certificate == null) {
            throw new Exception(this.rb.getResourceString("alias.notfound", alias));
        }
        return (certificate);
    }

    /**Returns the list of available X509 certificates
     */
    public List<X509Certificate> getX509CertificateList() {
        List<X509Certificate> certList = new ArrayList<X509Certificate>();
        synchronized (this.keyStoreCertificateList) {
            for (KeystoreCertificate cert : this.keyStoreCertificateList) {
                certList.add(cert.getX509Certificate());
            }
        }
        return (certList);
    }

    /**Returns the private key for an alias. If the assigned certificate
     *does not contain a private key an exception is thrown
     */
    public PrivateKey getPrivateKey(String alias) throws Exception {
        PrivateKey key = (PrivateKey) this.storage.getKey(alias);
        if (key == null) {
            throw new Exception(this.rb.getResourceString("alias.hasno.privatekey", alias));
        } else {
            return (key);
        }
    }

    /**Returns the public key for an alias.
     */
    public PublicKey getPublicKey(String alias) throws Exception {
        return (this.storage.getCertificate(alias).getPublicKey());
    }

    /**Returns the private key for a passed fingerprint (SHA1). If the assigned certificate
     *does not contain a private key an exception is thrown
     */
    public PrivateKey getPrivateKeyByFingerprintSHA1(byte[] fingerprintStrSHA1) throws Exception {
        //this will always return the private key if there is a public and a private key entry with the same serial in
        //the keystore
        KeystoreCertificate certificate = this.getKeystoreCertificateByFingerprintSHA1(fingerprintStrSHA1);
        return (this.getPrivateKey(certificate.getAlias()));
    }

    /**Returns the private key for a passed fingerprint (SHA1). If the assigned certificate
     *does not contain a private key an exception is thrown
     */
    public PrivateKey getPrivateKeyByFingerprintSHA1(String fingerprintStrSHA1) throws Exception {
        //this will always return the private key if there is a public and a private key entry with the same serial in
        //the keystore
        KeystoreCertificate certificate = this.getKeystoreCertificateByFingerprintSHA1(fingerprintStrSHA1);
        return (this.getPrivateKey(certificate.getAlias()));
    }

    /**Returns the public key or the private key for an alias.
     */
    public Key getKey(String alias) throws Exception {
        Key key = this.storage.getKey(alias);
        if (key == null) {
            throw new Exception(this.rb.getResourceString("alias.hasno.key", alias));
        } else {
            return (key);
        }
    }

    /**Stores the manages keystore
     */
    public void saveKeystore() throws Throwable {
        this.storage.save();
        //refresh the cert list
        this.rereadKeystoreCertificates();
    }

    /**Deletes an entry from the actual keystore*/
    public void deleteKeystoreEntry(String alias) throws Throwable {
        this.storage.deleteEntry(alias);
    }

    /**Renames an entry in the underlaying keystore. Please remember that PKCS#12 contains no
     * key pair password, pass null in this case
     * 
     */
    public void renameAlias(String oldAlias, String newAlias, char[] keypairPass) throws Exception {
        this.storage.renameEntry(oldAlias, newAlias, keypairPass);
        //rename alias in cert list
        synchronized (this.keyStoreCertificateList) {
            for (KeystoreCertificate cert : this.keyStoreCertificateList) {
                if (cert.getAlias().equals(oldAlias)) {
                    cert.setAlias(newAlias);
                    break;
                }
            }
        }
    }

    /**Refreshes the data
     */
    public void rereadKeystoreCertificates() throws Exception {
        synchronized (this.keyStoreCertificateList) {
            Map<String, Certificate> newCertificateMap = this.storage.loadCertificatesFromKeystore();
            this.keyStoreCertificateList.clear();
            Iterator<String> iterator = newCertificateMap.keySet().iterator();
            while (iterator.hasNext()) {
                KeystoreCertificate certificate = new KeystoreCertificate();
                String alias = iterator.next();
                certificate.setAlias(alias);
                X509Certificate foundCertificate = (X509Certificate) newCertificateMap.get(alias);
                certificate.setCertificate(foundCertificate);
                try {
                    certificate.setIsKeyPair(getKeystore().isKeyEntry(alias));
                } catch (Exception e) {
                    //no problem, thats what we wanted to know
                    certificate.setIsKeyPair(false);
                    if (this.logger != null) {
                        this.logger.warning(e.getMessage());
                    }
                }
                this.keyStoreCertificateList.add(certificate);
            }
        }
    }

    /**Called from external if the certificate storage has been changed. This method
     *calls the normal rereadkeystore method but logs the step
     */
    public void rereadKeystoreCertificatesLogged() {
        try {
            this.rereadKeystoreCertificates();
            if (this.logger != null) {
                this.logger.fine(this.rb.getResourceString("keystore.reloaded"));
            }
        } catch (Exception e) {
            if (this.logger != null) {
                this.rb.getResourceString("keystore.read.failure",
                        new Object[]{e.getMessage()});
            }
        }
    }

    /**Reads the certificates of the actual key store
     * @param type keystore typ as defined in the class BCCryptoHelper
     */
    public void loadKeystoreCertificates(KeystoreStorage storage) {
        this.storage = storage;
        try {
            this.rereadKeystoreCertificates();
        } catch (Exception e) {
            if (this.logger != null) {
                this.rb.getResourceString("keystore.read.failure",
                        new Object[]{e.getMessage()});
            }
        }
    }

    /**returns null if the alias does not exist*/
    public KeystoreCertificate getKeystoreCertificate(String alias) {
        synchronized (this.keyStoreCertificateList) {
            for (KeystoreCertificate cert : this.keyStoreCertificateList) {
                if (cert.getAlias().equalsIgnoreCase(alias)) {
                    return (cert);
                }
            }
        }
        return (null);
    }

    /**returns null if a certificate with the issuerDN and the serial does not exist*/
    public KeystoreCertificate getKeystoreCertificateByIssuerAndSerial(String issuerDN, String serial) {
        //it could happen that a cert and a key with the same fingerprint are in the keystore.
        //Always return the key in this case.
        KeystoreCertificate foundCert = null;
        synchronized (this.keyStoreCertificateList) {
            for (KeystoreCertificate cert : this.keyStoreCertificateList) {
                if (cert.getSerialNumberDEC().equals(serial) && cert.getIssuerDN().equals(issuerDN)) {
                    //no entry found so far: always store the found one
                    if (foundCert == null) {
                        foundCert = cert;
                    } else {
                        //entry already found: overwrite it only if the found entry is a key
                        if (cert.getIsKeyPair()) {
                            foundCert = cert;
                        }
                    }
                }
            }
        }
        return (foundCert);
    }

    /**returns null if a certificate with the issuerDN and the serial does not exist*/
    public KeystoreCertificate getKeystoreCertificateByIssuerAndSerial(X500Principal issuer, String serialDEC) {
        //it could happen that a cert and a key with the same fingerprint are in the keystore.
        //Always return the key in this case.
        KeystoreCertificate foundCert = null;
        synchronized (this.keyStoreCertificateList) {
            for (KeystoreCertificate cert : this.keyStoreCertificateList) {
                if (cert.getSerialNumberDEC().equals(serialDEC) && cert.getX509Certificate().getIssuerX500Principal().equals(issuer)) {
                    //no entry found so far: always store the found one
                    if (foundCert == null) {
                        foundCert = cert;
                    } else {
                        //entry already found: overwrite it only if the found entry is a key
                        if (cert.getIsKeyPair()) {
                            foundCert = cert;
                        }
                    }
                }
            }
        }
        return (foundCert);
    }

    /**Throws an exception if the requested certificate does not exist in the keystore*/
    public KeystoreCertificate getKeystoreCertificateByFingerprintSHA1NonNull(String fingerprintSHA1) throws Exception {
        KeystoreCertificate certificate = this.getKeystoreCertificateByFingerprintSHA1(fingerprintSHA1);
        if (certificate == null) {
            throw new Exception(this.rb.getResourceString("certificate.not.found.fingerprint", fingerprintSHA1));
        } else {
            return (certificate);
        }
    }

    /**Throws an exception if the requested certificate does not exist in the keystore*/
    public KeystoreCertificate getKeystoreCertificateByFingerprintSHA1NonNull(byte[] fingerprintSHA1) throws Exception {
        KeystoreCertificate certificate = this.getKeystoreCertificateByFingerprintSHA1(fingerprintSHA1);
        if (certificate == null) {
            throw new Exception(this.rb.getResourceString("certificate.not.found.fingerprint",
                    KeystoreCertificate.fingerprintBytesToStr(fingerprintSHA1)));
        } else {
            return (certificate);
        }
    }

    /**returns null if the fingerprint does not exist*/
    public KeystoreCertificate getKeystoreCertificateByFingerprintSHA1(byte[] fingerprintSHA1) {
        //if there is a prio in the partners certificates, e.g. for AS2/CEM it could happen that
        //a null request happends here. sample: A decryption request fails, the program tries
        //a fallback with a second certificate (prio 2) ..but this one is not set --> a null is passed
        if (fingerprintSHA1 == null) {
            return (null);
        }
        //it could happen that a cert and a key with the same fingerprint are in the keystore.
        //Always return the key in this case.
        KeystoreCertificate foundCert = null;
        synchronized (this.keyStoreCertificateList) {
            for (KeystoreCertificate cert : this.keyStoreCertificateList) {
                if (Arrays.equals(fingerprintSHA1, cert.getFingerPrintBytesSHA1())) {
                    //no entry found so far: always store the found one
                    if (foundCert == null) {
                        foundCert = cert;
                    } else {
                        //entry already found: overwrite it only if the found entry is a key
                        if (cert.getIsKeyPair()) {
                            foundCert = cert;
                        }
                    }
                }
            }
        }
        return (foundCert);
    }

    /**returns null if the fingerprint does not exist*/
    public KeystoreCertificate getKeystoreCertificateByFingerprintSHA1(String fingerprintSHA1) {
        //if there is a prio in the partners certificates, e.g. for AS2/CEM it could happen that
        //a null request happends here. sample: A decryption request fails, the program tries
        //a fallback with a second certificate (prio 2) ..but this one is not set --> a null is passed
        if (fingerprintSHA1 == null) {
            return (null);
        }
        byte[] fingerprintSHA1Bytes = KeystoreCertificate.fingerprintStrToBytes(fingerprintSHA1);
        return (this.getKeystoreCertificateByFingerprintSHA1(fingerprintSHA1Bytes));
    }

    /**Returns the list of certificates*/
    public List<KeystoreCertificate> getKeyStoreCertificateList() {
        synchronized (this.keyStoreCertificateList) {
            return (this.keyStoreCertificateList);
        }
    }

    /**Passes a logger to the certificate manager. There will be no logging if no logger has been passed*/
    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public char[] getKeystorePass() {
        return this.storage.getKeystorePass();
    }

    public KeyStore getKeystore() {
        return (this.storage.getKeystore());
    }

    /**
     * Returns a map with issuer as key and the available certs as value
     */
    public Map<X500Principal, List<X509Certificate>> getIssuerCertificateMap() throws Exception {
        Map<X500Principal, List<X509Certificate>> map = new HashMap<X500Principal, List<X509Certificate>>();
        synchronized (this.keyStoreCertificateList) {
            List<KeystoreCertificate> certList = this.getKeyStoreCertificateList();
            for (KeystoreCertificate keystoreCertificate : certList) {
                X509Certificate foundCert = (X509Certificate) keystoreCertificate.getX509Certificate();
                if (foundCert != null) {
                    X500Principal subjectDN = foundCert.getSubjectX500Principal();
                    List<X509Certificate> foundCertList = map.get(subjectDN);
                    if (foundCertList == null) {
                        foundCertList = new ArrayList<X509Certificate>();
                        foundCertList.add(foundCert);
                    } else {
                        if (!foundCertList.contains(foundCert)) {
                            foundCertList.add(foundCert);
                        }
                    }
                    map.put(subjectDN, foundCertList);
                }
            }
            return map;
        }
    }

    public void setKeyEntry(String alias, Key key, Certificate[] chain) throws Throwable {
        KeyStore keystore = this.getKeystore();
        keystore.setKeyEntry(alias, key, this.getKeystorePass(), chain);
        this.saveKeystore();
    }
}
