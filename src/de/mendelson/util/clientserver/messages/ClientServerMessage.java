//$Header: /cvsroot-fuse/mec-as2/39/mendelson/util/clientserver/messages/ClientServerMessage.java,v 1.1 2012/04/18 14:10:45 heller Exp $
package de.mendelson.util.clientserver.messages;

import java.io.Serializable;
/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Superclass of all messages for the client server protocol
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class ClientServerMessage implements Serializable{

    private static long referenceIdCounter = 0;
    private long referenceId = 0;
    private boolean _syncRequest = false;

    public ClientServerMessage(){
        this.referenceId = getNextReferenceId();
    }

    /**Returns the next unique reference id, thread safe*/
    public static synchronized long getNextReferenceId(){
        referenceIdCounter++;
        return( referenceIdCounter);
    }

    public Long getReferenceId(){
        return( Long.valueOf(this.referenceId));
    }

   
    /** Internal method, do NOT use it
     * @return the _syncRequest
     */
    public boolean _isSyncRequest() {
        return _syncRequest;
    }

    /** Internal method, do NOT use it
     * @param syncRequest the _syncRequest to set
     */
    public void _setSyncRequest(boolean syncRequest) {
        this._syncRequest = syncRequest;
    }

    /**
     * @param referenceId the referenceId to set
     */
    protected void _setReferenceId(long referenceId) {
        this.referenceId = referenceId;
    }

}
