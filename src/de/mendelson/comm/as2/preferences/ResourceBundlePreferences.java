//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/preferences/ResourceBundlePreferences.java,v 1.1 2012/04/18 14:10:35 heller Exp $
package de.mendelson.comm.as2.preferences;

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
public class ResourceBundlePreferences extends MecResourceBundle {

    @Override
    public Object[][] getContents() {
        return contents;
    }
    /**List of messages in the specific language*/
    static final Object[][] contents = {
        //preferences localized
        {PreferencesAS2.SERVER_HOST, "Server host"},
        {PreferencesAS2.SERVER_RMI_PORT, "Server RMI port"},
        {PreferencesAS2.DIR_MSG, "Message storage"},
        {"button.ok", "Ok"},
        {"button.cancel", "Cancel"},
        {"button.modify", "Modify"},
        {"button.browse", "Browse"},
        {"filechooser.selectdir", "Select a directory to set"},
        {"title", "Preferences"},
        {"tab.language", "Language"},
        {"tab.dir", "Directories"},
        {"tab.security", "Security"},
        {"tab.proxy", "Proxy"},
        {"tab.misc", "Misc"},
        {"tab.maintenance", "Maintenance"},
        {"tab.notification", "Notification"},
        {"tab.interface", "Modules"},
        {"header.dirname", "Type"},
        {"header.dirvalue", "Dir"},
        {"label.keystore.https.pass", "Keystore password (https send):"},
        {"label.keystore.pass", "Keystore password (encryption/signature):"},
        {"label.keystore.https", "Keystore (https send):"},
        {"label.keystore.encryptionsign", "Keystore (enc, sign):"},
        {"label.proxy.url", "Proxy URL:"},
        {"label.proxy.user", "Proxy login user:"},
        {"label.proxy.pass", "Proxy login pass:"},
        {"label.proxy.use", "Use a proxy for outgoing HTTP/HTTPs connections"},
        {"label.proxy.useauthentification", "Use proxy authentification"},
        {"filechooser.keystore", "Please select the keystore file (jks format)."},
        {"label.days", "days"},
        {"label.deletemsgolderthan", "Auto delete messages older than"},
        {"label.deletemsglog", "Inform in log about auto deleted messages"},
        {"label.deletestatsolderthan", "Auto delete statistic data older than"},
        {"label.asyncmdn.timeout", "Max waiting time for async MDN:"},
        {"label.httpsend.timeout", "HTTP(s) send timeout:"},
        {"label.min", "minutes"},
        {"receipt.subdir", "Create subdirectory for receipt messages per partner"},
        //notification
        {"checkbox.notifycertexpire", "Notify certificate expire"},
        {"checkbox.notifytransactionerror", "Notify transaction errors"},
        {"checkbox.notifycem", "Notify certificate exchange (CEM) events"},
        {"checkbox.notifyfailure", "Notify system problems"},
        {"checkbox.notifyresend", "Notify rejected resends"},
        {"button.testmail", "Send test mail"},
        {"label.mailhost", "Mail server host:"},
        {"label.mailport", "Port:"},
        {"label.mailaccount", "Mail server account:"},
        {"label.mailpass", "Mail server password:"},
        {"label.notificationmail", "Notification receiver address:"},
        {"label.replyto", "Replyto address:"},
        {"label.smtpauthentication", "Use SMTP authentication"},
        {"label.smtpauthentication.user", "User name:"},
        {"label.smtpauthentication.pass", "Password:"},
        {"testmail.message.success", "Test mail sent successfully."},
        {"testmail.message.error", "Error sending test mail:\n{0}"},
        {"testmail.title", "Test mail send result"},
        //interface
        {"label.showhttpheader", "Allow to configure the HTTP headers in the partner configuration"},
        {"label.showquota", "Allow to configure quota notification in the partner configuration"},
        {"label.cem", "Allow certificate exchange (CEM)"},
        {"label.outboundstatusfiles", "Write outbound transaction status files"},
        {"info.restart.client", "A client restart is required to make these changes valid!" },
        {"remotedir.select", "Select a directory on the server" },
    };
}
