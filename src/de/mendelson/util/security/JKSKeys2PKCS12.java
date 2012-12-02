//$Header: /cvsroot-fuse/mec-as2/39/mendelson/util/security/JKSKeys2PKCS12.java,v 1.1 2012/04/18 14:10:45 heller Exp $
package de.mendelson.util.security;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.interfaces.RSAPrivateCrtKey;
import java.util.logging.Logger;
/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Allows the conversion of a private key that is stored in a sun keystore
 * to the pkcs#12 format
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class JKSKeys2PKCS12{
    
    private Logger logger = null;
    
    private KeyStore targetKeyStore = null;    
    
    public JKSKeys2PKCS12( Logger logger ){
        this.logger = logger;
    }
    
    /**@param jksKeyStore JKS Keystore that contains the private key
     *@param jksKeyPassword JKS Key pass
     *@param alias alias of the key, used in both import and exported keystore
     */
    public void exportKey( KeyStore jksKeyStore, char[] jksKeyPassword, String alias ) throws Exception{
        //extract key
        RSAPrivateCrtKey jksPrivateCrtKey = (RSAPrivateCrtKey)jksKeyStore.getKey(alias, jksKeyPassword);
        Certificate jksCert = jksKeyStore.getCertificate(alias);
        //Get Certificate Chain
        Certificate[] jksCerts = jksKeyStore.getCertificateChain(alias);
        if(jksPrivateCrtKey == null || jksCerts == null) {
            this.logger.severe("I didn't find a private key entry with the alias \"" + alias + "\" in the JKS keystore");
            return;
        }
        KeyStore pkcs12Keystore = this.targetKeyStore;
            if( pkcs12Keystore == null ){
                pkcs12Keystore = this.generatePKCS12KeyStore();
            }
        //pkcs12 has no key password
        pkcs12Keystore.setKeyEntry(alias, jksPrivateCrtKey, "dummy".toCharArray(), jksCerts);
    }
    
        
    public void setTargetKeyStore( KeyStore keystore){
        this.targetKeyStore = keystore;
    }
    
    /**Loads ore creates a keystore to import the keys to
     */
    private KeyStore generatePKCS12KeyStore() throws Exception{
        //do not remove the BC paramter, SUN cannot handle the format proper
        KeyStore keystore = KeyStore.getInstance( BCCryptoHelper.KEYSTORE_PKCS12, "BC" );
        keystore.load( null, null );
        return( keystore );
    }
    
    /**Saves the passed keystore
     *@param keystorePass Password for the keystore
     *@param filename Filename where to save the keystore to
     */
    public void saveKeyStore( KeyStore keystore, char[] keystorePass,
            File file )throws Exception{
        OutputStream out = new FileOutputStream(file);
        keystore.store(out,keystorePass);
        out.close();
    }
}
