//$Header: /cvsroot-fuse/mec-as2/39/mendelson/util/clientserver/SyncRequestTransportLevelException.java,v 1.1 2012/04/18 14:10:41 heller Exp $
package de.mendelson.util.clientserver;
/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Exception that is thrown if a sync request failed on the transport level
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class SyncRequestTransportLevelException extends Exception{
    
    @Override
    public String getMessage(){
        return( "Sync request failed.");
    }

}
