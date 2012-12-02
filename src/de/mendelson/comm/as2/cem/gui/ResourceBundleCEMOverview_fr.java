//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/cem/gui/ResourceBundleCEMOverview_fr.java,v 1.1 2012/04/18 14:10:20 heller Exp $
package de.mendelson.comm.as2.cem.gui;
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
public class ResourceBundleCEMOverview_fr extends MecResourceBundle{
    
    @Override
    public Object[][] getContents() {
        return contents;
    }

    /**List of messages in the specific language*/
    static final Object[][] contents = {
        {"title", "Certificat d'échange présentation" },
        {"button.sendcem", "Nouveau certificat Exchange" },
        {"button.requestdetails", "Détails de la demande" },
        {"button.responsedetails", "Détails de la réponse" },
        {"button.exit", "Fermer" },
        {"button.cancel", "Annuler" },
        {"button.refresh", "Actualisation" },
        {"button.remove", "Supprimer" },
        {"header.state", "Réponse" },
        {"header.category", "Utilisé pour" },
        {"header.requestdate", "Demandé au" },
        {"header.initiator", "De" },
        {"header.receiver", "À" },
        {"label.certificate", "Certificat:"},
        {"header.alias", "Certificat"},
        {"header.activity", "Activité du système" },
        {"activity.waitingforprocessing", "En attente de traitement" },
        {"activity.waitingforanswer", "En attente de réponse" },
        {"activity.waitingfordate", "En attente de la date d''activation ({0})" },
        {"activity.activated", "Aucun - Activé au {0}" },
        {"activity.none", "Aucun" },
        {"tab.certificate", "Certificat" },
        {"tab.reasonforrejection", "Raison du rejet" },
    };
    
}