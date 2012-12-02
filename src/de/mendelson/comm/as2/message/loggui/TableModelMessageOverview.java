//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/message/loggui/TableModelMessageOverview.java,v 1.1 2012/04/18 14:10:30 heller Exp $
package de.mendelson.comm.as2.message.loggui;

import de.mendelson.comm.as2.message.AS2Message;
import de.mendelson.comm.as2.message.AS2MessageInfo;
import de.mendelson.comm.as2.message.AS2Payload;
import de.mendelson.comm.as2.message.ResourceBundleAS2Message;
import de.mendelson.comm.as2.partner.Partner;
import de.mendelson.util.ImageUtil;
import de.mendelson.util.MecResourceBundle;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
 * Model to display the message overview
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class TableModelMessageOverview extends AbstractTableModel {

    public static final ImageIcon ICON_IN = new ImageIcon(TableModelMessageOverview.class.getResource("/de/mendelson/comm/as2/message/loggui/in16x16.gif"));
    public static final ImageIcon ICON_OUT = new ImageIcon(TableModelMessageOverview.class.getResource("/de/mendelson/comm/as2/message/loggui/out16x16.gif"));
    public static final ImageIcon ICON_PENDING = new ImageIcon(TableModelMessageOverview.class.getResource("/de/mendelson/comm/as2/message/loggui/state_pending16x16.gif"));
    public static final ImageIcon ICON_STOPPED = new ImageIcon(TableModelMessageOverview.class.getResource("/de/mendelson/comm/as2/message/loggui/state_stopped16x16.gif"));
    public static final ImageIcon ICON_FINISHED = new ImageIcon(TableModelMessageOverview.class.getResource("/de/mendelson/comm/as2/message/loggui/state_finished16x16.gif"));
    public static final ImageIcon ICON_RESEND_OVERLAY = new ImageIcon(TableModelMessageOverview.class.getResource("/de/mendelson/comm/as2/message/loggui/resend_overlay16x16.gif"));
    /**ResourceBundle to localize the headers*/
    private MecResourceBundle rb = null;
    /**ResourceBundle to localize the enc/signature stuff*/
    private MecResourceBundle rbMessage = null;
    /**Stores all partner ids and the corresponding partner objects*/
    private final Map<String, Partner> partnerMap = Collections.synchronizedMap(new HashMap<String, Partner>());
    /**Data to display*/
    private final List<AS2Message> data = Collections.synchronizedList(new ArrayList<AS2Message>());
    /**Format the date display*/
    private DateFormat format = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
    private ImageUtil imageUtil = new ImageUtil(); 
    
    /** Creates new LogTableModel
     */
    public TableModelMessageOverview() {
        //load resource bundle
        try {
            this.rb = (MecResourceBundle) ResourceBundle.getBundle(
                    ResourceBundleMessageOverview.class.getName());
            this.rbMessage = (MecResourceBundle) ResourceBundle.getBundle(
                    ResourceBundleAS2Message.class.getName());
        } //load up  resourcebundle
        catch (MissingResourceException e) {
            throw new RuntimeException("Oops..resource bundle "
                    + e.getClassName() + " not found.");
        }        
    }

    
    /**Passes a list of partner ot this table
     **/
    public void passPartner(Map<String, Partner> partnerMap) {
        synchronized (this.partnerMap) {
            this.partnerMap.putAll(partnerMap);
        }
        this.fireTableDataChanged();
    }

    /**Passes data to the model and fires a table data update*/
    public void passNewData(List<AS2Message> newData) {
        synchronized (this.data) {
            this.data.clear();
            this.data.addAll(newData);
        }
        ((AbstractTableModel) this).fireTableDataChanged();
    }

    public void passPayload(AS2Message message, List<AS2Payload> payloads) {
        int index = -1;
        synchronized (this.data) {
            index = this.data.indexOf(message);
            if (index != -1) {
                AS2Message foundMessage = this.data.get(index);
                foundMessage.setPayloads(payloads);
            }
        }
        if (index != -1) {
            ((AbstractTableModel) this).fireTableRowsUpdated(index, index);
        }
    }

    /**Returns the data stored in the specific row
     *@param row Row to look into*/
    public AS2Message getRow(int row) {
        synchronized (this.data) {
            if (row > this.data.size() - 1) {
                return (null);
            }
            return (this.data.get(row));
        }
    }

    /**Returns the data stored in specific rows
     *@param row Rows to look into*/
    public AS2Message[] getRows(int[] row) {
        AS2Message[] rows = new AS2Message[row.length];
        synchronized (this.data) {
            for (int i = 0; i < row.length; i++) {
                rows[i] = this.data.get(row[i]);
            }
            return (rows);
        }
    }

    /**Number of rows to display*/
    @Override
    public int getRowCount() {
        synchronized (this.data) {
            return (this.data.size());
        }
    }

    /**Number of cols to display*/
    @Override
    public int getColumnCount() {
        return (10);
    }

    /**Returns a value at a specific position in the grid
     */
    @Override
    public Object getValueAt(int row, int col) {
        AS2Message overviewRow = null;
        synchronized (this.data) {
            overviewRow = this.data.get(row);
        }
        AS2MessageInfo info = (AS2MessageInfo) overviewRow.getAS2Info();
        switch (col) {
            case 0:
                if (info.getState() == AS2Message.STATE_FINISHED) {
                    if( info.getResendCounter() == 0 ){
                        return (ICON_FINISHED);
                    }else{
                        return (this.imageUtil.mixImages(ICON_FINISHED, ICON_RESEND_OVERLAY));
                    }
                } else if (info.getState() == AS2Message.STATE_STOPPED) {
                    if( info.getResendCounter() == 0 ){
                        return (ICON_STOPPED);
                    }else{
                        return (this.imageUtil.mixImages(ICON_STOPPED, ICON_RESEND_OVERLAY));
                    }
                }
                return (ICON_PENDING);
            case 1:
                if (info.getDirection() == AS2MessageInfo.DIRECTION_IN) {
                    return (ICON_IN);
                } else {
                    return (ICON_OUT);
                }
            case 2:
                return (info.getInitDate());
            case 3:
                if (info.getDirection() != AS2MessageInfo.DIRECTION_IN) {
                    String id = info.getSenderId();
                    synchronized (this.partnerMap) {
                        Partner sender = this.partnerMap.get(id);
                        if (sender != null) {
                            return (sender.getName());
                        } else {
                            return (id);
                        }
                    }
                } else {
                    String id = info.getReceiverId();
                    synchronized (this.partnerMap) {
                        Partner receiver = this.partnerMap.get(id);
                        if (receiver != null) {
                            return (receiver.getName());
                        } else {
                            return (id);
                        }
                    }
                }
            case 4:
                if (info.getDirection() == AS2MessageInfo.DIRECTION_IN) {
                    String id = info.getSenderId();
                    synchronized (this.partnerMap) {
                        Partner sender = this.partnerMap.get(id);
                        if (sender != null) {
                            return (sender.getName());
                        } else {
                            return (id);
                        }
                    }
                } else {
                    String id = info.getReceiverId();
                    synchronized (this.partnerMap) {
                        Partner receiver = this.partnerMap.get(id);
                        if (receiver != null) {
                            return (receiver.getName());
                        } else {
                            return (id);
                        }
                    }
                }
            case 5:
                return (info.getMessageId());
            case 6:
                if (overviewRow.getPayloadCount() == 0
                        || (overviewRow.getPayloadCount() == 1 && overviewRow.getPayload(0).getOriginalFilename() == null)) {
                    return ("--");
                } else if (overviewRow.getPayloadCount() == 1) {
                    return (overviewRow.getPayload(0).getOriginalFilename());
                } else {
                    return (this.rb.getResourceString("number.of.attachments", String.valueOf(overviewRow.getPayloadCount())));
                }
            case 7:
                return (this.rbMessage.getResourceString("encryption." + info.getEncryptionType()));
            case 8:
                return (this.rbMessage.getResourceString("signature." + info.getSignType()));
            case 9:
                if (info.requestsSyncMDN()) {
                    return ("SYNC");
                } else {
                    return ("ASYNC");
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
                return ("  ");
            case 2:
                return (this.rb.getResourceString("header.timestamp"));
            case 3:
                return (this.rb.getResourceString("header.localstation"));
            case 4:
                return (this.rb.getResourceString("header.partner"));
            case 5:
                return (this.rb.getResourceString("header.messageid"));
            case 6:
                return (this.rb.getResourceString("header.payload"));
            case 7:
                return (this.rb.getResourceString("header.encryption"));
            case 8:
                return (this.rb.getResourceString("header.signature"));
            case 9:
                return (this.rb.getResourceString("header.mdn"));
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
                    ImageIcon.class,
                    Date.class,
                    String.class,
                    String.class,
                    String.class,
                    String.class,
                    String.class,
                    String.class,
                    String.class
                }[col]);
    }
}
