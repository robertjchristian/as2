//$Header: /cvsroot-fuse/mec-as2/39/mendelson/util/security/PEMKeys2PKCS12.java,v 1.1 2012/04/18 14:10:45 heller Exp $
package de.mendelson.util.security;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.Security;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.logging.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PasswordFinder;
/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * This class allows to import keys that exist in PEM encoding (human readable format),
 * e.g. created by openssl, into a PKCS#12 keystore. Please remember that this class
 * does only work with the bountycastle provider as Suns JDK has no idea of how to
 * handle PKCS#12 keystores.
 * To give you an idea of how the PEM encoded key looks like, this is a part of a key
 * -----BEGIN RSA PRIVATE KEY-----
 *Proc-Type: 4,ENCRYPTED
 *DEK-Info: DES-EDE3-CBC,6FAA019A9B61FB51
 *
 *ky5DLG4z7r2op5W/DhPTBg34RdG0eDSKUP4nRNxtHfGYQBMDQwKSYGIu0tztnwij
 *akh3DSRi+r6oZYc7oowjxFUsubZ7JMz6SYgRiDpgN3aVt4SGqqGdFuphuvVsHNhx
 *-----END RSA PRIVATE KEY-----
 *
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class PEMKeys2PKCS12 implements PasswordFinder{
    
    private Logger logger = Logger.getAnonymousLogger();
    
    private char[] keypass = null;
    
    /**Keystore to use, if this is not set a new one will be created
     */
    private KeyStore keystore = null;
    
    
    /** Creates a new instance of PEMUtil 
     *@param logger Logger to log the information to
     */
    public PEMKeys2PKCS12( Logger logger ) {
        this.logger = logger;
        //forget it to work without BC at this point, the SUN JCE provider
        //could not handle pcks12        
        Security.addProvider(new BouncyCastleProvider());
    }
    
    /**@param pemReader Reader that accesses the RSA key in PEM format
     *@param keypass Passphrase for the keys stored in the PEM file
     *@param certificateStream Stream that accesses the certificate for the keys
     *@param alias Alias to use in the new keystore
     *
     */
    public void importKey( Reader pemReader, char[] keypass,
            InputStream certificateStream, String alias ) throws Exception{
        this.keypass = keypass;
        PEMReader reader = new PEMReader(pemReader, this);
        Object readObject = reader.readObject();
        KeyPair pair = null;
        if (readObject instanceof KeyPair) {
            pair = (KeyPair)readObject;
        } else {
            this.logger.severe("--SORRY, no key pair found!.--");
            return;
        }
        X509Certificate cert = this.readCertificate( certificateStream );
        KeyStore store = this.keystore;
        if( store == null )
            store = this.generateKeyStore();
        //PKCS12 keys dont have a password
        store.setKeyEntry( alias, pair.getPrivate(),
                "dummy".toCharArray(),
                new X509Certificate[]{cert});
    }
    
    /**@param pemKeyFile File that contains the RSA key in PEM format
     *@param keypass Passphrase for the keys stored in the PEM file
     *@param certificateFile File that hold the certificate for the keys
     *@param alias Alias to use in the new keystore
     *
     */
    public void importKey( File pemKeyFile, char[] keypass,
            File certificateFile, String alias ) throws Exception{
        FileReader fileReader = new FileReader( pemKeyFile );
        InputStream certStream = new FileInputStream( certificateFile );
        this.importKey( fileReader, keypass, certStream, alias );
        fileReader.close();
        certStream.close();
    }
    
    /**@param pemData array that contains the RSA key in PEM format
     *@param keypass Passphrase for the keys stored in the PEM file
     *@param certificateData array that holds the certificate for the keys
     *@param alias Alias to use in the new keystore
     *
     */
    public void importKey( byte[] pemData, char[] keypass,
            byte[] certificateData, String alias ) throws Exception{
        Reader reader = new InputStreamReader( new ByteArrayInputStream( pemData ));
        ByteArrayInputStream certStream = new ByteArrayInputStream( certificateData );
        this.importKey( reader, keypass, certStream, alias );
        reader.close();
        certStream.close();
    }
    
    
    /**Loads ore creates a keystore to import the keys to
     */
    private KeyStore generateKeyStore() throws Exception{
        //do not remove the BC paramter, SUN cannot handle the format proper
        KeyStore generatedKeystore = KeyStore.getInstance( "PKCS12", "BC" );
        generatedKeystore.load( null, null );
        return( generatedKeystore );
    }
    
    /**Sets an already existing keystore to this class. Without an existing keystore
     *a new one is created
     */
    public void setTargetKeyStore( KeyStore keystore, char[] keystorePass ){
        this.keystore = keystore;
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
    
    /**Reads the certificate from the certificate input stream and returns
     *the certificate itself
     */
    private X509Certificate readCertificate( InputStream certificateStream ) throws Exception{
        X509Certificate cert = null;
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        cert = (X509Certificate)factory.generateCertificate(certificateStream);
        return( cert );
    }
        
    /**makes this a PasswordFinder*/
    @Override
    public char[] getPassword() {
        return this.keypass;
    }
    
        
}
