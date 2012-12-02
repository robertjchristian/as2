//$Header: /cvsroot-fuse/mec-as2/39/mendelson/util/security/cert/ResourceBundleCertificateManager_fr.java,v 1.1 2012/04/18 14:10:47 heller Exp $
package de.mendelson.util.security.cert;

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
public class ResourceBundleCertificateManager_fr extends MecResourceBundle {

    @Override
    public Object[][] getContents() {
        return contents;
    }
    /**List of messages in the specific language*/
    static final Object[][] contents = {
        {"keystore.reloaded", "Les clefs privées et les certificats ont été rechargés."},
        {"alias.notfound", "Le porte-clef ne contient aucun certificat sous l''alias \"{0}\"."},
        {"alias.hasno.privatekey", "Le porte-clef ne contient aucune clef privée sous l''alias \"{0}\"."},
        {"alias.hasno.key", "Le porte-clef ne contient aucun objet sous l''alias \"{0}\"."},
        {"certificate.not.found.fingerprint", "Le certificat avec le \"{0}\" d'empreintes SHA-1 n''existe pas."},
        {"keystore.read.failure", "Le système est incapable de lire les certificats. Erreur: \"{0}\". S''il vous plaît vous assurer que vous utilisez le mot de passe keystore correct."},
    };
}
