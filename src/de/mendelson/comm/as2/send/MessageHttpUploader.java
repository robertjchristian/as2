//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/send/MessageHttpUploader.java,v 1.1 2012/04/18 14:10:35 heller Exp $
package de.mendelson.comm.as2.send;

import de.mendelson.comm.as2.client.rmi.GenericClient;
import de.mendelson.comm.as2.clientserver.ErrorObject;
import de.mendelson.comm.as2.clientserver.message.RefreshClientMessageOverviewList;
import de.mendelson.comm.as2.clientserver.serialize.CommandObjectIncomingMessage;
import de.mendelson.comm.as2.message.AS2Info;
import de.mendelson.comm.as2.message.AS2MDNInfo;
import de.mendelson.comm.as2.message.AS2Message;
import de.mendelson.comm.as2.message.AS2MessageInfo;
import de.mendelson.comm.as2.message.MDNAccessDB;
import de.mendelson.comm.as2.message.MessageAccessDB;
import de.mendelson.comm.as2.message.store.MessageStoreHandler;
import de.mendelson.comm.as2.partner.HTTPAuthentication;
import de.mendelson.comm.as2.partner.Partner;
import de.mendelson.comm.as2.partner.PartnerHttpHeader;
import de.mendelson.comm.as2.preferences.PreferencesAS2;
import de.mendelson.comm.as2.server.AS2Server;
import de.mendelson.comm.as2.statistic.QuotaAccessDB;
import de.mendelson.util.AS2Tools;
import de.mendelson.util.MecResourceBundle;
import de.mendelson.util.clientserver.ClientServer;
import de.mendelson.util.security.BCCryptoHelper;
import de.mendelson.util.security.cert.KeystoreStorage;
import de.mendelson.util.security.cert.KeystoreStorageImplFile;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.sql.Connection;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthState;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.NoConnectionReuseStrategy;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;


/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Class to allow HTTP multipart uploads
 * @author  S.Heller
 * @version $Revision: 1.1 $
 */
public class MessageHttpUploader {

    private Logger logger = null;
    private PreferencesAS2 preferences = new PreferencesAS2();
    /**localisze the GUI*/
    private MecResourceBundle rb = null;
    /**The header that has been built fro the request*/
    private Properties requestHeader = new Properties();
    /**remote answer*/
    private byte[] responseData = null;
    /**remote answer*/
    private Header[] responseHeader = null;
    /**remote answer*/
    private StatusLine responseStatusLine = null;
    private ClientServer clientserver = null;
    //DB connection
    private Connection configConnection = null;
    private Connection runtimeConnection = null;
    //keystore data
    private KeystoreStorage certStore = null;
    private KeystoreStorage trustStore = null;

    /** Creates new message uploader instance
     * @param hostname Name of the host to connect to
     * @param username Name of the user that will connect to the remote ftp server
     * @param password password to connect to the ftp server
     */
    public MessageHttpUploader() throws Exception {
        //Load default resourcebundle
        try {
            this.rb = (MecResourceBundle) ResourceBundle.getBundle(
                    ResourceBundleHttpUploader.class.getName());
        } //load up resourcebundle
        catch (MissingResourceException e) {
            throw new RuntimeException("Oops..resource bundle " + e.getClassName() + " not found.");
        }
    }

    /**Sets keystore parameter for SSL sending. This is only necessary if HTTPS is the
     * protocol used for the message POST
     * @param truststore Truststore file
     * @param truststorePass Password for the truststore
     * @param certstore Keystore file
     * @param certstorePass Password for the keystore
     */
    public void setSSLParameter(KeystoreStorage certStore, KeystoreStorage trustStore) {
        this.certStore = certStore;
        this.trustStore = trustStore;
    }

    /**Passes a logger to this class for logging purpose*/
    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    /**Passes a server instance to this class to refresh messages automatically for logging purpose*/
    public void setAbstractServer(ClientServer clientserver) {
        this.clientserver = clientserver;
    }

