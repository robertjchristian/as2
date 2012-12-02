//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/message/ResourceBundleMDNParser_fr.java,v 1.1 2012/04/18 14:10:30 heller Exp $
package de.mendelson.comm.as2.message;
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
public class ResourceBundleMDNParser_fr extends MecResourceBundle{
    
    @Override
    public Object[][] getContents() {
        return contents;
    }
    
    /**List of messages in the specific language*/
    static final Object[][] contents = {
        {"invalid.mdn.nocontenttype", "MDN invalide: Aucun type de contenu trouvé" },
        {"structure.failure.mdn", "Un MDN entrant a été analysé et il y a un échec de structure dans le MDN. Le MDN est inadmissible et ne pourrait pas être traité, le statut du message AS2/de transaction référencés ne sera pas changé: \"{0}\"" },
    };
    
}
