//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/send/ProxyObject.java,v 1.1 2012/04/18 14:10:35 heller Exp $
package de.mendelson.comm.as2.send;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Proxy object, used in the MessageHttpUploader to use proxies
 * @author  S.Heller
 * @version $Revision: 1.1 $
 */
public class ProxyObject{

    private String host = null;
    private int port = -1;
    private String user = null;
    private char[] password = null;
            
    public ProxyObject(){
    }

    /**
     * @return the host
     */
    public String getHost() {
        return host;
    }

    /**
     * @param host the host to set
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * @return the port
     */
    public int getPort() {
        return port;
    }

    /**
     * @param port the port to set
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * @return the user
     */
    public String getUser() {
        return user;
    }

    /**
     * @param user the user to set
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * @return the password
     */
    public char[] getPassword() {
        return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(char[] password) {
        this.password = password;
    }
            
    
}