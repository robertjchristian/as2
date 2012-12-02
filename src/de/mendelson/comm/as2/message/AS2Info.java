//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/message/AS2Info.java,v 1.1 2012/04/18 14:10:30 heller Exp $
package de.mendelson.comm.as2.message;

import java.io.Serializable;
import java.util.Date;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Stores all information about a as2 message
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public interface AS2Info extends Serializable {
    

    public boolean isMDN();

    public int getEncryptionType();

    public Date getInitDate();

    public void setInitDate(Date initDate);

    public String getSubject();

    /**Returns the MessageId*/
    public String getMessageId();

    /**sets the messge id, unescaped*/
    public void setMessageId(String messageId);


    /**Returns the senderId, unescaped*/
    public String getSenderId();

    /**sets the sender id, unescaped*/
    public void setSenderId(String senderId);

    /**sets the receiver id, unescaped*/
    public String getReceiverId();

    /**sets the sender id, unescaped*/
    public void setReceiverId(String receiverId);


    public String getRawFilename();

    public void setRawFilename(String rawFilename);
    
    public int getDirection();

    public void setDirection(int direction);

    public int getState();

    public void setState(int state);


    public int getSignType();

    public void setSignType(int signType);


    public String getHeaderFilename();

    public void setHeaderFilename(String headerFilename);


    public String getSenderHost();

    public void setSenderHost(String senderHost);

    /**Returns the content of this object for debug purpose
     */
    public String getDebugDisplay();

    public String getUserAgent();

    public void setUserAgent(String useragent);
 

}
