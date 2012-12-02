//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/cem/gui/ResourceBundleDialogSendCEM_de.java,v 1.1 2012/04/18 14:10:20 heller Exp $
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
public class ResourceBundleDialogSendCEM_de extends MecResourceBundle{
    
    @Override
    public Object[][] getContents() {
        return contents;
    }
    
    /**List of messages in the specific language*/
    static final Object[][] contents = {
        {"title", "Zertifikate mit Partnern austauschen (CEM)" },
        {"button.ok", "Ok" },
        {"button.cancel", "Abbrechen" },
        {"label.initiator", "Lokale Station:" },
        {"label.receiver", "Empfänger:" },
        {"label.certificate", "Zertifikat:"},
        {"label.activationdate", "Aktivierungsdatum:"},
        {"cem.request.failed", "Die CEM Anfrage konnte nicht durchgeführt werden:\n{0}" },
        {"cem.request.success", "Die CEM Anfrage wurde erfolgreich ausgeführt." },
        {"cem.request.title", "Zertifikataustausch über CEM" },
    };
    
}