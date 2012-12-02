//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/cem/gui/DialogCEMOverview.java,v 1.1 2012/04/18 14:10:20 heller Exp $
package de.mendelson.comm.as2.cem.gui;

import de.mendelson.comm.as2.cem.CEMAccessDB;
import de.mendelson.comm.as2.cem.CEMEntry;
import de.mendelson.comm.as2.clientserver.message.RefreshClientCEMDisplay;
import de.mendelson.util.security.cert.CertificateManager;
import de.mendelson.util.security.cert.KeystoreCertificate;
import de.mendelson.comm.as2.log.LogAccessDB;
import de.mendelson.comm.as2.message.AS2MessageInfo;
import de.mendelson.comm.as2.message.AS2Payload;
import de.mendelson.comm.as2.message.MessageAccessDB;
import de.mendelson.comm.as2.message.loggui.DialogMessageDetails;
import de.mendelson.comm.as2.partner.gui.TableCellRendererPartner;
import de.mendelson.comm.as2.preferences.PreferencesAS2;
import de.mendelson.util.MecResourceBundle;
import de.mendelson.util.clientserver.ClientsideMessageProcessor;
import de.mendelson.util.clientserver.GUIClient;
import de.mendelson.util.clientserver.clients.preferences.PreferencesClient;
import de.mendelson.util.clientserver.messages.ClientServerMessage;
import de.mendelson.util.security.BCCryptoHelper;
import de.mendelson.util.security.cert.KeystoreStorage;
import de.mendelson.util.security.cert.TableCellRendererCertificates;
import de.mendelson.util.security.cert.clientserver.KeystoreStorageImplClientServer;
import de.mendelson.util.tables.JTableColumnResizer;
import java.sql.Connection;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Gives an overview on all CEM messages
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class DialogCEMOverview extends JDialog implements ListSelectionListener, ClientsideMessageProcessor {

    /**Manages all internal certificates*/
    private CertificateManager certificateManager;
    /**DB connection of the application*/
    private Connection configConnection;
    private Connection runtimeConnection;
    /**localizes the GUI*/
    private MecResourceBundle rb;
    private GUIClient guiClient;
    private Logger logger = Logger.getLogger("de.mendelson.as2.client");

    /** Creates new form DialogCEMOverview */
    public DialogCEMOverview(JFrame parent, Connection configConnection, Connection runtimeConnection, GUIClient guiClient) {
        super(parent, true);
        this.configConnection = configConnection;
        this.runtimeConnection = runtimeConnection;
        this.guiClient = guiClient;
        //load resource bundle
        try {
            this.rb = (MecResourceBundle) ResourceBundle.getBundle(
                    ResourceBundleCEMOverview.class.getName());
        } //load up  resourcebundle
        catch (MissingResourceException e) {
            throw new RuntimeException("Oops..resource bundle " + e.getClassName() + " not found.");
        }
        initComponents();
        //load the certificates
        this.certificateManager = new CertificateManager(this.logger);
        PreferencesClient client = new PreferencesClient(guiClient.getBaseClient());
        char[] keystorePass = client.get(PreferencesAS2.KEYSTORE_PASS).toCharArray();
        String keystoreName = client.get(PreferencesAS2.KEYSTORE);
        try {
            KeystoreStorage storage = new KeystoreStorageImplClientServer(
                    guiClient.getBaseClient(), keystoreName, keystorePass, BCCryptoHelper.KEYSTORE_PKCS12);
            this.certificateManager = new CertificateManager(this.logger);
            this.certificateManager.loadKeystoreCertificates(storage);
        } catch (Exception e) {
            this.logger.severe(e.getMessage());
            throw new RuntimeException(e);
        }
        CEMAccessDB cemAccess = new CEMAccessDB(this.configConnection, this.runtimeConnection);
        ((TableModelCEMOverview) (this.jTable.getModel())).passNewData(cemAccess.getCEMEntries());
        this.jTable.getColumnModel().getColumn(0).setCellRenderer(new TableCellRendererCEMSystemState());
        this.jTable.getColumnModel().getColumn(1).setCellRenderer(new TableCellRendererCEMState(this.configConnection, this.runtimeConnection));
        this.jTable.getColumnModel().getColumn(3).setCellRenderer(new TableCellRendererPartner(this.configConnection, this.runtimeConnection));
        this.jTable.getColumnModel().getColumn(4).setCellRenderer(new TableCellRendererPartner(this.configConnection, this.runtimeConnection));
        this.jTable.getColumnModel().getColumn(5).setCellRenderer(new TableCellRendererCertificates(this.certificateManager,
                TableCellRendererCertificates.TYPE_FINGERPRINT_SHA1));
        JTableColumnResizer.adjustColumnWidthByContent(this.jTable);
        this.jTable.getSelectionModel().addListSelectionListener(this);
        this.jTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.jTabbedPane.remove(this.jPanelReasonForRejection);
        //this gui may process server messages, register it
        this.guiClient.addMessageProcessor(this);
        this.setButtonState();
    }

    private void setButtonState() {
        int selectedRow = this.jTable.getSelectedRow();
        boolean responseExists = false;
        boolean isPending = false;
        if (selectedRow >= 0) {
            CEMEntry entry = ((TableModelCEMOverview) this.jTable.getModel()).getRowAt(selectedRow);
            responseExists = entry.getResponseMessageid() != null;
            CEMSystemActivity activity = new CEMSystemActivity(entry);
            isPending = activity.getState() == CEMEntry.STATUS_PENDING_INT;
        }
        this.jButtonDisplayRequestDetails.setEnabled(selectedRow >= 0);
        this.jButtonDisplayResponseDetails.setEnabled(responseExists);
        this.jButtonCancel.setEnabled(isPending);
        this.jButtonRemove.setEnabled(selectedRow >= 0);
    }

    private void refresh() {
        CEMAccessDB cemAccess = new CEMAccessDB(this.configConnection, this.runtimeConnection);
        ((TableModelCEMOverview) (this.jTable.getModel())).passNewData(cemAccess.getCEMEntries());
    }

    /**Updates the actual selected rows content*/
    private void updateRowDetails() {
        int selectedRow = this.jTable.getSelectedRow();
        if (selectedRow < 0) {
            this.jTextAreaDetails.setText("");
        } else {
            CEMEntry entry = ((TableModelCEMOverview) (this.jTable.getModel())).getRowAt(selectedRow);
            KeystoreCertificate certificate = this.certificateManager.getKeystoreCertificateByIssuerAndSerial(
                    entry.getIssuername(), entry.getSerialId());
            if (certificate == null) {
                this.jTextAreaDetails.setText("");
            } else {
                this.jTextAreaDetails.setText(certificate.getInfo());
            }
            if (entry.getCemState() == CEMEntry.STATUS_REJECTED_INT && entry.getReasonForRejection() != null) {
                this.jTabbedPane.addTab(this.rb.getResourceString("tab.reasonforrejection"), this.jPanelReasonForRejection);
                this.jTextAreaReasonForRejection.setText(entry.getReasonForRejection());
            }
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jToolBar = new javax.swing.JToolBar();
        jButtonExit = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        jButtonSendCEM = new javax.swing.JButton();
        jButtonDisplayRequestDetails = new javax.swing.JButton();
        jButtonDisplayResponseDetails = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JToolBar.Separator();
        jButtonRemove = new javax.swing.JButton();
        jButtonCancel = new javax.swing.JButton();
        jPanelMain = new javax.swing.JPanel();
        jSplitPane = new javax.swing.JSplitPane();
        jScrollPaneTable = new javax.swing.JScrollPane();
        jTable = new javax.swing.JTable();
        jTabbedPane = new javax.swing.JTabbedPane();
        jPanelCertificateInfo = new javax.swing.JPanel();
        jScrollPaneDetails = new javax.swing.JScrollPane();
        jTextAreaDetails = new javax.swing.JTextArea();
        jPanelReasonForRejection = new javax.swing.JPanel();
        jScrollPaneReasonForRejection = new javax.swing.JScrollPane();
        jTextAreaReasonForRejection = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(this.rb.getResourceString( "title"));
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jToolBar.setFloatable(false);
        jToolBar.setRollover(true);

        jButtonExit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/mendelson/comm/as2/cem/gui/close16x16.gif"))); // NOI18N
        jButtonExit.setText(this.rb.getResourceString( "button.exit"));
        jButtonExit.setFocusable(false);
        jButtonExit.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonExitActionPerformed(evt);
            }
        });
        jToolBar.add(jButtonExit);
        jToolBar.add(jSeparator1);

        jButtonSendCEM.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/mendelson/comm/as2/cem/gui/cem16x16.gif"))); // NOI18N
        jButtonSendCEM.setText(this.rb.getResourceString( "button.sendcem"));
        jButtonSendCEM.setFocusable(false);
        jButtonSendCEM.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonSendCEM.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSendCEMActionPerformed(evt);
            }
        });
        jToolBar.add(jButtonSendCEM);

        jButtonDisplayRequestDetails.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/mendelson/comm/as2/cem/gui/messagedetails16x16.gif"))); // NOI18N
        jButtonDisplayRequestDetails.setText(this.rb.getResourceString( "button.requestdetails"));
        jButtonDisplayRequestDetails.setFocusable(false);
        jButtonDisplayRequestDetails.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonDisplayRequestDetails.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDisplayRequestDetailsActionPerformed(evt);
            }
        });
        jToolBar.add(jButtonDisplayRequestDetails);

        jButtonDisplayResponseDetails.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/mendelson/comm/as2/cem/gui/messagedetails16x16.gif"))); // NOI18N
        jButtonDisplayResponseDetails.setText(this.rb.getResourceString( "button.responsedetails"));
        jButtonDisplayResponseDetails.setFocusable(false);
        jButtonDisplayResponseDetails.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonDisplayResponseDetails.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDisplayResponseDetailsActionPerformed(evt);
            }
        });
        jToolBar.add(jButtonDisplayResponseDetails);
        jToolBar.add(jSeparator2);

        jButtonRemove.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/mendelson/comm/as2/cem/gui/delete_16x16.gif"))); // NOI18N
        jButtonRemove.setText(this.rb.getResourceString( "button.remove"));
        jButtonRemove.setFocusable(false);
        jButtonRemove.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRemoveActionPerformed(evt);
            }
        });
        jToolBar.add(jButtonRemove);

        jButtonCancel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/mendelson/comm/as2/cem/gui/delete_16x16.gif"))); // NOI18N
        jButtonCancel.setText(this.rb.getResourceString( "button.cancel"));
        jButtonCancel.setFocusable(false);
        jButtonCancel.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCancelActionPerformed(evt);
            }
        });
        jToolBar.add(jButtonCancel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        getContentPane().add(jToolBar, gridBagConstraints);

        jPanelMain.setLayout(new java.awt.GridBagLayout());

        jSplitPane.setDividerLocation(200);
        jSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        jTable.setModel(new TableModelCEMOverview());
        jTable.setShowHorizontalLines(false);
        jTable.setShowVerticalLines(false);
        jScrollPaneTable.setViewportView(jTable);

        jSplitPane.setLeftComponent(jScrollPaneTable);

        jPanelCertificateInfo.setLayout(new java.awt.GridBagLayout());

        jTextAreaDetails.setColumns(20);
        jTextAreaDetails.setEditable(false);
        jTextAreaDetails.setFont(new java.awt.Font("Dialog", 0, 12));
        jTextAreaDetails.setRows(5);
        jScrollPaneDetails.setViewportView(jTextAreaDetails);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanelCertificateInfo.add(jScrollPaneDetails, gridBagConstraints);

        jTabbedPane.addTab(this.rb.getResourceString( "tab.certificate"), jPanelCertificateInfo);

        jPanelReasonForRejection.setLayout(new java.awt.GridBagLayout());

        jTextAreaReasonForRejection.setColumns(20);
        jTextAreaReasonForRejection.setEditable(false);
        jTextAreaReasonForRejection.setFont(new java.awt.Font("Dialog", 0, 12));
        jTextAreaReasonForRejection.setRows(5);
        jScrollPaneReasonForRejection.setViewportView(jTextAreaReasonForRejection);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanelReasonForRejection.add(jScrollPaneReasonForRejection, gridBagConstraints);

        jTabbedPane.addTab(this.rb.getResourceString("tab.reasonforrejection"), jPanelReasonForRejection);

        jSplitPane.setRightComponent(jTabbedPane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanelMain.add(jSplitPane, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(jPanelMain, gridBagConstraints);

        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width-895)/2, (screenSize.height-520)/2, 895, 520);
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonSendCEMActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSendCEMActionPerformed
        JFrame parent = (JFrame) SwingUtilities.getAncestorOfClass(JFrame.class, this);
        DialogSendCEM dialog = new DialogSendCEM(parent, this.certificateManager,
                this.configConnection, this.runtimeConnection, this.guiClient.getBaseClient());
        dialog.setVisible(true);
    }//GEN-LAST:event_jButtonSendCEMActionPerformed

    private void jButtonDisplayRequestDetailsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDisplayRequestDetailsActionPerformed
        int selectedRow = this.jTable.getSelectedRow();
        if (selectedRow >= 0) {
            CEMEntry entry = ((TableModelCEMOverview) this.jTable.getModel()).getRowAt(selectedRow);
            MessageAccessDB messageAccess = new MessageAccessDB(this.configConnection, this.runtimeConnection);
            AS2MessageInfo info = messageAccess.getLastMessageEntry(entry.getRequestMessageid());
            if (info != null) {
                List<AS2Payload> payload = messageAccess.getPayload(entry.getRequestMessageid());
                JFrame parent = (JFrame) SwingUtilities.getAncestorOfClass(JFrame.class, this);
                DialogMessageDetails dialog = new DialogMessageDetails(parent,
                        this.configConnection, this.runtimeConnection,
                        this.guiClient.getBaseClient(), info, payload);
                dialog.setVisible(true);
            } else {
                this.logger.warning("CEMOverview: No message info available for for message id " + entry.getRequestMessageid());
            }
        }
    }//GEN-LAST:event_jButtonDisplayRequestDetailsActionPerformed

    private void jButtonDisplayResponseDetailsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDisplayResponseDetailsActionPerformed
        int selectedRow = this.jTable.getSelectedRow();
        if (selectedRow >= 0) {
            CEMEntry entry = ((TableModelCEMOverview) this.jTable.getModel()).getRowAt(selectedRow);
            MessageAccessDB messageAccess = new MessageAccessDB(this.configConnection, this.runtimeConnection);
            AS2MessageInfo info = messageAccess.getLastMessageEntry(entry.getResponseMessageid());
            if (info != null) {
                List<AS2Payload> payload = messageAccess.getPayload(entry.getResponseMessageid());
                JFrame parent = (JFrame) SwingUtilities.getAncestorOfClass(JFrame.class, this);
                DialogMessageDetails dialog = new DialogMessageDetails(parent,
                        this.configConnection, this.runtimeConnection,
                        this.guiClient.getBaseClient(), info, payload);
                dialog.setVisible(true);
            }
        }
    }//GEN-LAST:event_jButtonDisplayResponseDetailsActionPerformed

    private void jButtonExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonExitActionPerformed
        //this gui must no longer process the server messages
        this.guiClient.removeMessageProcessor(this);
        this.setVisible(false);
        this.dispose();
    }//GEN-LAST:event_jButtonExitActionPerformed

    private void jButtonRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRemoveActionPerformed
        int selectedRow = this.jTable.getSelectedRow();
        if (selectedRow >= 0) {
            TableModelCEMOverview model = (TableModelCEMOverview) this.jTable.getModel();
            //cancel the operation
            CEMEntry entry = model.getRowAt(selectedRow);
            CEMAccessDB cemAccess = new CEMAccessDB(this.configConnection, this.runtimeConnection);
            cemAccess.setPendingRequestsToState(entry.getInitiatorAS2Id(), entry.getReceiverAS2Id(), entry.getCategory(), entry.getRequestId(),
                    CEMEntry.STATUS_CANCELED_INT);
            //remove the underlaying log entries
            LogAccessDB logAccess = new LogAccessDB(this.configConnection, this.runtimeConnection);
            //remove the underlaying messages
            MessageAccessDB messageAccess = new MessageAccessDB(this.configConnection, this.runtimeConnection);
            if (entry.getRequestMessageid() != null) {
                logAccess.deleteMessageLog(entry.getRequestMessageid());
                messageAccess.deleteMessage(entry.getRequestMessageid());
            }
            if (entry.getResponseMessageid() != null) {
                logAccess.deleteMessageLog(entry.getResponseMessageid());
                messageAccess.deleteMessage(entry.getResponseMessageid());
            }
            cemAccess.removeEntry(entry.getInitiatorAS2Id(), entry.getReceiverAS2Id(), entry.getCategory(), entry.getRequestId());
            this.refresh();
            if (selectedRow >= model.getRowCount()) {
                selectedRow = model.getRowCount() - 1;
            }
            //last row?
            if (model.getRowCount() == 0) {
                selectedRow = -1;
            }
            if (selectedRow >= 0) {
                this.jTable.setRowSelectionInterval(selectedRow, selectedRow);
            }
        }
    }//GEN-LAST:event_jButtonRemoveActionPerformed

    private void jButtonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCancelActionPerformed
        int selectedRow = this.jTable.getSelectedRow();
        if (selectedRow >= 0) {
            CEMEntry entry = ((TableModelCEMOverview) this.jTable.getModel()).getRowAt(selectedRow);
            CEMAccessDB cemAccess = new CEMAccessDB(this.configConnection, this.runtimeConnection);
            //cancel entry
            cemAccess.setPendingRequestsToState(entry.getInitiatorAS2Id(), entry.getReceiverAS2Id(), entry.getCategory(), entry.getRequestId(),
                    CEMEntry.STATUS_CANCELED_INT);
            this.refresh();
            this.jTable.setRowSelectionInterval(selectedRow, selectedRow);
        }
    }//GEN-LAST:event_jButtonCancelActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonCancel;
    private javax.swing.JButton jButtonDisplayRequestDetails;
    private javax.swing.JButton jButtonDisplayResponseDetails;
    private javax.swing.JButton jButtonExit;
    private javax.swing.JButton jButtonRemove;
    private javax.swing.JButton jButtonSendCEM;
    private javax.swing.JPanel jPanelCertificateInfo;
    private javax.swing.JPanel jPanelMain;
    private javax.swing.JPanel jPanelReasonForRejection;
    private javax.swing.JScrollPane jScrollPaneDetails;
    private javax.swing.JScrollPane jScrollPaneReasonForRejection;
    private javax.swing.JScrollPane jScrollPaneTable;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JSplitPane jSplitPane;
    private javax.swing.JTabbedPane jTabbedPane;
    private javax.swing.JTable jTable;
    private javax.swing.JTextArea jTextAreaDetails;
    private javax.swing.JTextArea jTextAreaReasonForRejection;
    private javax.swing.JToolBar jToolBar;
    // End of variables declaration//GEN-END:variables

    /**Makes this a ListSelectionListener*/
    @Override
    public void valueChanged(ListSelectionEvent e) {
        this.jTabbedPane.remove(this.jPanelReasonForRejection);
        this.setButtonState();
        //display the selected rows content
        this.updateRowDetails();
    }

    @Override
    public boolean processMessageFromServer(ClientServerMessage message) {
        if (message instanceof RefreshClientCEMDisplay) {
            this.refresh();
            return (true);
        }
        return (false);
    }
}
