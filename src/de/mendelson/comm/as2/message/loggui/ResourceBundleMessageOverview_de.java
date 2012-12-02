//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/message/loggui/ResourceBundleMessageOverview_de.java,v 1.1 2012/04/18 14:10:30 heller Exp $
package de.mendelson.comm.as2.message.loggui;
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
public class ResourceBundleMessageOverview_de extends MecResourceBundle{
    
    public Object[][] getContents() {
        return contents;
    }
    
    /**List of messages in the specific language*/
    static final Object[][] contents = {
        {"header.timestamp", "Zeit" },
        {"header.localstation", "Lokale Station" },
        {"header.partner", "Partner" },                    
        {"header.messageid", "Referenznummer" },                            
        {"header.encryption", "Verschlüsselung" },
        {"header.signature", "Digitale Signatur" },
        {"header.mdn", "MDN" },         
        {"header.payload", "Nutzdaten" },   
        {"number.of.attachments", "* {0} Dateianhänge *" },
    };
    
}