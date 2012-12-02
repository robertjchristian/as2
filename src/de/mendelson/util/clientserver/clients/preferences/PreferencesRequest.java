//$Header: /cvsroot-fuse/mec-as2/39/mendelson/util/clientserver/clients/preferences/PreferencesRequest.java,v 1.1 2012/04/18 14:10:44 heller Exp $
package de.mendelson.util.clientserver.clients.preferences;

import de.mendelson.util.clientserver.messages.ClientServerMessage;
import java.io.Serializable;
/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Msg for the client server protocol
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class PreferencesRequest extends ClientServerMessage implements Serializable{

    public static final int TYPE_SET = 1;
    public static final int TYPE_GET = 2;
    public static final int TYPE_GET_DEFAULT = 3;

    private String key = null;
    private String value = null;
    private int type = TYPE_SET;

    @Override
    public String toString(){
        return( "Preferences request" );
    }

    /**
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * @param key the key to set
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * @return the type
     */
    public int getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(int type) {
        this.type = type;
    }
        
}
