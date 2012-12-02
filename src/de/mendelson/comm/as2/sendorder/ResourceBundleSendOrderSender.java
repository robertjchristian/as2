//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/sendorder/ResourceBundleSendOrderSender.java,v 1.1 2012/04/18 14:10:38 heller Exp $
package de.mendelson.comm.as2.sendorder;
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
public class ResourceBundleSendOrderSender extends MecResourceBundle{
    
    public Object[][] getContents() {
        return contents;
    }
    
    /**List of messages in the specific language*/
    static final Object[][] contents = {
        {"message.packed", "{0}: Outbound AS2 message created from \"{1}\" for the receiver \"{2}\" in {4}, raw message size: {3}" },
    };
    
}