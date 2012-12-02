//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/webclient2/FilePanel.java,v 1.1 2012/04/18 14:10:41 heller Exp $
package de.mendelson.comm.as2.webclient2;

import com.vaadin.ui.TextArea;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Dialog that display a file content
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class FilePanel extends TextArea{
    
     private String displayedFilename = "";
    /**Max filesize for the display of data in the panel, actual 100kB*/
    private final static double MAX_FILESIZE = 100*Math.pow(2,10);
    
    public FilePanel() {
        this.setRows(15);
        this.setSizeFull();
    }
    
    /**Copies all data from one stream to another*/
    private void copyStreams(InputStream in, OutputStream out) throws IOException {
        BufferedInputStream inStream = new BufferedInputStream(in);
        BufferedOutputStream outStream = new BufferedOutputStream(out);
        //copy the contents to an output stream
        byte[] buffer = new byte[1024];
        int read = 1024;
        //a read of 0 must be allowed, sometimes it takes time to
        //extract data from the input
        while (read != -1) {
            read = inStream.read(buffer);
            if (read > 0) {
                outStream.write(buffer, 0, read);
            }
        }
        outStream.flush();
    }
    
    public void displayFile( File file ){
        boolean readOnlyStateOld = this.isReadOnly();
        //there will be displayed a new value to the panel
        this.setReadOnly(false);
        String filename = "";        
        if (file == null) {
            this.setValue("No file");
        } else if (!file.exists()) {
            this.setValue("File not found: " + file.getAbsolutePath());
            filename = file.getAbsolutePath();
        } else if (file.length() > MAX_FILESIZE) {
            this.setValue("Filesize too large to display");            
            filename = file.getAbsolutePath();
        } else {
            try {
                ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                FileInputStream inStream = new FileInputStream(file);
                this.copyStreams(inStream, outStream);
                inStream.close();
                outStream.flush();
                this.setValue(new String(outStream.toByteArray()));
                outStream.close();
                filename = file.getAbsolutePath();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.displayedFilename = filename;
        this.setReadOnly(readOnlyStateOld);
    }
    
    
    public String getDisplayedFilename() {
        return displayedFilename;
    }
    
}
