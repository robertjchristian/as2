//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/message/ResourceBundleAS2MessageParser.java,v 1.1 2012/04/18 14:10:30 heller Exp $
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
public class ResourceBundleAS2MessageParser extends MecResourceBundle{
    
    @Override
    public Object[][] getContents() {
        return contents;
    }
    
    /**List of messages in the specific language*/
    static final Object[][] contents = {
        {"mdn.incoming", "{0}: Incoming transmission is a MDN." },
        {"mdn.answerto", "{0}: MDN is the answer to AS2 message \"{1}\"." },  
        {"mdn.state", "{0}: MDN state is [{1}]." },          
        {"mdn.details", "{0}: Details of MDN received from remote AS2 server: {1}" },
        {"msg.incoming", "{0}: Incoming transmission is a AS2 message, raw message size: {1}." },   
        {"mdn.signed", "{0}: MDN is signed." },
        {"mdn.unsigned.error", "{0}: MDN is not signed. The partner configuration defines MDN from the partner \"{1}\" to be signed." },
        {"mdn.signed.error", "{0}: MDN is signed. The partner configuration defines MDN from the partner \"{1}\" to be not signed." },
        {"msg.signed", "{0}: AS2 message is signed." },        
        {"msg.encrypted", "{0}: AS2 message is encrypted." },        
        {"msg.notencrypted", "{0}: AS2 message is not encrypted." },                
        {"msg.notsigned", "{0}: AS2 message is not signed." },                
        {"mdn.notsigned", "{0}: MDN is not signed." },
        {"signature.ok", "{0}: Digital signature verified successful." },
        {"signature.failure", "{0}: Verification of digital signature failed {1}" },
        {"signature.using.alias", "{0}: Using certificate \"{1}\" to verify signature." }, 
        {"decryption.done.alias", "{0}: The data has been decrypted using the key \"{1}\"." },
        {"mdn.unexpected.messageid", "{0}: The MDN references a AS2 message with the message id \"{1}\" that does not exist." },
        {"mdn.unexpected.state", "{0}: The MDN references the AS2 message with the message id \"{1}\" that is not waiting for an MDN." },
        {"data.compressed.expanded", "{0}: The compressed payload has been expanded from {1} to {2}." },
        {"found.attachments", "{0}: Found {1} payload attachments in the message." },
        {"decryption.inforequired", "{0}: To decrypt the data a key with the following parameter is required:\n{1}" },
        {"decryption.infoassigned", "{0}: A key with the following parameter is used to decrypt the data (alias \"{1}\"):\n{2}" },
        {"signature.analyzed.digest", "{0}: The sender used the algorithm {1} to sign the message." },
        {"filename.extraction.error", "{0}: Unable to extract original filename: \"{1}\", ignoring filename." },
        {"contentmic.match", "{0}: The Message Integrity Code (MIC) matches the sent AS2 message." },
        {"contentmic.failure", "{0}: The Message Integrity Code (MIC) does not match the sent AS2 message (required: {1}, returned: {2})." },
        {"found.cem", "{0}: The message is a Certificate Exchange Message (CEM)." },
    };
    
}