//$Header: /cvsroot-fuse/mec-as2/39/mendelson/util/rmi/RMIPing.java,v 1.1 2012/04/18 14:10:45 heller Exp $
package de.mendelson.util.rmi;
import java.io.*;
/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */


/**
 * Class to send an object via RMI, execute it on the
 * server side and return a return object
 * @author  S.Heller
 * @version $Revision: 1.1 $
 */
public class RMIPing implements Serializable{
    
  private String message = "Please ignore this, it is only a server ping";
    
  public RMIPing(){
  }

}
