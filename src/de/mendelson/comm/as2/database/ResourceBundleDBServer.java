//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/database/ResourceBundleDBServer.java,v 1.1 2012/04/18 14:10:29 heller Exp $
package de.mendelson.comm.as2.database;
import de.mendelson.util.MecResourceBundle;
/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */

/** 
 * ResourceBundle to localize the mendelson business integration  - if you want to localize 
 * eagle to your language, please contact us: localize@mendelson.de
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class ResourceBundleDBServer extends MecResourceBundle{

  public Object[][] getContents() {
    return contents;
  }

  /**List of messages in the specific language*/
  static final Object[][] contents = {
              
    {"server.started", "{0} started." },
        
    {"update.versioninfo", "Automatic DB updater: Found DB version is {0}"
        + ", the required DB version is {1}."},
    {"update.progress", "Incremental updating database ..." },
    {"update.progress.version.start", "Starting DB update to version {0}..." },
    {"update.progress.version.end", "Updated DB to version {0}." },
    {"update.error", "FATAL: impossible to update database "
        + " from version {0} to {1}.\n"
        + "Please delete the entire database by deleting"
        + " the related database files in the install directory.\n"
        + "After this all your user defined data will be lost." },
    {"update.successfully", "DB {0}: Update to the necessary version has been finished successfully." },
    {"update.notfound", "For the update, the file update{0}to{1}.sql and/or "
    + "Update{0}to{1}.class must exists in the directory {2}." },
    {"upgrade.required", "An upgrade is required.\nPlease execute as2upgrade.bat or as2upgrade.sh before starting the server." },
        
  };		
  
}