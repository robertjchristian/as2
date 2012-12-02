//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/message/AS2Payload.java,v 1.1 2012/04/18 14:10:30 heller Exp $
package de.mendelson.comm.as2.message;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
 * Stores all information about an as2 payload. Since AS2 1.2 it is allowed to have multiple attachments in as2 transmission
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class AS2Payload implements Serializable {

    /**Original filename of the sender, mustnt be provided
     */
    private String originalFilename = null;
    private ByteStorage data = new ByteStorage();
    /**Filename of the payload in the as2 system*/
    private String payloadFilename = null;
    /**Content id of this payload. May be null but is important for CEM because
     * the different certificates are refrenced by their content id header
     */
    private String contentId = null;
    /**contenttype of this payload. Is not important any may be null for normal AS2 messages
     * but is important for CEM because the description xml is identified by its content type
     */
    private String contentType = null;

    public AS2Payload() {
    }

    /**Returns the content of this object for debug purpose
     */
    public String getDebugDisplay() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("originalFilename=\t\t").append(this.originalFilename);
        buffer.append("\n");
        buffer.append("data size=\t\t").append(this.data != null ? String.valueOf(this.data.getSize()) : "0");
        buffer.append("\n");
        buffer.append("payloadFilename=\t\t").append(this.payloadFilename);
        buffer.append("\n");
        buffer.append("\n");
        return (buffer.toString());
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public byte[] getData() throws Exception {
        return this.data.get();
    }

    public void setData(byte[] data) throws Exception {
        this.data.put(data);
    }

    public String getPayloadFilename() {
        return payloadFilename;
    }

    public void setPayloadFilename(String payloadFilename) {
        this.payloadFilename = payloadFilename;
    }

    /**Writes the payload to the message to the passed file*/
    public void writeTo(File file) throws Exception {
        FileOutputStream outStream = new FileOutputStream(file);
        InputStream inStream = this.data.getInputStream();
        this.copyStreams(inStream, outStream);
        outStream.flush();
        outStream.close();
        inStream.close();
    }

    /**The standard instance of this payload does not contain the data but just a reference to its filename.
     * Calling this method will load the data into the object if possible.
     */
    public void loadDataFromPayloadFile() throws Exception {
        FileInputStream inStream = new FileInputStream(this.payloadFilename);
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        this.copyStreams(inStream, outStream);
        outStream.flush();
        outStream.close();
        inStream.close();
        this.data.put(outStream.toByteArray());
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

    /**
     * @return the contentId
     */
    public String getContentId() {
        return contentId;
    }

    /**
     * @param contentId the contentId to set
     */
    public void setContentId(String contentId) {
        if (contentId != null && contentId.startsWith("<") && contentId.endsWith(">")) {
            contentId = contentId.substring(1, contentId.length() - 1);
        }
        this.contentId = contentId;
    }

    /**
     * @return the contentType
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * @param contentType the contentType to set
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
