//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/AS2ServerVersion.java,v 1.1 2012/04/18 14:10:16 heller Exp $
package de.mendelson.comm.as2;

import de.mendelson.Copyright;
/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */

/**
 * Class to check the version and the build of the AS2 server
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class AS2ServerVersion {

    /**Returns the used user agent*/
    public static String getUserAgent() {
        StringBuilder agent = new StringBuilder();
        agent.append(AS2ServerVersion.getProductName()).append(" ");
        agent.append(AS2ServerVersion.getVersion()).append(" ");
        agent.append(AS2ServerVersion.getBuild()).append(" - www.mendelson-e-c.com");
        return (agent.toString());
    }

    /**Returns this products version number*/
    public static String getVersion() {
        return ("1.1");
    }

    /**Returns the internal build number*/
    public static String getBuild() {
        return ("build " + getBuildNo());
    }

    /**Returns the internal build number*/
    public static int getBuildNo() {
        return (39);
    }

    /**returns the name of this product*/
    public static final String getProductName() {
        return ("mendelson opensource AS2");
    }

    /**The data base has a version. It is found in the column actualversion of the
     *table version. If the found version does not match the version defined here,
     *an auto update of the database is performed.
     */
    public static int getRequiredDBVersionConfig() {
        return (35);
    }

    /**The data base has a version. It is found in the column actualversion of the
     *table version. If the found version does not match the version defined here,
     *an auto update of the database is performed.
     */
    public static int getRequiredDBVersionRuntime() {
        return (36);
    }

    /**returns the short name of this product, return the full name if not such
     *short name exists!
     */
    public static String getProductNameShortcut() {
        return ("mendelson opensource AS2");
    }

    /**Returns the date the package was last modified*/
    public static String getLastModificationDate() {
        String fullDate = "$Date: 2012/04/18 14:10:16 $";
        return (fullDate.substring(fullDate.indexOf(":") + 1, fullDate.lastIndexOf("$")));
    }

    /**Gets the copyright message for this product*/
    public static String getCopyrightMessage() {
        return (Copyright.getCopyrightMessage());
    }

    /**Gets the company name*/
    public static String getCompany() {
        return ("mendelson-e-commerce GmbH");
    }

    /**Gets the company address*/
    public static String getStreet() {
        return ("Potsdamer Platz 11");
    }

    /**Gets the company zip*/
    public static String getZip() {
        return ("D-10785 Berlin");
    }

    /**Gets the company telephone*/
    public static String getTelephone() {
        return ("Tel: +49 30 89 09 10 22");
    }

    /**Gets the company info email*/
    public static String getInfoEmail() {
        return ("gpl_as2@mendelson.de");
    }

    /**Returns the full name with build, version etc*/
    public static String getFullProductName() {
        StringBuilder builder = new StringBuilder();
        builder.append(AS2ServerVersion.getProductName());
        builder.append(" ");
        builder.append(AS2ServerVersion.getVersion());
        builder.append(" ");
        builder.append(AS2ServerVersion.getBuild());
        return (builder.toString());
    }
}
