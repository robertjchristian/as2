//$Header: /cvsroot-fuse/mec-as2/39/mendelson/util/clientserver/gui/ResourceBundleLogin_de.java,v 1.1 2012/04/18 14:10:44 heller Exp $
package de.mendelson.util.clientserver.gui;
import de.mendelson.util.MecResourceBundle;
/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */


/** 
 * ResourceBundle to localize the mendelson products - if you want to localize 
 * eagle to your language, please contact us: localize@mendelson.de
 * @author  S.Heller
 * @version $Revision: 1.1 $
 */
public class ResourceBundleLogin_de extends MecResourceBundle{

  public Object[][] getContents() {
    return contents;
  }

   /**List of messages in the specific language*/
  static final Object[][] contents = {
    //dialog
    {"button.ok", "Ok" },    
    {"button.cancel", "Abbrechen" },    
    {"label.user", "Benutzer:" },
    {"label.passwd", "Passwort:" },
    {"title.login", "Anmeldung" },
    
  };		
  
}