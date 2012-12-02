//$Header: /cvsroot-fuse/mec-as2/39/mendelson/util/clientserver/clients/datatransfer/DownloadResponse.java,v 1.1 2012/04/18 14:10:41 heller Exp $
package de.mendelson.util.clientserver.clients.datatransfer;

import de.mendelson.util.clientserver.messages.ClientServerResponse;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
public abstract class DownloadResponse extends ClientServerResponse implements Serializable {

    private byte[] data = null;
    private long size = 0;

    public DownloadResponse(DownloadRequest request) {
        super(request);
    }

    public void setData(InputStream inStream) throws IOException {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        this.copyStreams(inStream, outStream);
        outStream.flush();
        outStream.close();
        this.data = outStream.toByteArray();
    }

    public void setData(byte[] data) throws IOException {
        this.data = data;
    }

    /**
     * @return the data
     */
    public InputStream getDataStream() {
        ByteArrayInputStream inStream = new ByteArrayInputStream(this.data);
        return (inStream);
    }

    /**
     * @return the data
     */
    public byte[] getDataBytes() {
        return data;
    }

    /**Copies all data from one stream to another*/
    private void copyStreams(InputStream in, OutputStream out) throws IOException {
        BufferedInputStream inStream = new BufferedInputStream(in);
        BufferedOutputStream outStream = new BufferedOutputStream(out);
        //copy the contents to an output stream
        byte[] buffer = new byte[2048];
        int read = 2048;
        //a read of 0 must be allowed, sometimes it takes time to
        //extract data from the input
        while (read != -1) {
            read = inStream.read(buffer);
            if (read > 0) {
                outStream.write(buffer, 0, read);
            }
        }
        outStream.flush();
    }

    @Override
    public String toString() {
        return ("Download response");
    }

    /**
     * @return the size
     */
    public long getSize() {
        return size;
    }

    /**
     * @param size the size to set
     */
    public void setSize(long size) {
        this.size = size;
    }
}
