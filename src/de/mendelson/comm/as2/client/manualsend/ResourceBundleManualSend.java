//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/client/manualsend/ResourceBundleManualSend.java,v 1.1 2012/04/18 14:10:24 heller Exp $ 
package de.mendelson.comm.as2.client.manualsend;
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
public class ResourceBundleManualSend extends MecResourceBundle{

    @Override
  public Object[][] getContents() {
    return contents;
  }

  /**List of messages in the specific language*/
  static final Object[][] contents = {
        
    {"button.ok", "Ok" },
    {"button.cancel", "Cancel" },
    {"button.browse", "Browse" },
    {"label.filename", "Filename:" },
    {"label.partner", "Receiver:" }, 
    {"label.localstation", "Local station:" }, 
    {"label.selectfile", "Please select the file to send" },
    {"title", "Send file to partner" },
    {"send.success", "The file has been enqueued to the send process successfully." },
    {"send.failed", "The file has not been enqueued to the send process because of an error." },
  };		
  
}