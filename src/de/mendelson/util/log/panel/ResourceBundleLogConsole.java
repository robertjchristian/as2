//$Header: /cvsroot-fuse/mec-as2/39/mendelson/util/log/panel/ResourceBundleLogConsole.java,v 1.1 2012/04/18 14:10:45 heller Exp $ 
package de.mendelson.util.log.panel;
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
public class ResourceBundleLogConsole extends MecResourceBundle{

  public Object[][] getContents() {
    return contents;
  }

  /**List of messages in the specific language*/
  static final Object[][] contents = {

      {"title", "Console" },
      {"label.clear", "Clear" },
      {"label.toclipboard", "Copy log to clipboard" },
      {"label.tofile", "Save log to file" },
      {"filechooser.logfile", "Please select the file to write the log to." },
      {"write.success", "Log written successfully to \"{0}\"." },
      {"write.failure", "Error writing log to file: {0}." },        
  };		
  
}