//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/database/ResourceBundleDBServer_de.java,v 1.1 2012/04/18 14:10:29 heller Exp $
package de.mendelson.comm.as2.database;
import de.mendelson.util.MecResourceBundle;
/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */

/** 
 * ResourceBundle to localize the mendelson business integration  - if you want to localize 
 * eagle to your language, please contact us: localize@mendelson.de
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class ResourceBundleDBServer_de extends MecResourceBundle{

  public Object[][] getContents() {
    return contents;
  }

  /**List of messages in the specific language*/
  static final Object[][] contents = {
              
    {"server.started", "{0} gestartet." },

    {"update.versioninfo", "Automatisches Datenbankupdate: Die gefundene DB Version"
    + " ist {0}, die benoetigte ist {1}." },
    {"update.progress", "Inkrementelles Datenbankupdate gestartet..." },
    {"update.progress.version.start", "Beginne Update der DB auf Version {0}..." },
    {"update.progress.version.end", "Update der {1} DB auf Version {0} fertig." },
    {"update.error", "FATAL: Es ist unmoeglich, die DB von der Version {0} "
    + " zur Version {1} zu modifizieren.\n"
        + "Bitte loeschen Sie alle entsprechenden Dateien im Installationsverzeichnis.\n"
        + "Dadurch gehen alle benutzerdefinierten Daten verloren." },
    {"update.successfully", 
        "DB {0}: Die Datenbank wurde erfolgreich fuer die notwendige Version modifiziert." },
    {"update.notfound", "Fuer das Update muss die Datei update{0}to{1}.sql und/oder"
    + " die Datei Update{0}to{1}.class im Verzeichnis {2} existieren." },   
    {"upgrade.required", "Ein Upgrade muss durchgeführt werden.\nBitte führen Sie die Datei as2upgrade.bat oder as2upgrade.sh aus, bevor Sie den Server starten." },
  };		
  
}