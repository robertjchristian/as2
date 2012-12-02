//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/cem/messages/EDIINTCertificateExchangeRequest.java,v 1.1 2012/04/18 14:10:22 heller Exp $
package de.mendelson.comm.as2.cem.messages;

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
 * Creates a EDIINTCertificateExchangeRequest with several parameters
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class EDIINTCertificateExchangeRequest extends CEMStructure {

    private String requestId = null;
    private List<TrustRequest> trustRequestList = new ArrayList<TrustRequest>();
    private TradingPartnerInfo tradingPartnerInfo = null;

    /** Creates new message I/O log and connects to localhost
     *@param host host to connect to
     */
    public EDIINTCertificateExchangeRequest() {
    }

    /**Returns a created XML header*/
    public String getHeaderXML() {
        if (this.requestId == null) {
            throw new RuntimeException("EDIINTCertificateExchangeRequest: requestId not set.");
        }
        StringBuilder builder = new StringBuilder();
        builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        builder.append("<EDIINTCertificateExchangeRequest ");
        builder.append("xmlns=\"urn:ietf:params:xml:ns:ediintcertificateexchange\" ");
        builder.append("xmlns:ds=\"http://www.w3.org/2000/09/xmldsig#\" ");
        builder.append("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" ");
        builder.append("requestId=\"").append(this.requestId).append("\">\n");
        return (builder.toString());
    }

    /**Returns the request as xml*/
    @Override
    public String toXML() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.getHeaderXML());
        if (this.getTradingPartnerInfo() == null) {
            throw new RuntimeException("EDIINTCertificateExchangeRequest: Trading partner info not set.");
        }
        builder.append(this.getTradingPartnerInfo().toXML());
        for (TrustRequest trustRequest : this.trustRequestList) {
            builder.append(trustRequest.toXML());
        }
        builder.append("</EDIINTCertificateExchangeRequest>");
        return (builder.toString());
    }

    /**Parses the xml description of a CEM and creates a CEM structure of it*/
    public static final EDIINTCertificateExchangeRequest parse(byte[] data) throws Exception {
        EDIINTCertificateExchangeRequest request = new EDIINTCertificateExchangeRequest();
        ByteArrayInputStream inStream = new ByteArrayInputStream(data);
        XPathHelper helper = new XPathHelper(inStream);
        helper.addNamespace("x", "urn:ietf:params:xml:ns:ediintcertificateexchange");
        request.setRequestId(helper.getValue("/x:EDIINTCertificateExchangeRequest/@requestId"));
        request.setTradingPartnerInfo(TradingPartnerInfo.parse(data, "/x:EDIINTCertificateExchangeRequest"));
        request.setTrustRequestList(TrustRequest.parse(data));
        inStream.close();
        return (request);
    }

    /**
     * @return the requestId
     */
    public String getRequestId() {
        return requestId;
    }

    /**
     * @param requestId the requestId to set
     */
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    /**
     * @return the trustRequestList
     */
    public List<TrustRequest> getTrustRequestList() {
        return trustRequestList;
    }

    /**
     * @param trustRequestList the trustRequestList to set
     */
    public void setTrustRequestList(List<TrustRequest> trustRequestList) {
        this.trustRequestList = trustRequestList;
    }

    public void addTrustRequest(TrustRequest trustRequest) {
        this.trustRequestList.add(trustRequest);
    }

    /**
     * @return the tradingPartnerInfo
     */
    public TradingPartnerInfo getTradingPartnerInfo() {
        if (this.tradingPartnerInfo == null) {
            throw new RuntimeException("EDIINTCertificateExchangeRequest: Trading partner info not set.");
        }
        return( this.tradingPartnerInfo);
    }

    /**
     * @param tradingPartnerInfo the tradingPartnerInfo to set
     */
    public void setTradingPartnerInfo(TradingPartnerInfo tradingPartnerInfo) {
        this.tradingPartnerInfo = tradingPartnerInfo;
    }
}
