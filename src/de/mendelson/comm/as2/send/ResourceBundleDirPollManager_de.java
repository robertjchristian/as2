//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/send/ResourceBundleDirPollManager_de.java,v 1.1 2012/04/18 14:10:35 heller Exp $
package de.mendelson.comm.as2.send;
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
public class ResourceBundleDirPollManager_de extends MecResourceBundle{
    
    @Override
    public Object[][] getContents() {
        return contents;
    }
    
    /**List of messages in the specific language*/
    static final Object[][] contents = {
        {"manager.started", "Manager zur Verzeichnisüberwachung gestartet." },
        {"poll.stopped", "Verzeichnisüberwachung für die Beziehung \"{0}/{1}\" wurde gestoppt." },
        {"poll.started", "Verzeichnisüberwachung für die Beziehung \"{0}/{1}\" wurde gestartet. Ignoriere: \"{2}\". Intervall: {3}s" },
        {"warning.ro", "Ausgangsdatei {0} ist schreibgeschützt, Datei wird ignoriert." },
        {"warning.notcomplete", "Ausgangsdatei {0} ist noch nicht vollständig vorhanden, Datei wird ignoriert." },
        {"messagefile.deleted", "{0}: Die Datei \"{1}\" wurde gelöscht und der Verarbeitungswarteschlange des Servers übergeben." },
        {"processing.file", "Verarbeite die Datei \"{0}\" für die Beziehung \"{1}/{2}\"." }, 
        {"processing.file.error", "Verarbeitungsfehler der Datei \"{0}\" für die Beziehung \"{1}/{2}\": \"{3}\"." },
    };
    
}