//$Header: /cvsroot-fuse/mec-as2/39/mendelson/util/rmi/IRMISenderObject.java,v 1.1 2012/04/18 14:10:45 heller Exp $
package de.mendelson.util.rmi;
/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Interface for all objects send or received via the RMISender
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public interface IRMISenderObject {

    /**Indicates from the server site that an error occured working 
     * on the server*/
    public void indicateErrorOnServer( String[] errorText );
    
}

