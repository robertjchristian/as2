//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/cem/messages/CertificateReference.java,v 1.1 2012/04/18 14:10:22 heller Exp $
package de.mendelson.comm.as2.cem.messages;

import de.mendelson.util.security.cert.KeystoreCertificate;
import de.mendelson.util.XPathHelper;
import java.io.ByteArrayInputStream;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Represents a certificate reference structure
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class CertificateReference extends CEMStructure {

    private String issuerName = null;
    private String serialNumber = null;

    public CertificateReference() {
    }

    public void setCertficiate(KeystoreCertificate cert) {
        this.issuerName = cert.getIssuerDN();
        this.serialNumber = cert.getSerialNumberDEC();
    }

    public void setCertficiate(String issuerName, String serialNumber) {
        this.issuerName = issuerName;
        this.serialNumber = serialNumber;
    }

    /**parses the trust requests from an inbound request and returns them*/
    public static CertificateReference parse(byte[] data, String anchor) throws Exception {
        CertificateReference reference = new CertificateReference();
        ByteArrayInputStream inStream = new ByteArrayInputStream(data);
        XPathHelper helper = new XPathHelper(inStream);
        helper.addNamespace("x", "urn:ietf:params:xml:ns:ediintcertificateexchange");
        helper.addNamespace("ds", "http://www.w3.org/2000/09/xmldsig#");
        anchor = anchor + "/x:CertificateReference";
        reference.setIssuerName(helper.getValue(anchor + "/ds:X509IssuerName"));
        reference.setSerialNumber(helper.getValue(anchor + "/ds:X509SerialNumber"));
        inStream.close();
        reference.performContentCheck();
        return (reference);
    }

    /**Checks if the element contains its data*/
    private void performContentCheck() {
        if (this.issuerName == null || this.issuerName.trim().length() == 0 ) {
            throw new RuntimeException("CertificateReference: issuerName not set.");
        }
        if (this.serialNumber == null || this.serialNumber.trim().length() == 0) {
            throw new RuntimeException("CertificateReference: serialNumber not set.");
        }
    }

    /**
     * @return the issuerName
     */
    public String getIssuerName() {
        return issuerName;
    }

    /**
     * @param issuerName the issuerName to set
     */
    public void setIssuerName(String issuerName) {
        this.issuerName = issuerName;
    }

    /**
     * @return the serialNumber
     */
    public String getSerialNumber() {
        return serialNumber;
    }

    /**
     * @param serialNumber the serialNumber to set
     */
    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    @Override
    public String toXML() {
        this.performContentCheck();
        StringBuilder builder = new StringBuilder();
        builder.append("\t\t<CertificateReference>\n");
        builder.append("\t\t\t<ds:X509IssuerName>").append(this.issuerName).append("</ds:X509IssuerName>\n");
        builder.append("\t\t\t<ds:X509SerialNumber>").append(this.serialNumber).append("</ds:X509SerialNumber>\n");
        builder.append("\t\t</CertificateReference>\n");
        return (builder.toString());
    }
}
