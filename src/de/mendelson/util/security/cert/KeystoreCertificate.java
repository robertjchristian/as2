//$Header: /cvsroot-fuse/mec-as2/39/mendelson/util/security/cert/KeystoreCertificate.java,v 1.1 2012/04/18 14:10:47 heller Exp $
package de.mendelson.util.security.cert;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.cert.CertPath;
import java.security.cert.CertPathBuilder;
import java.security.cert.CertPathBuilderException;
import java.security.cert.CertPathValidator;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertPathValidatorResult;
import java.security.cert.CertStore;
import java.security.cert.CertStoreParameters;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.PKIXCertPathBuilderResult;
import java.security.cert.PKIXCertPathValidatorResult;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import javax.security.auth.x500.X500Principal;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERString;
import org.bouncycastle.asn1.x509.CRLDistPoint;
import org.bouncycastle.asn1.x509.DistributionPoint;
import org.bouncycastle.asn1.x509.DistributionPointName;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.KeyUsage;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Object that stores a single configuration certificate/key
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class KeystoreCertificate implements Comparable {

    private String alias = "";
    private X509Certificate certificate = null;
    private boolean isKeyPair = false;

    public KeystoreCertificate() {
    }

    /**Returns the extension value "extended key usage", OID 2.5.29.37
     * 
     */
    public List<String> getExtendedKeyUsage() {
        HashMap<String, String> oidMap = new HashMap<String, String>();
        oidMap.put("1.3.6.1.5.5.7.3.2", "Client authentication");
        oidMap.put("1.3.6.1.5.5.7.3.1", "Webserver authentication");
        oidMap.put("1.3.6.1.5.5.7.3.5", "IPSec end system");
        oidMap.put("1.3.6.1.5.5.7.3.6", "IPSec tunnel");
        oidMap.put("1.3.6.1.5.5.7.3.3", "Code signing");
        oidMap.put("1.3.6.1.5.5.7.3.7", "IPSec user");
        oidMap.put("1.3.6.1.5.5.7.3.4", "Email protection");
        oidMap.put("1.3.6.1.5.5.7.3.8", "Timestamping");
        oidMap.put("2.16.840.1.113733.1.8.1", "Verisign Server Gated Crypto");
        //Netscape extended key usages
        oidMap.put("2.16.840.1.113730.4.1", "Netscape Server Gated Crypto");
        oidMap.put("2.16.840.1.113730.1.2", "Netscape base URL");
        oidMap.put("2.16.840.1.113730.1.8", "Netscape CA policy URL");
        oidMap.put("2.16.840.1.113730.1.4", "Netscape CA revocation URL");
        oidMap.put("2.16.840.1.113730.1.7", "Netscape cert renewal URL");
        oidMap.put("2.16.840.1.113730.2.5", "Netscape cert sequence");
        oidMap.put("2.16.840.1.113730.1.1", "Netscape cert type");
        oidMap.put("2.16.840.1.113730.1.13", "Netscape comment");
        oidMap.put("2.16.840.1.113730.1.3", "Netscape revocation URL");
        oidMap.put("2.16.840.1.113730.1.12", "Netscape SSL server name");
        //MS extended key usages
        oidMap.put("1.3.6.1.4.1.311.10.3.3", "Microsoft Server Gated Crypto");
        oidMap.put("1.3.6.1.4.1.311.20.2.2", "Smart card logon");
        oidMap.put("1.3.6.1.4.1.311.10.3.4", "Encrypting filesystem");
        oidMap.put("1.3.6.1.4.1.311.10.3.12", "Document signing");
        oidMap.put("1.3.6.1.4.1.311.21.5", "CA encryption certificate");
        oidMap.put("1.3.6.1.4.1.311.10.3.1", "Microsoft trust list signing");
        oidMap.put("1.3.6.1.4.1.311.10.3.4.1", "File recovery");
        oidMap.put("1.3.6.1.4.1.311.10.3.11", "Key recovery");
        oidMap.put("1.3.6.1.4.1.311.10.3.10", "Qualified subordination");
        oidMap.put("1.3.6.1.4.1.311.10.3.9", "Root list signer");

        List<String> extendedKeyUsage = new ArrayList<String>();
        byte[] extensionValue = this.certificate.getExtensionValue("2.5.29.37");
        if (extensionValue == null) {
            return (extendedKeyUsage);
        }
        try {
            byte[] octedBytes = ((ASN1OctetString) ASN1Object.fromByteArray(extensionValue)).getOctets();
            ASN1Sequence asn1Sequence = (ASN1Sequence) ASN1Object.fromByteArray(octedBytes);
            for (int i = 0; i < asn1Sequence.size(); i++) {
                String oid = (asn1Sequence.getObjectAt(i).getDERObject().toString());
                if (oidMap.containsKey(oid)) {
                    extendedKeyUsage.add(oidMap.get(oid));
                } else {
                    extendedKeyUsage.add(oid);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (extendedKeyUsage);
    }

    /**Returns the key usages of this cert, OID 2.5.29.15*/
    public List<String> getKeyUsages() {
        List<String> keyUsages = new ArrayList<String>();
        byte[] extensionValue = this.certificate.getExtensionValue("2.5.29.15");
        if (extensionValue == null) {
            return (keyUsages);
        }
        try {
            byte[] octedBytes = ((ASN1OctetString) ASN1Object.fromByteArray(extensionValue)).getOctets();
            //bit encoded values for the key usage
            int val = KeyUsage.getInstance(ASN1Object.fromByteArray(octedBytes)).intValue();
            //bit 0
            if ((val & KeyUsage.digitalSignature) == KeyUsage.digitalSignature) {
                keyUsages.add("Digital signature");
            }
            //bit 1
            if ((val & KeyUsage.nonRepudiation) == KeyUsage.nonRepudiation) {
                keyUsages.add("Non repudiation");
            }
            //bit 2
            if ((val & KeyUsage.keyEncipherment) == KeyUsage.keyEncipherment) {
                keyUsages.add("Key encipherment");
            }
            //bit 3
            if ((val & KeyUsage.dataEncipherment) == KeyUsage.dataEncipherment) {
                keyUsages.add("Data encipherment");
            }
            //bit 4
            if ((val & KeyUsage.keyAgreement) == KeyUsage.keyAgreement) {
                keyUsages.add("Key agreement");
            }
            //bit 5
            if ((val & KeyUsage.keyCertSign) == KeyUsage.keyCertSign) {
                keyUsages.add("Key certificate signing");
            }
            //bit6
            if ((val & KeyUsage.cRLSign) == KeyUsage.cRLSign) {
                keyUsages.add("CRL signing");
            }
            if ((val & KeyUsage.decipherOnly) == KeyUsage.decipherOnly) {
                keyUsages.add("Decipher");
            }

            if ((val & KeyUsage.encipherOnly) == KeyUsage.encipherOnly) {
                keyUsages.add("Encipher");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return (keyUsages);
    }

    /**Returns the subject alternative name of this cert, OID 2.5.29.17*/
    public List<String> getSubjectAlternativeNames() {
        List<String> alternativeNames = new ArrayList<String>();
        byte[] extensionValue = this.certificate.getExtensionValue("2.5.29.17");
        if (extensionValue == null) {
            return (alternativeNames);
        }
        try {
            byte[] octedBytes = ((ASN1OctetString) ASN1Object.fromByteArray(extensionValue)).getOctets();
            GeneralName[] names = (GeneralNames.getInstance(ASN1Object.fromByteArray(octedBytes))).getNames();
            for (GeneralName name : names) {
                alternativeNames.add(((DERString) name.getName()).getString() + " (" + this.generalNameTagNoToString(name) + ")");
            }
        } catch (Exception e) {
            //nop
        }
        return (alternativeNames);
    }

    /**Converts the tag no of a general name to a human readable value*/
    private String generalNameTagNoToString(GeneralName name) {
        if (name.getTagNo() == GeneralName.dNSName) {
            return ("DNS name");
        }
        if (name.getTagNo() == GeneralName.directoryName) {
            return ("Directory name");
        }
        if (name.getTagNo() == GeneralName.ediPartyName) {
            return ("EDI party name");
        }
        if (name.getTagNo() == GeneralName.iPAddress) {
            return ("IP address");
        }
        if (name.getTagNo() == GeneralName.otherName) {
            return ("Other name");
        }
        if (name.getTagNo() == GeneralName.registeredID) {
            return ("Registered ID");
        }
        if (name.getTagNo() == GeneralName.rfc822Name) {
            return ("RFC822 name");
        }
        if (name.getTagNo() == GeneralName.uniformResourceIdentifier) {
            return ("URI");
        }
        if (name.getTagNo() == GeneralName.x400Address) {
            return ("x.400 address");
        }
        return ("");
    }

    /**
     * Get extension values for CRL Distribution Points as a string list or an empty list if an exception occured or
     * the extension doesnt exist
     * OID 2.5.29.31
     */
    public List<String> getCrlDistributionURLs() {
        List<String> ulrList = new ArrayList<String>();
        //CRL destribution points has OID 2.5.29.31
        byte[] extensionValue = this.certificate.getExtensionValue("2.5.29.31");
        if (extensionValue == null) {
            return (ulrList);
        }
        try {
            byte[] octedBytes = ((ASN1OctetString) ASN1Object.fromByteArray(extensionValue)).getOctets();
            CRLDistPoint distPoint = CRLDistPoint.getInstance(ASN1Object.fromByteArray(octedBytes));
            DistributionPoint[] points = distPoint.getDistributionPoints();
            for (DistributionPoint point : points) {
                DistributionPointName distributionPointName = point.getDistributionPoint();
                if (distributionPointName != null) {
                    if (distributionPointName.getType() == DistributionPointName.FULL_NAME) {
                        GeneralNames generalNames = (GeneralNames) distributionPointName.getName();
                        for (GeneralName generalName : generalNames.getNames()) {
                            //generalName.getTagNo() is GeneralName.uniformResourceIdentifier in this case
                            ulrList.add(((DERString) generalName.getName()).getString());
                        }
                    }
                }
            }
        } catch (Exception e) {
            //nop
        }
        return (ulrList);
    }

    /**Returns the enwrapped certificate version*/
    public int getVersion() {
        return (this.certificate.getVersion());
    }

    public String getSigAlgName() {
        return (this.certificate.getSigAlgName());
    }

    public String getPublicKeyAlgorithm() {
        return (this.certificate.getPublicKey().getAlgorithm());
    }

    /**Valid date start*/
    public Date getNotBefore() {
        return (this.certificate.getNotBefore());
    }

    /**Valid date end*/
    public Date getNotAfter() {
        return (this.certificate.getNotAfter());
    }

    public String getSubjectDN() {
        return (this.certificate.getSubjectDN().toString());
    }

    public String getIssuerDN() {
        return (this.certificate.getIssuerDN().toString());
    }

    /**Returns the serial number as decimal*/
    public String getSerialNumberDEC() {
        return (this.certificate.getSerialNumber().toString());
    }

    /**Returns the serial number as decimal*/
    public String getSerialNumberHEX() {
        return (this.certificate.getSerialNumber().toString(16).toUpperCase());
    }

    public void setAlias(String alias) {
        if (alias == null) {
            alias = "";
        }
        this.alias = alias;
    }

    public void setCertificate(X509Certificate certificate) {
        this.certificate = certificate;
    }

    public void setIsKeyPair(boolean isKeyPair) {
        this.isKeyPair = isKeyPair;
    }

    public X509Certificate getX509Certificate() {
        return (this.certificate);
    }

    public boolean getIsKeyPair() {
        return (this.isKeyPair);
    }

    public boolean isRootCertificate() {
        return (this.isSelfSigned() && this.certificate.getBasicConstraints() != -1);
    }

    public boolean isSelfSigned(){
        X500Principal subject = this.certificate.getSubjectX500Principal();
        X500Principal issuer = this.certificate.getIssuerX500Principal();
        return( subject.equals(issuer));
    }

    public String getAlias() {
        return (this.alias);
    }

    public int getPublicKeyLength() {
        PublicKey key = this.certificate.getPublicKey();
        if (key instanceof RSAPublicKey) {
            RSAPublicKey rsaKey = (RSAPublicKey) key;
            return (rsaKey.getModulus().bitLength());
        }
        return (0);
    }

    public byte[] getFingerPrintBytesSHA1() {
        return (this.getFingerPrintBytes("SHA1"));
    }

    public byte[] getFingerPrintBytesMD5() {
        return (this.getFingerPrintBytes("MD5"));
    }

    public String getFingerPrintSHA1() {
        return (this.getFingerPrint("SHA1"));
    }

    public String getFingerPrintMD5() {
        return (this.getFingerPrint("MD5"));
    }

    /**Deserializes a fingerprint string to a byte array
     * It is assumed that the fingerprint string has the format hex:hex:hex
     */
    public static byte[] fingerprintStrToBytes(String fingerprintStr) {
        String[] token = fingerprintStr.split(":");
        byte[] bytes = new byte[token.length];
        for (int i = 0; i < token.length; i++) {
            while( token[i].length() < 2){
                token[i] = "0" + token[i];
            }
            bytes[i] = fromHexString(token[i])[0];
        }
        return (bytes);
    }

    private static byte[] fromHexString(final String encoded) {
    if ((encoded.length() % 2) != 0)
        throw new IllegalArgumentException("Input string must contain an even number of characters");

    final byte result[] = new byte[encoded.length()/2];
    final char enc[] = encoded.toCharArray();
    for (int i = 0; i < enc.length; i += 2) {
        StringBuilder curr = new StringBuilder(2);
        curr.append(enc[i]).append(enc[i + 1]);
        result[i/2] = (byte) Integer.parseInt(curr.toString(), 16);
    }
    return result;
}


     /**Serializes a fingerprint string from a byte array to a String
     * It is assumed that the fingerprint string has the format hex:hex:hex
     */
    public static String fingerprintBytesToStr(byte[] fingerprintBytes) {
        StringBuilder fingerprint = new StringBuilder();
        for (int i = 0; i < fingerprintBytes.length; i++) {
            if (i > 0) {
                fingerprint.append(":");
            }
            String singleByte = Integer.toHexString(fingerprintBytes[i] & 0xFF).toUpperCase();
            if (singleByte.length() == 0) {
                fingerprint.append("00");
            } else if (singleByte.length() == 1) {
                fingerprint.append("0");
            }
            fingerprint.append(singleByte);
        }
        return fingerprint.toString();
    }

    /**@param digest to create the hash value, please use SHA1 or MD5 only
     *
     */
    private byte[] getFingerPrintBytes(String digest) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(digest);
            return (messageDigest.digest(this.certificate.getEncoded()));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * Returns a fingerprint string that returns the fingerprint using the format n:n:n
     * @param digest to create the hash value, please use SHA1 or MD5 only
     * 
     */
    private String getFingerPrint(String digest) {
        return( fingerprintBytesToStr(this.getFingerPrintBytes(digest)));
    }

    /**Returns the cert path for this certificate as it exists in the keystore
     * @return null if no cert path could be found
     */
    public PKIXCertPathBuilderResult getPKIXCertPathBuilderResult(KeyStore keystore, List<X509Certificate> certificateList) {
        try {
            X509CertSelector selector = new X509CertSelector();
            selector.setCertificate(this.getX509Certificate());
            boolean selected = selector.match(this.getX509Certificate());
            if (!selected) {
                return (null);
            }
            CertPathBuilder builder = CertPathBuilder.getInstance("PKIX", "BC");
            PKIXBuilderParameters pkix = new PKIXBuilderParameters(keystore, selector);
            pkix.setRevocationEnabled(false);
            CertStoreParameters params = new CollectionCertStoreParameters(certificateList);
            CertStore certstore = CertStore.getInstance("Collection", params, "BC");
            pkix.addCertStore(certstore);
            PKIXCertPathBuilderResult result = (PKIXCertPathBuilderResult) builder.build((PKIXBuilderParameters) pkix);
            return (result);
        } catch (NoSuchProviderException e) {
        } catch (KeyStoreException e) {
        } catch (NoSuchAlgorithmException e) {
        } catch (InvalidAlgorithmParameterException e) {
        } catch (CertPathBuilderException e) {
        }
        return (null);
    }

    /**Validates the certificate and returns the trust anchor certificate if the cert path is valid and the
     * full path could be validated
     * @return null if the certificate could not be trusted or an other failure like nosuchalg exception etc occurs
     */
    public X509Certificate validateCertPath(KeyStore keystore, List<X509Certificate> certificateList) {
        CertPath certPath = this.getPKIXCertPathBuilderResult(keystore, certificateList).getCertPath();
        if (certPath == null) {
            return (null);
        }
        try {
            // Validator params
            PKIXParameters params = new PKIXParameters(keystore);
            // Disable CRL checking since we are not supplying any CRLs
            params.setRevocationEnabled(false);
            //use BC here else PKCS#12 is not supported as keystore
            CertPathValidator certPathValidator = CertPathValidator.getInstance("PKIX", "BC");
            CertPathValidatorResult result = certPathValidator.validate(certPath, params);
            // Get the CA used to validate this path
            PKIXCertPathValidatorResult pkixResult = (PKIXCertPathValidatorResult) result;
            TrustAnchor ta = pkixResult.getTrustAnchor();
            X509Certificate taCert = ta.getTrustedCert();
            return (taCert);
        } catch (NoSuchProviderException e) {
        } catch (KeyStoreException e) {
        } catch (NoSuchAlgorithmException e) {
        } catch (InvalidAlgorithmParameterException e) {
        } catch (CertPathValidatorException e) {
            // Validation failed
        }
        return (null);
    }

    @Override
    public String toString() {
        return (this.alias);
    }

    @Override
    public int compareTo(Object object) {
        if (this.equals(object)) {
            return (0);
        }
        KeystoreCertificate otherCert = (KeystoreCertificate) object;
        return (this.alias.toUpperCase().compareTo(otherCert.alias.toUpperCase()));
    }

    /**Returns a string that contains information about the certificate*/
    public String getInfo() {
        DateFormat format = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT);
        StringBuilder infoText = new StringBuilder();
        infoText.append("Version: ").append(this.getVersion());
        if (this.isRootCertificate()) {
            infoText.append(" (Root certificate)");
        }
        infoText.append("\n");
        infoText.append("Subject: ").append(this.getSubjectDN()).append("\n");
        infoText.append("Issuer: ").append(this.getIssuerDN()).append("\n");
        infoText.append("Serial (dec): ").append(this.getSerialNumberDEC()).append("\n");
        infoText.append("Serial (hex): ").append(this.getSerialNumberHEX()).append("\n");
        infoText.append("Valid from: ").append(format.format(this.getNotBefore())).append("\n");
        infoText.append("Valid until: ").append(format.format(this.getNotAfter())).append("\n");
        infoText.append("Public key: ").append(this.getPublicKeyLength()).append(" ").append(this.getPublicKeyAlgorithm()).append("\n");
        infoText.append("Signature algorithm: ").append(this.getSigAlgName()).append("\n");
        try {
            infoText.append("Fingerprint (MD5): ").append(this.getFingerPrintMD5()).append("\n");
            infoText.append("Fingerprint (SHA-1): ").append(this.getFingerPrintSHA1()).append("\n");
        } catch (Exception e) {
            infoText.append("Fingerprint processing failed: ").append(e.getMessage());
        }
        return (infoText.toString());
    }

    /**Returns some information about the certificate extensions*/
    public String getInfoExtension() {
        StringBuilder extensionText = new StringBuilder();
        List<String> crl = this.getCrlDistributionURLs();
        for (int i = 0; i < crl.size(); i++) {
            extensionText.append("CRL distribution[").append(String.valueOf(i + 1)).append("]: ").append(crl.get(i)).append("\n");
        }
        List<String> alternativeNames = this.getSubjectAlternativeNames();
        if (alternativeNames.size() > 0) {
            extensionText.append("Subject alternative name: ").append(this.convertListToString(alternativeNames)).append("\n");
        }
        List<String> keyUsages = this.getKeyUsages();
        if (keyUsages.size() > 0) {
            extensionText.append("Key usage: ").append(this.convertListToString(keyUsages)).append("\n");
        }
        List<String> extkeyUsages = this.getExtendedKeyUsage();
        if (extkeyUsages.size() > 0) {
            extensionText.append("Extended key usage: ").append(this.convertListToString(extkeyUsages)).append("\n");
        }
        return (extensionText.toString());
    }

    /**Converts the arraylist content to a comma separated string*/
    private String convertListToString(Collection<String> list) {
        StringBuilder builder = new StringBuilder();
        for (String value : list) {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append(value);
        }
        return (builder.toString());
    }

    /**Overwrite the equal method of object
     *@param anObject object ot compare
     */
    @Override
    public boolean equals(Object anObject) {
        if (anObject == this) {
            return (true);
        }
        if (anObject != null && anObject instanceof KeystoreCertificate) {
            KeystoreCertificate cert = (KeystoreCertificate) anObject;
            String otherFingerPrint = null;
            String ownFingerPrint = null;
            try {
                otherFingerPrint = cert.getFingerPrintSHA1();
                ownFingerPrint = this.getFingerPrintSHA1();
                return (otherFingerPrint.equals(ownFingerPrint));
            } catch (Exception e) {
                //unable to obtain the finger print. Use the serial number and the dates.
                return (cert.getIssuerDN().equals(this.getIssuerDN())
                        && cert.getNotAfter().equals(this.getNotAfter())
                        && cert.getNotBefore().equals(this.getNotBefore()));
            }
        }
        return (false);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (this.alias != null ? this.alias.hashCode() : 0);
        hash = 97 * hash + (this.certificate != null ? this.certificate.hashCode() : 0);
        hash = 97 * hash + (this.isKeyPair ? 1 : 0);
        return hash;
    }


    /**'3D:A0:27:42:4D:92:6D:04:BB:74:66:1D:48:3E:61:6A:46:2A:05:B7'*/
//    public static final void main(String[] args) {
//        byte[] test = new byte[]{
//            (byte) 0x00, (byte) 0x3D, (byte)0x04 , (byte) 0xA0, (byte) 0x92,
//            (byte) 0x6D, (byte) 0x6D, (byte) 0x04, (byte) 0xBB, (byte) 0x74,
//            (byte) 0x66, (byte) 0x1D, (byte) 0x48, (byte) 0x3E, (byte) 0x61,
//            (byte) 0x6A, (byte) 0x46, (byte) 0x05, (byte) 0xB7, (byte) 0x42,
//        };
//        String str = KeystoreCertificate.fingerprintBytesToStr(test);
//        byte[] testbytes = KeystoreCertificate.fingerprintStrToBytes(str);
//        boolean areequal = Arrays.equals(test, testbytes);
//        System.out.println(areequal);
//    }
}
