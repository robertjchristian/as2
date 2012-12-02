//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/client/manualsend/JDialogManualSend.java,v 1.1 2012/04/18 14:10:24 heller Exp $
package de.mendelson.comm.as2.client.manualsend;

import de.mendelson.comm.as2.client.AS2StatusBar;
import de.mendelson.util.security.cert.CertificateManager;
import de.mendelson.comm.as2.partner.Partner;
import de.mendelson.comm.as2.partner.PartnerAccessDB;
import de.mendelson.comm.as2.partner.gui.ListCellRendererPartner;
import de.mendelson.comm.as2.preferences.PreferencesAS2;
import de.mendelson.util.LockingGlassPane;
import de.mendelson.util.MecFileChooser;
import de.mendelson.util.MecResourceBundle;
import de.mendelson.util.clientserver.BaseClient;
import de.mendelson.util.clientserver.clients.datatransfer.TransferClientWithProgress;
import de.mendelson.util.clientserver.clients.preferences.PreferencesClient;
import de.mendelson.util.security.BCCryptoHelper;
import de.mendelson.util.security.cert.KeystoreStorage;
import de.mendelson.util.security.cert.clientserver.KeystoreStorageImplClientServer;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
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
 * Dialog to send a file to a single partner
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class JDialogManualSend extends JDialog {

    /**ResourceBundle to localize the GUI*/
    private MecResourceBundle rb = null;
    private Logger logger = Logger.getLogger("de.mendelson.as2.client");
    private Partner[] localStations = null;
    private CertificateManager certificateManager = null;
    //DB connection for the partner access
    private Connection configConnection;
    private Connection runtimeConnection;
    private BaseClient baseClient;
    private AS2StatusBar statusbar;
    /**String that is displayed while the client uploads data to the server to send*/
    private String uploadDisplay;

    /** Creates new form JDialogPartnerConfig
     * @param uploadDisplay String that is displayed while the client uploads data to the server to send
     */
    public JDialogManualSend(JFrame parent, Connection configConnection, Connection runtimeConnection,BaseClient baseClient,
            AS2StatusBar statusbar, String uploadDisplay) {
        super(parent, true);
        this.configConnection = configConnection;
        this.runtimeConnection = runtimeConnection;
        this.statusbar = statusbar;
        this.uploadDisplay = uploadDisplay;
        //load resource bundle
        try {
            this.rb = (MecResourceBundle) ResourceBundle.getBundle(
                    ResourceBundleManualSend.class.getName());
        } catch (MissingResourceException e) {
            throw new RuntimeException("Oops..resource bundle "
                    + e.getClassName() + " not found.");
        }
        this.baseClient = baseClient;
        this.setTitle(this.rb.getResourceString("title"));
        initComponents();
        this.getRootPane().setDefaultButton(this.jButtonOk);
        //fill in data
        try {
            PartnerAccessDB partnerAccess = new PartnerAccessDB(this.configConnection, this.runtimeConnection);
            Partner[] partner = partnerAccess.getPartner();
            for (int i = 0; i < partner.length; i++) {
                if (!partner[i].isLocalStation()) {
                    this.jComboBoxPartner.addItem(partner[i]);
                }
            }
            this.localStations = partnerAccess.getLocalStations();
            this.certificateManager = new CertificateManager(this.logger);
            //ask the server for the password
            PreferencesClient client = new PreferencesClient(baseClient);
            char[] keystorePass = client.get(PreferencesAS2.KEYSTORE_PASS).toCharArray();
            String keystoreName = client.get(PreferencesAS2.KEYSTORE);
            KeystoreStorage storage = new KeystoreStorageImplClientServer(
                    baseClient, keystoreName, keystorePass, BCCryptoHelper.KEYSTORE_PKCS12);
            this.certificateManager.loadKeystoreCertificates(storage);
        } catch (Exception e) {
            this.logger.severe("JDialogManualSend: " + e.getMessage());
        }
        //single local stattion? No need to select the sender
        if (this.localStations.length == 1) {
            this.jLabelSender.setVisible(false);
            this.jComboBoxSender.setVisible(false);
        } else {
            this.jComboBoxSender.removeAllItems();
            for (Partner localStation : this.localStations) {
                this.jComboBoxSender.addItem(localStation);
            }
            this.jComboBoxSender.setSelectedItem(0);
        }
        this.jComboBoxPartner.setRenderer(new ListCellRendererPartner());
        this.jComboBoxSender.setRenderer(new ListCellRendererPartner());
        this.setButtonState();
    }

    /**Lock the component: Add a glasspane that prevents any action on the UI*/
    private void lock() {
        //init glasspane for first use
        if (!(this.getGlassPane() instanceof LockingGlassPane)) {
            this.setGlassPane(new LockingGlassPane());
        }
        this.getGlassPane().setVisible(true);
        this.getGlassPane().requestFocusInWindow();
    }

    /**Unlock the component: remove the glasspane that prevents any action on the UI*/
    private void unlock() {
        getGlassPane().setVisible(false);
    }

    /**Fills in some preselections for the file send dialog*/
    public void initialize(Partner sender, Partner receiver, String filename) {
        this.jComboBoxPartner.setSelectedItem(receiver);
        this.jComboBoxSender.setSelectedItem(sender);
        this.jTextFieldFilename.setText(filename);
        this.setButtonState();
    }

    /**Sets the ok and cancel buttons of this GUI*/
    private void setButtonState() {
        this.jButtonOk.setEnabled(
                this.jTextFieldFilename.getText().length() > 0);
    }

    /**Will be executed on click to OK*/
    public void performSend() throws Throwable {
        InputStream inStream = null;
        try {
            Partner receiver = (Partner) this.jComboBoxPartner.getSelectedItem();
            File sendFile = new File(this.jTextFieldFilename.getText());
            Partner sender = null;
            if (this.localStations.length == 1) {
                sender = this.localStations[0];
            } else {
                sender = (Partner) this.jComboBoxSender.getSelectedItem();
            }
            TransferClientWithProgress transferClient = new TransferClientWithProgress(
                    this.baseClient,
                    this.statusbar.getProgressPanel());
            inStream = new FileInputStream(sendFile);
            //perform the upload to the server, chunked
            String uploadHash = transferClient.uploadChunkedWithProgress(inStream, this.uploadDisplay, (int) sendFile.length());
            ManualSendRequest request = new ManualSendRequest();
            request.setUploadHash(uploadHash);
            request.setFilename(sendFile.getName());
            request.setReceiver(receiver);
            request.setSender(sender);
            ManualSendResponse response = (ManualSendResponse) transferClient.upload(request);
            if (response.getException() != null) {
                throw (response.getException());
            }
        } finally {
            if (inStream != null) {
                inStream.close();

            }
        }
    }

    private void okButtonPressed() {
        this.jButtonOk.setEnabled( false );
        this.jButtonCancel.setEnabled( false );
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                JDialogManualSend.this.lock();
                try {
                    //perform send has an own progress bar, no need to set one here
                    JDialogManualSend.this.performSend();
                    //display success dialog
                    JFrame parent = (JFrame) SwingUtilities.getAncestorOfClass(JFrame.class, JDialogManualSend.this);
                    JDialogManualSend.this.unlock();
                    JDialogManualSend.this.setVisible(false);
                    JOptionPane.showMessageDialog(parent,
                            JDialogManualSend.this.rb.getResourceString("send.success"));
                } catch (Throwable e) {
                    JDialogManualSend.this.logger.warning("Manual send: " + e.getMessage());
                } finally {
                    JDialogManualSend.this.unlock();
                    JDialogManualSend.this.setVisible(false);
                    JDialogManualSend.this.dispose();
                }
            }
        };
        Executors.newSingleThreadExecutor().submit(runnable);
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
        jLabelFilename = new javax.swing.JLabel();
        jTextFieldFilename = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        jLabelPartner = new javax.swing.JLabel();
        jComboBoxPartner = new javax.swing.JComboBox();
        jButtonBrowse = new javax.swing.JButton();
        jComboBoxSender = new javax.swing.JComboBox();
        jLabelSender = new javax.swing.JLabel();
        jPanelButtons = new javax.swing.JPanel();
        jButtonOk = new javax.swing.JButton();
        jButtonCancel = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jPanelEdit.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        jPanelEdit.setLayout(new java.awt.GridBagLayout());

        jLabelIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/mendelson/comm/as2/client/manualsend/send_32x32.gif"))); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        jPanelEdit.add(jLabelIcon, gridBagConstraints);

        jLabelFilename.setText(this.rb.getResourceString( "label.filename"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelEdit.add(jLabelFilename, gridBagConstraints);

        jTextFieldFilename.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextFieldFilenameKeyReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelEdit.add(jTextFieldFilename, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.weighty = 1.0;
        jPanelEdit.add(jPanel3, gridBagConstraints);

        jLabelPartner.setText(this.rb.getResourceString( "label.partner"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelEdit.add(jLabelPartner, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelEdit.add(jComboBoxPartner, gridBagConstraints);

        jButtonBrowse.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/mendelson/comm/as2/client/manualsend/folder.gif"))); // NOI18N
        jButtonBrowse.setToolTipText(this.rb.getResourceString( "button.browse"));
        jButtonBrowse.setMargin(new java.awt.Insets(2, 5, 2, 5));
        jButtonBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonBrowseActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 10);
        jPanelEdit.add(jButtonBrowse, gridBagConstraints);

        jComboBoxSender.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelEdit.add(jComboBoxSender, gridBagConstraints);

        jLabelSender.setText(this.rb.getResourceString( "label.localstation"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelEdit.add(jLabelSender, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(jPanelEdit, gridBagConstraints);

        jPanelButtons.setLayout(new java.awt.GridBagLayout());

        jButtonOk.setText(this.rb.getResourceString( "button.ok" ));
        jButtonOk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOkActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        jPanelButtons.add(jButtonOk, gridBagConstraints);

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
        setBounds((screenSize.width-425)/2, (screenSize.height-245)/2, 425, 245);
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonBrowseActionPerformed
        JFrame parent = (JFrame) SwingUtilities.getAncestorOfClass(JFrame.class, this);
        MecFileChooser chooser = new MecFileChooser(parent,
                this.rb.getResourceString("label.selectfile"));
        chooser.browseFilename(this.jTextFieldFilename);
        this.setButtonState();
    }//GEN-LAST:event_jButtonBrowseActionPerformed

    private void jTextFieldFilenameKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldFilenameKeyReleased
        this.setButtonState();
    }//GEN-LAST:event_jTextFieldFilenameKeyReleased

    private void jButtonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCancelActionPerformed
        this.setVisible(false);
        this.dispose();
    }//GEN-LAST:event_jButtonCancelActionPerformed

    private void jButtonOkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOkActionPerformed
        this.okButtonPressed();
    }//GEN-LAST:event_jButtonOkActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonBrowse;
    private javax.swing.JButton jButtonCancel;
    private javax.swing.JButton jButtonOk;
    private javax.swing.JComboBox jComboBoxPartner;
    private javax.swing.JComboBox jComboBoxSender;
    private javax.swing.JLabel jLabelFilename;
    private javax.swing.JLabel jLabelIcon;
    private javax.swing.JLabel jLabelPartner;
    private javax.swing.JLabel jLabelSender;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanelButtons;
    private javax.swing.JPanel jPanelEdit;
    private javax.swing.JTextField jTextFieldFilename;
    // End of variables declaration//GEN-END:variables
}
