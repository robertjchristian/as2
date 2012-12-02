//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/clientserver/ErrorObject.java,v 1.1 2012/04/18 14:10:26 heller Exp $
package de.mendelson.comm.as2.clientserver;
import java.io.*;
/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */

/** 
 * Object that can store numbers of errors and warnings for
 * any purpose
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class ErrorObject implements Serializable, Cloneable{

  /**Number of errors that occured*/
  private int errors = 0;
  
  /**Number of warnings that occured*/
  private int warnings = 0;
  
  /**Creates a new error object with NO errors and warnings in it*/
  public ErrorObject(){
  }
    
  /**Creates a new error object with errors and warnings in it
  * @param errors No of errors to store
  * @param warnings No of warnings to store
  */
  public ErrorObject( int errors, int warnings ){
    this.errors = errors;
    this.warnings = warnings;
  }
  
  /**Increases the error by one*/
  public void incErrors(){
    this.errors++;
  }
  
  /**Returns the number of errors that occured*/
  public int getErrors(){
    return( this.errors );
  }

  /**Increases the warnigs by one*/
  public void incWarnings(){
    this.warnings++;
  }

  /**Increases the warnigs by the passed value
   *@param additionalWarnings Warnings to add to the error object*/
  public void incWarnings( int additionalWarnings ){
    this.warnings+=additionalWarnings;
  }

  /**Increases the errors by the passed value
   *@param additionalErros Errors to add to the error object*/
  public void incErrors( int additionalErrors ){
    this.errors+=additionalErrors;
  }
  
  
  /**Returns the number of errors that occured*/
  public int getWarnings(){
    return( this.warnings );
  }
  
  /**Indicates if any warnings and errors are stored in this class*/
  public boolean noErrorsAndWarnings(){
    return( this.warnings == 0 && this.errors == 0 );
  }
  
  /**Sets new value for error number*/
  public void setErrors( int errors ){
    this.errors = errors;
  }

    /**Sets new value for error number*/
  public void setWarnings( int warnings ){
    this.warnings = warnings;
  }

  /**Sets this error object to stateless*/
  public void setNoState(){
      this.errors = -1;
      this.warnings = -1;
  }
    
  /**Clone this object
   */
  public Object clone(){
      try {
          ErrorObject object = (ErrorObject)super.clone();
          return( object );
      } catch (CloneNotSupportedException e ){
          return( null );
      }
  }
  
  
}