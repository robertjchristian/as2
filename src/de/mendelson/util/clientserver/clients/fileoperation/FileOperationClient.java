//$Header: /cvsroot-fuse/mec-as2/39/mendelson/util/clientserver/clients/fileoperation/FileOperationClient.java,v 1.1 2012/04/18 14:10:44 heller Exp $
package de.mendelson.util.clientserver.clients.fileoperation;

import de.mendelson.util.clientserver.BaseClient;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Performs file operations on the server side and returns a result
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class FileOperationClient {

    private BaseClient baseClient;

    public FileOperationClient(BaseClient baseClient) {
        this.baseClient = baseClient;
    }

    /**Rename a file on the server
     */
    public boolean rename(String oldName, String newName) {
        FileRenameRequest request = new FileRenameRequest();
        request.setOldName(oldName);
        request.setNewName(newName);
        FileRenameResponse response = (FileRenameResponse) this.baseClient.sendSync(request);
        if (response != null) {
            return (response.getSuccess());
        } else {
            return (false);
        }
    }

    /**Delete a file on the server
     */
    public boolean delete(String filename) {
        FileDeleteRequest request = new FileDeleteRequest();
        request.setFilename(filename);
        FileDeleteResponse response = (FileDeleteResponse) this.baseClient.sendSync(request);
        if (response != null) {
            return (response.getSuccess());
        } else {
            return (false);
        }
    }

}
