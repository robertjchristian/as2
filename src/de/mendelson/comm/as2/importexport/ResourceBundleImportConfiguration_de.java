//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/importexport/ResourceBundleImportConfiguration_de.java,v 1.1 2012/04/18 14:10:30 heller Exp $ 
package de.mendelson.comm.as2.importexport;

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
public class ResourceBundleImportConfiguration_de extends MecResourceBundle {

    public Object[][] getContents() {
        return contents;
    }
    /**List of messages in the specific language*/
    static final Object[][] contents = {
        {"button.import", "Importieren"},
        {"button.cancel", "Abbrechen"},
        {"title", "Import der Konfiguration"},
        {"import.info", "Bitte wählen Sie die Konfiguration und die Partner, die Sie importieren wollen."},
        {"label.propertiesimport", "Servereinstellungen importieren (Proxy, Keystore Pfade, Spracheinstellungen)"},
        {"label.notificationimport", "Benachrichtigungsdaten importieren (Mail Konto, Generelle Benachrichtigungsoptionen)"},
        {"invalid.importfile", "Das ist keine gültige Importdatei." },
        {"header.name", "Name" },
        {"header.as2id", "AS2 id" },
        {"import.success.msg", "Die Konfiguration wurde erfolgreich importiert." },
        {"import.success.title", "Erfolg" },
        {"import.failed.msg", "Es kam beim Import zu einem Fehler: \"{0}\"" },
        {"import.failed.title", "Import fehlgeschlagen" },
        {"title.partner", "Partner" },
        {"title.config", "Konfiguration" },
        {"partner.all", "Alle Partner" },
        {"partner.none", "Kein Partner" },
    };
}
