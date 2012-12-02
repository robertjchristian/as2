//$Header: /cvsroot-fuse/mec-as2/39/mendelson/util/LockingGlassPane.java,v 1.1 2012/04/18 14:10:41 heller Exp $
package de.mendelson.util;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JPanel;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**Panel that could be used as glass pane. It will gray out the underlaying componet
 * and prevent any user interaction with it.
 * 
 * Lock the component: Add a glasspane that prevents any action on the UI
 *  private void lock() {
 *  //init glasspane for first use
 *  if (!(this.getGlassPane() instanceof LockingGlassPane)) {
 *      this.setGlassPane(new LockingGlassPane());
 *  }
 *  this.getGlassPane().setVisible(true);
 *  this.getGlassPane().requestFocusInWindow();
 * }
 *
 *Unlock the component: remove the glasspane that prevents any action on the UI
 *  private void unlock() {
 *      getGlassPane().setVisible(false);
 *  }
 * 
 * 
 *@author S.Heller
 *@version $Revision: 1.1 $
 */
public class LockingGlassPane extends JPanel {

    public LockingGlassPane() {
        this.setOpaque(false);
        this.setBackground(new Color(0, 0, 0, 25));
        this.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
                e.consume();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                e.consume();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                e.consume();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        });

        this.addKeyListener(new KeyListener() {

            @Override
            public void keyPressed(KeyEvent e) {
                e.consume();
            }

            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyReleased(KeyEvent e) {
                e.consume();
            }
        });
        setFocusTraversalKeysEnabled(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(this.getBackground());
        g.fillRect(0, 0, this.getSize().width, this.getSize().height);
    }
}