//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/cem/messages/TradingPartnerInfo.java,v 1.1 2012/04/18 14:10:22 heller Exp $
package de.mendelson.comm.as2.cem.messages;

import de.mendelson.util.XPathHelper;
import java.io.ByteArrayInputStream;
import java.util.Date;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Represents a trading partner info structure
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class TradingPartnerInfo extends CEMStructure {

    private String senderAS2Id = null;
    private Date messageOriginated = new Date();

    public TradingPartnerInfo() {
    }

    /**parses the trust requests from an inbound request and returns them*/
    public static TradingPartnerInfo parse(byte[] data, String anchor) throws Exception {
        TradingPartnerInfo partnerInfo = new TradingPartnerInfo();
        ByteArrayInputStream inStream = new ByteArrayInputStream(data);
        XPathHelper helper = new XPathHelper(inStream);
        helper.addNamespace("x", "urn:ietf:params:xml:ns:ediintcertificateexchange");
        anchor = anchor + "/x:TradingPartnerInfo";
        partnerInfo.setSenderAS2Id(helper.getValue(anchor + "/x:Name"));
        partnerInfo.setMessageOriginated(CEMStructure.parseXMLDate(helper.getValue(anchor + "/x:MessageOriginated")));
        inStream.close();
        return (partnerInfo);
    }

    @Override
    public String toXML() {
        if (this.getSenderAS2Id() == null) {
            throw new RuntimeException("CEM TradingPartnerInfo: senderAS2Id not set.");
        }
        if (this.messageOriginated == null) {
            throw new RuntimeException("CEM TradingPartnerInfo: messageOriginated not set.");
        }
        StringBuilder builder = new StringBuilder();
        builder.append("\t<TradingPartnerInfo>\n");
        builder.append("\t\t<Name qualifier=\"AS2\">").append(this.getSenderAS2Id()).append("</Name>\n");
        builder.append("\t\t<MessageOriginated>").append(this.toXMLDate(this.messageOriginated)).append("</MessageOriginated>\n");
        builder.append("\t</TradingPartnerInfo>\n");
        return (builder.toString());
    }

    /**
     * @return the senderAS2Id
     */
    public String getSenderAS2Id() {
        return senderAS2Id;
    }

    /**
     * @param senderAS2Id the senderAS2Id to set
     */
    public void setSenderAS2Id(String senderAS2Id) {
        this.senderAS2Id = senderAS2Id;
    }

    /**
     * @return the messageOriginated
     */
    public Date getMessageOriginated() {
        return messageOriginated;
    }

    /**
     * @param messageOriginated the messageOriginated to set
     */
    public void setMessageOriginated(Date messageOriginated) {
        this.messageOriginated = messageOriginated;
    }
}
