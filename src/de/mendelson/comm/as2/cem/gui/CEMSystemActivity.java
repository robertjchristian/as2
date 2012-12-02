//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/cem/gui/CEMSystemActivity.java,v 1.1 2012/04/18 14:10:20 heller Exp $
package de.mendelson.comm.as2.cem.gui;

import de.mendelson.comm.as2.cem.CEMEntry;
import de.mendelson.util.MecResourceBundle;
import java.text.DateFormat;
import java.util.Date;
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
 * Helper class state stores beneath the cem protocol issues what the system will really do with the entry
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class CEMSystemActivity {

    private String text = "";
    private int state = CEMEntry.STATUS_ACCEPTED_INT;
    private MecResourceBundle rb;
    private DateFormat format = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);

    public CEMSystemActivity(CEMEntry entry) {
        //load resource bundle
        try {
            this.rb = (MecResourceBundle) ResourceBundle.getBundle(
                    ResourceBundleCEMOverview.class.getName());
        } //load up  resourcebundle
        catch (MissingResourceException e) {
            throw new RuntimeException("Oops..resource bundle " + e.getClassName() + " not found.");
        }
        this.processActivity(entry);
    }

    /**Returns the activity*/
    private void processActivity(CEMEntry entry) {
        //the user has canceled the process by gui or by sending a new request with the same parameters
        if (entry.getCemState() == CEMEntry.STATUS_CANCELED_INT) {
            this.text = this.rb.getResourceString("activity.none");
            this.state = CEMEntry.STATUS_CANCELED_INT;
        }
        //the receiver has rejected the certificate (no idea why he should do this???)
        if (entry.getCemState() == CEMEntry.STATUS_REJECTED_INT) {
            this.text = this.rb.getResourceString("activity.none");
            this.state = CEMEntry.STATUS_REJECTED_INT;
        }
        //processing failure, e.g. bad MDN on the request
        if (entry.getCemState() == CEMEntry.STATUS_PROCESSING_ERROR_INT) {
            this.text = this.rb.getResourceString("activity.none");
            this.state = CEMEntry.STATUS_PROCESSING_ERROR_INT;
        }
        //activation is either done by a direct response for all certs or - if a respondbydate is set
        // - by the respondby date for sign and ssl.
        if (entry.getCemState() == CEMEntry.STATUS_PENDING_INT) {
            if (entry.getCategory() == CEMEntry.CATEGORY_CRYPT) {
                this.text = this.rb.getResourceString("activity.waitingforanswer");
                this.state = CEMEntry.STATUS_PENDING_INT;
            } else {
                if (entry.hasRespondByDate()) {
                    //SSL and SIGN CEm request dont require an answer if the respondby date is transmitted, activation is always the date
                    this.text = this.rb.getResourceString("activity.waitingfordate", this.format.format(new Date(entry.getRespondByDate())));
                    this.state = CEMEntry.STATUS_PENDING_INT;
                }else{
                    //same as for crypt: change certs on cem response - dont do this if you have more than
                    //one partner - you will be unable to send other CEM requests because the digital signature
                    //will not match for the other partners
                    this.text = this.rb.getResourceString("activity.waitingforanswer");
                    this.state = CEMEntry.STATUS_PENDING_INT;
                }
            }
        }
        if (entry.getCemState() == CEMEntry.STATUS_ACCEPTED_INT) {
            if (entry.getCategory() == CEMEntry.CATEGORY_CRYPT) {
                if (entry.isProcessed()) {
                    this.text = this.rb.getResourceString("activity.activated", this.format.format(new Date(entry.getProcessDate())));
                    this.state = CEMEntry.STATUS_ACCEPTED_INT;
                } else {
                    //cem protocol state says ok but answer not yet processed
                    this.text = this.rb.getResourceString("activity.waitingforprocessing");
                    this.state = CEMEntry.STATUS_PENDING_INT;
                }
            } else {
                if (entry.isProcessed()) {
                    this.text = this.rb.getResourceString("activity.activated", this.format.format(new Date(entry.getProcessDate())));
                    this.state = CEMEntry.STATUS_ACCEPTED_INT;
                } else {
                    //SSL and SIGN CEm request dont require an answer, activation is always the date
                    this.text = this.rb.getResourceString("activity.waitingfordate", this.format.format(new Date(entry.getRespondByDate())));
                    this.state = CEMEntry.STATUS_PENDING_INT;
                }
            }
        }
    }

    /**
     * @return the text
     */
    public String getText() {
        return text;
    }

    /**
     * @return the state
     */
    public int getState() {
        return state;
    }
}
