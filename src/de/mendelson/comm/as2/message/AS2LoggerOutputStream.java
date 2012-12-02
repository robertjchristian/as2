//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/message/AS2LoggerOutputStream.java,v 1.1 2012/04/18 14:10:30 heller Exp $
package de.mendelson.comm.as2.message;

import java.io.OutputStream;
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
 * OutputStream that writes to a passed logger. This is useful as wrapper for modules
 * that should use a logger but log to a printStream. In this case you could create
 * a PrintStream Object on the Logger by calling
 * new PrintStream( LogOutputStream );
 * @author S.Heller
 */
public class AS2LoggerOutputStream extends OutputStream{
    
    /**Level to use for the logging: All print output will be written
     *to the log unsing this level. Mainly this is INFO
     */
    private Level level = null;
    
    /**Logger to write the output to*/
    private Logger logger = null;
    
    /**Buffer to store the text contents*/
    private StringBuffer text = new StringBuffer();
    
    private AS2MessageInfo messageInfo;
        
    /**@param logger Logger to write the logging to, Level is set to INFO
     */
    public AS2LoggerOutputStream( Logger logger, AS2MessageInfo messageInfo ){
        this.logger = logger;
        this.level = Level.INFO;
        this.messageInfo = messageInfo;
    }
    
    /**Map int to characters*/
    private final char int2char( int i ) {
        return (char) ( (i < 0)?i+0x100:i ) ;
    }
    
    @Override
    public void write( int i ){
        char value = (char)this.int2char(i);
        //update only on println
        if( (byte)i == (byte)'\n' ){
            this.text.insert( 0, " [Shell]:" );
            this.text.insert( 0, this.messageInfo.getMessageId() );
            this.logger.log( this.level, this.text.toString(), this.messageInfo );
            //delete the actual buffer
            this.text = new StringBuffer();
        } else
            this.text.append( value );
        
    }
}


