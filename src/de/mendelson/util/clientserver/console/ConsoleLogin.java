//$Header: /cvsroot-fuse/mec-as2/39/mendelson/util/clientserver/console/ConsoleLogin.java,v 1.1 2012/04/18 14:10:44 heller Exp $
package de.mendelson.util.clientserver.console;

import java.io.Console;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Class that performs a login on the console
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class ConsoleLogin {

    private Console console;
    private char[] pass = null;
    private String user = null;

    public ConsoleLogin(Console console) {
        this.console = console;
    }

    public void readUserPass() {
        while (this.getUser() == null || this.getPass() == null) {
            this.user = console.readLine("Enter your user name: ");
            if (this.getUser() == null || this.getUser().length() == 0) {
                System.out.println();
                System.out.println("No user name entered");
                this.user = null;
            } else {
                this.pass = console.readPassword("Enter your password: ");
                if (this.getPass() == null || getPass().length == 0 ) {
                    System.out.println();
                    System.out.println("No password entered");
                    this.pass = null;
                }
            }
        }
    }

    
    /**
     * @return the pass
     */
    public char[] getPass() {
        return pass;
    }

    /**
     * @return the user
     */
    public String getUser() {
        return user;
    }
}
