//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/cem/messages/EndEntity.java,v 1.1 2012/04/18 14:10:22 heller Exp $
package de.mendelson.comm.as2.cem.messages;

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
 * Represents a certificate identifier structure
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class EndEntity extends CEMStructure{

    private String issuerName = null;
    private String serialNumber = null;
    private String contentId = null;

    public EndEntity() {
    }

    /**parses the trust requests from an inbound request and returns them*/
    public static EndEntity parse(byte[] data, String anchor) throws Exception {
        EndEntity entity = new EndEntity();
        ByteArrayInputStream inStream = new ByteArrayInputStream(data);
        XPathHelper helper = new XPathHelper(inStream);
        helper.addNamespace("x", "urn:ietf:params:xml:ns:ediintcertificateexchange");
        helper.addNamespace("ds", "http://www.w3.org/2000/09/xmldsig#");
        anchor = anchor + "/x:EndEntity";
        String identifierAnchor = anchor + "/x:CertificateIdentifier";
        entity.setIssuerName(helper.getValue(identifierAnchor + "/ds:X509IssuerName"));
        entity.setSerialNumber(helper.getValue(identifierAnchor + "/ds:X509SerialNumber"));
        entity.setContentId(helper.getValue(anchor + "/x:CertificateContentID"));
        inStream.close();
        return (entity);
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

    /**
     * @return the contentId
     */
    public String getContentId() {
        return contentId;
    }

    /**
     * @param contentId the contentId to set
     */
    public void setContentId(String contentId) {
        this.contentId = contentId;
    }

    @Override
    public String toXML() {
        if( this.issuerName == null ){
            throw new RuntimeException( "EndEntity: issuerName not set.");
        }
        if( this.serialNumber == null ){
            throw new RuntimeException( "EndEntity: serialNumber not set.");
        }
        if( this.contentId == null ){
            throw new RuntimeException( "EndEntity: contentId not set.");
        }
        StringBuilder builder = new StringBuilder();
        builder.append( "\t\t<EndEntity>\n" );
        builder.append( "\t\t\t<CertificateIdentifier>\n" );
        builder.append( "\t\t\t\t<ds:X509IssuerName>" ).append( this.issuerName ).append( "</ds:X509IssuerName>\n" );
        builder.append( "\t\t\t\t<ds:X509SerialNumber>" ).append( this.serialNumber ).append("</ds:X509SerialNumber>\n" );
        builder.append( "\t\t\t</CertificateIdentifier>\n" );
        builder.append( "\t\t\t<CertificateContentID>" ).append( this.contentId ).append( "</CertificateContentID>\n" );
        builder.append( "\t\t</EndEntity>\n" );
        return( builder.toString());
    }
}
