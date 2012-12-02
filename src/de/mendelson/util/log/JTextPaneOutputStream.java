//$Header: /cvsroot-fuse/mec-as2/39/mendelson/util/log/JTextPaneOutputStream.java,v 1.1 2012/04/18 14:10:45 heller Exp $
package de.mendelson.util.log;

import java.io.OutputStream;
import java.util.Date;
import java.text.SimpleDateFormat;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */

/**
 * PrintStream to write to a JTextPane component, for logging purpose in swing 
 * environment
 * @author S.Heller
 */
public class JTextPaneOutputStream extends OutputStream {

    /**Format the out log if requested*/
    private SimpleDateFormat format = null;
    /**Component to write into*/
    private JTextPane jTextPane = null;
    /**Buffer to store the text contents*/
    private StringBuilder text = new StringBuilder();

    /**@param jTextPane text component to append data to*/
    public JTextPaneOutputStream(JTextPane jTextPane) {
        this.jTextPane = jTextPane;
        this.format = new SimpleDateFormat("[HH:mm:ss]");
    }

    /**Map int to characters*/
    private char int2char(int i) {
        return (char) ((i < 0) ? i + 0x100 : i);
    }

    @Override
    public void write(int i) {
        this.text.append(this.int2char(i));
        //update only on println
        if ((byte) i == (byte) '\n') {
            try {
                //this method is much much faster than the
                //this.jEditorPane.setText(  this.text.toString() );
                //method
                final int documentLength = this.jTextPane.getDocument().getLength();
                this.jTextPane.getDocument().insertString(
                        documentLength,
                        this.format.format(new Date()) + " " + this.text.toString(),
                        null);
                //delete the actual buffer
                this.text = new StringBuilder();
                //scroll to the last line
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        JTextPaneOutputStream.this.jTextPane.setCaretPosition(documentLength);
                    }
                });
            } catch (Exception ignore) {
            }
        }
    }
}