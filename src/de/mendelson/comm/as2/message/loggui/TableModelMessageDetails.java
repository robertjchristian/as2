//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/message/loggui/TableModelMessageDetails.java,v 1.1 2012/04/18 14:10:30 heller Exp $
package de.mendelson.comm.as2.message.loggui;

import de.mendelson.comm.as2.message.AS2Info;
import de.mendelson.comm.as2.message.AS2Message;
import de.mendelson.comm.as2.message.AS2MessageInfo;
import de.mendelson.comm.as2.message.ResourceBundleAS2Message;
import javax.swing.*;
import java.util.*;
import java.text.*;
import de.mendelson.util.MecResourceBundle;
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
public class TableModelMessageDetails extends AbstractTableModel {

    public static final ImageIcon ICON_IN = new ImageIcon(TableModelMessageOverview.class.getResource("/de/mendelson/comm/as2/message/loggui/in16x16.gif"));
    public static final ImageIcon ICON_OUT = new ImageIcon(TableModelMessageOverview.class.getResource("/de/mendelson/comm/as2/message/loggui/out16x16.gif"));
    public static final ImageIcon ICON_MESSAGE = new ImageIcon(TableModelMessageOverview.class.getResource("/de/mendelson/comm/as2/message/loggui/message16x16.gif"));
    public static final ImageIcon ICON_SIGNAL_OK = new ImageIcon(TableModelMessageOverview.class.getResource("/de/mendelson/comm/as2/message/loggui/signal_ok16x16.gif"));
    public static final ImageIcon ICON_SIGNAL_FAILURE = new ImageIcon(TableModelMessageOverview.class.getResource("/de/mendelson/comm/as2/message/loggui/signal_failure16x16.gif"));
    /**ResourceBundle to localize the headers*/
    private MecResourceBundle rb = null;
    /**ResourceBundle to localize the headers*/
    private MecResourceBundle rbMessage = null;
    /**Format the date output*/
    private DateFormat format = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM);
    private final List<AS2Info> data = Collections.synchronizedList(new ArrayList<AS2Info>());

    /** Creates new LogTableModel
     */
    public TableModelMessageDetails() {
        super();
        //load resource bundle
        try {
            this.rb = (MecResourceBundle) ResourceBundle.getBundle(
                    ResourceBundleMessageDetails.class.getName());
            this.rbMessage = (MecResourceBundle) ResourceBundle.getBundle(
                    ResourceBundleAS2Message.class.getName());
        } //load up  resourcebundle
        catch (MissingResourceException e) {
            throw new RuntimeException("Oops..resource bundle "
                    + e.getClassName() + " not found.");
        }
    }

    /**Passes data to the model and fires a table data update*/
    public void passNewData(List<AS2Info> newData) {
        synchronized (this.data) {
            this.data.clear();
            this.data.addAll(newData);
        }
        this.fireTableDataChanged();
    }

    /**Returns the data stored in the specific row
     *@param row Row to look into*/
    public AS2Info getRow(int row) {
        synchronized (this.data) {
            if (row > this.data.size() - 1) {
                return (null);
            }
            return (this.data.get(row));
        }
    }

    /**Number of rows to display*/
    @Override
    public int getRowCount() {
        synchronized (this.data) {
            if (this.data == null) {
                return (0);
            }
            return (this.data.size());
        }
    }

    /**Number of cols to display*/
    @Override
    public int getColumnCount() {
        return (8);
    }

    /**Returns a value at a specific position in the grid
     */
    @Override
    public Object getValueAt(int row, int col) {
        AS2Info detailRow = null;
        synchronized (this.data) {
            detailRow = this.data.get(row);
        }
        switch (col) {
            case 0:
                if (detailRow.getDirection() == AS2MessageInfo.DIRECTION_IN) {
                    return (ICON_IN);
                } else {
                    return (ICON_OUT);
                }
            case 1:
                return (this.format.format(detailRow.getInitDate()));
            case 2:
                if (detailRow.isMDN()) {
                    if (detailRow.getState() == AS2Message.STATE_FINISHED) {
                        return (ICON_SIGNAL_OK);
                    } else {
                        return (ICON_SIGNAL_FAILURE);
                    }
                } else {
                    return (ICON_MESSAGE);
                }
            case 3:
                return (detailRow.getMessageId());
            case 4:
                return (this.rbMessage.getResourceString("signature." + detailRow.getSignType()));
            case 5:
                if (detailRow.isMDN()) {
                    return ("--");
                } else {
                    AS2MessageInfo messageInfo = (AS2MessageInfo) detailRow;
                    return (this.rbMessage.getResourceString("encryption." + messageInfo.getEncryptionType()));
                }
            case 6:
                if (detailRow.getSenderHost() != null) {
                    return (detailRow.getSenderHost());
                } else {
                    return ("");
                }
            case 7:
                if (detailRow.getUserAgent() != null) {
                    return (detailRow.getUserAgent());
                } else {
                    return ("");
                }
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
                return (" ");
            case 1:
                return (this.rb.getResourceString("header.timestamp"));
            case 2:
                return ("  ");
            case 3:
                return (this.rb.getResourceString("header.messageid"));
            case 4:
                return (this.rb.getResourceString("header.signature"));
            case 5:
                return (this.rb.getResourceString("header.encryption"));
            case 6:
                return (this.rb.getResourceString("header.senderhost"));
            case 7:
                return (this.rb.getResourceString("header.useragent"));
        }
        return (null);
    }

    /**Set how to display the grid elements
     * @param col requested column
     */
    @Override
    public Class getColumnClass(int col) {
        return (new Class[]{
                    ImageIcon.class,
                    String.class,
                    ImageIcon.class,
                    String.class,
                    String.class,
                    String.class,
                    String.class,
                    String.class,}[col]);
    }
}
