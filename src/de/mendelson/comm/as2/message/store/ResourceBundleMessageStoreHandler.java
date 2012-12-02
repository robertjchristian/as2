//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/message/store/ResourceBundleMessageStoreHandler.java,v 1.1 2012/04/18 14:10:31 heller Exp $
package de.mendelson.comm.as2.message.store;
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
public class ResourceBundleMessageStoreHandler extends MecResourceBundle{
    
    @Override
    public Object[][] getContents() {
        return contents;
    }
    
    /**List of messages in the specific language*/
    static final Object[][] contents = {
        {"message.error.stored", "{0}: Message payload stored to \"{1}\"." },
        {"message.error.raw.stored", "{0}: Raw outgoing message stored to \"{1}\"." },        
        {"dir.createerror", "Unable to create directory \"{0}\"." },  
        {"comm.success", "{0}: AS2 communication successful, payload {1} has been moved to \"{2}\"." },
        {"outboundstatus.written", "{0}: Outbound status file written to \"{1}\"."},
    };
    
}