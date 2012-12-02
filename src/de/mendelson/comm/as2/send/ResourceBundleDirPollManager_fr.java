//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/send/ResourceBundleDirPollManager_fr.java,v 1.1 2012/04/18 14:10:35 heller Exp $
package de.mendelson.comm.as2.send;
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
public class ResourceBundleDirPollManager_fr extends MecResourceBundle{
    
    @Override
    public Object[][] getContents() {
        return contents;
    }
    
    /**List of messages in the specific language*/
    static final Object[][] contents = {
        {"manager.started", "Le gestionnaire de scrutation des répertoires a démarré." },
        {"poll.stopped", "Gestionnaire de scrutation des répertoires: Scrutation pour les relations \"{0}/{1}\" stoppé." },
        {"poll.started", "Gestionnaire de scrutation des répertoires: Scrutation pour les relations \"{0}/{1}\" démarré. Fichiers ignorés: \"{2}\". Intervalle de scrutation: {3}s" },
        {"warning.ro", "Le fichier {0} dans la boîte de départ est en lecture seule, ignoré." },
        {"warning.notcomplete", "{0}: Le dossier d'outbox n'est pas complet jusqu'ici et sera ignoré." },
        {"messagefile.deleted", "{0}: Le fichier \"{1}\" a été déplacé dans la queue de messages à traiter par le serveur." },
        {"processing.file", "Traitement du fichier \"{0}\" pour les relations \"{1}/{2}\"." },
        {"processing.file.error", "Erreur de traitement du fichier \"{0}\" pour les relations \"{1}/{2}\": \"{3}\"." },
    };
    
}
