//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/preferences/PreferencesPanelNotification.java,v 1.1 2012/04/18 14:10:35 heller Exp $
package de.mendelson.comm.as2.preferences;

import de.mendelson.comm.as2.clientserver.message.PerformNotificationTestRequest;
import de.mendelson.comm.as2.notification.NotificationAccessDB;
import de.mendelson.comm.as2.notification.NotificationData;
import de.mendelson.util.MecResourceBundle;
import de.mendelson.util.clientserver.BaseClient;
import de.mendelson.util.clientserver.clients.preferences.PreferencesClient;
import de.mendelson.util.clientserver.messages.ClientServerResponse;
import java.sql.Connection;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import javax.swing.JOptionPane;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 *Panel to define the directory preferences
 * @author S.Heller
 * @version: $Revision: 1.1 $
 */
public class PreferencesPanelNotification extends PreferencesPanel {

    /**Localize the GUI*/
    private MecResourceBundle rb = null;
    //DB connection
    private Connection configConnection = null;
    private Connection runtimeConnection = null;
    private PreferencesClient preferences;
    private BaseClient baseClient;

    /** Creates new form PreferencesPanelDirectories */
    public PreferencesPanelNotification(BaseClient baseClient) {
        //load resource bundle
        try {
            this.rb = (MecResourceBundle) ResourceBundle.getBundle(
                    ResourceBundlePreferences.class.getName());
        } catch (MissingResourceException e) {
            throw new RuntimeException("Oops..resource bundle " + e.getClassName() + " not found.");
        }
        this.baseClient = baseClient;
        this.preferences = new PreferencesClient(baseClient);
        this.initComponents();
    }

    /**Sets new preferences to this panel to changes/modify
     */
    @Override
    public void loadPreferences(Connection configConnection, Connection runtimeConnection) {
        this.configConnection = configConnection;
        NotificationAccessDB access = new NotificationAccessDB(this.configConnection);
        NotificationData data = access.getNotificationData();
        this.jTextFieldAccountName.setText(data.getAccountName());
        this.jPasswordFieldAccountPass.setText(new String(data.getAccountPassword()));
        this.jTextFieldHost.setText(data.getMailServer());
        this.jTextFieldNotificationMail.setText(data.getNotificationMail());
        this.jTextFieldPort.setText(String.valueOf(data.getMailServerPort()));
        this.jTextFieldReplyTo.setText(data.getReplyTo());
        this.jCheckBoxNotifyCert.setSelected(data.notifyCertExpire());
        this.jCheckBoxNotifyTransactionError.setSelected(data.notifyTransactionError());
        this.jCheckBoxNotifyCEM.setSelected(data.notifyCEM());
        this.jCheckBoxNotifySystemFailure.setSelected(data.notifySystemFailure());
        this.jCheckBoxNotifyResend.setSelected(data.notifyResendDetected());
        this.jCheckBoxSMTPAuthentication.setSelected(data.isUseSMTHAuth());
        if (data.getSmtpUser() != null) {
            this.jTextFieldUser.setText(data.getSmtpUser());
        } else {
            this.jTextFieldUser.setText("");
        }
        if (data.getSmtpPass() != null) {
            this.jPasswordFieldPass.setText(String.valueOf(data.getSmtpPass()));
        } else {
            this.jPasswordFieldPass.setText("");
        }
        this.setButtonState();
    }

    private void setButtonState() {
        this.jTextFieldUser.setEnabled(this.jCheckBoxSMTPAuthentication.isSelected());
        this.jPasswordFieldPass.setEnabled(this.jCheckBoxSMTPAuthentication.isSelected());
    }

