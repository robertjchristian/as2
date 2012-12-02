//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/send/ResourceBundleHttpUploader_de.java,v 1.1 2012/04/18 14:10:35 heller Exp $
package de.mendelson.comm.as2.send;
import de.mendelson.util.MecResourceBundle;

/**
 * ResourceBundle to localize a mendelson product
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class ResourceBundleHttpUploader_de extends MecResourceBundle{
    
    @Override
    public Object[][] getContents() {
        return contents;
    }
    
    /**List of messages in the specific language*/
    static final Object[][] contents = {
        {"returncode.ok", "{0}: Nachricht erfolgreich versandt (HTTP {1}); {2} übertragen in {3} [{4} KB/s]." },
        {"returncode.accepted", "{0}: Nachricht erfolgreich versandt (HTTP {1}); {2} übertragen in {3} [{4} KB/s]." },
        {"sending.msg.sync", "{0}: Sende AS2 Nachricht an {1}, erwarte synchrone MDN zur Empfangsbestätigung." },
        {"sending.cem.sync", "{0}: Sende CEM Nachricht an {1}, erwarte synchrone MDN zur Empfangsbestätigung." },
        {"sending.msg.async", "{0}: Sende AS2 Nachricht an {1}, erwarte asynchrone MDN zur Empfangsbestätigung auf {2}." },
        {"sending.cem.async", "{0}: Sende CEM Nachricht an {1}, erwarte asynchrone MDN zur Empfangsbestätigung auf {2}." },
        {"sending.mdn.async", "{0}: Sende asynchrone Empfangsbestätigung (MDN) an {1}." },
        {"error.httpupload", "{0}: Übertragung fehlgeschlagen, entfernter AS2 Server meldet \"{1}\"." },
        {"error.noconnection", "{0}: Verbindungsproblem, es konnten keine Daten übertragen werden." },
        {"using.proxy", "{0}: Benutze Proxy {1}:{2}." },  
        {"answer.no.sync.mdn", "{0}: Die empfangene synchrone Empfangsbestätigung ist nicht im richtigen Format. Der Header \"{1}\" konnte nicht gefunden werden." },
    };
    
}