    /**Pass a DB connection to this class for loggin purpose*/
    public void setDBConnection(Connection configConnection, Connection runtimeConnection) {
        this.configConnection = configConnection;
        this.runtimeConnection = runtimeConnection;
    }

    /**Returns the created header for the sent data*/
    public Properties upload(HttpConnectionParameter connectionParameter, AS2Message message, Partner sender, Partner receiver) throws Exception {
        NumberFormat formatter = new DecimalFormat("0.00");
        AS2Info as2Info = message.getAS2Info();
        MessageAccessDB messageAccess = null;
        MDNAccessDB mdnAccess = null;
        if (this.runtimeConnection != null && messageAccess == null && !as2Info.isMDN()) {
            messageAccess = new MessageAccessDB(this.configConnection, this.runtimeConnection);
            messageAccess.initializeOrUpdateMessage((AS2MessageInfo) as2Info);
        } else if (this.runtimeConnection != null && as2Info.isMDN()) {
            mdnAccess = new MDNAccessDB(this.configConnection, this.runtimeConnection);
            mdnAccess.initializeOrUpdateMDN((AS2MDNInfo) as2Info);
        }
        if (this.clientserver != null) {
            this.clientserver.broadcastToClients(new RefreshClientMessageOverviewList());
        }
        long startTime = System.currentTimeMillis();
        //sets the global requestHeader
        int returnCode = this.performUpload(connectionParameter, message, sender, receiver);
        long size = message.getRawDataSize();
        long transferTime = System.currentTimeMillis() - startTime;
        float bytePerSec = (float) ((float) size * 1000f / (float) transferTime);
        float kbPerSec = (float) (bytePerSec / 1024f);
        if (returnCode == HttpServletResponse.SC_OK) {
            if (this.logger != null) {
                this.logger.log(Level.INFO,
                        this.rb.getResourceString("returncode.ok",
                        new Object[]{
                            as2Info.getMessageId(),
                            String.valueOf(returnCode),
                            AS2Tools.getDataSizeDisplay(size),
                            AS2Tools.getTimeDisplay(transferTime),
                            formatter.format(kbPerSec),}), as2Info);
            }
        } else if (returnCode == HttpServletResponse.SC_ACCEPTED || returnCode == HttpServletResponse.SC_CREATED || returnCode == HttpServletResponse.SC_NO_CONTENT || returnCode == HttpServletResponse.SC_RESET_CONTENT || returnCode == HttpServletResponse.SC_PARTIAL_CONTENT) {
            if (this.logger != null) {
                this.logger.log(Level.INFO,
                        this.rb.getResourceString("returncode.accepted",
                        new Object[]{
                            as2Info.getMessageId(),
                            String.valueOf(returnCode),
                            AS2Tools.getDataSizeDisplay(size),
                            AS2Tools.getTimeDisplay(transferTime),
                            formatter.format(kbPerSec),}), as2Info);
            }
        } else {
            //the system was unable to connect the partner
            if (returnCode < 0) {
                throw new NoConnectionException(this.rb.getResourceString("error.noconnection", as2Info.getMessageId()));
            }
            if (this.runtimeConnection != null) {
                if (messageAccess == null) {
                    messageAccess = new MessageAccessDB(this.configConnection, this.runtimeConnection);
                }
                messageAccess.setMessageState(as2Info.getMessageId(), AS2Message.STATE_STOPPED);
            }
            throw new Exception(as2Info.getMessageId() + ": HTTP " + returnCode);
        }
        if (this.configConnection != null) {
            //inc the sent data size, this is for new connections (as2 messages, async mdn)
            AS2Server.incRawSentData(size);
            if (message.getAS2Info().isMDN()) {
                AS2MDNInfo mdnInfo = (AS2MDNInfo) message.getAS2Info();
                //ASYNC MDN sent: insert an entry into the statistic table
                QuotaAccessDB.incReceivedMessages(this.configConnection, this.runtimeConnection, mdnInfo.getSenderId(),
                        mdnInfo.getReceiverId(), mdnInfo.getState(), mdnInfo.getRelatedMessageId());
            }
        }
        if (this.configConnection != null) {
            MessageStoreHandler messageStoreHandler = new MessageStoreHandler(this.configConnection, this.runtimeConnection);
            messageStoreHandler.storeSentMessage(message, sender, receiver, this.requestHeader);
        }
        //inform the server of the result if a sync MDN has been requested
        if (!message.isMDN()) {
            AS2MessageInfo messageInfo = (AS2MessageInfo) message.getAS2Info();
            if (messageInfo.requestsSyncMDN()) {
                //perform a check if the answer really contains a MDN or is just an empty HTTP 200 with some header data
                //this check looks for the existance of some key header values
                boolean as2FromExists = false;
                boolean as2ToExists = false;
                for (int i = 0; i < this.getResponseHeader().length; i++) {
                    String key = this.getResponseHeader()[i].getName();
                    if (key.toLowerCase().equals("as2-to")) {
                        as2ToExists = true;
                    } else if (key.toLowerCase().equals("as2-from")) {
                        as2FromExists = true;
                    }
                }
                if (!as2ToExists) {
                    throw new Exception(this.rb.getResourceString("answer.no.sync.mdn",
                            new Object[]{as2Info.getMessageId(), "as2-to"}));
                }
                //send the data to the as2 server. It does not care if the MDN has been sync or async anymore
                GenericClient client = new GenericClient();
                CommandObjectIncomingMessage commandObject = new CommandObjectIncomingMessage();
                //create temporary file to store the data
                File tempFile = AS2Tools.createTempFile("SYNCMDN_received", ".bin");
                FileOutputStream outStream = new FileOutputStream(tempFile);
                ByteArrayInputStream memIn = new ByteArrayInputStream(this.responseData);
                this.copyStreams(memIn, outStream);
                memIn.close();
                outStream.flush();
                outStream.close();
                commandObject.setMessageDataFilename(tempFile.getAbsolutePath());
                for (int i = 0; i < this.getResponseHeader().length; i++) {
                    String key = this.getResponseHeader()[i].getName();
                    String value = this.getResponseHeader()[i].getValue();
                    commandObject.addHeader(key.toLowerCase(), value);
                    if (key.toLowerCase().equals("content-type")) {
                        commandObject.setContentType(value);
                    }
                }
                //compatibility issue: some AS2 systems do not send a as2-from in the sync case, even if
                //this if _NOT_ RFC conform
                //see RFC 4130, section 6.2: The AS2-To and AS2-From header fields MUST be
                //present in all AS2 messages and AS2 MDNs whether asynchronous or synchronous in nature,
                //except for asynchronous MDNs, which are sent using SMTP.
                if (!as2FromExists) {
                    commandObject.addHeader("as2-from", AS2Message.escapeFromToHeader(receiver.getAS2Identification()));
                }
                ErrorObject errorObject = client.send(commandObject);
                if (errorObject.getErrors() > 0) {
                    messageAccess.setMessageState(as2Info.getMessageId(), AS2Message.STATE_STOPPED);
                }
                tempFile.delete();
            }
        }
        return (this.requestHeader);
    }

