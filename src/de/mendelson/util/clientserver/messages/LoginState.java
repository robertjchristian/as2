//$Header: /cvsroot-fuse/mec-as2/39/mendelson/util/clientserver/messages/LoginState.java,v 1.1 2012/04/18 14:10:45 heller Exp $
package de.mendelson.util.clientserver.messages;

import de.mendelson.util.clientserver.user.User;
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
public class LoginState extends ClientServerMessage implements Serializable {

    /**Stores the users rights, this is returned by the server*/
    private User user;
    public static final int STATE_AUTHENTICATION_REQUIRED = 1;
    public static final int STATE_AUTHENTICATION_FAILURE = 2;
    public static final int STATE_AUTHENTICATION_FAILURE_PASSWORD_REQUIRED = 3;
    public static final int STATE_INCOMPATIBLE_CLIENT = 4;
    public static final int STATE_AUTHENTICATION_SUCCESS = 99;
    private int state = STATE_AUTHENTICATION_FAILURE;

    public int getState() {
        return state;
    }

    public String getPermission(Integer index) {
        if (this.user == null) {
            return ("");
        }
        return (this.user.getPermission(index));
    }

    public void setState(int state) {
        this.state = state;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {        
        StringBuilder buffer = new StringBuilder();
        if( this.user != null ){
            buffer.append("Login state for user ");
            buffer.append(this.user.getName());
            buffer.append(": ");
        }else{
            buffer.append( "Login state: ");
        }
        if (this.state == STATE_AUTHENTICATION_FAILURE) {
            buffer.append("FAILURE");
        } else if (this.state == STATE_AUTHENTICATION_SUCCESS) {
            buffer.append("SUCCESS");
        } else if (this.state == STATE_AUTHENTICATION_REQUIRED) {
            buffer.append("AUTHENTICATION REQUIRED");
        } else if (this.state == STATE_AUTHENTICATION_FAILURE_PASSWORD_REQUIRED) {
            buffer.append("PASSWORD_REQUIRED");
        } else if (this.state == STATE_AUTHENTICATION_SUCCESS) {
            buffer.append("AUTHENTICATION SUCCESS");
        }else if (this.state == STATE_INCOMPATIBLE_CLIENT) {
            buffer.append("INCOMPATIBLE CLIENT");
        } else {
            buffer.append("UNKNOWN");
        }
        return (buffer.toString());
    }
}
