//$Header: /cvsroot-fuse/mec-as2/39/mendelson/util/ImageButtonBar.java,v 1.1 2012/04/18 14:10:41 heller Exp $
package de.mendelson.util;

import com.l2fprod.common.swing.JButtonBar;
import com.l2fprod.common.swing.plaf.blue.BlueishButtonBarUI;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
import javax.swing.JToggleButton;

/**
 * Panel that contains image buttons and could be used to group panels. 
 * Use it the following way:
 * 1.initialize it
 * 2.Add panels to it by addButton()
 * 
 * @author  S.Heller
 * @version $Revision: 1.1 $
 */
public class ImageButtonBar extends JPanel {

    public static final int VERTICAL = JButtonBar.VERTICAL;
    public static final int HORIZONTAL = JButtonBar.HORIZONTAL;
    private JButtonBar bar;
    private List<ImageButtonComponents> componentsList = new ArrayList<ImageButtonComponents>();

    private int preferredButtonWidth = -1;
    private int preferredButtonHeight = -1;
    
    public ImageButtonBar(final int DIRECTION) {
        if (DIRECTION != VERTICAL && DIRECTION != HORIZONTAL) {
            throw new IllegalArgumentException("ImageButtonBar: unsupported direction");
        }
        this.bar = new JButtonBar(DIRECTION);
        bar.setUI(new BlueishButtonBarUI());
    }

    public void setPreferredButtonSize( int width, int height  ){
        this.preferredButtonHeight = height;
        this.preferredButtonWidth = width;
    }
    
    /**Add a panel that is controlled by the passed icon
     * 
     * @param icon Icon to display
     * @param text Text taht is assigned to the icon
     * @param panel Assigned component
     * @param initialSelected selects the component initial if set
     */
    public void addButton(ImageIcon icon, String text, JComponent[] components, boolean initialSelected) {
        this.componentsList.add(new ImageButtonComponents(icon, text, components, initialSelected));
    }

    /**Add a panel that is controlled by the passed icon
     *
     * @param icon Icon to display
     * @param text Text taht is assigned to the icon
     * @param panel Assigned component
     * @param initialSelected selects the component initial if set
     */
    public void addButton(ImageIcon icon, String text, JComponent component, boolean initialSelected) {
        this.addButton(icon, text, new JComponent[]{ component}, initialSelected);
    }

    public void build() {
        ButtonGroup group = new ButtonGroup();
        for (ImageButtonComponents singlePanel : this.componentsList) {
            Action action = new ImageButtonBarAbstractActionImpl(singlePanel, this.componentsList);
            JToggleButton button = new JToggleButton(action);            
            if( this.preferredButtonHeight != -1 && this.preferredButtonWidth != -1 ){
                button.setPreferredSize(new Dimension(this.preferredButtonWidth, this.preferredButtonHeight));
            }
            this.bar.add(button);
            group.add(button);
            for (JComponent component : singlePanel.getComponents()) {
                component.setVisible(singlePanel.getInitialSelected());
            }
            button.setSelected(singlePanel.getInitialSelected());
        }
        this.setLayout(new BorderLayout());
        this.add(bar, BorderLayout.CENTER);
    }

    private static class ImageButtonComponents {

        private ImageIcon icon;
        private String text;
        private JComponent[] components;
        private boolean initialSelected = false;

        public ImageButtonComponents(ImageIcon icon, String text, JComponent[] components, boolean initialSelected) {
            this.icon = icon;
            this.text = text;
            this.components = components;
            this.initialSelected = initialSelected;
        }

        /**Overwrite the equal method of object
         *@param anObject object ot compare
         */
        @Override
        public boolean equals(Object anObject) {
            if (anObject == this) {
                return (true);
            }
            if (anObject != null && anObject instanceof ImageButtonComponents) {
                ImageButtonComponents otherComponent = (ImageButtonComponents) anObject;
                boolean componentsAreEqual = this.components.length == otherComponent.components.length;
                for (int i = 0; componentsAreEqual && i < this.components.length; i++) {
                    componentsAreEqual = componentsAreEqual && this.components[i].equals(otherComponent.components[i]);
                }
                return (this.text.equals(otherComponent.text) && this.icon.equals(otherComponent.icon) && componentsAreEqual);
            }
            return (false);
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 59 * hash + (this.icon != null ? this.icon.hashCode() : 0);
            hash = 59 * hash + (this.text != null ? this.text.hashCode() : 0);
            hash = 59 * hash + Arrays.deepHashCode(this.components);
            hash = 59 * hash + (this.initialSelected ? 1 : 0);
            return hash;
        }

        public JComponent[] getComponents() {
            return this.components;
        }

        public ImageIcon getIcon() {
            return icon;
        }

        public String getText() {
            return text;
        }

        public boolean getInitialSelected() {
            return initialSelected;
        }

        public void setInitialSelected(boolean initialSelected) {
            this.initialSelected = initialSelected;
        }

        public void setVisible(boolean flag) {
            for (JComponent component : this.components) {
                component.setVisible(flag);
            }

        }
    }

    private static class ImageButtonBarAbstractActionImpl extends AbstractAction {

        private ImageButtonComponents ownComponents;
        private List<ImageButtonComponents> componentList;

        public ImageButtonBarAbstractActionImpl(ImageButtonComponents ownComponents, List<ImageButtonComponents> componentList) {
            super(ownComponents.getText(), ownComponents.getIcon());
            this.ownComponents = ownComponents;
            this.componentList = componentList;
        }

        /**Invisible all added panels but own panel
         */
        public void actionPerformed(ActionEvent e) {
            for (ImageButtonComponents singleComponents : this.componentList) {
                singleComponents.setVisible(ownComponents.equals(singleComponents));
            }
        }
    }
}
