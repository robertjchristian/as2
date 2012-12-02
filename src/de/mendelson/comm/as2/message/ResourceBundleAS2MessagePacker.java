//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/message/ResourceBundleAS2MessagePacker.java,v 1.1 2012/04/18 14:10:30 heller Exp $
package de.mendelson.comm.as2.message;
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
public class ResourceBundleAS2MessagePacker extends MecResourceBundle{
    
    @Override
    public Object[][] getContents() {
        return contents;
    }
    
    /**List of messages in the specific language*/
    static final Object[][] contents = {
        {"message.signed", "{0}: Outgoing message signed with the algorithm {2},using keystore alias \"{1}\"." },
        {"message.notsigned", "{0}: Outgoing message is not signed." },                 
        {"message.encrypted", "{0}: Outgoing message encrypted with the algorithm {2}, using keystore alias \"{1}\"." }, 
        {"message.notencrypted", "{0}: Outgoing message has not been encrypted." },     
        {"mdn.created", "{0}: MDN created, state set to [{1}]." },
        {"mdn.details", "{0}: MDN details: {1}" },
        {"message.compressed", "{0}: Outgoing payload compressed from {1} to {2}." },
        {"message.compressed.unknownratio", "{0}: Outgoing payload compressed." },
        {"mdn.signed", "{0}: Outgoing MDN has been signed with the algorithm \"{1}\"." },
        {"mdn.notsigned", "{0}: Outgoing MDN has not been signed." },
    };
    
}