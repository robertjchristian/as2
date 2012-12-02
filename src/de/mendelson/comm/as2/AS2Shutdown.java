//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/AS2Shutdown.java,v 1.1 2012/04/18 14:10:16 heller Exp $
package de.mendelson.comm.as2;
import de.mendelson.comm.as2.client.rmi.GenericClient;
import de.mendelson.comm.as2.clientserver.serialize.CommandObjectShutdown;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */

/**
 * Shutdown a AS2 server
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class AS2Shutdown{
       
    /**Method to start the server on from the command line*/
    public static void main( String args[] ){
        CommandObjectShutdown commandObject = new CommandObjectShutdown();
        GenericClient client = new GenericClient();
        client.send( commandObject );
    }
    
}