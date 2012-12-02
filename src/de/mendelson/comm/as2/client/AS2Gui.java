//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/client/AS2Gui.java,v 1.1 2012/04/18 14:10:23 heller Exp $
package de.mendelson.comm.as2.client;

import de.mendelson.comm.as2.AS2ServerVersion;
import de.mendelson.comm.as2.client.about.AboutDialog;
import de.mendelson.comm.as2.client.manualsend.JDialogManualSend;
import de.mendelson.comm.as2.clientserver.message.DeleteMessageRequest;
import de.mendelson.comm.as2.clientserver.message.RefreshClientCEMDisplay;
import de.mendelson.comm.as2.clientserver.message.RefreshClientMessageOverviewList;
import de.mendelson.util.security.cert.clientserver.RefreshKeystoreCertificates;
import de.mendelson.comm.as2.clientserver.message.RefreshTablePartnerData;
import de.mendelson.comm.as2.database.DBDriverManager;
import de.mendelson.comm.as2.importexport.ConfigurationExportRequest;
import de.mendelson.comm.as2.importexport.ConfigurationExportResponse;
import de.mendelson.comm.as2.importexport.JDialogImportConfiguration;
import de.mendelson.comm.as2.message.AS2Info;
import de.mendelson.comm.as2.message.AS2Message;
import de.mendelson.comm.as2.message.AS2MessageInfo;
import de.mendelson.comm.as2.message.AS2Payload;
import de.mendelson.comm.as2.message.MessageAccessDB;
import de.mendelson.comm.as2.message.MessageOverviewFilter;
import de.mendelson.comm.as2.message.loggui.DialogMessageDetails;
import de.mendelson.comm.as2.message.loggui.TableModelMessageOverview;
import de.mendelson.comm.as2.partner.Partner;
import de.mendelson.comm.as2.partner.PartnerAccessDB;
import de.mendelson.comm.as2.partner.gui.JDialogPartnerConfig;
import de.mendelson.comm.as2.preferences.JDialogPreferences;
import de.mendelson.comm.as2.preferences.PreferencesAS2;
import de.mendelson.comm.as2.preferences.PreferencesPanel;
import de.mendelson.comm.as2.preferences.PreferencesPanelDirectories;
import de.mendelson.comm.as2.preferences.PreferencesPanelMDN;
import de.mendelson.comm.as2.preferences.PreferencesPanelNotification;
import de.mendelson.comm.as2.preferences.PreferencesPanelProxy;
import de.mendelson.comm.as2.preferences.PreferencesPanelSecurity;
import de.mendelson.comm.as2.preferences.PreferencesPanelSystemMaintenance;
import de.mendelson.comm.as2.server.AS2Server;
import de.mendelson.util.*;
import de.mendelson.util.clientserver.ClientsideMessageProcessor;
import de.mendelson.util.clientserver.GUIClient;
import de.mendelson.util.clientserver.clients.datatransfer.DownloadRequestFile;
import de.mendelson.util.clientserver.clients.datatransfer.DownloadResponseFile;
import de.mendelson.util.clientserver.clients.datatransfer.TransferClient;
import de.mendelson.util.clientserver.clients.datatransfer.TransferClientWithProgress;
import de.mendelson.util.clientserver.messages.ClientServerMessage;
import de.mendelson.util.clientserver.messages.ServerInfo;
import de.mendelson.util.clientserver.user.User;
import de.mendelson.util.log.panel.LogConsolePanel;
import de.mendelson.util.tables.JTableColumnResizer;
import de.mendelson.util.tables.TableCellRendererDate;
import java.awt.Desktop;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.sql.Connection;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import oracle.help.Help;
import oracle.help.library.helpset.HelpSet;
/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software. Other product
 * and brand names are trademarks of their respective owners.
 */
import org.apache.http.Header;

