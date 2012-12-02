//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/preferences/PreferencesAS2.java,v 1.1 2012/04/18 14:10:35 heller Exp $
package de.mendelson.comm.as2.preferences;

import de.mendelson.comm.as2.AS2ServerVersion;
import de.mendelson.util.MecResourceBundle;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */

/**
 * Class to manage the preferences of the AS2 server
 * @author  S.Heller
 * @version $Revision: 1.1 $
 */
public class PreferencesAS2 {

    /**Position of the client frame X*/
    public static final String FRAME_X = "frameguix";
    /**Position of the client frame Y*/
    public static final String FRAME_Y = "frameguiy";
    /**Position of the client frame height*/
    public static final String FRAME_HEIGHT = "frameguiheight";
    /**Position of the IDE frame WIDTH*/
    public static final String FRAME_WIDTH = "frameguiwidth";
    /**Language to use for the software localization*/
    public static final String LANGUAGE = "language";
    /**the actual used server to connect to*/
    public static final String SERVER_HOST = "serverhost";
    /**The RMI connection port*/
    public static final String SERVER_RMI_PORT = "serverrmiport";
    /**RMI service*/
    public static final String SERVER_RMI_SERVICE = "rmiservice";
    /**DB server*/
    public static final String SERVER_DB_PORT = "dbport";
    /**client server comm port*/
    public static final String CLIENTSERVER_COMM_PORT = "clientservercommport";
    /**Directory the messageparts are stored in*/
    public static final String DIR_MSG = "dirmsg";
    public static final String DIR_LOG = "dirlog";
    public static final String ASYNC_MDN_TIMEOUT = "asyncmdntimeout";
    /**keystore for user defined certs in https*/
    public static final String KEYSTORE_HTTPS_SEND = "httpsendkeystore";
    /**password for user defined certs keystore in https*/
    public static final String KEYSTORE_HTTPS_SEND_PASS = "httpsendkeystorepass";
    /**password for the encryption/signature keystore*/
    public static final String KEYSTORE_PASS = "keystorepass";
    public static final String KEYSTORE = "keystore";
    public static final String PROXY_HOST = "proxyhost";
    public static final String PROXY_PORT = "proxyport";
    public static final String AUTH_PROXY_USER = "proxyuser";
    public static final String AUTH_PROXY_PASS = "proxypass";
    public static final String AUTH_PROXY_USE = "proxyuseauth";
    public static final String PROXY_USE = "proxyuse";
    public static final String AUTO_MSG_DELETE = "automsgdelete";
    public static final String AUTO_MSG_DELETE_OLDERTHAN = "automsgdeleteolderthan";
    public static final String AUTO_MSG_DELETE_LOG = "automsgdeletelog";
    public static final String AUTO_STATS_DELETE = "autostatsdelete";
    public static final String AUTO_STATS_DELETE_OLDERTHAN = "autostatsdeleteolderthan";
    public static final String JNDI_PORT = "jndiport";
    public static final String MQ_PROXY_PORT = "mqproxyport";
    public static final String RECEIPT_PARTNER_SUBDIR = "receiptpartnersubdir";
    public static final String HTTP_SEND_TIMEOUT = "httpsendtimeout";
    public static final String LAST_UPDATE_CHECK = "lastupdatecheck";
    public static final String CEM = "cem";
    public static final String COMMUNITY_EDITION = "commed";
    public static final String WRITE_OUTBOUND_STATUS_FILE = "outboundstatusfile";
    /**Settings stored for the user*/
    private Preferences preferences = null;

    /**Initialize the preferences*/
    public PreferencesAS2() {
        String os = System.getProperty("os.name").toLowerCase();
        //on windows systems it is common to use as root, the activation will
        //be system wide. On Linux/Unix systems it is ok to activate the
        //IDE for only one user (lets say user account "mendelson" )
        if (os.startsWith("win")) {
            //windows 7 and windows vista: use system node for the preferences, enables the settings
            //for a single user
            if (os.startsWith("windows 7") || os.startsWith("windows vista")) {
                this.preferences = Preferences.userNodeForPackage(AS2ServerVersion.class);
            } else {
                this.preferences = Preferences.systemNodeForPackage(AS2ServerVersion.class);
            }
            try {
                //check if the user has the rights to access the system node
                this.preferences.putInt("rights_check", 1);
                this.preferences.flush();
            } catch (BackingStoreException e) {
                //switch back to user preferences, user has no rights to access the system node
                this.preferences = Preferences.userNodeForPackage(AS2ServerVersion.class);
            } catch (SecurityException e) {
                //switch back to user preferences, user has no rights to access the system node
                this.preferences = Preferences.userNodeForPackage(AS2ServerVersion.class);
            }
        } else {
            this.preferences = Preferences.userNodeForPackage(AS2ServerVersion.class);
        }
    }

