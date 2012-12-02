//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/webclient2/TransactionDetailsDialog.java,v 1.1 2012/04/18 14:10:41 heller Exp $
package de.mendelson.comm.as2.webclient2;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import de.mendelson.comm.as2.log.LogAccessDB;
import de.mendelson.comm.as2.log.LogEntry;
import de.mendelson.comm.as2.message.AS2Info;
import de.mendelson.comm.as2.message.AS2MDNInfo;
import de.mendelson.comm.as2.message.AS2Message;
import de.mendelson.comm.as2.message.AS2MessageInfo;
import de.mendelson.comm.as2.message.AS2Payload;
import de.mendelson.comm.as2.message.MessageAccessDB;
import de.mendelson.comm.as2.message.ResourceBundleAS2Message;
import de.mendelson.util.MecResourceBundle;
import java.io.File;
import java.sql.Connection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;


/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * The about dialog for the as2 server web ui
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class TransactionDetailsDialog extends OkDialog {

    private Connection runtimeConnection = null;
    private Connection configConnection = null;
    private LogAccessDB logAccess;
    private MessageAccessDB messageAccess;
    private String messageId;
    private ThemeResource ICON_IN;
    private ThemeResource ICON_OUT;
    private ThemeResource ICON_MESSAGE;
    private ThemeResource ICON_SIGNAL_OK;
    private ThemeResource ICON_SIGNAL_FAILURE;
    private MecResourceBundle rbMessage = null;
    private String themeURI = null;
    private Table detailsTable = null;
    private FilePanel rawMessageDecryptedPanel = null;
    private FilePanel messageHeaderPanel = null;
    private FilePanel[] payloadPanel = null;
    private List<AS2Payload> payload = new ArrayList<AS2Payload>();
    private TextArea logPanel = null;
    private TabSheet tabSheet = null;

    public TransactionDetailsDialog(Connection configConnection, Connection runtimeConnection, AS2MessageInfo info, String themeURI) {
        super(800, 600, "Transaction details (" + info.getMessageId() + ")");
        this.setResizable(true);
        this.setClosable(true);
        this.runtimeConnection = runtimeConnection;
        this.configConnection = configConnection;
        this.messageId = info.getMessageId();
        this.themeURI = themeURI;
        this.addListener(new Window.ResizeListener() {

            @Override
            public void windowResized(ResizeEvent e) {
                TransactionDetailsDialog.this.rescalePanels();
            }
        });
    }

    private void rescalePanels() {
        int yGap = 295;
        float height = this.getHeight();
        this.logPanel.setHeight(height - yGap, this.getHeightUnits());        
        this.messageHeaderPanel.setHeight(height - yGap, this.getHeightUnits());
        this.rawMessageDecryptedPanel.setHeight(height - yGap, this.getHeightUnits());
        for (FilePanel singlePayloadPanel : this.payloadPanel) {
            singlePayloadPanel.setHeight(height - yGap, this.getHeightUnits());
        }
    }

    @Override
    public void init() {
        ICON_IN = new ThemeResource("images/in16x16.gif");
        ICON_OUT = new ThemeResource("images/out16x16.gif");
        ICON_MESSAGE = new ThemeResource("images/message16x16.gif");
        ICON_SIGNAL_OK = new ThemeResource("images/signal_ok16x16.gif");
        ICON_SIGNAL_FAILURE = new ThemeResource("images/signal_failure16x16.gif");
        //load resource bundle
        try {
            this.rbMessage = (MecResourceBundle) ResourceBundle.getBundle(
                    ResourceBundleAS2Message.class.getName(), Locale.ENGLISH);
        } //load up  resourcebundle
        catch (MissingResourceException e) {
            throw new RuntimeException("Oops..resource bundle " + e.getClassName() + " not found.");
        }
        this.logAccess = new LogAccessDB(this.configConnection, this.runtimeConnection);
        this.messageAccess = new MessageAccessDB(this.configConnection, this.runtimeConnection);
        this.payload = TransactionDetailsDialog.this.messageAccess.getPayload(TransactionDetailsDialog.this.messageId);
        super.init();
        this.refreshTableContent();
        this.rescalePanels();
    }

    /**Could be overwritten, contains the content to display*/
    @Override
    public AbstractComponent getContentPanel() {
        Panel panel = new Panel();
        VerticalLayout layout = new VerticalLayout();
        this.detailsTable = this.createDetailsTable();
        layout.addComponent(this.detailsTable);
        this.tabSheet = this.createTabSheet();
        layout.addComponent(this.tabSheet);
        layout.setSizeFull();
        panel.addComponent(layout);
        panel.setSizeFull();
        return (panel);
    }

    private TabSheet createTabSheet() {
        TabSheet tabsheet = new TabSheet();
        this.logPanel = this.createLogTab();
        tabsheet.addTab(this.logPanel, "Log", null);
        this.rawMessageDecryptedPanel = this.createRawMessageDecryptedPanel();
        tabsheet.addTab(rawMessageDecryptedPanel, "Raw message decrypted", null);
        this.messageHeaderPanel = this.createMessageHeaderPanel();
        tabsheet.addTab(this.messageHeaderPanel, "Message header", null);
        this.payloadPanel = this.createPayloadPanel();
        for (int i = 0; i < this.payloadPanel.length; i++) {
            String tabTitle = "Payload";
            if (this.payloadPanel.length > 1) {
                tabTitle += " " + (i + 1);
            }
            tabsheet.addTab(this.payloadPanel[i], tabTitle, null);
        }
        tabsheet.setSizeFull();
        return (tabsheet);
    }

    private TextArea createLogTab() {
        DateFormat format = SimpleDateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
        TextArea textArea = new TextArea();
        textArea.setRows(7);
        textArea.setSizeFull();
        LogEntry[] entries = this.logAccess.getLog(this.messageId);
        StringBuilder log = new StringBuilder();
        for (LogEntry entry : entries) {
            log.append("[").append(format.format(entry.getMillis())).append("] ");
            log.append(entry.getMessage());
            log.append("\n");
        }
        textArea.setValue(log.toString());
        textArea.setReadOnly(true);
        return (textArea);
    }

    private FilePanel createRawMessageDecryptedPanel() {
        FilePanel panel = new FilePanel();
        panel.setReadOnly(true);
        return (panel);
    }

    private FilePanel createMessageHeaderPanel() {
        FilePanel panel = new FilePanel();
        panel.setReadOnly(true);
        return (panel);
    }

    private FilePanel[] createPayloadPanel() {
        FilePanel[] panel = new FilePanel[this.payload.size()];        
        for( int i = 0; i < panel.length; i++ ){
            panel[i] = new FilePanel();
            panel[i].setReadOnly(true);
        }
        return (panel);
    }

    private Table createDetailsTable() {
        Table table = new Table();
        table.setSizeFull();
        table.setPageLength(3);
        table.setSelectable(true);
        table.setSortDisabled(true);
        // Send changes in selection immediately to server.
        table.setImmediate(true);
        // Handle selection change.
        table.addListener(new Property.ValueChangeListener() {

            @Override
            public void valueChange(ValueChangeEvent event) {
                AS2Info info = (AS2Info) TransactionDetailsDialog.this.detailsTable.getValue();
                File rawFile = null;
                if (!info.isMDN()) {
                    AS2MessageInfo messageInfo = (AS2MessageInfo) info;
                    if (messageInfo.getRawFilenameDecrypted() != null) {
                        rawFile = new File(messageInfo.getRawFilenameDecrypted());
                    } else if (messageInfo.getRawFilename() != null) {
                        rawFile = new File(messageInfo.getRawFilename());
                    }
                } else if (info.isMDN()) {
                    AS2MDNInfo mdnInfo = (AS2MDNInfo) info;
                    if (mdnInfo.getRawFilename() != null) {
                        rawFile = new File(mdnInfo.getRawFilename());
                    }
                }                
                TransactionDetailsDialog.this.rawMessageDecryptedPanel.displayFile(rawFile);
                File headerFile = null;
                if (info.getHeaderFilename() != null) {
                    headerFile = new File(info.getHeaderFilename());
                }
                TransactionDetailsDialog.this.messageHeaderPanel.displayFile(headerFile);
                try {
                    if (TransactionDetailsDialog.this.payload.size() > 0) {
                        for (int i = 0; i < TransactionDetailsDialog.this.payload.size(); i++) {
                            File payloadFile = new File(TransactionDetailsDialog.this.payload.get(i).getPayloadFilename());
                            TransactionDetailsDialog.this.payloadPanel[i].displayFile(payloadFile);
                        }
                    }
                } catch (Exception ex) {
                    TransactionDetailsDialog.this.showNotification("Problem:",
                            ex.getClass().getName() + " " + ex.getMessage(),
                            Window.Notification.TYPE_WARNING_MESSAGE);
                }
            }
        });
        //disallows unselection of a line
        table.setNullSelectionAllowed(false);
        table.addStyleName("components-inside");
        table.setSizeFull();
        table.addContainerProperty(" ", Label.class, null);
        table.addContainerProperty("Date", String.class, null);
        table.addContainerProperty("  ", Label.class, null);
        table.addContainerProperty("Security", String.class, null);
        table.addContainerProperty("Sender", String.class, null);
        table.addContainerProperty("Server", String.class, null);
        return (table);

    }

    private void refreshTableContent() {
        try {
            //add the content
            List<AS2Info> infoList = this.messageAccess.getMessageDetails(this.messageId);
            AS2Info objectToSelect = null;
            for (int i = 0; i < infoList.size(); i++) {
                AS2Info info = infoList.get(i);
                DateFormat format = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
                ThemeResource directionIcon = null;
                if (info.getDirection() == AS2MessageInfo.DIRECTION_IN) {
                    directionIcon = ICON_IN;
                } else {
                    directionIcon = ICON_OUT;
                }
                ThemeResource stateIcon = null;
                if (info.isMDN()) {
                    if (info.getState() == AS2Message.STATE_FINISHED) {
                        stateIcon = ICON_SIGNAL_OK;
                    } else {
                        stateIcon = ICON_SIGNAL_FAILURE;
                    }
                } else {
                    stateIcon = ICON_MESSAGE;
                }
                StringBuilder builderSecurity = new StringBuilder();
                builderSecurity.append(this.rbMessage.getResourceString("signature." + info.getSignType()));
                if (!info.isMDN()) {
                    builderSecurity.append("/");
                    builderSecurity.append(this.rbMessage.getResourceString("encryption." + info.getEncryptionType()));
                }
                String security = builderSecurity.toString();
                String sender = "";
                if (info.getSenderHost() != null) {
                    sender = info.getSenderHost();
                }
                String server = "";
                if (info.getUserAgent() != null) {
                    server = info.getUserAgent();
                }
                this.detailsTable.addItem(new Object[]{
                            this.generateImageLabel(directionIcon),
                            format.format(info.getInitDate()),
                            this.generateImageLabel(stateIcon),
                            security,
                            sender,
                            server
                        }, info);
                if (objectToSelect == null) {
                    objectToSelect = info;
                }
            }
            if (objectToSelect != null) {
                this.detailsTable.select(objectToSelect);
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.showNotification("Problem:",
                    e.getClass().getName() + " " + e.getMessage(),
                    Window.Notification.TYPE_WARNING_MESSAGE);
        }
    }

    private Label generateImageLabel(ThemeResource resource) {
        Label label = new Label();
        label.setContentMode(Label.CONTENT_XHTML);
        label.setIcon(resource);
        label.setValue("<img src=\"" + themeURI + resource.getResourceId() + "\" />");
        return (label);
    }
}
