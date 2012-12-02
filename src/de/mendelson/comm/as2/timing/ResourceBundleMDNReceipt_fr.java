//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/timing/ResourceBundleMDNReceipt_fr.java,v 1.1 2012/04/18 14:10:39 heller Exp $
package de.mendelson.comm.as2.timing;
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
 * @author E.Pailleau
 * @version $Revision: 1.1 $
 */
public class ResourceBundleMDNReceipt_fr extends MecResourceBundle{
    
    public Object[][] getContents() {
        return contents;
    }
    
    /**List of messages in the specific language*/
    static final Object[][] contents = {
        {"expired", "{0}: Temps d''attente pour le MDN expiré." },    
    };
    
}
