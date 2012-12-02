//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/clientserver/message/DeleteMessageRequest.java,v 1.1 2012/04/18 14:10:27 heller Exp $
package de.mendelson.comm.as2.clientserver.message;

import de.mendelson.comm.as2.message.AS2MessageInfo;
import de.mendelson.util.clientserver.messages.ClientServerMessage;
import java.io.Serializable;
import java.util.List;
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
public class DeleteMessageRequest extends ClientServerMessage implements Serializable{
    
    private List<AS2MessageInfo> deleteList;


    @Override
    public String toString(){
        return( "Delete messages" );
    }

    /**
     * @return the deleteList
     */
    public List<AS2MessageInfo> getDeleteList() {
        return deleteList;
    }

    /**
     * @param deleteList the deleteList to set
     */
    public void setDeleteList(List<AS2MessageInfo> deleteList) {
        this.deleteList = deleteList;
    }
    
    
}
