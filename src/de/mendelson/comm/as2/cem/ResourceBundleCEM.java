//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/cem/ResourceBundleCEM.java,v 1.1 2012/04/18 14:10:17 heller Exp $
package de.mendelson.comm.as2.cem;
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
public class ResourceBundleCEM extends MecResourceBundle{
    
    @Override
    public Object[][] getContents() {
        return contents;
    }
    
    /**List of messages in the specific language*/
    static final Object[][] contents = {                
        {"cem.validated.schema", "{0}: The CEM has been validated successfully." },
        {"cert.already.imported", "{0}: The submitted CEM certificate does already exist in the underlaying keystore (alias {1}), the import has been skipped."},
        {"cert.imported.success", "{0}: The submitted CEM certificate has been imported sucessfully to the underlaying keystore (alias {1})."},
        {"category." + CEMEntry.CATEGORY_CRYPT, "Encryption" },
        {"category." + CEMEntry.CATEGORY_SIGN, "Signature" },
        {"category." + CEMEntry.CATEGORY_SSL, "SSL" },
        {"state." + CEMEntry.STATUS_ACCEPTED_INT, "Accepted by {0}" },
        {"state." + CEMEntry.STATUS_PENDING_INT, "No answer so far from {0}" },
        {"state." + CEMEntry.STATUS_REJECTED_INT, "Rejected by {0}" },
        {"state." + CEMEntry.STATUS_CANCELED_INT, "Canceled" },
        {"state." + CEMEntry.STATUS_PROCESSING_ERROR_INT, "Processing error" },
        {"cemtype.response", "{0}: The CEM message is a certificate response" },
        {"cemtype.request", "{0}: The CEM message is a certificate request" },
        {"cem.response.relatedrequest.found", "{0}: The CEM response refers to the existing request \"{1}\"" },
        {"cem.response.prepared", "{0}: CEM response message has been created for the request {1}" },
    };
    
}