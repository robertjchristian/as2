//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/cem/messages/CEMStructure.java,v 1.1 2012/04/18 14:10:22 heller Exp $
package de.mendelson.comm.as2.cem.messages;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Superclass for all CEM structures
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public abstract class CEMStructure {

    public CEMStructure() {
    }

    /**Returns the structure as xml*/
    public abstract String toXML();

    /**Formats the date to the format yyyy-MM-ddTHH:mm:ss-HH:mm, e.g.
     * 2005-03-01T14:05:00-05:00
     */
    protected String toXMLDate(Date date) {
        int utcOffset = TimeZone.getDefault().getOffset(date.getTime());
        long offsetHours = TimeUnit.MILLISECONDS.toHours(utcOffset);
        DateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
        DateFormat format2 = new SimpleDateFormat("HH:mm:ss");
        NumberFormat offsetFormat = new DecimalFormat("+00;-00");
        return (format1.format(date)
                + "T"
                + format2.format(date)
                + offsetFormat.format(offsetHours) + ":00");
    }

    /**Parses a date format that is in format yyyy-MM-ddTHH:mm:ss-HH:mm*/
    protected static Date parseXMLDate(String xmlDate) throws Exception {
        if (xmlDate == null || xmlDate.length() != 25) {
            throw new Exception("Date expected in format 'yyyy-MM-ddTHH:mm:ss-HH:mm', found data: '" + xmlDate + "'");
        }
        String timeStr = xmlDate.substring(0, 10) + xmlDate.substring(11, 19);
        SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss");
        Date returnDate = null;
        returnDate = timeFormat.parse(timeStr);
        String offsetStr = xmlDate.substring(19);
        TimeZone zone = TimeZone.getTimeZone("UTC" + offsetStr);
        Calendar calendar = Calendar.getInstance(zone);
        calendar.setTime(returnDate);
        return (calendar.getTime());
    }

    @Override
    public String toString() {
        return (this.toXML());
    }

    /**Adds a cdata indicator to xml data*/
    public String toCDATA(String data) {
        return ("<![CDATA[" + data + "]]>");
    }

    
//    public static final void main(String[] args) {
//        try {
//            System.out.println(CEMStructure.parseXMLDate("2009-10-02T11:07:33+02:00"));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}
