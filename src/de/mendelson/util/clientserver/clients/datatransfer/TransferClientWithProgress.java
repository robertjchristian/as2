//$Header: /cvsroot-fuse/mec-as2/39/mendelson/util/clientserver/clients/datatransfer/TransferClientWithProgress.java,v 1.1 2012/04/18 14:10:41 heller Exp $
package de.mendelson.util.clientserver.clients.datatransfer;

import de.mendelson.util.ProgressPanel;
import de.mendelson.util.clientserver.BaseClient;
import java.io.InputStream;

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
public class TransferClientWithProgress extends TransferClient {

    private ProgressPanel progressPanel;

    public TransferClientWithProgress(BaseClient baseClient, ProgressPanel progressPanel) {
        super(baseClient);
        this.progressPanel = progressPanel;
    }

    /**Sends the data of the inputstream synced to the server and returns a unique number from the server
     * for the upload process
     */
    public String uploadChunkedWithProgress(InputStream inStream, String display, int maxBytes) throws Throwable {
        String targetHash = null;
        int readBytes = 0;
        String uniqueId = display + String.valueOf(maxBytes) + inStream.hashCode() + System.currentTimeMillis();
        try {
            this.progressPanel.startProgress(display, uniqueId, 0, maxBytes);
            while (true) {
                byte[] data = super.copyBytesFromStream(inStream, 50000);
                if (data != null && data.length > 0) {                                      
                    readBytes += data.length;
                    UploadRequestChunk uploadRequest = new UploadRequestChunk();
                    uploadRequest.setData(data);
                    uploadRequest.setTargetHash(targetHash);
                    UploadResponseChunk response = (UploadResponseChunk) super.getBaseClient().sendSync(uploadRequest, TransferClient.TIMEOUT);
                    if (response != null) {
                        targetHash = response.getTargetHash();
                    }
                    //display this progress in the progress bar
                    this.progressPanel.setProgressValue(uniqueId, readBytes);
                } else {
                    //file seems to be transferred
                    break;
                }
            }
        } finally {
            this.progressPanel.stopProgressIfExists(uniqueId);
        }
        return (targetHash);
    }
}
