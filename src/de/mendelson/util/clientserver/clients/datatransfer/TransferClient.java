//$Header: /cvsroot-fuse/mec-as2/39/mendelson/util/clientserver/clients/datatransfer/TransferClient.java,v 1.1 2012/04/18 14:10:41 heller Exp $
package de.mendelson.util.clientserver.clients.datatransfer;

import de.mendelson.util.clientserver.SyncRequestTransportLevelException;
import de.mendelson.util.clientserver.BaseClient;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Requests downloads from and sends new uploads to the server
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class TransferClient {

    /**Set a timeout of 30s for these requests*/
    public static final long TIMEOUT = TimeUnit.SECONDS.toMillis(30);
    private BaseClient baseClient;

    public TransferClient(BaseClient baseClient) {
        this.baseClient = baseClient;
    }

    /**Uploads data to the server
     *@return true if the transfer worked fine
     */
    public UploadResponseFile upload(UploadRequestFile request) throws Throwable{
        UploadResponseFile response = (UploadResponseFile) this.getBaseClient().sendSync(request, TIMEOUT);
        if( response == null ){
            throw new SyncRequestTransportLevelException();
        }
        if (response.getException() != null) {
            throw response.getException();
        }
        return (response);
    }

    /**Sends the data of the inputstream synced to the server and returns a unique number from the server
     * for the upload process
     */
    public String uploadChunked( InputStream inStream ) throws Throwable{        
        String targetHash = null;
        while( true ){
            byte[] data = this.copyBytesFromStream(inStream, 50000);
            if( data != null && data.length > 0 ){
                UploadRequestChunk uploadRequest = new UploadRequestChunk();
                uploadRequest.setData(data);
                uploadRequest.setTargetHash(targetHash);
                UploadResponseChunk response 
                        = (UploadResponseChunk) this.getBaseClient().sendSync(uploadRequest, TIMEOUT);
                if( response != null ){
                    targetHash = response.getTargetHash();
                }
            }else{
                //file seems to be transferred
                break;
            }
        }
        return( targetHash );
    }
    
    /**Copies a requested number of bytes from one stream to another*/
    protected byte[] copyBytesFromStream(InputStream in, int minChunkSize) throws IOException {
        //WARNING do not use buffered streams here, this is just a chunk that is cut of the stream!
        ByteArrayOutputStream memOut = new ByteArrayOutputStream();          
        //copy the contents to an output stream
        byte[] buffer = new byte[2048];
        int read = 2048;
        int actualCount = 0;
        //a read of 0 must be allowed, sometimes it takes time to
        //extract data from the input
        while (read != -1 && actualCount <= minChunkSize) {
            read = in.read(buffer);
            if (read > 0) {
                memOut.write(buffer, 0, read);
                actualCount += read;
            }
        }
        memOut.flush();
        memOut.close();
        return( memOut.toByteArray());
    }
    
    
    
    
    public DownloadResponse download(DownloadRequest request) throws Throwable {
        DownloadResponse response = (DownloadResponse) this.getBaseClient().sendSync(request, TIMEOUT);
        if( response == null ){
            throw new SyncRequestTransportLevelException();
        }
        if (response.getException() != null) {
            throw response.getException();
        }
        return (response);
    }

    /**
     * @return the baseClient
     */
    public BaseClient getBaseClient() {
        return baseClient;
    }
}