    /**Returns the localized preference*/
    public static String getLocalizedName(final String KEY) {

        //load resource bundle
        try {
            MecResourceBundle rb = (MecResourceBundle) ResourceBundle.getBundle(
                    ResourceBundlePreferences.class.getName());
            return (rb.getResourceString(KEY));
        } catch (MissingResourceException e) {
            throw new RuntimeException("Oops..resource bundle " + e.getClassName() + " not found.");
        }
    }

    /**Returns the default value for the key
     *@param KEY key to store properties with in the preferences
     */
    public String getDefaultValue(final String KEY) {
        if (KEY.equals(PreferencesAS2.FRAME_X)) {
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            Dimension dialogSize = new Dimension(
                    new Integer(this.getDefaultValue(PreferencesAS2.FRAME_WIDTH)).intValue(),
                    new Integer(this.getDefaultValue(PreferencesAS2.FRAME_HEIGHT)).intValue());
            return (String.valueOf((screenSize.width - dialogSize.width) / 2));
        }
        if (KEY.equals(PreferencesAS2.FRAME_Y)) {
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            Dimension dialogSize = new Dimension(
                    new Integer(this.getDefaultValue(PreferencesAS2.FRAME_WIDTH)).intValue(),
                    new Integer(this.getDefaultValue(PreferencesAS2.FRAME_HEIGHT)).intValue());
            return (String.valueOf((screenSize.height - dialogSize.height) / 2));
        }
        if (KEY.equals(PreferencesAS2.FRAME_WIDTH)) {
            return ("800");
        }
        if (KEY.equals(PreferencesAS2.FRAME_HEIGHT)) {
            return ("600");
        }
        //language used for the localization
        if (KEY.equals(PreferencesAS2.LANGUAGE)) {
            if (Locale.getDefault().equals(Locale.GERMANY)) {
                return ("de");
            }
            //default is always english
            return ("en");
        }
        //RMI port for client-server communication
        if (KEY.equals(PreferencesAS2.SERVER_RMI_PORT)) {
            return ("1099");
        }
        //DB port for the server
        if (KEY.equals(PreferencesAS2.SERVER_DB_PORT)) {
            return ("3333");
        }
        //RMI service provided by the rmi server
        if (KEY.equals(PreferencesAS2.SERVER_RMI_SERVICE)) {
            return ("MEC_AS2");
        }
        //server to connect to by default
        if (KEY.equals(PreferencesAS2.SERVER_HOST)) {
            return ("localhost");
        }
        //message part directory
        if (KEY.equals(PreferencesAS2.DIR_MSG)) {
            return (new File(System.getProperty("user.dir")).getAbsolutePath() + File.separator + "messages");
        }
        if (KEY.equals(DIR_LOG)) {
            return (new File(System.getProperty("user.dir")).getAbsolutePath() + File.separator + "log");
        }
        if (KEY.equals(PreferencesAS2.KEYSTORE_HTTPS_SEND)) {
            return ("jetty/etc/keystore");
        }
        if (KEY.equals(PreferencesAS2.KEYSTORE_HTTPS_SEND_PASS)) {
            return ("test");
        }
        if (KEY.equals(KEYSTORE)) {
            return ("certificates.p12");
        }
        if (KEY.equals(PreferencesAS2.PROXY_HOST)) {
            return ("localhost");
        }
        if (KEY.equals(PreferencesAS2.PROXY_PORT)) {
            return ("8118");
        }
        if (KEY.equals(PreferencesAS2.AUTH_PROXY_PASS)) {
            return ("mypass");
        }
        if (KEY.equals(PreferencesAS2.AUTH_PROXY_USER)) {
            return ("myuser");
        }
        if (KEY.equals(PreferencesAS2.AUTH_PROXY_USE)) {
            return ("FALSE");
        }
        if (KEY.equals(PreferencesAS2.PROXY_USE)) {
            return ("FALSE");
        }
        if (KEY.equals(PreferencesAS2.KEYSTORE_PASS)) {
            return ("test");
        }
        //30 minutes
        if (KEY.equals(PreferencesAS2.ASYNC_MDN_TIMEOUT)) {
            return ("30");
        }
        if (KEY.equals(PreferencesAS2.AUTO_MSG_DELETE)) {
            return ("TRUE");
        }
        if (KEY.equals(PreferencesAS2.AUTO_MSG_DELETE_LOG)) {
            return ("TRUE");
        }
        if (KEY.equals(PreferencesAS2.AUTO_MSG_DELETE_OLDERTHAN)) {
            return ("5");
        }
        if (KEY.equals(PreferencesAS2.JNDI_PORT)) {
            return ("16423");
        }
        if (KEY.equals(PreferencesAS2.MQ_PROXY_PORT)) {
            return ("16023");
        }
        if (KEY.equals(PreferencesAS2.RECEIPT_PARTNER_SUBDIR)) {
            return ("FALSE");
        }
        if (KEY.equals(CLIENTSERVER_COMM_PORT)) {
            return ("1235");
        }
        if (KEY.equals(HTTP_SEND_TIMEOUT)) {
            return ("5000");
        }
        //1.1.1970
        if (KEY.equals(LAST_UPDATE_CHECK)) {
            return ("0");
        }
        if (KEY.equals(CEM)) {
            return ("TRUE");
        }
        if (KEY.equals(COMMUNITY_EDITION)) {
            return ("TRUE");
        }
        if (KEY.equals(WRITE_OUTBOUND_STATUS_FILE)) {
            return ("FALSE");
        }
        throw new IllegalArgumentException("No defaults defined for prefs key " + KEY + " in " + this.getClass().getName());
    }

