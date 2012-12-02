//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/importexport/TableModelPartnerSelect.java,v 1.1 2012/04/18 14:10:30 heller Exp $
package de.mendelson.comm.as2.importexport;

import de.mendelson.comm.as2.partner.Partner;
import de.mendelson.comm.as2.partner.gui.ListCellRendererPartner;
import de.mendelson.util.MecResourceBundle;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import javax.swing.ImageIcon;
import javax.swing.table.AbstractTableModel;


/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Model to display a list of partners and select them
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class TableModelPartnerSelect extends AbstractTableModel {

    public static final ImageIcon ICON_LOCALSTATION = ListCellRendererPartner.ICON_LOCALSTATION;
    public static final ImageIcon ICON_REMOTESTATION =ListCellRendererPartner.ICON_REMOTESTATION;
    private List<Partner> partner = null;
    private List<Boolean> importState = null;

    /**Resource bundle to localize the gui*/
    private MecResourceBundle rb;

    /**Load resources*/
    public TableModelPartnerSelect() {
        //load resource bundle
        try {
            this.rb = (MecResourceBundle) ResourceBundle.getBundle(
                    ResourceBundleImportConfiguration.class.getName());
        } catch (MissingResourceException e) {
            throw new RuntimeException("Oops..resource bundle " + e.getClassName() + " not found.");
        }
    }

    /**Selects all partners*/
    public void selectAll(){
        for( int i = 0; i < this.importState.size(); i++ ){
            this.importState.set(i, Boolean.TRUE);
        }
        ((AbstractTableModel)this).fireTableDataChanged();
    }

    /**Selects no partner*/
    public void selectNone(){
        for( int i = 0; i < this.importState.size(); i++ ){
            this.importState.set(i, Boolean.FALSE);
        }
        ((AbstractTableModel)this).fireTableDataChanged();
    }

    /**Returns the result: should the partner be imported?
     */
    public boolean viewShouldBeSaved(int row) {
        return (this.importState.get(row));
    }

    public void passNewData(List<Partner> partner) {
        this.partner = partner;
        this.importState = new ArrayList<Boolean>(partner.size());
        for (int i = 0; i < this.partner.size(); i++) {
            this.importState.add(Boolean.FALSE);
        }
        ((AbstractTableModel)this).fireTableDataChanged();
    }

    /**Number of rows to display*/
    @Override
    public int getRowCount() {
        if (this.partner != null) {
            return (this.partner.size());
        }
        return (0);
    }

    /**Number of cols to display*/
    @Override
    public int getColumnCount() {
        return (4);
    }

    /**Returns a value at a specific position in the grid
     */
    @Override
    public Object getValueAt(int row, int col) {
        if (col == 0) {
            if (this.partner.get(row).isLocalStation()) {
                return (ICON_LOCALSTATION);
            } else {
                return (ICON_REMOTESTATION);
            }
        }
        if (col == 1) {
            return (this.partner.get(row).getName());
        }
        if (col == 2) {
            return (this.partner.get(row).getAS2Identification());
        }
        return (this.importState.get(row));
    }

    /**Returns the name of every column
     * @param col Column to get the header name of
     */
    @Override
    public String getColumnName(int col) {
        if (col == 0) {
            return (" ");
        }
        if (col == 1) {
            return (this.rb.getResourceString( "header.name"));
        }
        if (col == 2) {
            return (this.rb.getResourceString( "header.as2id"));
        }
        return ("  ");
    }

    /**Set how to display the grid elements
     * @param col requested column
     */
    @Override
    public Class getColumnClass(int col) {
        return (new Class[]{
                    ImageIcon.class,
                    String.class,
                    String.class,
                    Boolean.class,}[col]);
    }

    /**Swing GUI checks which cols are editable.
     */
    @Override
    public boolean isCellEditable(int row, int col) {
        return (this.getColumnClass(col).equals(Boolean.class));
    }

    /**This is automatically called if a cell value is changed..*/
    @Override
    public void setValueAt(Object value, int row, int col) {
        if (this.getColumnClass(col).equals(Boolean.class)) {
            this.importState.set(row, (Boolean) value);
        }
    }

    /**Returns a list of selected partner*/
    public List<Partner> getSelectedPartner() {
        List<Partner> selectedPartner = new ArrayList<Partner>();
        for (int i = 0; i < this.importState.size(); i++) {
            if (this.importState.get(i).booleanValue()) {
                selectedPartner.add(this.partner.get(i));
            }
        }
        return (selectedPartner);
    }
}
