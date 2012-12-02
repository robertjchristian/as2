//$Header: /cvsroot-fuse/mec-as2/39/mendelson/util/clientserver/gui/JPanelGradientUI.java,v 1.1 2012/04/18 14:10:44 heller Exp $
package de.mendelson.util.clientserver.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import javax.swing.JComponent;
import javax.swing.plaf.basic.BasicPanelUI;
/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */

/** 
 * Panel UI that contains a gradient
 * @author  S.Heller
 * @version $Revision: 1.1 $
 */
public class JPanelGradientUI extends BasicPanelUI {

    private Color color;
    private float[] scales;
    private float[] offsets = new float[4];
    private RescaleOp operation;
    

    public JPanelGradientUI(Color color, float opacity){
        this.color = color;
        this.scales = new float[]{1f, 1f, 1f, opacity};
        this.operation = new RescaleOp(scales, offsets, null);
    }
    
    @Override
    public void paint(Graphics g, JComponent component) {
        try {
            Dimension componentSize = component.getSize();
            int componentWidth = componentSize.width;
            int componentHeight = componentSize.height;
            BufferedImage image = new BufferedImage(componentWidth, componentHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D imageGraphics = (Graphics2D)image.getGraphics();
            imageGraphics.setPaint(new GradientPaint(0, 0, this.color, 0,
                componentHeight, this.color.brighter(), false));
            imageGraphics.fillRect(0, 0, componentWidth, componentHeight);
            Graphics2D graph2d = (Graphics2D) g;
            //Best rendering quality
            graph2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            // Enable antialiasing for shapes
            graph2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graph2d.drawImage(image, this.operation, 0, 0);
        } catch (Exception e) {
            super.paint(g, component);
        }
    }
   
}