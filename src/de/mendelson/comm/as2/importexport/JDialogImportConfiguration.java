//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/importexport/JDialogImportConfiguration.java,v 1.1 2012/04/18 14:10:30 heller Exp $
package de.mendelson.comm.as2.importexport;

import de.mendelson.comm.as2.partner.Partner;
import de.mendelson.util.MecResourceBundle;
import de.mendelson.util.clientserver.BaseClient;
import de.mendelson.util.clientserver.clients.datatransfer.TransferClient;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.table.TableColumn;


/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Dialog to configure a single partner
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class JDialogImportConfiguration extends JDialog {

    /**ResourceBundle to localize the GUI*/
    private MecResourceBundle rb = null;
    /**Import filename*/
    private String filename;
    /**component parent*/
    private JFrame parentFrame;
    private BaseClient baseClient;

    /** Creates new form JDialogImportConfiguration
     * @param filename Import filename
     */
    public JDialogImportConfiguration(JFrame parentFrame, String filename, 
            Connection configConnection, Connection runtimeConnection,
            BaseClient baseClient) throws Exception {
        super(parentFrame, true);
        this.parentFrame = parentFrame;
        this.filename = filename;
        this.baseClient = baseClient;
        //load resource bundle
        try {
            this.rb = (MecResourceBundle) ResourceBundle.getBundle(
                    ResourceBundleImportConfiguration.class.getName());
        } catch (MissingResourceException e) {
            throw new RuntimeException("Oops..resource bundle " + e.getClassName() + " not found.");
        }
        this.initComponents();
        InputStream inStream = null;
        List<Partner> partnerList = new ArrayList<Partner>();
        try {
            inStream = new FileInputStream(filename);
            ConfigurationImport configImport 
                    = new ConfigurationImport(configConnection, runtimeConnection);
            partnerList.addAll(configImport.readPartner(inStream));
            if (partnerList == null || partnerList.isEmpty()) {
                throw new Exception(this.rb.getResourceString("invalid.importfile"));
            }
        } finally {
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (Exception e) {
                }
            }
        }
        this.jTablePartner.getTableHeader().setReorderingAllowed(false);
        TableColumn column = this.jTablePartner.getColumnModel().getColumn(0);
        column.setMaxWidth(20);
        column.setMinWidth(20);
        column.setResizable(false);
        column = this.jTablePartner.getColumnModel().getColumn(3);
        column.setMaxWidth(20);
        column.setMinWidth(20);
        column.setResizable(false);
        ((TableModelPartnerSelect) this.jTablePartner.getModel()).passNewData(partnerList);
    }

    /**Sets the ok and cancel buttons of this GUI*/
    private void setButtonState() {
    }

    /**Finally performs the import*/
    private void performImport() throws Throwable {
        //collect all selected Partner
        List<Partner> selectedParter = ((TableModelPartnerSelect) this.jTablePartner.getModel()).getSelectedPartner();
        InputStream inStream = null;
        try {
            TransferClient transferClient = new TransferClient(this.baseClient);
            inStream = new FileInputStream(this.filename);
            //upload the data first, chunked
            String uploadHash = transferClient.uploadChunked(inStream);           
            //..then perform the import process
            ConfigurationImportRequest request = new ConfigurationImportRequest();
            request.setPartnerListToImport(selectedParter);
            request.setImportNotification(this.jCheckBoxImportNotification.isSelected());
            request.setImportServerSettings(this.jCheckBoxImportServerProperties.isSelected());
            request.setUploadHash(uploadHash);
            ConfigurationImportResponse response = (ConfigurationImportResponse) transferClient.upload(request);
            if (response.getException() != null) {
                throw response.getException();
            }
        } finally {
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (Exception e) {
                }
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

        jPanelEdit = new javax.swing.JPanel();
        jLabelIcon = new javax.swing.JLabel();
        jPanelImportOptions = new javax.swing.JPanel();
        jCheckBoxImportServerProperties = new javax.swing.JCheckBox();
        jCheckBoxImportNotification = new javax.swing.JCheckBox();
        jPanelSpace = new javax.swing.JPanel();
        jLabelInfo = new javax.swing.JLabel();
        jPanelPartner = new javax.swing.JPanel();
        jScrollPanePartnerTable = new javax.swing.JScrollPane();
        jTablePartner = new javax.swing.JTable();
        jButtonNoPartner = new javax.swing.JButton();
        jButtonAllPartner = new javax.swing.JButton();
        jPanelButtons = new javax.swing.JPanel();
        jButtonImport = new javax.swing.JButton();
        jButtonCancel = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(this.rb.getResourceString( "title"));
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jPanelEdit.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        jPanelEdit.setLayout(new java.awt.GridBagLayout());

        jLabelIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/mendelson/comm/as2/importexport/import_32x32.gif"))); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 20, 10);
        jPanelEdit.add(jLabelIcon, gridBagConstraints);

        jPanelImportOptions.setBorder(javax.swing.BorderFactory.createTitledBorder(this.rb.getResourceString( "title.config")));
        jPanelImportOptions.setLayout(new java.awt.GridBagLayout());

        jCheckBoxImportServerProperties.setText(this.rb.getResourceString( "label.propertiesimport"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelImportOptions.add(jCheckBoxImportServerProperties, gridBagConstraints);

        jCheckBoxImportNotification.setText(this.rb.getResourceString( "label.notificationimport"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelImportOptions.add(jCheckBoxImportNotification, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanelImportOptions.add(jPanelSpace, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelEdit.add(jPanelImportOptions, gridBagConstraints);

        jLabelInfo.setText(this.rb.getResourceString( "import.info"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelEdit.add(jLabelInfo, gridBagConstraints);

        jPanelPartner.setBorder(javax.swing.BorderFactory.createTitledBorder(this.rb.getResourceString( "title.partner")));
        jPanelPartner.setLayout(new java.awt.GridBagLayout());

        jTablePartner.setModel(new TableModelPartnerSelect());
        jScrollPanePartnerTable.setViewportView(jTablePartner);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelPartner.add(jScrollPanePartnerTable, gridBagConstraints);

        jButtonNoPartner.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/mendelson/comm/as2/importexport/no_partner_16x16.gif"))); // NOI18N
        jButtonNoPartner.setText(this.rb.getResourceString( "partner.none"));
        jButtonNoPartner.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonNoPartner.setMargin(new java.awt.Insets(2, 5, 2, 5));
        jButtonNoPartner.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        jButtonNoPartner.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonNoPartner.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonNoPartnerActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelPartner.add(jButtonNoPartner, gridBagConstraints);

        jButtonAllPartner.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/mendelson/comm/as2/importexport/all_partner_16x16.gif"))); // NOI18N
        jButtonAllPartner.setText(this.rb.getResourceString( "partner.all"));
        jButtonAllPartner.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonAllPartner.setMargin(new java.awt.Insets(2, 5, 2, 5));
        jButtonAllPartner.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        jButtonAllPartner.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonAllPartner.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAllPartnerActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelPartner.add(jButtonAllPartner, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelEdit.add(jPanelPartner, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(jPanelEdit, gridBagConstraints);

        jPanelButtons.setLayout(new java.awt.GridBagLayout());

        jButtonImport.setText(this.rb.getResourceString( "button.import" ));
        jButtonImport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonImportActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        jPanelButtons.add(jButtonImport, gridBagConstraints);

        jButtonCancel.setText(this.rb.getResourceString( "button.cancel" ));
        jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCancelActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        jPanelButtons.add(jButtonCancel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        getContentPane().add(jPanelButtons, gridBagConstraints);

        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width-596)/2, (screenSize.height-538)/2, 596, 538);
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCancelActionPerformed
        this.setVisible(false);
        this.dispose();
    }//GEN-LAST:event_jButtonCancelActionPerformed

    private void jButtonImportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonImportActionPerformed
        this.setVisible(false);
        try {
            this.performImport();
            JOptionPane.showMessageDialog(this.parentFrame, this.rb.getResourceString("import.success.msg"),
                    this.rb.getResourceString("import.success.title"),
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Throwable e) {
            JOptionPane.showMessageDialog(this.parentFrame, this.rb.getResourceString("import.failed.msg",
                    e.getMessage()),
                    this.rb.getResourceString("import.failed.title"),
                    JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_jButtonImportActionPerformed

    private void jButtonAllPartnerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAllPartnerActionPerformed
        ((TableModelPartnerSelect) this.jTablePartner.getModel()).selectAll();
    }//GEN-LAST:event_jButtonAllPartnerActionPerformed

    private void jButtonNoPartnerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonNoPartnerActionPerformed
        ((TableModelPartnerSelect) this.jTablePartner.getModel()).selectNone();
    }//GEN-LAST:event_jButtonNoPartnerActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonAllPartner;
    private javax.swing.JButton jButtonCancel;
    private javax.swing.JButton jButtonImport;
    private javax.swing.JButton jButtonNoPartner;
    private javax.swing.JCheckBox jCheckBoxImportNotification;
    private javax.swing.JCheckBox jCheckBoxImportServerProperties;
    private javax.swing.JLabel jLabelIcon;
    private javax.swing.JLabel jLabelInfo;
    private javax.swing.JPanel jPanelButtons;
    private javax.swing.JPanel jPanelEdit;
    private javax.swing.JPanel jPanelImportOptions;
    private javax.swing.JPanel jPanelPartner;
    private javax.swing.JPanel jPanelSpace;
    private javax.swing.JScrollPane jScrollPanePartnerTable;
    private javax.swing.JTable jTablePartner;
    // End of variables declaration//GEN-END:variables
}
