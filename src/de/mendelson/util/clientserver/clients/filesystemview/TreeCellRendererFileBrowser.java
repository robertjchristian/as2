//$Header: /cvsroot-fuse/mec-as2/39/mendelson/util/clientserver/clients/filesystemview/TreeCellRendererFileBrowser.java,v 1.1 2012/04/18 14:10:44 heller Exp $
package de.mendelson.util.clientserver.clients.filesystemview;

import java.awt.Component;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.filechooser.FileSystemView;
import javax.swing.filechooser.FileView;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * TreeCellRenderer that will display the icons of the config tree
 * @author  S.Heller
 * @version $Revision: 1.1 $
 */
public class TreeCellRendererFileBrowser extends DefaultTreeCellRenderer {

    private ImageIcon iconRoot = new ImageIcon(TreeCellRendererFileBrowser.class.getResource(
            "/de/mendelson/util/clientserver/clients/filesystemview/root16x16.gif"));
    private ImageIcon iconWait = new ImageIcon(TreeCellRendererFileBrowser.class.getResource(
            "/de/mendelson/util/clientserver/clients/filesystemview/waiting16x16.gif"));
    /**Stores the selected node*/
    private DefaultMutableTreeNode selectedNode = null;
    private boolean expanded = false;

    /**Constructor to create Renderer for console tree*/
    public TreeCellRendererFileBrowser() {
        super();
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree,
            Object selectedObject, boolean sel,
            boolean expanded,
            boolean leaf,
            int row, boolean hasFocus) {
        this.selectedNode = (DefaultMutableTreeNode) selectedObject;
        this.expanded = expanded;
        return (super.getTreeCellRendererComponent(tree, selectedObject, sel, expanded,
                leaf, row, hasFocus));
    }

    /**Returns the defined Icon of the entry, might be null if anything fails*/
    private Icon getDefinedIcon() {
        Object object = this.selectedNode.getUserObject();
        //is this root node?
        if (object == null) {
            return (super.getOpenIcon());
        }
        if (object instanceof String) {
            return (this.iconWait);
        }
        if (!(object instanceof FileObject)) {
            return (super.getOpenIcon());
        }
        FileObject userObject = (FileObject) object;
        if (userObject.getType() == FileObject.TYPE_DIR) {
            if (this.expanded) {
                return (super.getDefaultOpenIcon());
            } else {
                return (super.getDefaultClosedIcon());
            }
        } else if (userObject.getType() == FileObject.TYPE_ROOT) {
            return (this.iconRoot);
        }
        try {
            //this might fail
            FileSystemView view = FileSystemView.getFileSystemView();
            return (view.getSystemIcon(userObject.getFile()));
        } catch (Throwable e) {
            return( null );
        }
    }

    /**Gets the Icon by the type of the object*/
    @Override
    public Icon getLeafIcon() {
        Icon icon = this.getDefinedIcon();
        if (icon != null) {
            return (icon);
        }
        //nothing found: get default
        return (super.getLeafIcon());
    }

    @Override
    public Icon getOpenIcon() {
        Icon icon = this.getDefinedIcon();
        if (icon != null) {
            return (icon);
        }
        return (super.getOpenIcon());
    }

    @Override
    public Icon getClosedIcon() {
        Icon icon = this.getDefinedIcon();
        if (icon != null) {
            return (icon);
        }
        return (super.getClosedIcon());
    }
}