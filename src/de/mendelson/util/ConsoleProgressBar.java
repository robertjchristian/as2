//$Header: /cvsroot-fuse/mec-as2/39/mendelson/util/ConsoleProgressBar.java,v 1.1 2012/04/18 14:10:41 heller Exp $
package de.mendelson.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Progress bar to display on the console
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class ConsoleProgressBar {

    private static DecimalFormat format = new DecimalFormat("##0.00");

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        format.setDecimalFormatSymbols(symbols);
    }

    /**
     * Allows to display subpercents: A progress betrween startPercent and endPercent
     * which runs from 0..100%
     */
    public static void print(float percent, float startPercent, float endPercent) {
        float absolutePercent = startPercent + ((endPercent - startPercent) * percent/100f);
        print(absolutePercent);
    }

    public static void print(float percent) {
        StringBuilder barBuilder = new StringBuilder("[");
        for (int i = 0; i < 25; i++) {
            if (i < (percent / 4f)) {
                barBuilder.append("=");
            } else {
                barBuilder.append(" ");
            }
        }
        barBuilder.append("]  ");
        barBuilder.append(format.format(percent)).append("%");
        System.out.print("\r" + barBuilder.toString());
    }
}
