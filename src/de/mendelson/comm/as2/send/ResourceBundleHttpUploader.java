//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/send/ResourceBundleHttpUploader.java,v 1.1 2012/04/18 14:10:35 heller Exp $
package de.mendelson.comm.as2.send;
import de.mendelson.util.MecResourceBundle;

/**
 * ResourceBundle to localize a mendelson product
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class ResourceBundleHttpUploader extends MecResourceBundle{
    
    @Override
    public Object[][] getContents() {
        return contents;
    }
    
    /**List of messages in the specific language*/
    static final Object[][] contents = {
        {"returncode.ok", "{0}: Message sent successfully (HTTP {1}); {2} transfered in {3} [{4} KB/s]." },
        {"returncode.accepted", "{0}: Message sent successfully (HTTP {1}); {2} transfered in {3} [{4} KB/s]." },
        {"sending.msg.sync", "{0}: Sending AS2 message to {1}, sync MDN requested." },
        {"sending.cem.sync", "{0}: Sending CEM message to {1}, sync MDN requested." },
        {"sending.msg.async", "{0}: Sending AS2 message to {1}, async MDN requested at {2}." },
        {"sending.cem.async", "{0}: Sending CEM message to {1}, async MDN requested at {2}." },
        {"sending.mdn.async", "{0}: Sending async MDN to {1}." },  
        {"error.httpupload", "{0}: Transmission failed, remote AS2 server reports \"{1}\"." },
        {"error.noconnection", "{0}: Connection problem, failed to transmit data." },
        {"using.proxy", "{0}: Using proxy {1}:{2}." },
        {"answer.no.sync.mdn", "{0}: The received sync MDN seems not to be in right format. Missing header value \"{1}\"." },
    };
    
}