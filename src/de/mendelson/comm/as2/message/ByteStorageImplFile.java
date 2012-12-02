//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/message/ByteStorageImplFile.java,v 1.1 2012/04/18 14:10:30 heller Exp $
package de.mendelson.comm.as2.message;

import de.mendelson.util.AS2Tools;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Container that stores byte arrays in a temp file
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class ByteStorageImplFile implements IByteStorage {

    private File file = null;

    public ByteStorageImplFile() {
    }

    @Override
    /**Returns the actual stored data size*/
    public int getSize() {
        if (this.file == null) {
            return (0);
        }
        return ((int) this.file.length());
    }

    @Override
    /**store a byte array*/
    public void put(byte[] data) throws Exception {
        //create the file storage
        this.file = AS2Tools.createTempFile("AS2ByteStorage", ".bin");
        ByteArrayInputStream inStream = new ByteArrayInputStream(data);
        FileOutputStream outStream = new FileOutputStream(this.file);
        this.copyStreams(inStream, outStream);
        outStream.flush();
        inStream.close();
        outStream.close();
    }

    @Override
    public byte[] get() throws Exception {
        if (this.file == null) {
            return (new byte[0]);
        }
        FileInputStream inStream = new FileInputStream(this.file);
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        this.copyStreams(inStream, outStream);
        outStream.flush();
        inStream.close();
        outStream.close();
        return (outStream.toByteArray());
    }

    @Override
    /**Returns an input stream to read directly from the underlaying buffer*/
    public InputStream getInputStream() throws Exception {
        return (new FileInputStream(this.file));
    }

    @Override
    public void release() {
        boolean deleted = this.file.delete();
        //do not use deleteOnExit in servers, memory leak!
//        if( !deleted ){
//            this.file.deleteOnExit();
//        }
    }

    /**Copies all data from one stream to another*/
    private final void copyStreams(InputStream in, OutputStream out) throws IOException {
        BufferedInputStream inStream = new BufferedInputStream(in);
        BufferedOutputStream outStream = new BufferedOutputStream(out);
        //copy the contents to an output stream
        byte[] buffer = new byte[2048];
        int read = 0;
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
}
