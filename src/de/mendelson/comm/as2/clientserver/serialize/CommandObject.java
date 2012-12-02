//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/clientserver/serialize/CommandObject.java,v 1.1 2012/04/18 14:10:28 heller Exp $
package de.mendelson.comm.as2.clientserver.serialize;
import de.mendelson.comm.as2.clientserver.ErrorObject;
import java.io.*;
import java.util.*;
import de.mendelson.util.rmi.IRMISenderObject;
/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */


/**
 * Object to be executed at the server site. Do not change this class
 * without any serious reason. Any change in this class will make the
 * new created RMI interface incompatible to the last version!
 * @author  S.Heller
 * @version $Revision: 1.1 $
 * @since build 68
 */
public abstract class CommandObject implements Serializable, IRMISenderObject{
        
    /**Error log returned form the server site*/
    private ArrayList logList = new ArrayList();
    /**Number of warnings and errors occured on the server site*/
    private ErrorObject errorObject = new ErrorObject();
        
    public CommandObject(){
    }
            
    /**Client site use: number of warnings and errors that occured on the
     * server site*/
    public ErrorObject getErrorObject(){
        return( this.errorObject );
    }
    
    /**Log of the execution, localized, use this on the client site!*/
    public ArrayList getLog(){
        return( this.logList );
    }
    
    /**Subclasses have to implement this. The server will execute it and
     * delete all client data transmitted to the server.
     */
    public abstract void clearClientData();
    
    /** Indicates from the server site that an error occured working
     * on the server */
    public void indicateErrorOnServer(String[] errorText){
        this.errorObject.incErrors();
        this.logList.addAll( Arrays.asList( errorText ));
    }
            
}
