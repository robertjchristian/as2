//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/AS2ShutdownThread.java,v 1.1 2012/04/18 14:10:16 heller Exp $
package de.mendelson.comm.as2;

import de.mendelson.comm.as2.database.DBServer;
/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
import de.mendelson.comm.as2.server.AS2Server;
import java.io.File;

/**
 * Thread that is executed if the VM will shut down (that means the server
 * is shut down)
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class AS2ShutdownThread extends Thread {

    private DBServer dbServer;

    public AS2ShutdownThread(DBServer dbServer) {
        this.dbServer = dbServer;
    }

    /**This will start the thread, it is called if the JVM shutdown is detected*/
    @Override
    public void run() {        
        try {
            this.dbServer.shutdown();
        } catch (Throwable e) {
            //nop
        }
        //delete lock file
        AS2Server.deleteLockFile();
        System.out.println(AS2ServerVersion.getProductName() + " shutdown.");
    }
}
