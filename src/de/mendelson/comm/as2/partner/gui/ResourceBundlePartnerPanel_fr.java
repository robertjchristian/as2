//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/partner/gui/ResourceBundlePartnerPanel_fr.java,v 1.1 2012/04/18 14:10:32 heller Exp $
package de.mendelson.comm.as2.partner.gui;

import de.mendelson.util.MecResourceBundle;
/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */

/**
 * ResourceBundle to localize a mendelson product
 * @author S.Heller
 * @author E.Pailleau
 * @version $Revision: 1.1 $
 */
public class ResourceBundlePartnerPanel_fr extends MecResourceBundle {

    @Override
    public Object[][] getContents() {
        return contents;
    }
    /**List of messages in the specific language*/
    static final Object[][] contents = {
        {"title", "Configuration des partenaires"},
        {"label.name", "Nom:"},
        {"label.id", "AS2 id:"},
        {"label.partnercomment", "Commentaire:" },
        {"label.url", "URL de réception:"},
        {"label.mdnurl", "URL des MDN:"},
        {"label.signalias.key", "Clef privée (signature):"},
        {"label.cryptalias.key", "Clef privée (encryption):"},
        {"label.signalias.cert", "Certificat du partenaire (signature):"},
        {"label.cryptalias.cert", "Certificat du partenaire (encryption):"},
        {"label.signtype", "Algorithme de signature numérique:"},
        {"label.encryptiontype", "Algorithme de chiffrement des messages:"},
        {"label.email", "Adresse E-mail:"},
        {"label.localstation", "Station locale"},
        {"label.compression", "Compresser les messages sortants (nécessite une solution AS2 1.1 en face)"},
        {"label.usecommandonreceipt", "Commande système sur réception de message:"},
        {"label.usecommandonsenderror", "Commande système sur envoi échoué de message:"},
        {"label.usecommandonsendsuccess", "Commande système sur envoi réussi de message:"},
        {"label.keepfilenameonreceipt", "Garder le nom de fichier original sur réception (si l''émetteur a ajouté cette information)"},
        {"tab.misc", "Divers"},
        {"tab.security", "Sécurité"},
        {"tab.send", "Envoi"},
        {"tab.mdn", "MDN"},
        {"tab.dirpoll", "Scrutation de répertoire"},
        {"tab.receipt", "Réception"},
        {"tab.httpauth", "Authentication HTTP"},
        {"tab.httpheader", "En-tête de HTTP"},
        {"tab.notification", "Notification" },
        {"tab.events", "Evénements" },
        {"tab.partnersystem", "Info" },
        {"label.subject", "Sujet du contenu:"},
        {"label.contenttype", "Type de contenu:"},
        {"label.syncmdn", "Utilise des MDN synchrone"},
        {"label.asyncmdn", "Utilise des MDN asynchrone"},
        {"label.signedmdn", "Utilise des MDN signés"},
        {"label.polldir", "Répertoire de scrutation:"},
        {"label.pollinterval", "Intervalle de scrutation:"},
        {"label.pollignore", "Ignorer les fichiers:"},
        {"label.maxpollfiles", "Maximale des fichiers par sondage:"},
        {"label.usehttpauth", "Utiliser l''authentication HTTP pour envoyer les messages AS2"},
        {"label.usehttpauth.user", "Utilisateur:"},
        {"label.usehttpauth.pass", "Mot de passe:"},
        {"label.usehttpauth.asyncmdn", "Utiliser l''authentication HTTP pour envoyer les MDN asynchrones"},
        {"label.usehttpauth.asyncmdn.user", "Utilisateur:"},
        {"label.usehttpauth.asyncmdn.pass", "Mot de passe:"},
        {"hint.filenamereplacement.receipt1", "Remplacement: $'{'filename}, $'{'subject},"},
        {"hint.filenamereplacement.receipt2", "$'{'sender}, $'{'receiver}, $'{'messageid}"},
        {"hint.replacement.send1", "Remplacement: $'{'filename}, $'{'fullstoragefilename}, $'{'log}, $'{'subject},"},        
        {"hint.replacement.send2", "$'{'sender}, $'{'receiver}, $'{'messageid}, $'{'mdntext}"},
        {"hint.subject.replacement", "L''expression $'{'filename} est remplacée par le nom de fichier d''envoi."},
        {"hint.keepfilenameonreceipt", "Merci de vous assurer que votre partenaire envoi des nom de fichiers uniques avant d''activer cette option!"},
        {"label.url.hint", "Merci de faire démarrer cette URL avec le protocole \"http://\" ou \"https://\"."},
        {"label.notify.send", "Notifier lors d''un dépassement de quota sur message envoyé" },
        {"label.notify.receive", "Notifier lors d''un dépassement de quota sur message reçu" },
        {"label.notify.sendreceive", "Notifier lors d''un dépassement de quota sur message envoyé ou reçu" },
        {"header.httpheaderkey", "Nom" },
        {"header.httpheadervalue", "Valeur" },
        {"httpheader.add", "Ajouter " },
        {"httpheader.delete", "Éliminer" },
        {"label.as2version", "Version AS2:" },
        {"label.productname", "Nom du produit:" },
        {"label.features", "Fonctionnalités:" },
        {"label.features.cem", "Certificat d'échange via CEM" },
        {"label.features.ma", "Plusieurs pièces jointes" },
        {"label.features.compression", "Compression" },
        {"partnerinfo", "Votre partenaire transmet avec chaque message AS2 quelques informations à propos de ses capacités de système AS2. Il s'agit d'une liste de fonctions qui a été transmise par votre partenaire." },
        {"partnersystem.noinfo", "Aucune information n''est disponible, qu''il y avait déjà une transaction?" },
        {"label.httpversion", "Version du protocole HTTP:" },
    };
}