/**
 * Main GUI for the control of the mendelson AS2 server
 *
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class AS2Gui extends GUIClient implements ListSelectionListener, RowSorterListener, ClientsideMessageProcessor, MouseListener, PopupMenuListener {

    /**
     * Preferences of the application
     */
    private PreferencesAS2 clientPreferences = new PreferencesAS2();
    private Logger logger = Logger.getLogger("de.mendelson.as2.client");
    /**
     * Resourcebundle to localize the GUI
     */
    private MecResourceBundle rb = null;
    /**
     * actual loaded helpset
     */
    private HelpSet helpSet = null;
    /**
     * Actual help component
     */
    private Help help = null;
    /**
     * Flag to show/hide the filter panel
     */
    private boolean showFilterPanel = false;
    /**
     * download URL for the new version
     */
    private String downloadURLNewVersion = "http://www.mendelson-e-c.com";
    /**
     * DB connection
     */
    private Connection configConnection = null;
    private Connection runtimeConnection = null;
    /**
     * Refresh thread for the transaction overview - schedules the refresh requests
     */
    private RefreshThread refreshThread = new RefreshThread();
    /**
     * Store if the help has been displayed already
     */
    private boolean helpHasBeenDisplayed = false;
    /**
     * Host to connect to
     */
    private String host;

    /**
     * Creates new form NewJFrame
     */
    public AS2Gui(Splash splash, String host) {
        this.host = host;
        //Set System default look and feel
        try {
            //support the command line option -Dswing.defaultlaf=...
            if (System.getProperty("swing.defaultlaf") == null) {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }
        } catch (Exception e) {
            this.logger.warning(this.getClass().getName() + ":" + e.getMessage());
        }
        //load resource bundle
        try {
            this.rb = (MecResourceBundle) ResourceBundle.getBundle(
                    ResourceBundleAS2Gui.class.getName());
        } catch (MissingResourceException e) {
            throw new RuntimeException("Oops..resource bundle " + e.getClassName() + " not found.");
        }
        initComponents();                
        this.jButtonNewVersion.setVisible(false);
        this.jPanelRefreshWarning.setVisible(false);
        //set preference values to the GUI
        this.setBounds(
                this.clientPreferences.getInt(PreferencesAS2.FRAME_X),
                this.clientPreferences.getInt(PreferencesAS2.FRAME_Y),
                this.clientPreferences.getInt(PreferencesAS2.FRAME_WIDTH),
                this.clientPreferences.getInt(PreferencesAS2.FRAME_HEIGHT));
        //ensure to display all messages
        this.getLogger().setLevel(Level.ALL);
        LogConsolePanel consolePanel = new LogConsolePanel(this.getLogger());
        //define the colors for the log levels
        consolePanel.setColor(Level.SEVERE, LogConsolePanel.COLOR_BROWN);
        consolePanel.setColor(Level.WARNING, LogConsolePanel.COLOR_BLUE);
        consolePanel.setColor(Level.INFO, LogConsolePanel.COLOR_BLACK);
        consolePanel.setColor(Level.CONFIG, LogConsolePanel.COLOR_DARK_GREEN);
        consolePanel.setColor(Level.FINE, LogConsolePanel.COLOR_DARK_GREEN);
        consolePanel.setColor(Level.FINER, LogConsolePanel.COLOR_DARK_GREEN);
        consolePanel.setColor(Level.FINEST, LogConsolePanel.COLOR_DARK_GREEN);
        this.jPanelServerLog.add(consolePanel);
        this.setTitle(AS2ServerVersion.getProductName() + " " + AS2ServerVersion.getVersion());
        //initialize the help system if available
        this.initializeJavaHelp();
        this.jTableMessageOverview.getSelectionModel().addListSelectionListener(this);
        this.jTableMessageOverview.getTableHeader().setReorderingAllowed(false);
        //icon columns
        TableColumn column = this.jTableMessageOverview.getColumnModel().getColumn(0);
        column.setMaxWidth(20);
        column.setResizable(false);
        column = this.jTableMessageOverview.getColumnModel().getColumn(1);
        column.setMaxWidth(20);
        column.setResizable(false);
        this.jTableMessageOverview.setDefaultRenderer(Date.class, new TableCellRendererDate(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)));
        //add row sorter
        RowSorter<TableModel> sorter =
                new TableRowSorter<TableModel>(this.jTableMessageOverview.getModel());
        jTableMessageOverview.setRowSorter(sorter);
        sorter.addRowSorterListener(this);
        this.jPanelFilterOverview.setVisible(this.showFilterPanel);
        this.jMenuItemHelpForum.setEnabled(Desktop.isDesktopSupported());
        //destroy splash, possible login screen for the client should come up
        if (splash != null) {
            splash.destroy();
        }
        this.setButtonState();
        this.jTableMessageOverview.addMouseListener(this);
        //popup menu issues
        this.jPopupMenu.setInvoker(this.jScrollPaneMessageOverview);
        this.jPopupMenu.addPopupMenuListener(this);
        super.addMessageProcessor(this);
        //perform the connection to the server
        //warning! this works for localhost only so far
        int clientServerCommPort = this.clientPreferences.getInt(PreferencesAS2.CLIENTSERVER_COMM_PORT);
        if (splash != null) {
            splash.destroy();
        }
        this.browserLinkedPanel.cyleText(
                new String[]{
                    "For additional EDI software to convert and process your data please contact <a href='http://www.mendelson-e-c.com'>mendelson-e-commerce GmbH</a>",
                    "To buy a commercial license please visit the <a href='http://shop.mendelson-e-c.com/'>mendelson online shop</a>",
                    "Most trading partners demand a trusted certificate - Order yours at the <a href='http://ca.mendelson-e-c.com'>mendelson CA</a> now!",
                    "Looking for additional secure data transmission software? Try the <a href='http://oftp2.mendelson-e-c.com'>mendelson OFTP2</a> solution!",
                    "You want to send EDIFACT data from your SAP system? Ask <a href='mailto:info@mendelson.de?subject=Please%20inform%20me%20about%20your%20SAP%20integration%20solutions'>mendelson-e-commerce GmbH</a> for a solution.",
                    "You need a secure FTP solution? <a href='mailto:info@mendelson.de?subject=Please%20inform%20me%20about%20your%20SFTP%20solution'>Ask us</a> for the mendelson SFTP software.",
                    "Convert flat files, EDIFACT, SAP IDos, VDA, inhouse formats? <a href='mailto:info@mendelson.de?subject=Please%20inform%20me%20about%20your%20converter%20solution'>Ask us</a> for the mendelson EDI converter.",
                    "For commercial support of this software please buy a license at <a href='http://as2.mendelson-e-c.com'>the mendelson AS2</a> website.",
                    "Have a look at the <a href='http://www.mendelson-e-c.com/products_mbi.php'>mendelson business integration</a> for a powerful EDI solution.",
                    "The <a href='mailto:info@mendelson.de?subject=Please%20inform%20me%20about%20your%20RosettaNet%20solution'>mendelson RosettaNet solution</a> supports RNIF 1.1 and RNIF 2.0.",
                    "The <a href='http://www.mendelson-e-c.com/products_ide.php'>mendelson converter IDE</a> is the graphical mapper for the mendelson converter.",
                    "To process any XML data and convert it to EDIFACT, VDA, flat files, IDocs and inhouse formats use <a href='http://www.mendelson-e-c.com/products_converter.php'>the mendelson converter</a>.",
                    "To transmit your EDI data via HTTP/S please <a href='mailto:info@mendelson.de?subject=Please%20inform%20me%20about%20your%20HTTPS%20solution'>ask us</a> for the mendelson HTTPS solution.",
                    "If you have questions regarding this product please refer to the <a href='http://community.mendelson-e-c.com/'>mendelson community</a>.",});
        this.connect(new InetSocketAddress(host, clientServerCommPort), 5000);
        Runnable dailyNewsThread = new Runnable() {

            @Override
            public void run() {
                while (true) {
                    long lastUpdateCheck = Long.valueOf(clientPreferences.get(PreferencesAS2.LAST_UPDATE_CHECK));
                    //check only once a day even if the system is started n times a day
                    if (lastUpdateCheck < (System.currentTimeMillis() - TimeUnit.HOURS.toMillis(23))) {
                        clientPreferences.put(PreferencesAS2.LAST_UPDATE_CHECK, String.valueOf(System.currentTimeMillis()));
                        jButtonNewVersion.setVisible(false);
                        String version = (AS2ServerVersion.getVersion() + " " + AS2ServerVersion.getBuild()).replace(' ', '+');
                        Header[] header = htmlPanel.setURL("http://www.mendelson.de/en/mecas2/client_welcome.php?version=" + version,
                                AS2ServerVersion.getProductName() + " " + AS2ServerVersion.getVersion(),
                                new File("start/client_welcome.html"));
                        if (header != null) {
                            String downloadURL = null;
                            String actualBuild = null;
                            for (Header singleHeader : header) {
                                if (singleHeader.getName().equals("x-actual-build")) {
                                    actualBuild = singleHeader.getValue().trim();
                                }
                                if (singleHeader.getName().equals("x-download-url")) {
                                    downloadURL = singleHeader.getValue().trim();
                                }
                            }
                            if (downloadURL != null && actualBuild != null) {
                                try {
                                    int thisBuild = AS2ServerVersion.getBuildNo();
                                    int availableBuild = Integer.valueOf(actualBuild);
                                    if (thisBuild < availableBuild) {
                                        jButtonNewVersion.setVisible(true);
                                    }
                                    downloadURLNewVersion = downloadURL;
                                } catch (Exception e) {
                                    //nop
                                }
                            }
                        }
                    } else {
                        htmlPanel.setPage(new File("start/client_welcome.html"));
                    }
                    try {
                        //check once a day for new update
                        Thread.sleep(TimeUnit.DAYS.toMillis(1));
                    } catch (InterruptedException e) {
                        //nop
                    }
                }
            }
        };        
        Executors.newSingleThreadExecutor().submit(dailyNewsThread);        
        this.as2StatusBar.setConnectedHost(this.host);
    }

    @Override
    public void loginRequestedFromServer() {
        this.performLogin("admin", "admin".toCharArray(), AS2ServerVersion.getFullProductName());
    }

    /**
     * Mainly impossible in the open source version
     */
    @Override
    public void loginFailureIncompatibleClient() {
        System.exit(1);
    }

    @Override
    public Logger getLogger() {
        return (this.logger);
    }

    /**
     * Stores the actual GUIs preferences to restore the GUI at the next program
     * start
     */
    private void savePreferences() {
        this.clientPreferences.putInt(PreferencesAS2.FRAME_X,
                (int) this.getBounds().getX());
        this.clientPreferences.putInt(PreferencesAS2.FRAME_Y,
                (int) this.getBounds().getY());
        this.clientPreferences.putInt(PreferencesAS2.FRAME_WIDTH,
                (int) this.getBounds().getWidth());
        this.clientPreferences.putInt(PreferencesAS2.FRAME_HEIGHT,
                (int) this.getBounds().getHeight());
    }

    /**
     * Initialized a help set by a given name
     */
    private void initializeJavaHelp() {
        try {
            //At the moment only english and german help systems are implemented.
            String filename = null;
            //If the found default is none of them, set the english help as
            //default!
            if (!Locale.getDefault().getLanguage().equals(Locale.GERMANY.getLanguage()) && !Locale.getDefault().getLanguage().equals(Locale.US.getLanguage())) {
                this.getLogger().warning("Sorry, there is no specific HELPSET available for your language, ");
                this.getLogger().warning("the english help will be displayed.");
                filename =
                        "as2help/as2_en.hs";
            } else {
                filename = "as2help/as2_" + Locale.getDefault().getLanguage() + ".hs";
            }

            File helpSetFile = new File(filename);
            URL helpURL = helpSetFile.toURI().toURL();
            this.helpSet = new HelpSet(helpURL);
            this.help = new Help(true, false, true);
            Help.setHelpLocale(Locale.getDefault());
            help.setIconImage(new ImageIcon(getClass().
                    getResource("/de/mendelson/comm/as2/client/questionmark16x16.gif")).getImage());
            help.addBook(helpSet);
            help.setDefaultTopicID("as2_main");
            try {
                URL url = new File("./as2_help_favories.xml").toURI().toURL();
                help.enableFavoritesNavigator(url);
            } catch (MalformedURLException ignore) {
            }
        } catch (Exception e) {
            // could not find it! Disable menu item
            this.getLogger().warning("Helpset not found, helpsystem is disabled!");
            this.jMenuItemHelpSystem.setEnabled(false);
        }

    }

    private void updatePartnerFilter(Partner[] partner) {
        Partner selectedPartner = null;
        if (this.jComboBoxFilterPartner.getSelectedIndex() > 0) {
            selectedPartner = (Partner) this.jComboBoxFilterPartner.getSelectedItem();
        }

        Arrays.sort(partner);
        this.jComboBoxFilterPartner.removeAllItems();
        this.jComboBoxFilterPartner.addItem(this.rb.getResourceString("filter.none"));
        for (Partner singlePartner : partner) {
            this.jComboBoxFilterPartner.addItem(singlePartner);
        }

        if (selectedPartner != null) {
            this.jComboBoxFilterPartner.setSelectedItem(selectedPartner);
        }

        if (this.jComboBoxFilterPartner.getSelectedItem() == null) {
            this.jComboBoxFilterPartner.setSelectedIndex(0);
        }

    }

    /**
     * Scrolls to an entry of the passed table
     *
     * @param table Table to to scroll in
     * @param row Row to ensure visibility
     */
    private void makeRowVisible(JTable table, int row) {
        if (table.getColumnCount() == 0) {
            return;
        }
        if (row < 0 || row >= table.getRowCount()) {
            throw new IllegalArgumentException(
                    "Requested ensure visible of row " + String.valueOf(row) + ", table has only " + table.getRowCount() + " rows.");
        }
        Rectangle visible = table.getVisibleRect();
        Rectangle cell = table.getCellRect(row, 0, true);
        if (cell.y < visible.y) {
            visible.y = cell.y;
            table.scrollRectToVisible(visible);
        } else if (cell.y + cell.height > visible.y + visible.height) {
            visible.y = cell.y + cell.height - visible.height;
            table.scrollRectToVisible(visible);
        }
    }

    /**
     * Displays details for the selected msg row
     */
    private void showSelectedRowDetails() {
        if (this.runtimeConnection == null) {
            return;
        }
        final String uniqueId = this.getClass().getName() + ".showSelectedRowDetails." + System.currentTimeMillis();
        Runnable runnable = new Runnable() {

            @Override
            public void run() {

                try {
                    AS2Gui.this.jButtonMessageDetails.setEnabled(false);
                    AS2Gui.this.as2StatusBar.startProgressIndeterminate(
                            AS2Gui.this.rb.getResourceString("details"), uniqueId);
                    int selectedRow = AS2Gui.this.jTableMessageOverview.getSelectedRow();
                    if (selectedRow >= 0) {
                        AS2Message message = ((TableModelMessageOverview) AS2Gui.this.jTableMessageOverview.getModel()).getRow(selectedRow);
                        AS2MessageInfo info = (AS2MessageInfo) message.getAS2Info();
                        //download the full payload from the server
                        MessageAccessDB messageAccess = new MessageAccessDB(AS2Gui.this.configConnection, AS2Gui.this.runtimeConnection);
                        List<AS2Payload> payloads = messageAccess.getPayload(info.getMessageId());
                        message.setPayloads(payloads);
                        DialogMessageDetails dialog = new DialogMessageDetails(AS2Gui.this,
                                AS2Gui.this.configConnection,
                                AS2Gui.this.runtimeConnection, AS2Gui.this.getBaseClient(),
                                info,
                                message.getPayloads());
                        AS2Gui.this.as2StatusBar.stopProgressIfExists(uniqueId);
                        dialog.setVisible(true);
                    }
                } catch (Exception e) {
                    //nop
                } finally {
                    AS2Gui.this.as2StatusBar.stopProgressIfExists(uniqueId);
                    AS2Gui.this.setButtonState();
                }
            }
        };
        Executors.newSingleThreadExecutor().submit(runnable);
    }

    /**
     * Enables/disables the buttons of this GUI
     */
    private void setButtonState() {
        this.jButtonMessageDetails.setEnabled(this.jTableMessageOverview.getSelectedRow() >= 0);
        this.jButtonDeleteMessage.setEnabled(this.jTableMessageOverview.getSelectedRow() >= 0);
        //check if min one of the selected rows has the state "stopped" or "finished"
        int[] selectedRows = this.jTableMessageOverview.getSelectedRows();
        AS2Message[] overviewRows = ((TableModelMessageOverview) this.jTableMessageOverview.getModel()).getRows(selectedRows);
        boolean deletableRowSelected = false;
        for (int i = 0; i
                < overviewRows.length; i++) {
            AS2MessageInfo info = (AS2MessageInfo) overviewRows[i].getAS2Info();
            if (info.getState() == AS2Message.STATE_FINISHED || info.getState() == AS2Message.STATE_STOPPED) {
                deletableRowSelected = true;
                break;

            }


        }
        this.jButtonDeleteMessage.setEnabled(deletableRowSelected);
        ImageIcon iconBase = new ImageIcon(this.getClass().
                getResource("/de/mendelson/comm/as2/client/filter16x16.gif"));
        if (this.filterIsSet()) {
            ImageUtil imageUtil = new ImageUtil();
            ImageIcon iconActive = new ImageIcon(this.getClass().
                    getResource("/de/mendelson/comm/as2/client/mini_filteractive.gif"));
            this.jButtonFilter.setIcon(imageUtil.mixImages(iconBase, iconActive));
        } else {
            this.jButtonFilter.setIcon(iconBase);
        }

    }

    /**
     * Returns if a filter is set on the message overview entries
     */
    private boolean filterIsSet() {
        return (!this.jCheckBoxFilterShowOk.isSelected() || !this.jCheckBoxFilterShowPending.isSelected() || !this.jCheckBoxFilterShowStopped.isSelected() || this.jComboBoxFilterPartner.getSelectedIndex() > 0);
    }

    /**
     * Makes this a ListSelectionListener
     */
    @Override
    public void valueChanged(ListSelectionEvent listSelectionEvent) {
        this.as2StatusBar.setSelectedTransactionCount(this.jTableMessageOverview.getSelectedRowCount());
        this.setButtonState();
    }

    /**
     * Deletes the actual selected AS2 rows from the database, filesystem etc
     */
    private void deleteSelectedMessages() {
        if (this.runtimeConnection == null) {
            return;
        }
        int requestValue = JOptionPane.showConfirmDialog(
                this, this.rb.getResourceString("dialog.msg.delete.message"),
                this.rb.getResourceString("dialog.msg.delete.title"),
                JOptionPane.YES_NO_OPTION);
        if (requestValue != JOptionPane.YES_OPTION) {
            return;
        }
        int[] selectedRows = this.jTableMessageOverview.getSelectedRows();
        AS2Message[] overviewRows = ((TableModelMessageOverview) this.jTableMessageOverview.getModel()).getRows(selectedRows);
        List<AS2MessageInfo> deleteList = new ArrayList<AS2MessageInfo>();
        for (AS2Message message : overviewRows) {
            deleteList.add((AS2MessageInfo) message.getAS2Info());
        }
        DeleteMessageRequest request = new DeleteMessageRequest();
        request.setDeleteList(deleteList);
        this.getBaseClient().sendAsync(request);
    }

    /**
     * Starts a dialog that allows to send files manual to a partner
     */
    private void sendFileManual() {
        if (this.configConnection == null) {
            return;
        }
        JDialogManualSend dialog = new JDialogManualSend(this, this.configConnection,
                this.runtimeConnection,
                this.getBaseClient(), this.as2StatusBar,
                this.rb.getResourceString("uploading.to.server"));
        dialog.setVisible(true);
    }

    /**
     * The client received a message from the server
     */
    @Override
    public boolean processMessageFromServer(ClientServerMessage message) {
        if (message instanceof RefreshClientMessageOverviewList) {
            RefreshClientMessageOverviewList refreshRequest = (RefreshClientMessageOverviewList) message;
            if (refreshRequest.getOperation() == RefreshClientMessageOverviewList.OPERATION_PROCESSING_UPDATE) {
                this.refreshThread.serverRequestsOverviewRefresh();
            } else {
                //always perform update of a delete operation - even if the refresh has been disabled
                this.refreshThread.userRequestsOverviewRefresh();
            }
            return (true);
        } else if (message instanceof RefreshTablePartnerData) {
            this.refreshThread.requestPartnerRefresh();
            return (true);
        } else if (message instanceof ServerInfo) {
            ServerInfo serverInfo = (ServerInfo) message;
            this.getLogger().log(Level.CONFIG, serverInfo.getProductname());
            return (true);
        } else if (message instanceof RefreshClientCEMDisplay) {
            //return true for this message even if it is not processed here to prevent a
            //warning that the message was not processed
            return (true);
        }
        return (false);
    }

    @Override
    public void loggedIn(User user) {
        super.loggedIn(user);
        try {
            this.configConnection = DBDriverManager.getConnectionWithoutErrorHandling(
                    DBDriverManager.DB_CONFIG, this.host);
            this.runtimeConnection = DBDriverManager.getConnectionWithoutErrorHandling(
                    DBDriverManager.DB_RUNTIME, this.host);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(AS2Gui.this,
                    this.rb.getResourceString("dbconnection.failed.message", e.getMessage()),
                    this.rb.getResourceString("dbconnection.failed.title"), JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        this.as2StatusBar.setConnectedHost(this.host);
        //start the table update thread
        Executors.newSingleThreadExecutor().submit(this.refreshThread);
    }

    /**
     * Makes this a RowSorterListener, workaround for the bug that the selected
     * row will change to a random one after the sort process
     */
    @Override
    public void sorterChanged(RowSorterEvent e) {
        if (e.getType().equals(RowSorterEvent.Type.SORTED)) {
            this.jTableMessageOverview.getSelectionModel().clearSelection();
            this.setButtonState();
        }
    }

     /**Exports the configuration*/
    private void performConfigurationExport() {
        //prevent this action if the user is not logged in
        if (this.configConnection == null) {
            return;
        }
        MecFileChooser chooser = new MecFileChooser(
                this, this.rb.getResourceString("filechooser.export"));
        String exportFilename = chooser.browseFilename();
        if (exportFilename != null) {
            InputStream inStream = null;
            OutputStream outStream = null;
            try {
                TransferClient transferClient = new TransferClient(this.getBaseClient());
                ConfigurationExportResponse response = (ConfigurationExportResponse) transferClient.download(new ConfigurationExportRequest());
                if (response.getException() != null) {
                    throw response.getException();
                }
                outStream = new FileOutputStream(exportFilename);
                inStream = response.getDataStream();
                this.copyStreams(inStream, outStream);
                outStream.flush();
                this.getLogger().fine(this.rb.getResourceString("export.success",
                        new File(exportFilename).getAbsolutePath()));
            } catch (Throwable e) {
                this.getLogger().severe("ConfigurationExport: " + e.getMessage());
            } finally {
                if (inStream != null) {
                    try {
                        inStream.close();
                    } catch (Exception e) {
                    }
                }
                if (outStream != null) {
                    try {
                        outStream.close();
                    } catch (Exception e) {
                    }
                }
            }
        }
    }

    /**
     * Copies all data from one stream to another
     */
    private void copyStreams(InputStream in, OutputStream out) throws IOException {
        BufferedInputStream inStream = new BufferedInputStream(in);
        BufferedOutputStream outStream = new BufferedOutputStream(out);
        //copy the contents to an output stream
        byte[] buffer = new byte[1024];
        int read = 1024;
        //a read of 0 must be allowed, sometimes it takes time to
        //extract data from the input
        while (read != -1) {
            read = inStream.read(buffer);
            if (read > 0) {
                outStream.write(buffer, 0, read);
            }
        }
        outStream.flush();
    }

    /**Imports the configuration*/
    private void performConfigurationImport() {
        MecFileChooser chooser = new MecFileChooser(
                this,
                this.rb.getResourceString("filechooser.import"));
        String importFilename = chooser.browseFilename();
        if (importFilename != null) {
            try {
                JDialogImportConfiguration dialog = new JDialogImportConfiguration(this, importFilename,
                        this.configConnection, this.runtimeConnection,
                        this.getBaseClient());
                dialog.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, e.getMessage(), "Error during import", JOptionPane.ERROR_MESSAGE);
                this.getLogger().severe(e.getMessage());
            }
        }
    }

   /**Starts a dialog that allows to send files manual to a partner
     */
    private void sendFileManualFromSelectedTransaction() {
        if (this.configConnection == null) {
            return;
        }
        int requestValue = JOptionPane.showConfirmDialog(
                this, this.rb.getResourceString("dialog.resend.message"),
                this.rb.getResourceString("dialog.resend.title"),
                JOptionPane.YES_NO_OPTION);
        if (requestValue != JOptionPane.YES_OPTION) {
            return;
        }
        final String uniqueId = this.getClass().getName() + ".sendFileManualFromSelectedTransaction." + System.currentTimeMillis();
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                File tempFile = null;
                try {
                    AS2Gui.this.as2StatusBar.startProgressIndeterminate(AS2Gui.this.rb.getResourceString("menu.file.resend"), uniqueId);
                    int selectedRow = AS2Gui.this.jTableMessageOverview.getSelectedRow();
                    if (selectedRow >= 0) {
                        //download the payload for the selected message
                        MessageAccessDB messageAccess = new MessageAccessDB(AS2Gui.this.configConnection, AS2Gui.this.runtimeConnection);
                        JDialogManualSend dialog = new JDialogManualSend(AS2Gui.this,
                                AS2Gui.this.configConnection,
                                AS2Gui.this.runtimeConnection,
                                AS2Gui.this.getBaseClient(), AS2Gui.this.as2StatusBar,
                                AS2Gui.this.rb.getResourceString("uploading.to.server"));
                        AS2Message message = ((TableModelMessageOverview) AS2Gui.this.jTableMessageOverview.getModel()).getRow(selectedRow);
                        if (message != null) {
                            AS2MessageInfo info = (AS2MessageInfo) message.getAS2Info();
                            PartnerAccessDB partnerAccess = new PartnerAccessDB(AS2Gui.this.configConnection, AS2Gui.this.runtimeConnection);
                            Partner sender = partnerAccess.getPartner(info.getSenderId());
                            Partner receiver = partnerAccess.getPartner(info.getReceiverId());
                            List<AS2Payload> payloads = messageAccess.getPayload(info.getMessageId());
                            for (AS2Payload payload : payloads) {
                                message.addPayload(payload);
                            }
                            if (message.getPayloadCount() > 0) {
                                AS2Payload payload = message.getPayload(0);
                                //request the payload file from the server
                                TransferClientWithProgress transferClient = new TransferClientWithProgress(AS2Gui.this.getBaseClient(),
                                        AS2Gui.this.as2StatusBar.getProgressPanel());
                                DownloadRequestFile request = new DownloadRequestFile();
                                request.setFilename(payload.getPayloadFilename());
                                InputStream inStream = null;
                                OutputStream outStream = null;
                                try {
                                    DownloadResponseFile response = (DownloadResponseFile) transferClient.download(request);
                                    if (response.getException() != null) {
                                        throw response.getException();
                                    }
                                    String tempFilename = "as2.bin";
                                    if (payload.getOriginalFilename() != null) {
                                        tempFilename = payload.getOriginalFilename();
                                    }
                                    tempFile = AS2Tools.createTempFile(tempFilename, "");
                                    outStream = new FileOutputStream(tempFile);
                                    inStream = response.getDataStream();
                                    AS2Gui.this.copyStreams(inStream, outStream);
                                    outStream.flush();
                                } catch (Throwable e) {
                                    AS2Gui.this.logger.severe(e.getMessage());
                                    return;
                                } finally {
                                    if (inStream != null) {
                                        try {
                                            inStream.close();
                                        } catch (Exception e) {
                                        }
                                    }
                                    if (outStream != null) {
                                        try {
                                            outStream.close();
                                        } catch (Exception e) {
                                        }
                                    }
                                }
                                dialog.initialize(sender, receiver, tempFile.getAbsolutePath());
                            }
                            dialog.performSend();
                            info.setResendCounter(info.getResendCounter() + 1);
                            messageAccess.updateResendCounter(info);
                            Logger.getLogger( AS2Server.SERVER_LOGGER_NAME).log( Level.INFO, AS2Gui.this.rb.getResourceString( "resend.performed"), info);
                        }
                        AS2Gui.this.as2StatusBar.stopProgressIfExists(uniqueId);
                    }
                } catch (Throwable e) {
                    //nop
                } finally {
                    AS2Gui.this.as2StatusBar.stopProgressIfExists(uniqueId);
                    if (tempFile != null) {
                        tempFile.delete();
                    }
                }

            }
        };
        Executors.newSingleThreadExecutor().submit(runnable);
    }

    private void displayPartnerDialog() {
        final String uniqueId = this.getClass().getName() + ".displayPartnerDialog." + System.currentTimeMillis();
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                JDialogPartnerConfig dialog = null;
                try {
                    //display wait indicator
                    AS2Gui.this.as2StatusBar.startProgressIndeterminate(
                            AS2Gui.this.rb.getResourceString("menu.file.partner"), uniqueId);
                    dialog = new JDialogPartnerConfig(AS2Gui.this,
                            AS2Gui.this.configConnection, AS2Gui.this.runtimeConnection,
                            AS2Gui.this,
                            AS2Gui.this.as2StatusBar);
                    dialog.setDisplayNotificationPanel(false);
                    dialog.setDisplayHttpHeaderPanel(false);
                } finally {
                    AS2Gui.this.as2StatusBar.stopProgressIfExists(uniqueId);
                    if (dialog != null) {
                        dialog.setVisible(true);
                    }
                }
            }
        };
        Executors.newSingleThreadExecutor().submit(runnable);
    }

    private void displayHelpSystem() {
        if (this.helpHasBeenDisplayed) {
            this.help.setVisible(true);
        } else {
            final String uniqueId = this.getClass().getName() + ".displayHelpSystem." + System.currentTimeMillis();
            Runnable test = new Runnable() {

                @Override
                public void run() {
                    try {
                        //display wait indicator
                        AS2Gui.this.as2StatusBar.startProgressIndeterminate(AS2Gui.this.rb.getResourceString("menu.help.helpsystem"), uniqueId);
                        AS2Gui.this.help.showTopic(AS2Gui.this.helpSet, "as2_main");
                    } finally {
                        AS2Gui.this.as2StatusBar.stopProgressIfExists(uniqueId);
                        AS2Gui.this.helpHasBeenDisplayed = true;
                    }
                }
            };
            Executors.newSingleThreadExecutor().submit(test);
        }
    }

    private void displayPreferences() {
        final String uniqueId = this.getClass().getName() + ".displayPreferences." + System.currentTimeMillis();
        Runnable test = new Runnable() {

            @Override
            public void run() {
                JDialogPreferences dialog = null;
                try {
                    List<PreferencesPanel> panelList = new ArrayList<PreferencesPanel>();
                    panelList.add(new PreferencesPanelMDN(AS2Gui.this.getBaseClient()));
                    panelList.add(new PreferencesPanelProxy(AS2Gui.this.getBaseClient()));
                    panelList.add(new PreferencesPanelSecurity(AS2Gui.this.getBaseClient()));
                    panelList.add(new PreferencesPanelDirectories(AS2Gui.this.getBaseClient()));
                    panelList.add(new PreferencesPanelSystemMaintenance(AS2Gui.this.getBaseClient()));
                    panelList.add(new PreferencesPanelNotification(AS2Gui.this.getBaseClient()));                    
                    //display wait indicator
                    AS2Gui.this.as2StatusBar.startProgressIndeterminate(
                            AS2Gui.this.rb.getResourceString("menu.file.preferences"), uniqueId);
                    dialog = new JDialogPreferences(AS2Gui.this, AS2Gui.this.configConnection,
                            AS2Gui.this.runtimeConnection,
                            panelList);
                } finally {
                    AS2Gui.this.as2StatusBar.stopProgressIfExists(uniqueId);
                    if (dialog != null) {
                        dialog.setVisible(true);
                    }
                }
            }
        };
        Executors.newSingleThreadExecutor().submit(test);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPopupMenu = new javax.swing.JPopupMenu();
        jMenuItemPopupMessageDetails = new javax.swing.JMenuItem();
        jMenuItemPopupSendAgain = new javax.swing.JMenuItem();
        jSeparator9 = new javax.swing.JPopupMenu.Separator();
        jMenuItemPopupDeleteMessage = new javax.swing.JMenuItem();
        jTabbedPane = new javax.swing.JTabbedPane();
        jPanelLog = new javax.swing.JPanel();
        jToolBar = new javax.swing.JToolBar();
        jButtonPartner = new javax.swing.JButton();
        jButtonMessageDetails = new javax.swing.JButton();
        jButtonFilter = new javax.swing.JButton();
        jToggleButtonStopRefresh = new javax.swing.JToggleButton();
        jSeparator1 = new javax.swing.JSeparator();
        jButtonDeleteMessage = new javax.swing.JButton();
        jPanelMain = new javax.swing.JPanel();
        jSplitPane = new javax.swing.JSplitPane();
        jPanelMessageLog = new javax.swing.JPanel();
        jPanelFilterOverview = new javax.swing.JPanel();
        jCheckBoxFilterShowOk = new javax.swing.JCheckBox();
        jCheckBoxFilterShowPending = new javax.swing.JCheckBox();
        jCheckBoxFilterShowStopped = new javax.swing.JCheckBox();
        jLabelFilterShowOk = new javax.swing.JLabel();
        jLabelFilterShowPending = new javax.swing.JLabel();
        jLabelFilterShowError = new javax.swing.JLabel();
        jButtonHideFilter = new javax.swing.JButton();
        jComboBoxFilterPartner = new javax.swing.JComboBox();
        jPanelSpace = new javax.swing.JPanel();
        jLabelFilterPartner = new javax.swing.JLabel();
        jSeparator4 = new javax.swing.JSeparator();
        jScrollPaneMessageOverview = new javax.swing.JScrollPane();
        jTableMessageOverview = new de.mendelson.util.tables.JTableSortable();
        jPanelServerLog = new javax.swing.JPanel();
        jPanelRefreshWarning = new javax.swing.JPanel();
        jLabelRefreshStopWarning = new javax.swing.JLabel();
        htmlPanel = new de.mendelson.comm.as2.client.HTMLPanel();
        jPanelInfo = new javax.swing.JPanel();
        jButtonNewVersion = new javax.swing.JButton();
        browserLinkedPanel = new de.mendelson.comm.as2.client.BrowserLinkedPanel();
        as2StatusBar = new de.mendelson.comm.as2.client.AS2StatusBar();
        jMenuBar = new javax.swing.JMenuBar();
        jMenuFile = new javax.swing.JMenu();
        jMenuItemManualSend = new javax.swing.JMenuItem();
        jMenuItemKeyRefresh = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JSeparator();
        jMenuItemFilePreferences = new javax.swing.JMenuItem();
        jMenuItemPartner = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JSeparator();
        jMenuItemExportConfig = new javax.swing.JMenuItem();
        jMenuItemExportImport = new javax.swing.JMenuItem();
        jSeparator6 = new javax.swing.JSeparator();
        jMenuItemFileExit = new javax.swing.JMenuItem();
        jMenuHelp = new javax.swing.JMenu();
        jMenuItemHelpAbout = new javax.swing.JMenuItem();
        jSeparator5 = new javax.swing.JSeparator();
        jMenuItemHelpShop = new javax.swing.JMenuItem();
        jMenuItemHelpForum = new javax.swing.JMenuItem();
        jMenuItemHelpSystem = new javax.swing.JMenuItem();

        jMenuItemPopupMessageDetails.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/mendelson/comm/as2/client/messagedetails16x16.gif"))); // NOI18N
        jMenuItemPopupMessageDetails.setText(this.rb.getResourceString( "details" ));
        jMenuItemPopupMessageDetails.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemPopupMessageDetailsActionPerformed(evt);
            }
        });
        jPopupMenu.add(jMenuItemPopupMessageDetails);

        jMenuItemPopupSendAgain.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/mendelson/comm/as2/client/send_16x16.gif"))); // NOI18N
        jMenuItemPopupSendAgain.setText(this.rb.getResourceString("menu.file.resend"));
        jMenuItemPopupSendAgain.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemPopupSendAgainActionPerformed(evt);
            }
        });
        jPopupMenu.add(jMenuItemPopupSendAgain);
        jPopupMenu.add(jSeparator9);

        jMenuItemPopupDeleteMessage.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/mendelson/comm/as2/client/delete_16x16.gif"))); // NOI18N
        jMenuItemPopupDeleteMessage.setText(this.rb.getResourceString( "delete.msg"));
        jMenuItemPopupDeleteMessage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemPopupDeleteMessageActionPerformed(evt);
            }
        });
        jPopupMenu.add(jMenuItemPopupDeleteMessage);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setIconImage(new ImageIcon( AS2Gui.class.getResource( "/de/mendelson/comm/as2/client/os_logo16x16.gif")).getImage());
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jPanelLog.setLayout(new java.awt.BorderLayout());

        jToolBar.setFloatable(false);
        jToolBar.setRollover(true);

        jButtonPartner.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/mendelson/comm/as2/partner/gui/singlepartner16x16.gif"))); // NOI18N
        jButtonPartner.setText(this.rb.getResourceString( "menu.file.partner" ));
        jButtonPartner.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPartnerActionPerformed(evt);
            }
        });
        jToolBar.add(jButtonPartner);

        jButtonMessageDetails.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/mendelson/comm/as2/client/messagedetails16x16.gif"))); // NOI18N
        jButtonMessageDetails.setText(this.rb.getResourceString( "details" ));
        jButtonMessageDetails.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonMessageDetailsActionPerformed(evt);
            }
        });
        jToolBar.add(jButtonMessageDetails);

        jButtonFilter.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/mendelson/comm/as2/client/filter16x16.gif"))); // NOI18N
        jButtonFilter.setText(this.rb.getResourceString( "filter"));
        jButtonFilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonFilterActionPerformed(evt);
            }
        });
        jToolBar.add(jButtonFilter);

        jToggleButtonStopRefresh.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/mendelson/comm/as2/client/stop_16x16.gif"))); // NOI18N
        jToggleButtonStopRefresh.setText(this.rb.getResourceString( "stoprefresh.msg"));
        jToggleButtonStopRefresh.setFocusable(false);
        jToggleButtonStopRefresh.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToggleButtonStopRefresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButtonStopRefreshActionPerformed(evt);
            }
        });
        jToolBar.add(jToggleButtonStopRefresh);

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jSeparator1.setMaximumSize(new java.awt.Dimension(5, 32767));
        jToolBar.add(jSeparator1);

        jButtonDeleteMessage.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/mendelson/comm/as2/client/delete_16x16.gif"))); // NOI18N
        jButtonDeleteMessage.setText(this.rb.getResourceString( "delete.msg"));
        jButtonDeleteMessage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDeleteMessageActionPerformed(evt);
            }
        });
        jToolBar.add(jButtonDeleteMessage);

        jPanelLog.add(jToolBar, java.awt.BorderLayout.NORTH);

        jPanelMain.setLayout(new java.awt.GridBagLayout());

        jSplitPane.setDividerLocation(300);
        jSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        jPanelMessageLog.setLayout(new java.awt.GridBagLayout());

        jPanelFilterOverview.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanelFilterOverview.setLayout(new java.awt.GridBagLayout());

        jCheckBoxFilterShowOk.setSelected(true);
        jCheckBoxFilterShowOk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxFilterShowOkActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 0);
        jPanelFilterOverview.add(jCheckBoxFilterShowOk, gridBagConstraints);

        jCheckBoxFilterShowPending.setSelected(true);
        jCheckBoxFilterShowPending.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxFilterShowPendingActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 0);
        jPanelFilterOverview.add(jCheckBoxFilterShowPending, gridBagConstraints);

        jCheckBoxFilterShowStopped.setSelected(true);
        jCheckBoxFilterShowStopped.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxFilterShowStoppedActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 0);
        jPanelFilterOverview.add(jCheckBoxFilterShowStopped, gridBagConstraints);

        jLabelFilterShowOk.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/mendelson/comm/as2/client/state_finished16x16.gif"))); // NOI18N
        jLabelFilterShowOk.setText(this.rb.getResourceString( "filter.showfinished"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelFilterOverview.add(jLabelFilterShowOk, gridBagConstraints);

        jLabelFilterShowPending.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/mendelson/comm/as2/client/state_pending16x16.gif"))); // NOI18N
        jLabelFilterShowPending.setText(this.rb.getResourceString( "filter.showpending"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelFilterOverview.add(jLabelFilterShowPending, gridBagConstraints);

        jLabelFilterShowError.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/mendelson/comm/as2/client/state_stopped16x16.gif"))); // NOI18N
        jLabelFilterShowError.setText(this.rb.getResourceString( "filter.showstopped"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelFilterOverview.add(jLabelFilterShowError, gridBagConstraints);

        jButtonHideFilter.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/mendelson/comm/as2/client/hide_filter.gif"))); // NOI18N
        jButtonHideFilter.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jButtonHideFilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonHideFilterActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelFilterOverview.add(jButtonHideFilter, gridBagConstraints);

        jComboBoxFilterPartner.setMinimumSize(new java.awt.Dimension(100, 20));
        jComboBoxFilterPartner.setPreferredSize(new java.awt.Dimension(100, 22));
        jComboBoxFilterPartner.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxFilterPartnerActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelFilterOverview.add(jComboBoxFilterPartner, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanelFilterOverview.add(jPanelSpace, gridBagConstraints);

        jLabelFilterPartner.setText(this.rb.getResourceString( "filter.partner"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelFilterOverview.add(jLabelFilterPartner, gridBagConstraints);

        jSeparator4.setOrientation(javax.swing.SwingConstants.VERTICAL);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelFilterOverview.add(jSeparator4, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weightx = 1.0;
        jPanelMessageLog.add(jPanelFilterOverview, gridBagConstraints);

        jTableMessageOverview.setModel(new TableModelMessageOverview());
        jTableMessageOverview.setShowHorizontalLines(false);
        jTableMessageOverview.setShowVerticalLines(false);
        jTableMessageOverview.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTableMessageOverviewMouseClicked(evt);
            }
        });
        jScrollPaneMessageOverview.setViewportView(jTableMessageOverview);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanelMessageLog.add(jScrollPaneMessageOverview, gridBagConstraints);

        jSplitPane.setLeftComponent(jPanelMessageLog);

        jPanelServerLog.setLayout(new java.awt.BorderLayout());
        jSplitPane.setRightComponent(jPanelServerLog);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanelMain.add(jSplitPane, gridBagConstraints);

        jPanelRefreshWarning.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 0, 0)));
        jPanelRefreshWarning.setLayout(new java.awt.GridBagLayout());

        jLabelRefreshStopWarning.setFont(new java.awt.Font("Tahoma", 1, 11));
        jLabelRefreshStopWarning.setForeground(new java.awt.Color(204, 51, 0));
        jLabelRefreshStopWarning.setText(this.rb.getResourceString( "warning.refreshstopped"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        jPanelRefreshWarning.add(jLabelRefreshStopWarning, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelMain.add(jPanelRefreshWarning, gridBagConstraints);

        jPanelLog.add(jPanelMain, java.awt.BorderLayout.CENTER);

        jTabbedPane.addTab(this.rb.getResourceString( "tab.transactions"), jPanelLog);
        jTabbedPane.addTab(this.rb.getResourceString( "tab.welcome"), htmlPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(jTabbedPane, gridBagConstraints);

        jPanelInfo.setLayout(new java.awt.GridBagLayout());

        jButtonNewVersion.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/mendelson/comm/as2/client/newversion_16x16.gif"))); // NOI18N
        jButtonNewVersion.setToolTipText(this.rb.getResourceString( "new.version"));
        jButtonNewVersion.setMargin(new java.awt.Insets(2, 5, 2, 5));
        jButtonNewVersion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonNewVersionActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanelInfo.add(jButtonNewVersion, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanelInfo.add(browserLinkedPanel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        jPanelInfo.add(as2StatusBar, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        getContentPane().add(jPanelInfo, gridBagConstraints);

        jMenuFile.setText(this.rb.getResourceString( "menu.file" ));

        jMenuItemManualSend.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/mendelson/comm/as2/client/send_16x16.gif"))); // NOI18N
        jMenuItemManualSend.setText(this.rb.getResourceString( "menu.file.send"));
        jMenuItemManualSend.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemManualSendActionPerformed(evt);
            }
        });
        jMenuFile.add(jMenuItemManualSend);

        jMenuItemKeyRefresh.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/mendelson/util/security/cert/gui/keyrefresh16x16.gif"))); // NOI18N
        jMenuItemKeyRefresh.setText(this.rb.getResourceString( "keyrefresh" ));
        jMenuItemKeyRefresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemKeyRefreshActionPerformed(evt);
            }
        });
        jMenuFile.add(jMenuItemKeyRefresh);
        jMenuFile.add(jSeparator2);

        jMenuItemFilePreferences.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/mendelson/comm/as2/preferences/preferences16x16.gif"))); // NOI18N
        jMenuItemFilePreferences.setText(this.rb.getResourceString( "menu.file.preferences"));
        jMenuItemFilePreferences.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemFilePreferencesActionPerformed(evt);
            }
        });
        jMenuFile.add(jMenuItemFilePreferences);

        jMenuItemPartner.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/mendelson/comm/as2/partner/gui/singlepartner16x16.gif"))); // NOI18N
        jMenuItemPartner.setText(this.rb.getResourceString( "menu.file.partner"));
        jMenuItemPartner.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemPartnerActionPerformed(evt);
            }
        });
        jMenuFile.add(jMenuItemPartner);
        jMenuFile.add(jSeparator3);

        jMenuItemExportConfig.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/mendelson/comm/as2/importexport/export_16x16.gif"))); // NOI18N
        jMenuItemExportConfig.setText(this.rb.getResourceString( "menu.file.export"));
        jMenuItemExportConfig.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemExportConfigActionPerformed(evt);
            }
        });
        jMenuFile.add(jMenuItemExportConfig);

        jMenuItemExportImport.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/mendelson/comm/as2/importexport/import_16x16.gif"))); // NOI18N
        jMenuItemExportImport.setText(this.rb.getResourceString( "menu.file.import"));
        jMenuItemExportImport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemExportImportActionPerformed(evt);
            }
        });
        jMenuFile.add(jMenuItemExportImport);
        jMenuFile.add(jSeparator6);

        jMenuItemFileExit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/mendelson/comm/as2/client/close16x16.gif"))); // NOI18N
        jMenuItemFileExit.setText(this.rb.getResourceString( "menu.file.exit" ));
        jMenuItemFileExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemFileExitActionPerformed(evt);
            }
        });
        jMenuFile.add(jMenuItemFileExit);

        jMenuBar.add(jMenuFile);

        jMenuHelp.setText(this.rb.getResourceString( "menu.help"));

        jMenuItemHelpAbout.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/mendelson/comm/as2/client/os_logo16x16.gif"))); // NOI18N
        jMenuItemHelpAbout.setText(this.rb.getResourceString( "menu.help.about"));
        jMenuItemHelpAbout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemHelpAboutActionPerformed(evt);
            }
        });
        jMenuHelp.add(jMenuItemHelpAbout);
        jMenuHelp.add(jSeparator5);

        jMenuItemHelpShop.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/mendelson/comm/as2/client/os_logo16x16.gif"))); // NOI18N
        jMenuItemHelpShop.setText(this.rb.getResourceString( "menu.help.shop" ));
        jMenuItemHelpShop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemHelpShopActionPerformed(evt);
            }
        });
        jMenuHelp.add(jMenuItemHelpShop);

        jMenuItemHelpForum.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/mendelson/comm/as2/client/os_logo16x16.gif"))); // NOI18N
        jMenuItemHelpForum.setText(this.rb.getResourceString( "menu.help.forum" ));
        jMenuItemHelpForum.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemHelpForumActionPerformed(evt);
            }
        });
        jMenuHelp.add(jMenuItemHelpForum);

        jMenuItemHelpSystem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/mendelson/comm/as2/client/os_logo16x16.gif"))); // NOI18N
        jMenuItemHelpSystem.setText(this.rb.getResourceString( "menu.help.helpsystem" ));
        jMenuItemHelpSystem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemHelpSystemActionPerformed(evt);
            }
        });
        jMenuHelp.add(jMenuItemHelpSystem);

        jMenuBar.add(jMenuHelp);

        setJMenuBar(jMenuBar);

        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width-749)/2, (screenSize.height-581)/2, 749, 581);
    }// </editor-fold>//GEN-END:initComponents
    private void jMenuItemHelpForumActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemHelpForumActionPerformed
        try {
            URI uri = new URI("http://community.mendelson-e-c.com/forum/as2");
            Desktop.getDesktop().browse(uri);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }//GEN-LAST:event_jMenuItemHelpForumActionPerformed

    private void jMenuItemManualSendActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemManualSendActionPerformed
        this.sendFileManual();
    }//GEN-LAST:event_jMenuItemManualSendActionPerformed

    private void jButtonDeleteMessageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDeleteMessageActionPerformed
        this.deleteSelectedMessages();
    }//GEN-LAST:event_jButtonDeleteMessageActionPerformed

    private void jButtonHideFilterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonHideFilterActionPerformed
        this.showFilterPanel = !this.showFilterPanel;
        this.jPanelFilterOverview.setVisible(this.showFilterPanel);
    }//GEN-LAST:event_jButtonHideFilterActionPerformed

    private void jCheckBoxFilterShowOkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxFilterShowOkActionPerformed
        this.refreshThread.userRequestsOverviewRefresh();
        this.setButtonState();
    }//GEN-LAST:event_jCheckBoxFilterShowOkActionPerformed

    private void jCheckBoxFilterShowPendingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxFilterShowPendingActionPerformed
        this.refreshThread.userRequestsOverviewRefresh();
        this.setButtonState();
    }//GEN-LAST:event_jCheckBoxFilterShowPendingActionPerformed

    private void jCheckBoxFilterShowStoppedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxFilterShowStoppedActionPerformed
        this.refreshThread.userRequestsOverviewRefresh();
        this.setButtonState();
    }//GEN-LAST:event_jCheckBoxFilterShowStoppedActionPerformed

    private void jButtonFilterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonFilterActionPerformed
        this.showFilterPanel = !this.showFilterPanel;
        this.jPanelFilterOverview.setVisible(this.showFilterPanel);
    }//GEN-LAST:event_jButtonFilterActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        this.savePreferences();
        try {
            if (this.configConnection != null) {
                this.configConnection.close();
            }
        } catch (Exception e) {
            //nop
        }
        try {
            if (this.runtimeConnection != null) {
                this.runtimeConnection.close();
            }
        } catch (Exception e) {
            //nop
        }
    }//GEN-LAST:event_formWindowClosing

    private void jButtonPartnerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPartnerActionPerformed
        this.displayPartnerDialog();
        this.refreshThread.requestPartnerRefresh();
    }//GEN-LAST:event_jButtonPartnerActionPerformed

    private void jButtonMessageDetailsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonMessageDetailsActionPerformed
        this.showSelectedRowDetails();
    }//GEN-LAST:event_jButtonMessageDetailsActionPerformed

    private void jTableMessageOverviewMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTableMessageOverviewMouseClicked
        //double click on a row
        if (evt.getClickCount() == 2) {
            this.showSelectedRowDetails();
        }
    }//GEN-LAST:event_jTableMessageOverviewMouseClicked

    private void jMenuItemFilePreferencesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemFilePreferencesActionPerformed
        this.displayPreferences();
    }//GEN-LAST:event_jMenuItemFilePreferencesActionPerformed

    private void jMenuItemPartnerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemPartnerActionPerformed
        this.displayPartnerDialog();
        this.refreshThread.requestPartnerRefresh();
    }//GEN-LAST:event_jMenuItemPartnerActionPerformed

    private void jMenuItemHelpSystemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemHelpSystemActionPerformed
        this.displayHelpSystem();
    }//GEN-LAST:event_jMenuItemHelpSystemActionPerformed

    private void jMenuItemHelpAboutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemHelpAboutActionPerformed
        AboutDialog dialog = new AboutDialog(this);
        dialog.setVisible(true);
    }//GEN-LAST:event_jMenuItemHelpAboutActionPerformed

    private void jMenuItemFileExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemFileExitActionPerformed
        this.savePreferences();
        this.setVisible(false);
        try {
            if (this.configConnection != null) {
                this.configConnection.close();
            }
        } catch (Exception e) {
            //nop
        }
        try {
            if (this.runtimeConnection != null) {
                this.runtimeConnection.close();
            }
        } catch (Exception e) {
            //nop
        }
        this.logout();
        System.exit(0);
    }//GEN-LAST:event_jMenuItemFileExitActionPerformed

    private void jToggleButtonStopRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButtonStopRefreshActionPerformed
        this.jPanelRefreshWarning.setVisible(this.jToggleButtonStopRefresh.isSelected());
    }//GEN-LAST:event_jToggleButtonStopRefreshActionPerformed

    private void jComboBoxFilterPartnerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxFilterPartnerActionPerformed
        this.refreshThread.userRequestsOverviewRefresh();
        this.setButtonState();
    }//GEN-LAST:event_jComboBoxFilterPartnerActionPerformed

