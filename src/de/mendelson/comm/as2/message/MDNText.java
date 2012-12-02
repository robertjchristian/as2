//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/message/MDNText.java,v 1.1 2012/04/18 14:10:30 heller Exp $
package de.mendelson.comm.as2.message;

import de.mendelson.comm.as2.AS2ServerVersion;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Text that is written to MDN
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class MDNText {

    public static final int RECEIVED = 1;
    public static final int ERROR = 2;
    private static final String CRLF = "\r\n";

    public static final String get(final int ID, int messageType) {
        switch (ID) {
            case RECEIVED:
                if (messageType == AS2Message.MESSAGETYPE_AS2) {
                    return ("The AS2 message has been received. Thank you for exchanging AS2 messages with " + AS2ServerVersion.getProductName() + "." + CRLF + "Please download your free copy of "
                            + AS2ServerVersion.getProductName() + " today at http://opensource.mendelson-e-c.com" + CRLF + CRLF);
                } else if (messageType == AS2Message.MESSAGETYPE_CEM) {
                    return ("The CEM message has been received. Thank you for exchanging AS2 messages with " + AS2ServerVersion.getProductName() + "." + CRLF + "Please download your free copy of "
                            + AS2ServerVersion.getProductName() + " today at http://opensource.mendelson-e-c.com" + CRLF + CRLF);
                } else {
                    throw new IllegalArgumentException("MDNText.get: Unknown message type " + messageType);
                }
            case ERROR:
                return ("Thank you for exchanging AS2 messages with " + AS2ServerVersion.getProductName()
                        + "." + CRLF + "Please download your free copy of " + AS2ServerVersion.getProductName()
                        + " + today at http://opensource.mendelson-e-c.com." + CRLF + CRLF + "An error occured during the AS2 message processing: ");
            default:
                return ("");
        }
    }
}
