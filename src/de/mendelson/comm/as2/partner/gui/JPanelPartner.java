//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/partner/gui/JPanelPartner.java,v 1.1 2012/04/18 14:10:32 heller Exp $
package de.mendelson.comm.as2.partner.gui;

import de.mendelson.util.security.cert.CertificateManager;
import de.mendelson.util.security.cert.KeystoreCertificate;
import de.mendelson.comm.as2.message.AS2Message;
import de.mendelson.comm.as2.message.ResourceBundleAS2Message;
import de.mendelson.comm.as2.message.store.MessageStoreHandler;
import de.mendelson.comm.as2.partner.Partner;
import de.mendelson.comm.as2.partner.PartnerCertificateInformation;
import de.mendelson.comm.as2.partner.PartnerHttpHeader;
import de.mendelson.comm.as2.partner.PartnerSystem;
import de.mendelson.comm.as2.partner.PartnerSystemAccessDB;
import de.mendelson.comm.as2.preferences.PreferencesAS2;
import de.mendelson.comm.as2.send.HttpConnectionParameter;
import de.mendelson.util.MecResourceBundle;
import de.mendelson.util.clientserver.BaseClient;
import de.mendelson.util.clientserver.clients.preferences.PreferencesClient;
import de.mendelson.util.security.cert.ListCellRendererCertificates;
import java.awt.Color;
import java.awt.Component;
import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Panel to edit a single partner
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class JPanelPartner extends JPanel {

    private final String STR_CONTENT_TRANSFER_ENCODING_BINARY = "binary";
    private final String STR_CONTENT_TRANSFER_ENCODING_BASE64 = "base64";
    /**Localize your GUI!*/
    private MecResourceBundle rb = null;
    private MecResourceBundle rbMessage = null;
    /**Partner to edit*/
    private Partner partner = null;
    private DefaultMutableTreeNode partnerNode = null;
    private JTreePartner tree = null;
    private CertificateManager certificateManager;
    private JButton buttonOk = null;
    private PreferencesClient preferences;
    private Logger logger = Logger.getLogger("de.mendelson.as2.client");
    private final Color errorColor = new Color(255, 204, 204);
    private boolean displayNotificationPanel = false;
    private boolean displayHttpHeaderPanel = false;
    private Connection configConnection;
    private Connection runtimeConnection;
    /**Stores the last selection of the tab panels if a new partner is set*/
    private Component lastSelectedPanel = null;

    /** Creates new form JPanelFunctionGraph */
    public JPanelPartner(BaseClient baseClient, JTreePartner tree, Connection configConnection,
            Connection runtimeConnection,
            CertificateManager certificateManager, JButton buttonOk) {
        this.configConnection = configConnection;
        this.runtimeConnection = runtimeConnection;
        this.tree = tree;
        this.buttonOk = buttonOk;
        //load resource bundle
        try {
            this.rb = (MecResourceBundle) ResourceBundle.getBundle(
                    ResourceBundlePartnerPanel.class.getName());
            this.rbMessage = (MecResourceBundle) ResourceBundle.getBundle(
                    ResourceBundleAS2Message.class.getName());
        } catch (MissingResourceException e) {
            throw new RuntimeException("Oops..resource bundle " + e.getClassName() + " not found.");
        }
        this.preferences = new PreferencesClient(baseClient);
        this.initComponents();
        //some disabled checkboxes should still have black text: wrapp their text in html tags
        this.jCheckBoxEdiintFeaturesCEM.setText("<html>" + this.jCheckBoxEdiintFeaturesCEM.getText() + "</html>");
        this.jCheckBoxEdiintFeaturesCompression.setText("<html>" + this.jCheckBoxEdiintFeaturesCompression.getText() + "</html>");
        this.jCheckBoxEdiintFeaturesMA.setText("<html>" + this.jCheckBoxEdiintFeaturesMA.getText() + "</html>");
        this.jTextAreaPartnerSystemInformation.setText(this.rb.getResourceString("partnerinfo"));
        this.jComboBoxContentTransferEncoding.removeAllItems();
        this.jComboBoxContentTransferEncoding.addItem(STR_CONTENT_TRANSFER_ENCODING_BINARY);
        this.jComboBoxContentTransferEncoding.addItem(STR_CONTENT_TRANSFER_ENCODING_BASE64);
        this.jComboBoxHTTPProtocolVersion.removeAllItems();
        this.jComboBoxHTTPProtocolVersion.addItem(HttpConnectionParameter.HTTP_1_0);
        this.jComboBoxHTTPProtocolVersion.addItem(HttpConnectionParameter.HTTP_1_1);
        this.certificateManager = certificateManager;
        for (int i = 0; i < 20; i++) {
            if (i == AS2Message.SIGNATURE_NONE) {
                this.jComboBoxSignType.addItem(this.rbMessage.getResourceString("signature." + AS2Message.SIGNATURE_NONE));
            } else if (i == AS2Message.SIGNATURE_SHA1) {
                this.jComboBoxSignType.addItem(this.rbMessage.getResourceString("signature." + AS2Message.SIGNATURE_SHA1));
            } else if (i == AS2Message.SIGNATURE_MD5) {
                this.jComboBoxSignType.addItem(this.rbMessage.getResourceString("signature." + AS2Message.SIGNATURE_MD5));
            }
            if (i == AS2Message.ENCRYPTION_NONE) {
                this.jComboBoxEncryptionType.addItem(this.rbMessage.getResourceString("encryption." + AS2Message.ENCRYPTION_NONE));
            } else if (i == AS2Message.ENCRYPTION_3DES) {
                this.jComboBoxEncryptionType.addItem(this.rbMessage.getResourceString("encryption." + AS2Message.ENCRYPTION_3DES));
            } else if (i == AS2Message.ENCRYPTION_RC2_40) {
                this.jComboBoxEncryptionType.addItem(this.rbMessage.getResourceString("encryption." + AS2Message.ENCRYPTION_RC2_40));
            } else if (i == AS2Message.ENCRYPTION_RC2_64) {
                this.jComboBoxEncryptionType.addItem(this.rbMessage.getResourceString("encryption." + AS2Message.ENCRYPTION_RC2_64));
            } else if (i == AS2Message.ENCRYPTION_RC2_128) {
                this.jComboBoxEncryptionType.addItem(this.rbMessage.getResourceString("encryption." + AS2Message.ENCRYPTION_RC2_128));
            } else if (i == AS2Message.ENCRYPTION_RC2_196) {
                this.jComboBoxEncryptionType.addItem(this.rbMessage.getResourceString("encryption." + AS2Message.ENCRYPTION_RC2_196));
            } else if (i == AS2Message.ENCRYPTION_AES_128) {
                this.jComboBoxEncryptionType.addItem(this.rbMessage.getResourceString("encryption." + AS2Message.ENCRYPTION_AES_128));
            } else if (i == AS2Message.ENCRYPTION_AES_192) {
                this.jComboBoxEncryptionType.addItem(this.rbMessage.getResourceString("encryption." + AS2Message.ENCRYPTION_AES_192));
            } else if (i == AS2Message.ENCRYPTION_AES_256) {
                this.jComboBoxEncryptionType.addItem(this.rbMessage.getResourceString("encryption." + AS2Message.ENCRYPTION_AES_256));
            } else if (i == AS2Message.ENCRYPTION_RC4_40) {
                this.jComboBoxEncryptionType.addItem(this.rbMessage.getResourceString("encryption." + AS2Message.ENCRYPTION_RC4_40));
            } else if (i == AS2Message.ENCRYPTION_RC4_56) {
                this.jComboBoxEncryptionType.addItem(this.rbMessage.getResourceString("encryption." + AS2Message.ENCRYPTION_RC4_56));
            } else if (i == AS2Message.ENCRYPTION_RC4_128) {
                this.jComboBoxEncryptionType.addItem(this.rbMessage.getResourceString("encryption." + AS2Message.ENCRYPTION_RC4_128));
            } else if (i == AS2Message.ENCRYPTION_DES) {
                this.jComboBoxEncryptionType.addItem(this.rbMessage.getResourceString("encryption." + AS2Message.ENCRYPTION_DES));
            }
        }
        List<KeystoreCertificate> certificateList = this.certificateManager.getKeyStoreCertificateList();
        //clone the array
        List<KeystoreCertificate> sortedCertificateList = new ArrayList<KeystoreCertificate>();
        sortedCertificateList.addAll(certificateList);
        Collections.sort(sortedCertificateList);
        this.jComboBoxSignCert.setRenderer(new ListCellRendererCertificates());
        this.jComboBoxCryptCert.setRenderer(new ListCellRendererCertificates());
        for (KeystoreCertificate cert : sortedCertificateList) {
            this.jComboBoxSignCert.addItem(cert);
            this.jComboBoxCryptCert.addItem(cert);
        }
        this.jTextPanePartnerComment.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                if (partner != null) {
                    partner.setComment(jTextPanePartnerComment.getText());
                    informTreeModelNodeChanged();
                }
                setButtonState();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (partner != null) {
                    partner.setComment(jTextPanePartnerComment.getText());
                    informTreeModelNodeChanged();
                }
                setButtonState();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                if (partner != null) {
                    partner.setComment(jTextPanePartnerComment.getText());
                    informTreeModelNodeChanged();
                }
                setButtonState();
            }
        });
        this.jTableHttpHeader.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                jButtonHttpHeaderRemove.setEnabled(jTableHttpHeader.getSelectedRow() >= 0);
            }
        });
        this.jTableHttpHeader.getTableHeader().setReorderingAllowed(false);
    }

    public void setDisplayNotificationPanel(boolean display) {
        this.displayNotificationPanel = display;
    }

    public void setDisplayHttpHeaderPanel(boolean display) {
        this.displayHttpHeaderPanel = display;
    }

    /**Informs the partner tree model that a node value has been changed*/
    private void informTreeModelNodeChanged() {
        ((DefaultTreeModel) this.tree.getModel()).nodeChanged(this.partnerNode);
    }

    /**Edits a passed partner*/
    public void setPartner(Partner partner, DefaultMutableTreeNode selectedNode) {
        this.lastSelectedPanel = this.jTabbedPane.getSelectedComponent();
        this.partnerNode = selectedNode;
        this.partner = partner;
        this.jTextFieldId.setText(partner.getAS2Identification());
        this.jTextFieldName.setText(partner.getName());
        this.jTextFieldURL.setText(partner.getURL());
        this.jTextFieldMDNURL.setText(partner.getMdnURL());
        this.jTextFieldEMail.setText(partner.getEmail());
        this.jCheckBoxLocalStation.setSelected(partner.isLocalStation());
        this.jComboBoxSignCert.setSelectedItem(this.certificateManager.getKeystoreCertificateByFingerprintSHA1(partner.getSignFingerprintSHA1()));
        this.jComboBoxCryptCert.setSelectedItem(this.certificateManager.getKeystoreCertificateByFingerprintSHA1(partner.getCryptFingerprintSHA1()));
        if (partner.isLocalStation()) {
            this.jLabelCryptAlias.setText(this.rb.getResourceString("label.cryptalias.key"));
            this.jLabelSignAlias.setText(this.rb.getResourceString("label.signalias.key"));
        } else {
            this.jLabelCryptAlias.setText(this.rb.getResourceString("label.cryptalias.cert"));
            this.jLabelSignAlias.setText(this.rb.getResourceString("label.signalias.cert"));
        }
        if (partner.getSignType() == AS2Message.SIGNATURE_NONE) {
            this.jComboBoxSignType.setSelectedItem(this.rbMessage.getResourceString("signature." + AS2Message.SIGNATURE_NONE));
        } else if (partner.getSignType() == AS2Message.SIGNATURE_SHA1) {
            this.jComboBoxSignType.setSelectedItem(this.rbMessage.getResourceString("signature." + AS2Message.SIGNATURE_SHA1));
        } else if (partner.getSignType() == AS2Message.SIGNATURE_MD5) {
            this.jComboBoxSignType.setSelectedItem(this.rbMessage.getResourceString("signature." + AS2Message.SIGNATURE_MD5));
        }
        if (partner.getEncryptionType() == AS2Message.ENCRYPTION_NONE) {
            this.jComboBoxEncryptionType.setSelectedItem(this.rbMessage.getResourceString("encryption." + AS2Message.ENCRYPTION_NONE));
        } else if (partner.getEncryptionType() == AS2Message.ENCRYPTION_3DES) {
            this.jComboBoxEncryptionType.setSelectedItem(this.rbMessage.getResourceString("encryption." + AS2Message.ENCRYPTION_3DES));
        } else if (partner.getEncryptionType() == AS2Message.ENCRYPTION_RC2_40) {
            this.jComboBoxEncryptionType.setSelectedItem(this.rbMessage.getResourceString("encryption." + AS2Message.ENCRYPTION_RC2_40));
        } else if (partner.getEncryptionType() == AS2Message.ENCRYPTION_RC2_64) {
            this.jComboBoxEncryptionType.setSelectedItem(this.rbMessage.getResourceString("encryption." + AS2Message.ENCRYPTION_RC2_64));
        } else if (partner.getEncryptionType() == AS2Message.ENCRYPTION_RC2_128) {
            this.jComboBoxEncryptionType.setSelectedItem(this.rbMessage.getResourceString("encryption." + AS2Message.ENCRYPTION_RC2_128));
        } else if (partner.getEncryptionType() == AS2Message.ENCRYPTION_RC2_196) {
            this.jComboBoxEncryptionType.setSelectedItem(this.rbMessage.getResourceString("encryption." + AS2Message.ENCRYPTION_RC2_196));
        } else if (partner.getEncryptionType() == AS2Message.ENCRYPTION_AES_128) {
            this.jComboBoxEncryptionType.setSelectedItem(this.rbMessage.getResourceString("encryption." + AS2Message.ENCRYPTION_AES_128));
        } else if (partner.getEncryptionType() == AS2Message.ENCRYPTION_AES_192) {
            this.jComboBoxEncryptionType.setSelectedItem(this.rbMessage.getResourceString("encryption." + AS2Message.ENCRYPTION_AES_192));
        } else if (partner.getEncryptionType() == AS2Message.ENCRYPTION_AES_256) {
            this.jComboBoxEncryptionType.setSelectedItem(this.rbMessage.getResourceString("encryption." + AS2Message.ENCRYPTION_AES_256));
        } else if (partner.getEncryptionType() == AS2Message.ENCRYPTION_RC4_40) {
            this.jComboBoxEncryptionType.setSelectedItem(this.rbMessage.getResourceString("encryption." + AS2Message.ENCRYPTION_RC4_40));
        } else if (partner.getEncryptionType() == AS2Message.ENCRYPTION_RC4_56) {
            this.jComboBoxEncryptionType.setSelectedItem(this.rbMessage.getResourceString("encryption." + AS2Message.ENCRYPTION_RC4_56));
        } else if (partner.getEncryptionType() == AS2Message.ENCRYPTION_RC4_128) {
            this.jComboBoxEncryptionType.setSelectedItem(this.rbMessage.getResourceString("encryption." + AS2Message.ENCRYPTION_RC4_128));
        } else if (partner.getEncryptionType() == AS2Message.ENCRYPTION_DES) {
            this.jComboBoxEncryptionType.setSelectedItem(this.rbMessage.getResourceString("encryption." + AS2Message.ENCRYPTION_DES));
        }
        this.jTextFieldSubject.setText(partner.getSubject());
        this.jTextFieldContentType.setText(partner.getContentType());
        this.jRadioButtonSyncMDN.setSelected(partner.isSyncMDN());
        this.jRadioButtonAsyncMDN.setSelected(!partner.isSyncMDN());
        this.jLabelIconSyncMDN.setEnabled(partner.isSyncMDN());
        this.jLabelIconAsyncMDN.setEnabled(!partner.isSyncMDN());
        this.jCheckBoxSignedMDN.setSelected(partner.isSignedMDN());
        this.setOkButton(this.partner, this.partner.getURL(), this.partner.getMdnURL(),
                this.partner.getName(), this.partner.getAS2Identification());
        this.updatePollDirDisplay();
        String pollIgnoreList = this.partner.getPollIgnoreListAsString();
        if (pollIgnoreList == null) {
            this.jTextFieldIgnorePollFilterList.setText("");
        } else {
            this.jTextFieldIgnorePollFilterList.setText(pollIgnoreList);
        }
        this.jTextFieldPollMaxFiles.setText(String.valueOf(this.partner.getMaxPollFiles()));
        this.jTextFieldPollInterval.setText(String.valueOf(this.partner.getPollInterval()));
        this.jCheckBoxCompress.setSelected(this.partner.getCompressionType() == AS2Message.COMPRESSION_ZLIB);
        this.jTextFieldCommandOnReceipt.setText(this.partner.getCommandOnReceipt());
        this.jCheckBoxUseCommandOnReceipt.setSelected(this.partner.useCommandOnReceipt());
        this.jTextFieldCommandOnReceipt.setEditable(this.jCheckBoxUseCommandOnReceipt.isSelected());
        this.jTextFieldCommandOnReceipt.setEnabled(this.jCheckBoxUseCommandOnReceipt.isSelected());
        this.jCheckBoxHttpAuth.setSelected(this.partner.getAuthentication().isEnabled());
        this.jTextFieldHttpAuthUser.setText(this.partner.getAuthentication().getUser());
        this.jPasswordFieldHttpPass.setText(this.partner.getAuthentication().getPassword());
        this.jCheckBoxHttpAuthAsyncMDN.setSelected(this.partner.getAuthenticationAsyncMDN().isEnabled());
        this.jTextFieldHttpAuthAsyncMDNUser.setText(this.partner.getAuthenticationAsyncMDN().getUser());
        this.jPasswordFieldHttpPassAsyncMDN.setText(this.partner.getAuthenticationAsyncMDN().getPassword());
        this.jCheckBoxKeepFilenameOnReceipt.setSelected(this.partner.getKeepOriginalFilenameOnReceipt());
        if (this.partner.getComment() != null && this.partner.getComment().length() > 0) {
            this.jTextPanePartnerComment.setText(this.partner.getComment());
        } else if (this.jTextPanePartnerComment.getText().length() > 0) {
            this.jTextPanePartnerComment.setText("");
        }
        this.jCheckBoxNotifySend.setSelected(this.partner.isNotifySendEnabled());
        this.jCheckBoxNotifyReceive.setSelected(this.partner.isNotifyReceiveEnabled());
        this.jCheckBoxNotifySendReceive.setSelected(this.partner.isNotifySendReceiveEnabled());
        this.jTextFieldNotifySend.setText(String.valueOf(this.partner.getNotifySend()));
        this.jTextFieldNotifyReceive.setText(String.valueOf(this.partner.getNotifyReceive()));
        this.jTextFieldNotifySendReceive.setText(String.valueOf(this.partner.getNotifySendReceive()));
        this.jTextFieldCommandOnSendError.setText(this.partner.getCommandOnSendError());
        this.jCheckBoxUseCommandOnSendError.setSelected(this.partner.useCommandOnSendError());
        this.jTextFieldCommandOnSendError.setEditable(this.jCheckBoxUseCommandOnSendError.isSelected());
        this.jTextFieldCommandOnSendError.setEnabled(this.jCheckBoxUseCommandOnSendError.isSelected());
        this.jTextFieldCommandOnSendSuccess.setText(this.partner.getCommandOnSendSuccess());
        this.jCheckBoxUseCommandOnSendSuccess.setSelected(this.partner.useCommandOnSendSuccess());
        this.jTextFieldCommandOnSendSuccess.setEditable(this.jCheckBoxUseCommandOnSendSuccess.isSelected());
        this.jTextFieldCommandOnSendSuccess.setEnabled(this.jCheckBoxUseCommandOnSendSuccess.isSelected());

        if (this.partner.getContentTransferEncoding() == AS2Message.CONTENT_TRANSFER_ENCODING_BINARY) {
            this.jComboBoxContentTransferEncoding.setSelectedItem(STR_CONTENT_TRANSFER_ENCODING_BINARY);
        } else {
            this.jComboBoxContentTransferEncoding.setSelectedItem(STR_CONTENT_TRANSFER_ENCODING_BASE64);
        }
        PartnerSystemAccessDB partnerSystemAccess 
                = new PartnerSystemAccessDB(this.configConnection, this.runtimeConnection);
        PartnerSystem partnerSystem = partnerSystemAccess.getPartnerSystem(partner);
        if (partnerSystem != null) {
            this.jTextFieldAS2Version.setText(partnerSystem.getAs2Version());
            this.jTextFieldProductName.setText(partnerSystem.getProductName());
            this.jCheckBoxEdiintFeaturesCompression.setSelected(partnerSystem.supportsCompression());
            this.jCheckBoxEdiintFeaturesCEM.setSelected(partnerSystem.supportsCEM());
            this.jCheckBoxEdiintFeaturesMA.setSelected(partnerSystem.supportsMA());
        } else {
            this.jTextFieldAS2Version.setText(this.rb.getResourceString("partnersystem.noinfo"));
            this.jTextFieldProductName.setText(this.rb.getResourceString("partnersystem.noinfo"));
            this.jCheckBoxEdiintFeaturesCompression.setSelected(false);
            this.jCheckBoxEdiintFeaturesCEM.setSelected(false);
            this.jCheckBoxEdiintFeaturesMA.setSelected(false);
        }
        ((TableModelHttpHeader) this.jTableHttpHeader.getModel()).passNewData(partner.getHttpHeader());
        this.jComboBoxHTTPProtocolVersion.setSelectedItem(partner.getHttpProtocolVersion());
        this.handleVisibilityStateOfWidgets();
        this.updateHttpAuthState();
        this.setPanelVisiblilityState();
        try {
            if (this.lastSelectedPanel != null) {
                this.jTabbedPane.setSelectedComponent(this.lastSelectedPanel);
            }
        } catch (Exception e) {
            //ignore, not every panel that was selected for the last partner must be available for this
            //partner
        }
    }

    /**Sets the visibility state depending if the partner is local station or not. Has to be called
     * every time the local station state changes.
     */
    private void handleVisibilityStateOfWidgets() {
        this.jTextFieldMDNURL.setVisible(this.partner.isLocalStation());
        this.jLabelMDNURL.setVisible(this.partner.isLocalStation());
        this.jLabelMDNURLHint.setVisible(this.partner.isLocalStation());
        this.jRadioButtonAsyncMDN.setVisible(!partner.isLocalStation());
        this.jLabelIconAsyncMDN.setVisible(!partner.isLocalStation());
        this.jLabelIconSyncMDN.setVisible(!partner.isLocalStation());
        this.jRadioButtonSyncMDN.setVisible(!partner.isLocalStation());
        this.jCheckBoxSignedMDN.setVisible(!partner.isLocalStation());
    }

    private void setPanelVisiblilityState() {
        this.jTabbedPane.removeAll();
        this.jTabbedPane.addTab(this.rb.getResourceString("tab.misc"), this.jPanelMisc);
        this.jTabbedPane.addTab(this.rb.getResourceString("tab.security"), this.jPanelSecurity);
        if (!this.partner.isLocalStation()) {
            this.jTabbedPane.addTab(this.rb.getResourceString("tab.send"), this.jPanelSend);
        }
        this.jTabbedPane.addTab(this.rb.getResourceString("tab.mdn"), this.jPanelMDN);
        if (!this.partner.isLocalStation()) {
            this.jTabbedPane.addTab(this.rb.getResourceString("tab.dirpoll"), this.jPanelDirPoll);
            this.jTabbedPane.addTab(this.rb.getResourceString("tab.receipt"), this.jPanelReceipt);
            this.jTabbedPane.addTab(this.rb.getResourceString("tab.httpauth"), this.jPanelHTTPAuth);
            if (this.displayHttpHeaderPanel) {
                this.jTabbedPane.addTab(this.rb.getResourceString("tab.httpheader"), this.jPanelHTTPHeader);
            }
            if (this.displayNotificationPanel) {
                this.jTabbedPane.addTab(this.rb.getResourceString("tab.notification"), this.jPanelNotification);
            }
            this.jTabbedPane.addTab(this.rb.getResourceString("tab.events"), this.jPanelEvents);
            this.jTabbedPane.addTab(this.rb.getResourceString("tab.partnersystem"), this.jPanelPartnerSystem);
        }
    }

    /**graphically updates the state of the input fields in the HTTP auth panel
     */
    private void updateHttpAuthState() {
        this.jTextFieldHttpAuthUser.setEditable(this.jCheckBoxHttpAuth.isSelected());
        this.jTextFieldHttpAuthUser.setEnabled(this.jCheckBoxHttpAuth.isSelected());
        this.jPasswordFieldHttpPass.setEditable(this.jCheckBoxHttpAuth.isSelected());
        this.jPasswordFieldHttpPass.setEnabled(this.jCheckBoxHttpAuth.isSelected());
        this.jTextFieldHttpAuthAsyncMDNUser.setEditable(this.jCheckBoxHttpAuthAsyncMDN.isSelected());
        this.jTextFieldHttpAuthAsyncMDNUser.setEnabled(this.jCheckBoxHttpAuthAsyncMDN.isSelected());
        this.jPasswordFieldHttpPassAsyncMDN.setEditable(this.jCheckBoxHttpAuthAsyncMDN.isSelected());
        this.jPasswordFieldHttpPassAsyncMDN.setEnabled(this.jCheckBoxHttpAuthAsyncMDN.isSelected());
    }

    /**Displays the directory that is assigned with the partner to be polled. It must not be the same because
     *the name may not be a valid filename
     */
    private void updatePollDirDisplay() {
        StringBuilder filename = new StringBuilder();
        filename.append(this.preferences.get(PreferencesAS2.DIR_MSG));
        filename.append(File.separator);
        filename.append(MessageStoreHandler.convertToValidFilename(this.partner.getName()));
        filename.append(File.separator);
        filename.append("outbox");
        //for single local stations display add the name of the local station, else display <loalstation>
        Partner[] localStations = this.tree.getLocalStations();
        String localStationDir = "<localstation>";
        if (localStations.length == 1) {
            localStationDir = localStations[0].getName();
        }
        this.jTextFieldPollDir.setText(new File(filename.toString()).getAbsolutePath() + File.separator + localStationDir);
    }

    private void setButtonState() {
        if (this.partner != null) {
            this.jTextFieldURL.setEditable(!this.partner.isLocalStation());
            this.jTextFieldURL.setEnabled(!this.partner.isLocalStation());
            this.jTextFieldEMail.setEnabled(this.partner.isLocalStation());
            this.jTextFieldEMail.setEditable(this.partner.isLocalStation());
            this.jLabelCertSignType.setVisible(!this.partner.isLocalStation());
            this.jLabelEncryptionType.setVisible(!this.partner.isLocalStation());
            this.jComboBoxEncryptionType.setVisible(!this.partner.isLocalStation());
            this.jComboBoxSignType.setVisible(!this.partner.isLocalStation());
            this.jPanelSendMain.setVisible(!this.partner.isLocalStation());
            this.jPanelPollOptions.setVisible(!this.partner.isLocalStation());
            this.jPanelReceiptOptions.setVisible(!this.partner.isLocalStation());
            this.jPanelHttpAuthData.setVisible(!this.partner.isLocalStation());
            this.jTextFieldNotifySend.setEnabled(this.partner.isNotifySendEnabled());
            this.jTextFieldNotifySend.setEditable(this.partner.isNotifySendEnabled());
            this.jTextFieldNotifyReceive.setEnabled(this.partner.isNotifyReceiveEnabled());
            this.jTextFieldNotifyReceive.setEditable(this.partner.isNotifyReceiveEnabled());
            this.jTextFieldNotifySendReceive.setEnabled(this.partner.isNotifySendReceiveEnabled());
            this.jTextFieldNotifySendReceive.setEditable(this.partner.isNotifySendReceiveEnabled());
        }
    }

    /**Sets the ok button depending on the partner settings
     */
    private void setOkButton(Partner partner, String receiverURL, String mdnURL, String partnerName,
            String as2id) {
        boolean error = this.checkURLProtocol(partner, receiverURL, mdnURL) || this.checkForNonUniqueValues(partnerName, as2id);
        this.buttonOk.setEnabled(!error);
    }

    /**Checks if the passed URLs contain a leading protocol entry
     * 
     */
    private boolean checkURLProtocol(Partner partner, String receiverURL, String mdnURL) {
        boolean error = false;
        if (!partner.isLocalStation()) {
            //no local station
            if (receiverURL == null || (!receiverURL.startsWith("http://") && !receiverURL.startsWith("https://"))) {
                this.jTextFieldURL.setBackground(this.errorColor);
                error = true;
            } else {
                this.jTextFieldURL.setBackground(UIManager.getDefaults().getColor("TextField.background"));
            }
        } else {
            //local station
            if (mdnURL == null || (!mdnURL.startsWith("http://") && !mdnURL.startsWith("https://"))) {
                this.jTextFieldMDNURL.setBackground(this.errorColor);
                error = true;
            } else {
                this.jTextFieldURL.setBackground(UIManager.getDefaults().getColor("TextField.background"));
            }
        }
        return (error);
    }

    /**Checks if new name is unique and changes color in textfield if not
     */
    private boolean checkForNonUniqueValues(String newName, String newAS2Id) {
        boolean error = false;
        Partner existingPartnerName = this.tree.getPartnerByName(newName);
        int nameCount = this.tree.getPartnerCountByName(newName);
        if (newName != null && newName.trim().length() > 0 && ((nameCount == 1 && existingPartnerName.equals(this.partner)) || nameCount == 0)) {
            this.jTextFieldName.setBackground(UIManager.getDefaults().getColor("TextField.background"));
        } else {
            this.jTextFieldName.setBackground(this.errorColor);
            error = true;
        }
        Partner existingPartnerAS2Id = this.tree.getPartnerByAS2Id(newAS2Id);
        int idCount = this.tree.getPartnerCountByAS2Id(newAS2Id);
        if (newAS2Id != null && newAS2Id.trim().length() > 0 && ((idCount == 1 && existingPartnerAS2Id.equals(this.partner)) || idCount == 0)) {
            this.jTextFieldId.setBackground(UIManager.getDefaults().getColor("TextField.background"));
        } else {
            this.jTextFieldId.setBackground(this.errorColor);
            error = true;
        }
        return (error);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        buttonGroupSyncAsyncMDN = new javax.swing.ButtonGroup();
        jTabbedPane = new javax.swing.JTabbedPane();
        jPanelMisc = new javax.swing.JPanel();
        jTextFieldId = new javax.swing.JTextField();
        jTextFieldName = new javax.swing.JTextField();
        jLabelName = new javax.swing.JLabel();
        jLabelId = new javax.swing.JLabel();
        jCheckBoxLocalStation = new javax.swing.JCheckBox();
        jLabelEMail = new javax.swing.JLabel();
        jTextFieldEMail = new javax.swing.JTextField();
        jScrollPanePartnerComment = new javax.swing.JScrollPane();
        jTextPanePartnerComment = new javax.swing.JTextPane();
        jLabelPartnerComment = new javax.swing.JLabel();
        jPanelSecurity = new javax.swing.JPanel();
        jLabelSignAlias = new javax.swing.JLabel();
        jComboBoxSignCert = new javax.swing.JComboBox();
        jPanelSpace2 = new javax.swing.JPanel();
        jComboBoxSignType = new javax.swing.JComboBox();
        jLabelEncryptionType = new javax.swing.JLabel();
        jComboBoxEncryptionType = new javax.swing.JComboBox();
        jLabelCertSignType = new javax.swing.JLabel();
        jLabelCryptAlias = new javax.swing.JLabel();
        jComboBoxCryptCert = new javax.swing.JComboBox();
        jPanelSend = new javax.swing.JPanel();
        jPanelSendMain = new javax.swing.JPanel();
        jLabelURL = new javax.swing.JLabel();
        jTextFieldURL = new javax.swing.JTextField();
        jLabelSubject = new javax.swing.JLabel();
        jTextFieldSubject = new javax.swing.JTextField();
        jLabelContentType = new javax.swing.JLabel();
        jTextFieldContentType = new javax.swing.JTextField();
        jPanelSpace14 = new javax.swing.JPanel();
        jCheckBoxCompress = new javax.swing.JCheckBox();
        jLabelSendUrlHint = new javax.swing.JLabel();
        jLabelSubjectHint = new javax.swing.JLabel();
        jPanelSep = new javax.swing.JPanel();
        jSeparator1 = new javax.swing.JSeparator();
        jComboBoxContentTransferEncoding = new javax.swing.JComboBox();
        jLabelContentTransferEncoding = new javax.swing.JLabel();
        jLabelHTTPProtocolVersion = new javax.swing.JLabel();
        jComboBoxHTTPProtocolVersion = new javax.swing.JComboBox();
        jPanelMDN = new javax.swing.JPanel();
        jPanelMDNMain = new javax.swing.JPanel();
        jLabelMDNURL = new javax.swing.JLabel();
        jTextFieldMDNURL = new javax.swing.JTextField();
        jPanelSpace99 = new javax.swing.JPanel();
        jRadioButtonSyncMDN = new javax.swing.JRadioButton();
        jRadioButtonAsyncMDN = new javax.swing.JRadioButton();
        jCheckBoxSignedMDN = new javax.swing.JCheckBox();
        jLabelMDNURLHint = new javax.swing.JLabel();
        jLabelIconSyncMDN = new javax.swing.JLabel();
        jLabelIconAsyncMDN = new javax.swing.JLabel();
        jPanelDirPoll = new javax.swing.JPanel();
        jPanelPollOptions = new javax.swing.JPanel();
        jLabelPollDir = new javax.swing.JLabel();
        jTextFieldPollDir = new javax.swing.JTextField();
        jLabelPollInterval = new javax.swing.JLabel();
        jTextFieldPollInterval = new javax.swing.JTextField();
        jPanelSpaceX = new javax.swing.JPanel();
        jLabelSeconds = new javax.swing.JLabel();
        jLabelIgnorePollFilterList = new javax.swing.JLabel();
        jTextFieldIgnorePollFilterList = new javax.swing.JTextField();
        jLabelPollMaxFiles = new javax.swing.JLabel();
        jTextFieldPollMaxFiles = new javax.swing.JTextField();
        jPanelReceipt = new javax.swing.JPanel();
        jPanelReceiptOptions = new javax.swing.JPanel();
        jPanelSpace456 = new javax.swing.JPanel();
        jCheckBoxKeepFilenameOnReceipt = new javax.swing.JCheckBox();
        jLabelHintKeepFilenameOnReceipt = new javax.swing.JLabel();
        jPanelHTTPAuth = new javax.swing.JPanel();
        jPanelHttpAuthData = new javax.swing.JPanel();
        jCheckBoxHttpAuth = new javax.swing.JCheckBox();
        jLabelHttpAuth = new javax.swing.JLabel();
        jTextFieldHttpAuthUser = new javax.swing.JTextField();
        jLabelHttpPass = new javax.swing.JLabel();
        jPasswordFieldHttpPass = new javax.swing.JPasswordField();
        jCheckBoxHttpAuthAsyncMDN = new javax.swing.JCheckBox();
        jLabelHttpAuthAsyncMDN = new javax.swing.JLabel();
        jTextFieldHttpAuthAsyncMDNUser = new javax.swing.JTextField();
        jLabelHttpPassAsyncMDN = new javax.swing.JLabel();
        jPasswordFieldHttpPassAsyncMDN = new javax.swing.JPasswordField();
        jPanelSpace199 = new javax.swing.JPanel();
        jPanelHTTPHeader = new javax.swing.JPanel();
        jScrollPaneHttpHeader = new javax.swing.JScrollPane();
        jTableHttpHeader = new javax.swing.JTable();
        jButtonHttpHeaderAdd = new javax.swing.JButton();
        jButtonHttpHeaderRemove = new javax.swing.JButton();
        jPanelNotification = new javax.swing.JPanel();
        jPanelNotificationMain = new javax.swing.JPanel();
        jCheckBoxNotifySend = new javax.swing.JCheckBox();
        jCheckBoxNotifyReceive = new javax.swing.JCheckBox();
        jCheckBoxNotifySendReceive = new javax.swing.JCheckBox();
        jTextFieldNotifyReceive = new javax.swing.JTextField();
        jTextFieldNotifySend = new javax.swing.JTextField();
        jTextFieldNotifySendReceive = new javax.swing.JTextField();
        jPanelSpace23 = new javax.swing.JPanel();
        jPanelEvents = new javax.swing.JPanel();
        jPanelEventsMain = new javax.swing.JPanel();
        jCheckBoxUseCommandOnSendError = new javax.swing.JCheckBox();
        jTextFieldCommandOnSendError = new javax.swing.JTextField();
        jLabelHintCommandOnSendError1 = new javax.swing.JLabel();
        jCheckBoxUseCommandOnSendSuccess = new javax.swing.JCheckBox();
        jTextFieldCommandOnSendSuccess = new javax.swing.JTextField();
        jLabelHintCommandOnSendSuccess1 = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        jCheckBoxUseCommandOnReceipt = new javax.swing.JCheckBox();
        jTextFieldCommandOnReceipt = new javax.swing.JTextField();
        jLabelHintCommandOnReceipt1 = new javax.swing.JLabel();
        jPanelSpace = new javax.swing.JPanel();
        jLabelHintCommandOnReceipt2 = new javax.swing.JLabel();
        jLabelHintCommandOnSendError2 = new javax.swing.JLabel();
        jLabelHintCommandOnSendSuccess2 = new javax.swing.JLabel();
        jPanelPartnerSystem = new javax.swing.JPanel();
        jPanelPartnerSystemMain = new javax.swing.JPanel();
        jLabelAS2Version = new javax.swing.JLabel();
        jLabelProductName = new javax.swing.JLabel();
        jLabelFeatures = new javax.swing.JLabel();
        jCheckBoxEdiintFeaturesCompression = new javax.swing.JCheckBox();
        jCheckBoxEdiintFeaturesMA = new javax.swing.JCheckBox();
        jCheckBoxEdiintFeaturesCEM = new javax.swing.JCheckBox();
        jTextFieldAS2Version = new javax.swing.JTextField();
        jTextFieldProductName = new javax.swing.JTextField();
        jPanelSpaceSpace = new javax.swing.JPanel();
        jScrollPaneTextAreaPartnerSystemInformation = new javax.swing.JScrollPane();
        jTextAreaPartnerSystemInformation = new javax.swing.JTextArea();

        setLayout(new java.awt.GridBagLayout());

        jPanelMisc.setLayout(new java.awt.GridBagLayout());

        jTextFieldId.setColumns(30);
        jTextFieldId.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextFieldIdKeyReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 10);
        jPanelMisc.add(jTextFieldId, gridBagConstraints);

        jTextFieldName.setColumns(30);
        jTextFieldName.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextFieldNameKeyReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 10);
        jPanelMisc.add(jTextFieldName, gridBagConstraints);

        jLabelName.setText(this.rb.getResourceString( "label.name"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 5);
        jPanelMisc.add(jLabelName, gridBagConstraints);

        jLabelId.setText(this.rb.getResourceString( "label.id"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 5);
        jPanelMisc.add(jLabelId, gridBagConstraints);

        jCheckBoxLocalStation.setText(this.rb.getResourceString( "label.localstation"));
        jCheckBoxLocalStation.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCheckBoxLocalStationItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 5, 5);
        jPanelMisc.add(jCheckBoxLocalStation, gridBagConstraints);

        jLabelEMail.setText(this.rb.getResourceString( "label.email"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 5);
        jPanelMisc.add(jLabelEMail, gridBagConstraints);

        jTextFieldEMail.setColumns(30);
        jTextFieldEMail.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextFieldEMailKeyReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 10);
        jPanelMisc.add(jTextFieldEMail, gridBagConstraints);

        jScrollPanePartnerComment.setViewportView(jTextPanePartnerComment);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 10, 10);
        jPanelMisc.add(jScrollPanePartnerComment, gridBagConstraints);

        jLabelPartnerComment.setText(this.rb.getResourceString( "label.partnercomment"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 5);
        jPanelMisc.add(jLabelPartnerComment, gridBagConstraints);

        jTabbedPane.addTab(this.rb.getResourceString( "tab.misc"), jPanelMisc);

        jPanelSecurity.setLayout(new java.awt.GridBagLayout());

        jLabelSignAlias.setText("<signalias>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 5);
        jPanelSecurity.add(jLabelSignAlias, gridBagConstraints);

        jComboBoxSignCert.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxSignCertActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 10);
        jPanelSecurity.add(jComboBoxSignCert, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.weighty = 1.0;
        jPanelSecurity.add(jPanelSpace2, gridBagConstraints);

        jComboBoxSignType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxSignTypeActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 10);
        jPanelSecurity.add(jComboBoxSignType, gridBagConstraints);

        jLabelEncryptionType.setText(this.rb.getResourceString( "label.encryptiontype" ));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 5);
        jPanelSecurity.add(jLabelEncryptionType, gridBagConstraints);

        jComboBoxEncryptionType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxEncryptionTypeActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 10);
        jPanelSecurity.add(jComboBoxEncryptionType, gridBagConstraints);

        jLabelCertSignType.setText(this.rb.getResourceString( "label.signtype"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 5);
        jPanelSecurity.add(jLabelCertSignType, gridBagConstraints);

        jLabelCryptAlias.setText("<cryptalias>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 5, 5);
        jPanelSecurity.add(jLabelCryptAlias, gridBagConstraints);

        jComboBoxCryptCert.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxCryptCertActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 5, 10);
        jPanelSecurity.add(jComboBoxCryptCert, gridBagConstraints);

        jTabbedPane.addTab(this.rb.getResourceString( "tab.security"), jPanelSecurity);

        jPanelSend.setLayout(new java.awt.GridBagLayout());

        jPanelSendMain.setLayout(new java.awt.GridBagLayout());

        jLabelURL.setText(this.rb.getResourceString( "label.url"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelSendMain.add(jLabelURL, gridBagConstraints);

        jTextFieldURL.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextFieldURLKeyReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelSendMain.add(jTextFieldURL, gridBagConstraints);

        jLabelSubject.setText(this.rb.getResourceString( "label.subject"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelSendMain.add(jLabelSubject, gridBagConstraints);

        jTextFieldSubject.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextFieldSubjectKeyReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelSendMain.add(jTextFieldSubject, gridBagConstraints);

        jLabelContentType.setText(this.rb.getResourceString( "label.contenttype"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelSendMain.add(jLabelContentType, gridBagConstraints);

        jTextFieldContentType.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextFieldContentTypeKeyReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelSendMain.add(jTextFieldContentType, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.weighty = 1.0;
        jPanelSendMain.add(jPanelSpace14, gridBagConstraints);

        jCheckBoxCompress.setText(this.rb.getResourceString( "label.compression"));
        jCheckBoxCompress.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jCheckBoxCompress.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jCheckBoxCompress.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxCompressActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 10, 5);
        jPanelSendMain.add(jCheckBoxCompress, gridBagConstraints);

        jLabelSendUrlHint.setFont(new java.awt.Font("Tahoma", 2, 11));
        jLabelSendUrlHint.setText(this.rb.getResourceString( "label.url.hint"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 10, 5);
        jPanelSendMain.add(jLabelSendUrlHint, gridBagConstraints);

        jLabelSubjectHint.setFont(new java.awt.Font("Tahoma", 2, 11));
        jLabelSubjectHint.setText(this.rb.getResourceString( "hint.subject.replacement"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelSendMain.add(jLabelSubjectHint, gridBagConstraints);

        jPanelSep.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanelSep.add(jSeparator1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 10, 0);
        jPanelSendMain.add(jPanelSep, gridBagConstraints);

        jComboBoxContentTransferEncoding.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBoxContentTransferEncoding.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxContentTransferEncodingActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 5, 5);
        jPanelSendMain.add(jComboBoxContentTransferEncoding, gridBagConstraints);

        jLabelContentTransferEncoding.setText("Content Transfer Encoding:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 5, 5);
        jPanelSendMain.add(jLabelContentTransferEncoding, gridBagConstraints);

        jLabelHTTPProtocolVersion.setText(this.rb.getResourceString("label.httpversion"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 5, 5);
        jPanelSendMain.add(jLabelHTTPProtocolVersion, gridBagConstraints);

        jComboBoxHTTPProtocolVersion.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBoxHTTPProtocolVersion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxHTTPProtocolVersionActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 5, 5);
        jPanelSendMain.add(jComboBoxHTTPProtocolVersion, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelSend.add(jPanelSendMain, gridBagConstraints);

        jTabbedPane.addTab(this.rb.getResourceString( "tab.send"), jPanelSend);

        jPanelMDN.setLayout(new java.awt.GridBagLayout());

        jPanelMDNMain.setLayout(new java.awt.GridBagLayout());

        jLabelMDNURL.setText(this.rb.getResourceString( "label.mdnurl"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelMDNMain.add(jLabelMDNURL, gridBagConstraints);

        jTextFieldMDNURL.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextFieldMDNURLKeyReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelMDNMain.add(jTextFieldMDNURL, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanelMDNMain.add(jPanelSpace99, gridBagConstraints);

        buttonGroupSyncAsyncMDN.add(jRadioButtonSyncMDN);
        jRadioButtonSyncMDN.setSelected(true);
        jRadioButtonSyncMDN.setText(this.rb.getResourceString( "label.syncmdn"));
        jRadioButtonSyncMDN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonSyncMDNActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelMDNMain.add(jRadioButtonSyncMDN, gridBagConstraints);

        buttonGroupSyncAsyncMDN.add(jRadioButtonAsyncMDN);
        jRadioButtonAsyncMDN.setText(this.rb.getResourceString( "label.asyncmdn"));
        jRadioButtonAsyncMDN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonAsyncMDNActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelMDNMain.add(jRadioButtonAsyncMDN, gridBagConstraints);

        jCheckBoxSignedMDN.setText(this.rb.getResourceString( "label.signedmdn"));
        jCheckBoxSignedMDN.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jCheckBoxSignedMDN.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jCheckBoxSignedMDN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxSignedMDNActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(15, 9, 5, 5);
        jPanelMDNMain.add(jCheckBoxSignedMDN, gridBagConstraints);

        jLabelMDNURLHint.setFont(new java.awt.Font("Tahoma", 2, 11));
        jLabelMDNURLHint.setText(this.rb.getResourceString( "label.url.hint"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 10, 5);
        jPanelMDNMain.add(jLabelMDNURLHint, gridBagConstraints);

        jLabelIconSyncMDN.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/mendelson/comm/as2/partner/gui/mdn_sync.jpg"))); // NOI18N
        jLabelIconSyncMDN.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelMDNMain.add(jLabelIconSyncMDN, gridBagConstraints);

        jLabelIconAsyncMDN.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/mendelson/comm/as2/partner/gui/mdn_async.jpg"))); // NOI18N
        jLabelIconAsyncMDN.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelMDNMain.add(jLabelIconAsyncMDN, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelMDN.add(jPanelMDNMain, gridBagConstraints);

        jTabbedPane.addTab(this.rb.getResourceString( "tab.mdn"), jPanelMDN);

        jPanelDirPoll.setLayout(new java.awt.GridBagLayout());

        jPanelPollOptions.setLayout(new java.awt.GridBagLayout());

        jLabelPollDir.setText(this.rb.getResourceString( "label.polldir"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 5, 5);
        jPanelPollOptions.add(jLabelPollDir, gridBagConstraints);

        jTextFieldPollDir.setEditable(false);
        jTextFieldPollDir.setBorder(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 5, 10);
        jPanelPollOptions.add(jTextFieldPollDir, gridBagConstraints);

        jLabelPollInterval.setText(this.rb.getResourceString( "label.pollinterval"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 5);
        jPanelPollOptions.add(jLabelPollInterval, gridBagConstraints);

        jTextFieldPollInterval.setColumns(5);
        jTextFieldPollInterval.setMinimumSize(new java.awt.Dimension(70, 20));
        jTextFieldPollInterval.setPreferredSize(new java.awt.Dimension(70, 20));
        jTextFieldPollInterval.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextFieldPollIntervalKeyReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 0);
        jPanelPollOptions.add(jTextFieldPollInterval, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.weighty = 1.0;
        jPanelPollOptions.add(jPanelSpaceX, gridBagConstraints);

        jLabelSeconds.setText("s");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 10);
        jPanelPollOptions.add(jLabelSeconds, gridBagConstraints);

        jLabelIgnorePollFilterList.setText(this.rb.getResourceString( "label.pollignore"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 5);
        jPanelPollOptions.add(jLabelIgnorePollFilterList, gridBagConstraints);

        jTextFieldIgnorePollFilterList.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextFieldIgnorePollFilterListKeyReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 10);
        jPanelPollOptions.add(jTextFieldIgnorePollFilterList, gridBagConstraints);

        jLabelPollMaxFiles.setText(this.rb.getResourceString( "label.maxpollfiles"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 5);
        jPanelPollOptions.add(jLabelPollMaxFiles, gridBagConstraints);

        jTextFieldPollMaxFiles.setColumns(5);
        jTextFieldPollMaxFiles.setMinimumSize(new java.awt.Dimension(70, 20));
        jTextFieldPollMaxFiles.setPreferredSize(new java.awt.Dimension(70, 20));
        jTextFieldPollMaxFiles.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextFieldPollMaxFilesKeyReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 0);
        jPanelPollOptions.add(jTextFieldPollMaxFiles, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanelDirPoll.add(jPanelPollOptions, gridBagConstraints);

        jTabbedPane.addTab(this.rb.getResourceString( "tab.dirpoll"), jPanelDirPoll);

        jPanelReceipt.setLayout(new java.awt.GridBagLayout());

        jPanelReceiptOptions.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanelReceiptOptions.add(jPanelSpace456, gridBagConstraints);

        jCheckBoxKeepFilenameOnReceipt.setText(this.rb.getResourceString( "label.keepfilenameonreceipt"));
        jCheckBoxKeepFilenameOnReceipt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxKeepFilenameOnReceiptActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 5, 10);
        jPanelReceiptOptions.add(jCheckBoxKeepFilenameOnReceipt, gridBagConstraints);

        jLabelHintKeepFilenameOnReceipt.setFont(new java.awt.Font("Tahoma", 2, 11));
        jLabelHintKeepFilenameOnReceipt.setText(this.rb.getResourceString( "hint.keepfilenameonreceipt"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 10);
        jPanelReceiptOptions.add(jLabelHintKeepFilenameOnReceipt, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanelReceipt.add(jPanelReceiptOptions, gridBagConstraints);

        jTabbedPane.addTab(this.rb.getResourceString( "tab.receipt"), jPanelReceipt);

        jPanelHTTPAuth.setLayout(new java.awt.GridBagLayout());

        jPanelHttpAuthData.setLayout(new java.awt.GridBagLayout());

        jCheckBoxHttpAuth.setText(this.rb.getResourceString( "label.usehttpauth" ));
        jCheckBoxHttpAuth.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jCheckBoxHttpAuth.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jCheckBoxHttpAuth.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxHttpAuthActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelHttpAuthData.add(jCheckBoxHttpAuth, gridBagConstraints);

        jLabelHttpAuth.setText(this.rb.getResourceString( "label.usehttpauth.user" ));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelHttpAuthData.add(jLabelHttpAuth, gridBagConstraints);

        jTextFieldHttpAuthUser.setColumns(30);
        jTextFieldHttpAuthUser.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextFieldHttpAuthUserKeyReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelHttpAuthData.add(jTextFieldHttpAuthUser, gridBagConstraints);

        jLabelHttpPass.setText(this.rb.getResourceString( "label.usehttpauth.pass" ));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelHttpAuthData.add(jLabelHttpPass, gridBagConstraints);

        jPasswordFieldHttpPass.setColumns(30);
        jPasswordFieldHttpPass.setText("jPasswordField1");
        jPasswordFieldHttpPass.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jPasswordFieldHttpPassKeyReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelHttpAuthData.add(jPasswordFieldHttpPass, gridBagConstraints);

        jCheckBoxHttpAuthAsyncMDN.setText(this.rb.getResourceString( "label.usehttpauth.asyncmdn" ));
        jCheckBoxHttpAuthAsyncMDN.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jCheckBoxHttpAuthAsyncMDN.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jCheckBoxHttpAuthAsyncMDN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxHttpAuthAsyncMDNActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(20, 5, 5, 5);
        jPanelHttpAuthData.add(jCheckBoxHttpAuthAsyncMDN, gridBagConstraints);

        jLabelHttpAuthAsyncMDN.setText(this.rb.getResourceString( "label.usehttpauth.asyncmdn.user" ));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelHttpAuthData.add(jLabelHttpAuthAsyncMDN, gridBagConstraints);

        jTextFieldHttpAuthAsyncMDNUser.setColumns(30);
        jTextFieldHttpAuthAsyncMDNUser.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextFieldHttpAuthAsyncMDNUserKeyReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelHttpAuthData.add(jTextFieldHttpAuthAsyncMDNUser, gridBagConstraints);

        jLabelHttpPassAsyncMDN.setText(this.rb.getResourceString( "label.usehttpauth.asyncmdn.pass" ));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelHttpAuthData.add(jLabelHttpPassAsyncMDN, gridBagConstraints);

        jPasswordFieldHttpPassAsyncMDN.setColumns(30);
        jPasswordFieldHttpPassAsyncMDN.setText("jPasswordField1");
        jPasswordFieldHttpPassAsyncMDN.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jPasswordFieldHttpPassAsyncMDNKeyReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelHttpAuthData.add(jPasswordFieldHttpPassAsyncMDN, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.weighty = 1.0;
        jPanelHttpAuthData.add(jPanelSpace199, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelHTTPAuth.add(jPanelHttpAuthData, gridBagConstraints);

        jTabbedPane.addTab(this.rb.getResourceString( "tab.httpauth"), jPanelHTTPAuth);

        jPanelHTTPHeader.setLayout(new java.awt.GridBagLayout());

        jTableHttpHeader.setModel(new TableModelHttpHeader());
        jScrollPaneHttpHeader.setViewportView(jTableHttpHeader);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridheight = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelHTTPHeader.add(jScrollPaneHttpHeader, gridBagConstraints);

        jButtonHttpHeaderAdd.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/mendelson/comm/as2/partner/gui/add_16x16.gif"))); // NOI18N
        jButtonHttpHeaderAdd.setText(this.rb.getResourceString( "httpheader.add"));
        jButtonHttpHeaderAdd.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jButtonHttpHeaderAdd.setMargin(new java.awt.Insets(2, 5, 2, 5));
        jButtonHttpHeaderAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonHttpHeaderAddActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelHTTPHeader.add(jButtonHttpHeaderAdd, gridBagConstraints);

        jButtonHttpHeaderRemove.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/mendelson/comm/as2/partner/gui/delete_16x16.gif"))); // NOI18N
        jButtonHttpHeaderRemove.setText(this.rb.getResourceString( "httpheader.delete"));
        jButtonHttpHeaderRemove.setEnabled(false);
        jButtonHttpHeaderRemove.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jButtonHttpHeaderRemove.setMargin(new java.awt.Insets(2, 5, 2, 5));
        jButtonHttpHeaderRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonHttpHeaderRemoveActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelHTTPHeader.add(jButtonHttpHeaderRemove, gridBagConstraints);

        jTabbedPane.addTab(this.rb.getResourceString( "tab.httpheader"), jPanelHTTPHeader);

        jPanelNotification.setLayout(new java.awt.GridBagLayout());

        jPanelNotificationMain.setLayout(new java.awt.GridBagLayout());

        jCheckBoxNotifySend.setText(this.rb.getResourceString("label.notify.send"));
        jCheckBoxNotifySend.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxNotifySendActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelNotificationMain.add(jCheckBoxNotifySend, gridBagConstraints);

        jCheckBoxNotifyReceive.setText(this.rb.getResourceString("label.notify.receive"));
        jCheckBoxNotifyReceive.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxNotifyReceiveActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelNotificationMain.add(jCheckBoxNotifyReceive, gridBagConstraints);

        jCheckBoxNotifySendReceive.setText(this.rb.getResourceString("label.notify.sendreceive"));
        jCheckBoxNotifySendReceive.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxNotifySendReceiveActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelNotificationMain.add(jCheckBoxNotifySendReceive, gridBagConstraints);

        jTextFieldNotifyReceive.setText("0");
        jTextFieldNotifyReceive.setPreferredSize(new java.awt.Dimension(50, 20));
        jTextFieldNotifyReceive.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextFieldNotifyReceiveKeyReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelNotificationMain.add(jTextFieldNotifyReceive, gridBagConstraints);

        jTextFieldNotifySend.setText("0");
        jTextFieldNotifySend.setPreferredSize(new java.awt.Dimension(50, 20));
        jTextFieldNotifySend.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextFieldNotifySendKeyReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelNotificationMain.add(jTextFieldNotifySend, gridBagConstraints);

        jTextFieldNotifySendReceive.setText("0");
        jTextFieldNotifySendReceive.setPreferredSize(new java.awt.Dimension(50, 20));
        jTextFieldNotifySendReceive.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextFieldNotifySendReceiveKeyReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelNotificationMain.add(jTextFieldNotifySendReceive, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanelNotificationMain.add(jPanelSpace23, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelNotification.add(jPanelNotificationMain, gridBagConstraints);

        jTabbedPane.addTab(this.rb.getResourceString( "tab.notification"), jPanelNotification);

        jPanelEvents.setLayout(new java.awt.GridBagLayout());

        jPanelEventsMain.setLayout(new java.awt.GridBagLayout());

        jCheckBoxUseCommandOnSendError.setText(this.rb.getResourceString( "label.usecommandonsenderror"));
        jCheckBoxUseCommandOnSendError.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jCheckBoxUseCommandOnSendError.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jCheckBoxUseCommandOnSendError.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxUseCommandOnSendErrorActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 5, 5);
        jPanelEventsMain.add(jCheckBoxUseCommandOnSendError, gridBagConstraints);

        jTextFieldCommandOnSendError.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextFieldCommandOnSendErrorKeyReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 5, 10);
        jPanelEventsMain.add(jTextFieldCommandOnSendError, gridBagConstraints);

        jLabelHintCommandOnSendError1.setFont(new java.awt.Font("Tahoma", 2, 11));
        jLabelHintCommandOnSendError1.setText(this.rb.getResourceString( "hint.replacement.send1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 10);
        jPanelEventsMain.add(jLabelHintCommandOnSendError1, gridBagConstraints);

        jCheckBoxUseCommandOnSendSuccess.setText(this.rb.getResourceString( "label.usecommandonsendsuccess"));
        jCheckBoxUseCommandOnSendSuccess.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jCheckBoxUseCommandOnSendSuccess.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jCheckBoxUseCommandOnSendSuccess.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxUseCommandOnSendSuccessActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 5, 5);
        jPanelEventsMain.add(jCheckBoxUseCommandOnSendSuccess, gridBagConstraints);

        jTextFieldCommandOnSendSuccess.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextFieldCommandOnSendSuccessKeyReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 5, 10);
        jPanelEventsMain.add(jTextFieldCommandOnSendSuccess, gridBagConstraints);

        jLabelHintCommandOnSendSuccess1.setFont(new java.awt.Font("Tahoma", 2, 11));
        jLabelHintCommandOnSendSuccess1.setText(this.rb.getResourceString( "hint.replacement.send1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 10);
        jPanelEventsMain.add(jLabelHintCommandOnSendSuccess1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 10, 0);
        jPanelEventsMain.add(jSeparator2, gridBagConstraints);

        jCheckBoxUseCommandOnReceipt.setText(this.rb.getResourceString( "label.usecommandonreceipt"));
        jCheckBoxUseCommandOnReceipt.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jCheckBoxUseCommandOnReceipt.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jCheckBoxUseCommandOnReceipt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxUseCommandOnReceiptActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 5, 5);
        jPanelEventsMain.add(jCheckBoxUseCommandOnReceipt, gridBagConstraints);

        jTextFieldCommandOnReceipt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextFieldCommandOnReceiptKeyReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 5, 10);
        jPanelEventsMain.add(jTextFieldCommandOnReceipt, gridBagConstraints);

        jLabelHintCommandOnReceipt1.setFont(new java.awt.Font("Tahoma", 2, 11));
        jLabelHintCommandOnReceipt1.setText(this.rb.getResourceString( "hint.filenamereplacement.receipt1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 10);
        jPanelEventsMain.add(jLabelHintCommandOnReceipt1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.weighty = 1.0;
        jPanelEventsMain.add(jPanelSpace, gridBagConstraints);

        jLabelHintCommandOnReceipt2.setFont(new java.awt.Font("Tahoma", 2, 11));
        jLabelHintCommandOnReceipt2.setText(this.rb.getResourceString( "hint.filenamereplacement.receipt2"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 10);
        jPanelEventsMain.add(jLabelHintCommandOnReceipt2, gridBagConstraints);

        jLabelHintCommandOnSendError2.setFont(new java.awt.Font("Tahoma", 2, 11));
        jLabelHintCommandOnSendError2.setText(this.rb.getResourceString( "hint.replacement.send2"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 10);
        jPanelEventsMain.add(jLabelHintCommandOnSendError2, gridBagConstraints);

        jLabelHintCommandOnSendSuccess2.setFont(new java.awt.Font("Tahoma", 2, 11));
        jLabelHintCommandOnSendSuccess2.setText(this.rb.getResourceString( "hint.replacement.send2"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 10);
        jPanelEventsMain.add(jLabelHintCommandOnSendSuccess2, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        jPanelEvents.add(jPanelEventsMain, gridBagConstraints);

        jTabbedPane.addTab(this.rb.getResourceString( "tab.events"), jPanelEvents);

        jPanelPartnerSystem.setLayout(new java.awt.GridBagLayout());

        jPanelPartnerSystemMain.setLayout(new java.awt.GridBagLayout());

        jLabelAS2Version.setText(this.rb.getResourceString( "label.as2version"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelPartnerSystemMain.add(jLabelAS2Version, gridBagConstraints);

        jLabelProductName.setText(this.rb.getResourceString( "label.productname"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelPartnerSystemMain.add(jLabelProductName, gridBagConstraints);

        jLabelFeatures.setText(this.rb.getResourceString( "label.features"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(20, 5, 10, 5);
        jPanelPartnerSystemMain.add(jLabelFeatures, gridBagConstraints);

        jCheckBoxEdiintFeaturesCompression.setText(this.rb.getResourceString( "label.features.compression"));
        jCheckBoxEdiintFeaturesCompression.setEnabled(false);
        jCheckBoxEdiintFeaturesCompression.setFocusable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelPartnerSystemMain.add(jCheckBoxEdiintFeaturesCompression, gridBagConstraints);

        jCheckBoxEdiintFeaturesMA.setText(this.rb.getResourceString( "label.features.ma"));
        jCheckBoxEdiintFeaturesMA.setEnabled(false);
        jCheckBoxEdiintFeaturesMA.setFocusable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelPartnerSystemMain.add(jCheckBoxEdiintFeaturesMA, gridBagConstraints);

        jCheckBoxEdiintFeaturesCEM.setText(this.rb.getResourceString( "label.features.cem"));
        jCheckBoxEdiintFeaturesCEM.setEnabled(false);
        jCheckBoxEdiintFeaturesCEM.setFocusable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelPartnerSystemMain.add(jCheckBoxEdiintFeaturesCEM, gridBagConstraints);

        jTextFieldAS2Version.setBackground(java.awt.SystemColor.control);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelPartnerSystemMain.add(jTextFieldAS2Version, gridBagConstraints);

        jTextFieldProductName.setBackground(java.awt.SystemColor.control);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelPartnerSystemMain.add(jTextFieldProductName, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanelPartnerSystemMain.add(jPanelSpaceSpace, gridBagConstraints);

        jScrollPaneTextAreaPartnerSystemInformation.setBorder(null);

        jTextAreaPartnerSystemInformation.setBackground(java.awt.SystemColor.control);
        jTextAreaPartnerSystemInformation.setColumns(20);
        jTextAreaPartnerSystemInformation.setEditable(false);
        jTextAreaPartnerSystemInformation.setFont(new java.awt.Font("Dialog", 0, 13));
        jTextAreaPartnerSystemInformation.setLineWrap(true);
        jTextAreaPartnerSystemInformation.setRows(5);
        jTextAreaPartnerSystemInformation.setWrapStyleWord(true);
        jTextAreaPartnerSystemInformation.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
        jTextAreaPartnerSystemInformation.setFocusable(false);
        jScrollPaneTextAreaPartnerSystemInformation.setViewportView(jTextAreaPartnerSystemInformation);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        jPanelPartnerSystemMain.add(jScrollPaneTextAreaPartnerSystemInformation, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelPartnerSystem.add(jPanelPartnerSystemMain, gridBagConstraints);

        jTabbedPane.addTab(this.rb.getResourceString( "tab.partnersystem"), jPanelPartnerSystem);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jTabbedPane, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    private void jComboBoxCryptCertActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxCryptCertActionPerformed
        if (this.partner != null && this.jComboBoxCryptCert.getSelectedItem() != null) {
            KeystoreCertificate certificate = (KeystoreCertificate) this.jComboBoxCryptCert.getSelectedItem();
            PartnerCertificateInformation cryptInfo = new PartnerCertificateInformation(
                    certificate.getFingerPrintSHA1(),
                    PartnerCertificateInformation.CATEGORY_CRYPT);
            cryptInfo.setPriority(1);
            partner.setCertificateInformation(cryptInfo);
            this.informTreeModelNodeChanged();
        }
        this.setButtonState();
    }//GEN-LAST:event_jComboBoxCryptCertActionPerformed

    private void jPasswordFieldHttpPassAsyncMDNKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jPasswordFieldHttpPassAsyncMDNKeyReleased
        if (this.jCheckBoxHttpAuthAsyncMDN.isSelected()) {
            if (this.partner != null && this.partner.getAuthenticationAsyncMDN() != null) {
                this.partner.getAuthenticationAsyncMDN().setPassword(new String(this.jPasswordFieldHttpPassAsyncMDN.getPassword()));
                this.informTreeModelNodeChanged();
            }
        }
    }//GEN-LAST:event_jPasswordFieldHttpPassAsyncMDNKeyReleased

    private void jTextFieldHttpAuthAsyncMDNUserKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldHttpAuthAsyncMDNUserKeyReleased
        if (this.jCheckBoxHttpAuthAsyncMDN.isSelected()) {
            if (this.partner != null && this.partner.getAuthenticationAsyncMDN() != null) {
                this.partner.getAuthenticationAsyncMDN().setUser(this.jTextFieldHttpAuthAsyncMDNUser.getText());
                this.informTreeModelNodeChanged();
            }
        }
    }//GEN-LAST:event_jTextFieldHttpAuthAsyncMDNUserKeyReleased

    private void jPasswordFieldHttpPassKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jPasswordFieldHttpPassKeyReleased
        if (this.jCheckBoxHttpAuth.isSelected()) {
            if (this.partner != null && this.partner.getAuthentication() != null) {
                this.partner.getAuthentication().setPassword(new String(this.jPasswordFieldHttpPass.getPassword()));
                this.informTreeModelNodeChanged();
            }
        }
    }//GEN-LAST:event_jPasswordFieldHttpPassKeyReleased

    private void jTextFieldHttpAuthUserKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldHttpAuthUserKeyReleased
        if (this.jCheckBoxHttpAuth.isSelected()) {
            if (this.partner != null && this.partner.getAuthentication() != null) {
                this.partner.getAuthentication().setUser(this.jTextFieldHttpAuthUser.getText());
                this.informTreeModelNodeChanged();
            }
        }
    }//GEN-LAST:event_jTextFieldHttpAuthUserKeyReleased

    private void jCheckBoxHttpAuthAsyncMDNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxHttpAuthAsyncMDNActionPerformed
        if (this.partner != null) {
            this.partner.getAuthenticationAsyncMDN().setEnabled(this.jCheckBoxHttpAuthAsyncMDN.isSelected());
            this.informTreeModelNodeChanged();
        }
        this.updateHttpAuthState();
    }//GEN-LAST:event_jCheckBoxHttpAuthAsyncMDNActionPerformed

    private void jCheckBoxHttpAuthActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxHttpAuthActionPerformed
        if (this.partner != null) {
            this.partner.getAuthentication().setEnabled(this.jCheckBoxHttpAuth.isSelected());
            this.informTreeModelNodeChanged();
        }
        this.updateHttpAuthState();
    }//GEN-LAST:event_jCheckBoxHttpAuthActionPerformed

    private void jTextFieldCommandOnReceiptKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldCommandOnReceiptKeyReleased
        if (this.partner != null) {
            this.partner.setCommandOnReceipt(this.jTextFieldCommandOnReceipt.getText());
            this.informTreeModelNodeChanged();
        }
    }//GEN-LAST:event_jTextFieldCommandOnReceiptKeyReleased

    private void jCheckBoxUseCommandOnReceiptActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxUseCommandOnReceiptActionPerformed
        this.jTextFieldCommandOnReceipt.setEditable(this.jCheckBoxUseCommandOnReceipt.isSelected());
        this.jTextFieldCommandOnReceipt.setEnabled(this.jCheckBoxUseCommandOnReceipt.isSelected());
        if (this.partner != null) {
            this.partner.setUseCommandOnReceipt(this.jCheckBoxUseCommandOnReceipt.isSelected());
            this.partner.setCommandOnReceipt(this.jTextFieldCommandOnReceipt.getText());
            this.informTreeModelNodeChanged();
        }
    }//GEN-LAST:event_jCheckBoxUseCommandOnReceiptActionPerformed

    private void jCheckBoxSignedMDNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxSignedMDNActionPerformed
        if (this.partner != null) {
            this.partner.setSignedMDN(this.jCheckBoxSignedMDN.isSelected());
            this.informTreeModelNodeChanged();
        }
    }//GEN-LAST:event_jCheckBoxSignedMDNActionPerformed

    private void jCheckBoxCompressActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxCompressActionPerformed
        if (this.partner != null) {
            this.partner.setCompressionType(this.jCheckBoxCompress.isSelected() ? AS2Message.COMPRESSION_ZLIB : AS2Message.COMPRESSION_NONE);
            this.informTreeModelNodeChanged();
        }
    }//GEN-LAST:event_jCheckBoxCompressActionPerformed

    private void jTextFieldIgnorePollFilterListKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldIgnorePollFilterListKeyReleased
        if (this.partner != null) {
            this.partner.setPollIgnoreListString(this.jTextFieldIgnorePollFilterList.getText());
            this.informTreeModelNodeChanged();
        }
    }//GEN-LAST:event_jTextFieldIgnorePollFilterListKeyReleased

    private void jTextFieldPollIntervalKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldPollIntervalKeyReleased
        if (this.partner != null) {
            try {
                int pollInterval = Integer.valueOf(this.jTextFieldPollInterval.getText().trim()).intValue();
                this.partner.setPollInterval(pollInterval);
                this.informTreeModelNodeChanged();
            } catch (Exception e) {
                //nop
            }
        }
    }//GEN-LAST:event_jTextFieldPollIntervalKeyReleased

    private void jRadioButtonSyncMDNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonSyncMDNActionPerformed
        if (this.partner != null) {
            this.partner.setSyncMDN(true);
            this.jLabelIconSyncMDN.setEnabled(true);
            this.jLabelIconAsyncMDN.setEnabled(false);
            this.informTreeModelNodeChanged();
        }
    }//GEN-LAST:event_jRadioButtonSyncMDNActionPerformed

    private void jRadioButtonAsyncMDNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonAsyncMDNActionPerformed
        if (this.partner != null) {
            this.partner.setSyncMDN(false);
            this.jLabelIconSyncMDN.setEnabled(false);
            this.jLabelIconAsyncMDN.setEnabled(true);
            this.informTreeModelNodeChanged();
        }
    }//GEN-LAST:event_jRadioButtonAsyncMDNActionPerformed

    private void jTextFieldContentTypeKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldContentTypeKeyReleased
        if (this.partner != null) {
            if (this.jTextFieldContentType.getText().trim().length() == 0) {
                this.partner.setContentType("application/EDI-Consent");
            } else {
                this.partner.setContentType(this.jTextFieldContentType.getText());
            }
            this.informTreeModelNodeChanged();
        }
    }//GEN-LAST:event_jTextFieldContentTypeKeyReleased

    private void jTextFieldSubjectKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldSubjectKeyReleased
        if (this.partner != null) {
            if (this.jTextFieldSubject.getText().trim().length() == 0) {
                this.partner.setSubject("AS2 message");
            } else {
                this.partner.setSubject(this.jTextFieldSubject.getText());
            }
            this.informTreeModelNodeChanged();
        }
    }//GEN-LAST:event_jTextFieldSubjectKeyReleased

    private void jTextFieldEMailKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldEMailKeyReleased
        if (this.partner != null) {
            if (this.jTextFieldEMail.getText().trim().length() == 0) {
                this.partner.setEmail("sender@as2server.com");
            } else {
                this.partner.setEmail(this.jTextFieldEMail.getText());
            }
            this.informTreeModelNodeChanged();
        }
    }//GEN-LAST:event_jTextFieldEMailKeyReleased

    private void jTextFieldMDNURLKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldMDNURLKeyReleased
        if (this.partner != null) {
            this.setOkButton(this.partner, this.jTextFieldURL.getText(), this.jTextFieldMDNURL.getText(),
                    this.jTextFieldName.getText(), this.jTextFieldId.getText());
            if (this.jTextFieldMDNURL.getText().trim().length() == 0) {
                this.partner.setMdnURL(this.partner.getDefaultURL());
            } else {
                this.partner.setMdnURL(this.jTextFieldMDNURL.getText());
            }
            this.informTreeModelNodeChanged();
        }
    }//GEN-LAST:event_jTextFieldMDNURLKeyReleased

    private void jComboBoxEncryptionTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxEncryptionTypeActionPerformed
        if (this.partner != null) {
            if (this.jComboBoxEncryptionType.getSelectedItem() != null) {
                String item = (String) this.jComboBoxEncryptionType.getSelectedItem();
                if (item.equals(this.rbMessage.getResourceString("encryption." + AS2Message.ENCRYPTION_NONE))) {
                    this.partner.setEncryptionType(AS2Message.ENCRYPTION_NONE);
                } else if (item.equals(this.rbMessage.getResourceString("encryption." + AS2Message.ENCRYPTION_3DES))) {
                    this.partner.setEncryptionType(AS2Message.ENCRYPTION_3DES);
                } else if (item.equals(this.rbMessage.getResourceString("encryption." + AS2Message.ENCRYPTION_RC2_40))) {
                    this.partner.setEncryptionType(AS2Message.ENCRYPTION_RC2_40);
                } else if (item.equals(this.rbMessage.getResourceString("encryption." + AS2Message.ENCRYPTION_RC2_64))) {
                    this.partner.setEncryptionType(AS2Message.ENCRYPTION_RC2_64);
                } else if (item.equals(this.rbMessage.getResourceString("encryption." + AS2Message.ENCRYPTION_RC2_128))) {
                    this.partner.setEncryptionType(AS2Message.ENCRYPTION_RC2_128);
                } else if (item.equals(this.rbMessage.getResourceString("encryption." + AS2Message.ENCRYPTION_RC2_196))) {
                    this.partner.setEncryptionType(AS2Message.ENCRYPTION_RC2_196);
                } else if (item.equals(this.rbMessage.getResourceString("encryption." + AS2Message.ENCRYPTION_AES_128))) {
                    this.partner.setEncryptionType(AS2Message.ENCRYPTION_AES_128);
                } else if (item.equals(this.rbMessage.getResourceString("encryption." + AS2Message.ENCRYPTION_AES_192))) {
                    this.partner.setEncryptionType(AS2Message.ENCRYPTION_AES_192);
                } else if (item.equals(this.rbMessage.getResourceString("encryption." + AS2Message.ENCRYPTION_AES_256))) {
                    this.partner.setEncryptionType(AS2Message.ENCRYPTION_AES_256);
                } else if (item.equals(this.rbMessage.getResourceString("encryption." + AS2Message.ENCRYPTION_RC4_40))) {
                    this.partner.setEncryptionType(AS2Message.ENCRYPTION_RC4_40);
                } else if (item.equals(this.rbMessage.getResourceString("encryption." + AS2Message.ENCRYPTION_RC4_56))) {
                    this.partner.setEncryptionType(AS2Message.ENCRYPTION_RC4_56);
                } else if (item.equals(this.rbMessage.getResourceString("encryption." + AS2Message.ENCRYPTION_RC4_128))) {
                    this.partner.setEncryptionType(AS2Message.ENCRYPTION_RC4_128);
                } else if (item.equals(this.rbMessage.getResourceString("encryption." + AS2Message.ENCRYPTION_DES))) {
                    this.partner.setEncryptionType(AS2Message.ENCRYPTION_DES);
                }
                this.informTreeModelNodeChanged();
            }
        }
    }//GEN-LAST:event_jComboBoxEncryptionTypeActionPerformed

    private void jComboBoxSignCertActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxSignCertActionPerformed
        if (this.partner != null && this.jComboBoxSignCert.getSelectedItem() != null) {
            KeystoreCertificate certificate = (KeystoreCertificate) this.jComboBoxSignCert.getSelectedItem();
            PartnerCertificateInformation signInfo = new PartnerCertificateInformation(
                    certificate.getFingerPrintSHA1(),
                    PartnerCertificateInformation.CATEGORY_SIGN);
            signInfo.setPriority(1);
            partner.setCertificateInformation(signInfo);
            this.informTreeModelNodeChanged();
        }
        this.setButtonState();
    }//GEN-LAST:event_jComboBoxSignCertActionPerformed

    private void jComboBoxSignTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxSignTypeActionPerformed
        if (this.partner != null && this.jComboBoxSignType.getSelectedItem() != null) {
            String item = (String) this.jComboBoxSignType.getSelectedItem();
            if (item.equals(this.rbMessage.getResourceString("signature." + AS2Message.SIGNATURE_NONE))) {
                this.partner.setSignType(AS2Message.SIGNATURE_NONE);
            } else if (item.equals(this.rbMessage.getResourceString("signature." + AS2Message.SIGNATURE_SHA1))) {
                this.partner.setSignType(AS2Message.SIGNATURE_SHA1);
            } else if (item.equals(this.rbMessage.getResourceString("signature." + AS2Message.SIGNATURE_MD5))) {
                this.partner.setSignType(AS2Message.SIGNATURE_MD5);
            }
            this.informTreeModelNodeChanged();
        }
        this.setButtonState();
    }//GEN-LAST:event_jComboBoxSignTypeActionPerformed

    private void jTextFieldURLKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldURLKeyReleased
        if (this.partner != null) {
            this.setOkButton(this.partner, this.jTextFieldURL.getText(), this.jTextFieldMDNURL.getText(),
                    this.jTextFieldName.getText(), this.jTextFieldId.getText());
            this.partner.setURL(this.jTextFieldURL.getText());
            this.informTreeModelNodeChanged();
        }
    }//GEN-LAST:event_jTextFieldURLKeyReleased

    private void jTextFieldIdKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldIdKeyReleased
        if (this.partner != null) {
            this.setOkButton(this.partner, this.jTextFieldURL.getText(), this.jTextFieldMDNURL.getText(),
                    this.jTextFieldName.getText(), this.jTextFieldId.getText());
            this.partner.setAS2Identification(this.jTextFieldId.getText());
            this.informTreeModelNodeChanged();
        }
    }//GEN-LAST:event_jTextFieldIdKeyReleased

    private void jTextFieldNameKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldNameKeyReleased
        if (this.partner != null) {
            this.setOkButton(this.partner, this.jTextFieldURL.getText(), this.jTextFieldMDNURL.getText(),
                    this.jTextFieldName.getText(), this.jTextFieldId.getText());
            this.partner.setName(this.jTextFieldName.getText());
            this.updatePollDirDisplay();
            this.informTreeModelNodeChanged();
        }
    }//GEN-LAST:event_jTextFieldNameKeyReleased

    private void jCheckBoxLocalStationItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCheckBoxLocalStationItemStateChanged
        if (this.partner != null) {
            this.partner.setLocalStation(this.jCheckBoxLocalStation.isSelected());
            if (this.partner.isLocalStation()) {
                this.tree.setToLocalStation(this.partner);
            }
            this.informTreeModelNodeChanged();
            this.setPanelVisiblilityState();
            this.handleVisibilityStateOfWidgets();
            this.updatePollDirDisplay();
        }
        this.setButtonState();
    }//GEN-LAST:event_jCheckBoxLocalStationItemStateChanged

    private void jCheckBoxKeepFilenameOnReceiptActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxKeepFilenameOnReceiptActionPerformed
        if (this.partner != null) {
            this.partner.setKeepOriginalFilenameOnReceipt(this.jCheckBoxKeepFilenameOnReceipt.isSelected());
            this.informTreeModelNodeChanged();
        }
        this.setButtonState();
    }//GEN-LAST:event_jCheckBoxKeepFilenameOnReceiptActionPerformed

private void jTextFieldNotifySendKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldNotifySendKeyReleased
    if (this.jCheckBoxNotifySend.isSelected()) {
        if (this.partner != null) {
            try {
                this.partner.setNotifySend(Integer.valueOf(this.jTextFieldNotifySend.getText()).intValue());
                this.informTreeModelNodeChanged();
            } catch (NumberFormatException e) {
                //nop
            }
        }
    }
}//GEN-LAST:event_jTextFieldNotifySendKeyReleased

private void jTextFieldNotifyReceiveKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldNotifyReceiveKeyReleased
    if (this.jCheckBoxNotifyReceive.isSelected()) {
        if (this.partner != null) {
            try {
                this.partner.setNotifyReceive(Integer.valueOf(this.jTextFieldNotifyReceive.getText()).intValue());
                this.informTreeModelNodeChanged();
            } catch (NumberFormatException e) {
                //nop
            }
        }
    }
}//GEN-LAST:event_jTextFieldNotifyReceiveKeyReleased

private void jTextFieldNotifySendReceiveKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldNotifySendReceiveKeyReleased
    if (this.jCheckBoxNotifySendReceive.isSelected()) {
        if (this.partner != null) {
            try {
                this.partner.setNotifySendReceive(Integer.valueOf(this.jTextFieldNotifySendReceive.getText()).intValue());
                this.informTreeModelNodeChanged();
            } catch (NumberFormatException e) {
                //nop
            }
        }
    }
}//GEN-LAST:event_jTextFieldNotifySendReceiveKeyReleased

private void jCheckBoxNotifySendActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxNotifySendActionPerformed
    if (this.partner != null) {
        this.partner.setNotifySendEnabled(this.jCheckBoxNotifySend.isSelected());
        this.informTreeModelNodeChanged();
        this.setButtonState();
    }
}//GEN-LAST:event_jCheckBoxNotifySendActionPerformed

private void jCheckBoxNotifyReceiveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxNotifyReceiveActionPerformed
    if (this.partner != null) {
        this.partner.setNotifyReceiveEnabled(this.jCheckBoxNotifyReceive.isSelected());
        this.informTreeModelNodeChanged();
        this.setButtonState();
    }
}//GEN-LAST:event_jCheckBoxNotifyReceiveActionPerformed

private void jCheckBoxNotifySendReceiveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxNotifySendReceiveActionPerformed
    if (this.partner != null) {
        this.partner.setNotifySendReceiveEnabled(this.jCheckBoxNotifySendReceive.isSelected());
        this.informTreeModelNodeChanged();
        this.setButtonState();
    }
}//GEN-LAST:event_jCheckBoxNotifySendReceiveActionPerformed

private void jCheckBoxUseCommandOnSendSuccessActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxUseCommandOnSendSuccessActionPerformed
    this.jTextFieldCommandOnSendSuccess.setEditable(this.jCheckBoxUseCommandOnSendSuccess.isSelected());
    this.jTextFieldCommandOnSendSuccess.setEnabled(this.jCheckBoxUseCommandOnSendSuccess.isSelected());
    if (this.partner != null) {
        this.partner.setUseCommandOnSendSuccess(this.jCheckBoxUseCommandOnSendSuccess.isSelected());
        this.partner.setCommandOnSendSuccess(this.jTextFieldCommandOnSendSuccess.getText());
        this.informTreeModelNodeChanged();
    }
}//GEN-LAST:event_jCheckBoxUseCommandOnSendSuccessActionPerformed

private void jTextFieldCommandOnSendSuccessKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldCommandOnSendSuccessKeyReleased
    if (this.partner != null) {
        this.partner.setCommandOnSendSuccess(this.jTextFieldCommandOnSendSuccess.getText());
        this.informTreeModelNodeChanged();
    }
}//GEN-LAST:event_jTextFieldCommandOnSendSuccessKeyReleased

private void jCheckBoxUseCommandOnSendErrorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxUseCommandOnSendErrorActionPerformed
    this.jTextFieldCommandOnSendError.setEditable(this.jCheckBoxUseCommandOnSendError.isSelected());
    this.jTextFieldCommandOnSendError.setEnabled(this.jCheckBoxUseCommandOnSendError.isSelected());
    if (this.partner != null) {
        this.partner.setUseCommandOnSendError(this.jCheckBoxUseCommandOnSendError.isSelected());
        this.partner.setCommandOnSendError(this.jTextFieldCommandOnSendError.getText());
        this.informTreeModelNodeChanged();
    }
}//GEN-LAST:event_jCheckBoxUseCommandOnSendErrorActionPerformed

private void jTextFieldCommandOnSendErrorKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldCommandOnSendErrorKeyReleased
    if (this.partner != null) {
        this.partner.setCommandOnSendError(this.jTextFieldCommandOnSendError.getText());
        this.informTreeModelNodeChanged();
    }
}//GEN-LAST:event_jTextFieldCommandOnSendErrorKeyReleased

private void jComboBoxContentTransferEncodingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxContentTransferEncodingActionPerformed
    if (this.partner != null) {
        int newTransferEncoding = -1;
        if (this.jComboBoxContentTransferEncoding.getSelectedItem().equals(STR_CONTENT_TRANSFER_ENCODING_BINARY)) {
            newTransferEncoding = AS2Message.CONTENT_TRANSFER_ENCODING_BINARY;
        } else {
            newTransferEncoding = AS2Message.CONTENT_TRANSFER_ENCODING_BASE64;
        }
        if (this.partner.getContentTransferEncoding() != newTransferEncoding) {
            this.partner.setContentTransferEncoding(newTransferEncoding);
            this.informTreeModelNodeChanged();
        }
    }
}//GEN-LAST:event_jComboBoxContentTransferEncodingActionPerformed

private void jButtonHttpHeaderAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonHttpHeaderAddActionPerformed
    PartnerHttpHeader header = new PartnerHttpHeader();
    header.setKey("");
    header.setValue("");
    ((TableModelHttpHeader) this.jTableHttpHeader.getModel()).addRow(header);

}//GEN-LAST:event_jButtonHttpHeaderAddActionPerformed

