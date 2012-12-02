//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/server/ResourceBundleAS2ServerRemoteImpl_de.java,v 1.1 2012/04/18 14:10:39 heller Exp $
package de.mendelson.comm.as2.server;

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
public class ResourceBundleAS2ServerRemoteImpl_de extends MecResourceBundle {

    public Object[][] getContents() {
        return contents;
    }
    /**List of messages in the specific language*/
    static final Object[][] contents = {
        {"server.shutdown", "Der Benutzer {0} ({1}) fährt den Server herunter."},
        {"server.start", "Starte {0} Server"},
        {"rmi.port.in.use", "Der {2} Server kann nicht gestartet werden: Der Serverport\n" + "{0} Wird bereits von einem anderen Prozess belegt.\n" + "Fehlermeldung:\n" + "{1}."},
        {"unknown.sender", "Der Nachrichtensender \"{0}\" existiert nicht."},
        {"unknown.receiver", "Der Nachrichtenempfänger \"{0}\" existiert nicht."},
        {"receiver.not.localstation", "Der Nachrichtenempfänger \"{0}\" ist als Empfänger in der Nachricht definiert, nicht jedoch in den Partnereinstellungen als lokale Station."},
        {"incoming.exception", "Fehlersignal wurde empfangen."},
        {"exception.errorcode", "Fehlercode ist {0} im Abschnitt {1}."},
        {"exception.type", "Fehlertyp: {0}."},
        {"exception.description", "Fehlerbeschreibung: {0}."},
        {"sync.mdn.sent", "{0}: Synchrone MDN als Antwort auf {1} versandt."},
        {"invalid.request.from", "Eine ungültige Anfrage ist eingegangen. Sie wird nicht verarbeitet, weil kein as2-from Header vorhanden ist."},
        {"invalid.request.to", "Eine ungültige Anfrage ist eingegangen. Sie wird nicht verarbeitet, weil kein as2-to Header vorhanden ist."},
        {"invalid.request.messageid", "Eine ungültige Anfrage ist eingegangen. Sie wird nicht verarbeitet, weil kein message-id Header vorhanden ist."},        
    };
}