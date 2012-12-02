//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/preferences/ResourceBundlePreferences_de.java,v 1.1 2012/04/18 14:10:35 heller Exp $
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
public class ResourceBundlePreferences_de extends MecResourceBundle{
    
    @Override
    public Object[][] getContents() {
        return contents;
    }
    
    /**List of messages in the specific language*/
    static final Object[][] contents = {
        //preferences localized
        { PreferencesAS2.SERVER_HOST, "Server host" },
        { PreferencesAS2.SERVER_RMI_PORT, "Server RMI port" },
        { PreferencesAS2.DIR_MSG, "Nachrichtenverzeichnis" },
                
        {"button.ok", "Ok" },
        {"button.cancel", "Abbrechen" },
        {"button.modify", "Bearbeiten" },
        {"button.browse", "Durchsuchen"},
                
        {"filechooser.selectdir", "Bitte wählen Sie das zu setzene Verzeichnis" },
                
        {"title", "Einstellungen" },
        {"tab.language", "Sprache" },
        {"tab.dir", "Verzeichnisse" },
        {"tab.security", "Sicherheit" },
        {"tab.proxy", "Proxy" },
        {"tab.misc", "Allgemein" },
        {"tab.maintenance", "Systempflege" },    
        {"tab.notification", "Benachrichtigungen" },
        {"tab.interface", "Module" },
                
        {"header.dirname", "Typ" },
        {"header.dirvalue", "Verzeichnis" },
                
        {"label.keystore.https.pass", "Keystore Passwort (zum Senden via Https):" },
        {"label.keystore.pass", "Keystore Password (Verschlüsselung/digitale Signatur):" },        
        {"label.keystore.https", "Keystore (zum Senden via Https):" },
        {"label.keystore.encryptionsign", "Keystore( Veschl., Signatur):" },
        {"label.proxy.url", "Proxy URL:" },
        {"label.proxy.user", "Proxy Login Benutzer:" },
        {"label.proxy.pass", "Proxy Login Passwort:" },
        {"label.proxy.use", "Proxy für ausgehende HTTP/HTTPs Verbindungen benutzen" },
        {"label.proxy.useauthentification", "Authentifizierung für Proxy benutzen" },
                
        {"filechooser.keystore", "Bitte wählen Sie die Keystore Datei (JKS Format)." },
                
        {"label.days", "Tage" },
        {"label.deletemsgolderthan", "Automatisches Löschen von Nachrichten, die älter sind als" },
        {"label.deletemsglog", "Logeinträge für automatisch gelöschte Nachrichten" },
        {"label.deletestatsolderthan", "Automatisches Löschen von Statistikdaten, die älter sind als"},
                
        {"label.asyncmdn.timeout", "Maximale Wartezeit auf asynchrone MDNs:" },
        {"label.httpsend.timeout", "HTTP(s) Sende-Timeout:" },
        {"label.min", "Minuten" },
        {"receipt.subdir", "Unterverzeichnisse pro Partner für Nachrichtenempfang anlegen" },
        
        //notification
        {"checkbox.notifycertexpire", "Vor dem Auslaufen von Zertifikaten" },
        {"checkbox.notifytransactionerror", "Nach Fehlern in Transaktionen" },
        {"checkbox.notifycem", "Ereignisse beim Zertifikataustausch (CEM)" },
        {"checkbox.notifyfailure", "Nach Systemproblemen"},
        {"checkbox.notifyresend", "Nach abgewiesenen Resends"},
        {"button.testmail","Sende Test Mail"},
        {"label.mailhost", "Mailserver:" },
        {"label.mailport", "Port:" },
        {"label.mailaccount", "Mailserver Account:" },
        {"label.mailpass", "Mailserver Passwort:" },
        {"label.notificationmail", "Benachrichtigungsempfänger eMail:" },
        {"label.replyto", "Replyto Addresse:" },
        {"label.smtpauthentication", "SMTP Authentifizierung benutzen" },
        {"label.smtpauthentication.user", "Benutzername:" },
        {"label.smtpauthentication.pass", "Passwort:" },
        {"testmail.message.success", "Eine Test-eMail wurde erfolgreich versandt." },
        {"testmail.message.error", "Fehler beim Senden der Test-eMail:\n{0}" },
        {"testmail.title", "Senden einer Test-eMail" },
        //interface
        {"label.showhttpheader", "Anzeige der HTTP Header Konfiguration bei den Partnereinstellungen" },
        {"label.showquota", "Anzeige der Benachrichtigungskonfiguration bei den Partnereinstellungen" },
        {"label.cem", "Zertifikataustausch erlauben (CEM)"},
        {"label.outboundstatusfiles", "Statusdateien für ausgehende Transaktionen schreiben"},
        {"info.restart.client", "Sie müssen den Client neu starten, damit diese Änderungen gültig werden!" },
        {"remotedir.select", "Verzeichnis auf dem Server wählen" },
    };
    
}