//$Header: /cvsroot-fuse/mec-as2/39/mendelson/util/security/cert/KeystoreStorageImplFile.java,v 1.1 2012/04/18 14:10:47 heller Exp $
package de.mendelson.util.security.cert;

import de.mendelson.util.MecResourceBundle;
import de.mendelson.util.security.BCCryptoHelper;
import de.mendelson.util.security.KeyStoreUtil;
import java.io.File;
import java.security.Key;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Keystore storage implementation that relies on a keystore file
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class KeystoreStorageImplFile implements KeystoreStorage {

    private KeyStore keystore = null;
    private char[] keystorePass = null;
    private String keystoreFilename = null;
    private KeyStoreUtil keystoreUtil = new KeyStoreUtil();
    private String keystoreType = BCCryptoHelper.KEYSTORE_PKCS12;
    private MecResourceBundle rb;

    /**
     * @param keystoreFilename
     * @param keystorePass
     * @param keystoreType keystore type as defined in the class BCCryptoHelper
     */
    public KeystoreStorageImplFile(String keystoreFilename, char[] keystorePass, String keystoreType) throws Exception {
        //load resource bundle
        try {
            this.rb = (MecResourceBundle) ResourceBundle.getBundle(
                    ResourceBundleKeystoreStorage.class.getName());
        } catch (MissingResourceException e) {
            throw new RuntimeException("Oops..resource bundle " + e.getClassName() + " not found.");
        }        
        this.keystoreFilename = keystoreFilename;
        this.keystorePass = keystorePass;
        this.keystoreType = keystoreType;
        BCCryptoHelper cryptoHelper = new BCCryptoHelper();
        this.keystore = cryptoHelper.createKeyStoreInstance(keystoreType);
        this.keystoreUtil.loadKeyStore(this.keystore, this.keystoreFilename, this.keystorePass);
    }


    @Override
    public void save() throws Exception {
        if (this.keystore == null) {
            //internal error, should not happen
            throw new Exception(this.rb.getResourceString("error.save.notloaded"));
        }
        this.keystoreUtil.saveKeyStore(this.keystore, this.keystorePass, this.keystoreFilename);
    }

    @Override
    public Key getKey(String alias) throws Exception {
        Key key = this.keystore.getKey(alias, this.keystorePass);
        return (key);
    }

    @Override
    public Certificate[] getCertificateChain(String alias) throws Exception {
        Certificate[] chain = this.keystore.getCertificateChain(alias);
        return (chain);
    }

    @Override
    public X509Certificate getCertificate(String alias) throws Exception {
        return ((X509Certificate) this.keystore.getCertificate(alias));
    }

    @Override
    public void renameEntry(String oldAlias, String newAlias, char[] keypairPass) throws Exception {
        KeyStoreUtil keystoreUtility = new KeyStoreUtil();
        keystoreUtility.renameEntry(this.keystore, oldAlias, newAlias, keypairPass);
    }

    @Override
    public KeyStore getKeystore() {
        return (this.keystore);
    }

    @Override
    public char[] getKeystorePass() {
        return (this.keystorePass);
    }

    @Override
    public void deleteEntry(String alias) throws Exception {
        if (this.keystore == null) {
            //internal error, should not happen
            throw new Exception(this.rb.getResourceString("error.delete.notloaded"));
        }
        this.keystore.deleteEntry(alias);
    }

    @Override
    public Map<String, Certificate> loadCertificatesFromKeystore() throws Exception {
        File keystoreFile = new File(this.keystoreFilename);
        if (!keystoreFile.canRead()) {
            throw new Exception(this.rb.getResourceString(this.rb.getResourceString("error.readaccess", this.keystoreFilename)));
        }
        if (!keystoreFile.exists()) {
            throw new Exception(this.rb.getResourceString(this.rb.getResourceString("error.filexists", this.keystoreFilename)));
        }
        if (!keystoreFile.isFile()) {
            throw new Exception(this.rb.getResourceString(this.rb.getResourceString("error.notafile", this.keystoreFilename)));
        }        
        //recreate keystore object
        this.keystoreUtil.loadKeyStore(this.keystore, this.keystoreFilename, this.keystorePass);
        Map<String, Certificate> certificateMap = this.keystoreUtil.getCertificatesFromKeystore(this.keystore);
        return (certificateMap);
    }

    @Override
    public boolean isKeyEntry(String alias) throws Exception{
        return( this.keystore.isKeyEntry(alias));
    }

    @Override
    public String getOriginalKeystoreFilename() {
        return( this.keystoreFilename);
    }

    @Override
    public boolean canWrite() {
        return( new File(this.keystoreFilename).canWrite() );
    }

    @Override
    public String getKeystoreType() {
        return( this.keystoreType);
    }
}
