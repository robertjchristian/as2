//$Header: /cvsroot-fuse/mec-as2/39/mendelson/util/security/cert/TableModelCertificates.java,v 1.1 2012/04/18 14:10:47 heller Exp $
package de.mendelson.util.security.cert;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;
import javax.swing.table.AbstractTableModel;
import de.mendelson.util.MecResourceBundle;
/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
import de.mendelson.util.security.DNUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * table model to display a configuration grid
 * @author  S.Heller
 * @version $Revision: 1.1 $
 */
public class TableModelCertificates extends AbstractTableModel {

    public static final ImageIcon ICON_CERTIFICATE =
            new ImageIcon(TableModelCertificates.class.getResource(
            "/de/mendelson/util/security/cert/certificate16x16.gif"));
    public static final ImageIcon ICON_KEY =
            new ImageIcon(TableModelCertificates.class.getResource(
            "/de/mendelson/util/security/cert/key16x16.gif"));
    public static final ImageIcon ICON_VALID =
            new ImageIcon(TableModelCertificates.class.getResource(
            "/de/mendelson/util/security/cert/cert_valid16x16.gif"));
    public static final ImageIcon ICON_INVALID =
            new ImageIcon(TableModelCertificates.class.getResource(
            "/de/mendelson/util/security/cert/cert_invalid16x16.gif"));
    public static final ImageIcon ICON_CERTIFICATE_ROOT =
            new ImageIcon(TableModelCertificates.class.getResource(
            "/de/mendelson/util/security/cert/cert_root16x16.gif"));
    public static final ImageIcon ICON_CERTIFICATE_MISSING =
            new ImageIcon(TableModelCertificates.class.getResource(
            "/de/mendelson/util/security/cert/gui/cert_missing16x16.gif"));
    /*ResourceBundle to localize the headers*/
    private MecResourceBundle rb = null;
    private DateFormat format = SimpleDateFormat.getDateInstance(DateFormat.SHORT);
    private final List<KeystoreCertificate> data = Collections.synchronizedList(new ArrayList<KeystoreCertificate>());

    /** Creates new table model
     */
    public TableModelCertificates() {
        //load resource bundle
        try {
            this.rb = (MecResourceBundle) ResourceBundle.getBundle(
                    ResourceBundleTableModelCertificates.class.getName());
        } catch (MissingResourceException e) {
            throw new RuntimeException("Oops..resource bundle " + e.getClassName() + " not found.");
        }
    }

    /**Passes data to the model and fires a table data update
     *@param list new data to display
     */
    public void setNewData(List<KeystoreCertificate> data) {
        synchronized (this.data) {
            this.data.clear();
            this.data.addAll(data);
        }
        ((AbstractTableModel) this).fireTableDataChanged();
    }

    /**returns the number of rows in the table*/
    @Override
    public int getRowCount() {
        synchronized (this.data) {
            return (this.data.size());
        }
    }

    /**returns the number of columns in the table. should be const for a table*/
    @Override
    public int getColumnCount() {
        return (7);
    }

    /**Returns the name of every column
     *@param col Column to get the header name of
     */
    @Override
    public String getColumnName(int col) {
        if (col == 0) {
            return (" ");
        }
        if (col == 1) {
            return ("  ");
        }
        if (col == 2) {
            return (this.rb.getResourceString("header.alias"));
        }
        if (col == 3) {
            return (this.rb.getResourceString("header.expire"));
        }
        if (col == 4) {
            return (this.rb.getResourceString("header.length"));
        }
        if (col == 5) {
            return (this.rb.getResourceString("header.organization"));
        }
        if (col == 6) {
            return (this.rb.getResourceString("header.ca"));
        }
        //should not happen
        return ("");
    }

    public ImageIcon getIconForCertificate(KeystoreCertificate certificate){
        if (certificate.getIsKeyPair()) {
                return (ICON_KEY);
            }
            if (certificate.isRootCertificate()) {
                return (ICON_CERTIFICATE_ROOT);
            }
            return (ICON_CERTIFICATE);
    }


    /**Returns the grid value*/
    @Override
    public Object getValueAt(int row, int col) {
        KeystoreCertificate certificate = null;
        synchronized (this.data) {
            certificate = this.data.get(row);
        }
        if (col == 0) {
            return( this.getIconForCertificate(certificate));
        }
        if (col == 1) {
            try {
                certificate.getX509Certificate().checkValidity();
                return (ICON_VALID);
            } catch (Exception e) {
                return (ICON_INVALID);
            }
        }
        if (col == 2) {
            return (certificate.getAlias());
        }
        if (col == 3) {
            return (this.format.format(certificate.getNotAfter()));
        }
        if (col == 4) {
            return (String.valueOf(certificate.getPublicKeyLength()));
        }
        if (col == 5) {
            return (DNUtil.getOrganization(certificate.getX509Certificate(), DNUtil.SUBJECT));
        }
        if (col == 6) {
            return (DNUtil.getCommonName(certificate.getX509Certificate(), DNUtil.ISSUER));
        }
        return ("");
    }

    /**Swing GUI checks which cols are editable.
     */
    @Override
    public boolean isCellEditable(int row, int col) {
        return (false);
    }

    /**Set how to display the grid elements
     * @param col requested column
     */
    @Override
    public Class getColumnClass(int col) {
        return (new Class[]{
                    ImageIcon.class,
                    ImageIcon.class,
                    String.class,
                    String.class,
                    String.class,
                    String.class,
                    String.class,}[col]);
    }

    /**Returns the certificate at the passed row
     */
    public KeystoreCertificate getParameter(int row) {
        synchronized (this.data) {
            return (this.data.get(row));
        }
    }
}
