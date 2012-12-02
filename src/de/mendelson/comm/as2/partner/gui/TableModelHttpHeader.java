//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/partner/gui/TableModelHttpHeader.java,v 1.1 2012/04/18 14:10:32 heller Exp $
package de.mendelson.comm.as2.partner.gui;

import de.mendelson.comm.as2.partner.PartnerHttpHeader;
import javax.swing.table.*;
import java.util.*;
import de.mendelson.util.MecResourceBundle;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Table model to display the properties to set
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class TableModelHttpHeader extends AbstractTableModel {

    /*Actual data to display, list of directory prefs*/
    private List<PartnerHttpHeader> array = new ArrayList<PartnerHttpHeader>();
    /*ResourceBundle to localize the headers*/
    private MecResourceBundle rb = null;

    /** Creates new preferences table model
     *@param rb Resourcebundle to localize the header rows
     */
    public TableModelHttpHeader() {
        //load resource bundle
        try {
            this.rb = (MecResourceBundle) ResourceBundle.getBundle(
                    ResourceBundlePartnerPanel.class.getName());
        } catch (MissingResourceException e) {
            throw new RuntimeException("Oops..resource bundle " + e.getClassName() + " not found.");
        }
    }

    /**Passes data to the model and fires a table data update
     *@param newData data array, may be null to delete the actual data contents
     */
    public void passNewData(List<PartnerHttpHeader> newData) {
        this.array = newData;
        ((AbstractTableModel) this).fireTableDataChanged();
    }

    /**Passes a new single value to the array
     *@param header new header to add to the partner
     */
    public void addRow(PartnerHttpHeader header) {
        this.array.add(header);
        ((AbstractTableModel) this).fireTableDataChanged();
    }

    /**Passes a new single value to the array
     *@param header new header to add to the partner
     */
    public void deleteRow(int row) {
        this.array.remove(row);
        ((AbstractTableModel) this).fireTableDataChanged();
    }

    /**return one value defined by row and column
     *@param row row that contains value
     *@param col column that contains value
     */
    @Override
    public Object getValueAt(int row, int col) {
        PartnerHttpHeader header = this.array.get(row);

        //preferences name
        if (col == 0) {
            return (header.getKey());
        }
        //assigned value
        if (col == 1) {
            return (header.getValue());
        }
        return (null);
    }

    /**returns the number of rows in the table*/
    @Override
    public int getRowCount() {
        return array.size();
    }

    /**returns the number of columns in the table. should be const for a table*/
    @Override
    public int getColumnCount() {
        return (2);
    }

    /**Returns the name of every column
     *@param col Column to get the header name of
     */
    @Override
    public String getColumnName(int col) {

        switch (col) {
            case 0:
                return this.rb.getResourceString("header.httpheaderkey");
            case 1:
                return this.rb.getResourceString("header.httpheadervalue");
            default:
                return "";
        }
    }

    @Override
    public boolean isCellEditable( int rowIndex, int columnIndex){
        return( true );
    }


    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex){
        String value = (String)aValue;
        if( columnIndex == 0 ){
            this.array.get(rowIndex).setKey(value);
        }else{
            this.array.get(rowIndex).setValue(value);
        }
    }
    
}
