//$Header: /cvsroot-fuse/mec-as2/39/mendelson/util/rmi/ResourceBundleRMISender.java,v 1.1 2012/04/18 14:10:45 heller Exp $
package de.mendelson.util.rmi;
import de.mendelson.util.MecResourceBundle;
/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */


/** 
 * ResourceBundle to localize the RMISender messages
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class ResourceBundleRMISender extends MecResourceBundle{

  public Object[][] getContents() {
    return contents;
  }

  /**List of messages in the specific language*/
  static final Object[][] contents = {
      {"fatal.error.short", "Warning" },
      {"fatal.error.long", 
      "Warning on the server site, request could not be executed!" },
      {"message.returned", "Error message returned by the call:" },
      {"no.server", "Unable to detect a running server on {0}:{1}."},
  };		
  
}