//$Header: /cvsroot-fuse/mec-as2/39/mendelson/util/MecFileChooser.java,v 1.1 2012/04/18 14:10:41 heller Exp $
package de.mendelson.util;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.zip.Adler32;
import javax.swing.AbstractAction;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.filechooser.FileSystemView;
import javax.swing.filechooser.FileView;
import javax.swing.text.JTextComponent;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * This shows a file chooser, it is possible to select a native or the
 * swing file chooser.
 * @author  S.Heller
 * @version $Revision: 1.1 $
 */
public class MecFileChooser extends JFileChooser {
    //supports only one type of choosers at the moment

    public static int TYPE_SWING = 1;
    /**ResourceBundle to store localized informations*/
    private MecResourceBundle rb = null;
    /**ParentFrame of this component*/
    private Frame parent = null;
    /**indicates if the user canceled the dialog*/
    private boolean canceled = false;
    /**Remind the selected directories and restore them if the widget is called in the same context again*/
    public final static Map<Long, File> lastGoodSelectionMap = new HashMap<Long, File>();
    /**Stores a unique id for this call*/
    private Long uniqueId = null;

    /** Creates new MecFileChooser
     * Creates a new FileChooser with the given default directory
     * @param defaultDirectory Directory to start by default, may be a file
     * @param dialogTitle Title to show at the chooser
     * @param parent parent component
     * @param TYPE type of the dialog to choose as defined in the class
     *@deprecated use the same method without the type settings
     */
    public MecFileChooser(Frame parent, String defaultDirectory,
            String dialogTitle, final int TYPE) {
        this(parent, dialogTitle);
        this.setPreselectedFile(new File(defaultDirectory));
    }

    /** Creates new MecFileChooser
     * Creates a new FileChooser with the given default directory
     * @param defaultDirectory Directory to start by default, may be a file
     * @param dialogTitle Title to show at the chooser
     * @param parent parent component
     */
    public MecFileChooser(Frame parent, String dialogTitle) {
        super(FileSystemView.getFileSystemView());
        //Load default resourcebundle
        try {
            this.rb = (MecResourceBundle) ResourceBundle.getBundle(
                    ResourceBundleMecFileChooser.class.getName());
        } //load up default english resourcebundle
        catch (MissingResourceException e) {
            throw new RuntimeException("Oops..resource bundle " + e.getClassName() + " not found");
        }
        this.parent = parent;
        this.setDialogTitle(dialogTitle);
        this.setMultiSelectionEnabled(false);
        //trow an exception to get the call stack. Then build a hash on this call stack to figure out the context
        try {
            throw new Exception("get callstack");
        } catch (Exception e) {
            StackTraceElement[] traceElements = e.getStackTrace();
            StringBuilder buffer = new StringBuilder();
            for (int i = 0; i < traceElements.length; i++) {
                buffer.append(traceElements[i].toString());
                if (i > 3) {
                    break;
                }
            }
            Adler32 adler32 = new Adler32();
            adler32.update(buffer.toString().getBytes());
            this.uniqueId = Long.valueOf(adler32.getValue());
            //does this call context already exist?
            if (lastGoodSelectionMap.containsKey(this.uniqueId)) {
                File lastGoodFile = lastGoodSelectionMap.get(this.uniqueId);
                if (lastGoodFile.exists()) {
                    if (lastGoodFile.isDirectory()) {
                        this.setCurrentDirectory(lastGoodFile);
                    } else {
                        this.setSelectedFile(lastGoodFile);
                    }
                }
            }
        }
    }

    /**Set a new file view to the chooser, e.g. to display new icons etc*/
    @Override
    public void setFileView(FileView fileview) {
        super.setFileView(fileview);
    }

    /**Sets the type of the chooser: load*/
    public void setTypeLoad() {
        this.setDialogType(JFileChooser.OPEN_DIALOG);
    }

    /**Sets the type of the chooser: save*/
    public void setTypeSave() {
        this.setDialogType(JFileChooser.SAVE_DIALOG);
    }

