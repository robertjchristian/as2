//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/client/rmi/GenericClient.java,v 1.1 2012/04/18 14:10:25 heller Exp $
package de.mendelson.comm.as2.client.rmi;
import de.mendelson.comm.as2.clientserver.ErrorObject;
import de.mendelson.comm.as2.clientserver.serialize.CommandObject;
import de.mendelson.comm.as2.preferences.PreferencesAS2;
import javax.swing.*;

import java.util.*;
import de.mendelson.util.rmi.RMISender;
import de.mendelson.util.rmi.RMISenderAS2;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */

/**
 * Client to just send an object without any special functions
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class GenericClient{
    
    /*Command object to send*/
    private CommandObject commandObject = null;
    
    /**Product preferences*/
    private PreferencesAS2 preferences = new PreferencesAS2();
     
    /** Creates new  generic client
     *@param commandObject command request object, available from a higher
     *communication request e.g. SOAP or null, if the object should be created.
     *@param parentComponent Parent component to display error messages in,
     *must be null for text-only clients
     *@param server Server to connect to via RMI
     */
    public GenericClient(){
    }
    
    /**Returns the clients error object*/
    public ErrorObject getErrorObject(){
        return( this.commandObject.getErrorObject() );
    }
    
    /**Returns the requested command object from the server,
     *call this on the client site to access the requested values
     */
    public CommandObject getCommandObject(){
        return( this.commandObject );
    }
    
    
    /**Sends the request to the server
     *@return an ErrorObject that contains the state of the request
     */
    private void sendRequest( CommandObject commandObject ){
        this.commandObject = commandObject;
        //create a sender
        RMISender sender = new RMISenderAS2(
        this.preferences.get( PreferencesAS2.SERVER_HOST ),
        this.preferences.getInt( PreferencesAS2.SERVER_RMI_PORT),
        this.preferences.get( PreferencesAS2.SERVER_RMI_SERVICE ) );
        //do not send the request if the object build process was not error free
        if( this.getErrorObject().noErrorsAndWarnings() ){
            //execute the request on the server
            this.commandObject
            = (CommandObject)sender.sendRequest( this.commandObject );
        }
    }
    
    /**Sends the request to the server
     *@return an ErrorObject that contains the state of the request
     */
    public ErrorObject send( CommandObject commandObject ){
        this.sendRequest( commandObject );
        return( this.getErrorObject() );
    }
    
    
    /**Returns the clients log*/
    public ArrayList getLog(){
        return( this.commandObject.getLog() );
    }
    
}
