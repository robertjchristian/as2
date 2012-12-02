//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/message/ResourceBundleMDNParser_de.java,v 1.1 2012/04/18 14:10:30 heller Exp $
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
public class ResourceBundleMDNParser_de extends MecResourceBundle{
    
    public Object[][] getContents() {
        return contents;
    }
    
    /**List of messages in the specific language*/
    static final Object[][] contents = {
        {"invalid.mdn.nocontenttype", "MDN ungültig: Kein content-type definiert." },
        {"structure.failure.mdn", "Eine eingehende MDN wurde erkannt. Leider ist die Struktur der MDN fehlerhaft, sodass sie nicht verarbeitet werden konnte. Die zugehörige Transaktion hat Ihren Status nicht verändert: \"{0}\"" },
    };
    
}