    /**Creates a chooser dialog and displays it*/
    private void showChooserDialog() {
        this.setApproveButtonText(this.rb.getResourceString("button.select"));
        final JDialog dialog = this.createDialog(this.parent);
        // Add listener for approve and cancel events
        this.addActionListener(new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent evt) {
                if (JFileChooser.CANCEL_SELECTION.equals(evt.getActionCommand())) {
                    canceled = true;
                    dialog.setVisible(false);
                } else if (JFileChooser.APPROVE_SELECTION.equals(evt.getActionCommand())) {
                    dialog.setVisible(false);
                }
            }
        });
        // Add listener for window closing events
        dialog.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                canceled = true;
            }
        });
        dialog.setVisible(true);
    }

    /**Browses for a filename and returns it
     * @param parent Parent component
     * @return null if the user cancels the action!
     */
    public String browseFilename() {
        this.showChooserDialog();
        if (this.canceled) {
            return (null);
        }
        File file = this.getSelectedFile();
        lastGoodSelectionMap.put(this.uniqueId, file);
        return (file.getAbsolutePath());
    }

    /**Browses the directory for a filename
     * @param component JComponent where the chosen filename will displayed
     * @param filter FileFilters that are accepted
     */
    public String browseFilename(JComponent component, String[] filter) {
        if (filter != null) {
            this.addChoosableFileFilter(new MecFileFilter(filter));
            //component is a text field
        }
        if (component instanceof JTextField) {
            JTextField textField = (JTextField) component;
            //if there is something typed in in the text field the already predefined file
            //or directory should be ignored
            if (textField.getText().length() > 0) {
                this.setPreselectedFile(new File(textField.getText()));
            }
        }
        this.showChooserDialog();
        if (this.canceled) {
            return (null);
        }
        File file = this.getSelectedFile();
        if (component != null) {
            if (component instanceof JTextComponent) {
                ((JTextComponent) component).setText(file.getAbsolutePath());
            }
            if (component instanceof JComboBox) {
                this.setItem((JComboBox) component, file.getAbsolutePath());
            }
        }
        return (file.getAbsolutePath());
    }

    /**Sets the preselected file in the directory chooser and returns if this was successful*/
    public void setPreselectedFile(File preselectedFile) {
        if (!preselectedFile.exists()) {
            //preselection file does not exist: first try to set the parent directory as default.
            //if this does not work use the users home directory
            File parentDir = preselectedFile.getParentFile();
            if( parentDir != null && parentDir.exists()){
                this.setCurrentDirectory(parentDir);
            }else{
                this.setCurrentDirectory(new File(System.getProperty("user.dir")));
            }
        } else if (!preselectedFile.isDirectory()) {
            String localParent = preselectedFile.getParent();
            if (localParent != null) {
                this.setCurrentDirectory(new File(localParent));
            }
            this.setSelectedFile(preselectedFile);
        } else {
            this.setCurrentDirectory(preselectedFile);
        }
    }

    /**Sets the preselected directory in the directory chooser*/
    @Override
    public void setCurrentDirectory(File preselectedDir) {
        if (preselectedDir != null && !preselectedDir.exists()) {
            super.setCurrentDirectory(new File(System.getProperty("user.dir")));
        } else {
            super.setCurrentDirectory(preselectedDir);
        }
    }

    /**Adds an item to a comboBox. If the item already exists,
     * it is set as selected
     * @param comboBox Component to set the item
     * @param item Object to write into the ComboBox
     */
    private void setItem(JComboBox comboBox, Object item) {
        //Check if element exists. if the item exists, set it and return
        for (int i = 0; i < comboBox.getItemCount(); i++) {
            if (comboBox.getItemAt(i).equals(item)) {
                comboBox.setSelectedIndex(i);
                return;
            }
        }
        comboBox.addItem(item);
        comboBox.setSelectedItem(item);
    }

    /**Browses the directory for a filename, all files are accepted. If there is
     * already a file name displayed in the component, its path will set as
     * default path.
     * @param component JComponent where the chosen filename will displayed
     *@return null if the user cancels the action!
     */
    public String browseFilename(JComponent component) {
        return (this.browseFilename(component, null));
    }

    /**Browses directories ONLY, no file selection allowed
     * @param component TextComponent where the chosen filename will displayed
     */
    public String browseDirectory(JComponent component) {
        this.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        return (this.browseFilename(component));
    }

    /**Browses directories ONLY, no file selection allowed
     */
    public String browseDirectory() {
        this.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        return (this.browseFilename());
    }

    /**Filefilter for the chooser*/
    public static class MecFileFilter extends javax.swing.filechooser.FileFilter {

        private String filePath = "";
        /**Stores the possible file filter extentions*/
        protected String[] filter = null;

        public MecFileFilter(String[] filter) {
            super();
            this.filter = filter;
        }

        @Override
        public boolean accept(File file) {
            if (file.isDirectory()) {
                return (true);
            }
            this.filePath = file.getPath().toLowerCase();
            //check accept
            if (this.filter != null) {
                boolean accept = false;
                for (int i = 0; i < this.filter.length; i++) {
                    if (this.filePath.endsWith(this.filter[i])) {
                        accept = true;
                        break;
                    }
                }
                return (accept);
            }
            return (true);
        }

        /**return descriptions of choosable file extentions*/
        @Override
        public String getDescription() {
            if (this.filePath.endsWith(".xsl")) {
                return ("Format conversion (*.xsl)");
            }
            if (this.filePath.endsWith(".xml")) {
                return ("Extended Markup Language (*.xml)");
            }
            if (this.filePath.endsWith(".properties")) {
                return ("Properties File (*.properties)");
            }
            return ("");
        }
    }
}