    /**Captures the gui settings, creates a notification data object from these settings and returns
     * this
     */
    private NotificationData captureGUIData() {
        NotificationData data = new NotificationData();
        data.setAccountName(this.jTextFieldAccountName.getText());
        data.setAccountPassword(this.jPasswordFieldAccountPass.getPassword());
        data.setMailServer(this.jTextFieldHost.getText());
        try {
            data.setMailServerPort(Integer.valueOf(this.jTextFieldPort.getText()).intValue());
        } catch (NumberFormatException e) {
            //if there is nonsense in this field just take the default value of the object
        }
        data.setNotifyCertExpire(this.jCheckBoxNotifyCert.isSelected());
        data.setNotifyTransactionError(this.jCheckBoxNotifyTransactionError.isSelected());
        data.setNotifyCEM(this.jCheckBoxNotifyCEM.isSelected());
        data.setNotifySystemFailure(this.jCheckBoxNotifySystemFailure.isSelected());
        data.setNotificationMail(this.jTextFieldNotificationMail.getText());
        data.setUseSMTHAuth(this.jCheckBoxSMTPAuthentication.isSelected());
        data.setSmtpUser(this.jTextFieldUser.getText());
        data.setSmtpPass(this.jPasswordFieldPass.getPassword());
        data.setReplyTo(this.jTextFieldReplyTo.getText());
        data.setNotifyResendDetected(this.jCheckBoxNotifyResend.isSelected());
        return (data);
    }

