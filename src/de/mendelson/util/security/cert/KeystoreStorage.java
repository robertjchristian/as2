//$Header: /cvsroot-fuse/mec-as2/39/mendelson/util/security/cert/KeystoreStorage.java,v 1.1 2012/04/18 14:10:47 heller Exp $
package de.mendelson.util.security.cert;

import java.security.Key;
import java.security.cert.Certificate;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.Map;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Interface for a keystore storage implementation. It should be possible to pass the
 * keystore as file, as byte array, as inputstream etc, this depends on the implementation
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public interface KeystoreStorage {

    public void save() throws Throwable;

    public Key getKey(String alias) throws Exception;

    public Certificate[] getCertificateChain(String alias) throws Exception;

    public X509Certificate getCertificate(String alias) throws Exception;

    public void renameEntry(String oldAlias, String newAlias, char[] keypairPass) throws Exception;

    public void deleteEntry(String alias) throws Exception;

    public KeyStore getKeystore();

    public char[] getKeystorePass();

    public String getKeystoreType();

    public Map<String, Certificate> loadCertificatesFromKeystore() throws Exception;

    public boolean isKeyEntry(String alias) throws Exception;

    public String getOriginalKeystoreFilename();

    public boolean canWrite();
}
