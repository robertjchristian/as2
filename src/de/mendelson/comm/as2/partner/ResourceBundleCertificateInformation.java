//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/partner/ResourceBundleCertificateInformation.java,v 1.1 2012/04/18 14:10:32 heller Exp $
package de.mendelson.comm.as2.partner;

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
public class ResourceBundleCertificateInformation extends MecResourceBundle {

    @Override
    public Object[][] getContents() {
        return contents;
    }
    /**List of messages in the specific language*/
    static final Object[][] contents = {
        {"localstation.decrypt.prio1", "Inbound messages for the local station \"{0}\" will be decrypted using the certificate \"{1}\"."},
        {"localstation.decrypt.prio2", "\"{0}\" is also prepared to decrypt inbound messages using the certificate \"{1}\"."},
        {"localstation.sign.prio1", "Outbound messages from the local station \"{0}\" will be signed using the certificate \"{1}\"."},
        {"partner.encrypt.prio1", "Outbound messages to the partner \"{0}\" will be encrypted using the certificate \"{1}\"."},
        {"partner.sign.prio1", "Inbound message signatures from the partner \"{0}\" will be verified using the certificate \"{1}\"."},
        {"partner.sign.prio2", "The system is also prepared to verify inbound message signatures from \"{0}\" using the certificate \"{1}\"."},};
}
