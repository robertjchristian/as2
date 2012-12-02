//$Header: /cvsroot-fuse/mec-as2/39/mendelson/util/MecResourceBundle.java,v 1.1 2012/04/18 14:10:41 heller Exp $
package de.mendelson.util;

import java.util.*;
import java.io.*;
import java.text.MessageFormat;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/** 
 * Class that implements some additional methods to the ListResourceBundle and wrapps error messages
 * if some resources are not found
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public abstract class MecResourceBundle extends ListResourceBundle implements Serializable {

    /** Creates new MecResourceBundle */
    public MecResourceBundle() {
    }

    /** Stores the resources as array*/
    public abstract Object[][] getContents();

    /**Returns the encoded string of the resource
     * @param resourceStr Resource key to read
     */
    public final String getResourceString(String resourceStr) {
        return (this.getResourceString(resourceStr, null));
    }

    /**Returns a formatted localized message with passed argument
     * @param resourceStr Resource indeitification String
     * @param arg Argument passed to the message
     */
    public String getResourceString(String resourceStr, Object arg) {
        return (this.getResourceString(resourceStr, new Object[]{arg}));
    }

    /**Returns a formatted localized message with passed arguments
     * @param resourceStr Resource indeitification String
     * @param arg Arguments passed to the message
     */
    public String getResourceString(String resourceStr, Object[] args) {
        String localizedMessage = null;
        try {
            localizedMessage = super.getString(resourceStr);
        } catch (MissingResourceException e) {
            System.err.println(e.getMessage() + " (" + e.getKey() + ")");
            System.err.println("ResourceBundle lookup error in  " + e.getClassName() + ".");
            return ("###");
        }
        //Reformat message using the message patterns. Always call this method (even if no arg is passed
        //because it modifies the resource String (e.g. "''" is replaced by "'" )
        try{
            localizedMessage = MessageFormat.format(localizedMessage, args);
        }
        catch( IllegalArgumentException e ){
            System.err.println( "ResourceBundle format error: " + e.getMessage());
            System.err.println( "Resource text: " + localizedMessage);
        }
        return (localizedMessage);
    }
}