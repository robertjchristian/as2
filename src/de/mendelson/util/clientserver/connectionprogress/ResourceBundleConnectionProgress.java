//$Header: /cvsroot-fuse/mec-as2/39/mendelson/util/clientserver/connectionprogress/ResourceBundleConnectionProgress.java,v 1.1 2012/04/18 14:10:44 heller Exp $
package de.mendelson.util.clientserver.connectionprogress;
import de.mendelson.util.MecResourceBundle;
/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */


/** 
 * Localize a mandelson product
 * @author  S.Heller
 * @version $Revision: 1.1 $
 */
public class ResourceBundleConnectionProgress extends MecResourceBundle{

    @Override
  public Object[][] getContents() {
    return contents;
  }

   /**List of messages in the specific language*/
  static final Object[][] contents = {
    {"connecting.to", "Connecting to {0}..." },
  };		
  
}