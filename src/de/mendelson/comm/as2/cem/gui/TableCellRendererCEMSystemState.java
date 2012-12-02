//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/cem/gui/TableCellRendererCEMSystemState.java,v 1.1 2012/04/18 14:10:20 heller Exp $
package de.mendelson.comm.as2.cem.gui;

import de.mendelson.comm.as2.cem.CEMEntry;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.Component;
import java.awt.Color;
import java.awt.Rectangle;

/** 
 * Renders the system activity column
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class TableCellRendererCEMSystemState extends DefaultTableCellRenderer implements TableCellRenderer {

    private Color colorAccepted = new Color(166, 247, 164);
    private Color colorPending = new Color(255, 255, 183);

    /**
     * Creates a default table cell renderer.
     */
    public TableCellRendererCEMSystemState() {
        super();
        this.setOpaque(true);
    }
    // implements javax.swing.table.TableCellRenderer

    /**
     *
     * Returns the default table cell renderer.
     *
     * @param table  the <code>JTable</code>
     * @param value  the value to assign to the cell at
     *			<code>[row, column]</code>
     * @param isSelected true if cell is selected
     * @param hasFocus true if cell has focus
     * @param row  the row of the cell to render
     * @param column the column of the cell to render
     * @return the default table cell renderer
     */
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        if (isSelected) {
            this.setBackground(table.getSelectionBackground());
            this.setForeground(table.getSelectionForeground());

        } else {
            this.setBackground(table.getBackground());
            this.setForeground(table.getForeground());
        }

        this.setEnabled(table.isEnabled());
        this.setFont(table.getFont());

        if (value instanceof CEMSystemActivity) {
            CEMSystemActivity activity = (CEMSystemActivity) value;
            int state = activity.getState();
            this.setText(activity.getText());
            if (state == CEMEntry.STATUS_ACCEPTED_INT || state == CEMEntry.STATUS_PENDING_INT) {
                Color backgroundColor = table.getBackground();
                if (state == CEMEntry.STATUS_ACCEPTED_INT) {
                    backgroundColor = this.colorAccepted;
                } else if (state == CEMEntry.STATUS_PENDING_INT) {
                    backgroundColor = this.colorPending;
                }
                if (isSelected) {
                    this.setBackground(backgroundColor.darker());
                } else {
                    this.setBackground(backgroundColor);
                }
            }
        }
        return (this);
    }

    /*
     * The following methods are overridden as a performance measure to
     * to prune code-paths are often called in the case of renders
     * but which we know are unnecessary.  Great care should be taken
     * when writing your own renderer to weigh the benefits and
     * drawbacks of overriding methods like these.
     */
    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a>
     * for more information.
     */
    @Override
    public boolean isOpaque() {
        Color back = getBackground();
        Component p = getParent();
        if (p != null) {
            p = p.getParent();
        }
        // p should now be the JTable.
        boolean colorMatch = (back != null) && (p != null)
                && back.equals(p.getBackground())
                && p.isOpaque();
        return !colorMatch && super.isOpaque();
    }

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a>
     * for more information.
     */
    @Override
    public void validate() {
    }

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a>
     * for more information.
     */
    @Override
    public void revalidate() {
    }

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a>
     * for more information.
     */
    @Override
    public void repaint(long tm, int x, int y, int width, int height) {
    }

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a>
     * for more information.
     */
    @Override
    public void repaint(Rectangle r) {
    }

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a>
     * for more information.
     */
    @Override
    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        // Strings get interned...
        if (propertyName.equals("text")) {
            super.firePropertyChange(propertyName, oldValue, newValue);
        }
    }

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a>
     * for more information.
     */
    @Override
    public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
    }
}


