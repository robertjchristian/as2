//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/preferences/TableModelPreferencesDir.java,v 1.1 2012/04/18 14:10:35 heller Exp $
package de.mendelson.comm.as2.preferences;

import de.mendelson.util.MecResourceBundle;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import javax.swing.table.AbstractTableModel;

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
public class TableModelPreferencesDir extends AbstractTableModel {
    
    /*Actual data to display, list of directory prefs*/
    private List<PreferencesObjectKeyValue> array = new ArrayList<PreferencesObjectKeyValue>();
  
    /*ResourceBundle to localize the headers*/
    private MecResourceBundle rb = null;
    
    /** Creates new preferences table model
     *@param rb Resourcebundle to localize the header rows
     */
    public TableModelPreferencesDir() {
        //load resource bundle
        try {
            this.rb = (MecResourceBundle) ResourceBundle.getBundle(
                    ResourceBundlePreferences.class.getName());
        } catch (MissingResourceException e) {
            throw new RuntimeException("Oops..resource bundle " + e.getClassName() + " not found.");
        }
    }

    /**Passes data to the model and fires a table data update
     *@param newData data array, may be null to delete the actual data contents
     */
    public void passNewData( List<PreferencesObjectKeyValue> newData ) {
        if( newData == null )
            this.array.clear();
        else
            this.array = newData;
        ((AbstractTableModel)this).fireTableDataChanged();
    }
        
    /**return one value defined by row and column
     *@param row row that contains value
     *@param col column that contains value
     */
    @Override
    public Object getValueAt(int row, int col) {
        PreferencesObjectKeyValue keyValue = this.array.get( row );
       
        //preferences name
        if( col == 0 )
            return( keyValue.getName() );
        //assigned value
        if( col == 1 )
            return( keyValue.getValue());
        return( null );
    }
    
    /**Returns the preference object at a special row position
     *@param row row to get the preference directory for
     */
    public PreferencesObjectKeyValue getPreference( int row ){
        return( (PreferencesObjectKeyValue)this.array.get(row) );
    }
    
    /**returns the number of rows in the table*/
    @Override
    public int getRowCount() {
        return array.size();
    }
    
    /**returns the number of columns in the table. should be const for a table*/
    @Override
    public int getColumnCount() {
        return(2);
    }
    
    /**Returns the name of every column
     *@param col Column to get the header name of
     */
    @Override
    public String getColumnName( int col ){
        
        switch (col) {
            case 0:
                return this.rb.getResourceString("header.dirname");
            case 1:
                return this.rb.getResourceString("header.dirvalue");
            default:
                return "";
        }
    }
                
}
