//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/partner/gui/JDialogPartnerConfig.java,v 1.1 2012/04/18 14:10:32 heller Exp $
package de.mendelson.comm.as2.partner.gui;

import de.mendelson.comm.as2.client.AS2StatusBar;
import de.mendelson.util.security.cert.CertificateManager;
import de.mendelson.comm.as2.clientserver.message.PartnerConfigurationChanged;
import de.mendelson.comm.as2.partner.Partner;
import de.mendelson.comm.as2.partner.PartnerAccessDB;
import de.mendelson.comm.as2.preferences.PreferencesAS2;
import de.mendelson.util.ImageUtil;
import de.mendelson.util.LockingGlassPane;
import de.mendelson.util.MecResourceBundle;
import de.mendelson.util.clientserver.GUIClient;
import de.mendelson.util.clientserver.clients.fileoperation.FileOperationClient;
import de.mendelson.util.clientserver.clients.preferences.PreferencesClient;
import de.mendelson.util.security.BCCryptoHelper;
import de.mendelson.util.security.cert.KeystoreStorage;
import de.mendelson.util.security.cert.clientserver.KeystoreStorageImplClientServer;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Dialog to configure the partner of the rosettanet server
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class JDialogPartnerConfig extends JDialog {

    /**Resource to locaize the GUI*/
    private MecResourceBundle rb = null;
    /**List of all available partner*/
    private List<Partner> partnerList = new ArrayList<Partner>();
    private JPanelPartner panelEditPartner = null;
    private JTreePartner jTreePartner = null;
    private PreferencesAS2 clientPreferences = new PreferencesAS2();
    private CertificateManager certificateManager = null;
    private GUIClient guiClient;
    //DB connection
    private Connection configConnection;
    private Connection runtimeConnection;
    private Logger logger = Logger.getLogger("de.mendelson.as2.client");
    private AS2StatusBar status;

    /** Creates new form JDialogMessageMapping */
    public JDialogPartnerConfig(JFrame parent, 
            Connection configConnection, Connection runtimeConnection, GUIClient guiClient,
            AS2StatusBar status) {
        super(parent, true);
        this.status = status;
        this.configConnection = configConnection;
        this.runtimeConnection = runtimeConnection;
        this.guiClient = guiClient;
        //load resource bundle
        try {
            this.rb = (MecResourceBundle) ResourceBundle.getBundle(
                    ResourceBundlePartnerConfig.class.getName());
        } catch (MissingResourceException e) {
            throw new RuntimeException("Oops..resource bundle " + e.getClassName() + " not found.");
        }
        this.certificateManager = new CertificateManager(this.logger);
        try {
            //ask the server for the keystore password
            PreferencesClient client = new PreferencesClient(guiClient.getBaseClient());
            char[] keystorePass = client.get(PreferencesAS2.KEYSTORE_PASS).toCharArray();
            String keystoreName = client.get(PreferencesAS2.KEYSTORE);
            KeystoreStorage storage = new KeystoreStorageImplClientServer(
                    guiClient.getBaseClient(), keystoreName, keystorePass, BCCryptoHelper.KEYSTORE_PKCS12);
            this.certificateManager.loadKeystoreCertificates(storage);
        } catch (Exception e) {
            this.logger.severe(e.getMessage());
        }
        this.jTreePartner = new JTreePartner(this.configConnection, this.runtimeConnection);
        this.initComponents();
        this.jScrollPaneTree.setViewportView(this.jTreePartner);
        this.panelEditPartner 
                = new JPanelPartner(this.guiClient.getBaseClient(), this.jTreePartner, 
                        this.configConnection, this.runtimeConnection,
                this.certificateManager, this.jButtonOk);
        this.jPanelPartner.add(this.panelEditPartner);
        this.getRootPane().setDefaultButton(this.jButtonOk);
        try {
            Partner[] partner = this.jTreePartner.buildTree();
            this.partnerList.addAll(Arrays.asList(partner));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(parent, e.getMessage());
            return;
        }
        //mix up the add/edit/delete icons
        ImageUtil imageUtil = new ImageUtil();
        ImageIcon iconBase = new ImageIcon(this.getClass().
                getResource("/de/mendelson/comm/as2/partner/gui/singlepartner16x16.gif"));

        ImageIcon iconAdd = new ImageIcon(this.getClass().
                getResource("/de/mendelson/comm/as2/partner/gui/mini_add.gif"));
        ImageIcon iconDelete = new ImageIcon(this.getClass().
                getResource("/de/mendelson/comm/as2/partner/gui/mini_delete.gif"));
        ImageIcon iconMixedAdd = imageUtil.mixImages(iconBase, iconAdd);
        ImageIcon iconMixedDelete = imageUtil.mixImages(iconBase, iconDelete);

        this.jButtonNewPartner.setIcon(iconMixedAdd);
        this.jButtonDeletePartner.setIcon(iconMixedDelete);
        this.jTreePartner.addTreeSelectionListener(new TreeSelectionListener() {

            @Override
            public void valueChanged(TreeSelectionEvent evt) {
                displayPartnerValues();
            }
        });
        this.displayPartnerValues();
    }

    public void setDisplayNotificationPanel(boolean display) {
        this.panelEditPartner.setDisplayNotificationPanel(display);
    }

    public void setDisplayHttpHeaderPanel(boolean display) {
        this.panelEditPartner.setDisplayHttpHeaderPanel(display);
    }

    private void displayPartnerValues() {
        DefaultMutableTreeNode selectedNode = this.jTreePartner.getSelectedNode();
        if (selectedNode == null) {
            return;
        }
        Partner selectedPartner = (Partner) selectedNode.getUserObject();
        this.panelEditPartner.setPartner(selectedPartner, selectedNode);
    }

    private void deleteSelectedPartner() {
        Partner partner = this.jTreePartner.getSelectedPartner();
        if (partner != null) {
            //ask the user if the partner should be really deleted, all data is lost
            int requestValue = JOptionPane.showConfirmDialog(
                    this, this.rb.getResourceString("dialog.partner.delete.message", partner.getName()),
                    this.rb.getResourceString("dialog.partner.delete.title"),
                    JOptionPane.YES_NO_OPTION);
            if (requestValue != JOptionPane.YES_OPTION) {
                return;
            }
            partner = this.jTreePartner.deleteSelectedPartner();
            if (partner != null) {
                this.partnerList.remove(partner);
            }
        }
    }

    private boolean checkAllLocalStationsHavePrivateKeys() {
        Partner[] localStations = this.jTreePartner.getLocalStations();
        //no local station? should not happen
        if (localStations == null || localStations.length == 0) {
            return (false);
        }
        for (Partner localStation : localStations) {
            String signSerial = localStation.getSignFingerprintSHA1();
            String cryptSerial = localStation.getCryptFingerprintSHA1();
            try {
                this.certificateManager.getPrivateKeyByFingerprintSHA1(signSerial);
                this.certificateManager.getPrivateKeyByFingerprintSHA1(cryptSerial);
            } catch (Exception e) {
                e.printStackTrace();
                return (false);
            }
        }
        return (true);
    }

    /**A partner name has been changed: Ask if the underlaying directory should be changed, too*/
    private void handlePartnerNameChange(Partner existingPartner, Partner newPartner) {
        //get the message path from the server
        PreferencesClient preferences = new PreferencesClient(this.guiClient.getBaseClient());
        String messageDir = preferences.get(PreferencesAS2.DIR_MSG);
        int requestValue = JOptionPane.showConfirmDialog(
                this, this.rb.getResourceString("dialog.partner.renamedir.message",
                new Object[]{existingPartner.getName(), newPartner.getName(),
                    existingPartner.getMessagePath(messageDir)}),
                this.rb.getResourceString("dialog.partner.renamedir.title"),
                JOptionPane.YES_NO_OPTION);
        if (requestValue != JOptionPane.YES_OPTION) {
            return;
        }
        FileOperationClient fileClient = new FileOperationClient(this.guiClient.getBaseClient());
        boolean success = fileClient.rename(existingPartner.getMessagePath(messageDir),
                newPartner.getMessagePath(messageDir));
        if (success) {
            this.logger.log(Level.FINE, this.rb.getResourceString("directory.rename.success",
                    new Object[]{existingPartner.getMessagePath(messageDir),
                        newPartner.getMessagePath(messageDir)}));
        } else {
            this.logger.log(Level.SEVERE, this.rb.getResourceString("directory.rename.failure",
                    new Object[]{existingPartner.getMessagePath(messageDir),
                        newPartner.getMessagePath(messageDir)}));
        }
    }

    private void handlePartnerDelete(Partner existingPartner) {
        //get the message path from the server
        PreferencesClient preferences = new PreferencesClient(this.guiClient.getBaseClient());
        String messageDir = preferences.get(PreferencesAS2.DIR_MSG);
        int requestValue = JOptionPane.showConfirmDialog(
                this, this.rb.getResourceString("dialog.partner.deletedir.message",
                new Object[]{existingPartner.getName(),
                    existingPartner.getMessagePath(messageDir)}),
                this.rb.getResourceString("dialog.partner.deletedir.title"),
                JOptionPane.YES_NO_OPTION);
        if (requestValue != JOptionPane.YES_OPTION) {
            return;
        }
        FileOperationClient fileClient = new FileOperationClient(this.guiClient.getBaseClient());
        boolean success = fileClient.delete(existingPartner.getMessagePath(messageDir));
        if (success) {
            this.logger.log(Level.FINE, this.rb.getResourceString("directory.delete.success",
                    new Object[]{existingPartner.getMessagePath(messageDir)}));
        } else {
            this.logger.log(Level.SEVERE, this.rb.getResourceString("directory.delete.failure",
                    new Object[]{existingPartner.getMessagePath(messageDir)}));
        }
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
    
    private void okPressed() {
        //check if a local station is set
                if (!JDialogPartnerConfig.this.jTreePartner.localStationIsSet()) {
                    JOptionPane.showMessageDialog(JDialogPartnerConfig.this,
                            JDialogPartnerConfig.this.rb.getResourceString("nolocalstation.message"),
                            JDialogPartnerConfig.this.rb.getResourceString("nolocalstation.title"),
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                //check if the localstation contains a private key in security settings
                if (!JDialogPartnerConfig.this.checkAllLocalStationsHavePrivateKeys()) {
                    JOptionPane.showMessageDialog(JDialogPartnerConfig.this,
                            JDialogPartnerConfig.this.rb.getResourceString("localstation.noprivatekey.message"),
                            JDialogPartnerConfig.this.rb.getResourceString("localstation.noprivatekey.title"),
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
        final String uniqueId = this.getClass().getName() + ".okPressed." + System.currentTimeMillis();
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                
                try {
                    PartnerAccessDB access 
                            = new PartnerAccessDB(JDialogPartnerConfig.this.configConnection, JDialogPartnerConfig.this.runtimeConnection);
                    //detect if a partner name has been changed
                    for (Partner newPartner : JDialogPartnerConfig.this.partnerList) {
                        if (newPartner.getDBId() != -1) {
                            Partner existingPartner = access.getPartner(newPartner.getDBId());
                            if (existingPartner != null && !newPartner.getName().equals(existingPartner.getName())) {
                                JDialogPartnerConfig.this.handlePartnerNameChange(existingPartner, newPartner);
                            }
                        }
                    }
                    //detect if a partner has been deleted
                    Partner[] existingPartnerArray = access.getPartner();
                    for (Partner existingPartner : existingPartnerArray) {
                        boolean doesStillExist = false;
                        for (Partner newPartner : JDialogPartnerConfig.this.partnerList) {
                            if (newPartner.getDBId() == existingPartner.getDBId()) {
                                doesStillExist = true;
                                break;
                            }
                        }
                        if (!doesStillExist) {
                            JDialogPartnerConfig.this.handlePartnerDelete(existingPartner);
                        }
                    }
                    JDialogPartnerConfig.this.lock();
                    //display wait indicator
                    JDialogPartnerConfig.this.status.startProgressIndeterminate(
                            JDialogPartnerConfig.this.rb.getResourceString("saving"), uniqueId);
                    Partner[] newPartnerArray = new Partner[JDialogPartnerConfig.this.partnerList.size()];
                    JDialogPartnerConfig.this.partnerList.toArray(newPartnerArray);
                    access.updatePartner(newPartnerArray);
                    //inform the server that the configuration has been changed
                    PartnerConfigurationChanged signal = new PartnerConfigurationChanged();
                    JDialogPartnerConfig.this.guiClient.sendAsync(signal);                    
                } catch (Exception e) {
                    JDialogPartnerConfig.this.unlock();                    
                    JDialogPartnerConfig.this.status.stopProgressIfExists(uniqueId);
                    JFrame parent = (JFrame) SwingUtilities.getAncestorOfClass(JFrame.class, JDialogPartnerConfig.this);
                    JOptionPane.showMessageDialog(parent, e.getMessage());
                } finally {
                    JDialogPartnerConfig.this.unlock();
                    JDialogPartnerConfig.this.status.stopProgressIfExists(uniqueId);                    
                    JDialogPartnerConfig.this.setVisible(false);
                    JDialogPartnerConfig.this.dispose();
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

        jToolBar = new javax.swing.JToolBar();
        jButtonNewPartner = new javax.swing.JButton();
        jButtonDeletePartner = new javax.swing.JButton();
        jPanelMain = new javax.swing.JPanel();
        jPanelPartnerMain = new javax.swing.JPanel();
        jSplitPane = new javax.swing.JSplitPane();
        jScrollPaneTree = new javax.swing.JScrollPane();
        jPanelPartner = new javax.swing.JPanel();
        jPanelButton = new javax.swing.JPanel();
        jButtonOk = new javax.swing.JButton();
        jButtonCancel = new javax.swing.JButton();

        setTitle(this.rb.getResourceString( "title" ));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        jToolBar.setFloatable(false);
        jToolBar.setRollover(true);

        jButtonNewPartner.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/mendelson/comm/as2/partner/gui/singlepartner16x16.gif"))); // NOI18N
        jButtonNewPartner.setText(this.rb.getResourceString( "button.new"));
        jButtonNewPartner.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonNewPartnerActionPerformed(evt);
            }
        });
        jToolBar.add(jButtonNewPartner);

        jButtonDeletePartner.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/mendelson/comm/as2/partner/gui/singlepartner16x16.gif"))); // NOI18N
        jButtonDeletePartner.setText(this.rb.getResourceString( "button.delete"));
        jButtonDeletePartner.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDeletePartnerActionPerformed(evt);
            }
        });
        jToolBar.add(jButtonDeletePartner);

        getContentPane().add(jToolBar, java.awt.BorderLayout.NORTH);

        jPanelMain.setLayout(new java.awt.GridBagLayout());

        jPanelPartnerMain.setLayout(new java.awt.GridBagLayout());

        jSplitPane.setDividerLocation(150);
        jSplitPane.setLeftComponent(jScrollPaneTree);

        jPanelPartner.setLayout(new java.awt.BorderLayout());
        jSplitPane.setRightComponent(jPanelPartner);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanelPartnerMain.add(jSplitPane, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelMain.add(jPanelPartnerMain, gridBagConstraints);

        jPanelButton.setLayout(new java.awt.GridBagLayout());

        jButtonOk.setText(this.rb.getResourceString( "button.ok" ));
        jButtonOk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOkActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelButton.add(jButtonOk, gridBagConstraints);

        jButtonCancel.setText(this.rb.getResourceString( "button.cancel" ));
        jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCancelActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelButton.add(jButtonCancel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelMain.add(jPanelButton, gridBagConstraints);

        getContentPane().add(jPanelMain, java.awt.BorderLayout.CENTER);

        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width-841)/2, (screenSize.height-586)/2, 841, 586);
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonDeletePartnerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDeletePartnerActionPerformed
        this.deleteSelectedPartner();
    }//GEN-LAST:event_jButtonDeletePartnerActionPerformed

    private void jButtonNewPartnerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonNewPartnerActionPerformed
        Partner partner = this.jTreePartner.createNewPartner(this.certificateManager);
        this.partnerList.add(partner);
    }//GEN-LAST:event_jButtonNewPartnerActionPerformed

    private void jTreePartnerValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_jTreePartnerValueChanged
    }//GEN-LAST:event_jTreePartnerValueChanged

    private void jButtonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCancelActionPerformed
        this.setVisible(false);
        this.dispose();
    }//GEN-LAST:event_jButtonCancelActionPerformed

    private void jButtonOkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOkActionPerformed
        this.okPressed();
    }//GEN-LAST:event_jButtonOkActionPerformed

    /** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        setVisible(false);
        this.dispose();
    }//GEN-LAST:event_closeDialog
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonCancel;
    private javax.swing.JButton jButtonDeletePartner;
    private javax.swing.JButton jButtonNewPartner;
    private javax.swing.JButton jButtonOk;
    private javax.swing.JPanel jPanelButton;
    private javax.swing.JPanel jPanelMain;
    private javax.swing.JPanel jPanelPartner;
    private javax.swing.JPanel jPanelPartnerMain;
    private javax.swing.JScrollPane jScrollPaneTree;
    private javax.swing.JSplitPane jSplitPane;
    private javax.swing.JToolBar jToolBar;
    // End of variables declaration//GEN-END:variables
}
