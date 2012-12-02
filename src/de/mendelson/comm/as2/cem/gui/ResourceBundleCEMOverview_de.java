//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/cem/gui/ResourceBundleCEMOverview_de.java,v 1.1 2012/04/18 14:10:20 heller Exp $
package de.mendelson.comm.as2.cem.gui;
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
public class ResourceBundleCEMOverview_de extends MecResourceBundle{
    
    @Override
    public Object[][] getContents() {
        return contents;
    }

    /**List of messages in the specific language*/
    static final Object[][] contents = {
        {"title", "Verwaltung Zertifikataustausch" },
        {"button.sendcem", "Neues Zertifikat austauschen" },
        {"button.requestdetails", "Details der Anfrage" },
        {"button.responsedetails", "Details der Antwort" },
        {"button.exit", "Schliessen" },
        {"button.cancel", "Abbrechen" },
        {"button.refresh", "Aktualisieren" },
        {"button.remove", "Löschen" },
        {"header.state", "Antwort" },
        {"header.category", "Benutzt für" },
        {"header.requestdate", "Anfragedatum" },
        {"header.initiator", "Von" },
        {"header.receiver", "An" },
        {"label.certificate", "Zertifikat:"},
        {"header.alias", "Zertifikat"},
        {"header.activity", "Systemaktivität" },
        {"activity.waitingforprocessing", "Warte auf Verarbeitung" },
        {"activity.waitingforanswer", "Warte auf Antwort" },
        {"activity.waitingfordate", "Warte bis zum Aktivierungdatum ({0})" },
        {"activity.activated", "Keine - Aktiviert am {0}" },
        {"activity.none", "Keine" },
        {"tab.certificate", "Zertifikatinformation" },
        {"tab.reasonforrejection", "Ablehnungsbegründung" },
    };
    
}