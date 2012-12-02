//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/message/DispositionNotificationOptions.java,v 1.1 2012/04/18 14:10:30 heller Exp $
package de.mendelson.comm.as2.message;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Stores the options about the MDN, have been set by an inbound AS2 message
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class DispositionNotificationOptions implements Serializable{
    
    private String headerValue = "signed-receipt-protocol=optional, pkcs7-signature; signed-receipt-micalg=optional, sha1, md5";
    
    /**Stores the parsed options*/
    private HashMap<String,String> propertyMap = new HashMap<String,String>();
    
    /** Creates a new instance of DispositionNotificationOptions */
    public DispositionNotificationOptions() {
        this.parseHeaderValue( this.headerValue );
    }
    
    private void parseHeaderValue( String headerValue ){
        this.propertyMap.clear();
        if( headerValue == null ){
            return;
        }
        StringTokenizer tokenizer = new StringTokenizer( headerValue.toLowerCase(), ";");
        while( tokenizer.hasMoreTokens() ){
            String token = tokenizer.nextToken();
            int index = token.indexOf( "=" );
            if( index > 0 && index < token.length() ){
                String key = token.substring( 0, index ).trim();
                String value = token.substring( index+1 );
                this.propertyMap.put( key, value );
            }
        }
    }
    
    /**Returns the disposition-notification-options header*/
    public String getHeaderValue() {
        return( headerValue );
    }
    
    /**Sets the header for the disposition-notification-options*/
    public void setHeaderValue(String headerValue) {
        this.parseHeaderValue( headerValue );
        this.headerValue = headerValue;
    }
    
    public boolean signMDN(){
        String value = this.propertyMap.get( "signed-receipt-protocol" );
        if( value == null ){
            return( false );
        }
        return( value.indexOf( "pkcs7-signature" ) >= 0 );
    }
    
    /**Returns the allowed signature algorithm requested by the disposition notification
     */
    public int[] getSignatureAlgorithm(){
        String value = this.propertyMap.get( "signed-receipt-micalg" );
        if( value == null ){
            return( new int[0] );
        }
        //may be sha1 or md5 but older S/MIME implementations also allow rsa-md5 and rsa-sha1
        List<Integer> list = new ArrayList<Integer>();
        if( value.indexOf( "sha1" ) >= 0 ){
            list.add( Integer.valueOf( AS2Message.SIGNATURE_SHA1 ));
        }
        if( value.indexOf( "md5" ) >= 0 ){
            list.add( Integer.valueOf( AS2Message.SIGNATURE_MD5 ));
        }
        int[] returnValues = new int[list.size()];
        for( int i = 0; i < list.size(); i++ ){
            returnValues[i] = list.get(i).intValue();
        }
        return( returnValues );
    }
    
    
}
