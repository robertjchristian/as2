//$Header: /cvsroot-fuse/mec-as2/39/mendelson/util/IMecFileChooser.java,v 1.1 2012/04/18 14:10:41 heller Exp $
package de.mendelson.util;
import javax.swing.*;
import javax.swing.filechooser.*;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */

/** 
 * Interface to keep the file dialog selectable: native or swing like
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public interface IMecFileChooser{
  
  /**Sets the mode of the dialog: load*/
  public void setTypeLoad();
  
  /**Sets the mode of the dialog: load*/
  public void setTypeSave();

  /**Browses for a filename and returns it
  * @return null if the user cancels the action!
  */
  public String browseFilename();
  
  
  /**Browses the directory for a filename
   * @param component JComponent where the chosen filename will displayed, may be null
   * @param filter FileFilters that are accepted
   */
  public String browseFilename( JComponent component,
                              String[] filter );
  
  /**Browses the directory for a filename, all files are accepted
   * @param component JComponent where the chosen filename will displayed, may be null
   */
  public String browseFilename( JComponent component );
  
  /**Browses directories ONLY, no file selection allowed
  * @param component Component where the chosen filename will displayed, may be null
  */
  public String browseDirectory( JComponent component );
  
  /**Browses directories ONLY, no file selection allowed
  */
  public String browseDirectory();
  
  /**Sets the default directory to the chooser
  * @param defaultDirectory Defautl directory to set */
  public void setDefaultDirectory( String defaultDirectory );
  
  /**Set a new file view to the chooser, e.g. to display new icons etc*/
  public void setFileView( FileView fileview );
  
}
