//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/partner/gui/ResourceBundlePartnerPanel_de.java,v 1.1 2012/04/18 14:10:32 heller Exp $
package de.mendelson.comm.as2.partner.gui;
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
public class ResourceBundlePartnerPanel_de extends MecResourceBundle{
    
    @Override
    public Object[][] getContents() {
        return contents;
    }
    
    /**List of messages in the specific language*/
    static final Object[][] contents = {
        {"title", "Partnerkonfiguration" },
        {"label.name", "Name:" },
        {"label.id", "AS2 id:" },
        {"label.partnercomment", "Kommentar:" },
        {"label.url", "Empfangs-URL:" },
        {"label.mdnurl", "MDN URL:" },
        {"label.signalias.key", "Privater Schlüssel (Digitale Signatur):" },
        {"label.cryptalias.key", "Privater Schlüssel (Verschlüsselung):" },
        {"label.signalias.cert", "Partnerzertifikat (Digitale Signatur):" },
        {"label.cryptalias.cert", "Partnerzertifikat (Verschlüsselung):" },
        {"label.signtype", "Digitale Signatur:" },
        {"label.encryptiontype", "Nachrichtenverschlüsselung:" },
        {"label.email", "EMail Adresse:" },
        {"label.localstation", "Lokale Station" },       
        {"label.compression", "Ausgehende Nachrichten komprimieren (benötigt AS2 1.1 Gegenstelle)" }, 
        {"label.usecommandonreceipt", "Kommando nach Empfang:" },
        {"label.usecommandonsenderror", "Kommando nach Versand (fehlerhaft):"},
        {"label.usecommandonsendsuccess", "Kommando nach Versand (erfolgreich):"},
        {"label.keepfilenameonreceipt", "Original Dateiname beibehalten (Wenn der Sender diese Information zur Verfügung stellt)"},
        {"tab.misc", "Allgemein" },
        {"tab.security", "Sicherheit" },
        {"tab.send", "Versand" },
        {"tab.mdn", "MDN" },   
        {"tab.dirpoll", "Verzeichnisüberwachung" }, 
        {"tab.receipt", "Empfang" },  
        {"tab.httpauth", "HTTP Authentifizierung" },
        {"tab.httpheader", "HTTP Header"},
        {"tab.notification", "Benachrichtigung" },
        {"tab.events", "Ereignisse" },
        {"tab.partnersystem", "Info" },
        {"label.subject", "Nutzdaten Subject:" },
        {"label.contenttype", "Nutzdaten Content Type:" },
        {"label.syncmdn", "Sychrone Empfangsbestätigung (MDN) anfordern" },
        {"label.asyncmdn", "Asychrone Empfangsbestätigung (MDN) anfordern" },
        {"label.signedmdn", "Signierte Empfangsbestätigung (MDN) anfordern" },
        {"label.polldir", "Überwachtes Verzeichnis:" },
        {"label.pollinterval", "Abholintervall:" },
        {"label.pollignore", "Abholen ignorieren für:" },        
        {"label.maxpollfiles", "Maximale Dateianzahl pro Abholvorgang:"},
        {"label.usehttpauth", "Benutze HTTP Authentifizierung beim Senden von AS2 Nachrichten" },
        {"label.usehttpauth.user", "Benutzername:" },
        {"label.usehttpauth.pass", "Passwort:" },
        {"label.usehttpauth.asyncmdn", "Benutze HTTP Authentifizierung beim Senden von async MDN" },
        {"label.usehttpauth.asyncmdn.user", "Benutzername:" },
        {"label.usehttpauth.asyncmdn.pass", "Passwort:" },        
        {"hint.filenamereplacement.receipt1", "Ersetzungen: $'{'filename}, $'{'subject}," },
        {"hint.filenamereplacement.receipt2", "$'{'sender}, $'{'receiver}, $'{'messageid}" },
        {"hint.replacement.send1", "Ersetzungen: $'{'filename}, $'{'fullstoragefilename}, $'{'log}, $'{'subject}," },        
        {"hint.replacement.send2", "$'{'sender}, $'{'receiver}, $'{'messageid}, $'{'mdntext}." },
        {"hint.subject.replacement", "$'{'filename} wird durch den Sendedateinamen ersetzt."},
        {"hint.keepfilenameonreceipt", "Empfangene Dateinamen müssen eindeutig sein, wenn diese Option eingeschaltet ist!"},
        {"label.url.hint", "Bitte setzen Sie vor die URL das Protokoll (\"http://\" oder \"https://\")."},
        {"label.notify.send", "Benachrichtigen, wenn das Sendekontingent folgenden Wert übersteigt:" },
        {"label.notify.receive", "Benachrichtigen, wenn das Empfangskontingent folgenden Wert übersteigt:" },
        {"label.notify.sendreceive", "Benachrichtigen, wenn das Sende/Empfangskontingent folgenden Wert übersteigt:" },
        {"header.httpheaderkey", "Name" },
        {"header.httpheadervalue", "Wert" },
        {"httpheader.add", "Hinzufügen" },
        {"httpheader.delete", "Entfernen" },
        {"label.as2version", "AS2 Version:" },
        {"label.productname", "Produktname:" },
        {"label.features", "Funktionen:" },
        {"label.features.cem", "Zertifikataustausch über CEM" },
        {"label.features.ma", "Mehrere Anhänge" },
        {"label.features.compression", "Datenkomprimierung" },
        {"partnerinfo", "Ihr Partner übermittelt mit jeder AS2 Nachricht auch Informationen über die Funktionen seines AS2 Systems. Dies ist die Liste dieser Funktionen." },
        {"partnersystem.noinfo", "Keine Information verfügbar - gab es schon eine Transaktion?" },
        {"label.httpversion", "HTTP Protokollversion:" },
    };
    
}