private void jMenuItemKeyRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemKeyRefreshActionPerformed
    //inform the server that the keystore should be reloaded, it has been changed outside
    RefreshKeystoreCertificates signal = new RefreshKeystoreCertificates();
    this.sendAsync(signal);
}//GEN-LAST:event_jMenuItemKeyRefreshActionPerformed

private void jButtonNewVersionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonNewVersionActionPerformed
    try {
        URI uri = new URI(new URL(this.downloadURLNewVersion).toExternalForm());
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().browse(uri);
        }
    } catch (Exception e) {
        this.getLogger().severe(e.getMessage());
    }
}//GEN-LAST:event_jButtonNewVersionActionPerformed

private void jMenuItemExportConfigActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemExportConfigActionPerformed
    this.performConfigurationExport();
}//GEN-LAST:event_jMenuItemExportConfigActionPerformed

private void jMenuItemExportImportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemExportImportActionPerformed
    this.performConfigurationImport();
}//GEN-LAST:event_jMenuItemExportImportActionPerformed

private void jMenuItemPopupMessageDetailsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemPopupMessageDetailsActionPerformed
    this.showSelectedRowDetails();
}//GEN-LAST:event_jMenuItemPopupMessageDetailsActionPerformed

private void jMenuItemPopupSendAgainActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemPopupSendAgainActionPerformed
    this.sendFileManualFromSelectedTransaction();
}//GEN-LAST:event_jMenuItemPopupSendAgainActionPerformed

private void jMenuItemPopupDeleteMessageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemPopupDeleteMessageActionPerformed
    this.deleteSelectedMessages();
}//GEN-LAST:event_jMenuItemPopupDeleteMessageActionPerformed

private void jMenuItemHelpShopActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemHelpShopActionPerformed
    try {
        URI uri = new URI("http://shop.mendelson-e-c.com");
        Desktop.getDesktop().browse(uri);
    } catch (Exception e) {
        e.printStackTrace();
    }
}//GEN-LAST:event_jMenuItemHelpShopActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private de.mendelson.comm.as2.client.AS2StatusBar as2StatusBar;
    private de.mendelson.comm.as2.client.BrowserLinkedPanel browserLinkedPanel;
    private de.mendelson.comm.as2.client.HTMLPanel htmlPanel;
    private javax.swing.JButton jButtonDeleteMessage;
    private javax.swing.JButton jButtonFilter;
    private javax.swing.JButton jButtonHideFilter;
    private javax.swing.JButton jButtonMessageDetails;
    private javax.swing.JButton jButtonNewVersion;
    private javax.swing.JButton jButtonPartner;
    private javax.swing.JCheckBox jCheckBoxFilterShowOk;
    private javax.swing.JCheckBox jCheckBoxFilterShowPending;
    private javax.swing.JCheckBox jCheckBoxFilterShowStopped;
    private javax.swing.JComboBox jComboBoxFilterPartner;
    private javax.swing.JLabel jLabelFilterPartner;
    private javax.swing.JLabel jLabelFilterShowError;
    private javax.swing.JLabel jLabelFilterShowOk;
    private javax.swing.JLabel jLabelFilterShowPending;
    private javax.swing.JLabel jLabelRefreshStopWarning;
    private javax.swing.JMenuBar jMenuBar;
    private javax.swing.JMenu jMenuFile;
    private javax.swing.JMenu jMenuHelp;
    private javax.swing.JMenuItem jMenuItemExportConfig;
    private javax.swing.JMenuItem jMenuItemExportImport;
    private javax.swing.JMenuItem jMenuItemFileExit;
    private javax.swing.JMenuItem jMenuItemFilePreferences;
    private javax.swing.JMenuItem jMenuItemHelpAbout;
    private javax.swing.JMenuItem jMenuItemHelpForum;
    private javax.swing.JMenuItem jMenuItemHelpShop;
    private javax.swing.JMenuItem jMenuItemHelpSystem;
    private javax.swing.JMenuItem jMenuItemKeyRefresh;
    private javax.swing.JMenuItem jMenuItemManualSend;
    private javax.swing.JMenuItem jMenuItemPartner;
    private javax.swing.JMenuItem jMenuItemPopupDeleteMessage;
    private javax.swing.JMenuItem jMenuItemPopupMessageDetails;
    private javax.swing.JMenuItem jMenuItemPopupSendAgain;
    private javax.swing.JPanel jPanelFilterOverview;
    private javax.swing.JPanel jPanelInfo;
    private javax.swing.JPanel jPanelLog;
    private javax.swing.JPanel jPanelMain;
    private javax.swing.JPanel jPanelMessageLog;
    private javax.swing.JPanel jPanelRefreshWarning;
    private javax.swing.JPanel jPanelServerLog;
    private javax.swing.JPanel jPanelSpace;
    private javax.swing.JPopupMenu jPopupMenu;
    private javax.swing.JScrollPane jScrollPaneMessageOverview;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JSeparator jSeparator6;
    private javax.swing.JPopupMenu.Separator jSeparator9;
    private javax.swing.JSplitPane jSplitPane;
    private javax.swing.JTabbedPane jTabbedPane;
    private de.mendelson.util.tables.JTableSortable jTableMessageOverview;
    private javax.swing.JToggleButton jToggleButtonStopRefresh;
    private javax.swing.JToolBar jToolBar;
    // End of variables declaration//GEN-END:variables

    @Override
    public void mouseClicked(MouseEvent evt) {
        if (evt.isPopupTrigger() || evt.isMetaDown()) {
            if (evt.getSource().equals(this.jTableMessageOverview)) {
                this.jPopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
    }

    @Override
    public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
    }

    @Override
    public void popupMenuCanceled(PopupMenuEvent e) {
    }

    /**Checks at fixed interval if a refresh request is available. This prevents a refresh flooding
     * from the server on heavy load
     */
    private class RefreshThread implements Runnable {

        private boolean overviewRefreshRequested = true;
        private boolean partnerRefreshRequested = true;
        private LazyLoaderThread lazyLoader = null;

        @Override
        public void run() {
            boolean firstStart = true;
            while (true) {
                if (this.overviewRefreshRequested) {
                    this.overviewRefreshRequested = false;
                    this.refreshMessageOverviewList();
                }
                if (this.partnerRefreshRequested) {
                    this.partnerRefreshRequested = false;
                    this.refreshTablePartnerData();
                }
                if (firstStart) {
                    firstStart = false;
                    JTableColumnResizer.adjustColumnWidthByContent(AS2Gui.this.jTableMessageOverview);
                }
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (Exception e) {
                }
            }
        }

        public void userRequestsOverviewRefresh() {
            this.overviewRefreshRequested = true;
        }

        public void serverRequestsOverviewRefresh() {
            if (!AS2Gui.this.jToggleButtonStopRefresh.isSelected()) {
                this.overviewRefreshRequested = true;
            }
        }

        public void requestPartnerRefresh() {
            this.partnerRefreshRequested = true;
        }

        /**Reloads the partner ids with their names and passes these information
         *to the overview table. Also refreshes the partner filter.
         *
         */
        public void refreshTablePartnerData() {
            if (AS2Gui.this.configConnection == null) {
                return;
            }
            try {
                PartnerAccessDB partnerAccess = new PartnerAccessDB(AS2Gui.this.configConnection, AS2Gui.this.runtimeConnection);
                Partner[] partner = partnerAccess.getPartner();
                Map<String, Partner> partnerMap = new HashMap<String, Partner>();
                for (int i = 0; i < partner.length; i++) {
                    partnerMap.put(partner[i].getAS2Identification(), partner[i]);
                }
                ((TableModelMessageOverview) AS2Gui.this.jTableMessageOverview.getModel()).passPartner(partnerMap);
                AS2Gui.this.updatePartnerFilter(partner);
            } catch (Exception e) {
                //nop
            }
        }

        /**Loads the payloads for the passed messages in the background*/
        private void lazyloadPayloads(final List<AS2Message> messageList) {
            this.lazyLoader = new LazyLoaderThread(messageList);
            Executors.newSingleThreadExecutor().submit(this.lazyLoader);
        }

        /**Refreshes the message overview list from the database.
         */
        private void refreshMessageOverviewList() {
            //the lazy load process from the last refresh is no longer needed
            if (this.lazyLoader != null) {
                this.lazyLoader.stopLazyLoad();
            }
            final String uniqueId = this.getClass().getName() + ".refreshMessageOverviewList." + System.currentTimeMillis();
            //the sql connection is buld up after the client logged in to the server.
            //if a refresh signal is received during login time a Nullpointer will occur.
            if (AS2Gui.this.runtimeConnection == null) {
                //ignore request, client is not logged in
                return;
            }
            try {
                AS2Gui.this.as2StatusBar.startProgressIndeterminate(
                        AS2Gui.this.rb.getResourceString("refresh.overview"), uniqueId);
                MessageAccessDB messageAccess = new MessageAccessDB(AS2Gui.this.configConnection,
                        AS2Gui.this.runtimeConnection);
                MessageOverviewFilter filter = new MessageOverviewFilter();
                filter.setShowFinished(AS2Gui.this.jCheckBoxFilterShowOk.isSelected());
                filter.setShowPending(AS2Gui.this.jCheckBoxFilterShowPending.isSelected());
                filter.setShowStopped(AS2Gui.this.jCheckBoxFilterShowStopped.isSelected());
                if (AS2Gui.this.jComboBoxFilterPartner.getSelectedIndex() <= 0) {
                    filter.setShowPartner(null);
                } else {
                    filter.setShowPartner((Partner) AS2Gui.this.jComboBoxFilterPartner.getSelectedItem());
                }
//                if (AS2Gui.this.jComboBoxFilterLocalStation.getSelectedIndex() <= 0) {
//                    filter.setShowLocalStation(null);
//                } else {
//                    filter.setShowLocalStation((Partner) AS2Gui.this.jComboBoxFilterLocalStation.getSelectedItem());
//                }
//                if (AS2Gui.this.jComboBoxFilterDirection.getSelectedIndex() == 0) {
//                    filter.setShowDirection(MessageOverviewFilter.DIRECTION_ALL);
//                } else if (AS2Gui.this.jComboBoxFilterDirection.getSelectedIndex() == 1) {
//                    filter.setShowDirection(MessageOverviewFilter.DIRECTION_IN);
//                } else if (AS2Gui.this.jComboBoxFilterDirection.getSelectedIndex() == 2) {
//                    filter.setShowDirection(MessageOverviewFilter.DIRECTION_OUT);
//                }
                int countAll = 0;
                int countOk = 0;
                int countPending = 0;
                int countFailure = 0;
                int countSelected = 0;
                List<AS2MessageInfo> overviewList = messageAccess.getMessageOverview(filter);
                countAll = overviewList.size();
                List<AS2Message> messageList = new ArrayList<AS2Message>();
                for (AS2MessageInfo messageInfo : overviewList) {
                    AS2Message message = new AS2Message(messageInfo);
                    switch (messageInfo.getState()) {
                        case AS2Message.STATE_FINISHED:
                            countOk++;
                            break;
                        case AS2Message.STATE_PENDING:
                            countPending++;
                            break;
                        case AS2Message.STATE_STOPPED:
                            countFailure++;
                            break;
                    }
                    //add the payloads related to this message
                    messageList.add(message);
                }
                TableModelMessageOverview tableModel = (TableModelMessageOverview) AS2Gui.this.jTableMessageOverview.getModel();
                tableModel.passNewData(messageList);
                this.lazyloadPayloads(messageList);
                //try to jump to latest entry
                try {
                    int rowCount = AS2Gui.this.jTableMessageOverview.getRowCount();
                    AS2Gui.this.jTableMessageOverview.getSelectionModel().
                            setSelectionInterval(rowCount - 1, rowCount - 1);
                    RefreshThread.this.makeRowVisible(AS2Gui.this.jTableMessageOverview, rowCount - 1);
                } catch (Throwable ignore) {
                    //nop
                }
                countSelected = AS2Gui.this.jTableMessageOverview.getSelectedRowCount();
                AS2Gui.this.as2StatusBar.setTransactionCount(countAll, countOk, countPending, countFailure, countSelected);
            } catch (Exception e) {
                AS2Gui.this.getLogger().severe("refreshMessageOverviewList: " + e.getMessage());
            } finally {
                AS2Gui.this.as2StatusBar.stopProgressIfExists(uniqueId);
            }
        }

        /**Scrolls to an entry of the passed table
         * @param table Table to to scroll in
         * @param row Row to ensure visibility
         */
        private void makeRowVisible(final JTable table, final int row) {

            try {
                SwingUtilities.invokeAndWait(new Runnable() {

                    @Override
                    public void run() {
                        if (!table.isVisible()) {
                            return;
                        }
                        if (table.getColumnCount() == 0) {
                            return;
                        }
                        if (row < 0 || row >= table.getRowCount()) {
                            return;
                        }
                        try {
                            Rectangle visible = table.getVisibleRect();
                            Rectangle cell = table.getCellRect(row, 0, true);
                            if (cell.y < visible.y) {
                                visible.y = cell.y;
                                table.scrollRectToVisible(visible);
                            } else if (cell.y + cell.height > visible.y + visible.height) {
                                visible.y = cell.y + cell.height - visible.height;
                                table.scrollRectToVisible(visible);
                            }
                        } catch (Throwable e) {
                            //nop
                        }
                    }
                });
            } catch (Exception e) {
                //nop
            }
        }
    }

    private class LazyLoaderThread implements Runnable {

        private List<AS2Message> messageList = new ArrayList<AS2Message>();
        private boolean stopLazyLoad = false;

        public LazyLoaderThread(List<AS2Message> messageList) {
            this.messageList.addAll(messageList);
        }

        public void stopLazyLoad() {
            this.stopLazyLoad = true;
        }

        @Override
        public void run() {
            TableModelMessageOverview tableModel = (TableModelMessageOverview) AS2Gui.this.jTableMessageOverview.getModel();
            MessageAccessDB messageAccess = new MessageAccessDB(AS2Gui.this.configConnection,
                    AS2Gui.this.runtimeConnection);
            for (AS2Message message : this.messageList) {
                //bail out, lazy load is no longer required because a new overview refresh occured
                if (this.stopLazyLoad) {
                    break;
                } else {
                    List<AS2Payload> payloads = messageAccess.getPayload(message.getAS2Info().getMessageId());
                    tableModel.passPayload(message, payloads);
                }
            }
        }
    }
}
