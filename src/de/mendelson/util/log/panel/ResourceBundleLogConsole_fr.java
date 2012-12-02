//$Header: /cvsroot-fuse/mec-as2/39/mendelson/util/log/panel/ResourceBundleLogConsole_fr.java,v 1.1 2012/04/18 14:10:45 heller Exp $
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
 * @author E.Pailleau
 * @version $Revision: 1.1 $
 */
public class ResourceBundleLogConsole_fr extends MecResourceBundle{

  public Object[][] getContents() {
    return contents;
  }

  /**List of messages in the specific language*/
  static final Object[][] contents = {

      {"title", "Console" },
      {"label.clear", "Nettoyer" },
      {"label.toclipboard", "Copier le log vers le presse-papier" },
      {"label.tofile", "Sauver le log vers un fichier" },
      {"filechooser.logfile", "Merci de sélectionner un fichier pour l''enregistrement du log." },
      {"write.success", "Log enregistré avec succès dans \"{0}\"." },
      {"write.failure", "Erreur d''écriture du log vers le fichier: {0}." },        
  };		
  
}
