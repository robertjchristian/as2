//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/notification/ResourceBundleNotification_fr.java,v 1.1 2012/04/18 14:10:31 heller Exp $
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
 * @author E.Pailleau
 * @version $Revision: 1.1 $
 */
public class ResourceBundleNotification_fr extends MecResourceBundle{

  public Object[][] getContents() {
    return contents;
  }

   /**List of messages in the specific language*/
  static final Object[][] contents = {
    //dialog
    {"test.message.send", "Un e-mail de test a été envoyé à {0}." },
    {"transaction.message.send", "{0}: Un e-mail de notification d''erreur de transaction a été envoyé à {1}." },
    {"transaction.message.send.error", "{0}: Envoyer d''une information sur une erreur de transaction à {1} a manqué: {2}"},
    {"misc.message.send", "Un e-mail de notification a été envoyé à {0}."},
    {"cert.message.send", "Un e-mail de notification d''expiration de certificat a été envoyé à {0} [{1}]." },
    {"quota.send.message.send", "Un e-mail de notification de dépassement de quota sur émission a été envoyé à {0}." },
    {"quota.receive.message.send", "Un e-mail de notification de dépassement de quota sur réception a été envoyé à {0}." },
    {"quota.sendreceive.message.send", "Un e-mail de notification de dépassement de quota sur émission+réception a été envoyé à {0}." },
  };		
  
}
