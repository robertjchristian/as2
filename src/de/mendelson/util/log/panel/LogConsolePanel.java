//$Header: /cvsroot-fuse/mec-as2/39/mendelson/util/log/panel/LogConsolePanel.java,v 1.1 2012/04/18 14:10:45 heller Exp $
package de.mendelson.util.log.panel;


import de.mendelson.util.MecResourceBundle;
import de.mendelson.util.log.IRCColors;
import de.mendelson.util.log.JTextPaneLoggingHandler;
import de.mendelson.util.log.JTextPaneOutputStream;
import de.mendelson.util.log.LogFormatter;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * The frame system output/debug info is written to
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class LogConsolePanel extends JPanel implements ClipboardOwner {

    public static final String COLOR_BLACK = IRCColors.BLACK;
    public static final String COLOR_BLUE = IRCColors.BLUE;
    public static final String COLOR_BROWN = IRCColors.BROWN;
    public static final String COLOR_CYAN = IRCColors.CYAN;
    public static final String COLOR_DARK_BLUE = IRCColors.DARK_BLUE;
    public static final String COLOR_DARK_GRAY = IRCColors.DARK_GRAY;
    public static final String COLOR_DARK_GREEN = IRCColors.DARK_GREEN;
    public static final String COLOR_GREEN = IRCColors.GREEN;
    public static final String COLOR_LIGHT_GRAY = IRCColors.LIGHT_GRAY;
    public static final String COLOR_MAGENTA = IRCColors.MAGENTA;
    public static final String COLOR_OLIVE = IRCColors.OLIVE;
    public static final String COLOR_PURPLE = IRCColors.PURPLE;
    public static final String COLOR_RED = IRCColors.RED;
    public static final String COLOR_TEAL = IRCColors.TEAL;
    public static final String COLOR_WHITE = IRCColors.WHITE;
    public static final String COLOR_YELLOW = IRCColors.YELLOW;

    /**PrintStream to write in, this is just a wrapper to the internal logger.
     */
    private PrintStream out = null;
    /**Logger to log to*/
    private Logger logger;
    /**ResourceBundle to localize this GUI*/
    private MecResourceBundle rb;
    private JTextPaneLoggingHandler handler;


    public LogConsolePanel( Logger logger, Formatter logFormatter ) {
        //load resource bundle
        try {
            this.rb = (MecResourceBundle) ResourceBundle.getBundle(
                    ResourceBundleLogConsole.class.getName());
        } catch (MissingResourceException e) {
            throw new RuntimeException("Oops..resource bundle " + e.getClassName() + " not found.");
        }
        this.initComponents();
        this.initialize(logger, logFormatter);
    }
    
    public LogConsolePanel( Logger logger ) {        
        //load resource bundle
        try {
            this.rb = (MecResourceBundle) ResourceBundle.getBundle(
                    ResourceBundleLogConsole.class.getName());
        } catch (MissingResourceException e) {
            throw new RuntimeException("Oops..resource bundle " + e.getClassName() + " not found.");
        }
        this.initComponents();
        LogFormatter logFormatter = new LogFormatter();
        logFormatter.setMessageFormat("[{0,time}]");
        this.initialize(logger, logFormatter);
    }

    private void initialize(Logger logger, Formatter logFormatter){
        this.logger = logger;
        this.logger.setUseParentHandlers(false);
        OutputStream logStream = new JTextPaneOutputStream(this.jTextPane);
        this.out = new PrintStream(logStream);        
        this.handler = new JTextPaneLoggingHandler(this.jTextPane, logFormatter);
        this.setDefaultColors();
        this.logger.addHandler(handler);
        this.jPopupMenu.setInvoker(this.jTextPane);
    }

    /**Sets some default colors for the logger levels. Overwrite these colors using the setColor method
     *
     */
    public void setDefaultColors(){        
        this.handler.setColor(Level.SEVERE, COLOR_RED);
        this.handler.setColor(Level.WARNING, COLOR_BROWN);
        this.handler.setColor(Level.INFO, COLOR_BLACK);
        this.handler.setColor(Level.CONFIG, COLOR_DARK_GREEN);
        this.handler.setColor(Level.FINE, COLOR_BLUE);
        this.handler.setColor(Level.FINER, COLOR_PURPLE);
        this.handler.setColor(Level.FINEST, COLOR_LIGHT_GRAY);
    }

    /**Sets a special color for a special log level. Please use the class constant values*/
    public void setColor( Level level, String color ){
        this.handler.setColor(level, color);
    }

    /**returns the printstream to write output data to*/
    public PrintStream getPrintStream() {
        return (this.out);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPopupMenu = new javax.swing.JPopupMenu();
        jMenuItemClear = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        jMenuItemCopyToClipBoard = new javax.swing.JMenuItem();
        jScrollPane = new javax.swing.JScrollPane();
        jTextPane = new javax.swing.JTextPane();

        jMenuItemClear.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/mendelson/util/log/panel/clear_16x16.gif"))); // NOI18N
        jMenuItemClear.setText(this.rb.getResourceString( "label.clear" ));
        jMenuItemClear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemClearActionPerformed(evt);
            }
        });
        jPopupMenu.add(jMenuItemClear);
        jPopupMenu.add(jSeparator1);

        jMenuItemCopyToClipBoard.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/mendelson/util/log/panel/paste_16x16.gif"))); // NOI18N
        jMenuItemCopyToClipBoard.setText(this.rb.getResourceString( "label.toclipboard" ));
        jMenuItemCopyToClipBoard.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemCopyToClipBoardActionPerformed(evt);
            }
        });
        jPopupMenu.add(jMenuItemCopyToClipBoard);

        setLayout(new java.awt.GridBagLayout());

        jScrollPane.setPreferredSize(new java.awt.Dimension(300, 100));

        jTextPane.setEditable(false);
        jTextPane.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jTextPaneMousePressed(evt);
            }
        });
        jScrollPane.setViewportView(jTextPane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jScrollPane, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    private void jMenuItemCopyToClipBoardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemCopyToClipBoardActionPerformed
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(new StringSelection(this.jTextPane.getText()), this);
    }//GEN-LAST:event_jMenuItemCopyToClipBoardActionPerformed

    private void jMenuItemClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemClearActionPerformed
        this.jTextPane.setText("");
    }//GEN-LAST:event_jMenuItemClearActionPerformed

    private void jTextPaneMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTextPaneMousePressed
        if (evt.isPopupTrigger() || evt.isMetaDown()) {
            this.jPopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
        }
    }//GEN-LAST:event_jTextPaneMousePressed

    @Override
    public void lostOwnership(Clipboard clipboard, Transferable transferable) {
    //Clipboard contents replaced, dont care!
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem jMenuItemClear;
    private javax.swing.JMenuItem jMenuItemCopyToClipBoard;
    private javax.swing.JPopupMenu jPopupMenu;
    private javax.swing.JScrollPane jScrollPane;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTextPane jTextPane;
    // End of variables declaration//GEN-END:variables

    /**
     * @return the handler
     */
    public JTextPaneLoggingHandler getHandler() {
        return handler;
    }
}
