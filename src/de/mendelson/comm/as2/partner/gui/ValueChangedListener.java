//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/partner/gui/ValueChangedListener.java,v 1.1 2012/04/18 14:10:32 heller Exp $
package de.mendelson.comm.as2.partner.gui;

import java.util.*;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 *Listener that listens to changes in a table
 * @author  S.Heller
 * @version $Revision: 1.1 $
 */
public interface ValueChangedListener extends EventListener {
    public abstract void valueChanged(ValueChangedEvent e);
    
    public class ValueChangedEvent extends EventObject {
        
        /*the row that has changed*/
        private int row;
        
        /*the col that has changed*/
        private int col;
        
        /*the new value*/
        private Object value;
        
        /**returns the row that changed*/
        public int getRow() {
            return this.row;
        }
        
        /**returns the col that changed*/
        public int getCol() {
            return this.col;
        }
        
        /**returns the new value*/
        public Object getValue() {
            return this.value;
        }
        
        public ValueChangedEvent(Object source, int row, int col, Object value) {
            super(source);
            this.row = row;
            this.col = col;
            this.value = value;
        }
    }
    
}