    /**Sets necessary HTTP authentication for this partner, depending on if it is an asny MDN that will be sent or an AS2 message.
     *If the partner is not configured to use HTTP authentication in any kind nothing will happen in here
     */
    private void setHTTPAuthentication(DefaultHttpClient client, Partner receiver, boolean isMDN) {
        HTTPAuthentication authentication = null;
        if (isMDN) {
            authentication = receiver.getAuthenticationAsyncMDN();
        } else {
            authentication = receiver.getAuthentication();
        }
        if (authentication.isEnabled()) {
            Credentials userPassCredentials = new UsernamePasswordCredentials(authentication.getUser(), authentication.getPassword());
            client.getCredentialsProvider().setCredentials(AuthScope.ANY, userPassCredentials);
            BasicHttpContext localcontext = new BasicHttpContext();
            // Generate BASIC scheme object and stick it to the local
            // execution context
            BasicScheme basicAuth = new BasicScheme();
            localcontext.setAttribute("preemptive-auth", basicAuth);
            // Add as the first request interceptor
            client.addRequestInterceptor(new PreemptiveAuth(), 0);
        }
    }

    /**Builds a proxy object from the actual preferences, returns null if no proxy is requested*/
    public ProxyObject createProxyObjectFromPreferences() {
        if (!this.preferences.getBoolean(PreferencesAS2.PROXY_USE)) {
            //return empty proxy object, is not used
            return (null);
        }
        ProxyObject proxy = new ProxyObject();
        proxy.setHost(this.preferences.get(PreferencesAS2.PROXY_HOST));
        proxy.setPort(this.preferences.getInt(PreferencesAS2.PROXY_PORT));
        if (this.preferences.getBoolean(PreferencesAS2.AUTH_PROXY_USE)) {
            proxy.setUser(this.preferences.get(PreferencesAS2.AUTH_PROXY_USER));
            proxy.setPassword(this.preferences.get(PreferencesAS2.AUTH_PROXY_PASS).toCharArray());
        }
        return (proxy);
    }

