//$Header: /cvsroot-fuse/mec-as2/39/mendelson/util/clientserver/clients/filesystemview/FileSystemViewRequest.java,v 1.1 2012/04/18 14:10:44 heller Exp $
package de.mendelson.util.clientserver.clients.filesystemview;

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
public class FileSystemViewRequest extends ClientServerMessage implements Serializable {

    public static final int TYPE_LIST_ROOTS = 1;
    public static final int TYPE_LIST_CHILDREN = 2;
    public static final int TYPE_GET_PATH_STR = 3;
    public static final int TYPE_GET_PATH_ELEMENTS = 4;
    private int requestType = TYPE_LIST_ROOTS;
    private FileObject parameterFile = null;
    private String parameterString = null;

    public FileSystemViewRequest( int requestType ){
        this.requestType = requestType;
    }
    
    public int getRequestType(){
        return( this.requestType);
    }
    
    @Override
    public String toString() {
        return ("File system view request");
    }

    /**
     * @return the parameterFile
     */
    public FileObject getParameterFile() {
        return parameterFile;
    }

    /**
     * @param parameterFile the parameterFile to set
     */
    public void setParameterFile(FileObject parameterFile) {
        this.parameterFile = parameterFile;
    }

    /**
     * @return the parameterString
     */
    public String getParameterString() {
        return parameterString;
    }

    /**
     * @param parameterString the parameterString to set
     */
    public void setParameterString(String parameterString) {
        this.parameterString = parameterString;
    }

}
