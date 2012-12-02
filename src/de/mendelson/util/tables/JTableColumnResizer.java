//$Header: /cvsroot-fuse/mec-as2/39/mendelson/util/tables/JTableColumnResizer.java,v 1.1 2012/04/18 14:10:47 heller Exp $
package de.mendelson.util.tables;

import java.awt.Component;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.*;
/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */

/**
 * Looks at the content of the columns of a JTable and sets the prefered widths
 * JTableColumnResizer.adjustColumnWidthByContent(myJTableObject);
 *
 */
public class JTableColumnResizer {

    public static void adjustColumnWidthByContent(final JTable table) {
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                for (int column = 0; column < table.getColumnCount(); column++) {
                    int maxWidth = 0;
                    for (int row = 0; row < table.getRowCount(); row++) {
                        TableCellRenderer renderer = table.getCellRenderer(row, column);
                        Object value = table.getValueAt(row, column);
                        Component comp =
                                renderer.getTableCellRendererComponent(table,
                                value,
                                false,
                                false,
                                row,
                                column);
                        maxWidth = Math.max(comp.getPreferredSize().width, maxWidth);
                    }
                    TableColumn tableColumn = table.getColumnModel().getColumn(column);
                    TableCellRenderer headerRenderer = tableColumn.getHeaderRenderer();
                    if (headerRenderer == null) {
                        headerRenderer = table.getTableHeader().getDefaultRenderer();
                    }
                    Object headerValue = tableColumn.getHeaderValue();
                    Component headerComponent =
                            headerRenderer.getTableCellRendererComponent(table,
                            headerValue,
                            false,
                            false,
                            0,
                            column);
                    maxWidth = Math.max(maxWidth, headerComponent.getPreferredSize().width);
                    tableColumn.setPreferredWidth(maxWidth);
                }
            }
        };
        try{
            SwingUtilities.invokeLater(runnable);
        }
        catch( Exception e ){
            e.printStackTrace();
        }
    }
}
