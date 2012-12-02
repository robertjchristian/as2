//$Header: /cvsroot-fuse/mec-as2/39/mendelson/util/clientserver/ClientServerSessionHandler.java,v 1.1 2012/04/18 14:10:41 heller Exp $
package de.mendelson.util.clientserver;

import de.mendelson.util.clientserver.messages.ClientServerMessage;
import de.mendelson.util.clientserver.messages.LoginRequest;
import de.mendelson.util.clientserver.messages.LoginState;
import de.mendelson.util.clientserver.messages.QuitRequest;
import de.mendelson.util.clientserver.messages.ServerInfo;
import de.mendelson.util.clientserver.messages.ServerLogMessage;
import de.mendelson.util.clientserver.user.PermissionDescription;
import de.mendelson.util.clientserver.user.User;
import de.mendelson.util.clientserver.user.UserAccess;
import de.mendelson.util.log.LogFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Session handler for the server implemetation
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class ClientServerSessionHandler extends IoHandlerAdapter {

    private static final String SESSION_ATTRIB_USER = "user";
    /**User readable description of user permissions*/
    private PermissionDescription permissionDescription = null;
    private Logger logger = Logger.getAnonymousLogger();
    /**Synchronized structure to perform user defined processing on the server depending on the
     *incoming message object type
     */
    private final List<ClientServerProcessing> processingList = Collections.synchronizedList(new ArrayList<ClientServerProcessing>());
    /**Stores the product name, this is displayed on login requests
     */
    private String productName = "";
    /**Stores all sessions*/
    private final List<IoSession> sessions = Collections.synchronizedList(new ArrayList<IoSession>());
    private PasswordValidationHandler loginHandler;
    private Logger serverSessionLogger = Logger.getAnonymousLogger();

    public ClientServerSessionHandler(Logger logger, String[] validClientIds) {
        if (logger != null) {
            this.logger = logger;
        }
        this.loginHandler = new PasswordValidationHandler(validClientIds);
        this.serverSessionLogger.setUseParentHandlers(false);
        // Create a file handler that uses 3 logfiles, each with a limit of 1Mbyte
        String serverSessionLogPattern = "client_server_session%g.log";
        int limit = 1000000; // 1 Mb
        int numLogFiles = 3;
        try {
            FileHandler fileHandler = new FileHandler(serverSessionLogPattern, limit, numLogFiles);
            this.serverSessionLogger.addHandler(fileHandler);
            fileHandler.setFormatter(new LogFormatter());
        } catch (Exception e) {
            logger.warning("Unable to initialize the server session handler: " + e.getMessage());
        }
    }

    /**Logs something to the clients log - but only if the level is higher than the
     *defined loglevelThreshold
     */
    public void log(Level logLevel, String message) {
        this.logger.log(logLevel, message);
    }

    private void logSession(IoSession session, Level logLevel, String message) {
        StringBuilder builder = new StringBuilder();
        builder.append("Session ");
        builder.append(session.getId()).append(" ");
        if (session.getRemoteAddress() != null) {
            builder.append("[").append(session.getRemoteAddress()).append("] ");
        }
        builder.append(message);
        this.serverSessionLogger.log(logLevel, builder.toString());
    }

    @Override
    /**The session has been opened: send a server info object*/
    public void sessionOpened(IoSession session) {
        this.logSession(session, Level.INFO, "Incoming connection");
        //send information about what this server is
        ServerInfo info = new ServerInfo();
        info.setProductname(this.productName);
        session.write(info);
        //request a login
        LoginState state = new LoginState();
        state.setState(LoginState.STATE_AUTHENTICATION_REQUIRED);
        session.write(state);
    }

    public void setPermissionDescription(PermissionDescription permissionDescription) {
        this.permissionDescription = permissionDescription;
    }

    /**Incoming message on the server site*/
    @Override
    public void messageReceived(IoSession session, Object message) {
        if (message instanceof QuitRequest) {
            session.close(false);
            return;
        }
        //it is a login request
        if (message instanceof LoginRequest) {
            LoginRequest request = (LoginRequest) message;
            UserAccess access = new UserAccess(this.logger);
            //validate passwd first, close session if it fails
            User definedUser = access.readUser(request.getUserName());
            if (definedUser != null && this.permissionDescription != null) {
                definedUser.setPermissionDescription(this.permissionDescription);
            }
            User transmittedUser = new User();
            transmittedUser.setName(request.getUserName());
            transmittedUser.setPasswdCrypted(request.getCryptedPasswd());
            int validationState = this.loginHandler.validate(definedUser, transmittedUser,
                    request.getClientId());
            if (validationState == PasswordValidationHandler.STATE_FAILURE) {
                this.logSession(session, Level.INFO, "Authentication failed for user " + request.getUserName());
                LoginState state = new LoginState();
                state.setUser(transmittedUser);
                state.setState(LoginState.STATE_AUTHENTICATION_FAILURE);
                session.write(state);
                return;
            } else if (validationState == PasswordValidationHandler.STATE_INCOMPATIBLE_CLIENT) {
                this.logSession(session, Level.INFO, "Authentication failed: incompatible client");
                LoginState state = new LoginState();
                state.setUser(transmittedUser);
                state.setState(LoginState.STATE_INCOMPATIBLE_CLIENT);
                session.write(state);
                boolean closeImmediately = false;
                session.close(closeImmediately);
                return;
            } else if (validationState == PasswordValidationHandler.STATE_PASSWORD_REQUIRED) {
                this.logSession(session, Level.INFO, "Authentication failed, no password send for user " + request.getUserName());
                LoginState state = new LoginState();
                state.setUser(transmittedUser);
                state.setState(LoginState.STATE_AUTHENTICATION_FAILURE_PASSWORD_REQUIRED);
                session.write(state);
                return;
            }
            session.setAttribute(SESSION_ATTRIB_USER, request.getUserName());
            synchronized (this.sessions) {
                this.sessions.add(session);
            }
            this.logSession(session, Level.INFO, "Authentication successful, user " + definedUser.getName() + " logged in");
            //success!
            LoginState state = new LoginState();
            state.setUser(definedUser);
            state.setState(LoginState.STATE_AUTHENTICATION_SUCCESS);
            session.write(state);
            return;
        }
        boolean loggedIn = session.containsAttribute(SESSION_ATTRIB_USER);
        //user not logged in so far
        if (!loggedIn) {
            LoginState state = new LoginState();
            User userObj = new User();
            if (this.permissionDescription != null) {
                userObj.setPermissionDescription(this.permissionDescription);
            }
            state.setUser(userObj);
            //login before requesting server services
            state.setState(LoginState.STATE_AUTHENTICATION_FAILURE);
            session.write(state);
            session.close(false);
            return;
        }
        //here starts the user defined processing to extend the server functionality
        this.performUserDefinedProcessing(session, (ClientServerMessage) message);
    }

    /**User defined extensions for the server processing*/
    private synchronized void performUserDefinedProcessing(IoSession session, ClientServerMessage message) {
        boolean processed = false;
        for (int i = 0; i < this.processingList.size(); i++) {
            processed |= this.processingList.get(i).process(session, message);
        }
        if (!processed) {
            this.log(Level.WARNING, "performUserDefinedProcessing: inbound message of class "
                    + message.getClass().getName() + " has not been processed.");
        }
    }

    /**User defined actions for messages sent by any client. The user may extend the framework by
     *implementing a ServerProcessing interface
     */
    public void addServerProcessing(ClientServerProcessing serverProcessing) {
        synchronized (this.processingList) {
            this.processingList.add(serverProcessing);
        }
    }

    /**Sends a message object to all connected clients*/
    public void broadcast(Object data) {
        synchronized (this.sessions) {
            for (IoSession session : this.sessions) {
                if (session.isConnected()) {
                    session.write(data);
                }
            }
        }
    }

    /**Sends a log message to all connected clients*/
    public void broadcastLogMessage(Level level, String message, Object[] parameter) {
        ServerLogMessage serverMessage = new ServerLogMessage();
        serverMessage.setLevel(level);
        serverMessage.setMessage(message);
        serverMessage.setParameter(parameter);
        this.broadcast(serverMessage);
    }

    /**Sends a log message to all connected clients*/
    public void broadcastLogMessage(Level level, String message) {
        this.broadcastLogMessage(level, message, null);
    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        String user = (String) session.getAttribute(SESSION_ATTRIB_USER);
        if (user != null) {
            this.logSession(session, Level.INFO, "Closed");
            synchronized (this.sessions) {
                this.sessions.remove(session);
            }
            //this.log(Level.INFO, "Session closed for user " + user);
        }
    }

    @Override
    public void sessionIdle(IoSession session, IdleStatus status) {
        // disconnect an idle client
        session.close(false);
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) {
        this.logSession(session, Level.WARNING, "Exception caught: " + cause.getMessage());
        cause.printStackTrace();
        // Close connection when unexpected exception is caught.
        session.close(true);
    }

    public int getConnectedClients() {
        return (this.sessions.size());
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }
}
