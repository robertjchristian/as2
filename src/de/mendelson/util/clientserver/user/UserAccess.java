//$Header: /cvsroot-fuse/mec-as2/39/mendelson/util/clientserver/user/UserAccess.java,v 1.1 2012/04/18 14:10:45 heller Exp $
package de.mendelson.util.clientserver.user;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.util.logging.Logger;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Contains several utilities for the user access
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class UserAccess {

    private File passwdFile = new File("passwd");
    private Logger logger;

    public UserAccess(Logger logger) {
        this.logger = logger;
    }

    public User addUser(String userName, char[] password) throws Exception {
        if (this.readUser(userName) != null) {
            throw new Exception("User \"" + userName + "\" does already exist.");
        }
        User user = new User();
        user.setName(userName);
        user.setPasswdCrypted(User.cryptPassword(password));
        for (int i = 0; i < 3; i++) {
            user.setPermission(i, "");
        }
        RandomAccessFile file = new RandomAccessFile(this.passwdFile, "rw");
        file.seek(this.passwdFile.length());
        String newLine = User.serialize(user);
        file.writeBytes("\n");
        file.writeBytes(newLine);
        file.writeBytes("\n");
        file.close();
        return( user );
    }


    /**Reads a user from of the actual passwd file*/
    public User readUser(String userName) {
        User user = null;
        String userLine = this.readUserLine(userName);
        if (userLine != null) {            
            user = User.parse(userLine);
        }
        return (user);
    }

    /**Loads the passwd file and looks for the user
     *a single line should be like:
     *username:passwd:passwdcrypted:permission1 (1/0):permission2 (1/0):permission3 (1/0):permissionn (1/0)
     */
    public String readUserLine(String userName) {
        BufferedReader bufferedReader = null;
        try {
            Reader reader = new InputStreamReader(new FileInputStream(this.passwdFile), "UTF-8");
            bufferedReader = new BufferedReader(reader);
            String line = "";
            while (line != null) {
                line = bufferedReader.readLine();
                if (line != null) {
                    if (line.startsWith("#")) {
                        continue;
                    }
                    String[] token = line.split(":");
                    if (token[0].equalsIgnoreCase(userName)) {
                        return (line);
                    }
                }
            }
        } catch (Exception e) {
            this.logger.warning("Password storage read error: " + e.getMessage());
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (Exception ee) {
                    //nop
                }
            }
        }
        return (null);
    }
}
