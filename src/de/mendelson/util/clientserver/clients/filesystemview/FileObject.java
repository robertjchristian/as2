//$Header: /cvsroot-fuse/mec-as2/39/mendelson/util/clientserver/clients/filesystemview/FileObject.java,v 1.1 2012/04/18 14:10:44 heller Exp $
package de.mendelson.util.clientserver.clients.filesystemview;

import java.io.File;
import java.io.Serializable;
/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */

/**
 * Msg for the client server protocol
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class FileObject implements Serializable {

    public static final int TYPE_ROOT = 1;
    public static final int TYPE_DIR = 2;
    public static final int TYPE_FILE = 3;
    private File file;
    private String pathStr;
    private int type = TYPE_FILE;

    public FileObject(File file, int type) {
        this.type = type;
        this.file = file;
        this.pathStr = file.getAbsolutePath();
    }

    @Override
    public String toString() {
        if( this.type == TYPE_ROOT){
            return( this.file.toString());
        }
        return (this.getFile().getName());
    }

    /**
     * @return the file
     */
    public File getFile() {
        return file;
    }

    /**
     * @param file the file to set
     */
    public void setFile(File file) {
        this.file = file;
    }

    /**Overwrite the equal method of object
     *@param anObject object ot compare
     */
    @Override
    public boolean equals(Object anObject) {
        if (anObject == this) {
            return (true);
        }
        if (anObject != null && anObject instanceof FileObject) {
            FileObject otherObject = (FileObject) anObject;
            return (otherObject.getFile().equals(this.file));
        }
        return (false);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 17 * hash + (this.file != null ? this.file.hashCode() : 0);
        hash = 17 * hash + this.type;
        return hash;
    }

    /**
     * @return the type
     */
    public int getType() {
        return type;
    }

    /**
     * @return the pathStr
     */
    public String getPathStr() {
        return pathStr;
    }

}
