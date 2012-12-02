//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/send/ResourceBundleHttpSendManager.java,v 1.1 2012/04/18 14:10:35 heller Exp $
package de.mendelson.comm.as2.send;
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
public class ResourceBundleHttpSendManager extends MecResourceBundle{
    
    public Object[][] getContents() {
        return contents;
    }
    
    /**List of messages in the specific language*/
    static final Object[][] contents = {
        {"queue.started", "Http send queue started." },    
        {"async.mdn.wait", "{0}: Will wait for async MDN until {1}." },            
    };
    
}