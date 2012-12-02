//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/cem/messages/TrustResponse.java,v 1.1 2012/04/18 14:10:22 heller Exp $
package de.mendelson.comm.as2.cem.messages;

import de.mendelson.comm.as2.cem.CEMEntry;
import de.mendelson.util.XPathHelper;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;


/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Represents a trust response structure
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class TrustResponse extends CEMStructure {

    private final static int STATUS_PENDING_INT = CEMEntry.STATUS_PENDING_INT;
    private final static int STATUS_REJECTED_INT = CEMEntry.STATUS_REJECTED_INT;
    private final static int STATUS_ACCEPTED_INT = CEMEntry.STATUS_ACCEPTED_INT;
    private final static int STATUS_EXPIRED_INT = CEMEntry.STATUS_EXPIRED_INT;
    private final static int STATUS_REVOKED_INT = CEMEntry.STATUS_REVOKED_INT;
    public static final String STATUS_ACCEPTED_STR = EDIINTCertificateExchangeResponse.STATUS_ACCEPTED_STR;
    public static final String STATUS_REJECTED_STR = EDIINTCertificateExchangeResponse.STATUS_REJECTED_STR;
    private int state = STATUS_ACCEPTED_INT;
    private String reasonForRejection = null;
    private CertificateReference certificateReference = null;

    public TrustResponse() {
    }

    /**Returns the int representation of the passed state string*/
    public static final int convertState(String state) {
        if (state.equals(STATUS_ACCEPTED_STR)) {
            return (STATUS_ACCEPTED_INT);
        } else if (state.equals(STATUS_REJECTED_STR)) {
            return (STATUS_REJECTED_INT);
        } else {
            throw new IllegalArgumentException("TrustResponse convertState: Unknown state " + state);
        }
    }

    /**Returns the str representation of the passed int string*/
    public static final String convertState(int state) {
        if (state == STATUS_ACCEPTED_INT) {
            return (STATUS_ACCEPTED_STR);
        } else if (state == STATUS_REJECTED_INT) {
            return (STATUS_REJECTED_STR);
        } else {
            throw new IllegalArgumentException("TrustResponse convertState: Unknown state " + state);
        }
    }

    /**parses the trust requests from an inbound request and returns them*/
    public static List<TrustResponse> parse(byte[] data) throws Exception {
        List<TrustResponse> responseList = new ArrayList<TrustResponse>();
        ByteArrayInputStream inStream = new ByteArrayInputStream(data);
        XPathHelper helper = new XPathHelper(inStream);
        helper.addNamespace("x", "urn:ietf:params:xml:ns:ediintcertificateexchange");
        helper.addNamespace("ds", "http://www.w3.org/2000/09/xmldsig#");
        int requestCount = helper.getNodeCount("//x:TrustResponse");
        for (int i = 0; i < requestCount; i++) {
            TrustResponse response = new TrustResponse();
            String anchor = "//x:TrustResponse[" + (i + 1) + "]";
            response.setState(helper.getValue(anchor + "/x:CertStatus"));
            if( helper.getNodeCount(anchor + "/x:ReasonForRejection") > 0 ){
                response.setReasonForRejection( helper.getValue(anchor + "/x:ReasonForRejection"));
            }
            response.setCertificateReference(CertificateReference.parse(data, anchor));
            responseList.add(response);
        }
        inStream.close();
        return (responseList);
    }

    public void setState(String state) {
        this.state = convertState(state);
    }

    public int getState() {
        return (this.state);
    }

    public String getStateStr() {
        return (convertState(this.state));
    }

    @Override
    public String toXML() {
        if (this.getCertificateReference() == null) {
            throw new RuntimeException("TrustResponse: CertificateReference not set.");
        }
        StringBuilder builder = new StringBuilder();
        builder.append("\t<TrustResponse>\n");
        builder.append("\t\t<CertStatus>").append(convertState(this.state)).append("</CertStatus>\n");
        if( this.state == STATUS_REJECTED_INT && this.reasonForRejection != null ){
            builder.append("\t\t<ReasonForRejection>").append(this.toCDATA(this.reasonForRejection)).append("</ReasonForRejection>\n");
        }
        builder.append(this.getCertificateReference().toXML());
        builder.append("\t</TrustResponse>\n");
        return (builder.toString());
    }

    /**
     * @return the certificateReference
     */
    public CertificateReference getCertificateReference() {
        if (this.certificateReference == null) {
            throw new RuntimeException("TrustResponse: Certificate reference not set.");
        }
        return certificateReference;
    }

    /**
     * @param certificateReference the certificateReference to set
     */
    public void setCertificateReference(CertificateReference certificateReference) {
        this.certificateReference = certificateReference;
    }

    /**
     * @return the reasonForRejection
     */
    public String getReasonForRejection() {
        return reasonForRejection;
    }

    /**
     * @param reasonForRejection the reasonForRejection to set
     */
    public void setReasonForRejection(String reasonForRejection) {
        this.reasonForRejection = reasonForRejection;
    }
}
