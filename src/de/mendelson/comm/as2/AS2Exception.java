//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/AS2Exception.java,v 1.1 2012/04/18 14:10:16 heller Exp $
package de.mendelson.comm.as2;
import de.mendelson.comm.as2.message.AS2Message;
/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * An exception that is thrown if anything works wrong during the processing
 * of a message, this is send to the partner as error MDN
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class AS2Exception extends Exception{
    
    /**authentication-failed*/
    public static final String AUTHENTIFICATION_ERROR = "authentication-failed";
    /**decompression-failed*/
    public static final String DECOMPRESSSION_ERROR = "decompression-failed";
    /**decryption-failed*/
    public static final String DECRYPTION_ERROR = "decryption-failed";
    /**insufficient-message-security*/
    public static final String INSUFFICIENT_SECURITY_ERROR = "insufficient-message-security";
    /**integrity-check-failed*/
    public static final String INTEGRITY_ERROR = "integrity-check-failed";
    /**unexpected-processing-error*/
    public static final String PROCESSING_ERROR = "unexpected-processing-error";
    /**unknown-trading-partner*/
    public static final String UNKNOWN_TRADING_PARTNER_ERROR = "unknown-trading-partner";
    
    private String errorType;
    
    private AS2Message as2Message;

    /**Creates a new exception
     *@param ERROR_TYPE One of the error types as defined in this class
     *@param message detailled error message
     */
    public AS2Exception( final String ERROR_TYPE, String errorMessage, AS2Message as2Message ){
        super( errorMessage );
        this.errorType = ERROR_TYPE;        
        this.as2Message = as2Message;
    }
    
    
    public String getErrorType() {
        return errorType;
    }

    public AS2Message getAS2Message() {
        return as2Message;
    }
        
}