    /**Capture the GUI values and store them in the database
     * 
     */
    @Override
    public void savePreferences() {
        NotificationAccessDB access = new NotificationAccessDB(this.configConnection);
        NotificationData data = this.captureGUIData();
        access.updateNotification(data);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jTextFieldAccountName = new javax.swing.JTextField();
        jLabelAccountName = new javax.swing.JLabel();
        jPanelSpace = new javax.swing.JPanel();
        jLabelAccountPass = new javax.swing.JLabel();
        jPasswordFieldAccountPass = new javax.swing.JPasswordField();
        jCheckBoxNotifyCert = new javax.swing.JCheckBox();
        jCheckBoxNotifyTransactionError = new javax.swing.JCheckBox();
        jCheckBoxNotifyCEM = new javax.swing.JCheckBox();
        jLabelHost = new javax.swing.JLabel();
        jTextFieldHost = new javax.swing.JTextField();
        jLabelPort = new javax.swing.JLabel();
        jTextFieldPort = new javax.swing.JTextField();
        jLabelNotificationMail = new javax.swing.JLabel();
        jTextFieldNotificationMail = new javax.swing.JTextField();
        jLabelReplyTo = new javax.swing.JLabel();
        jTextFieldReplyTo = new javax.swing.JTextField();
        jButtonSendTestMail = new javax.swing.JButton();
        jPanelSep = new javax.swing.JPanel();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();
        jCheckBoxSMTPAuthentication = new javax.swing.JCheckBox();
        jPasswordFieldPass = new javax.swing.JPasswordField();
        jTextFieldUser = new javax.swing.JTextField();
        jLabelUser = new javax.swing.JLabel();
        jLabelPass = new javax.swing.JLabel();
        jCheckBoxNotifySystemFailure = new javax.swing.JCheckBox();
        jCheckBoxNotifyResend = new javax.swing.JCheckBox();

        setLayout(new java.awt.GridBagLayout());

        jTextFieldAccountName.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextFieldAccountNameKeyReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(jTextFieldAccountName, gridBagConstraints);

        jLabelAccountName.setText(this.rb.getResourceString( "label.mailaccount"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(jLabelAccountName, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 15;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.weighty = 1.0;
        add(jPanelSpace, gridBagConstraints);

        jLabelAccountPass.setText(this.rb.getResourceString( "label.mailpass"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(jLabelAccountPass, gridBagConstraints);

        jPasswordFieldAccountPass.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jPasswordFieldAccountPassKeyReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(jPasswordFieldAccountPass, gridBagConstraints);

        jCheckBoxNotifyCert.setText(this.rb.getResourceString( "checkbox.notifycertexpire"));
        jCheckBoxNotifyCert.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxNotifyCertActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(jCheckBoxNotifyCert, gridBagConstraints);

        jCheckBoxNotifyTransactionError.setText(this.rb.getResourceString("checkbox.notifytransactionerror"));
        jCheckBoxNotifyTransactionError.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxNotifyTransactionErrorActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(jCheckBoxNotifyTransactionError, gridBagConstraints);

        jCheckBoxNotifyCEM.setText(this.rb.getResourceString("checkbox.notifycem"));
        jCheckBoxNotifyCEM.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxNotifyCEMActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(jCheckBoxNotifyCEM, gridBagConstraints);

        jLabelHost.setText(this.rb.getResourceString("label.mailhost"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(jLabelHost, gridBagConstraints);

        jTextFieldHost.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextFieldHostKeyReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.7;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(jTextFieldHost, gridBagConstraints);

        jLabelPort.setText(this.rb.getResourceString( "label.mailport"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(jLabelPort, gridBagConstraints);

        jTextFieldPort.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextFieldPortKeyReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.3;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(jTextFieldPort, gridBagConstraints);

        jLabelNotificationMail.setText(this.rb.getResourceString( "label.notificationmail"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(jLabelNotificationMail, gridBagConstraints);

        jTextFieldNotificationMail.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextFieldNotificationMailKeyReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(jTextFieldNotificationMail, gridBagConstraints);

        jLabelReplyTo.setText(this.rb.getResourceString( "label.replyto"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(jLabelReplyTo, gridBagConstraints);

        jTextFieldReplyTo.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextFieldReplyToKeyReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(jTextFieldReplyTo, gridBagConstraints);

        jButtonSendTestMail.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/mendelson/comm/as2/preferences/testconnection16x16.gif"))); // NOI18N
        jButtonSendTestMail.setText(this.rb.getResourceString("button.testmail"));
        jButtonSendTestMail.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSendTestMailActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 10, 5);
        add(jButtonSendTestMail, gridBagConstraints);

        jPanelSep.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanelSep.add(jSeparator1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 10, 0);
        add(jPanelSep, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 10, 0);
        add(jSeparator2, gridBagConstraints);

        jCheckBoxSMTPAuthentication.setText(this.rb.getResourceString( "label.smtpauthentication"));
        jCheckBoxSMTPAuthentication.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxSMTPAuthenticationActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(jCheckBoxSMTPAuthentication, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(jPasswordFieldPass, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(jTextFieldUser, gridBagConstraints);

        jLabelUser.setText(this.rb.getResourceString( "label.smtpauthentication.user"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(jLabelUser, gridBagConstraints);

        jLabelPass.setText(this.rb.getResourceString( "label.smtpauthentication.pass"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(jLabelPass, gridBagConstraints);

        jCheckBoxNotifySystemFailure.setText(this.rb.getResourceString( "checkbox.notifyfailure"));
        jCheckBoxNotifySystemFailure.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxNotifySystemFailureActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(jCheckBoxNotifySystemFailure, gridBagConstraints);

        jCheckBoxNotifyResend.setText(this.rb.getResourceString( "checkbox.notifyresend"));
        jCheckBoxNotifyResend.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxNotifyResendActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(jCheckBoxNotifyResend, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonSendTestMailActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSendTestMailActionPerformed
        NotificationData data = this.captureGUIData();
        PerformNotificationTestRequest message = new PerformNotificationTestRequest(data);
        ClientServerResponse response = this.baseClient.sendSync(message);
        if (response == null) {
            JOptionPane.showMessageDialog(this,
                    this.rb.getResourceString("testmail.message.error", "--"),
                    this.rb.getResourceString("testmail.title"),
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (response.getException() != null) {
            JOptionPane.showMessageDialog(this,
                    this.rb.getResourceString("testmail.message.error", response.getException().getMessage()),
                    this.rb.getResourceString("testmail.title"),
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        JOptionPane.showMessageDialog(this,
                this.rb.getResourceString("testmail.message.success"),
                this.rb.getResourceString("testmail.title"),
                JOptionPane.INFORMATION_MESSAGE);
    }//GEN-LAST:event_jButtonSendTestMailActionPerformed

private void jTextFieldHostKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldHostKeyReleased
    this.savePreferences();
}//GEN-LAST:event_jTextFieldHostKeyReleased

private void jTextFieldPortKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldPortKeyReleased
    this.savePreferences();
}//GEN-LAST:event_jTextFieldPortKeyReleased

private void jTextFieldAccountNameKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldAccountNameKeyReleased
    this.savePreferences();
}//GEN-LAST:event_jTextFieldAccountNameKeyReleased

private void jPasswordFieldAccountPassKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jPasswordFieldAccountPassKeyReleased
    this.savePreferences();
}//GEN-LAST:event_jPasswordFieldAccountPassKeyReleased

private void jTextFieldReplyToKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldReplyToKeyReleased
    this.savePreferences();
}//GEN-LAST:event_jTextFieldReplyToKeyReleased

private void jTextFieldNotificationMailKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldNotificationMailKeyReleased
    this.savePreferences();
}//GEN-LAST:event_jTextFieldNotificationMailKeyReleased

private void jCheckBoxNotifyCertActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxNotifyCertActionPerformed
    this.savePreferences();
}//GEN-LAST:event_jCheckBoxNotifyCertActionPerformed

private void jCheckBoxNotifyTransactionErrorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxNotifyTransactionErrorActionPerformed
    this.savePreferences();
}//GEN-LAST:event_jCheckBoxNotifyTransactionErrorActionPerformed

private void jCheckBoxNotifyCEMActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxNotifyCEMActionPerformed
    this.savePreferences();
}//GEN-LAST:event_jCheckBoxNotifyCEMActionPerformed

private void jCheckBoxSMTPAuthenticationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxSMTPAuthenticationActionPerformed
    this.setButtonState();
}//GEN-LAST:event_jCheckBoxSMTPAuthenticationActionPerformed

private void jCheckBoxNotifySystemFailureActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxNotifySystemFailureActionPerformed
    this.savePreferences();
}//GEN-LAST:event_jCheckBoxNotifySystemFailureActionPerformed

    private void jCheckBoxNotifyResendActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxNotifyResendActionPerformed
        this.savePreferences();
    }//GEN-LAST:event_jCheckBoxNotifyResendActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonSendTestMail;
    private javax.swing.JCheckBox jCheckBoxNotifyCEM;
    private javax.swing.JCheckBox jCheckBoxNotifyCert;
    private javax.swing.JCheckBox jCheckBoxNotifyResend;
    private javax.swing.JCheckBox jCheckBoxNotifySystemFailure;
    private javax.swing.JCheckBox jCheckBoxNotifyTransactionError;
    private javax.swing.JCheckBox jCheckBoxSMTPAuthentication;
    private javax.swing.JLabel jLabelAccountName;
    private javax.swing.JLabel jLabelAccountPass;
    private javax.swing.JLabel jLabelHost;
    private javax.swing.JLabel jLabelNotificationMail;
    private javax.swing.JLabel jLabelPass;
    private javax.swing.JLabel jLabelPort;
    private javax.swing.JLabel jLabelReplyTo;
    private javax.swing.JLabel jLabelUser;
    private javax.swing.JPanel jPanelSep;
    private javax.swing.JPanel jPanelSpace;
    private javax.swing.JPasswordField jPasswordFieldAccountPass;
    private javax.swing.JPasswordField jPasswordFieldPass;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JTextField jTextFieldAccountName;
    private javax.swing.JTextField jTextFieldHost;
    private javax.swing.JTextField jTextFieldNotificationMail;
    private javax.swing.JTextField jTextFieldPort;
    private javax.swing.JTextField jTextFieldReplyTo;
    private javax.swing.JTextField jTextFieldUser;
    // End of variables declaration//GEN-END:variables

    @Override
    public String getIconResource() {
        return ("/de/mendelson/comm/as2/preferences/notification32x32.gif");
    }

    @Override
    public String getTabResource() {
        return ("tab.notification");
    }
}
