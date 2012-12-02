//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/message/loggui/ResourceBundleMessageDetails_fr.java,v 1.1 2012/04/18 14:10:30 heller Exp $
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
 * ResourceBundle to localize gui entries
 * @author S.Heller
 * @author E.Pailleau
 * @version $Revision: 1.1 $
 */
public class ResourceBundleMessageDetails_fr extends MecResourceBundle{
    
    public Object[][] getContents() {
        return contents;
    }
    
    /**List of messages in the specific language*/
    static final Object[][] contents = {
        
        {"title", "Détails du message" },
        {"button.ok", "Valider" },
        {"header.timestamp", "Date" },
        {"header.messageid", "Réf No" },
        {"message.raw.decrypted", "Données brutes (non décrypté)" },
        {"message.header", "Entête message" },
        {"message.payload", "Contenu transféré" },
        {"message.payload.multiple", "Contenu ({0})" },
        {"tab.log", "Log de l''instance de ce message" },
        {"header.encryption", "Cryptage" },
        {"header.signature", "Signature" },
        {"header.senderhost", "Emetteur" },
        {"header.useragent", "Serveur AS2" },
    };
    
}
