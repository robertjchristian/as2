//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/message/ResourceBundleAS2MessageParser_de.java,v 1.1 2012/04/18 14:10:30 heller Exp $
package de.mendelson.comm.as2.message;

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
public class ResourceBundleAS2MessageParser_de extends MecResourceBundle {

    @Override
    public Object[][] getContents() {
        return contents;
    }
    /**List of messages in the specific language*/
    static final Object[][] contents = {
        {"mdn.incoming", "{0}: Eingehende Übertragung ist eine Empfangsbestätigung (MDN)."},
        {"mdn.answerto", "{0}: Empfangsbestätigung (MDN) ist die Antwort auf die AS2 Nachricht \"{1}\"."},
        {"mdn.state", "{0}: Status der Empfangsbestätigung (MDN) ist [{1}]."},
        {"mdn.details", "{0}: Details der Empfangsbestätigung (MDN) des entfernten AS2 Servers: {1}"},
        {"msg.incoming", "{0}: Eingehende Übertragung ist eine AS2 Nachricht, Rohdatengrösse: {1}"},
        {"mdn.signed", "{0}: Empfangsbestätigung (MDN) ist digital signiert."},
        {"mdn.unsigned.error", "{0}: Empfangsbestätigung (MDN) ist entgegen der Konfiguration des Partners \"{1}\" NICHT digital signiert."},
        {"mdn.signed.error", "{0}: Empfangsbestätigung (MDN) ist entgegen der Konfiguration des Partners \"{1}\" digital signiert."},
        {"msg.signed", "{0}: AS2 Nachricht ist digital signiert."},
        {"msg.encrypted", "{0}: AS2 Nachricht ist verschlüsselt."},
        {"msg.notencrypted", "{0}: AS2 Nachricht ist nicht verschlüsselt."},
        {"msg.notsigned", "{0}: AS2 Nachricht ist nicht signiert."},
        {"mdn.notsigned", "{0}: Empfangsbestätigung (MDN) ist nicht signiert."},
        {"signature.ok", "{0}: Digitale Signatur wurde erfolgreich überprüft."},
        {"signature.failure", "{0}: Überprüfung der digitalen Signatur schlug fehl - {1}"},
        {"signature.using.alias", "{0}: Benutze das Zertifikat \"{1}\" zum Überprüfen der digitalen Signatur."},
        {"decryption.done.alias", "{0}: Die Daten wurden mit Hilfe des Schlüssels \"{1}\" entschlüsselt."},
        {"mdn.unexpected.messageid", "{0}: Die Empfangsbestätigung (MDN) referenziert eine AS2 Nachricht der Referenznummer \"{1}\", die nicht existert."},
        {"mdn.unexpected.messageid", "{0}: Die Empfangsbestätigung (MDN) referenziert die AS2 Nachricht der Referenznummer \"{1}\", die keine MDN erwartet."},
        {"data.compressed.expanded", "{0}: Die komprimierten Nutzdaten wurden von {1} auf {2} expandiert."},
        {"found.attachments", "{0}: Es wurden {1} Anhänge mit Nutzdaten in der AS2 Nachricht gefunden."},
        {"decryption.inforequired", "{0}: Zum Entschlüsseln der Daten ist ein Schlüssel mit folgenden Parametern notwendig:\n{1}"},
        {"decryption.infoassigned", "{0}: Zum Entschlüsseln wurde ein Schlüssel mit folgenden Parametern benutzt (Alias \"{1}\"):\n{2}"},
        {"signature.analyzed.digest", "{0}: Für die digitale Signatur wurde vom Sender der Algorithmus {1} verwendet."},
        {"filename.extraction.error", "{0}: Extrahieren des Originaldateinamen ist nicht möglich: \"{1}\", wird ignoriert."},
        {"contentmic.match", "{0}: Der Message Integrity Code (MIC) stimmt mit der gesandten AS2 Nachricht überein."},
        {"contentmic.failure", "{0}: Der Message Integrity Code (MIC) stimmt nicht mit der gesandten AS2 Nachricht überein (erwartet: {1}, erhalten: {2})."},
        {"found.cem", "{0}: Die Nachricht ist eine Anfrage für einen Zertifikataustausch (CEM)."},};
}
