//$Header: /cvsroot-fuse/mec-as2/39/mendelson/util/clientserver/ClientsideMessageProcessor.java,v 1.1 2012/04/18 14:10:41 heller Exp $
package de.mendelson.util.clientserver;

import de.mendelson.util.clientserver.messages.ClientServerMessage;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Interface for all client class that process a server message
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public interface ClientsideMessageProcessor {

    /**Returns if the message has been processed by the instance*/
    public boolean processMessageFromServer( ClientServerMessage message );

}