    /**Sets the proxy authentification for the client*/
    private void setProxyToConnection(DefaultHttpClient client, AS2Message message, ProxyObject proxy) {
        //is a proxy requested?
        if (proxy.getHost() == null) {
            return;
        }
        HttpHost proxyHost = new HttpHost(proxy.getHost(), proxy.getPort(), "http");
        client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxyHost);
        if (proxy.getUser() != null) {
            CredentialsProvider credsProvider = new BasicCredentialsProvider();
            credsProvider.setCredentials(
                    new AuthScope(proxy.getHost(), proxy.getPort()),
                    new UsernamePasswordCredentials(proxy.getUser(), String.valueOf(proxy.getPassword())));
            client.setCredentialsProvider(credsProvider);
        }
        if (this.logger != null) {
            this.logger.log(Level.INFO,
                    this.rb.getResourceString("using.proxy",
                    new Object[]{
                        message.getAS2Info().getMessageId(),
                        proxy.getHost(), String.valueOf(proxy.getPort()),}), message.getAS2Info());
        }
    }

    /**Uploads the data, returns the HTTP result code*/
    public int performUpload(HttpConnectionParameter connectionParameter, AS2Message message, Partner sender, Partner receiver) {
        return (this.performUpload(connectionParameter, message, sender, receiver, null));
    }

    /**Uploads the data, returns the HTTP result code*/
    public int performUpload(HttpConnectionParameter connectionParameter, AS2Message message, Partner sender, Partner receiver, URL receiptURL) {
        String ediintFeatures = "multiple-attachments, CEM";
        //set the http connection/routing/protocol parameter
        HttpParams httpParams = new BasicHttpParams();
        if (connectionParameter.getConnectionTimeoutMillis() != -1) {
            HttpConnectionParams.setConnectionTimeout(httpParams, connectionParameter.getConnectionTimeoutMillis());
        }
        if (connectionParameter.getSoTimeoutMillis() != -1) {
            HttpConnectionParams.setSoTimeout(httpParams, connectionParameter.getSoTimeoutMillis());
        }
        HttpConnectionParams.setStaleCheckingEnabled(httpParams, connectionParameter.isStaleConnectionCheck());
        if (connectionParameter.getHttpProtocolVersion() == null) {
            //default settings: HTTP 1.1
            HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
        } else if (connectionParameter.getHttpProtocolVersion().equals(HttpConnectionParameter.HTTP_1_0)) {
            HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_0);
        } else if (connectionParameter.getHttpProtocolVersion().equals(HttpConnectionParameter.HTTP_1_1)) {
            HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
        }
        HttpProtocolParams.setUseExpectContinue(httpParams, connectionParameter.isUseExpectContinue());
        HttpProtocolParams.setUserAgent(httpParams, connectionParameter.getUserAgent());
        if (connectionParameter.getLocalAddress() != null) {
            ConnRouteParams.setLocalAddress(httpParams, connectionParameter.getLocalAddress());
        }
        int status = -1;
        HttpPost filePost = null;
        DefaultHttpClient httpClient = null;
        try {
            ClientConnectionManager clientConnectionManager = this.createClientConnectionManager(httpParams);
            httpClient = new DefaultHttpClient(clientConnectionManager, httpParams);
            //some ssl implementations have problems with a session/connection reuse
            httpClient.setReuseStrategy(new NoConnectionReuseStrategy());
            //disable SSL hostname verification. Do not confuse this with SSL trust verification!
            SSLSocketFactory sslFactory = (SSLSocketFactory) httpClient.getConnectionManager().getSchemeRegistry().get("https").getSocketFactory();
            sslFactory.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            //determine the receipt URL if it is not set
            if (receiptURL == null) {
                //async MDN requested?
                if (message.isMDN()) {
                    if (this.runtimeConnection == null) {
                        throw new IllegalArgumentException("MessageHTTPUploader.performUpload(): A MDN receipt URL is not set, unable to determine where to send the MDN");
                    }
                    MessageAccessDB messageAccess = new MessageAccessDB(this.configConnection, this.runtimeConnection);
                    AS2MessageInfo relatedMessageInfo = messageAccess.getLastMessageEntry(((AS2MDNInfo) message.getAS2Info()).getRelatedMessageId());
                    receiptURL = new URL(relatedMessageInfo.getAsyncMDNURL());
                } else {
                    receiptURL = new URL(receiver.getURL());
                }
            }
            filePost = new HttpPost(receiptURL.toExternalForm());
            filePost.addHeader("as2-version", "1.2");
            filePost.addHeader("ediint-features", ediintFeatures);
            filePost.addHeader("mime-version", "1.0");
            filePost.addHeader("recipient-address", receiptURL.toExternalForm());
            filePost.addHeader("message-id", "<" + message.getAS2Info().getMessageId() + ">");
            filePost.addHeader("as2-from", AS2Message.escapeFromToHeader(sender.getAS2Identification()));
            filePost.addHeader("as2-to", AS2Message.escapeFromToHeader(receiver.getAS2Identification()));
            String originalFilename = null;
            if (message.getPayloads() != null && message.getPayloads().size() > 0) {
                originalFilename = message.getPayloads().get(0).getOriginalFilename();
            }
            if (originalFilename != null) {
                String subject = this.replace(message.getAS2Info().getSubject(),
                        "${filename}", originalFilename);
                filePost.addHeader("subject", subject);
                //update the message infos subject with the actual content
                if (!message.isMDN()) {
                    ((AS2MessageInfo) message.getAS2Info()).setSubject(subject);
                    //refresh this in the database if it is requested
                    if (this.runtimeConnection != null) {
                        MessageAccessDB access = new MessageAccessDB(this.configConnection, this.runtimeConnection);
                        access.updateSubject((AS2MessageInfo) message.getAS2Info());
                    }
                }
            } else {
                filePost.addHeader("subject", message.getAS2Info().getSubject());
            }
            filePost.addHeader("from", sender.getEmail());
            filePost.addHeader("connection", "close, TE");
            //the data header must be always in english locale else there would be special
            //french characters (e.g. 13 déc. 2011 16:28:56 CET) which is not allowed after 
            //RFC 4130           
            DateFormat format = new SimpleDateFormat("EE, dd MMM yyyy HH:mm:ss zz", Locale.US);
            filePost.addHeader("date", format.format(new Date()));
            String contentType = null;
            if (message.getAS2Info().getEncryptionType() != AS2Message.ENCRYPTION_NONE) {
                contentType = "application/pkcs7-mime; smime-type=enveloped-data; name=smime.p7m";
            } else {
                contentType = message.getContentType();
            }
            filePost.addHeader("content-type", contentType);
            //MDN header, this is always the way for async MDNs
            if (message.isMDN()) {
                if (this.logger != null) {
                    this.logger.log(Level.INFO,
                            this.rb.getResourceString("sending.mdn.async",
                            new Object[]{
                                message.getAS2Info().getMessageId(),
                                receiptURL
                            }), message.getAS2Info());
                }
                filePost.addHeader("server", message.getAS2Info().getUserAgent());
            } else {
                AS2MessageInfo messageInfo = (AS2MessageInfo) message.getAS2Info();
                //outbound AS2/CEM message
                if (messageInfo.requestsSyncMDN()) {
                    if (this.logger != null) {
                        if (messageInfo.getMessageType() == AS2Message.MESSAGETYPE_CEM) {
                            this.logger.log(Level.INFO,
                                    this.rb.getResourceString("sending.cem.sync",
                                    new Object[]{
                                        messageInfo.getMessageId(),
                                        receiver.getURL()
                                    }), messageInfo);
                        } else if (messageInfo.getMessageType() == AS2Message.MESSAGETYPE_AS2) {
                            this.logger.log(Level.INFO,
                                    this.rb.getResourceString("sending.msg.sync",
                                    new Object[]{
                                        messageInfo.getMessageId(),
                                        receiver.getURL()
                                    }), messageInfo);
                        }
                    }
                } else {
                    //Message with ASYNC MDN request
                    if (this.logger != null) {
                        if (messageInfo.getMessageType() == AS2Message.MESSAGETYPE_CEM) {
                            this.logger.log(Level.INFO,
                                    this.rb.getResourceString("sending.cem.async",
                                    new Object[]{
                                        messageInfo.getMessageId(),
                                        receiver.getURL(),
                                        sender.getMdnURL()
                                    }), messageInfo);
                        } else if (messageInfo.getMessageType() == AS2Message.MESSAGETYPE_AS2) {
                            this.logger.log(Level.INFO,
                                    this.rb.getResourceString("sending.msg.async",
                                    new Object[]{
                                        messageInfo.getMessageId(),
                                        receiver.getURL(),
                                        sender.getMdnURL()
                                    }), messageInfo);
                        }
                    }
                    //The following header indicates that this requests an asnc MDN.
                    //When the header "receipt-delivery-option" is present,
                    //the header "disposition-notification-to" serves as a request
                    //for an asynchronous MDN.
                    //The header "receipt-delivery-option" must always be accompanied by
                    //the header "disposition-notification-to".
                    //When the header "receipt-delivery-option" is not present and the header
                    //"disposition-notification-to" is present, the header "disposition-notification-to"
                    //serves as a request for a synchronous MDN.
                    filePost.addHeader("receipt-delivery-option", sender.getMdnURL());
                }
                filePost.addHeader("disposition-notification-to", sender.getMdnURL());
                //request a signed MDN if this is set up in the partner configuration
                if (receiver.isSignedMDN()) {
                    filePost.addHeader("disposition-notification-options",
                            messageInfo.getDispositionNotificationOptions().getHeaderValue());
                }
                if (messageInfo.getSignType() != AS2Message.SIGNATURE_NONE) {
                    filePost.addHeader("content-disposition", "attachment; filename=\"smime.p7m\"");
                } else if (messageInfo.getSignType() == AS2Message.SIGNATURE_NONE && message.getAS2Info().getSignType() == AS2Message.ENCRYPTION_NONE) {
                    filePost.addHeader("content-disposition", "attachment; filename=\"" + message.getPayload(0).getOriginalFilename() + "\"");
                }
            }
            int port = receiptURL.getPort();
            if (port == -1) {
                port = receiptURL.getDefaultPort();
            }
            filePost.addHeader("host", receiptURL.getHost() + ":" + port);
            InputStream rawDataInputStream = message.getRawDataInputStream();
            InputStreamEntity postEntity = new InputStreamEntity(rawDataInputStream, message.getRawDataSize());
            postEntity.setContentType(contentType);
            filePost.setEntity(postEntity);
            if (connectionParameter.getProxy() != null) {
                this.setProxyToConnection(httpClient, message, connectionParameter.getProxy());
            }
            this.setHTTPAuthentication(httpClient, receiver, message.getAS2Info().isMDN());
            this.updateUploadHttpHeader(filePost, receiver);
            HttpHost targetHost = new HttpHost(receiptURL.getHost(), receiptURL.getPort(), receiptURL.getProtocol());
            BasicHttpContext localcontext = new BasicHttpContext();
            // Generate BASIC scheme object and stick it to the local
            // execution context. Without this a HTTP authentication will not be sent
            BasicScheme basicAuth = new BasicScheme();
            localcontext.setAttribute("preemptive-auth", basicAuth);
            HttpResponse httpResponse = httpClient.execute(targetHost, filePost, localcontext);
            rawDataInputStream.close();
            this.responseData = this.readEntityData(httpResponse);
            if (httpResponse != null) {
                this.responseStatusLine = httpResponse.getStatusLine();
                status = this.responseStatusLine.getStatusCode();
                this.responseHeader = httpResponse.getAllHeaders();
            }
            for (Header singleHeader : filePost.getAllHeaders()) {
                if (singleHeader.getValue() != null) {
                    this.requestHeader.setProperty(singleHeader.getName(), singleHeader.getValue());
                }
            }
            //accept all 2xx answers
            //SC_ACCEPTED Status code (202) indicating that a request was accepted for processing, but was not completed.
            //SC_CREATED  Status code (201) indicating the request succeeded and created a new resource on the server.
            //SC_NO_CONTENT Status code (204) indicating that the request succeeded but that there was no new information to return.
            //SC_NON_AUTHORITATIVE_INFORMATION Status code (203) indicating that the meta information presented by the client did not originate from the server.
            //SC_OK Status code (200) indicating the request succeeded normally.
            //SC_RESET_CONTENT Status code (205) indicating that the agent SHOULD reset the document view which caused the request to be sent.
            //SC_PARTIAL_CONTENT Status code (206) indicating that the server has fulfilled the partial GET request for the resource.
            if (status != HttpServletResponse.SC_OK && status != HttpServletResponse.SC_ACCEPTED && status != HttpServletResponse.SC_CREATED && status != HttpServletResponse.SC_NO_CONTENT && status != HttpServletResponse.SC_NON_AUTHORITATIVE_INFORMATION && status != HttpServletResponse.SC_RESET_CONTENT && status != HttpServletResponse.SC_PARTIAL_CONTENT) {
                if (this.logger != null) {
                    this.logger.severe(
                            this.rb.getResourceString("error.httpupload",
                            new Object[]{message.getAS2Info().getMessageId(),
                                URLDecoder.decode(this.responseStatusLine == null ? "" : this.responseStatusLine.getReasonPhrase(), "UTF-8")
                            }));
                }
            }
        } catch (Exception ex) {
            if (this.logger != null) {
                StringBuilder errorMessage = new StringBuilder(message.getAS2Info().getMessageId());
                errorMessage.append(": MessageHTTPUploader.performUpload: [");
                errorMessage.append(ex.getClass().getSimpleName());
                errorMessage.append("]");
                if (ex.getMessage() != null) {
                    errorMessage.append(": ").append(ex.getMessage());
                }
                this.logger.log(Level.SEVERE, errorMessage.toString(), message.getAS2Info());
            }
        } finally {
            if (httpClient != null && httpClient.getConnectionManager() != null) {
                //shutdown the HTTPClient to release the resources
                httpClient.getConnectionManager().shutdown();
            }
        }
        return (status);
    }

    private ClientConnectionManager createClientConnectionManager(HttpParams httpParams) throws Exception {
        //register protocols
        SchemeRegistry registry = new SchemeRegistry();
        Scheme http = new Scheme("http", PlainSocketFactory.getSocketFactory(), 80);
        registry.register(http);
        registry.register(this.createHTTPSScheme());
        ClientConnectionManager manager = new ThreadSafeClientConnManager(httpParams, registry);
        return (manager);
    }

    private Scheme createHTTPSScheme() throws Exception {
        //cert store not set so far: take the preferences data
        if (this.certStore == null) {
            this.certStore = new KeystoreStorageImplFile(
                    this.preferences.get(PreferencesAS2.KEYSTORE_HTTPS_SEND),
                    this.preferences.get(PreferencesAS2.KEYSTORE_HTTPS_SEND_PASS).toCharArray(),
                    BCCryptoHelper.KEYSTORE_JKS);
            this.trustStore = new KeystoreStorageImplFile(
                    this.preferences.get(PreferencesAS2.KEYSTORE_HTTPS_SEND),
                    this.preferences.get(PreferencesAS2.KEYSTORE_HTTPS_SEND_PASS).toCharArray(),
                    BCCryptoHelper.KEYSTORE_JKS);
        }
        SSLSocketFactory socketFactory = new SSLSocketFactory(this.certStore.getKeystore(),
                new String(this.certStore.getKeystorePass()),
                this.trustStore.getKeystore());
        socketFactory.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        return (new Scheme("https", socketFactory, 443));
    }

    /**Updates the passed post HTTP headers with the headers defined for the sender*/
    private void updateUploadHttpHeader(HttpPost post, Partner receiver) {
        List<String> usedHeaderKeys = new ArrayList<String>();
        for (Header singleHeader : post.getAllHeaders()) {
            PartnerHttpHeader headerReplacement = receiver.getHttpHeader(singleHeader.getName());
            if (headerReplacement != null) {
                //a value to replace is set
                if (headerReplacement.getValue() != null && headerReplacement.getValue().length() > 0) {
                    post.setHeader(singleHeader.getName(), headerReplacement.getValue());
                } else {
                    //no value to replace is set: delete the header
                    post.removeHeader(singleHeader);
                }
                usedHeaderKeys.add(singleHeader.getName());
            }
        }
        //add additional user defined headers
        List<PartnerHttpHeader> additionalHeaders = receiver.getAllNonListedHttpHeader(usedHeaderKeys);
        for (PartnerHttpHeader additionalHeader : additionalHeaders) {
            //add the header if a value is set
            if (additionalHeader.getValue() != null && additionalHeader.getValue().length() > 0) {
                post.setHeader(additionalHeader.getKey(), additionalHeader.getValue());
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

    /**Returns the version of this class*/
    public static String getVersion() {
        String revision = "$Revision: 1.1 $";
        return (revision.substring(revision.indexOf(":") + 1,
                revision.lastIndexOf("$")).trim());
    }

    /**Copies all data from one stream to another*/
    private void copyStreams(InputStream in, OutputStream out)
            throws IOException {
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

    /**Returns the response data as byte array*/
    public byte[] getResponseData() {
        return (this.responseData);
    }

    /**Reads the data of a HTTP response entity*/
    public byte[] readEntityData(HttpResponse httpResponse) throws Exception {
        if (httpResponse == null) {
            return (null);
        }
        if (httpResponse.getEntity() == null) {
            return (null);
        }
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        httpResponse.getEntity().writeTo(outStream);
        outStream.flush();
        outStream.close();
        return (outStream.toByteArray());
    }

    /**Returns the array of response headers after the upload process has been performed
     * @return the responseHeader
     */
    public Header[] getResponseHeader() {
        if (this.responseHeader == null) {
            return (new Header[0]);
        }
        return (this.responseHeader);
    }

    static class PreemptiveAuth implements HttpRequestInterceptor {

        @Override
        public void process(
                final HttpRequest request,
                final HttpContext context) throws HttpException, IOException {

            AuthState authState = (AuthState) context.getAttribute(ClientContext.TARGET_AUTH_STATE);

            // If no auth scheme avaialble yet, try to initialize it preemptively
            if (authState.getAuthScheme() == null) {
                AuthScheme authScheme = (AuthScheme) context.getAttribute(
                        "preemptive-auth");
                CredentialsProvider credsProvider = (CredentialsProvider) context.getAttribute(
                        ClientContext.CREDS_PROVIDER);
                HttpHost targetHost = (HttpHost) context.getAttribute(
                        ExecutionContext.HTTP_TARGET_HOST);
                if (authScheme != null) {
                    Credentials creds = credsProvider.getCredentials(
                            new AuthScope(
                            targetHost.getHostName(),
                            targetHost.getPort()));
                    if (creds == null) {
                        throw new HttpException("No credentials for preemptive authentication");
                    }
                    authState.setAuthScheme(authScheme);
                    authState.setCredentials(creds);
                }
            }

        }
    }
}