private void jButtonHttpHeaderRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonHttpHeaderRemoveActionPerformed
    int selectedRow = this.jTableHttpHeader.getSelectedRow();
    ((TableModelHttpHeader) this.jTableHttpHeader.getModel()).deleteRow(selectedRow);
    if (selectedRow > this.jTableHttpHeader.getRowCount() - 1) {
        selectedRow = this.jTableHttpHeader.getRowCount() - 1;
    }
    this.jTableHttpHeader.getSelectionModel().setSelectionInterval(selectedRow, selectedRow);
}//GEN-LAST:event_jButtonHttpHeaderRemoveActionPerformed

private void jComboBoxHTTPProtocolVersionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxHTTPProtocolVersionActionPerformed
    if (this.partner != null) {
        this.partner.setHttpProtocolVersion((String) this.jComboBoxHTTPProtocolVersion.getSelectedItem());
        this.informTreeModelNodeChanged();
    }
}//GEN-LAST:event_jComboBoxHTTPProtocolVersionActionPerformed

private void jTextFieldPollMaxFilesKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldPollMaxFilesKeyReleased
    if (this.partner != null) {
        try {
            int maxPollFiles = Integer.valueOf(this.jTextFieldPollMaxFiles.getText().trim()).intValue();
            this.partner.setMaxPollFiles(maxPollFiles);
            this.informTreeModelNodeChanged();
        } catch (Exception e) {
            //nop
        }
    }
}//GEN-LAST:event_jTextFieldPollMaxFilesKeyReleased
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroupSyncAsyncMDN;
    private javax.swing.JButton jButtonHttpHeaderAdd;
    private javax.swing.JButton jButtonHttpHeaderRemove;
    private javax.swing.JCheckBox jCheckBoxCompress;
    private javax.swing.JCheckBox jCheckBoxEdiintFeaturesCEM;
    private javax.swing.JCheckBox jCheckBoxEdiintFeaturesCompression;
    private javax.swing.JCheckBox jCheckBoxEdiintFeaturesMA;
    private javax.swing.JCheckBox jCheckBoxHttpAuth;
    private javax.swing.JCheckBox jCheckBoxHttpAuthAsyncMDN;
    private javax.swing.JCheckBox jCheckBoxKeepFilenameOnReceipt;
    private javax.swing.JCheckBox jCheckBoxLocalStation;
    private javax.swing.JCheckBox jCheckBoxNotifyReceive;
    private javax.swing.JCheckBox jCheckBoxNotifySend;
    private javax.swing.JCheckBox jCheckBoxNotifySendReceive;
    private javax.swing.JCheckBox jCheckBoxSignedMDN;
    private javax.swing.JCheckBox jCheckBoxUseCommandOnReceipt;
    private javax.swing.JCheckBox jCheckBoxUseCommandOnSendError;
    private javax.swing.JCheckBox jCheckBoxUseCommandOnSendSuccess;
    private javax.swing.JComboBox jComboBoxContentTransferEncoding;
    private javax.swing.JComboBox jComboBoxCryptCert;
    private javax.swing.JComboBox jComboBoxEncryptionType;
    private javax.swing.JComboBox jComboBoxHTTPProtocolVersion;
    private javax.swing.JComboBox jComboBoxSignCert;
    private javax.swing.JComboBox jComboBoxSignType;
    private javax.swing.JLabel jLabelAS2Version;
    private javax.swing.JLabel jLabelCertSignType;
    private javax.swing.JLabel jLabelContentTransferEncoding;
    private javax.swing.JLabel jLabelContentType;
    private javax.swing.JLabel jLabelCryptAlias;
    private javax.swing.JLabel jLabelEMail;
    private javax.swing.JLabel jLabelEncryptionType;
    private javax.swing.JLabel jLabelFeatures;
    private javax.swing.JLabel jLabelHTTPProtocolVersion;
    private javax.swing.JLabel jLabelHintCommandOnReceipt1;
    private javax.swing.JLabel jLabelHintCommandOnReceipt2;
    private javax.swing.JLabel jLabelHintCommandOnSendError1;
    private javax.swing.JLabel jLabelHintCommandOnSendError2;
    private javax.swing.JLabel jLabelHintCommandOnSendSuccess1;
    private javax.swing.JLabel jLabelHintCommandOnSendSuccess2;
    private javax.swing.JLabel jLabelHintKeepFilenameOnReceipt;
    private javax.swing.JLabel jLabelHttpAuth;
    private javax.swing.JLabel jLabelHttpAuthAsyncMDN;
    private javax.swing.JLabel jLabelHttpPass;
    private javax.swing.JLabel jLabelHttpPassAsyncMDN;
    private javax.swing.JLabel jLabelIconAsyncMDN;
    private javax.swing.JLabel jLabelIconSyncMDN;
    private javax.swing.JLabel jLabelId;
    private javax.swing.JLabel jLabelIgnorePollFilterList;
    private javax.swing.JLabel jLabelMDNURL;
    private javax.swing.JLabel jLabelMDNURLHint;
    private javax.swing.JLabel jLabelName;
    private javax.swing.JLabel jLabelPartnerComment;
    private javax.swing.JLabel jLabelPollDir;
    private javax.swing.JLabel jLabelPollInterval;
    private javax.swing.JLabel jLabelPollMaxFiles;
    private javax.swing.JLabel jLabelProductName;
    private javax.swing.JLabel jLabelSeconds;
    private javax.swing.JLabel jLabelSendUrlHint;
    private javax.swing.JLabel jLabelSignAlias;
    private javax.swing.JLabel jLabelSubject;
    private javax.swing.JLabel jLabelSubjectHint;
    private javax.swing.JLabel jLabelURL;
    private javax.swing.JPanel jPanelDirPoll;
    private javax.swing.JPanel jPanelEvents;
    private javax.swing.JPanel jPanelEventsMain;
    private javax.swing.JPanel jPanelHTTPAuth;
    private javax.swing.JPanel jPanelHTTPHeader;
    private javax.swing.JPanel jPanelHttpAuthData;
    private javax.swing.JPanel jPanelMDN;
    private javax.swing.JPanel jPanelMDNMain;
    private javax.swing.JPanel jPanelMisc;
    private javax.swing.JPanel jPanelNotification;
    private javax.swing.JPanel jPanelNotificationMain;
    private javax.swing.JPanel jPanelPartnerSystem;
    private javax.swing.JPanel jPanelPartnerSystemMain;
    private javax.swing.JPanel jPanelPollOptions;
    private javax.swing.JPanel jPanelReceipt;
    private javax.swing.JPanel jPanelReceiptOptions;
    private javax.swing.JPanel jPanelSecurity;
    private javax.swing.JPanel jPanelSend;
    private javax.swing.JPanel jPanelSendMain;
    private javax.swing.JPanel jPanelSep;
    private javax.swing.JPanel jPanelSpace;
    private javax.swing.JPanel jPanelSpace14;
    private javax.swing.JPanel jPanelSpace199;
    private javax.swing.JPanel jPanelSpace2;
    private javax.swing.JPanel jPanelSpace23;
    private javax.swing.JPanel jPanelSpace456;
    private javax.swing.JPanel jPanelSpace99;
    private javax.swing.JPanel jPanelSpaceSpace;
    private javax.swing.JPanel jPanelSpaceX;
    private javax.swing.JPasswordField jPasswordFieldHttpPass;
    private javax.swing.JPasswordField jPasswordFieldHttpPassAsyncMDN;
    private javax.swing.JRadioButton jRadioButtonAsyncMDN;
    private javax.swing.JRadioButton jRadioButtonSyncMDN;
    private javax.swing.JScrollPane jScrollPaneHttpHeader;
    private javax.swing.JScrollPane jScrollPanePartnerComment;
    private javax.swing.JScrollPane jScrollPaneTextAreaPartnerSystemInformation;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JTabbedPane jTabbedPane;
    private javax.swing.JTable jTableHttpHeader;
    private javax.swing.JTextArea jTextAreaPartnerSystemInformation;
    private javax.swing.JTextField jTextFieldAS2Version;
    private javax.swing.JTextField jTextFieldCommandOnReceipt;
    private javax.swing.JTextField jTextFieldCommandOnSendError;
    private javax.swing.JTextField jTextFieldCommandOnSendSuccess;
    private javax.swing.JTextField jTextFieldContentType;
    private javax.swing.JTextField jTextFieldEMail;
    private javax.swing.JTextField jTextFieldHttpAuthAsyncMDNUser;
    private javax.swing.JTextField jTextFieldHttpAuthUser;
    private javax.swing.JTextField jTextFieldId;
    private javax.swing.JTextField jTextFieldIgnorePollFilterList;
    private javax.swing.JTextField jTextFieldMDNURL;
    private javax.swing.JTextField jTextFieldName;
    private javax.swing.JTextField jTextFieldNotifyReceive;
    private javax.swing.JTextField jTextFieldNotifySend;
    private javax.swing.JTextField jTextFieldNotifySendReceive;
    private javax.swing.JTextField jTextFieldPollDir;
    private javax.swing.JTextField jTextFieldPollInterval;
    private javax.swing.JTextField jTextFieldPollMaxFiles;
    private javax.swing.JTextField jTextFieldProductName;
    private javax.swing.JTextField jTextFieldSubject;
    private javax.swing.JTextField jTextFieldURL;
    private javax.swing.JTextPane jTextPanePartnerComment;
    // End of variables declaration//GEN-END:variables
}
