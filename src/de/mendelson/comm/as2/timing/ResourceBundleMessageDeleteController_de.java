//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/timing/ResourceBundleMessageDeleteController_de.java,v 1.1 2012/04/18 14:10:39 heller Exp $
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
 * @version $Revision: 1.1 $
 */
public class ResourceBundleMessageDeleteController_de extends MecResourceBundle{
    
    public Object[][] getContents() {
        return contents;
    }
    
    /**List of messages in the specific language*/
    static final Object[][] contents = {
        {"autodelete", "{0}: Diese Nachricht ist älter als {1} Tage und wurde automatisch vom Systempflegeprozess gelöscht." },    
    };
    
}