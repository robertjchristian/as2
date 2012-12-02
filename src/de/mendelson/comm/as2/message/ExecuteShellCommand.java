//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/message/ExecuteShellCommand.java,v 1.1 2012/04/18 14:10:30 heller Exp $
package de.mendelson.comm.as2.message;

import de.mendelson.comm.as2.log.LogAccessDB;
import de.mendelson.comm.as2.log.LogEntry;
import de.mendelson.comm.as2.partner.Partner;
import de.mendelson.comm.as2.partner.PartnerAccessDB;
import de.mendelson.comm.as2.server.AS2Server;
import de.mendelson.util.Exec;
import de.mendelson.util.MecResourceBundle;
import java.io.File;
import java.io.PrintStream;
import java.sql.Connection;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Allows to execute a shell command. This is used to execute a shell command on
 *message receipt
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class ExecuteShellCommand {

    private Logger logger = Logger.getLogger(AS2Server.SERVER_LOGGER_NAME);
    private MessageAccessDB messageAccess;
    /**Localize your GUI!*/
    private MecResourceBundle rb = null;
    //DB connection
    private Connection runtimeConnection;
    private Connection configConnection;

    public ExecuteShellCommand(Connection configConnection, Connection runtimeConnection) {
        this.runtimeConnection = runtimeConnection;
        this.configConnection = configConnection;
        this.messageAccess = new MessageAccessDB(configConnection, runtimeConnection);
        //Load resourcebundle
        try {
            this.rb = (MecResourceBundle) ResourceBundle.getBundle(
                    ResourceBundleExecuteShellCommand.class.getName());
        } //load up  resourcebundle
        catch (MissingResourceException e) {
            throw new RuntimeException("Oops..resource bundle " + e.getClassName() + " not found.");
        }
    }

    /**Executes a shell command for an inbound AS2 message if this has been defined in the partner settings
     */
    public void executeShellCommandOnSend(AS2MessageInfo messageInfo, AS2MDNInfo mdnInfo) {
        //do not execute a command for CEM messages
        if (messageInfo.getMessageType() == AS2Message.MESSAGETYPE_CEM) {
            return;
        }
        PartnerAccessDB partnerAccess = new PartnerAccessDB(this.configConnection, this.runtimeConnection);
        Partner messageSender = partnerAccess.getPartner(messageInfo.getSenderId());
        Partner messageReceiver = partnerAccess.getPartner(messageInfo.getReceiverId());
        List<AS2Payload> payload = this.messageAccess.getPayload(messageInfo.getMessageId());
        String rawCommand = null;
        if (messageInfo.getState() == AS2Message.STATE_FINISHED) {
            if (!messageReceiver.useCommandOnSendSuccess()) {
                return;
            } else {
                rawCommand = messageReceiver.getCommandOnSendSuccess();
                //empty command?
                if (rawCommand == null || rawCommand.trim().length() == 0) {
                    return;
                }
            }
        } else if (messageInfo.getState() == AS2Message.STATE_STOPPED) {
            if (!messageReceiver.useCommandOnSendError()) {
                return;
            } else {
                rawCommand = messageReceiver.getCommandOnSendError();                
                //empty command, huh?
                if (rawCommand == null || rawCommand.trim().length() == 0) {
                    return;
                }
            }
        } else {
            //state:pending should not execute anything
            return;
        }
        if (payload != null && payload.size() > 0) {
            this.logger.log(Level.INFO, this.rb.getResourceString("executing.send", messageInfo.getMessageId()), messageInfo);
            for (AS2Payload singlePayload : payload) {
                if (singlePayload.getPayloadFilename() == null) {
                    this.logger.warning("executeShellCommandOnSend: payload filename does not exist.");
                    continue;
                }
                String filename = singlePayload.getOriginalFilename();
                String command = this.replace(rawCommand, "${filename}", filename);
                command = this.replace(command, "${fullstoragefilename}", singlePayload.getPayloadFilename());
                command = this.replace(command, "${sender}", messageSender.getName());
                command = this.replace(command, "${receiver}", messageReceiver.getName());
                command = this.replace(command, "${messageid}", messageInfo.getMessageId());
                if (messageInfo.getSubject() != null) {
                    command = this.replace(command, "${subject}", messageInfo.getSubject());
                } else {
                    command = this.replace(command, "${subject}", "");
                }
                if (mdnInfo != null) {
                    command = this.replace(command, "${mdntext}", mdnInfo.getRemoteMDNText());
                } else {
                    command = this.replace(command, "${mdntext}", "");
                }
                //add log?
                if (command.contains("${log}")) {
                    try {
                        LogAccessDB logAccess = new LogAccessDB(this.configConnection, this.runtimeConnection);
                        LogEntry[] entries = logAccess.getLog(messageInfo.getMessageId());
                        StringBuilder logBuffer = new StringBuilder();
                        for (LogEntry logEntry : entries) {
                            logBuffer.append(logEntry.getMessage()).append("\\n");
                        }
                        //dont use single and double quotes, this is used in command line environment
                        String logText = this.replace(logBuffer.toString(), "\"", "");
                        logText = this.replace(logText, "'", "");
                        command = this.replace(command, "${log}", logText);
                    } catch (Exception e) {
                        this.logger.warning(e.getMessage());
                    }
                }
                this.logger.log(Level.INFO, this.rb.getResourceString("executing.command",
                        new Object[]{messageInfo.getMessageId(), command}), messageInfo);
                Exec exec = new Exec();
                try {
                    int returnCode = exec.start(command, new PrintStream(new AS2LoggerOutputStream(this.logger, messageInfo)),
                            new PrintStream(new AS2LoggerOutputStream(this.logger, messageInfo)));
                    this.logger.log(Level.INFO, this.rb.getResourceString("executed.command",
                            new Object[]{messageInfo.getMessageId(), String.valueOf(returnCode)}), messageInfo);
                } catch (Exception e) {
                    this.logger.warning(e.getMessage());
                }
            }
        } else {
            this.logger.warning("executeShellCommandOnSend: No payload found for message " + messageInfo.getMessageId());
        }
    }

    /**Executes a shell command for an inbound AS2 message if this has been defined in the partner settings
     */
    public void executeShellCommandOnReceipt(Partner messageSender, Partner messageReceiver, AS2MessageInfo messageInfo) {
        //do not execute a command for CEM messages
        if (messageInfo.getMessageType() == AS2Message.MESSAGETYPE_CEM) {
            return;
        }
        if (!messageSender.useCommandOnReceipt()) {
            return;
        }
        if (messageSender.getCommandOnReceipt() == null || messageSender.getCommandOnReceipt().trim().length() == 0) {
            return;
        }
        List<AS2Payload> payload = this.messageAccess.getPayload(messageInfo.getMessageId());
        if (payload != null) {
            this.logger.log(Level.INFO, this.rb.getResourceString("executing.receipt", messageInfo.getMessageId()), messageInfo);
            for (int i = 0; i < payload.size(); i++) {
                if (payload.get(i).getPayloadFilename() == null) {
                    continue;
                }
                String filename = payload.get(i).getPayloadFilename();
                String command = this.replace(messageSender.getCommandOnReceipt(), "${filename}",
                        new File(filename).getAbsolutePath());
                command = this.replace(command, "${sender}", messageSender.getName());
                command = this.replace(command, "${receiver}", messageReceiver.getName());
                command = this.replace(command, "${messageid}", messageInfo.getMessageId());
                if (messageInfo.getSubject() != null) {
                    command = this.replace(command, "${subject}", messageInfo.getSubject());
                } else {
                    command = this.replace(command, "${subject}", "");
                }
                this.logger.log(Level.INFO, this.rb.getResourceString("executing.command",
                        new Object[]{messageInfo.getMessageId(), command}), messageInfo);
                Exec exec = new Exec();
                try {
                    int returnCode = exec.start(command, new PrintStream(new AS2LoggerOutputStream(this.logger, messageInfo)),
                            new PrintStream(new AS2LoggerOutputStream(this.logger, messageInfo)));
                    this.logger.log(Level.INFO, this.rb.getResourceString("executed.command",
                            new Object[]{messageInfo.getMessageId(), String.valueOf(returnCode)}), messageInfo);
                } catch (Exception e) {
                    this.logger.warning(e.getMessage());
                }
            }
        }
    }

    /** Replaces the string tag by the string replacement in the sourceString
     * @param source Source string
     * @param tag	String that will be replaced
     * @param replacement String that will replace the tag
     * @return String that contains the replaced values
     */
    private String replace(String source, String tag, String replacement) {
        if (source == null) {
            return null;
        }
        StringBuilder buffer = new StringBuilder();
        while (true) {
            int index = source.indexOf(tag);
            if (index == -1) {
                buffer.append(source);
                return (buffer.toString());
            }
            buffer.append(source.substring(0, index));
            buffer.append(replacement);
            source = source.substring(index + tag.length());
        }
    }
}
