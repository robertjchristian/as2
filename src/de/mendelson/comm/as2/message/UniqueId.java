//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/message/UniqueId.java,v 1.1 2012/04/18 14:10:30 heller Exp $
package de.mendelson.comm.as2.message;

import de.mendelson.comm.as2.AS2ServerVersion;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Class that ensures that a requested number is unique in the VM
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class UniqueId {

    private static long currentMessageId = 0L;
    private static long currentId = System.currentTimeMillis();

    /**Creates a new message id for the AS2 messages*/
    static public synchronized String createMessageId(String senderId, String receiverId) {
        StringBuilder idBuffer = new StringBuilder();
        idBuffer.append(AS2ServerVersion.getProductNameShortcut().replace(' ', '_'));
        idBuffer.append("-");
        idBuffer.append(String.valueOf(System.currentTimeMillis()));
        idBuffer.append("-");
        idBuffer.append(String.valueOf(currentMessageId++));
        idBuffer.append("@");
        if (senderId != null) {
            idBuffer.append(senderId);
        } else {
            idBuffer.append("unknown");
        }
        idBuffer.append("_");
        if (receiverId != null) {
            idBuffer.append(receiverId);
        } else {
            idBuffer.append("unknown");
        }
        return (idBuffer.toString());
    }

    /**Creates a new id in the format yyyyMMddHHmm-nn*/
    public static synchronized String createId(){        
        StringBuilder idBuffer = new StringBuilder();
        DateFormat format = new SimpleDateFormat("yyyyMMddHHmm");
        idBuffer.append( format.format( new Date()));
        idBuffer.append( "-" );
        idBuffer.append( currentId++ );
        return( idBuffer.toString() );
    }
}
