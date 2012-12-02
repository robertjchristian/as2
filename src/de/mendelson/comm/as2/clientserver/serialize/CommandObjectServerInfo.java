//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/clientserver/serialize/CommandObjectServerInfo.java,v 1.1 2012/04/18 14:10:29 heller Exp $
package de.mendelson.comm.as2.clientserver.serialize;
import java.io.*;
import java.util.Properties;
/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */

/**
 * Get some information about the server
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class CommandObjectServerInfo extends CommandObject implements Serializable{

    public static final String SERVER_PRODUCT_NAME = "serverprodname";
    public static final String SERVER_VERSION = "serverversion";
    public static final String SERVER_BUILD = "serverbuild";
    public static final String SERVER_START_TIME = "serverstarttime";
    
    /**Properties to return*/
    private Properties properties = new Properties();
    
    public CommandObjectServerInfo() {
        super();
    }

    /**Sets a single property*/
    public void setProperty( String key, String value ){
        this.properties.setProperty( key.toLowerCase(), value );
    }

    /**Returns the server properties*/
    public Properties getProperties(){
        return( this.properties );
    }
    
    /**Clear the client data on the server to prevent hardcore serialization*/
    public void clearClientData(){
        //nop
    }

    
}
