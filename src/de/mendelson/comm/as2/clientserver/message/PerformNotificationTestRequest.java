//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/clientserver/message/PerformNotificationTestRequest.java,v 1.1 2012/04/18 14:10:27 heller Exp $
package de.mendelson.comm.as2.clientserver.message;

import de.mendelson.comm.as2.notification.NotificationData;
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
public class PerformNotificationTestRequest extends ClientServerMessage implements Serializable{
    
    private NotificationData notificationData;
    
    public PerformNotificationTestRequest(NotificationData notificationData){
        this.notificationData = notificationData;
    }
    
    @Override
    public String toString(){
        return( "Perform notification test" );
    }

    /**
     * @return the data
     */
    public NotificationData getNotificationData() {
        return notificationData;
    }

    
}
