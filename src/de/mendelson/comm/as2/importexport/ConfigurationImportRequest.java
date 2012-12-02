//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/importexport/ConfigurationImportRequest.java,v 1.1 2012/04/18 14:10:30 heller Exp $
package de.mendelson.comm.as2.importexport;

import de.mendelson.comm.as2.partner.Partner;
import de.mendelson.util.clientserver.clients.datatransfer.UploadRequestFile;
import java.io.Serializable;
import java.util.List;
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
public class ConfigurationImportRequest extends UploadRequestFile implements Serializable{

    private List<Partner> partnerListToImport;
    private boolean importNotification = false;;
    private boolean importServerSettings = false;

    @Override
    public String toString(){
        return( "Import request" );
    }

    /**
     * @return the partnerListToImport
     */
    public List<Partner> getPartnerListToImport() {
        return partnerListToImport;
    }

    /**
     * @param partnerListToImport the partnerListToImport to set
     */
    public void setPartnerListToImport(List<Partner> partnerListToImport) {
        this.partnerListToImport = partnerListToImport;
    }

    /**
     * @return the importNotification
     */
    public boolean getImportNotification() {
        return importNotification;
    }

    /**
     * @param importNotification the importNotification to set
     */
    public void setImportNotification(boolean importNotification) {
        this.importNotification = importNotification;
    }

    /**
     * @return the importServerSettings
     */
    public boolean getImportServerSettings() {
        return importServerSettings;
    }

    /**
     * @param importServerSettings the importServerSettings to set
     */
    public void setImportServerSettings(boolean importServerSettings) {
        this.importServerSettings = importServerSettings;
    }

}
