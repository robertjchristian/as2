//$Header: /cvsroot-fuse/mec-as2/39/mendelson/util/clientserver/ClientSessionHandler.java,v 1.1 2012/04/18 14:10:41 heller Exp $
package de.mendelson.util.clientserver;

import de.mendelson.util.clientserver.messages.ClientServerMessage;
import de.mendelson.util.clientserver.messages.ClientServerResponse;
import de.mendelson.util.clientserver.messages.LoginState;
import de.mendelson.util.clientserver.messages.ServerLogMessage;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Client side protocol handler
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class ClientSessionHandler extends IoHandlerAdapter {

    /**Callback that is used for event notification*/
    private final ClientSessionHandlerCallback callback;
    /**stores the sync requests*/
    private Map<Long, BlockingQueue<ClientServerResponse>> syncMap = new ConcurrentHashMap<Long, BlockingQueue<ClientServerResponse>>();
    /**Indicates if the server log messages should be displayed in the client log*/
    private boolean displayServerLogMessages = true;

    public ClientSessionHandler(ClientSessionHandlerCallback callback) {
        this.callback = callback;
    }

    /**Indicates if server log messages should be displayed in the client or
     * simply ignored*/
    public void setDisplayServerLogMessages(boolean flag) {
        this.displayServerLogMessages = flag;
    }

    @Override
    public void sessionCreated(IoSession session) throws Exception {
    }

    @Override
    public void sessionOpened(IoSession session) throws Exception {
        this.callback.connected(session.getRemoteAddress());
    }

    @Override
    public void messageSent(IoSession session, Object messageObj) {
    }

    @Override
    public void messageReceived(IoSession session, Object messageObj) throws Exception {
        if (messageObj == null) {
            this.callback.getLogger().log(Level.WARNING, "ClientSessionHandler.messageReceived: Received null type message.");
            return;
        }
        if (!(messageObj instanceof ClientServerMessage)) {
            this.callback.getLogger().log(Level.WARNING, "ClientSessionHandler.messageReceived: Client server message type is not supported: "
                    + messageObj.getClass().getName());
            return;
        }
        ClientServerMessage message = (ClientServerMessage) messageObj;
        //sync response: check if there was a request for this message
        if (message._isSyncRequest()) {
            ClientServerResponse response = (ClientServerResponse) message;
            if (!this.syncMap.containsKey(response.getReferenceId())) {
                Exception unreferredSyncResponse = new Exception("The client received a unreferred sync response. Type: " + response.getClass().getName()
                        + ", reference id: " + response.getReferenceId());
                this.callback.syncRequestFailed(unreferredSyncResponse);
            }
            BlockingQueue queue = this.syncMap.get(response.getReferenceId());
            queue.offer(response);
        }
        if (message instanceof LoginState) {
            LoginState state = (LoginState) message;
            if (state.getState() == LoginState.STATE_AUTHENTICATION_SUCCESS) {
                this.callback.loggedIn(state.getUser());
            } else if (state.getState() == LoginState.STATE_AUTHENTICATION_FAILURE_PASSWORD_REQUIRED) {
                this.callback.loginFailureServerRequestsPassword(state.getUser().getName());
            } else if (state.getState() == LoginState.STATE_INCOMPATIBLE_CLIENT) {
                this.callback.loginFailureIncompatibleClient();
            } else if (state.getState() == LoginState.STATE_AUTHENTICATION_REQUIRED) {
                this.callback.loginRequestedFromServer();
            } else {
                //STATE_AUTHENTICATION_FAILURE
                callback.loginFailure(state.getUser().getName());
            }
        } else if (message instanceof ServerLogMessage) {
            if (this.displayServerLogMessages) {
                //server log messages are just passed through to the client log if requested
                ServerLogMessage serverMessage = (ServerLogMessage) message;
                this.callback.getLogger().log(serverMessage.getLevel(), serverMessage.getMessage(),
                        serverMessage.getParameter());
            }
        } else {
            this.callback.messageReceivedFromServer((ClientServerMessage) message);
        }
    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        callback.disconnected();
    }

    /**
     * waits a specified time for an inbound message, used for sync processing*/
    public ClientServerResponse waitForSyncAnswer(Long referenceId, long timeoutInMS) throws Exception {
        if (!this.syncMap.containsKey(referenceId)) {
            Exception unreferredSyncResponse = new Exception("ClientSessionHandler.waitForSyncAnswer: "
                    + "The message that should be waited for is unreferred."
                    + " Reference id: " + referenceId);
            this.callback.syncRequestFailed(unreferredSyncResponse);
        }
        BlockingQueue<ClientServerResponse> queue = this.syncMap.get(referenceId);
        ClientServerResponse response = queue.poll(timeoutInMS, TimeUnit.MILLISECONDS);
        return (response);
    }

    public void addSyncRequest(ClientServerMessage request) {
        BlockingQueue<ClientServerResponse> queue = new LinkedBlockingQueue<ClientServerResponse>(1);
        this.syncMap.put(request.getReferenceId(), queue);
    }

    public void removeSyncRequest(ClientServerMessage request) {
        if (!this.syncMap.containsKey(request.getReferenceId())) {
            Exception unreferredSyncResponse = new Exception("ClientSessionHandler.removeSyncRequest: "
                    + "The message to remove from the sync map does not exist. "
                    + " Reference id: " + request.getReferenceId());
            this.callback.syncRequestFailed(unreferredSyncResponse);
        }
        BlockingQueue<ClientServerMessage> queue = new LinkedBlockingQueue<ClientServerMessage>(1);
        this.syncMap.remove(request.getReferenceId());
    }

    /**Inform the callback that a sync request failed*/
    public void syncRequestFailed(Throwable throwable) {
        this.callback.syncRequestFailed(throwable);
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) {
        cause.printStackTrace();
    }
}
