//$Header: /cvsroot-fuse/mec-as2/39/mendelson/util/rmi/RMISender.java,v 1.1 2012/04/18 14:10:45 heller Exp $
package de.mendelson.util.rmi;
import java.rmi.*;

import de.mendelson.util.MecResourceBundle;
import java.util.*;
import java.util.logging.Logger;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */


/**
 * Class to send an object via RMI, execute it on the
 * server side and return a return object
 * @author  S.Heller
 * @version $Revision: 1.1 $
 */
public abstract class RMISender{
        
    /**Name of the host to connect to*/
    protected String host = null;
    /**Port number to connect to*/
    protected int port = -1;
    /**name of the registered service*/
    protected String service = null;
    /**ResourceBundle to localize this messages*/
    protected MecResourceBundle rb = null;
    
    protected Logger logger = Logger.getLogger( "de.mendelson.mbi");
    
    /**There exists only a text client, no popups*/
    protected boolean textOnly = false;
    
    /**Create a new RMISender object
     * @param host name of the server
     * @param port Port of the RMI server to connect to
     * @param service name of the registred service to connect to
     */
    public RMISender( String host, int port, String service ){
        
        //Load default resourcebundle
        try{
            this.rb = (MecResourceBundle)ResourceBundle.getBundle(
            ResourceBundleRMISender.class.getName());
        }
        //load up default english resourcebundle
        catch ( MissingResourceException e ) {
            throw new RuntimeException( "Oops..resource bundle "
            + e.getClassName() + " not found" );
        }
        this.host = host;
        this.port = port;
        this.service = service;
    }
            
    /**Checks if the requested host is alive and allows a
     * RMI connection */
    public boolean hostIsAlive(){
        try{
            MecRemote request = null;
            request = (MecRemote)Naming.lookup( "rmi://" + this.host + ":" + this.port
            + "/" + this.service );
            RMIPing ping = request.ping();
            return( true );
        }
        catch( Exception e ){
            this.logger.warning(
                this.rb.getResourceString( "no.server", new Object[]{ this.host,
                String.valueOf( this.port)}));
            this.logger.info( e.getMessage() );
        }
        return( false );
    }
    
    /**Sets a new server to the sender class
     * @param host nes server to connect to
     * @param port port the RMI server listens to
     */
    public void setNewServer( String host, int port ){
        this.host = host;
        this.port = port;
    }
    
    /**Returns the actual host this sender sends requests to*/
    public String getHost(){
        return( this.host );
    }
    
    /**Returns the RMI port this sender is connecting to*/
    public int getPort(){
        return( this.port );
    }
    
    
    /**Sends a request to the server
     * @param requestObject contents of the request
     */
    public abstract Object sendRequest( IRMISenderObject requestObject );
}
