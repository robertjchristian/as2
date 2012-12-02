//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/client/ResourceBundleAS2Gui.java,v 1.1 2012/04/18 14:10:23 heller Exp $ 
package de.mendelson.comm.as2.client;

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
public class ResourceBundleAS2Gui extends MecResourceBundle {

    @Override
    public Object[][] getContents() {
        return contents;
    }
    /**List of messages in the specific language*/
    static final Object[][] contents = {
        {"menu.file", "File"},
        {"menu.file.exit", "Exit"},
        {"menu.file.partner", "Partner"},
        {"menu.file.certificates", "Certificates"},
        {"menu.file.certificate", "Certificates"},
        {"menu.file.certificate.signcrypt", "Certificates (sign, crypt)"},
        {"menu.file.certificate.ssl", "Certificates (SSL)"},
        {"menu.file.cem", "Certificate Exchange Manager (CEM)"},
        {"menu.file.cemsend", "Exchange certificates with partners (CEM)"},
        {"menu.file.statistic", "Statistic"},
        {"menu.file.quota", "Quota"},
        {"menu.file.export", "Export configuration"},
        {"menu.file.import", "Import configuration"},
        {"menu.file.preferences", "Preferences"},
        {"menu.file.send", "Send file to partner"},
        {"menu.file.resend", "Send as new transaction"},
        {"menu.help", "Help"},
        {"menu.help.about", "About"},
        {"menu.help.shop", "mendelson online shop"},
        {"menu.help.helpsystem", "Help system"},
        {"menu.help.forum", "Forum"},
        {"details", "Message details"},
        {"filter.showfinished", "Show finished"},
        {"filter.showpending", "Show pending"},
        {"filter.showstopped", "Show stopped"},
        {"filter.none", "-- None --"},
        {"filter.partner", "Partner restriction:"},
        {"filter.localstation", "Local station restriction:"},
        {"filter.direction", "Direction restriction:"},
        {"filter.direction.inbound", "Inbound"},
        {"filter.direction.outbound", "Outbound"},
        {"filter", "Filter"},
        {"keyrefresh", "Reload keystore"},
        {"delete.msg", "Delete selected messages"},
        {"stoprefresh.msg", "Toggle refresh"},
        {"dialog.msg.delete.message", "Do you really want to delete the selected messages permanent?"},
        {"dialog.msg.delete.title", "Delete messages"},
        {"welcome", "Welcome, {0}"},
        {"warning.eval", "This is an evaluation copy."},
        {"warning.refreshstopped", "The GUI refresh has been stopped."},
        {"tab.welcome", "News and updates"},
        {"tab.transactions", "Transactions"},
        {"new.version", "A new version is available. Click here to download it."},
        {"filechooser.export", "Please select an export file."},
        {"filechooser.import", "Please select an import file."},
        {"export.success", "The configuration has been exported successfully to \"{0}\"."},
        {"dbconnection.failed.message", "Unable to establish a DB connection to the AS2 server: {0}"},
        {"dbconnection.failed.title", "Unable to connect"},
        {"login.failed.client.incompatible.message", "The server reports that this client is incompatible. Please use the proper client version."},
        {"login.failed.client.incompatible.title", "Login rejected"},
        {"uploading.to.server", "Uploading to server"},
        {"refresh.overview", "Refreshing transaction list"},
        {"resend.performed", "This transaction has been manually resend as a new transaction."},
        {"dialog.resend.message", "Do you really want to resend the data of the selected transaction?"},
        {"dialog.resend.title", "Transaction resend"},
    };
}