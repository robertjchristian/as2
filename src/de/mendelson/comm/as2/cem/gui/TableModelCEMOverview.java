//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/cem/gui/TableModelCEMOverview.java,v 1.1 2012/04/18 14:10:20 heller Exp $
package de.mendelson.comm.as2.cem.gui;

import de.mendelson.comm.as2.cem.CEMEntry;
import java.util.*;
import de.mendelson.util.MecResourceBundle;
import java.text.DateFormat;
import javax.swing.table.AbstractTableModel;


/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Model to display the message overview
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class TableModelCEMOverview extends AbstractTableModel {

    /**ResourceBundle to localize the headers*/
    private MecResourceBundle rb = null;
    /**Data to display*/
    private List<CEMEntry> data = new ArrayList<CEMEntry>();
    private DateFormat format = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);

    /** Creates new LogTableModel
     */
    public TableModelCEMOverview() {
        //load resource bundle
        try {
            this.rb = (MecResourceBundle) ResourceBundle.getBundle(
                    ResourceBundleCEMOverview.class.getName());
        } //load up  resourcebundle
        catch (MissingResourceException e) {
            throw new RuntimeException("Oops..resource bundle " + e.getClassName() + " not found.");
        }
    }

    /**Passes data to the model and fires a table data update*/
    public void passNewData(List<CEMEntry> newData) {
        this.data = newData;
        this.fireTableDataChanged();
    }

    /**Number of rows to display*/
    @Override
    public int getRowCount() {
        return (this.data.size());
    }

    /**Number of cols to display*/
    @Override
    public int getColumnCount() {
        return (7);
    }

    /**Returns a row of the content*/
    public CEMEntry getRowAt( int row ){
        return( this.data.get(row));
    }

    /**Returns a value at a specific position in the grid
     */
    @Override
    public Object getValueAt(int row, int col) {
        CEMEntry entry = this.data.get(row);
        switch (col) {
            case 0:
                return (new CEMSystemActivity(entry));
            case 1:
                return entry;
            case 2:
                return (this.format.format(new Date(entry.getRequestMessageOriginated())));
            case 3:
                return (entry.getInitiatorAS2Id());
            case 4:
                return (entry.getReceiverAS2Id());
            case 5:
                return (entry.getSerialId());
            case 6:
                return (CEMEntry.getCategoryLocalized(entry.getCategory()));
        }
        return (null);
    }

    

    /**Returns the name of every column
     * @param col Column to get the header name of
     */
    @Override
    public String getColumnName(int col) {
        switch (col) {
            case 0:
                return (this.rb.getResourceString("header.activity"));
            case 1:
                return (this.rb.getResourceString("header.state"));            
            case 2:
                return (this.rb.getResourceString("header.requestdate"));
            case 3:
                return (this.rb.getResourceString("header.initiator"));
            case 4:
                return (this.rb.getResourceString("header.receiver"));
            case 5:
                return (this.rb.getResourceString("header.alias"));
            case 6:
                return (this.rb.getResourceString("header.category"));
        }
        return (null);
    }

    /**Set how to display the grid elements
     * @param col requested column
     */
    @Override
    public Class getColumnClass(int col) {
        return (new Class[]{
                    CEMSystemActivity.class,
                    CEMEntry.class,
                    String.class,
                    String.class,
                    String.class,
                    String.class,
                    String.class,
                    String.class,
                }[col]);
    }
}
