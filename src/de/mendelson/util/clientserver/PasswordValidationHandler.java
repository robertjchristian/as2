//$Header: /cvsroot-fuse/mec-as2/39/mendelson/util/clientserver/PasswordValidationHandler.java,v 1.1 2012/04/18 14:10:41 heller Exp $
package de.mendelson.util.clientserver;

import de.mendelson.util.clientserver.messages.LoginState;
import de.mendelson.util.clientserver.user.User;
import java.util.logging.Logger;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Handler that cares for the user permissions and password validation
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class PasswordValidationHandler {

    public static final int STATE_PASSWORD_REQUIRED = LoginState.STATE_AUTHENTICATION_FAILURE_PASSWORD_REQUIRED;
    public static final int STATE_FAILURE = LoginState.STATE_AUTHENTICATION_FAILURE;
    public static final int STATE_SUCCESS = LoginState.STATE_AUTHENTICATION_SUCCESS;
    public static final int STATE_INCOMPATIBLE_CLIENT = LoginState.STATE_INCOMPATIBLE_CLIENT;    
    private Logger logger = Logger.getAnonymousLogger();
    private String[] validClientIds = null;

    /**Allows all client ids to login, use carefully*/
    public PasswordValidationHandler() {
    }

    public PasswordValidationHandler(String[] validClientIds) {
        this.validClientIds = validClientIds;
    }

    public void setLogger(Logger logger) {
        if (logger != null) {
            this.logger = logger;
        }
    }

    /**Validates the password for a given user
     *a single passwd entry line line should be like:
     *username:passwd:passwdcrypted:permission1 (1/0):permission2 (1/0):permission3 (1/0):permissionn (1/0)
     */
    public int validate(User definedUser, User transmittedUser, String clientId) {
        //check if the client is valid
        if (this.validClientIds != null) {
            if (clientId == null) {
                return (STATE_INCOMPATIBLE_CLIENT);
            }
            boolean isValid = false;
            for (String validClientId : this.validClientIds) {
                if (clientId.equals(validClientId)) {
                    isValid = true;
                    break;
                }
            }
            if (!isValid) {
                return (STATE_INCOMPATIBLE_CLIENT);
            }
        }
        //unknown user - do not give any detail to the client but just send a login failure
        if (definedUser == null) {
            return (STATE_FAILURE);
        }
        //no password defined for the found user, let the user in without checking the password
        if (definedUser.getPasswdCrypted() == null || definedUser.getPasswdCrypted().length() == 0) {
            return (STATE_SUCCESS);
        }
        //transmitted password is not set and defined password is not empty
        if ((transmittedUser.getPasswdCrypted() == null || transmittedUser.getPasswdCrypted().length() == 0)
                && (definedUser.getPasswdCrypted() != null || definedUser.getPasswdCrypted().length() > 0)) {
            return (STATE_PASSWORD_REQUIRED);
        }
        //a password has been sent, compare the crypted versions
        if (transmittedUser.getPasswdCrypted().equals(definedUser.getPasswdCrypted())) {
            return (STATE_SUCCESS);
        }
        return (STATE_FAILURE);
    }
}
