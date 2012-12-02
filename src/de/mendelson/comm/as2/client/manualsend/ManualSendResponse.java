//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/client/manualsend/ManualSendResponse.java,v 1.1 2012/04/18 14:10:24 heller Exp $
package de.mendelson.comm.as2.client.manualsend;

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
public class ManualSendResponse extends UploadResponseFile implements Serializable {

    public ManualSendResponse(ManualSendRequest request) {
        super(request);
    }

    @Override
    public String toString() {
        return ("Manual send response");
    }
}
