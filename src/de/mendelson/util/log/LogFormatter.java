//$Header: /cvsroot-fuse/mec-as2/39/mendelson/util/log/LogFormatter.java,v 1.1 2012/04/18 14:10:45 heller Exp $
package de.mendelson.util.log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */

/**
 * Formatter to format the log of mq messages
 * @author S.Heller
 */
public class LogFormatter extends Formatter {

    private Date date = new Date();
    private MessageFormat formatter = new MessageFormat("[{0,date} {0,time}]");
    // Line separator string.  This is the value of the line.separator
    private Object args[] = new Object[1];    
    // property at the moment that the Formatter was created.
    private String lineSeparator = System.getProperty("line.separator");

    /**Sets a new message format to the formatter. The default is "{0,date} {0,time}"
     */
    public void setMessageFormat(String format) {
        this.formatter = new MessageFormat(format);
    }

    /**
     * Format the given LogRecord.
     * @param record the log record to be formatted.
     * @return a formatted log record
     */
    @Override
    public synchronized String format(LogRecord record) {
        StringBuilder builder = new StringBuilder();
        // Minimize memory allocations here.
        this.date.setTime(record.getMillis());
        this.args[0] = date;
        StringBuffer textBuffer = new StringBuffer();
        this.formatter.format(args, textBuffer, null);
        builder.append(textBuffer);
        builder.append(" ");
        builder.append(formatMessage(record));
        builder.append(this.lineSeparator);
        if (record.getThrown() != null) {
            try {
                StringWriter stringWriter = new StringWriter();
                PrintWriter printWriter = new PrintWriter(stringWriter);
                record.getThrown().printStackTrace(printWriter);
                printWriter.close();
                builder.append(stringWriter.toString());
            } catch (Exception ex) {
            }
        }
        return (builder.toString());
    }
}

