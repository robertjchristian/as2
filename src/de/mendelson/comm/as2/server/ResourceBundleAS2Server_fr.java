//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/server/ResourceBundleAS2Server_fr.java,v 1.1 2012/04/18 14:10:39 heller Exp $
package de.mendelson.comm.as2.server;

import de.mendelson.comm.as2.AS2ServerVersion;
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
public class ResourceBundleAS2Server_fr extends MecResourceBundle {

    @Override
    public Object[][] getContents() {
        return contents;
    }
    /**List of messages in the specific language*/
    static final Object[][] contents = {
        {"fatal.limited.strength", "La force principale limitée a été détectée dans le JVM. Veuillez installer le \"Unlimited jurisdiction key strength policy\" dossiers avant de courir le serveur " + AS2ServerVersion.getProductName() + "." },
        {"server.willstart", "{0}"},
        {"server.started", "Démarrage du " + AS2ServerVersion.getFullProductName() + " dans {0} ms."},
        {"rmi.port.in.use", "Impossible de démarrer le serveur {2} : Le port RMI \n" + "{0} est déjà utilisé par un autre processus.\n" + "Le {2} est basé sur une architecture client-serveur. Par conséquent, il a besoin\n" + "d''un port de communication via ''Remote Method Invocation''.\n" + "Le message d''erreur de la commande de bind: \n" + "{1}."},
        {"server.already.running", "Une instance de " + AS2ServerVersion.getProductName() + " semble déjà en cours.\nIl est aussi possible qu''une instance précédente du programme ne s''est pas terminée correctement. Si vous êtes sûr qu''aucune autre instance n''est en cours\nmerci de supprimer le fichier de lock \"{0}\" (Date de démarrage {1}) et redémarrer le serveur."},
        {"server.nohttp", "Le HTTP serveur intégré n''a pas été commencé." },
    };
}
