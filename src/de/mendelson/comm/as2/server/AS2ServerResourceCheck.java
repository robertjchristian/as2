//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/server/AS2ServerResourceCheck.java,v 1.1 2012/04/18 14:10:39 heller Exp $
package de.mendelson.comm.as2.server;

import de.mendelson.comm.as2.preferences.PreferencesAS2;
import de.mendelson.util.MecResourceBundle;
import java.io.IOException;
import java.net.Socket;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Class that checks resources of the host before starting the server
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class AS2ServerResourceCheck {

    private MecResourceBundle rb = null;

    public AS2ServerResourceCheck() {
        //Load default resourcebundle
        try {
            this.rb = (MecResourceBundle) ResourceBundle.getBundle(
                    ResourceBundleAS2ServerResourceCheck.class.getName());
        } //load up  resourcebundle
        catch (MissingResourceException e) {
            throw new RuntimeException("Oops..resource bundle " + e.getClassName() + " not found.");
        }
    }

    /**Checks if a required port is used by another process*/
    public void performPortCheck() throws Exception {
        PreferencesAS2 preferences = new PreferencesAS2();
        int[] ports = new int[]{
            preferences.getInt(PreferencesAS2.CLIENTSERVER_COMM_PORT),
            preferences.getInt(PreferencesAS2.JNDI_PORT),
            preferences.getInt(PreferencesAS2.MQ_PROXY_PORT),
            preferences.getInt(PreferencesAS2.SERVER_DB_PORT),
            preferences.getInt(PreferencesAS2.SERVER_RMI_PORT)
        };
        for (int port : ports) {
            this.checkPort(port);
        }
    }

    private void checkPort(int port) throws Exception {
        try {
            Socket socket = new Socket("localhost", port);
            socket.close();
            throw new Exception(this.rb.getResourceString("port.in.use", String.valueOf(port)));
        } catch (IOException ex) {
            // The host is not listening on this port
        }
    }

}
