//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/client/manualsend/ManualSendRequest.java,v 1.1 2012/04/18 14:10:24 heller Exp $
package de.mendelson.comm.as2.client.manualsend;

import de.mendelson.comm.as2.partner.Partner;
import de.mendelson.util.clientserver.clients.datatransfer.UploadRequestFile;
import java.io.Serializable;
/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */

/**
 * Msg for the client server protocol
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class ManualSendRequest extends UploadRequestFile implements Serializable {

    private Partner sender;
    private Partner receiver;
    private String filename;

    @Override
    public String toString() {
        return ("Manual send request");
    }

    /**
     * @return the sender
     */
    public Partner getSender() {
        return sender;
    }

    /**
     * @param sender the sender to set
     */
    public void setSender(Partner sender) {
        this.sender = sender;
    }

    /**
     * @return the receiver
     */
    public Partner getReceiver() {
        return receiver;
    }

    /**
     * @param receiver the receiver to set
     */
    public void setReceiver(Partner receiver) {
        this.receiver = receiver;
    }

    /**
     * @return the filename
     */
    public String getFilename() {
        return filename;
    }

    /**
     * @param filename the filename to set
     */
    public void setFilename(String filename) {
        this.filename = filename;
    }

}
