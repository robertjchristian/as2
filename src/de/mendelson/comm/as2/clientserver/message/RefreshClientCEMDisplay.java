//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/clientserver/message/RefreshClientCEMDisplay.java,v 1.1 2012/04/18 14:10:27 heller Exp $
package de.mendelson.comm.as2.clientserver.message;

import de.mendelson.util.clientserver.messages.ClientServerMessage;
import java.io.Serializable;
/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Msg for the client server protocol
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class RefreshClientCEMDisplay extends ClientServerMessage implements Serializable{
    
    @Override
    public String toString(){
        return( "CEM display refresh request" );
    }
    
    
}
