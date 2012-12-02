//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/message/AS2Message.java,v 1.1 2012/04/18 14:10:30 heller Exp $
package de.mendelson.comm.as2.message;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

/**
 * Stores a AS2 message
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class AS2Message implements Serializable {

    public static final int ENCRYPTION_UNKNOWN = 0;
    public static final int ENCRYPTION_NONE = 1;
    public static final int ENCRYPTION_3DES = 2;
    public static final int ENCRYPTION_RC2_40 = 3;
    public static final int ENCRYPTION_RC2_64 = 4;
    public static final int ENCRYPTION_RC2_128 = 5;
    public static final int ENCRYPTION_RC2_196 = 6;
    public static final int ENCRYPTION_RC2_UNKNOWN = 7;
    public static final int ENCRYPTION_AES_128 = 8;
    public static final int ENCRYPTION_AES_192 = 9;
    public static final int ENCRYPTION_AES_256 = 10;
    public static final int ENCRYPTION_RC4_40 = 11;
    public static final int ENCRYPTION_RC4_56 = 12;
    public static final int ENCRYPTION_RC4_128 = 13;
    public static final int ENCRYPTION_RC4_UNKNOWN = 14;
    public static final int ENCRYPTION_DES = 15;
    public static final int ENCRYPTION_UNKNOWN_ALGORITHM = 99;
    public static final int SIGNATURE_UNKNOWN = 0;
    public static final int SIGNATURE_NONE = 1;
    public static final int SIGNATURE_SHA1 = 2;
    public static final int SIGNATURE_MD5 = 3;
    public static final int COMPRESSION_UNKNOWN = 0;
    public static final int COMPRESSION_NONE = 1;
    public static final int COMPRESSION_ZLIB = 2;
    public static final int STATE_FINISHED = 1;
    public static final int STATE_PENDING = 2;
    public static final int STATE_STOPPED = 3;
    public static final int CONTENT_TRANSFER_ENCODING_BINARY = 1;
    public static final int CONTENT_TRANSFER_ENCODING_BASE64 = 2;
    public static final int MESSAGETYPE_AS2 = 1;
    public static final int MESSAGETYPE_CEM = 2;
    /**Stores all details about the message*/
    private AS2Info as2Info = null;
    /**Stores the raw message data*/
    private ByteStorage rawData = new ByteStorage();
    /**Stores the raw message data, decrypted. Contains the same data as the raw data
     *if the message has been sent unencrypted
     */
    private ByteStorage decryptedRawData = new ByteStorage();
    /**Payload of the as2 message, will be only one if the AS2 version is < AS2 1.2
     */
    private List<AS2Payload> payload = new ArrayList<AS2Payload>();
    private Properties header = new Properties();
    private String contentType;

    /**Constructor to create a new message, empty message object
     */
    public AS2Message(AS2Info as2Info) {
        this.as2Info = as2Info;
    }

    public boolean isMDN(){
        return( this.as2Info.isMDN());
    }

    /**Escapes the AS2-TO and AS2-FROM headers in sending direction, related to
     * RFC 4130 section 6.2
     * @param identification as2-from or as2-to value to escape
     * @return escaped value
     */
    public static String escapeFromToHeader(String identification) {
        boolean containsBlank = false;
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < identification.length(); i++) {
            char singleChar = identification.charAt(i);
            if (singleChar == ' ') {
                containsBlank = true;
            } else if (singleChar == '"') {
                builder.append("\\");
            } else if (singleChar == '\\') {
                builder.append("\\");
            }
            builder.append(singleChar);
        }
        //quote the value if it contains blanks
        if (containsBlank) {
            builder.insert(0, "\"");
            builder.append("\"");
        }
        return (builder.toString());
    }

    /**Returns the number of attachments of the AS2 message. This will mainly be 1 if the AS2 version is < AS2 1.2
     */
    public int getPayloadCount() {
        return (this.payload.size());
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

    /**Returns the actual size of the stored raw data*/
    public int getRawDataSize() {
        return (this.rawData.getSize());
    }

    public InputStream getRawDataInputStream() throws Exception {
        return (this.rawData.getInputStream());
    }

    public byte[] getRawData() throws Exception {
        return (this.rawData.get());
    }

    public void setRawData(byte[] rawData) throws Exception {
        this.rawData.put(rawData);
    }

    /**Returns the actual size of the stored decrypted raw data*/
    public int getDecryptedRawDataSize() {
        return (this.rawData.getSize());
    }

    public InputStream getDecryptedRawDataInputStream() throws Exception {
        return (this.decryptedRawData.getInputStream());
    }

    public byte[] getDecryptedRawData() throws Exception {
        return (this.decryptedRawData.get());
    }

    public void setDecryptedRawData(byte[] decryptedRawData) throws Exception {
        this.decryptedRawData.put(decryptedRawData);
    }

    public Properties getHeader() {
        return header;
    }

    public void setHeader(Properties header) {
        this.header = header;
    }

    /**Will return the payload of the passed index. The index should be 0 if the AS2 version is < AS2 1.2
     */
    public AS2Payload getPayload(int index) {
        if (this.payload == null || this.payload.isEmpty()) {
            throw new IllegalArgumentException("AS2 message does not contain " + index + " payloads.");
        }
        return (this.payload.get(index));
    }

    public void addPayload(AS2Payload data) {
        this.payload.add(data);
    }

    /**Will return the payloads of the message
     */
    public List<AS2Payload> getPayloads() {
        List<AS2Payload> list = new ArrayList<AS2Payload>();
        list.addAll(this.payload);
        return( list );
    }

    /**Deletes the actual payloads and adds the passed ones*/
    public void setPayloads( List<AS2Payload> payloads ){
        this.payload.clear();
        this.payload.addAll(payloads);
    }
    
    
    /**Writes the payload to the message to the passed file*/
    public void writeRawDecryptedTo(File file) throws Exception {
        FileOutputStream outStream = new FileOutputStream(file);
        InputStream inStream = this.decryptedRawData.getInputStream();
        this.copyStreams(inStream, outStream);
        outStream.flush();
        outStream.close();
        inStream.close();
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * @return the as2Info
     */
    public AS2Info getAS2Info() {
        return as2Info;
    }

    /**
     * @param as2Info the as2Info to set
     */
    public void setAS2Info(AS2Info as2Info) {
        this.as2Info = as2Info;
    }

}
