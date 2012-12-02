//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/preferences/ResourceBundlePreferences_fr.java,v 1.1 2012/04/18 14:10:35 heller Exp $
package de.mendelson.comm.as2.preferences;
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
public class ResourceBundlePreferences_fr extends MecResourceBundle{
    
    @Override
    public Object[][] getContents() {
        return contents;
    }
    
    /**List of messages in the specific language*/
    static final Object[][] contents = {
        //preferences localized
        { PreferencesAS2.SERVER_HOST, "Hôte serveur" },
        { PreferencesAS2.SERVER_RMI_PORT, "Port du serveur RMI" },
        { PreferencesAS2.DIR_MSG, "Archivage message" },
                
        {"button.ok", "Valider" },
        {"button.cancel", "Annuler" },
        {"button.modify", "Modifier" },
        {"button.browse", "Parcourir..."},
                
        {"filechooser.selectdir", "Sélectionner un répertoire" },
                
        {"title", "Préférences" },
        {"tab.language", "Langage" },
        {"tab.dir", "Répertoires" },
        {"tab.security", "Sécurité" },
        {"tab.proxy", "Proxy" },
        {"tab.misc", "Divers" },
        {"tab.maintenance", "Maintenance" },
        {"tab.notification", "Notification" },
        {"tab.interface", "Modules" },

        {"header.dirname", "Type" },
        {"header.dirvalue", "Rép." },

        {"label.keystore.https.pass", "Mot de passe du porte-clef (envoi https):" },
        {"label.keystore.pass", "Mot de passe du porte-clef (encryption/signature):" },
        {"label.keystore.https", "Porte-clef (envoi https):" },
        {"label.keystore.encryptionsign", "Porte-clef (enc, sign):" },
        {"label.proxy.url", "URL du proxy:" },
        {"label.proxy.user", "Login utilisateur du proxy:" },
        {"label.proxy.pass", "Mot de passe utilisateur du proxy:" },
        {"label.proxy.use", "Utiliser un proxy pour les connexions sortante HTTP/HTTPs" },
        {"label.proxy.useauthentification", "Utiliser l''authentification auprès du proxy" },
                
        {"filechooser.keystore", "Merci de sélectionner le fichier porte-clef (format jks)." },
                
        {"label.days", "jours" },
        {"label.deletemsgolderthan", "Supprimer automatiquement les messages plus vieux que" },
        {"label.deletemsglog", "Tenir informer dans le log à propos des messages automatiquement supprimés" },
        {"label.deletestatsolderthan", "Supprimer automatiquement les statistiques qui sont plus vieux que"},
        {"label.asyncmdn.timeout", "Temps d''attente maximal pour un MDN asynchrone:" },
        {"label.httpsend.timeout", "Timeout sur envoi HTTP(s):" },
        {"label.min", "minutes" },
        {"receipt.subdir", "Créer des sous-répertoires par partenaires pour les messages reçus" },
        
        //notification
        {"checkbox.notifycertexpire", "Notifier l''expiration de certificats" },
        {"checkbox.notifytransactionerror", "Notifier les erreurs de transaction" },
        {"checkbox.notifycem", "Notifier des événements d'échange certificats (CEM)" },
        {"checkbox.notifyfailure", "Notifier les problems système"},
        {"checkbox.notifyresend", "Notifier renvoie rejetés"},
        {"button.testmail","Envoyer un e-mail de test"},
        {"label.mailhost", "Hôte du serveur de mail :" },
        {"label.mailport", "Port:" },
        {"label.mailaccount", "Compte sur le serveur de mail:" },
        {"label.mailpass", "Mot de passe sur le serveur de mail:" },
        {"label.notificationmail", "Adresse de notification du destinataire:" },
        {"label.replyto", "Adresse de réponse (Replyto):" },
        {"label.smtpauthentication", "Authentification d'utilisation SMTP" },
        {"label.smtpauthentication.user", "Nom d'utilisateur:" },
        {"label.smtpauthentication.pass", "Mot de passe:" },
        {"testmail.message.success", "E-mail de test envoyé avec succés." },
        {"testmail.message.error", "Erreur lors de l''envoi de l''e-mail de test:\n{0}" },
        {"testmail.title", "Résultat de l''envoi de l''email de test" },
        //interface
        {"label.showhttpheader", "Laissez configurer les en-têtes de HTTP dans la configuration d''associé" },
        {"label.showquota", "Laissez configurer l''avis de quote-part dans la configuration d''associé" },
        {"label.cem", "Permettre l''échange de certificat (CEM)"},
        {"label.outboundstatusfiles", "Écrire des fichiers de statut de transaction sortante"},
        {"info.restart.client", "Un redémarrage du client est requise pour effectuer ces modifications valide!" },
        {"remotedir.select", "Sélectionnez le répertoire sur le serveur" },
    };
    
}
