//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/importexport/ResourceBundleImportConfiguration.java,v 1.1 2012/04/18 14:10:30 heller Exp $ 
package de.mendelson.comm.as2.importexport;

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
public class ResourceBundleImportConfiguration extends MecResourceBundle {

    public Object[][] getContents() {
        return contents;
    }
    /**List of messages in the specific language*/
    static final Object[][] contents = {
        {"button.import", "Import!"},
        {"button.cancel", "Cancel"},
        {"title", "Configuration import"},
        {"import.info", "Please select the configuration and partner you would like to import."},
        {"label.propertiesimport", "Import server properties (proxy, keystore pathes, language)"},
        {"label.notificationimport", "Import notification data (mail account, general notification options)"},
        {"invalid.importfile", "This is not a valid import file."},
        {"header.name", "Name"},
        {"header.as2id", "AS2 id"},
        {"import.success.msg", "The configuration data has been imported successful." },
        {"import.success.title", "Success" },
        {"import.failed.msg", "An error occured during the import: \"{0}\"" },
        {"import.failed.title", "Import failed" },
        {"title.partner", "Partner" },
        {"title.config", "Configuration" },
        {"partner.all", "All partner" },
        {"partner.none", "No partner" },
    };
}
