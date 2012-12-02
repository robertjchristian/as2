//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/cem/gui/DialogSendCEM.java,v 1.1 2012/04/18 14:10:20 heller Exp $
package de.mendelson.comm.as2.cem.gui;

import de.mendelson.comm.as2.cem.CEMInitiator;
import de.mendelson.util.security.cert.CertificateManager;
import de.mendelson.util.security.cert.KeystoreCertificate;
import de.mendelson.comm.as2.partner.Partner;
import de.mendelson.comm.as2.partner.PartnerAccessDB;
import de.mendelson.comm.as2.partner.gui.ListCellRendererPartner;
import de.mendelson.comm.as2.preferences.PreferencesAS2;
import de.mendelson.util.MecResourceBundle;
import de.mendelson.util.clientserver.BaseClient;
import de.mendelson.util.clientserver.clients.preferences.PreferencesClient;
import de.mendelson.util.security.BCCryptoHelper;
import de.mendelson.util.security.cert.KeystoreStorage;
import de.mendelson.util.security.cert.ListCellRendererCertificates;
import de.mendelson.util.security.cert.clientserver.KeystoreStorageImplClientServer;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Allows to select a partner and sends a certificate to him via your mail application
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class DialogSendCEM extends JDialog {

    private MecResourceBundle rb = null;
    private CertificateManager certificateManagerEncSign;
    private Connection configConnection;
    private Connection runtimeConnection;
    private Logger logger = Logger.getLogger("de.mendelson.as2.client");

    public DialogSendCEM(JFrame parent, Connection configConnection, Connection runtimeConnection, BaseClient baseClient) {
        this(parent, null, configConnection, runtimeConnection, baseClient);
    }

    public DialogSendCEM(JFrame parent, CertificateManager certificateManager, Connection configConnection,
            Connection runtimeConnection,
            BaseClient baseClient) {
        super(parent, true);
        //load the certificates if they arent passed here
        if (certificateManager == null) {
            PreferencesClient client = new PreferencesClient(baseClient);
            char[] keystorePass = client.get(PreferencesAS2.KEYSTORE_PASS).toCharArray();
            String keystoreName = client.get(PreferencesAS2.KEYSTORE);
            try {
                KeystoreStorage storage = new KeystoreStorageImplClientServer(
                        baseClient, keystoreName, keystorePass, BCCryptoHelper.KEYSTORE_PKCS12);
                this.certificateManagerEncSign = new CertificateManager(this.logger);
                this.certificateManagerEncSign.loadKeystoreCertificates(storage);
            } catch (Exception e) {
                this.logger.severe(e.getMessage());
                throw new RuntimeException(e);
            }
        } else {
            this.certificateManagerEncSign = certificateManager;
        }
        this.configConnection = configConnection;
        this.runtimeConnection = runtimeConnection;
        //load resource bundle
        try {
            this.rb = (MecResourceBundle) ResourceBundle.getBundle(
                    ResourceBundleDialogSendCEM.class.getName());
        } catch (MissingResourceException e) {
            throw new RuntimeException("Oops..resource bundle " + e.getClassName() + " not found.");
        }
        initComponents();
        List<KeystoreCertificate> certificateList = this.certificateManagerEncSign.getKeyStoreCertificateList();
        //clone the array
        List<KeystoreCertificate> sortedCertificateList = new ArrayList<KeystoreCertificate>();
        for (KeystoreCertificate cert : certificateList) {
            if (cert.getIsKeyPair()) {
                sortedCertificateList.add(cert);
            }
        }
        Collections.sort(sortedCertificateList);
        this.jComboBoxReceiver.removeAllItems();
        this.jComboBoxInitiator.removeAllItems();
        this.jComboBoxKeys.removeAllItems();
        this.jComboBoxKeys.setRenderer(new ListCellRendererCertificates());
        for (KeystoreCertificate cert : sortedCertificateList) {
            this.jComboBoxKeys.addItem(cert);
        }
        this.jComboBoxInitiator.setRenderer(new ListCellRendererPartner());
        PartnerAccessDB partnerAccess = new PartnerAccessDB(this.configConnection, this.runtimeConnection);
        Partner[] initiatorList = partnerAccess.getLocalStations();
        for (Partner partner : initiatorList) {
            this.jComboBoxInitiator.addItem(partner);
        }
        this.jComboBoxReceiver.setRenderer(new ListCellRendererPartner());
        Partner[] receiverList = partnerAccess.getNonLocalStations();
        for (Partner partner : receiverList) {
            this.jComboBoxReceiver.addItem(partner);
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.YEAR, 1);
        Date nextYear = calendar.getTime();
        calendar.add(Calendar.YEAR, -1);
        calendar.add(Calendar.DAY_OF_YEAR, 30);
        Date thirtyDays = calendar.getTime();
        this.jDateChooser.setSelectableDateRange(new Date(), nextYear);
        this.jDateChooser.setDate(thirtyDays);
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

        jPanelMain = new javax.swing.JPanel();
        jComboBoxKeys = new javax.swing.JComboBox();
        jComboBoxReceiver = new javax.swing.JComboBox();
        jDateChooser = new com.toedter.calendar.JDateChooser();
        jLabelReceiver = new javax.swing.JLabel();
        jLabelKeys = new javax.swing.JLabel();
        jLabelActivationDate = new javax.swing.JLabel();
        jPanelSpace = new javax.swing.JPanel();
        jLabelInitiator = new javax.swing.JLabel();
        jComboBoxInitiator = new javax.swing.JComboBox();
        jPanelButton = new javax.swing.JPanel();
        jButtonOk = new javax.swing.JButton();
        jButtonCancel = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(this.rb.getResourceString( "title"));
        setModal(true);
        setResizable(false);
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jPanelMain.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        jPanelMain.setLayout(new java.awt.GridBagLayout());

        jComboBoxKeys.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 4;
        gridBagConstraints.ipady = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelMain.add(jComboBoxKeys, gridBagConstraints);

        jComboBoxReceiver.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 4;
        gridBagConstraints.ipady = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelMain.add(jComboBoxReceiver, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelMain.add(jDateChooser, gridBagConstraints);

        jLabelReceiver.setText(this.rb.getResourceString( "label.receiver"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelMain.add(jLabelReceiver, gridBagConstraints);

        jLabelKeys.setText(this.rb.getResourceString( "label.certificate"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelMain.add(jLabelKeys, gridBagConstraints);

        jLabelActivationDate.setText(this.rb.getResourceString( "label.activationdate"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelMain.add(jLabelActivationDate, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.weighty = 1.0;
        jPanelMain.add(jPanelSpace, gridBagConstraints);

        jLabelInitiator.setText(this.rb.getResourceString( "label.initiator"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 5, 5);
        jPanelMain.add(jLabelInitiator, gridBagConstraints);

        jComboBoxInitiator.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 4;
        gridBagConstraints.ipady = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 5, 5);
        jPanelMain.add(jComboBoxInitiator, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(jPanelMain, gridBagConstraints);

        jPanelButton.setLayout(new java.awt.GridBagLayout());

        jButtonOk.setText(this.rb.getResourceString( "button.ok"));
        jButtonOk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOkActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        jPanelButton.add(jButtonOk, gridBagConstraints);

        jButtonCancel.setText(this.rb.getResourceString( "button.cancel"));
        jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCancelActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        jPanelButton.add(jButtonCancel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        getContentPane().add(jPanelButton, gridBagConstraints);

        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width-433)/2, (screenSize.height-247)/2, 433, 247);
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCancelActionPerformed
        this.setVisible(false);
        this.dispose();
    }//GEN-LAST:event_jButtonCancelActionPerformed

    private void jButtonOkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOkActionPerformed
        this.setVisible(false);
        CEMInitiator cemInitiator = new CEMInitiator(this.configConnection, 
                this.runtimeConnection, this.certificateManagerEncSign);
        Partner receiver = (Partner) this.jComboBoxReceiver.getSelectedItem();
        Partner initiator = (Partner) this.jComboBoxInitiator.getSelectedItem();
        KeystoreCertificate certificate = (KeystoreCertificate) this.jComboBoxKeys.getSelectedItem();
        Date activationDate = this.jDateChooser.getDate();
        //set time to 0:01 of this day
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(activationDate);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 1);
        calendar.set(Calendar.SECOND, 0);
        JFrame parent = (JFrame) SwingUtilities.getAncestorOfClass(JFrame.class, this);
        try {
            cemInitiator.sendRequest(initiator, receiver,
                    certificate, true, true, false, calendar.getTime());
            JOptionPane.showMessageDialog(parent,
                    this.rb.getResourceString("cem.request.success"),
                    this.rb.getResourceString("cem.request.title"),
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(parent,
                    this.rb.getResourceString("cem.request.failed", e.getMessage()),
                    this.rb.getResourceString("cem.request.title"),
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
        this.dispose();
    }//GEN-LAST:event_jButtonOkActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonCancel;
    private javax.swing.JButton jButtonOk;
    private javax.swing.JComboBox jComboBoxInitiator;
    private javax.swing.JComboBox jComboBoxKeys;
    private javax.swing.JComboBox jComboBoxReceiver;
    private com.toedter.calendar.JDateChooser jDateChooser;
    private javax.swing.JLabel jLabelActivationDate;
    private javax.swing.JLabel jLabelInitiator;
    private javax.swing.JLabel jLabelKeys;
    private javax.swing.JLabel jLabelReceiver;
    private javax.swing.JPanel jPanelButton;
    private javax.swing.JPanel jPanelMain;
    private javax.swing.JPanel jPanelSpace;
    // End of variables declaration//GEN-END:variables
}
