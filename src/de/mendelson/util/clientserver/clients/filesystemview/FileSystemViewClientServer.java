//$Header: /cvsroot-fuse/mec-as2/39/mendelson/util/clientserver/clients/filesystemview/FileSystemViewClientServer.java,v 1.1 2012/04/18 14:10:44 heller Exp $
package de.mendelson.util.clientserver.clients.filesystemview;

import de.mendelson.util.clientserver.BaseClient;
import java.util.List;
import java.util.concurrent.TimeUnit;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Handles the access to remote directories
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class FileSystemViewClientServer{

    private BaseClient baseClient;

    public FileSystemViewClientServer(BaseClient baseClient) {
        this.baseClient = baseClient;
    }
    
    public List<FileObject> getPathElements( String path ){
        FileSystemViewRequest request = new FileSystemViewRequest(FileSystemViewRequest.TYPE_GET_PATH_ELEMENTS);
        request.setParameterString(path);
        return (this.sendSyncRequest(request)).getParameterFileArray();
    }
    
    public String getPathStr(FileObject file){
        FileSystemViewRequest request = new FileSystemViewRequest(FileSystemViewRequest.TYPE_GET_PATH_STR);
        request.setParameterFile(file);
        return (this.sendSyncRequest(request)).getParameterString();
    }
    
    public List<FileObject> listRoots() {
        FileSystemViewRequest request = new FileSystemViewRequest(FileSystemViewRequest.TYPE_LIST_ROOTS);
        return (this.sendSyncRequest(request)).getParameterFileArray();
    }

    public List<FileObject> listChildren( FileObject parent ) {
        FileSystemViewRequest request = new FileSystemViewRequest(FileSystemViewRequest.TYPE_LIST_CHILDREN);
        request.setParameterFile(parent);
        return (this.sendSyncRequest(request)).getParameterFileArray();
    }
    
    private FileSystemViewResponse sendSyncRequest(FileSystemViewRequest request) {
        //there could be a IO timeout, e.g. for an unused CD drive
        FileSystemViewResponse response = (FileSystemViewResponse) this.baseClient.sendSync(request, TimeUnit.SECONDS.toMillis(30));
        return (response);
    }
}
