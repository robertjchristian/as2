//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/webclient2/AS2WebUI.java,v 1.1 2012/04/18 14:10:41 heller Exp $
package de.mendelson.comm.as2.webclient2;

import com.vaadin.Application;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.terminal.gwt.server.WebApplicationContext;
import com.vaadin.terminal.gwt.server.WebBrowser;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.LoginForm;
import com.vaadin.ui.LoginForm.LoginEvent;
import com.vaadin.ui.LoginForm.LoginListener;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.Panel;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.Table;
import com.vaadin.ui.Window;
import de.mendelson.comm.as2.AS2ServerVersion;
import de.mendelson.comm.as2.database.DBDriverManager;
import de.mendelson.comm.as2.message.AS2Message;
import de.mendelson.comm.as2.message.AS2MessageInfo;
import de.mendelson.comm.as2.message.AS2Payload;
import de.mendelson.comm.as2.message.MessageAccessDB;
import de.mendelson.comm.as2.message.MessageOverviewFilter;
import de.mendelson.comm.as2.message.ResourceBundleAS2Message;
import de.mendelson.comm.as2.partner.Partner;
import de.mendelson.comm.as2.partner.PartnerAccessDB;
import de.mendelson.util.MecResourceBundle;
import de.mendelson.util.clientserver.user.User;
import de.mendelson.util.clientserver.user.UserAccess;
import java.net.URI;
import java.sql.Connection;
import java.text.DateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Logger;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Main frame for the web interface
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class AS2WebUI extends Application {

    private Connection configConnection = null;
    private Connection runtimeConnection = null;
    private Button buttonDetails = null;
    /**Format the date display*/
    private DateFormat format = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
    private Window mainWindow = new Window(AS2ServerVersion.getFullProductName());
    private Panel mainPanel = new Panel();
    private Panel loginPanel = new Panel();
    private ThemeResource ICON_IN;
    private ThemeResource ICON_OUT;
    private ThemeResource ICON_PENDING;
    private ThemeResource ICON_STOPPED;
    private ThemeResource ICON_FINISHED;
    private ThemeResource ICON_ALL;
    private MecResourceBundle rbMessage;
    /**Stores information about the browser*/
    private WebBrowser browser;
    private User user = null;
    private Label labelUsername = new Label();
    private Table overviewTable = null;
    /**Footer*/
    private Label footerTransactionSum = new Label();
    private Label footerTransactionOkSum = new Label();
    private Label footerTransactionPendingSum = new Label();
    private Label footerTransactionErrorSum = new Label();
    private HorizontalLayout footerLayout;

    /** This is the entry point of you application as denoted in your web.xml */
    public AS2WebUI() {
        //load resource bundle
        try {
            this.rbMessage = (MecResourceBundle) ResourceBundle.getBundle(
                    ResourceBundleAS2Message.class.getName());
        } //load up  resourcebundle
        catch (MissingResourceException e) {
            throw new RuntimeException("Oops..resource bundle " + e.getClassName() + " not found.");
        }
    }

    /**
     * Init is invoked on application load (when a user accesses the application
     * for the first time).
     */
    @Override
    public void init() {
        this.user = null;
        //display fav icon, set theme
        setTheme("mendelson");
        ICON_IN = new ThemeResource("images/in16x16.gif");
        ICON_OUT = new ThemeResource("images/out16x16.gif");
        ICON_PENDING = new ThemeResource("images/state_pending16x16.gif");
        ICON_STOPPED = new ThemeResource("images/state_stopped16x16.gif");
        ICON_FINISHED = new ThemeResource("images/state_finished16x16.gif");
        ICON_ALL = new ThemeResource("images/state_all16x16.gif");
        this.browser = ((WebApplicationContext) getContext()).getBrowser();
        //register the database drivers for the VM
        try {
            Class.forName("org.hsqldb.jdbcDriver");
        } catch (Exception e) {
            e.printStackTrace();
            mainWindow.showNotification("Fatal",
                    e.getMessage(),
                    Window.Notification.TYPE_ERROR_MESSAGE);
            return;
        }
        //establish database connection
        try {
            this.configConnection = DBDriverManager.getConnectionWithoutErrorHandling(DBDriverManager.DB_CONFIG, "localhost");
            this.runtimeConnection = DBDriverManager.getConnectionWithoutErrorHandling(DBDriverManager.DB_RUNTIME, "localhost");
        } catch (Exception e) {
            e.printStackTrace();
            mainWindow.showNotification("Fatal",
                    e.getMessage(),
                    Window.Notification.TYPE_ERROR_MESSAGE);
        }
        Embedded logoImage = new Embedded("", new ThemeResource("images/logo.jpg"));
        logoImage.setType(Embedded.TYPE_IMAGE);
        this.mainWindow.addComponent(logoImage);
        LoginForm loginForm = new LoginForm();
        loginForm.addListener(new LoginListener() {

            @Override
            public void onLogin(LoginEvent event) {
                try {
                    String username = event.getLoginParameter("username");
                    String password = event.getLoginParameter("password");
                    UserAccess access = new UserAccess(Logger.getAnonymousLogger());
                    User foundUser = access.readUser(username);
                    if (foundUser == null
                            || foundUser.getPasswdCrypted() == null
                            || !(foundUser.getPasswdCrypted().equals(User.cryptPassword(password.toCharArray())))
                            || !foundUser.getPermission(1).equals("FULL")) {
                        mainWindow.showNotification("Login failed",
                                "Wrong credentials or no permission",
                                Window.Notification.TYPE_WARNING_MESSAGE);
                    } else {
                        //login accepted
                        AS2WebUI.this.user = foundUser;
                        AS2WebUI.this.labelUsername.setValue("User: " + AS2WebUI.this.user.getName());
                        AS2WebUI.this.mainWindow.removeComponent(AS2WebUI.this.loginPanel);
                        AS2WebUI.this.mainWindow.addComponent(AS2WebUI.this.mainPanel);
                        AS2WebUI.this.refreshOverviewTableData();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    mainWindow.showNotification("Service not available",
                            "Login not possible at the moment - please try later",
                            Window.Notification.TYPE_WARNING_MESSAGE);
                }
            }
        });
        this.loginPanel.addComponent(loginForm);
        this.mainWindow.addComponent(this.loginPanel);
        this.mainPanel.addComponent(this.createMenuBar());
        this.mainPanel.addComponent(this.createButtonBar());
        this.overviewTable = this.createOverviewTable();
        this.mainPanel.addComponent(overviewTable);
        this.footerLayout = new HorizontalLayout();
        footerLayout.setMargin(true, false, false, false);
        footerLayout.setSpacing(true);
        footerLayout.addComponent(this.footerTransactionSum);
        footerLayout.addComponent(this.footerTransactionOkSum);
        footerLayout.addComponent(this.footerTransactionPendingSum);        
        footerLayout.addComponent(this.footerTransactionErrorSum);
        this.mainPanel.addComponent(footerLayout);
        this.setMainWindow(this.mainWindow);
    }

    private void displayMessageDetailsOfSelectedRow() {
        AS2MessageInfo selectedInfo = (AS2MessageInfo) AS2WebUI.this.overviewTable.getValue();
        OkDialog dialog = new TransactionDetailsDialog(AS2WebUI.this.configConnection, AS2WebUI.this.runtimeConnection,
                selectedInfo, AS2WebUI.this.getThemeURI());
        dialog.init();
        AS2WebUI.this.mainWindow.addWindow(dialog);
    }

    private HorizontalLayout createButtonBar() {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setSizeFull();
        Button buttonRefresh = new Button("Refresh");
        buttonRefresh.setIcon(new ThemeResource("images/refresh16x16.gif"));
        buttonRefresh.setEnabled(true);
        buttonRefresh.addListener(new ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                AS2WebUI.this.refreshOverviewTableData();
            }
        });
        buttonLayout.addComponent(buttonRefresh);
        this.buttonDetails = new Button("Message details");
        this.buttonDetails.setIcon(new ThemeResource("images/messagedetails16x16.gif"));
        this.buttonDetails.setEnabled(false);
        this.buttonDetails.addListener(new ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                AS2WebUI.this.displayMessageDetailsOfSelectedRow();
            }
        });
        buttonLayout.addComponent(this.buttonDetails);
        this.labelUsername.setWidth(null);
        buttonLayout.addComponent(this.labelUsername);
        buttonLayout.setComponentAlignment(this.labelUsername, Alignment.MIDDLE_RIGHT);
        buttonLayout.setExpandRatio(buttonRefresh, 0.0f);
        buttonLayout.setExpandRatio(this.buttonDetails, 0.0f);
        buttonLayout.setExpandRatio(this.labelUsername, 1.0f);
        return (buttonLayout);
    }

    public void logout() {
        this.user = null;
        this.overviewTable.removeAllItems();
        this.mainWindow.removeComponent(this.mainPanel);
        this.mainWindow.addComponent(this.loginPanel);
    }

    private MenuBar createMenuBar() {
        MenuBar.Command logoutCommand = new MenuBar.Command() {

            @Override
            public void menuSelected(MenuItem selectedItem) {
                logout();
            }
        };
        MenuBar.Command stateCommand = new MenuBar.Command() {

            @Override
            public void menuSelected(MenuItem selectedItem) {
                OkDialog dialog = new StateDialog();
                dialog.init();
                AS2WebUI.this.mainWindow.addWindow(dialog);
            }
        };
        MenuBar.Command aboutCommand = new MenuBar.Command() {

            @Override
            public void menuSelected(MenuItem selectedItem) {
                OkDialog dialog = new AboutDialog();
                dialog.init();
                AS2WebUI.this.mainWindow.addWindow(dialog);
            }
        };
        MenuBar menubar = new MenuBar();
        MenuBar.MenuItem fileItem = menubar.addItem("AS2 server", null, null);
        fileItem.addItem("State", null, stateCommand);
        fileItem.addItem("Logout", null, logoutCommand);
        MenuBar.MenuItem helpItem = menubar.addItem("Help", null, null);
        helpItem.addItem("About", null, aboutCommand);
        menubar.setSizeFull();
        return (menubar);
    }

    private String getThemeURI() {
        try {
            URI uri = new URI(this.getURL() + "VAADIN/themes/" + this.getTheme() + "/");
            return uri.normalize().toString();
        } catch (Exception e) {
            this.mainWindow.showNotification("Warning",
                    "Theme location could not be resolved:" + e,
                    Window.Notification.TYPE_WARNING_MESSAGE);
        }
        return "";
    }

    /**Connect to the database and load the data into a table*/
    private Table createOverviewTable() {
        Table table = new Table();
        table.setSelectable(true);
        // Send changes in selection immediately to server.
        table.setImmediate(true);
        //disallows unselection of a line
        table.setNullSelectionAllowed(false);
        // Handle selection change.
        table.addListener(new Property.ValueChangeListener() {

            @Override
            public void valueChange(ValueChangeEvent event) {
                AS2WebUI.this.buttonDetails.setEnabled(true);
            }
        });
        table.addListener(new ItemClickEvent.ItemClickListener() {

            @Override
            public void itemClick(ItemClickEvent event) {
                if (event.isDoubleClick()) {
                    AS2WebUI.this.displayMessageDetailsOfSelectedRow();
                }
            }
        });
        table.addStyleName("components-inside");
        table.setPageLength(20);
        table.setSizeFull();
        /* Define the names and data types of columns.
         * The "default value" parameter is meaningless here. */
        table.addContainerProperty(" ", Label.class, null);
        table.addContainerProperty("  ", Label.class, null);
        table.addContainerProperty("Timestamp", String.class, null);
        table.addContainerProperty("Local station", String.class, null);
        table.addContainerProperty("Partner", String.class, null);
        table.addContainerProperty("Message id", String.class, null);
        table.addContainerProperty("Payload", String.class, null);
        table.addContainerProperty("Encryption", String.class, null);
        table.addContainerProperty("Signature", String.class, null);
        table.addContainerProperty("MDN", String.class, null);
        return (table);
    }

    private void refreshOverviewTableData() {
        //always check if there is still a user available, parallel screens are possible
        if (this.user == null) {
            this.logout();
            return;
        }
        /**Stores all partner ids and the corresponding partner objects*/
        Map<String, Partner> partnerMap = new HashMap<String, Partner>();
        //load partner data
        try {
            PartnerAccessDB partnerAccess = new PartnerAccessDB(this.configConnection, this.runtimeConnection);
            Partner[] partner = partnerAccess.getPartner();
            for (Partner singlePartner : partner) {
                partnerMap.put(singlePartner.getAS2Identification(), singlePartner);
            }
        } catch (Exception e) {
            this.mainWindow.showNotification("Problem",
                    e.getMessage(),
                    Window.Notification.TYPE_WARNING_MESSAGE);
        }
        int sum = 0;
        int sumPending = 0;
        int sumOk = 0;
        int sumError = 0;
        //load message data
        try {
            Object selection = null;
            MessageAccessDB access = new MessageAccessDB(this.configConnection, this.runtimeConnection);
            MessageOverviewFilter filter = new MessageOverviewFilter();
            List<AS2MessageInfo> info = access.getMessageOverview(filter);
            this.overviewTable.removeAllItems();
            for (int i = 0; i < info.size(); i++) {
                AS2MessageInfo messageInfo = info.get(i);
                AS2Message message = new AS2Message(messageInfo);
                List<AS2Payload> payloads = access.getPayload(messageInfo.getMessageId());
                for (AS2Payload payload : payloads) {
                    message.addPayload(payload);
                }
                sum++;
                ThemeResource stateIcon = ICON_PENDING;
                if (messageInfo.getState() == AS2Message.STATE_FINISHED) {
                    stateIcon = ICON_FINISHED;
                    sumOk++;
                } else if (messageInfo.getState() == AS2Message.STATE_STOPPED) {
                    stateIcon = ICON_STOPPED;
                    sumError++;
                } else {
                    sumPending++;
                }
                ThemeResource directionIcon = null;
                if (messageInfo.getDirection() == AS2MessageInfo.DIRECTION_IN) {
                    directionIcon = ICON_IN;
                } else {
                    directionIcon = ICON_OUT;
                }
                String localStationStr = null;
                if (messageInfo.getDirection() != AS2MessageInfo.DIRECTION_IN) {
                    String id = messageInfo.getSenderId();
                    Partner sender = partnerMap.get(id);
                    if (sender != null) {
                        localStationStr = sender.getName();
                    } else {
                        localStationStr = id;
                    }
                } else {
                    String id = messageInfo.getReceiverId();
                    Partner receiver = partnerMap.get(id);
                    if (receiver != null) {
                        localStationStr = receiver.getName();
                    } else {
                        localStationStr = id;
                    }
                }
                //add the remote partner to the table
                String partnerStr = null;
                if (messageInfo.getDirection() == AS2MessageInfo.DIRECTION_OUT) {
                    String id = messageInfo.getReceiverId();
                    Partner receiver = partnerMap.get(id);
                    if (receiver != null) {
                        partnerStr = receiver.getName();
                    } else {
                        partnerStr = id;
                    }
                } else {
                    String id = messageInfo.getSenderId();
                    Partner sender = partnerMap.get(id);
                    if (sender != null) {
                        partnerStr = sender.getName();
                    } else {
                        partnerStr = id;
                    }                    
                }
                String payload = null;
                if (message.getPayloadCount() == 0
                        || (message.getPayloadCount() == 1 && message.getPayload(0).getOriginalFilename() == null)) {
                    payload = "--";
                } else if (message.getPayloadCount() == 1) {
                    payload = message.getPayload(0).getOriginalFilename();
                } else {
                    payload = "Number of attachments: " + String.valueOf(message.getPayloadCount());
                }
                this.overviewTable.addItem(new Object[]{
                            this.generateImageLabel(stateIcon),
                            this.generateImageLabel(directionIcon),
                            this.format.format(messageInfo.getInitDate()),
                            localStationStr,
                            partnerStr,
                            messageInfo.getMessageId(),
                            payload,
                            this.rbMessage.getResourceString("encryption." + messageInfo.getEncryptionType()),
                            this.rbMessage.getResourceString("signature." + messageInfo.getSignType()),
                            messageInfo.requestsSyncMDN() ? "SYNC" : "ASYNC"}, messageInfo);
                selection = messageInfo;
            }
            if (selection != null) {
                this.overviewTable.select(selection);
                this.overviewTable.setCurrentPageFirstItemId(selection);
            }
            //refresh the footer
            this.refreshFooterLabel(this.footerTransactionSum, ICON_ALL, sum);
            this.refreshFooterLabel(this.footerTransactionErrorSum, ICON_STOPPED, sumError);
            this.refreshFooterLabel(this.footerTransactionPendingSum, ICON_PENDING, sumPending);
            this.refreshFooterLabel(this.footerTransactionOkSum, ICON_FINISHED, sumOk);
        } catch (Exception e) {
            this.mainWindow.showNotification("Problem",
                    "[" + e.getClass().getName() + "] " + e.getMessage(),
                    Window.Notification.TYPE_WARNING_MESSAGE);
        }
    }

    public Label generateImageLabel(ThemeResource resource) {
        Label label = new Label();
        label.setContentMode(Label.CONTENT_XHTML);
        label.setIcon(resource);
        label.setValue("<img src=\"" + this.getThemeURI() + resource.getResourceId() + "\" />");
        return (label);
    }

    public void refreshFooterLabel(Label label, ThemeResource resource, int count) {
        label.setContentMode(Label.CONTENT_XHTML);
        label.setValue("<img src=\"" + this.getThemeURI() + resource.getResourceId() + "\" align=\"top\"/>" + String.valueOf(count));
    }
}
