//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/message/ByteStorage.java,v 1.1 2012/04/18 14:10:30 heller Exp $
package de.mendelson.comm.as2.message;

import java.io.InputStream;
import java.io.Serializable;

/**
 * Container that stores byte arrays
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class ByteStorage implements Serializable {

    /**Switch to file storage at 3 MB payload size*/
    private final int THRESHOLD = 3* 1024 * 1024;
    private IByteStorage storage = null;

    public ByteStorage() {
    }

    /**Returns the actual stored data size*/
    public int getSize() {
        if (this.storage == null) {
            return (0);
        }
        return (this.storage.getSize());
    }

    /**store a byte array*/
    public void put(byte[] data) throws Exception {
        //release an existing storage if it exists
        if (this.storage != null) {
            this.storage.release();
        }
        if (data.length > THRESHOLD) {
            this.storage = new ByteStorageImplFile();
        } else {
            this.storage = new ByteStorageImplMemory();
        }
        this.storage.put(data);
    }

    public byte[] get() throws Exception {
        if (this.storage == null) {
            return (new byte[0]);
        } else {
            return (this.storage.get());
        }
    }

    /**Returns an input stream to read directly from the underlaying buffer*/
    public InputStream getInputStream() throws Exception {
        return (this.storage.getInputStream());
    }
}
