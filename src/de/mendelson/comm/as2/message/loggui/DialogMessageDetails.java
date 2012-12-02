//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/message/loggui/DialogMessageDetails.java,v 1.1 2012/04/18 14:10:30 heller Exp $
package de.mendelson.comm.as2.message.loggui;

import de.mendelson.comm.as2.log.LogAccessDB;
import de.mendelson.comm.as2.log.LogEntry;
import de.mendelson.comm.as2.message.AS2Info;
import de.mendelson.comm.as2.message.AS2MessageInfo;
import de.mendelson.comm.as2.message.AS2Payload;
import de.mendelson.comm.as2.message.MessageAccessDB;
import de.mendelson.util.log.IRCColors;
import java.text.SimpleDateFormat;
import de.mendelson.util.MecResourceBundle;
import de.mendelson.util.clientserver.BaseClient;
import de.mendelson.util.tables.JTableColumnResizer;
import java.awt.Color;
import java.sql.Connection;
import java.text.DateFormat;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Dialog to show the about info
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class DialogMessageDetails extends JDialog implements ListSelectionListener {

    private Logger logger = Logger.getLogger("de.mendelson.as2.client");
    /**Localize the GUI*/
    private MecResourceBundle rb = null;
    /**Stores information about the message
     */
    private AS2MessageInfo overviewInfo = null;
    /**Stores the payloads*/
    private List<AS2Payload> payload = null;
    private JPanelFileDisplay jPanelFileDisplayRaw;
    private JPanelFileDisplay jPanelFileDisplayHeader;
    private JPanelFileDisplay[] jPanelFileDisplayPayload;
    private LogAccessDB logAccess;
    //db connection
    private Connection runtimeConnection;
    private Connection configConnection;

    /** Creates new form AboutDialog */
    public DialogMessageDetails(JFrame parent, Connection configConnection, Connection runtimeConnection,
            BaseClient baseClient, AS2MessageInfo overviewInfo, List<AS2Payload> payload) {
        super(parent, true);
        this.runtimeConnection = runtimeConnection;
        this.configConnection = configConnection;
        //load resource bundle
        try {
            this.rb = (MecResourceBundle) ResourceBundle.getBundle(
                    ResourceBundleMessageDetails.class.getName());
        } catch (MissingResourceException e) {
            throw new RuntimeException("Oops..resource bundle "
                    + e.getClassName() + " not found.");
        }
        this.jPanelFileDisplayRaw = new JPanelFileDisplay(baseClient);
        this.jPanelFileDisplayHeader = new JPanelFileDisplay(baseClient);
        this.payload = payload;
        this.overviewInfo = overviewInfo;
        this.setTitle(this.rb.getResourceString("title"));
        this.initComponents();
        this.jLabelAS2MessageInfo.setText(overviewInfo.getMessageId());
        this.getRootPane().setDefaultButton(this.jButtonOk);
        this.jTableMessageDetails.getTableHeader().setReorderingAllowed(false);
        //first icon
        TableColumn column = this.jTableMessageDetails.getColumnModel().getColumn(0);
        column.setMaxWidth(20);
        column.setResizable(false);
        column = this.jTableMessageDetails.getColumnModel().getColumn(2);
        column.setMaxWidth(20);
        column.setResizable(false);
        this.displayData(overviewInfo);
        this.jTabbedPane.addTab(this.rb.getResourceString("message.raw.decrypted"), jPanelFileDisplayRaw);
        this.jTabbedPane.addTab(this.rb.getResourceString("message.header"), jPanelFileDisplayHeader);
        this.jPanelFileDisplayPayload = new JPanelFileDisplay[payload.size()];
        for (int i = 0; i < this.payload.size(); i++) {
            this.jPanelFileDisplayPayload[i] = new JPanelFileDisplay(baseClient);
            if (payload.size() == 1) {
                this.jTabbedPane.addTab(this.rb.getResourceString("message.payload"), jPanelFileDisplayPayload[i]);
            } else {
                this.jTabbedPane.addTab(this.rb.getResourceString("message.payload.multiple",
                        String.valueOf(i + 1)), jPanelFileDisplayPayload[i]);
            }
        }
        this.jTableMessageDetails.getSelectionModel().addListSelectionListener(this);
        try {
            this.logAccess = new LogAccessDB(this.configConnection, this.runtimeConnection);
        } catch (Exception e) {
            this.logger.severe(e.getMessage());
        }
        this.displayProcessLog();
        JTableColumnResizer.adjustColumnWidthByContent(this.jTableMessageDetails);
        this.jTableMessageDetails.getSelectionModel().setSelectionInterval(0, 0);
    }

    /**Displays the message details log*/
    private void displayProcessLog() {
        StyledDocument document = (StyledDocument) this.jTextPaneLog.getDocument();
        StyleContext context = StyleContext.getDefaultStyleContext();
        Style currentStyle = context.getStyle(StyleContext.DEFAULT_STYLE);
        Color defaultColor = (Color) currentStyle.getAttribute(StyleConstants.Foreground);
        LogEntry[] entries = this.logAccess.getLog(overviewInfo.getMessageId());
        StringBuilder buffer = new StringBuilder();
        DateFormat format = SimpleDateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
        for (int i = 0; i < entries.length; i++) {
            currentStyle.removeAttribute(StyleConstants.Foreground);
            currentStyle.addAttribute(StyleConstants.Foreground, defaultColor);
            buffer.append("[").append(format.format(entries[i].getMillis())).append("] ");
            try {
                document.insertString(document.getLength(), buffer.toString(), currentStyle);
            } catch (Throwable ignore) {
                //nop
            }
            buffer.setLength(0);
            if (entries[i].getLevel().equals(Level.WARNING)) {
                currentStyle.addAttribute(StyleConstants.Foreground, IRCColors.COLOR_NAVY.brighter());
            } else if (entries[i].getLevel().equals(Level.SEVERE)) {
                currentStyle.addAttribute(StyleConstants.Foreground, IRCColors.COLOR_RED);
            } else if (entries[i].getLevel().equals(Level.FINE)) {
                currentStyle.addAttribute(StyleConstants.Foreground, IRCColors.COLOR_GREEN);
            } else {
                currentStyle.addAttribute(StyleConstants.Foreground, defaultColor);
            }
            buffer.append(entries[i].getMessage()).append("\n");
            try {
                document.insertString(document.getLength(), buffer.toString(), currentStyle);
            } catch (Throwable ignore) {
                //nop
            }
            buffer.setLength(0);
            currentStyle.removeAttribute(StyleConstants.Foreground);
            currentStyle.addAttribute(StyleConstants.Foreground, defaultColor);
        }
    }

    /**Displays all messages that contain to the passed overview object*/
    private void displayData(AS2MessageInfo overviewRow) {
        try {
            MessageAccessDB messageAccess = new MessageAccessDB(this.configConnection, this.runtimeConnection);
            List<AS2Info> details = messageAccess.getMessageDetails(overviewRow.getMessageId());
            ((TableModelMessageDetails) this.jTableMessageDetails.getModel()).passNewData(details);
        } catch (Exception e) {
            JFrame parent = (JFrame) SwingUtilities.getAncestorOfClass(JFrame.class, this);
            JOptionPane.showMessageDialog(parent, e.getMessage());
        }
    }

    /**ListSelectionListener*/
    @Override
    public void valueChanged(ListSelectionEvent listSelectionEvent) {
        int selectedRow = this.jTableMessageDetails.getSelectedRow();
        if (selectedRow >= 0) {
            AS2Info info = ((TableModelMessageDetails) this.jTableMessageDetails.getModel()).getRow(selectedRow);
            String rawFileName = null;
            if (!info.isMDN()) {
                AS2MessageInfo messageInfo = (AS2MessageInfo) info;
                if (messageInfo.getRawFilenameDecrypted() != null) {
                    rawFileName = messageInfo.getRawFilenameDecrypted();
                } else if (messageInfo.getRawFilename() != null) {
                    rawFileName = messageInfo.getRawFilename();
                }
            } else {
                if (info.getRawFilename() != null) {
                    rawFileName = info.getRawFilename();
                }
            }
            this.jPanelFileDisplayRaw.displayFile(rawFileName);
            String headerFilename = null;
            if (info.getHeaderFilename() != null) {
                headerFilename = info.getHeaderFilename();
            }
            this.jPanelFileDisplayHeader.displayFile(headerFilename);
            try {
                if (this.payload.size() > 0) {
                    for (int i = 0; i < payload.size(); i++) {
                        String payloadFilename = this.payload.get(i).getPayloadFilename();
                        this.jPanelFileDisplayPayload[i].displayFile(payloadFilename);
                    }
                }
            } catch (Exception e) {
                //nop
            }

        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanelMain = new javax.swing.JPanel();
        jPanelHeader = new javax.swing.JPanel();
        jLabelAS2MessageInfo = new javax.swing.JLabel();
        jPanelInfo = new javax.swing.JPanel();
        jSplitPane = new javax.swing.JSplitPane();
        jScrollPaneList = new javax.swing.JScrollPane();
        jTableMessageDetails = new javax.swing.JTable();
        jTabbedPane = new javax.swing.JTabbedPane();
        jPanelProcessLog = new javax.swing.JPanel();
        jScrollPaneLog = new javax.swing.JScrollPane();
        jTextPaneLog = new javax.swing.JTextPane();
        jPanelButton = new javax.swing.JPanel();
        jButtonOk = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        jPanelMain.setLayout(new java.awt.GridBagLayout());

        jPanelHeader.setLayout(new java.awt.GridBagLayout());

        jLabelAS2MessageInfo.setText("<Info>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        jPanelHeader.add(jLabelAS2MessageInfo, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelMain.add(jPanelHeader, gridBagConstraints);

        jPanelInfo.setLayout(new java.awt.GridBagLayout());

        jSplitPane.setDividerLocation(200);
        jSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        jTableMessageDetails.setModel(new TableModelMessageDetails());
        jTableMessageDetails.setShowHorizontalLines(false);
        jTableMessageDetails.setShowVerticalLines(false);
        jScrollPaneList.setViewportView(jTableMessageDetails);

        jSplitPane.setLeftComponent(jScrollPaneList);

        jPanelProcessLog.setLayout(new java.awt.GridBagLayout());

        jTextPaneLog.setEditable(false);
        jScrollPaneLog.setViewportView(jTextPaneLog);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanelProcessLog.add(jScrollPaneLog, gridBagConstraints);

        jTabbedPane.addTab(this.rb.getResourceString( "tab.log"), jPanelProcessLog);

        jSplitPane.setRightComponent(jTabbedPane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanelInfo.add(jSplitPane, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelMain.add(jPanelInfo, gridBagConstraints);

        jButtonOk.setFont(new java.awt.Font("Dialog", 0, 12));
        jButtonOk.setText(this.rb.getResourceString( "button.ok" ));
        jButtonOk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOkActionPerformed(evt);
            }
        });
        jPanelButton.add(jButtonOk);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        jPanelMain.add(jPanelButton, gridBagConstraints);

        getContentPane().add(jPanelMain, java.awt.BorderLayout.CENTER);

        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width-929)/2, (screenSize.height-565)/2, 929, 565);
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonOkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOkActionPerformed
        this.setVisible(false);
        this.dispose();
    }//GEN-LAST:event_jButtonOkActionPerformed

    /** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        this.setVisible(false);
        this.dispose();
    }//GEN-LAST:event_closeDialog
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonOk;
    private javax.swing.JLabel jLabelAS2MessageInfo;
    private javax.swing.JPanel jPanelButton;
    private javax.swing.JPanel jPanelHeader;
    private javax.swing.JPanel jPanelInfo;
    private javax.swing.JPanel jPanelMain;
    private javax.swing.JPanel jPanelProcessLog;
    private javax.swing.JScrollPane jScrollPaneList;
    private javax.swing.JScrollPane jScrollPaneLog;
    private javax.swing.JSplitPane jSplitPane;
    private javax.swing.JTabbedPane jTabbedPane;
    private javax.swing.JTable jTableMessageDetails;
    private javax.swing.JTextPane jTextPaneLog;
    // End of variables declaration//GEN-END:variables
}