    /**Returns a single string value from the preferences or the default
     *if it is not found
     *@param key one of the class internal constants
     */
    public String get(final String KEY) {
        return (this.preferences.get(KEY, this.getDefaultValue(KEY)));
    }

    /**Stores a value in the preferences. If the passed value is null or an
     *empty string the key-value pair will be deleted from the registry.
     *@param KEY Key as defined in this class
     *@param value value to set
     */
    public void put(final String KEY, String value) {
        if (value == null || value.length() == 0) {
            this.preferences.remove(KEY);
        } else {
            this.preferences.put(KEY, value);
        }
        try {
            this.preferences.flush();
        } catch (BackingStoreException ignore) {
        }
    }

    /**Puts a value to the preferences and stores the prefs
     *@param KEY Key as defined in this class
     *@param value value to set
     */
    public void putInt(final String KEY, int value) {
        this.preferences.putInt(KEY, value);
        try {
            this.preferences.flush();
        } catch (BackingStoreException ignore) {
        }
    }

    /**Returns the value for the asked key, if noen is defined it returns
     *the default value*/
    public int getInt(final String KEY) {
        return (this.preferences.getInt(KEY,
                new Integer(this.getDefaultValue(KEY)).intValue()));
    }

    /**Puts a value to the preferences and stores the prefs
     *@param KEY Key as defined in this class
     *@param value value to set
     */
    public void putBoolean(final String KEY, boolean value) {
        this.preferences.putBoolean(KEY, value);
        try {
            this.preferences.flush();
        } catch (BackingStoreException ignore) {
        }
    }

    /**Returns the value for the asked key, if non is defined it returns
     *the default value*/
    public boolean getBoolean(final String KEY) {
        return (this.preferences.getBoolean(KEY,
                Boolean.valueOf(this.getDefaultValue(KEY)).booleanValue()));
    }

    /**Returns the value for the asked key, if noen is defined it returns
     *the second parameters value*/
    public boolean getBoolean(final String KEY, boolean defaultValue) {
        return (this.preferences.getBoolean(KEY, defaultValue));
    }
}
