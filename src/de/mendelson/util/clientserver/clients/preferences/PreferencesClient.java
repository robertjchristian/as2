//$Header: /cvsroot-fuse/mec-as2/39/mendelson/util/clientserver/clients/preferences/PreferencesClient.java,v 1.1 2012/04/18 14:10:44 heller Exp $
package de.mendelson.util.clientserver.clients.preferences;

import de.mendelson.util.clientserver.BaseClient;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Requests and preferences from and sets new values to the server
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class PreferencesClient {

    private BaseClient baseClient;

    public PreferencesClient(BaseClient baseClient) {
        this.baseClient = baseClient;
    }

    /**Returns a single string value from the preferences or the default
     *if it is not found
     *@param key one of the class internal constants
     * @return In case of an error during the sync request an empty string is returned
     */
    public String get(final String KEY) {
        PreferencesRequest request = new PreferencesRequest();
        request.setKey(KEY);
        request.setType(PreferencesRequest.TYPE_GET);
        PreferencesResponse response = (PreferencesResponse) this.baseClient.sendSync(request);
        if (response != null) {
            return (response.getValue());
        } else {
            return ("");
        }
    }

    /**Returns a single string value from the preferences or the default
     *if it is not found
     *@param key one of the class internal constants
     * @return In case of an error during the sync request an empty string is returned
     */
    public String getDefaultValue(final String KEY) {
        PreferencesRequest request = new PreferencesRequest();
        request.setKey(KEY);
        request.setType(PreferencesRequest.TYPE_GET_DEFAULT);
        PreferencesResponse response = (PreferencesResponse) this.baseClient.sendSync(request);
        if (response != null) {
            return (response.getValue());
        } else {
            return ("");
        }
    }

    /**Stores a value in the preferences. If the passed value is null or an
     *empty string the key-value pair will be deleted from the registry.
     *@param KEY Key as defined in this class
     *@param value value to set
     */
    public void put(final String KEY, String value) {
        PreferencesRequest request = new PreferencesRequest();
        request.setKey(KEY);
        request.setValue(value);
        request.setType(PreferencesRequest.TYPE_SET);
        this.baseClient.sendAsync(request);
    }

    /**Puts a value to the preferences and stores the prefs
     *@param KEY Key as defined in this class
     *@param value value to set
     */
    public void putInt(final String KEY, int value) {
        PreferencesRequest request = new PreferencesRequest();
        request.setKey(KEY);
        request.setValue(String.valueOf(value));
        request.setType(PreferencesRequest.TYPE_SET);
        this.baseClient.sendAsync(request);
    }

    /**Returns the value, as fallback its default value and -1 on client-server connection loss*/
    public int getInt(final String KEY) {
        PreferencesRequest request = new PreferencesRequest();
        request.setKey(KEY);
        request.setType(PreferencesRequest.TYPE_GET);
        PreferencesResponse response = (PreferencesResponse) this.baseClient.sendSync(request);
        if (response != null) {
            return (Integer.valueOf(response.getValue()));
        } else {
            return (-1);
        }
    }

    /**Puts a value to the preferences and stores the prefs
     *@param KEY Key as defined in this class
     *@param value value to set
     */
    public void putBoolean(final String KEY, boolean value) {
        PreferencesRequest request = new PreferencesRequest();
        request.setKey(KEY);
        request.setValue(String.valueOf(value));
        request.setType(PreferencesRequest.TYPE_SET);
        this.baseClient.sendAsync(request);
    }

    /**Returns the value for the asked key, if non is defined it returns
     *the default value*/
    public boolean getBoolean(final String KEY) {
        PreferencesRequest request = new PreferencesRequest();
        request.setKey(KEY);
        request.setType(PreferencesRequest.TYPE_GET);
        PreferencesResponse response = (PreferencesResponse) this.baseClient.sendSync(request);
        if (response != null) {
            return (Boolean.valueOf(response.getValue()).booleanValue());
        } else {
            return (false);
        }
    }
}
