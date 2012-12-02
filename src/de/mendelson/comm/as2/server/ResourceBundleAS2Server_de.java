//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/server/ResourceBundleAS2Server_de.java,v 1.1 2012/04/18 14:10:39 heller Exp $
package de.mendelson.comm.as2.server;

import de.mendelson.comm.as2.AS2ServerVersion;
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
public class ResourceBundleAS2Server_de extends MecResourceBundle {

    @Override
    public Object[][] getContents() {
        return contents;
    }
    /**List of messages in the specific language*/
    static final Object[][] contents = {
        {"fatal.limited.strength", "Diese Java VM unterstützt nicht die notwendige Schlüssellänge. Bitte installieren Sie die \"Unlimited jurisdiction key strength policy\" Dateien, bevor Sie den " + AS2ServerVersion.getProductName() + " Server starten." },
        {"server.willstart", "{0}"},
        {"server.started", AS2ServerVersion.getFullProductName() + " gestartet in {0} ms."},
        {"rmi.port.in.use", "Fehler beim Starten des {2} Servers: Der Serverport\n" + "{0} wird bereits von einem anderen Prozess belegt.\n" + "Fehlermeldung: \n" + "{1}."},
        {"server.already.running", "Eine " + AS2ServerVersion.getProductName() + " Instanz scheint bereits zu laufen.\nEs könnte jedoch auch sein, dass eine vorherige Instanz nicht korrekt beendet wurde." + " Wenn Sie sicher sind, dass keine andere Instanz läuft,\nlöschen Sie bitte die Lock Datei \"{0}\"\n(Start Datum {1}) und starten den Server erneut."},
        {"server.nohttp", "Der integrierte HTTP Server wurde nicht gestartet." },
    };
}