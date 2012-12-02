//$Header: /cvsroot-fuse/mec-as2/39/mendelson/util/clientserver/BaseClient.java,v 1.1 2012/04/18 14:10:41 heller Exp $
package de.mendelson.util.clientserver;

import de.mendelson.util.clientserver.messages.ClientServerMessage;
import de.mendelson.util.clientserver.messages.ClientServerResponse;
import de.mendelson.util.clientserver.messages.LoginRequest;
import de.mendelson.util.clientserver.messages.QuitRequest;
import de.mendelson.util.clientserver.user.User;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.serialization.ObjectSerializationCodecFactory;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.filter.executor.UnorderedThreadPoolExecutor;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Abstract client for a user
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class BaseClient {

    private Logger logger = Logger.getAnonymousLogger();
    private final ClientSessionHandler clientSessionHandler;
    private IoSession session = null;
    private NioSocketConnector connector;
    /**Host the client is connected to*/
    private String host = null;
    /**User that is connected*/
    private User user = null;    
        
    public BaseClient(ClientSessionHandlerCallback callback) {
        this.connector = new NioSocketConnector();
        this.clientSessionHandler = new ClientSessionHandler(callback);
    }

    /**Indicates if server log messages should be displayed in the client or
     * simply ignored*/
    public void setDisplayServerLogMessages( boolean flag ){
        this.clientSessionHandler.setDisplayServerLogMessages(flag);
    }
    
    /**Logs something to the clients log
     */
    public void log(Level logLevel, String message) {
        this.logger.log(logLevel, message);
    }

    public void setLogger(Logger logger) {
        if (logger != null) {
            this.logger = logger;
        }
    }

    public void login(String user, char[] passwd, String clientId) {
        if (!this.isConnected()) {
            throw new IllegalStateException("login: Not connected. Please connect first.");
        }
        LoginRequest login = new LoginRequest();
        login.setPasswd(passwd);
        login.setUserName(user);
        login.setClientId(clientId);
        this.session.write(login);
    }

    /**checks if the client is connected*/
    public boolean isConnected() {
        return (this.session != null && this.session.isConnected());
    }

    public boolean connect(InetSocketAddress hostAddress, long timeout) {
        if (this.isConnected()) {
            throw new IllegalStateException("Already connected to " + hostAddress + ". Disconnect first.");
        }
        try {
            this.connector.setConnectTimeoutMillis(timeout);
            this.connector.setHandler(this.clientSessionHandler);
            ObjectSerializationCodecFactory codecFactory = new ObjectSerializationCodecFactory();
            codecFactory.setDecoderMaxObjectSize(Integer.MAX_VALUE);
            codecFactory.setEncoderMaxObjectSize(Integer.MAX_VALUE);
            //add CPU bound tasks first
            this.connector.getFilterChain().addLast("codec", new ProtocolCodecFilter(codecFactory));
            //log client-server communication
            //this.connector.getFilterChain().addLast("logger", new LoggingFilter());
            //multi threaded model: allow and receive simulanously
            this.connector.getFilterChain().addLast("executor", new ExecutorFilter(new UnorderedThreadPoolExecutor()));
            ConnectFuture connFuture = this.connector.connect(hostAddress).awaitUninterruptibly();
            if (connFuture.isConnected()) {
                this.host = hostAddress.getHostName();
                this.session = connFuture.getSession();
                return (true);
            } else {
                this.connector.dispose();
                return (false);
            }
        } catch (Exception e) {
            this.log(Level.WARNING, "Connect: " + e.getMessage());
            return false;
        }
    }

    public void broadcast(ClientServerMessage message) {
        if (!this.isConnected()) {
            throw new IllegalStateException("broadcast: Not connected. Please connect first.");
        }
        this.session.write(message);
    }

    /**Sends an async message to the server and does not care for an answer*/
    public void sendAsync(ClientServerMessage message) {
        if (!this.isConnected()) {
            throw new IllegalStateException("sendAsync: Not connected. Please connect first.");
        }
        WriteFuture future = this.session.write(message);
    }

    /**Sends a sync message and throws a timeout exception if the client does
     * not answer in a proper time (5s)
     * @param message
     */
    public ClientServerResponse sendSync(ClientServerMessage request) {
        long timeout = TimeUnit.SECONDS.toMillis(5);
        return (this.sendSync(request, timeout));
    }

    /**Sends a sync message and throws a timeout exception if the client does
     * not answer in a proper time. The default timeout is 5s
     * @param message
     */
    public ClientServerResponse sendSync(ClientServerMessage request, long timeout) {
        if (!this.isConnected()) {
            throw new IllegalStateException("sendSync: Not connected. Please connect first.");
        }
        ClientServerResponse response = null;
        request._setSyncRequest(true);
        try {
            this.clientSessionHandler.addSyncRequest(request);
            WriteFuture writeFuture = this.session.write(request);
            boolean isSent = writeFuture.await(timeout, TimeUnit.MILLISECONDS);
            if (!isSent) {
                throw new TimeoutException("Timeout - Could not send sync request to server after " + timeout + "ms");
            }
            if (writeFuture.getException() != null) {
                throw (writeFuture.getException());
            }
            response = this.clientSessionHandler.waitForSyncAnswer(request.getReferenceId(), timeout);
            if (response == null) {
                throw new TimeoutException("Timeout - Could not receive the sync response after " + timeout + "ms");
            }
        } catch (Throwable throwable) {
            this.clientSessionHandler.syncRequestFailed(throwable);
        } finally {
            //remove the request from the map
            this.clientSessionHandler.removeSyncRequest(request);
        }
        return (response);
    }

    /**Performs a logout, closes the session
     */
    public void logout() {
        if (this.session != null) {
            if (this.session.isConnected()) {
                QuitRequest quitRequest = new QuitRequest();
                quitRequest.setUser(this.user.getName());
                this.session.write(quitRequest);
            }
            this.session.close(false);
        }
        this.host = null;
    }

    /**
     * @param user the user to set
     */
    public void setUser(User user) {
        this.user = user;
    }
}
