//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/cem/messages/TrustRequest.java,v 1.1 2012/04/18 14:10:22 heller Exp $
package de.mendelson.comm.as2.cem.messages;

import de.mendelson.util.XPathHelper;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Represents a trust request structure
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class TrustRequest extends CEMStructure {

    private boolean certUsageEncryption = false;
    private boolean certUsageSignature = false;
    private boolean certUsageSSL = false;
    private Date respondByDate = null;
    private String responseURL = null;
    private EndEntity endEntity = null;

    public TrustRequest() {
    }

    /**Returns this request as XML*/
    @Override
    public String toXML() {
        if (this.endEntity == null) {
            throw new RuntimeException("TrustRequest: endEntity not set.");
        }
        if (this.responseURL == null) {
            throw new RuntimeException("TrustRequest: responseURL not set.");
        }
        StringBuilder builder = new StringBuilder();
        builder.append("\t<TrustRequest>\n");
        if (this.certUsageEncryption) {
            builder.append("\t\t<CertUsage>");
            builder.append("keyEncipherment");
            builder.append("</CertUsage>\n");
        }
        if (this.certUsageSSL) {
            builder.append("\t\t<CertUsage>");
            builder.append("tlsServer");
            builder.append("</CertUsage>\n");
        }
        if (this.certUsageSignature) {
            builder.append("\t\t<CertUsage>");
            builder.append("digitalSignature");
            builder.append("</CertUsage>\n");
        }
        if( this.respondByDate != null ){
            builder.append("\t\t<RespondByDate>");
            builder.append(this.toXMLDate(this.respondByDate));
            builder.append("</RespondByDate>\n");
        }
        builder.append("\t\t<ResponseURL>");
        builder.append(this.responseURL);
        builder.append("</ResponseURL>\n");
        builder.append(this.endEntity.toXML());
        builder.append("\t</TrustRequest>\n");
        return (builder.toString());
    }

    /**parses the trust requests from an inbound request and returns them*/
    public static List<TrustRequest> parse(byte[] data) throws Exception {
        List<TrustRequest> requestList = new ArrayList<TrustRequest>();
        ByteArrayInputStream inStream = new ByteArrayInputStream(data);
        XPathHelper helper = new XPathHelper(inStream);
        helper.addNamespace("x", "urn:ietf:params:xml:ns:ediintcertificateexchange");
        helper.addNamespace("ds", "http://www.w3.org/2000/09/xmldsig#");
        int requestCount = helper.getNodeCount("//x:TrustRequest");
        for (int i = 0; i < requestCount; i++) {
            TrustRequest request = new TrustRequest();
            String anchor = "//x:TrustRequest[" + (i + 1) + "]";
            request.setResponseURL(helper.getValue(anchor + "/x:ResponseURL"));
            //respond by date is conditional for encryption and signature. But mainly it will be submitted
            if( helper.getNodeCount( anchor + "/x:RespondByDate") == 1){
                request.setRespondByDate( CEMStructure.parseXMLDate( helper.getValue(anchor + "/x:RespondByDate")));
            }
            int usageCount = helper.getNodeCount(anchor + "/x:CertUsage");
            for (int ii = 0; ii < usageCount; ii++) {
                String usageAnchor = anchor + "/x:CertUsage[" + (ii + 1) + "]";
                String usage = helper.getValue(usageAnchor);
                if (usage.equals("keyEncipherment")) {
                    request.setCertUsageEncryption(true);
                } else if (usage.equals("digitalSignature")) {
                    request.setCertUsageSignature(true);
                } else if (usage.equals("tlsServer")) {
                    request.setCertUsageSSL(true);
                }
            }
            request.setEndEntity(EndEntity.parse(data, anchor));
            requestList.add(request);
        }
        inStream.close();
        return (requestList);
    }

    /**
     * @return the certUsageEncryption
     */
    public boolean isCertUsageEncryption() {
        return certUsageEncryption;
    }

    /**
     * @param certUsageEncryption the certUsageEncryption to set
     */
    public void setCertUsageEncryption(boolean certUsageEncryption) {
        this.certUsageEncryption = certUsageEncryption;
    }

    /**
     * @return the certUsageSignature
     */
    public boolean isCertUsageSignature() {
        return certUsageSignature;
    }

    /**
     * @param certUsageSignature the certUsageSignature to set
     */
    public void setCertUsageSignature(boolean certUsageSignature) {
        this.certUsageSignature = certUsageSignature;
    }

    /**
     * @return the certUsageSSL
     */
    public boolean isCertUsageSSL() {
        return certUsageSSL;
    }

    /**
     * @param certUsageSSL the certUsageSSL to set
     */
    public void setCertUsageSSL(boolean certUsageSSL) {
        this.certUsageSSL = certUsageSSL;
    }

    /**
     * @return the respondByDate
     */
    public Date getRespondByDate() {
        return respondByDate;
    }

    /**
     * @param respondByDate the respondByDate to set
     */
    public void setRespondByDate(Date respondByDate) {
        this.respondByDate = respondByDate;
    }

    /**
     * @return the responseURL
     */
    public String getResponseURL() {
        return responseURL;
    }

    /**
     * @param responseURL the responseURL to set
     */
    public void setResponseURL(String responseURL) {
        this.responseURL = responseURL;
    }

    /**
     * @return the endEntity
     */
    public EndEntity getEndEntity() {
        return endEntity;
    }

    /**
     * @param endEntity the endEntity to set
     */
    public void setEndEntity(EndEntity endEntity) {
        this.endEntity = endEntity;
    }
}
