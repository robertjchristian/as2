//$Header: /cvsroot-fuse/mec-as2/39/mendelson/util/log/IRCColors.java,v 1.1 2012/04/18 14:10:45 heller Exp $
package de.mendelson.util.log;

import java.awt.Color;
/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */

/**
 * IRC colors to use in the log. To create messages that are colorized in IRC style
 * just add these constants to the data, like
 *<pre>
 * String myMessage = IRCColors.RED + "Error: " + IRCColors.NORMAL + "An error occured.";
 *</pre>
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class IRCColors {

    /** IRC indexed colors. They are used if an index is passed for the color and not the
     * color code
     *Color code list
     * 0 white
     * 1 black
     * 2 blue     (navy)
     * 3 green
     * 4 red
     * 5 brown    (maroon)
     * 6 purple
     * 7 orange   (olive)
     * 8 yellow
     * 9 lt.green (lime)
     * 10 teal    (a kinda green/blue cyan)
     * 11 lt.cyan (cyan ?) (aqua)
     * 12 lt.blue (royal)
     * 13 pink    (light purple) (fuchsia)
     * 14 grey
     * 15 lt.grey (silver)
     */
    protected static final Color[] indexedColors = {
        Color.white,
        Color.black,
        new Color(0x00007f),
        new Color(0x009300),
        Color.red.darker(),
        new Color(0x7f0000),
        new Color(0x9c009c),
        new Color(0xfc7f00),
        Color.yellow,
        Color.green,
        new Color(0x009393),
        // new Color(0x00ffff),
        Color.cyan,
        Color.blue,
        // new Color(0xff00ff),
        Color.magenta,
        // new Color(0x7f7f7f),
        Color.darkGray,
        // new Color(0xd2d2d2),
        Color.lightGray
    };

    public static final Color COLOR_WHITE = indexedColors[0];
    public static final Color COLOR_BLACK = indexedColors[1];
    public static final Color COLOR_NAVY = indexedColors[2];
    public static final Color COLOR_GREEN = indexedColors[3];
    public static final Color COLOR_RED = indexedColors[4];
    public static final Color COLOR_BROWN = indexedColors[5];
    public static final Color COLOR_PURPLE = indexedColors[6];
    public static final Color COLOR_ORANGE = indexedColors[7];
    public static final Color COLOR_YELLOW = indexedColors[8];
    public static final Color COLOR_LIME = indexedColors[9];
    public static final Color COLOR_CYAN = indexedColors[10];
    public static final Color COLOR_AQUA = indexedColors[11];
    public static final Color COLOR_ROYAL = indexedColors[12];
    public static final Color COLOR_FUCHSIA = indexedColors[13];
    public static final Color COLOR_GRAY = indexedColors[14];
    public static final Color COLOR_SILVER = indexedColors[15];
    

    /**
     * Removes all previously applied color and formatting attributes.
     */
    public static final String NORMAL = "\u000f";
    /**
     * Bold text.
     */
    public static final String BOLD = "\u0002";
    /**
     * Underlined text.
     */
    public static final String UNDERLINE = "\u001f";
    /**
     * Reversed text (may be rendered as italic text sometimes).
     */
    public static final String ITALIC = "\u0016";
    /**
     * White colored text.
     */
    public static final String WHITE = "\u000300";
    /**
     * Black colored text.
     */
    public static final String BLACK = "\u000301";
    /**
     * Dark blue colored text.
     */
    public static final String DARK_BLUE = "\u000302";
    /**
     * Dark green colored text.
     */
    public static final String DARK_GREEN = "\u000303";
    /**
     * Red colored text.
     */
    public static final String RED = "\u000304";
    /**
     * Brown colored text.
     */
    public static final String BROWN = "\u000305";
    /**
     * Purple colored text.
     */
    public static final String PURPLE = "\u000306";
    /**
     * Olive colored text.
     */
    public static final String OLIVE = "\u000307";
    /**
     * Yellow colored text.
     */
    public static final String YELLOW = "\u000308";
    /**
     * Green colored text.
     */
    public static final String GREEN = "\u000309";
    /**
     * Teal colored text.
     */
    public static final String TEAL = "\u000310";
    /**
     * Cyan colored text.
     */
    public static final String CYAN = "\u000311";
    /**
     * Blue colored text.
     */
    public static final String BLUE = "\u000312";
    /**
     * Magenta colored text.
     */
    public static final String MAGENTA = "\u000313";
    /**
     * Dark gray colored text.
     */
    public static final String DARK_GRAY = "\u000314";
    /**
     * Light gray colored text.
     */
    public static final String LIGHT_GRAY = "\u000315";

    /**Returns a java color object for the passed IRC constant*/
    public static Color toColor(String ircColor) {
        int index = Integer.valueOf(ircColor.substring(1)).intValue();
        return (indexedColors[index]);
    }

    /**
     * Removes all colors and formatting from a line that contains IRC style formatting
     */
    public static String removeIRCColors(String line) {
        int length = line.length();
        StringBuilder buffer = new StringBuilder();
        int i = 0;
        while (i < length) {
            char ch = line.charAt(i);
            //remove formatting
            if (ch == '\u000f' || ch == '\u0002' || ch == '\u001f' || ch == '\u0016') {
                i++;
                continue;
            }
            //remove colors
            if (ch == '\u0003') {
                i++;
                // Skip "x" or "xy" (foreground color).
                if (i < length) {
                    ch = line.charAt(i);
                    if (Character.isDigit(ch)) {
                        i++;
                        if (i < length) {
                            ch = line.charAt(i);
                            if (Character.isDigit(ch)) {
                                i++;
                            }
                        }
                        // Now skip ",x" or ",xy" (background color).
                        if (i < length) {
                            ch = line.charAt(i);
                            if (ch == ',') {
                                i++;
                                if (i < length) {
                                    ch = line.charAt(i);
                                    if (Character.isDigit(ch)) {
                                        i++;
                                        if (i < length) {
                                            ch = line.charAt(i);
                                            if (Character.isDigit(ch)) {
                                                i++;
                                            }
                                        }
                                    } else {
                                        // Keep the comma.
                                        i--;
                                    }
                                } else {
                                    // Keep the comma.
                                    i--;
                                }
                            }
                        }
                    }
                }
            } else if (ch == '\u000f') {
                i++;
            } else {
                buffer.append(ch);
                i++;
            }
        }
        return (buffer.toString());
    }
}
