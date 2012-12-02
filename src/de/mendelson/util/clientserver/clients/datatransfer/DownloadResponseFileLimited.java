//$Header: /cvsroot-fuse/mec-as2/39/mendelson/util/clientserver/clients/datatransfer/DownloadResponseFileLimited.java,v 1.1 2012/04/18 14:10:41 heller Exp $
package de.mendelson.util.clientserver.clients.datatransfer;

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
public class DownloadResponseFileLimited extends DownloadResponseFile implements Serializable {

    private boolean sizeExceeded = false;

    public DownloadResponseFileLimited(DownloadRequestFileLimited request) {
        super(request);
    }

    /**
     * @return the exceeded
     */
    public boolean isSizeExceeded() {
        return sizeExceeded;
    }

    /**
     * @param exceeded the exceeded to set
     */
    public void setSizeExceeded(boolean exceeded) {
        this.sizeExceeded = exceeded;
    }



}
