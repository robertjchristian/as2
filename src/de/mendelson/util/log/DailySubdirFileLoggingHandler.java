//$Header: /cvsroot-fuse/mec-as2/39/mendelson/util/log/DailySubdirFileLoggingHandler.java,v 1.1 2012/04/18 14:10:45 heller Exp $
package de.mendelson.util.log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
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
 * Handler to log logger data to a file in a daily subdirectory. A log dir and a log file name
 * could be passed to this class, the log will be written to
 * logDir/yyMMdd/logfilename
 * Sample:
 * logger.addHandler( new DailySubdirFileLoggingHandler(new File("mylogdir"), "mylogfile.log") );
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class DailySubdirFileLoggingHandler extends Handler {

    private boolean doneHeader;
    private BufferedWriter writer = null;
    private DateFormat logDateFormat = new SimpleDateFormat("yyyyMMdd");
    private File logDir;
    private String logfileName;
    //stores the actual log file name that is used to write log to
    private String actualLogFilename = null;


    public DailySubdirFileLoggingHandler(File logDir, String logfileName) {
        this.logDir = logDir;
        this.logfileName = logfileName;
        this.setFormatter(new LogFormatter());
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
        if (!isLoggable(record)) {
            return;
        }
        String msg;
        int rawMessageLength = 0;
        try {
            msg = this.getFormatter().format(record);
            String rawMessage = this.getFormatter().formatMessage(record);
            if (rawMessage != null) {
                rawMessageLength = rawMessage.length();
            }
        } catch (Exception ex) {
            // We don't want to throw an exception here, but we
            // report the exception to any registered ErrorManager.
            reportError(null, ex, ErrorManager.FORMAT_FAILURE);
            return;
        }
        try {
            if (!doneHeader) {
                this.logMessage(record.getLevel(), this.getFormatter().getHead(this), rawMessageLength);
                doneHeader = true;
            }
            this.logMessage(record.getLevel(), msg, rawMessageLength);
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

    private File getFullLogDir() {
        StringBuilder path = new StringBuilder();
        path.append(this.logDir.getAbsolutePath());
        path.append(File.separator);
        path.append( this.logDateFormat.format( new Date()));
        return( new File( path.toString()));
    }

    /**Finally logs the passed message to the text component and sets the canvas pos
     */
    private synchronized void logMessage(Level level, String message, int rawMessageLength) {
        File fullLogDir = this.getFullLogDir();
        String newLogFilename = fullLogDir.getAbsolutePath() + File.separator + this.logfileName;
        //check if the loggers output stream is still valid        
        if (this.writer == null || this.actualLogFilename == null || !newLogFilename.equals( this.actualLogFilename)) {
            if( this.writer != null ){
                try{
                    this.writer.flush();
                    this.writer.close();
                }
                catch( Exception e ){
                    e.printStackTrace();
                }
            }
            if( !fullLogDir.exists()){
                fullLogDir.mkdirs();
            }
            try{
                this.writer = new BufferedWriter( new FileWriter(newLogFilename, true ));
                this.actualLogFilename = newLogFilename;
            }
            catch( Exception e ){
                e.printStackTrace();
            }
        }
        try{
            this.writer.write( message );
            this.writer.flush();
        }
        catch( Exception e ){
            e.printStackTrace();
        }
    }
}
