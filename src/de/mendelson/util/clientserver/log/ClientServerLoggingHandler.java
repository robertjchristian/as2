//$Header: /cvsroot-fuse/mec-as2/39/mendelson/util/clientserver/log/ClientServerLoggingHandler.java,v 1.1 2012/04/18 14:10:45 heller Exp $
package de.mendelson.util.clientserver.log;

import de.mendelson.util.clientserver.ClientServerSessionHandler;
import java.util.logging.ErrorManager;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Handler to log logger data via the client-server interface
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class ClientServerLoggingHandler extends Handler {

    private ClientServerSessionHandler sessionHandler;

    public ClientServerLoggingHandler(ClientServerSessionHandler sessionHandler) {
        this.sessionHandler = sessionHandler;
        this.setErrorManager(new ClientServerLoggingErrorHandler());
    }

    /**
     * Set (or change) the character encoding used by this <tt>Handler</tt>.
     * <p>
     * The encoding should be set before any <tt>LogRecords</tt> are written
     * to the <tt>Handler</tt>.
     *
     * @param encoding  The name of a supported character encoding.
     *	      May be null, to indicate the default platform encoding.
     * @exception  SecurityException  if a security manager exists and if
     *             the caller does not have <tt>LoggingPermission("control")</tt>.
     * @exception  UnsupportedEncodingException if the named encoding is
     *		not supported.
     */
    @Override
    public void setEncoding(String encoding)
            throws SecurityException, java.io.UnsupportedEncodingException {
        super.setEncoding(encoding);
    }

    /**
     * Format and publish a LogRecord.
     * @param  record  description of the log event
     */
    @Override
    public synchronized void publish(LogRecord record) {
        if (!this.isLoggable(record)) {
            return;
        }
        try {
            this.logMessage(record.getLevel(), record.getMessage(),
                    record.getParameters());
        } catch (Exception ex) {
            // We don't want to throw an exception here, but we
            // report the exception to any registered ErrorManager.
            reportError(null, ex, ErrorManager.WRITE_FAILURE);
        }
    }

    /**
     * Check if this Handler would actually log a given LogRecord, depending of the
     * log level
     * @param record a LogRecord
     * @return true if the LogRecord would be logged.
     *
     */
    @Override
    public boolean isLoggable(LogRecord record) {
        return super.isLoggable(record);
    }

    /**
     * Flush any buffered messages.
     */
    @Override
    public synchronized void flush() {
    }

    /**Just flushes the current message
     */
    @Override
    public synchronized void close() throws SecurityException {
        this.flush();
    }

    /**Finally logs the passed message to the text component and sets the canvas pos
     */
    private synchronized void logMessage(Level level, String message, Object[] parameter) {
        this.sessionHandler.broadcastLogMessage(level, message, parameter);
    }
}
