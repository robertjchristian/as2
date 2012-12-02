//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/cem/messages/EDIINTCertificateExchangeResponse.java,v 1.1 2012/04/18 14:10:22 heller Exp $
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
 * Creates a EDIINTCertificateExchangeResponse with several parameters
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class EDIINTCertificateExchangeResponse extends CEMStructure{

    public static final String STATUS_ACCEPTED_STR = "Accepted";
    public static final String STATUS_REJECTED_STR = "Rejected";

    private TradingPartnerInfo tradingPartnerInfo = null;
    private String footer = "</EDIINTCertificateExchangeResponse>";
    private String requestId = null;
    private List<TrustResponse> trustResponseList = new ArrayList<TrustResponse>();


    /** Creates new message I/O log and connects to localhost
     *@param host host to connect to
     */
    public EDIINTCertificateExchangeResponse() {
    }

    /**Parses the xml description of a CEM and creates a CEM structure of it*/
    public static final EDIINTCertificateExchangeResponse parse(byte[] data) throws Exception {
        EDIINTCertificateExchangeResponse response = new EDIINTCertificateExchangeResponse();
        ByteArrayInputStream inStream = new ByteArrayInputStream(data);
        XPathHelper helper = new XPathHelper(inStream);
        helper.addNamespace("x", "urn:ietf:params:xml:ns:ediintcertificateexchange");
        response.setRequestId(helper.getValue("/x:EDIINTCertificateExchangeResponse/@requestId"));
        response.setTradingPartnerInfo( TradingPartnerInfo.parse(data, "/x:EDIINTCertificateExchangeResponse"));
        response.setTrustResponseList( TrustResponse.parse( data ));
        inStream.close();
        return (response);
    }

    /**Returns the header as XML*/
    public String getHeaderXML(){
        if( this.getRequestId() == null ){
            throw new RuntimeException( "EDIINTCertificateExchangeResponse: request id not set.");
        }
        StringBuilder builder = new StringBuilder();
        builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        builder.append("<EDIINTCertificateExchangeResponse " );
        builder.append( "xmlns=\"urn:ietf:params:xml:ns:ediintcertificateexchange\" " );
        builder.append( "xmlns:ds=\"http://www.w3.org/2000/09/xmldsig#\" " );
        builder.append( "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " );
        builder.append("requestId=\"").append(this.getRequestId()).append("\">\n");
        return( builder.toString());
    }

    public void addTrustResponse( TrustResponse trustResponse ){
        this.getTrustResponseList().add( trustResponse );
    }

    /**Returns the message*/
    @Override
    public String toXML() {
        StringBuilder message = new StringBuilder();
        message.append( this.getHeaderXML());
        if( this.getTradingPartnerInfo() == null ){
            throw new RuntimeException( "EDIINTCertificateExchangeResponse: TradingPartnerInfo not set.");
        }else{
            message.append( this.getTradingPartnerInfo());
        }
        for( TrustResponse response: this.getTrustResponseList() ){
            message.append( response );
        }
        message.append( this.footer );
        return (message.toString());
    }

    /**
     * @return the tradingPartnerInfo
     */
    public TradingPartnerInfo getTradingPartnerInfo() {
        if( this.tradingPartnerInfo == null ){
            throw new RuntimeException( "EDIINTCertificateExchangeResponse: Trading partner info not set.");
        }
        return tradingPartnerInfo;
    }

    /**
     * @param tradingPartnerInfo the tradingPartnerInfo to set
     */
    public void setTradingPartnerInfo(TradingPartnerInfo tradingPartnerInfo) {
        this.tradingPartnerInfo = tradingPartnerInfo;
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
     * @return the trustResponseList
     */
    public List<TrustResponse> getTrustResponseList() {
        return trustResponseList;
    }

    /**
     * @param trustResponseList the trustResponseList to set
     */
    public void setTrustResponseList(List<TrustResponse> trustResponseList) {
        this.trustResponseList = trustResponseList;
    }
}
