//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/message/store/ResourceBundleMessageStoreHandler_fr.java,v 1.1 2012/04/18 14:10:31 heller Exp $
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
 * @author E.Pailleau
 * @version $Revision: 1.1 $
 */
public class ResourceBundleMessageStoreHandler_fr extends MecResourceBundle{
    
    @Override
    public Object[][] getContents() {
        return contents;
    }
    
    /**List of messages in the specific language*/
    static final Object[][] contents = {
        {"message.error.stored", "{0}: Contenu du message stocké vers \"{1}\"." },
        {"message.error.raw.stored", "{0}: Message sortant brut stocké vers \"{1}\"." },
        {"dir.createerror", "Création impossible du répertoire \"{0}\"." },
        {"comm.success", "{0}: Succès de la communication AS2, le contenu {1} a été déplacé vers \"{2}\"." },
        {"outboundstatus.written", "{0}: Fichier d''état sortant écrit \"{1}\"."},
    };
    
}
