//$Header: /cvsroot-fuse/mec-as2/39/mendelson/util/clientserver/clients/datatransfer/UploadRequestChunk.java,v 1.1 2012/04/18 14:10:41 heller Exp $
package de.mendelson.util.clientserver.clients.datatransfer;

import de.mendelson.util.clientserver.messages.ClientServerMessage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
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
public class UploadRequestChunk extends ClientServerMessage implements Serializable {

    private byte[] data = null;
    private String targetHash = null;

    public void setData(byte[] data){
        this.data = data;
    }

    @Override
    public String toString() {
        return ("Upload request chunk");
    }
    
    /**
     * @return the data
     */
    public InputStream getDataStream() {
        ByteArrayInputStream inStream = new ByteArrayInputStream(this.getDataBytes());
        return (inStream);
    }

    /**
     * @return the data
     */
    public byte[] getDataBytes() {
        return data;
    }

    /**
     * @return the targetHash
     */
    public String getTargetHash() {
        return targetHash;
    }

    /**
     * @param targetHash the targetHash to set
     */
    public void setTargetHash(String targetHash) {
        this.targetHash = targetHash;
    }

}
