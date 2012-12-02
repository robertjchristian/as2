//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/clientserver/serialize/CommandObjectShutdown.java,v 1.1 2012/04/18 14:10:29 heller Exp $
package de.mendelson.comm.as2.clientserver.serialize;
import java.io.*;
import java.net.InetAddress;
/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */

/**
 * Object to send to the AS2 server. It shuts down the server
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class CommandObjectShutdown extends CommandObject implements Serializable{

    /**User who tried to shutdown the server*/
    private String clientUser = null;
    /**IP of the client that tries to shutdown the server*/
    private String clientIP = null;
    /**Client address*/
    private String clientName = null;
    
    
    public CommandObjectShutdown() {
        super();
        try{
            InetAddress address = InetAddress.getLocalHost();
            this.clientIP = address.getHostAddress();
            this.clientName = address.getHostName();
            this.clientUser = System.getProperty( "user.name" );
        }
        catch( Exception ignore ){
        }
    }
    
    /**Clear the client data on the server to prevent hardcore serialization*/
    public void clearClientData(){
    }

    public String getClientUser() {
        return clientUser;
    }

    public String getClientIP() {
        return clientIP;
    }

    public String getClientName() {
        return clientName;
    }
    
}
