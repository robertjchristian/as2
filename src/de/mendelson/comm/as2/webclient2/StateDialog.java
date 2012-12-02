//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/webclient2/StateDialog.java,v 1.1 2012/04/18 14:10:41 heller Exp $
package de.mendelson.comm.as2.webclient2;

import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import de.mendelson.comm.as2.AS2ServerVersion;
import de.mendelson.comm.as2.client.rmi.GenericClient;
import de.mendelson.comm.as2.clientserver.ErrorObject;
import de.mendelson.comm.as2.clientserver.serialize.CommandObjectServerInfo;
import java.text.DateFormat;
import java.util.ArrayList;


/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Displays the state of the receipt unit
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class StateDialog extends OkDialog {
        
    public StateDialog() {
        super(670, 410, "Server state");
        this.setResizable(false);
        this.setClosable(false);
    }

    /**Could be overwritten, contains the content to display*/
    @Override
    public AbstractComponent getContentPanel() {
        DateFormat format = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
        Panel panel = new Panel();
        StringBuilder sourceBuffer = new StringBuilder();
        sourceBuffer.append("<p>The AS2 HTTP receipt unit <strong>");
        sourceBuffer.append(AS2ServerVersion.getProductName());
        sourceBuffer.append(" ");
        sourceBuffer.append(AS2ServerVersion.getVersion());
        sourceBuffer.append(" ");
        sourceBuffer.append(AS2ServerVersion.getBuild());
        sourceBuffer.append(" </strong> is up and running.<br></p>");
        boolean processingUnitUp = false;
        GenericClient client = new GenericClient();
        CommandObjectServerInfo commandObject = new CommandObjectServerInfo();
        ErrorObject errorObject = client.send(commandObject);
        if (errorObject.noErrorsAndWarnings()) {
            commandObject = (CommandObjectServerInfo) client.getCommandObject();
            long startTime = new Long(commandObject.getProperties().getProperty(CommandObjectServerInfo.SERVER_START_TIME)).longValue();
            sourceBuffer.append("The AS2 processing unit <strong>");
            sourceBuffer.append(commandObject.getProperties().getProperty(CommandObjectServerInfo.SERVER_PRODUCT_NAME));
            sourceBuffer.append(" ").append(commandObject.getProperties().getProperty(CommandObjectServerInfo.SERVER_VERSION));
            sourceBuffer.append(" ").append(commandObject.getProperties().getProperty(CommandObjectServerInfo.SERVER_BUILD));
            sourceBuffer.append("</strong> is up and running since <strong>");
            sourceBuffer.append(format.format(startTime));
            sourceBuffer.append("</strong>.");
            processingUnitUp = true;
        } else {
            sourceBuffer.append("Error connecting to AS2 processing unit: ");
            ArrayList log = client.getLog();
            for (int i = 0; i < log.size(); i++) {
                if (log.get(i) != null) {
                    sourceBuffer.append(log.get(i));
                }
            }
        }
        sourceBuffer.append("<br><br>");
        if (processingUnitUp) {
            sourceBuffer.append("<strong><font color='green'>System status is fine.</font></strong><br><br><br>Please send your AS2 messages now to <a href=\"/as2/HttpReceiver\" target=\"_new\"><strong>HttpReceiver</strong></a>.");
        } else {
            sourceBuffer.append("<strong><font color='red'>Errors encounted.</font></strong><br>Please fix them before sending messages to <a href=\"/as2/HttpReceiver\" target=\"_new\"><strong>HttpReceiver</strong></a>.");
        }
        sourceBuffer.append("<br><br><br><hr><p>If you are running into any problem please visit the forum at <a href=\"http://community.mendelson-e-c.com\"><strong>community.mendelson-e-c.com</strong></a> or contact the mendelson team by sending a mail to <a href=\"mailto: info@mendelson.de\"><strong>info@mendelson.de</strong></a>.</p>");
        Label label = new Label(sourceBuffer.toString(), Label.CONTENT_XHTML);
        panel.addComponent(label);
        return (panel);
    }
}
