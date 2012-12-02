//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/notification/ResourceBundleNotification.java,v 1.1 2012/04/18 14:10:31 heller Exp $
package de.mendelson.comm.as2.notification;
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
public class ResourceBundleNotification extends MecResourceBundle{

    @Override
  public Object[][] getContents() {
    return contents;
  }

   /**List of messages in the specific language*/
  static final Object[][] contents = {
    //dialog
    {"test.message.send", "A test email has been sent to {0}." },
    {"transaction.message.send", "{0}: A transaction error notification mail has been sent to {1}." },
    {"transaction.message.send.error", "{0}: Sending a transaction error notification mail to {1} failed: {2}." },
    {"misc.message.send", "A notification mail has been sent to {0}." },
    {"cert.message.send", "A certificate expire notification mail has been sent to {0} [{1}]." },
    {"quota.send.message.send", "A send quota exceed notification mail has been sent to {0}." },
    {"quota.receive.message.send", "A receive quota exceed notification mail has been sent to {0}." },
    {"quota.sendreceive.message.send", "A send+receive quota exceed notification mail has been sent to {0}." },
  };		
  
}