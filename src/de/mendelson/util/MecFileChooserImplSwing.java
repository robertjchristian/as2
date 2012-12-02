//$Header: /cvsroot-fuse/mec-as2/39/mendelson/util/MecFileChooserImplSwing.java,v 1.1 2012/04/18 14:10:41 heller Exp $
package de.mendelson.util;

import javax.swing.filechooser.*;
import javax.swing.text.JTextComponent;
import javax.swing.*;
import java.awt.Frame;
import java.io.*;
import java.util.*;


/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */


/**
 * Special extention of the standard filechooser class, also implements
 * a FileFilter
 * @author  S.Heller
 * @version $Revision: 1.1 $
 */
public class MecFileChooserImplSwing extends JFileChooser{
    
    /**ParentFrame of this component*/
    private Frame parentFrame = null;
    
    /**ResourceBundle to store localized informations*/
    private MecResourceBundle rb = null;
    
    /** Creates new MecFileChooser
     * Creates a new FileChooser with the given default directory
     * @param defaultDirectory Directory to start by default
     * @param dialogTitle Title to show at the chooser
     * @param parentFrame This is the parent component
     */
    public MecFileChooserImplSwing( Frame parentFrame,
            String defaultDirectory, String dialogTitle ) {
        super( defaultDirectory, FileSystemView.getFileSystemView() );
        //Load default resourcebundle
        try{
            this.rb = (MecResourceBundle)ResourceBundle.getBundle(
                    ResourceBundleMecFileChooser.class.getName());
        }
        //load up default english resourcebundle
        catch ( MissingResourceException e ) {
            throw new RuntimeException( "Oops..resource bundle "
                    + e.getClassName() + " not found" );
        }
        this.setDialogTitle( dialogTitle );
        this.parentFrame = parentFrame;
        this.setMultiSelectionEnabled( false );
    }
    
    /**Sets the type of the chooser: load*/
    public void setTypeLoad(){
        this.setDialogType( JFileChooser.OPEN_DIALOG );
    }
    
    /**Sets the type of the chooser: save*/
    public void setTypeSave(){
        this.setDialogType( JFileChooser.SAVE_DIALOG );
    }
    
    
    /**Browses for a filename and returns it
     * @param parent Parent component
     * @return null if the user cancels the action!
     */
    public String browseFilename(){
        this.showDialog( this.parentFrame, this.rb.getResourceString( "button.select" ));
        File file = this.getSelectedFile();
        //user pressed select button
        if( file != null ){
            return( file.getAbsolutePath() );
        }
        return( null );
    }
    
    
    /**Browses the directory for a filename
     * @param component JComponent where the chosen filename will displayed
     * @param filter FileFilters that are accepted
     */
    public String browseFilename( JComponent component, String[] filter){
        if( filter != null )
            this.addChoosableFileFilter( new MecFileFilter( filter ));
        this.showDialog( this.parentFrame, this.rb.getResourceString( "button.select" ));
        File file = this.getSelectedFile();
        //user pressed select button
        if( file == null )
            return( null );
        
        if( component != null ){
            if( component instanceof JTextComponent )
                ((JTextComponent)component).setText( file.getAbsolutePath());
            if( component instanceof JComboBox )
                this.setItem( (JComboBox)component,
                        file.getAbsolutePath() );
        }
        return( file.getAbsolutePath() );
    }
    
    /**Adds an item to a comboBox. If the item already exists,
     * it is set as selected
     * @param comboBox Component to set the item
     * @param item Object to write into the ComboBox
     */
    protected void setItem( JComboBox comboBox, Object item ){
        //Check if element exists. if the item exists, set it and return
        for( int i = 0; i < comboBox.getItemCount(); i++ )
            if( comboBox.getItemAt(i).equals( item )){
            comboBox.setSelectedIndex(i);
            return;
            }
        comboBox.addItem( item );
        comboBox.setSelectedItem( item );
    }
    
    /**Browses the directory for a filename, all files are accepted
     * @param component JComponent where the chosen filename will displayed
     */
    public String browseFilename(JComponent component){
        return( this.browseFilename( component, null ) );
    }
    
    /**Browses directories ONLY, no file selection allowed
     * @param component TextComponent where the chosen filename will displayed
     */
    public String browseDirectory(JComponent component){
        this.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
        return( this.browseFilename( component ) );
    }
    
    /**Browses directories ONLY, no file selection allowed
     */
    public String browseDirectory(){
        this.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
        return( this.browseFilename());
    }
    
    
    /**Filefilter for the chooser*/
    public static class MecFileFilter extends javax.swing.filechooser.FileFilter{
        
        private String filePath = "";
        /**Stores the possible file filter extentions*/
        protected String[] filter = null;
        
        public MecFileFilter( String[] filter ){
            super();
            this.filter = filter;
        }
        
        public boolean accept( File file ){
            if( file.isDirectory() )
                return( true );
            
            this.filePath = file.getPath().toLowerCase();
            //check accept
            if( this.filter != null ){
                boolean accept = false;
                for( int i = 0; i < this.filter.length; i++ )
                    if( this.filePath.endsWith( this.filter[i] )){
                    accept = true;
                    break;
                    }
                return( accept );
            }
            return( true );
        }
        
        /**return descriptions of choosable file extentions*/
        public String getDescription(){
            if( this.filePath.endsWith( ".xsl" ))
                return( "Format conversion (*.xsl)" );
            if( this.filePath.endsWith( ".xml" ))
                return( "Extended Markup Language (*.xml)" );
            if( this.filePath.endsWith( ".properties" ))
                return( "Properties File (*.properties)" );
            return( "" );
        }
    }
    
    
}
