//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/log/LogEntry.java,v 1.1 2012/04/18 14:10:30 heller Exp $
package de.mendelson.comm.as2.log;

import de.mendelson.comm.as2.server.AS2Server;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Enwrapps a single db log entry in an object
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class LogEntry {

    /**Logger to log inforamtion to*/
    private Logger logger = Logger.getLogger(AS2Server.SERVER_LOGGER_NAME);
    private Level level;
    private String message;
    private long millis;
    private String messageId;

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getMillis() {
        return millis;
    }

    public void setMillis(long millis) {
        this.millis = millis;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
}
