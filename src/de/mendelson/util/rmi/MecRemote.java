//$Header: /cvsroot-fuse/mec-as2/39/mendelson/util/rmi/MecRemote.java,v 1.1 2012/04/18 14:10:45 heller Exp $
package de.mendelson.util.rmi;
import java.rmi.Remote;
import java.rmi.RemoteException;
/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */


/**
 * Interface for the RMI request implementation of all mendelson products
 * @author  S.Heller
 * @version $Revision: 1.1 $
 */
public interface MecRemote extends Remote{

  /**Send a request to a running server
   * @param object Object that contains informations about the command to execute
   */
  public Object execute( Object object ) throws RemoteException;
  
  /**Ping the server, sometimes a service is bound somewhere but this
   *method indicates perfectly that the RIGHT server is running
   */
  public RMIPing ping() throws RemoteException;
  
}