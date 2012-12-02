//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/server/ResourceBundleAS2ServerRemoteImpl.java,v 1.1 2012/04/18 14:10:39 heller Exp $
package de.mendelson.comm.as2.server;
import de.mendelson.util.MecResourceBundle;
/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */

/**
 * ResourceBundle to localize a mendelson product
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class ResourceBundleAS2ServerRemoteImpl extends MecResourceBundle{
    
    public Object[][] getContents() {
        return contents;
    }
    
    /**List of messages in the specific language*/
    static final Object[][] contents = {
        { "server.shutdown", "The user {0} ({1}) requests a server shutdown." },        
        { "server.start", "Starting {0} server" },
        { "rmi.port.in.use", "Unable to start the {2} server: The server port\n"
                  + "{0} is already in use by an other process.\n"
                  + "Error message of the bind command: \n"
                  + "{1}."
        },
        {"unknown.sender", "Message sender \"{0}\" does not exist." },
        {"unknown.receiver", "Message receiver \"{0}\" does not exist." },
        {"receiver.not.localstation", "Message receiver \"{0}\" is defined as message receiver in the incoming message but is not defined as local station in the partner settings." },
        {"incoming.exception", "Incoming Exception received." },
        {"exception.errorcode", "Error code {0} in {1}." },
        {"exception.type", "Error type: {0}." },
        {"exception.description", "Error description: {0}." },
        {"sync.mdn.sent", "{0}: Synchronous MDN sent as answer to message {1}." },
        {"invalid.request.from", "An invalid request has been detected. It has not been processed because it does not contain a as2-from header."},
        {"invalid.request.to", "An invalid request has been detected. It has not been processed because it does not contain a as2-to header."},
        {"invalid.request.messageid", "An invalid request has been detected. It has not been processed because it does not contain a message id header."},
    };
    
}