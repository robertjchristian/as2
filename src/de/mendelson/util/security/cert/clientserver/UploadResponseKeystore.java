//$Header: /cvsroot-fuse/mec-as2/39/mendelson/util/security/cert/clientserver/UploadResponseKeystore.java,v 1.1 2012/04/18 14:10:47 heller Exp $
package de.mendelson.util.security.cert.clientserver;

import de.mendelson.util.clientserver.clients.datatransfer.UploadResponseFile;
import java.io.Serializable;
/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */

/**
 * Msg for the client server protocol
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class UploadResponseKeystore extends UploadResponseFile implements Serializable {

    public UploadResponseKeystore(UploadRequestKeystore request) {
        super(request);
    }

    @Override
    public String toString() {
        return ("Upload response keystore");
    }
}
