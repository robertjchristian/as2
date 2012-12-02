//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/message/ResourceBundleAS2MessagePacker_de.java,v 1.1 2012/04/18 14:10:30 heller Exp $
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
public class ResourceBundleAS2MessagePacker_de extends MecResourceBundle{
    
    @Override
    public Object[][] getContents() {
        return contents;
    }
    
    /**List of messages in the specific language*/
    static final Object[][] contents = {
        {"message.signed", "{0}: Die ausgehende Nachricht wurde mit dem Algorithmus {2} digital signiert, benutzt wurde der Schlüssel Alias \"{1}\"." },
        {"message.notsigned", "{0}: Die ausgehende Nachricht wurde nicht digital signiert." },
        {"message.encrypted", "{0}: Die ausgehende Nachricht wurde mit dem Algorithmus {2} verschlüsselt, benutzt wurde der Schlüssel Alias \"{1}\"." },
        {"message.notencrypted", "{0}: Die ausgehende Nachricht wurde nicht verschlüsselt." },
        {"mdn.created", "{0}: MDN erstellt, Status auf [{1}] gesetzt." },
        {"mdn.details", "{0}: MDN Details: {1}" },
        {"message.compressed", "{0}: Die ausgehenden Nutzdaten wurden von {1} auf {2} komprimiert." },
        {"message.compressed.unknownratio", "{0}: Die ausgehenden Nutzdaten wurden komprimiert." },
        {"mdn.signed", "{0}: Ausgehende MDN wurde mit dem Algorithmus \"{1}\" signiert." },
        {"mdn.notsigned", "{0}: Ausgehende MDN wurde nicht signiert." },
    };
    
}