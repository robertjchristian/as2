//$Header: /cvsroot-fuse/mec-as2/39/mendelson/util/clientserver/clients/filesystemview/FileSystemViewResponse.java,v 1.1 2012/04/18 14:10:44 heller Exp $
package de.mendelson.util.clientserver.clients.filesystemview;

import de.mendelson.util.clientserver.messages.ClientServerResponse;
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
public class FileSystemViewResponse extends ClientServerResponse implements Serializable {

    private FileObject parameterFile = null;
    private List<FileObject> parameterFileArray = null;
    private String parameterString = null;

    public FileSystemViewResponse(FileSystemViewRequest request) {
        super(request);
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
     * @return the parameterFileArray
     */
    public List<FileObject> getParameterFileArray() {
        return parameterFileArray;
    }

    /**
     * @param parameterFileArray the parameterFileArray to set
     */
    public void setParameterFileArray(List<FileObject> parameterFileArray) {
        this.parameterFileArray = parameterFileArray;
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
