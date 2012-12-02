//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/clientserver/serialize/CommandObjectIncomingMessage.java,v 1.1 2012/04/18 14:10:29 heller Exp $
package de.mendelson.comm.as2.clientserver.serialize;
import java.io.*;
import java.util.Properties;
import javax.servlet.http.HttpServletResponse;
/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */

/**
 * Object to send to the AS2 server. It indicates an incoming signal or message
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class CommandObjectIncomingMessage extends CommandObject implements Serializable{

    private String contentType = null;
    
    private String remoteHost = null;
    
    private Properties header = new Properties();

    private String messageDataFilename = null;

    private byte[] mdnData = null;

    /**Stores the http return code that is transmitted to the receipt servlet and will be returned*/
    private int httpReturnCode = HttpServletResponse.SC_OK;

    public CommandObjectIncomingMessage() {
        super();
    }

    public void addHeader( String key, String value ){
        this.header.setProperty( key.toLowerCase(), value );
    }
    
    /**Deletes the existing request header and sets new
     */
    public void setHeader( Properties header ){
        this.header = header;
    }
    
    public Properties getHeader(){
        return( this.header );
    }
    
    /**Clear the client data on the server to prevent hardcore serialization*/
    public void clearClientData(){
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getRemoteHost() {
        return remoteHost;
    }

    public void setRemoteHost(String remoteHost) {
        this.remoteHost = remoteHost;
    }

    /**
     * @return the httpReturnCode
     */
    public int getHttpReturnCode() {
        return httpReturnCode;
    }

    /**
     * @param httpReturnCode the httpReturnCode to set
     */
    public void setHttpReturnCode(int httpReturnCode) {
        this.httpReturnCode = httpReturnCode;
    }

    /**
     * @return the messageDataFilename
     */
    public String getMessageDataFilename() {
        return messageDataFilename;
    }

    /**
     * @param messageDataFilename the messageDataFilename to set
     */
    public void setMessageDataFilename(String messageDataFilename) {
        this.messageDataFilename = messageDataFilename;
    }

    /**
     * @return the mdnData
     */
    public byte[] getMDNData() {
        return mdnData;
    }

    /**
     * @param mdnData the mdnData to set
     */
    public void setMDNData(byte[] mdnData) {
        this.mdnData = mdnData;
    }
    
}
