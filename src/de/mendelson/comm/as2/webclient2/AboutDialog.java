//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/webclient2/AboutDialog.java,v 1.1 2012/04/18 14:10:41 heller Exp $
package de.mendelson.comm.as2.webclient2;

import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import de.mendelson.Copyright;
import de.mendelson.comm.as2.AS2ServerVersion;


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
public class AboutDialog extends OkDialog {

    public AboutDialog() {
        super(470, 430, "About");
        this.setResizable(false);
        this.setClosable(false);
    }

    /**Could be overwritten, contains the content to display*/
    @Override
    public AbstractComponent getContentPanel() {
        int maxX = 7;
        Panel panel = new Panel();
        GridLayout gridLayout = new GridLayout(maxX, 13);
        gridLayout.setSizeFull();
        Embedded logComm = new Embedded("", new ThemeResource("images/logocommprotocols.gif"));
        logComm.setType(Embedded.TYPE_IMAGE);
        VerticalLayout gapLayout = new VerticalLayout();
        gapLayout.setMargin(false, true, true, false);
        gapLayout.addComponent(logComm);
        gridLayout.addComponent(gapLayout, 0, 0, 1, 3);
        gridLayout.addComponent(new Label("<strong>" + AS2ServerVersion.getFullProductName() + "</strong>", Label.CONTENT_XHTML), 2, 1, maxX - 1, 1);
        gridLayout.addComponent(new Label(AS2ServerVersion.getLastModificationDate()), 2, 2, maxX - 1, 2);
        gridLayout.addComponent(new Label("<hr/>", Label.CONTENT_XHTML), 0, 4, maxX - 1, 4);
        gridLayout.addComponent(new Label(Copyright.getCopyrightMessage(), Label.CONTENT_XHTML), 0, 5, maxX - 1, 5);
        gridLayout.addComponent(new Label(AS2ServerVersion.getStreet(), Label.CONTENT_XHTML), 0, 6, maxX - 1, 6);
        gridLayout.addComponent(new Label(AS2ServerVersion.getZip(), Label.CONTENT_XHTML), 0, 7, maxX - 1, 7);
        gridLayout.addComponent(new Label(AS2ServerVersion.getTelephone(), Label.CONTENT_XHTML), 0, 8, maxX - 1, 8);
        gridLayout.addComponent(new Label(AS2ServerVersion.getInfoEmail(), Label.CONTENT_XHTML), 0, 9, maxX - 1, 9);
        gridLayout.addComponent(new Label("<hr/>", Label.CONTENT_XHTML), 0, 10, 6, 10);
        gridLayout.addComponent(new Link("http://www.mendelson.de", new ExternalResource("http://www.mendelson.de")), 0, 11, maxX - 1, 11);
        gridLayout.addComponent(new Link("http://www.mendelson-e-c.com", new ExternalResource("http://www.mendelson-e-c.com")), 0, 12, maxX - 1, 12);
        panel.addComponent(gridLayout);
        return (panel);
    }
}
