//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/sendorder/SendOrder.java,v 1.1 2012/04/18 14:10:38 heller Exp $
package de.mendelson.comm.as2.sendorder;

import de.mendelson.comm.as2.message.AS2Message;
import de.mendelson.comm.as2.partner.Partner;
import java.io.Serializable;
/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */

/**
 * Send order that will be enqueued into the as2 server message queue
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class SendOrder implements Serializable {

    public static final int STATE_WAITING = 0;
    public static final int STATE_PROCESSING = 1;
    
    private Partner receiver;
    private AS2Message message;
    private Partner sender;
    private int retryCount = 0;
    private int dbId = -1;

    public Partner getReceiver() {
        return receiver;
    }

    public void setReceiver(Partner receiver) {
        this.receiver = receiver;
    }

    public AS2Message getMessage() {
        return message;
    }

    public void setMessage(AS2Message message) {
        this.message = message;
    }

    public Partner getSender() {
        return sender;
    }

    public void setSender(Partner sender) {
        this.sender = sender;
    }

    public synchronized int incRetryCount() {
        this.retryCount++;
        return (this.retryCount);
    }

    /**
     * @return the dbId
     */
    public int getDbId() {
        return dbId;
    }

    /**
     * @param dbId the dbId to set
     */
    public void setDbId(int dbId) {
        this.dbId = dbId;
    }
        
}
