//$Header: /cvsroot-fuse/mec-as2/39/mendelson/util/security/cert/ResourceBundleCertificateManager_de.java,v 1.1 2012/04/18 14:10:47 heller Exp $
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
 * @version $Revision: 1.1 $
 */
public class ResourceBundleCertificateManager_de extends MecResourceBundle {

    @Override
    public Object[][] getContents() {
        return contents;
    }
    /**List of messages in the specific language*/
    static final Object[][] contents = {
        {"keystore.reloaded", "Die Zertifikatdatei wurde erneut eingeladen, alle Schlüssel und Zertifikate wurden aktualisiert."},
        {"alias.notfound", "Die Zertifikatdatei beinhaltet kein Zertifikat mit dem Alias \"{0}\"."},
        {"alias.hasno.privatekey", "Die Zertifikatdatei beinhaltet keinen privaten Schlüssel mit dem Alias \"{0}\"."},
        {"alias.hasno.key", "Die Zertifikatdatei beinhaltet keinen Schlüssel mit dem Alias \"{0}\"."},
        {"certificate.not.found.fingerprint", "Das Zertifikat mit dem  SHA-1 Fingerprint \"{0}\" existiert nicht."},
        {"keystore.read.failure", "Das System kann die hinterlegten Zertifikate/Schlüssel nicht lesen. Fehlermeldung: \"{0}\". Bitte stellen Sie sicher, dass Sie das richtige Keystore Passwort gesetzt haben."},
    };
}
