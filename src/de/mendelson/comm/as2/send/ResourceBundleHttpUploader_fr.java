//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/send/ResourceBundleHttpUploader_fr.java,v 1.1 2012/04/18 14:10:35 heller Exp $
package de.mendelson.comm.as2.send;
import de.mendelson.util.MecResourceBundle;

/**
 * ResourceBundle to localize a mendelson product
 * @author S.Heller
 * @author E.Pailleau
 * @version $Revision: 1.1 $
 */
public class ResourceBundleHttpUploader_fr extends MecResourceBundle{
    
    @Override
    public Object[][] getContents() {
        return contents;
    }
    
    /**List of messages in the specific language*/
    static final Object[][] contents = {
        {"returncode.ok", "{0}: Message envoyé avec succès (HTTP {1}); {2} transféré en {3} [{4} KB/s]." },
        {"returncode.accepted", "{0}: Message envoyé avec succès (HTTP {1}); {2} transféré en {3} [{4} KB/s]." },
        {"sending.msg.sync", "{0}: Envoi du AS2 message vers {1}, sync MDN demandé." },
        {"sending.cem.sync", "{0}: Envoi du CEM message vers {1}, sync MDN demandé." },
        {"sending.msg.async", "{0}: Envoi du AS2 message vers {1}, async MDN demandé vers {2}." },
        {"sending.cem.async", "{0}: Envoi du CEM message vers {1}, async MDN demandé vers {2}." },
        {"sending.mdn.async", "{0}: Envoi d''un MDN asynchrone vers {1}." },
        {"error.httpupload", "{0}: La transmission a echouée, le serveur AS2 distant signale \"{1}\"." },
        {"error.noconnection", "{0}: Problème de connexion, données non transmises." },
        {"using.proxy", "{0}: Utilisation d''un serveur mandataire (proxy) {1}:{2}." },
        {"answer.no.sync.mdn", "{0}: Le MDN synchrone reçu semble avoir un format incorrect. Valeur d'entête manquante \"{1}\"." },
    };
    
}
