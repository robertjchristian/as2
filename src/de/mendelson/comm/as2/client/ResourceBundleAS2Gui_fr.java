//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/client/ResourceBundleAS2Gui_fr.java,v 1.1 2012/04/18 14:10:23 heller Exp $
package de.mendelson.comm.as2.client;

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
public class ResourceBundleAS2Gui_fr extends MecResourceBundle {

    @Override
    public Object[][] getContents() {
        return contents;
    }
    /**List of messages in the specific language*/
    static final Object[][] contents = {
        {"menu.file", "Fichier"},
        {"menu.file.exit", "Fermer"},
        {"menu.file.partner", "Partenaire"},
        {"menu.file.certificates", "Certificats"},
        {"menu.file.certificate", "Certificats"},
        {"menu.file.certificate.signcrypt", "Certificats (signature, cryptage)"},
        {"menu.file.certificate.ssl", "Certificats (SSL)"},
        {"menu.file.cem", "Certificat d'échange présentation (CEM)"},
        {"menu.file.cemsend", "Certificats d''échange avec des partenaires (CEM)"},
        {"menu.file.statistic", "Statistiques"},
        {"menu.file.quota", "Quota"},
        {"menu.file.export", "Configuration d''exportation"},
        {"menu.file.import", "Configuration d''importation"},
        {"menu.file.preferences", "Préférences"},
        {"menu.file.send", "Envoyer un fichier à un partenaire"},
        {"menu.file.resend", "Envoyer en tant que nouvelle transaction"},
        {"menu.help", "Aide"},
        {"menu.help.about", "A propos"},
        {"menu.help.shop", "mendelson online shop"},
        {"menu.help.helpsystem", "Système d''aide"},
        {"menu.help.forum", "Forum"},
        {"details", "Détails du message"},
        {"filter.showfinished", "Voir les terminés"},
        {"filter.showpending", "Voir les en-cours"},
        {"filter.showstopped", "Voir les stoppés"},
        {"filter.none", "-- Aucun --"},
        {"filter.partner", "Filtrer le partenaire:"},
        {"filter.localstation", "Filtrer le station locale:"},
        {"filter.direction", "Filtrer le direction:"},
        {"filter.direction.inbound", "Entrer"},
        {"filter.direction.outbound", "Sortant"},
        {"filter", "Filtrer"},
        {"keyrefresh", "Recharger les clés"},
        {"delete.msg", "Supprimer les messages sélectionnés"},
        {"stoprefresh.msg", "Figer le rafraîchissement"},
        {"dialog.msg.delete.message", "Voulez-vous vraiment supprimer de manière permanente les messages sélectionnés ?"},
        {"dialog.msg.delete.title", "Suppression de messages"},
        {"welcome", "Bienvenue, {0}"},
        {"warning.eval", "Ceci est une copie d''évaluation."},
        {"warning.refreshstopped", "Le rafraîchissement de l''interface a été arrêté."},
        {"tab.welcome", "Nouveautés et mises à jour"},
        {"tab.transactions", "Transactions"},
        {"new.version", "Une nouvelle version est disponible. Cliquez ici pour la télécharger."},
        {"filechooser.export", "Veuillez choisir un dossier d''exportation."},
        {"filechooser.import", "Veuillez choisir un dossier d''importation."},
        {"export.success", "La configuration a été exportée avec succès vers \"{0}\"."},
        {"dbconnection.failed.message", "Incapable d''établir une connexion DB au serveur AS2: {0}"},
        {"dbconnection.failed.title", "Impossible de se connecter"},
        {"login.failed.client.incompatible.message", "Le serveur de rapports que ce client est incompatible. Veuillez utiliser la version du client approprié."},
        {"login.failed.client.incompatible.title", "Login rejeté"},
        {"uploading.to.server", "Téléchargement sur le serveur"},
        {"refresh.overview", "Rafraîchissant"},
        {"resend.performed", "Cette transaction a été renvoyer manuellement comme une nouvelle transaction." },
        {"dialog.resend.message", "Voulez-vous vraiment de renvoyer les données de la transaction sélectionnée?"},
        {"dialog.resend.title", "Transaction renvoyer"},        
    };
}