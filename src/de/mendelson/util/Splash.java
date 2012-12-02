//$Header: /cvsroot-fuse/mec-as2/39/mendelson/util/Splash.java,v 1.1 2012/04/18 14:10:41 heller Exp $
package de.mendelson.util;

import javax.swing.*;

import java.awt.*;
import java.awt.image.*;
import java.awt.geom.*;
import java.awt.font.*;
import java.util.*;
import java.io.*;
import java.text.*;
import java.util.List;
import javax.imageio.ImageIO;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Splash window to been shown while one of the mendelson products load
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class Splash extends JWindow implements SwingConstants {

    /**Image to display*/
    private BufferedImage image = null;
    /**PrintStream to pass to out components to let them write stuff into*/
    private SplashPrintStream out = null;
    /**Indicates if this splash should have a progress bar, this is done
     *if this is != null
     */
    private Progress progress = null;
    /**List of display string to display static in the Splash*/
    private List<DisplayString> displayStringList = new ArrayList<DisplayString>();
    /**Indicates if textual output should be antialiased in the splash*/
    private boolean textAntialiasing = true;

    /**Creates a new splash with the given width and length
     * @param imageResource ResourcePath to the image
     */
    public Splash(String imageResource) {
        this.image = this.loadImage(imageResource);
        this.getContentPane().setLayout(new BorderLayout());
        int width = this.image.getWidth(this);
        int height = this.image.getHeight(this);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.setBounds((screenSize.width - width) / 2,
                (screenSize.height - height) / 2, width, height);
    }

    /**Adds a static display string to the splash, this is always painted
     *using the passed parameters
     *@param font Font to use to display the text in the splash
     *@param x X position of the output
     *@param y y position of the output
     *@param text Text to display
     *@param color Color to use for the text display
     *@param transform Allows to transform the font in any kind. Please
     *remember that there is always a translation on the font, depending
     *on the position! This parameter may be null.
     */
    public void addDisplayString(Font font, int x, int y, String text,
            Color color, AffineTransform transform) {
        DisplayString displayString = new DisplayString(font, x, y, text, color, transform);
        this.displayStringList.add(displayString);
    }

    /**Defines where to write the output to, with which properties
     *@param font Font to use
     *@param x X Position of the output
     *@param y Y Position of the output
     *@param fontColor Font color to use
     */
    public PrintStream createPrintStream(Font font, int x, int y, Color fontColor) {
        StringBuffer buffer = new StringBuffer();
        StringBufferOutputStream outStream = new StringBufferOutputStream(this, buffer);
        this.out = new SplashPrintStream(outStream, buffer, font, x, y, fontColor);
        return (this.out);
    }

    /**Passes a progress container to this splash. By passing a progress this is
     *shown!
     *@param x XPos of the bar
     *@param y yPos of the bar
     *@param height Bars height
     *@param width Bars width
     *@param border set this to null to not draw a border
     *@param foreground foreground color of the bar
     *@param background set this to null to have a transparent background
     *@param showPercent indicates to show the progress in procent
     */
    public Splash.Progress createProgress(
            int x, int y, int height, int width, Color foreground,
            Color background, Color border, boolean showPercent) {
        this.progress = new Progress(x, y, height, width,
                foreground, background, border, showPercent);
        return (this.progress);
    }

    /**Enables or disables the antialiasing of text output on the Splash*/
    public void setTextAntiAliasing(boolean textAntialiasing) {
        this.textAntialiasing = textAntialiasing;
    }

    /**Draw/update this component*/
    public void paint(Graphics g) {
        if (this.image == null) {
            return;
        }
        //draw into the memory image off-screen
        BufferedImage memoryImage = new BufferedImage(
                this.image.getWidth(this), this.image.getHeight(this),
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D imageGraphics = (Graphics2D) memoryImage.getGraphics();
        imageGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        imageGraphics.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        if (this.textAntialiasing) {
            imageGraphics.setRenderingHint(
                    RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        } else {
            imageGraphics.setRenderingHint(
                    RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        //copy the image into memory, off-screen
        }
        imageGraphics.drawImage(this.image, 0, 0, this);
        FontRenderContext renderContext = imageGraphics.getFontRenderContext();
        //display static strings in Splash
        for (int i = 0; i < this.displayStringList.size(); i++) {
            DisplayString displayString = (DisplayString) this.displayStringList.get(i);
            TextLayout layout = new TextLayout(displayString.getText(),
                    displayString.getFont(), renderContext);
            AffineTransform transformPosition = new AffineTransform();
            transformPosition.setToTranslation(
                    displayString.getX(), displayString.getY());
            //only concat a transform if the display string has one
            if (displayString.getTransform() != null) {
                transformPosition.concatenate(displayString.getTransform());
            }
            Shape shape = layout.getOutline(transformPosition);
            imageGraphics.setColor(displayString.getColor());
            imageGraphics.fill(shape);
        }
        //draw printstream output
        if (this.out != null) {
            imageGraphics.setFont(this.out.getFont());
            imageGraphics.setColor(this.out.getFontColor());
            imageGraphics.drawString(this.out.getText(),
                    this.out.getX(), this.out.getY());
        }
        //draw progress bar
        if (this.progress != null) {
            if (this.progress.getBorder() != null) {
                imageGraphics.setColor(this.progress.getBorder());
                imageGraphics.drawRect(this.progress.getX() - 1, this.progress.getY() - 1,
                        this.progress.getWidth() + 1, this.progress.getHeight() + 1);
            }
            if (this.progress.getBackground() != null) {
                if (this.progress.getUseGradientPaint()) {
                    //draw background of the progress bar with 3d effects
                    GradientPaint paint = new GradientPaint(
                            this.progress.getX(),
                            this.progress.getY() - this.progress.getHeight(),
                            this.progress.getBackground().darker(),
                            this.progress.getX(),
                            this.progress.getY() + this.progress.getHeight() / 2,
                            this.progress.getBackground());
                    imageGraphics.setPaint(paint);
                } else {
                    imageGraphics.setPaint(progress.getBackground());
                }
                imageGraphics.fillRect(this.progress.getX(), this.progress.getY(),
                        this.progress.getWidth(), this.progress.getHeight() / 2);
                if (this.progress.getUseGradientPaint()) {
                    //draw background of the progress bar with 3d effects
                    GradientPaint paint = new GradientPaint(
                            this.progress.getX(),
                            this.progress.getY() + this.progress.getHeight() / 2,
                            this.progress.getBackground(),
                            this.progress.getX(),
                            this.progress.getY() + 2 * this.progress.getHeight(),
                            this.progress.getBackground().brighter());
                    imageGraphics.setPaint(paint);
                } else {
                    imageGraphics.setPaint(progress.getBackground());
                }
                imageGraphics.fillRect(this.progress.getX(),
                        this.progress.getY() + this.progress.getHeight() / 2,
                        this.progress.getWidth(), this.progress.getHeight() / 2);
            }
            if (this.progress.getUseGradientPaint()) {
                //draw bar itself, with 3D effect
                GradientPaint paint = new GradientPaint(
                        this.progress.getX(), this.progress.getY(),
                        this.progress.getForeground(),
                        this.progress.getX(),
                        this.progress.getY() + this.progress.getHeight(),
                        Color.white);
                imageGraphics.setPaint(paint);
            } else {
                imageGraphics.setPaint(progress.getForeground());
            }
            imageGraphics.fillRect(this.progress.getX(), this.progress.getY(),
                    this.progress.getProgress(), this.progress.getHeight() / 2);
            if (this.progress.getUseGradientPaint()) {
                //draw bar itself, with 3D effect
                GradientPaint paint = new GradientPaint(
                        this.progress.getX(), this.progress.getY(),
                        Color.white,
                        this.progress.getX(),
                        this.progress.getY() + this.progress.getHeight(),
                        this.progress.getForeground());
                imageGraphics.setPaint(paint);
            } else {
                imageGraphics.setPaint(progress.getForeground());
            }
            imageGraphics.fillRect(this.progress.getX(),
                    this.progress.getY() + this.progress.getHeight() / 2,
                    this.progress.getProgress(), this.progress.getHeight() / 2);
            if (this.progress.showPercent()) {
                TextLayout layout = new TextLayout(this.progress.getPercent(),
                        new Font("Dialog", Font.PLAIN, (int) (this.progress.getHeight() / 2)),
                        renderContext);
                AffineTransform transformPosition = new AffineTransform();
                transformPosition.setToTranslation(
                        this.progress.getX() + this.progress.getWidth() / 2,
                        (int) (this.progress.getY() + this.progress.getHeight() - this.progress.getHeight() * 0.25));
                Shape shape = layout.getOutline(transformPosition);
                imageGraphics.setColor(progress.getForeground());
                imageGraphics.fill(shape);
                imageGraphics.clip(new Rectangle(this.progress.getX(), this.progress.getY(),
                        this.progress.getProgress(), this.progress.getHeight()));
                imageGraphics.setColor(progress.getBackground());
                imageGraphics.fill(shape);
            }
        }
        //bring the image to the screen
        g.setPaintMode();
        g.drawImage(memoryImage, 0, 0, memoryImage.getWidth(this),
                memoryImage.getHeight(this), this);
    }

    /**Loads the image and tracks it
     * @param resource image resource to load the image
     */
    private BufferedImage loadImage(String resource) {
        BufferedImage bufferedImage = null;
        try {
            //get an input stream from the resource
            InputStream inStream = Splash.class.getResourceAsStream(resource);
            bufferedImage = ImageIO.read(inStream);
            inStream.close();
        } catch (Exception e) {
            System.err.println("Fatal: Unable to load splash image resource " + resource + ".");
            System.exit(-1);
        }
        return (bufferedImage);
    }

    @Override
    public void setVisible(boolean flag) {
        super.setVisible(flag);
        if (flag) {
            this.toFront();
        }
    }

    /**removes the splash window
     */
    public void destroy() {
        this.setVisible(false);
    }

    /**PrintStream that will stores also information about position of the output
     *and refreshed the passed component whenever a \n occurs
     */
    public static class SplashPrintStream extends PrintStream {

        /**Font to use for the text printstream output in splash*/
        private Font font = new Font("Dialog", Font.PLAIN, 10);
        /**Font color to use in splash*/
        private Color fontColor = Color.black;
        /**Position to use to write output to*/
        private int outputX = 0;
        private int outputY = 0;
        private StringBuffer buffer = new StringBuffer();

        /**
         *@param @param outStream stream to write entries to a string buffer
         *@param font Font to use
         *@param x X Position of the output
         *@param y Y Position of the output
         *@param fontColor Font color to use
         */
        public SplashPrintStream(StringBufferOutputStream outStream,
                StringBuffer buffer,
                Font font, int x, int y, Color fontColor) {
            super(outStream);
            this.buffer = buffer;
            this.font = font;
            this.outputX = x;
            this.outputY = y;
            this.fontColor = fontColor;
        }

        public Font getFont() {
            return (this.font);
        }

        public int getX() {
            return (this.outputX);
        }

        public int getY() {
            return (this.outputY);
        }

        public Color getFontColor() {
            return (this.fontColor);
        }

        public String getText() {
            return (this.buffer.toString());
        }
    }

    /**Output Stream that writes it output to a stringbuffer, the buffer is only
     *refreshed if a \n occurs
     **/
    public static class StringBufferOutputStream extends OutputStream {

        private StringBuffer buffer = null;
        private Component component = null;
        /**TempBuffer is necessary because an update of the component could be
         *forced outside this class, the LAST valid value is always in the passed
         *buffer pointer. Whenever a \n accurs, the pass buffer will get a
         *valid value and store it until the next \n appears
         */
        private StringBuffer tempBuffer = new StringBuffer();

        /**@param component Component to update on an end of a line
         *@param buffer Buffer to write output to
         */
        public StringBufferOutputStream(Component component, StringBuffer buffer) {
            this.buffer = buffer;
            this.component = component;
        }

        public void write(int i) throws IOException {
            char addChar = this.int2char(i);
            if (addChar == '\n') {
                this.buffer.delete(0, this.buffer.length());
                this.buffer.append(tempBuffer.toString());
                if (this.component.getGraphics() != null) {
                    this.component.update(this.component.getGraphics());
                }
                tempBuffer.delete(0, this.buffer.length());
            } else {
                this.tempBuffer.append(addChar);
            }
        }

        /**Map bytes to characters, bytes are always signed in java!*/
        private final char int2char(int i) {
            return (char) ((i < 0) ? i + 0x100 : i);
        }
    }

    /**Class that stores progress information*/
    public class Progress {

        /**Position and size of the progress bar*/
        private int x = 0;
        private int y = 0;
        private int height = 0;
        private int width = 0;
        /**Progress bar border*/
        private Color background = Color.white;
        /**Progress bar color itself*/
        private Color foreground = Color.blue;
        /**Border color for the progress bar*/
        private Color border = Color.darkGray;
        /**Progress state*/
        private int maxProgress = 0;
        private int actualProgress = 0;
        private boolean showPercent = false;
        /**Format to format the percent output*/
        private DecimalFormat format = new DecimalFormat("0.0");
        /**Indicates if the bar should be rendered with a 3D effect*/
        private boolean useGradientPaint = true;

        /**Initialize the progress bar
         *@param x XPos of the bar
         *@param y yPos of the bar
         *@param height Bars height
         *@param width Bars width
         *@param background color, set this to null to have a transparent background
         *@param foreground bar color
         *@param border progress bar border color set this to null to not have a border
         *@param showPercent indicates to show the progress in procent
         */
        public Progress(int x, int y, int height, int width,
                Color foreground, Color background, Color border, boolean showPercent) {
            this.x = x;
            this.y = y;
            this.height = height;
            this.width = width;
            this.foreground = foreground;
            this.background = background;
            this.border = border;
            this.showPercent = showPercent;
        }

        /**Number of steps untill 100%*/
        public void setMax(int max) {
            this.maxProgress = max;
        }

        public int getX() {
            return (this.x);
        }

        public int getY() {
            return (this.y);
        }

        public int getWidth() {
            return (this.width);
        }

        public int getHeight() {
            return (this.height);
        }

        /**indicates if the progress bar should be painted using a 3D effect*/
        public void setUseGradientPaint(boolean useGradientPaint) {
            this.useGradientPaint = useGradientPaint;
        }

        /**indicates if the progress bar should be painted using a 3D effect*/
        public boolean getUseGradientPaint() {
            return (this.useGradientPaint);
        }

        /**Increates the progress
         */
        public void inc() {
            this.actualProgress++;
            //force a graphic refresh
            update(getGraphics());
        }

        /**gets the progress in pixel, depends on the width!
         */
        public int getProgress() {
            return ((int) (((float) this.width / (float) maxProgress) * (float) this.actualProgress));
        }

        /**Background color*/
        public Color getBackground() {
            return (this.background);
        }

        /**Foreground color*/
        public Color getForeground() {
            return (this.foreground);
        }

        /**Border color*/
        public Color getBorder() {
            return (this.border);
        }

        public boolean showPercent() {
            return (this.showPercent);
        }

        /**Returns the percent string to show*/
        public String getPercent() {
            return (format.format(this.getPercentValue()) + '%');
        }

        /**Returns the computed progress percent*/
        public float getPercentValue() {
            float percent = 0.0f;
            if (this.maxProgress > 0) {
                percent = (float) this.actualProgress * 100f / (float) this.maxProgress;
            }
            return (percent);
        }
    }

    /**Class that stores output strings to display in the splash screen. This
     *text will appear on each update of the component!
     */
    public static class DisplayString {

        /**Font to use for the output*/
        private Font font = null;
        /**Position where to output the string*/
        private int x = 0;
        private int y = 0;
        private String text = null;
        private Color color = null;
        private AffineTransform transform = null;

        /**@param font Font to use to display the text in the splash
         *@param x X position of the output
         *@param y y position of the output
         *@param text Text to display
         *@param color Color to use for the text display
         *@param transform Allows to transform the font in any kind. Please
         *remember that there is always a translation on the font, depending
         *on the position! This parameter may be null.
         */
        public DisplayString(Font font, int x, int y, String text, Color color,
                AffineTransform transform) {
            this.font = font;
            this.x = x;
            this.y = y;
            this.text = text;
            this.color = color;
            this.transform = transform;
        }

        public int getX() {
            return (this.x);
        }

        public int getY() {
            return (this.y);
        }

        public Font getFont() {
            return (this.font);
        }

        public String getText() {
            return (this.text);
        }

        public Color getColor() {
            return (this.color);
        }

        public AffineTransform getTransform() {
            return (this.transform);
        }
    }
}
