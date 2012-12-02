//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/send/RawMessageSender.java,v 1.1 2012/04/18 14:10:35 heller Exp $
package de.mendelson.comm.as2.send;

import de.mendelson.comm.as2.AS2ServerVersion;
import de.mendelson.comm.as2.client.rmi.GenericClient;
import de.mendelson.comm.as2.clientserver.ErrorObject;
import de.mendelson.comm.as2.clientserver.serialize.CommandObjectIncomingMessage;
import de.mendelson.comm.as2.server.AS2Server;
import de.mendelson.util.security.BCCryptoHelper;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Logger;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Raw data uploader, mainly for test purpose. Sends a already fully prepared AS2 message to a specified sender
 * @author  S.Heller
 * @version $Revision: 1.1 $
 */
public class RawMessageSender {

    private Logger logger = Logger.getLogger(AS2Server.SERVER_LOGGER_NAME);

    /** Creates new raw message sender
     */
    public RawMessageSender() {
    }

    private CommandObjectIncomingMessage send(File rawDataFile, File headerFile) throws Exception {
        Properties header = new Properties();
        FileInputStream headerStream = new FileInputStream(headerFile);
        header.load(headerStream);
        headerStream.close();
        GenericClient client = new GenericClient();
        CommandObjectIncomingMessage commandObject = new CommandObjectIncomingMessage();
        commandObject.setMessageDataFilename(rawDataFile.getAbsolutePath());
        commandObject.setHeader(header);
        commandObject.setContentType(header.getProperty("content-type"));
        commandObject.setRemoteHost("localhost");
        ErrorObject errorObject = client.send(commandObject);
        if (errorObject.getErrors() > 0) {
            StringBuilder exceptionBuffer = new StringBuilder();
            ArrayList log = commandObject.getLog();
            for (int i = 0; i < log.size(); i++) {
                if (log.get(i) != null) {
                    this.logger.severe(log.get(i).toString());
                    exceptionBuffer.append(log.get(i).toString()).append("\n");
                }
            }
            throw new Exception(exceptionBuffer.toString());
        }
        return ((CommandObjectIncomingMessage) client.getCommandObject());
    }

    /**Displays a usage of how to use this class
     */
    public static void printUsage() {
        System.out.println("java " + RawMessageSender.class.getName() + " <options>");
        System.out.println("Start up a " + AS2ServerVersion.getProductNameShortcut() + " server ");
        System.out.println("Options are:");
        System.out.println("-datafile <String>: File that contains the AS2 message, fully packed");
        System.out.println("-headerfile <String>: File that contains the AS2 message header");
    }

    public static final void main(String[] args) {
        String file = null;
        String header = null;
        int optind;
        for (optind = 0; optind < args.length; optind++) {
            if (args[optind].toLowerCase().equals("-datafile")) {
                file = args[++optind];
            } else if (args[optind].toLowerCase().equals("-headerfile")) {
                header = args[++optind];
            } else if (args[optind].toLowerCase().equals("-?")) {
                RawMessageSender.printUsage();
                System.exit(1);
            } else if (args[optind].toLowerCase().equals("-h")) {
                RawMessageSender.printUsage();
                System.exit(1);
            } else if (args[optind].toLowerCase().equals("-help")) {
                RawMessageSender.printUsage();
                System.exit(1);
            }
        }
        if (file == null) {
            System.err.println("Parameter missing: " + "datafile");
            System.exit(1);
        }
        if (header == null) {
            System.err.println("Parameter missing: " + "headerfile");
            System.exit(1);
        }
        RawMessageSender sender = new RawMessageSender();
        try {
            //register the database drivers for the VM
            Class.forName("org.hsqldb.jdbcDriver");
            //initialize the security provider
            BCCryptoHelper helper = new BCCryptoHelper();
            helper.initialize();
            CommandObjectIncomingMessage command = sender.send(new File(file), new File(header));
            if (command.getMDNData() != null) {
                Logger.getLogger(AS2Server.SERVER_LOGGER_NAME).info(new String(command.getMDNData()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
