//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/send/ComparatorFiledateOldestFirst.java,v 1.1 2012/04/18 14:10:35 heller Exp $
package de.mendelson.comm.as2.send;

import java.io.File;
import java.util.Comparator;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Comparator to use to compare file dates by their age
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class ComparatorFiledateOldestFirst implements Comparator {

    /**Compares tow object of the type File by their last modification date
     */
    @Override
    public int compare(Object o1, Object o2) {
        long lastModified1 = ((File) o1).lastModified();
        long lastModified2 = ((File) o2).lastModified();
        if (lastModified1 < lastModified2) {
            return (-1);
        }
        if (lastModified1 > lastModified2) {
            return (1);
        }
        return (0);
    }
}