//$Header: /cvsroot-fuse/mec-as2/39/mendelson/util/ImageUtil.java,v 1.1 2012/04/18 14:10:41 heller Exp $
package de.mendelson.util;

import javax.swing.*;
import java.awt.image.*;
import java.awt.*;
/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */

/**Class that contains routines for the image processing
 *@author S.Heller
 *@version $Revision: 1.1 $
 */
public class ImageUtil {

    /**Composite used to paint the "hidden" element transparent
     */
    private Composite compositeTransparent = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);

    /**Replaces a single color in the passed image and returns the new one
     * @param background original image to set new rgb values in
     * @param hexColorOld RGB hex str for the old color to replace
     * @param hexColorNew RGB hex str for the new, replacing color
     */
    public ImageIcon replaceColor(ImageIcon background, String hexColorOld, String hexColorNew) {
        if( hexColorOld.startsWith( "#")){
            hexColorOld = hexColorOld.substring(1);
        }
        if( hexColorNew.startsWith( "#")){
            hexColorNew = hexColorNew.substring(1);
        }
        if( !hexColorOld.startsWith( "0x")){
            hexColorOld = "0x" + hexColorOld;
        }
        if( !hexColorNew.startsWith( "0x")){
            hexColorNew = "0x" + hexColorNew;
        }
        Color oldColor = Color.decode(hexColorOld);
        Color newColor = Color.decode(hexColorNew);
        int oldColorRGB = oldColor.getRGB();
        int newColorRGB = newColor.getRGB();
        BufferedImage image = new BufferedImage(
                background.getIconWidth(),
                background.getIconHeight(),
                BufferedImage.TYPE_INT_ARGB);
        Graphics g = image.getGraphics();
        g.drawImage(background.getImage(), 0, 0, null);
        for (int x = 0; x < background.getIconWidth(); x++) {
            for (int y = 0; y < background.getIconHeight(); y++) {
                if (image.getRGB(x, y) == oldColorRGB) {
                    image.setRGB(x, y, newColorRGB);
                }
            }
        }
        return (new ImageIcon(image));
    }

    /**Mixes two images, the foreground image is painted onto the
     *background image
     *@param background Background image, is painted first
     *@param foreground Foreground image, is painted second
     */
    public ImageIcon mixImages(ImageIcon background, ImageIcon foreground) {
        BufferedImage image = new BufferedImage(
                background.getIconWidth(),
                background.getIconHeight(),
                BufferedImage.TYPE_INT_ARGB);
        Graphics g = image.getGraphics();
        g.drawImage(background.getImage(), 0, 0, null);
        g.drawImage(foreground.getImage(), 0, 0, null);
        return (new ImageIcon(image));
    }

    /**Turns the passed icon into a transparent image, this is used to mark
     * a hidden element
     */
    public ImageIcon transparentImage(ImageIcon icon) {
        BufferedImage image = new BufferedImage(
                icon.getIconWidth(),
                icon.getIconHeight(),
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) image.getGraphics();
        g.setComposite(this.compositeTransparent);
        g.drawImage(icon.getImage(), 0, 0, null);
        return (new ImageIcon(image));
    }

    /**Turns the passed icon into a grayed out and returns it
     * @param brightness brightness from 0-100
     */
    public ImageIcon grayImage(ImageIcon icon, int brightness) {
        Image image = icon.getImage();
        ImageFilter filter = new GrayFilter(true, brightness);
        FilteredImageSource filteredSrc = new FilteredImageSource(image.getSource(), filter);
        image = Toolkit.getDefaultToolkit().createImage(filteredSrc);
        return (new ImageIcon(image));
    }
}