//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/cem/gui/ResourceBundleDialogSendCEM.java,v 1.1 2012/04/18 14:10:20 heller Exp $
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
public class ResourceBundleDialogSendCEM extends MecResourceBundle{
    
    @Override
    public Object[][] getContents() {
        return contents;
    }
    
    /**List of messages in the specific language*/
    static final Object[][] contents = {
        {"title", "Exchange certificate with partners via CEM" },
        {"button.ok", "Ok" },
        {"button.cancel", "Cancel" },
        {"label.initiator", "Local station:" },
        {"label.receiver", "Receiver:" },
        {"label.certificate", "Certificate:"},
        {"label.activationdate", "Activation date:"},
        {"cem.request.failed", "The CEM request failed:\n{0}" },
        {"cem.request.success", "The CEM request has been sent successful." },
        {"cem.request.title", "Certificate exchange via CEM" },
    };
    
}