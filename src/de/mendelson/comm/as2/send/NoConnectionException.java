//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/send/NoConnectionException.java,v 1.1 2012/04/18 14:10:35 heller Exp $
package de.mendelson.comm.as2.send;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Indicates that a connection attempt failed
 * @author  S.Heller
 * @version $Revision: 1.1 $
 */
public class NoConnectionException extends Exception{

    
    public NoConnectionException( String message){
        super( message );
    }
    
}