//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/send/HttpConnectionParameter.java,v 1.1 2012/04/18 14:10:35 heller Exp $
package de.mendelson.comm.as2.send;

import de.mendelson.comm.as2.AS2ServerVersion;
import java.net.InetAddress;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Sets several parameter for an outbound http connection. This includes routing, connection and protocol issues
 * @author  S.Heller
 * @version $Revision: 1.1 $
 */
public class HttpConnectionParameter {

    public static final String HTTP_1_0 = "1.0";
    public static final String HTTP_1_1 = "1.1";
    
    private boolean staleConnectionCheck = true;
    private int connectionTimeoutMillis = -1;
    private int soTimeoutMillis = -1;
    private InetAddress localAddress = null;
    private String userAgent = AS2ServerVersion.getUserAgent();
    private String httpProtocolVersion = null;
    private boolean useExpectContinue = true;
    private ProxyObject proxy = null;

    public HttpConnectionParameter() {
    }


    public void setProxy( String host, int port, String user, char[] password){
        this.setProxy(new ProxyObject());
        this.getProxy().setHost(host);
        this.getProxy().setPort(port);
        if( user != null ){
            this.getProxy().setUser(user);
            if( password != null ){
                this.getProxy().setPassword(password);
            }
        }
    }

    /**
     * @return the staleConnectionCheck
     */
    public boolean isStaleConnectionCheck() {
        return staleConnectionCheck;
    }

    /**
     * @param staleConnectionCheck the staleConnectionCheck to set
     */
    public void setStaleConnectionCheck(boolean staleConnectionCheck) {
        this.staleConnectionCheck = staleConnectionCheck;
    }

    /**
     * @return the connectionTimeout
     */
    public int getConnectionTimeoutMillis() {
        return connectionTimeoutMillis;
    }

    /**
     * @param connectionTimeout the connectionTimeout to set
     */
    public void setConnectionTimeoutMillis(int connectionTimeout) {
        this.connectionTimeoutMillis = connectionTimeout;
    }

    /**
     * @return the soTimeout
     */
    public int getSoTimeoutMillis() {
        return soTimeoutMillis;
    }

    /**
     * @param soTimeout the soTimeout to set
     */
    public void setSoTimeoutMillis(int soTimeoutMillis) {
        this.soTimeoutMillis = soTimeoutMillis;
    }

    /**
     * @return the localAddress
     */
    public InetAddress getLocalAddress() {
        return localAddress;
    }

    /**
     * @param localAddress the localAddress to set
     */
    public void setLocalAddress(InetAddress localAddress) {
        this.localAddress = localAddress;
    }

    /**
     * @return the userAgent
     */
    public String getUserAgent() {
        return userAgent;
    }

    /**
     * @param userAgent the userAgent to set
     */
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    /**
     * @return the httpProtocolVersion
     */
    public String getHttpProtocolVersion() {
        return httpProtocolVersion;
    }

    /**
     * @param httpProtocolVersion the httpProtocolVersion to set
     */
    public void setHttpProtocolVersion(String httpProtocolVersion) {
        this.httpProtocolVersion = httpProtocolVersion;
    }

    /**
     * @return the useExpectContinue
     */
    public boolean isUseExpectContinue() {
        return useExpectContinue;
    }

    /**
     * @param useExpectContinue the useExpectContinue to set
     */
    public void setUseExpectContinue(boolean useExpectContinue) {
        this.useExpectContinue = useExpectContinue;
    }

    /**
     * @return the proxy
     */
    public ProxyObject getProxy() {
        return proxy;
    }

    /**
     * @param proxy the proxy to set
     */
    public void setProxy(ProxyObject proxy) {
        this.proxy = proxy;
    }

}
