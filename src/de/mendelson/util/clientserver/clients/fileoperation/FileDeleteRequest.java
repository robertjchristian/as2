//$Header: /cvsroot-fuse/mec-as2/39/mendelson/util/clientserver/clients/fileoperation/FileDeleteRequest.java,v 1.1 2012/04/18 14:10:44 heller Exp $
package de.mendelson.util.clientserver.clients.fileoperation;

import de.mendelson.util.clientserver.messages.ClientServerMessage;
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
public class FileDeleteRequest extends ClientServerMessage implements Serializable{

    private String filename = null;

    @Override
    public String toString(){
        return( "File delete request" );
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
