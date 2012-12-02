//$Header: /cvsroot-fuse/mec-as2/39/mendelson/util/Exec.java,v 1.1 2012/04/18 14:10:41 heller Exp $
package de.mendelson.util;

import java.io.*;
/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */

/**
 * Executes a native command
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class Exec {

    /**Indicates if the exec should stop the calling thread to wait for
     * a return*/
    private boolean waitFor = false;

    /** Creates new Exec
     */
    public Exec() {
    }

    /**Indicates if the exec should stop the calling thread to wait for
     * a return*/
    public void setWaitFor(boolean waitFor) {
        this.waitFor = waitFor;
    }

    /**Starts a native command and writes the output to the
     * passed printstreams
     * @param command command line to execute on the system
     * @param out PrintStream to write normal output to, System.out if parameter is null
     * @param err PrintStream to write error to, System.err if parameter is null
     *@return Returnvalue of the call if waitfor is set, else 0
     */
    public int start(String command, PrintStream out, PrintStream err)
            throws IOException, InterruptedException {
        if (out == null) {
            out = System.out;
        }
        if (err == null) {
            err = System.err;
        }
        int returnValue = 0;
        ExecArgumentParser parser = new ExecArgumentParser();
        String[] arguments = parser.parse(command);
        Process process = Runtime.getRuntime().exec(arguments);
        // copy input and error to the output stream
        StreamPumper inputPumper = new StreamPumper(process.getInputStream(), out);
        StreamPumper errorPumper = new StreamPumper(process.getErrorStream(), err);
        // starts pumping away the generated output/error
        inputPumper.start();
        errorPumper.start();
        if (this.waitFor) {
            returnValue = process.waitFor();
            process.destroy();
        }
        return (returnValue);
    }

    /**Starts a native command and writes the output to stdout and stderr
     * @param command command line to execute on the system
     */
    public int start(String command) throws IOException, InterruptedException {
        return (this.start(command, null, null));
    }

    /**Thread that reads continuesly the output/input stream data from the 
     *native thread and redirects it to a printstream*/
    public static class StreamPumper extends Thread {

        /**Reader to read the data from*/
        private BufferedInputStream inStream;
        private boolean endOfStream = false;
        private final int SLEEP_TIME = 3;
        private final int BUFFER_SIZE = 2048;
        /**Stream to write the pumped info into*/
        private PrintStream outputStream = null;

        /**Create a pumper*/
        public StreamPumper(InputStream is, PrintStream outputStream) {
            this.outputStream = outputStream;
            this.inStream = new BufferedInputStream(is);
        }

        /**Explicit pump of the stream*/
        private void pumpStream() throws IOException {
            byte[] buf = new byte[BUFFER_SIZE];
            int read = 0;
            if (!endOfStream) {
                read = this.inStream.read(buf);
                if (read > 0) {
                    outputStream.write(buf, 0, read);
                }else if (read == -1) {
                    endOfStream = true;
                }
            }
        }

        /**Start method of the thread*/
        public void run() {
            try {
                try {
                    while (!endOfStream) {
                        pumpStream();
                        sleep(SLEEP_TIME);
                    }
                } catch (InterruptedException ie) {
                //nop
                }
                inStream.close();
            } catch (Exception ioe) {
            //nop, ignore this
            }
        }
    }
}
