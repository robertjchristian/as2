//$Header: /cvsroot-fuse/mec-as2/39/mendelson/util/tables/JTableSortable.java,v 1.1 2012/04/18 14:10:47 heller Exp $
package de.mendelson.util.tables;

import javax.swing.DefaultListSelectionModel;
import javax.swing.JTable;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 *Table that will automatically translate view rows into model rows. This is only necessary if a
 * rowsorter has been added
 */
public class JTableSortable extends JTable {

    /**Returns the selected row in the model*/
    @Override
    public int getSelectedRow() {
        int selectedRow = super.getSelectedRow();
        if (selectedRow >= 0) {
            return (this.convertRowIndexToModel(selectedRow));
        } else {
            return (selectedRow);
        }
    }

    /**Returns the selected rows in the model*/
    @Override
    public int[] getSelectedRows() {
        int[] selectedRows = super.getSelectedRows();
        if (selectedRows != null) {
            for (int i = 0; i < selectedRows.length; i++) {
                selectedRows[i] = this.convertRowIndexToModel(selectedRows[i]);
            }
        }
        return (selectedRows);
    }

    /**Sets the selected rows where index0 and index1 are the model selection indicies*/
    public void setSelectionInterval(int index0, int index1) {
        int modelIndex0 = convertRowIndexToView(index0);
        int modelIndex1 = convertRowIndexToView(index1);
        this.getSelectionModel().setSelectionInterval(modelIndex0, modelIndex1);
    }
}
