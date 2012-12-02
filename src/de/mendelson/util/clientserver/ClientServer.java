//$Header: /cvsroot-fuse/mec-as2/39/mendelson/util/clientserver/ClientServer.java,v 1.1 2012/04/18 14:10:41 heller Exp $
package de.mendelson.util.clientserver;

import de.mendelson.util.clientserver.messages.ClientServerMessage;
import java.net.InetSocketAddress;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.serialization.ObjectSerializationCodecFactory;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.filter.executor.UnorderedThreadPoolExecutor;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Server root for the mendelson client/server architecture
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class ClientServer {

    private long startTime = 0;
    private Logger logger;
    private ClientServerSessionHandler sessionHandler = null;
    private int port = 0;
    private String productName = "";

    /** Creates a new instance of Server
     */
    public ClientServer(Logger logger, int port) {
        this.port = port;
        this.logger = logger;
    }

    public void setSessionHandler(ClientServerSessionHandler sessionHandler) {
        this.sessionHandler = sessionHandler;
    }

    public void setClientServerPort(int port) {
        this.port = port;
    }

    /**Returns the start time of the server*/
    public long getStartTime() {
        return startTime;
    }

    /**Sends a message object to all connected clients*/
    public void broadcastToClients(ClientServerMessage message) {
        if (this.sessionHandler != null) {
            sessionHandler.broadcast(message);
        }
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    /**Finally starts the server*/
    public void start() throws Exception {
        this.logger.log(Level.INFO, "Starting " + this.productName + " client-server interface, listening on port " + this.port);
        if (this.sessionHandler != null) {
            this.sessionHandler.setProductName(this.productName);
        } else {
            this.logger.log(Level.WARNING, "No session handler assigned to the client server!");
        }
        NioSocketAcceptor acceptor = new NioSocketAcceptor();
        ObjectSerializationCodecFactory codecFactory = new ObjectSerializationCodecFactory();
        codecFactory.setDecoderMaxObjectSize(Integer.MAX_VALUE);
        codecFactory.setEncoderMaxObjectSize(Integer.MAX_VALUE);
        //add CPU bound tasks first
        acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(codecFactory));
        //log client-server communication
        //acceptor.getFilterChain().addLast("logger", new LoggingFilter());
        //see https://issues.apache.org/jira/browse/DIRMINA-682?page=com.atlassian.jira.plugin.system.issuetabpanels:all-tabpanel
        //..and now set up the thread pool
        acceptor.getFilterChain().addLast("executor", new ExecutorFilter(new UnorderedThreadPoolExecutor()));
        if (this.sessionHandler != null) {
            acceptor.setHandler(this.sessionHandler);
        }
        //finally bind the protocol handler to the port
        acceptor.bind(new InetSocketAddress(this.port));
        this.logger.log(Level.INFO, this.productName + " client-server interface started.");
        this.startTime = new Date().getTime();
    }
}
