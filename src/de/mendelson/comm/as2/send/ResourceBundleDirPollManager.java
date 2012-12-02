//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/send/ResourceBundleDirPollManager.java,v 1.1 2012/04/18 14:10:35 heller Exp $
package de.mendelson.comm.as2.send;
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
public class ResourceBundleDirPollManager extends MecResourceBundle{
    
    @Override
    public Object[][] getContents() {
        return contents;
    }
    
    /**List of messages in the specific language*/
    static final Object[][] contents = {
        {"manager.started", "Directory poll manager started." },
        {"poll.stopped", "Directory poll manager: Poll for relationship \"{0}/{1}\" stopped." },
        {"poll.started", "Directory poll manager: Poll for relationship \"{0}/{1}\" started. Ignore files: \"{2}\". Poll interval: {3}s" },
        {"warning.ro", "Outbox file {0} is read-only, ignoring." },
        {"warning.notcomplete", "Outbox file {0} is not complete so far, ignoring." },
        {"messagefile.deleted", "{0}: The file \"{1}\" has been deleted and enqueued into the processing message queue of the server." },
        {"processing.file", "Processing the file \"{0}\" for the relationship \"{1}/{2}\"." },
        {"processing.file.error", "Error processing the file \"{0}\" for the relationship  \"{1}/{2}\": \"{3}\"." },
    };
    
}