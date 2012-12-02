//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/partner/gui/ResourceBundlePartnerPanel.java,v 1.1 2012/04/18 14:10:32 heller Exp $
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
public class ResourceBundlePartnerPanel extends MecResourceBundle {

    @Override
    public Object[][] getContents() {
        return contents;
    }
    /**List of messages in the specific language*/
    static final Object[][] contents = {
        {"title", "Partner configuration"},
        {"label.name", "Name:"},
        {"label.id", "AS2 id:"},
        {"label.partnercomment", "Comment:" },
        {"label.url", "Receipt URL:"},
        {"label.mdnurl", "MDN URL:"},
        {"label.signalias.key", "Private key (signature):"},
        {"label.cryptalias.key", "Private key (encryption):"},
        {"label.signalias.cert", "Partner certificate (signature):"},
        {"label.cryptalias.cert", "Partner certificate (encryption):"},
        {"label.signtype", "Digital signature algorithm:"},
        {"label.encryptiontype", "Message encryption algorithm:"},
        {"label.email", "EMail address:"},
        {"label.localstation", "Local station"},
        {"label.compression", "Compress outbound messages (requires AS2 1.1 partner solution)"},
        {"label.usecommandonreceipt", "Command on msg receipt:"},
        {"label.usecommandonsenderror", "Command on msg send (error):"},
        {"label.usecommandonsendsuccess", "Command on msg send (success):"},
        {"label.keepfilenameonreceipt", "Keep original file name on receipt (if sender added this information)"},
        {"tab.misc", "Misc"},
        {"tab.security", "Security"},
        {"tab.send", "Send"},
        {"tab.mdn", "MDN"},
        {"tab.dirpoll", "Directory polling"},
        {"tab.receipt", "Receipt"},
        {"tab.httpauth", "HTTP authentication"},
        {"tab.httpheader", "HTTP header"},
        {"tab.notification", "Notification" },
        {"tab.events", "Events" },
        {"tab.partnersystem", "Info" },
        {"label.subject", "Payload subject:"},
        {"label.contenttype", "Payload content type:"},
        {"label.syncmdn", "Request sync MDN"},
        {"label.asyncmdn", "Request async MDN"},
        {"label.signedmdn", "Request signed MDN"},
        {"label.polldir", "Poll directory:"},
        {"label.pollinterval", "Poll interval:"},
        {"label.pollignore", "Poll ignore files:"},
        {"label.maxpollfiles", "Max files per poll:"},
        {"label.usehttpauth", "Use HTTP authentication to send AS2 messages"},
        {"label.usehttpauth.user", "Username:"},
        {"label.usehttpauth.pass", "Password:"},
        {"label.usehttpauth.asyncmdn", "Use HTTP authentication to send async MDN"},
        {"label.usehttpauth.asyncmdn.user", "Username:"},
        {"label.usehttpauth.asyncmdn.pass", "Password:"},
        {"hint.filenamereplacement.receipt1", "Replacements: $'{'filename}, $'{'subject},"},
        {"hint.filenamereplacement.receipt2", "$'{'sender}, $'{'receiver}, $'{'messageid}."},
        {"hint.replacement.send1", "Replacements: $'{'filename}, $'{'fullstoragefilename}, $'{'log}, $'{'subject},"},        
        {"hint.replacement.send2", "$'{'sender}, $'{'receiver}, $'{'messageid}, $'{'mdntext}."},
        {"hint.subject.replacement", "The pattern $'{'filename} will be replaced by the send filename."},
        {"hint.keepfilenameonreceipt", "Please ensure your partner sends unique file names before enabling this option!"},
        {"label.url.hint", "Please start this URL with the protocol \"http://\" or \"https://\"."},
        {"label.notify.send", "Notify if send message quota exceeds" },
        {"label.notify.receive", "Notify if receive message quota exceeds" },
        {"label.notify.sendreceive", "Notify if receive and send message quota exceeds" },
        {"header.httpheaderkey", "Name" },
        {"header.httpheadervalue", "Value" },
        {"httpheader.add", "Add" },
        {"httpheader.delete", "Remove" },
        {"label.as2version", "AS2 version:" },
        {"label.productname", "Product name:" },
        {"label.features", "Features:" },
        {"label.features.cem", "Certificate exchange via CEM" },
        {"label.features.ma", "Multiple attachments" },
        {"label.features.compression", "Compression" },
        {"partnerinfo", "Your trading partner transmits with every AS2 message some informations about his AS2 system capabilities. This is a list of features that has been transmitted by your partner." },
        {"partnersystem.noinfo", "No info available - has there been already a transaction?" },
        {"label.httpversion", "HTTP protocol version:" },
    };
}
