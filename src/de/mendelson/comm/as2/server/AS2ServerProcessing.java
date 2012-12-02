//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/server/AS2ServerProcessing.java,v 1.1 2012/04/18 14:10:39 heller Exp $
package de.mendelson.comm.as2.server;

import de.mendelson.comm.as2.client.manualsend.ManualSendRequest;
import de.mendelson.comm.as2.client.manualsend.ManualSendResponse;
import de.mendelson.comm.as2.clientserver.message.DeleteMessageRequest;
import de.mendelson.util.security.cert.CertificateManager;
import de.mendelson.comm.as2.clientserver.message.PartnerConfigurationChanged;
import de.mendelson.comm.as2.clientserver.message.PerformNotificationTestRequest;
import de.mendelson.comm.as2.clientserver.message.RefreshClientMessageOverviewList;
import de.mendelson.comm.as2.importexport.ConfigurationExport;
import de.mendelson.comm.as2.importexport.ConfigurationExportRequest;
import de.mendelson.comm.as2.importexport.ConfigurationExportResponse;
import de.mendelson.comm.as2.importexport.ConfigurationImport;
import de.mendelson.comm.as2.importexport.ConfigurationImportRequest;
import de.mendelson.comm.as2.importexport.ConfigurationImportResponse;
import de.mendelson.comm.as2.message.AS2Message;
import de.mendelson.comm.as2.message.AS2MessageInfo;
import de.mendelson.comm.as2.message.AS2Payload;
import de.mendelson.comm.as2.notification.Notification;
import de.mendelson.comm.as2.preferences.PreferencesAS2;
import de.mendelson.util.security.cert.clientserver.RefreshKeystoreCertificates;
import de.mendelson.comm.as2.send.DirPollManager;
import de.mendelson.comm.as2.sendorder.SendOrderSender;
import de.mendelson.comm.as2.statistic.StatisticExport;
import de.mendelson.comm.as2.statistic.StatisticExportRequest;
import de.mendelson.comm.as2.statistic.StatisticExportResponse;
import de.mendelson.comm.as2.timing.MessageDeleteController;
import de.mendelson.util.AS2Tools;
import de.mendelson.util.MecResourceBundle;
import de.mendelson.util.clientserver.ClientServer;
import de.mendelson.util.clientserver.ClientServerProcessing;
import de.mendelson.util.clientserver.clients.datatransfer.DownloadRequestFile;
import de.mendelson.util.clientserver.clients.datatransfer.DownloadRequestFileLimited;
import de.mendelson.util.clientserver.clients.datatransfer.DownloadResponseFile;
import de.mendelson.util.clientserver.clients.datatransfer.DownloadResponseFileLimited;
import de.mendelson.util.clientserver.clients.datatransfer.UploadRequestChunk;
import de.mendelson.util.clientserver.clients.datatransfer.UploadRequestFile;
import de.mendelson.util.clientserver.clients.datatransfer.UploadResponseChunk;
import de.mendelson.util.clientserver.clients.datatransfer.UploadResponseFile;
import de.mendelson.util.clientserver.clients.fileoperation.FileDeleteRequest;
import de.mendelson.util.clientserver.clients.fileoperation.FileDeleteResponse;
import de.mendelson.util.clientserver.clients.fileoperation.FileRenameRequest;
import de.mendelson.util.clientserver.clients.fileoperation.FileRenameResponse;
import de.mendelson.util.clientserver.clients.filesystemview.FileSystemViewProcessorServer;
import de.mendelson.util.clientserver.clients.filesystemview.FileSystemViewRequest;
import de.mendelson.util.clientserver.clients.preferences.PreferencesRequest;
import de.mendelson.util.clientserver.clients.preferences.PreferencesResponse;
import de.mendelson.util.clientserver.messages.ClientServerMessage;
import de.mendelson.util.clientserver.messages.ClientServerResponse;
import de.mendelson.util.security.cert.clientserver.UploadRequestKeystore;
import de.mendelson.util.security.cert.clientserver.UploadResponseKeystore;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.mina.core.session.IoSession;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * User defined processing to extend the client-server framework
 * @author  S.Heller
 * @version $Revision: 1.1 $
 * @since build 68
 */
public class AS2ServerProcessing implements ClientServerProcessing {

    private DirPollManager pollManager;
    private CertificateManager certificateManager;
    private Connection configConnection;
    private Connection runtimeConnection;
    private Logger logger = Logger.getLogger(AS2Server.SERVER_LOGGER_NAME);
    /**ResourceBundle to localize messages of the server*/
    private MecResourceBundle rb = null;
    private ClientServer clientserver;
    private final Map<String, String> uploadMap = Collections.synchronizedMap(new HashMap<String, String>());
    private int uploadCounter = 0;
    private FileSystemViewProcessorServer filesystemview;

    public AS2ServerProcessing(ClientServer clientserver, DirPollManager pollManager, CertificateManager certificateManager,
            Connection configConnection, Connection runtimeConnection) {
        //Load default resourcebundle
        try {
            this.rb = (MecResourceBundle) ResourceBundle.getBundle(
                    ResourceBundleAS2ServerProcessing.class.getName());
        } //load up  resourcebundle
        catch (MissingResourceException e) {
            throw new RuntimeException("Oops..resource bundle " + e.getClassName() + " not found.");
        }
        this.filesystemview = new FileSystemViewProcessorServer(this.logger);
        this.clientserver = clientserver;
        this.configConnection = configConnection;
        this.runtimeConnection = runtimeConnection;
        this.pollManager = pollManager;
        this.certificateManager = certificateManager;
    }

    private synchronized String incUploadRequest() {
        this.uploadCounter++;
        return (String.valueOf(this.uploadCounter));
    }

    @Override
    public boolean process(IoSession session, ClientServerMessage message) {
        try {
            if (message instanceof PartnerConfigurationChanged) {
                this.pollManager.partnerConfigurationChanged();
                return (true);
            } else if (message instanceof RefreshKeystoreCertificates) {
                this.certificateManager.rereadKeystoreCertificatesLogged();
                return (true);
            } else if (message instanceof PreferencesRequest) {
                this.processPreferencesRequest(session, (PreferencesRequest) message);
                return (true);
            } else if (message instanceof DeleteMessageRequest) {
                this.processDeleteMessageRequest(session, (DeleteMessageRequest) message);
                return (true);
            } else if (message instanceof ManualSendRequest) {
                this.processManualSendRequest(session, (ManualSendRequest) message);
                return (true);
            } else if (message instanceof UploadRequestKeystore) {
                this.processUploadRequestKeystore(session, (UploadRequestKeystore) message);
                return (true);
            } else if (message instanceof FileRenameRequest) {
                this.processFileRenameRequest(session, (FileRenameRequest) message);
                return (true);
            } else if (message instanceof FileDeleteRequest) {
                this.processFileDeleteRequest(session, (FileDeleteRequest) message);
                return (true);
            } else if (message instanceof ConfigurationExportRequest) {
                this.processConfigurationExportRequest(session, (ConfigurationExportRequest) message);
                return (true);
            } else if (message instanceof ConfigurationImportRequest) {
                this.processConfigurationImportRequest(session, (ConfigurationImportRequest) message);
                return (true);
            } else if (message instanceof StatisticExportRequest) {
                this.processStatisticExportRequest(session, (StatisticExportRequest) message);
                return (true);
            } else if (message instanceof DownloadRequestFile) {
                this.processDownloadRequestFile(session, (DownloadRequestFile) message);
                return (true);
            } else if (message instanceof UploadRequestChunk) {
                this.processUploadRequestChunk(session, (UploadRequestChunk) message);
                return (true);
            } else if (message instanceof UploadRequestFile) {
                this.processUploadRequestFile(session, (UploadRequestFile) message);
                return (true);
            } else if (message instanceof PerformNotificationTestRequest) {
                this.performNotificationTest(session, (PerformNotificationTestRequest) message);
                return (true);
            } else if (message instanceof FileSystemViewRequest) {
                session.write(this.filesystemview.performRequest((FileSystemViewRequest) message));
                return (true);
            }
        } catch (Throwable e) {
            e.printStackTrace();
            this.logger.warning(this.rb.getResourceString("unable.to.process", message.toString()));
        }
        return (false);
    }

    private void performNotificationTest(IoSession session, PerformNotificationTestRequest message) throws Exception {
        ClientServerResponse response = new ClientServerResponse(message);
        try {
            Notification notification = new Notification(message.getNotificationData(), this.configConnection, this.runtimeConnection);
            notification.sendTest();
        } catch (Exception e) {
            response.setException(e);
        }
        session.write(response);
    }

    /**Appends a chunk to a formerly sent data. If this is the first chunk an entry is created in the upload map of this class*/
    private void processUploadRequestChunk(IoSession session, UploadRequestChunk request) {
        UploadResponseChunk response = new UploadResponseChunk(request);
        OutputStream outStream = null;
        InputStream inStream = null;
        try {
            if (request.getTargetHash() == null) {
                File tempFile = AS2Tools.createTempFile("upload_as2", ".bin");
                String newHash = this.incUploadRequest();
                synchronized (this.uploadMap) {
                    this.uploadMap.put(newHash, tempFile.getAbsolutePath());
                }
                request.setTargetHash(newHash);
            }
            response.setTargetHash(request.getTargetHash());
            File tempFile = null;
            synchronized (this.uploadMap) {
                tempFile = new File(this.uploadMap.get(request.getTargetHash()));
            }
            outStream = new FileOutputStream(tempFile, true);
            inStream = request.getDataStream();
            this.copyStreams(inStream, outStream);
        } catch (IOException e) {
            response.setException(e);
        } finally {
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (Exception e) {
                    //nop
                }
            }
            if (outStream != null) {
                try {
                    outStream.flush();
                    outStream.close();
                } catch (Exception e) {
                    //nop
                }
            }
        }
        session.write(response);
    }

    private void processStatisticExportRequest(IoSession session, StatisticExportRequest request) {
        StatisticExportResponse response = new StatisticExportResponse(request);
        StatisticExport exporter = new StatisticExport(this.configConnection, this.runtimeConnection);
        ByteArrayOutputStream outStream = null;
        try {
            outStream = new ByteArrayOutputStream();
            exporter.export(outStream,
                    request.getStartDate(),
                    request.getEndDate(),
                    request.getTimestep(), request.getLocalStation(),
                    request.getPartner());
            outStream.flush();
            response.setData(outStream.toByteArray());
        } catch (Throwable e) {
            response.setException(e);
        } finally {
            if (outStream != null) {
                try {
                    outStream.close();
                } catch (Exception e) {
                    //nop
                }
            }
        }
        //sync respond to the request
        session.write(response);
    }

    private void processDeleteMessageRequest(IoSession session, DeleteMessageRequest request) {
        MessageDeleteController controller = new MessageDeleteController(null,
                this.configConnection, this.runtimeConnection);
        List<AS2MessageInfo> deleteList = request.getDeleteList();
        RefreshClientMessageOverviewList refreshRequest = new RefreshClientMessageOverviewList();
        refreshRequest.setOperation(RefreshClientMessageOverviewList.OPERATION_DELETE_UPDATE);
        for (int i = 0; i < deleteList.size(); i++) {
            controller.deleteMessageFromLog(deleteList.get(i));
            if (i % 10 == 0) {
                this.clientserver.broadcastToClients(refreshRequest);
            }
        }
        this.clientserver.broadcastToClients(refreshRequest);
    }

    private void processConfigurationImportRequest(IoSession session, ConfigurationImportRequest request) {
        ConfigurationImportResponse response = new ConfigurationImportResponse(request);
        InputStream inStream = null;
        try {
            String uploadHash = request.getUploadHash();
            File uploadFile = null;
            synchronized (this.uploadMap) {
                uploadFile = new File(this.uploadMap.get(uploadHash));
                this.uploadMap.remove(uploadHash);
            }
            inStream = new FileInputStream(uploadFile);
            ByteArrayOutputStream memOut = new ByteArrayOutputStream();
            this.copyStreams(inStream, memOut);
            byte[] importData = memOut.toByteArray();
            memOut.close();
            ConfigurationImport importer = new ConfigurationImport(this.configConnection, this.runtimeConnection);
            inStream = new ByteArrayInputStream(importData);
            importer.importData(inStream,
                    request.getPartnerListToImport(),
                    request.getImportNotification(),
                    request.getImportServerSettings());
        } catch (Exception e) {
            response.setException(e);
            e.printStackTrace();
        } finally {
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (Exception e) {
                    //nop
                }
            }
        }
        session.write(response);
    }

    private void processConfigurationExportRequest(IoSession session, ConfigurationExportRequest request) {
        ConfigurationExportResponse response = new ConfigurationExportResponse(request);
        try {
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            ConfigurationExport export = new ConfigurationExport(this.configConnection, this.runtimeConnection);
            export.export(outStream);
            outStream.flush();
            response.setData(outStream.toByteArray());
        } catch (Exception e) {
            response.setException(e);
        }
        session.write(response);
    }

    private void processUploadRequestFile(IoSession session, UploadRequestFile request) {
        UploadResponseFile response = new UploadResponseFile(request);
        try {
            String uploadHash = request.getUploadHash();
            File tempFile = new File(this.uploadMap.get(uploadHash));
            File targetFile = new File(request.getTargetFilename());
            FileUtils.deleteQuietly(targetFile);
            FileUtils.moveFile(tempFile, targetFile);
            synchronized (this.uploadMap) {
                this.uploadMap.remove(uploadHash);
            }
        } catch (IOException e) {
            response.setException(e);
        }
        session.write(response);
    }

    private void processUploadRequestKeystore(IoSession session, UploadRequestKeystore request) {
        UploadResponseKeystore response = new UploadResponseKeystore(request);
        try {
            String uploadHash = request.getUploadHash();
            File tempFile = null;
            synchronized (this.uploadMap) {
                tempFile = new File(this.uploadMap.get(uploadHash));
            }
            File targetFile = new File(request.getTargetFilename());
            FileUtils.deleteQuietly(targetFile);
            FileUtils.moveFile(tempFile, targetFile);
            synchronized (this.uploadMap) {
                this.uploadMap.remove(uploadHash);
            }
        } catch (IOException e) {
            response.setException(e);
        }
        session.write(response);
        try {
            this.certificateManager.rereadKeystoreCertificates();
        } catch (Exception e) {
            //nop
        }
    }

    /**A client performed a file rename request
     *
     */
    private void processFileRenameRequest(IoSession session, FileRenameRequest request) {
        FileRenameResponse response = new FileRenameResponse(request);
        boolean success = new File(request.getOldName()).renameTo(new File(request.getNewName()));
        response.setSuccess(success);
        session.write(response);
    }

    /**A client performed a file delete request
     *
     */
    private void processFileDeleteRequest(IoSession session, FileDeleteRequest request) {
        FileDeleteResponse response = new FileDeleteResponse(request);
        boolean success = false;
        File fileToDelete = new File(request.getFilename());
        try {
            if (fileToDelete.isDirectory()) {
                FileUtils.deleteDirectory(fileToDelete);
            } else {
                FileUtils.forceDelete(fileToDelete);
            }
            success = true;
        } catch (Exception e) {
        }
        response.setSuccess(success);
        session.write(response);
    }

    /**A client performed a manual send request
     * 
     * @param session
     * @param request
     */
    private void processManualSendRequest(IoSession session, ManualSendRequest request) {
        ManualSendResponse response = new ManualSendResponse(request);
        SendOrderSender orderSender = new SendOrderSender(this.configConnection, this.runtimeConnection);
        InputStream inStream = null;
        try {
            AS2Payload payload = new AS2Payload();
            String uploadHash = request.getUploadHash();
            File uploadedFile = null;
            synchronized( this.uploadMap ){
                uploadedFile = new File(this.uploadMap.get(uploadHash));
            }
            inStream = new FileInputStream(uploadedFile);
            ByteArrayOutputStream memStream = new ByteArrayOutputStream();
            this.copyStreams(inStream, memStream);
            memStream.close();
            payload.setData(memStream.toByteArray());
            payload.setOriginalFilename(request.getFilename().replace(' ', '_'));
            AS2Message message = orderSender.send(this.certificateManager, request.getSender(),
                    request.getReceiver(), payload);
            if (message == null) {
                throw new Exception(this.rb.getResourceString("send.failed"));
            } else {
                this.clientserver.broadcastToClients(new RefreshClientMessageOverviewList());
            }
        } catch (Exception e) {
            response.setException(e);
        } finally {
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (Exception e) {
                    //nop
                }
            }
        }
        session.write(response);
    }

    /**A client performed a preferences request
     * 
     * @param session
     * @param request
     */
    private void processPreferencesRequest(IoSession session, PreferencesRequest request) {
        PreferencesAS2 preferences = new PreferencesAS2();
        if (request.getType() == PreferencesRequest.TYPE_GET) {
            PreferencesResponse response = new PreferencesResponse(request);
            response.setValue(preferences.get(request.getKey()));
            session.write(response);
        } else if (request.getType() == PreferencesRequest.TYPE_GET_DEFAULT) {
            PreferencesResponse response = new PreferencesResponse(request);
            response.setValue(preferences.getDefaultValue(request.getKey()));
            session.write(response);
        } else if (request.getType() == PreferencesRequest.TYPE_SET) {
            preferences.put(request.getKey(), request.getValue());
        }
    }

    /**A client performed a download request
     *
     * @param session
     * @param request
     */
    private void processDownloadRequestFile(IoSession session, DownloadRequestFile request) {
        DownloadResponseFile response = null;
        if (request instanceof DownloadRequestFileLimited) {
            DownloadRequestFileLimited requestLimited = (DownloadRequestFileLimited) request;
            response = new DownloadResponseFileLimited(requestLimited);
            InputStream inStream = null;
            try {
                if (request.getFilename() == null) {
                    throw new FileNotFoundException();
                }
                File downloadFile = new File(requestLimited.getFilename());
                response.setFullFilename(downloadFile.getAbsolutePath());
                response.setReadOnly(!downloadFile.canWrite());
                response.setSize(downloadFile.length());
                if (downloadFile.length() < requestLimited.getMaxSize()) {
                    inStream = new FileInputStream(request.getFilename());
                    response.setData(inStream);
                    ((DownloadResponseFileLimited) response).setSizeExceeded(false);
                } else {
                    ((DownloadResponseFileLimited) response).setSizeExceeded(true);
                }
            } catch (Exception e) {
                response.setException(e);
            } finally {
                if (inStream != null) {
                    try {
                        inStream.close();
                    } catch (Exception e) {
                        //nop
                    }
                }
            }
        } else {
            response = new DownloadResponseFile(request);
            InputStream inStream = null;
            try {
                if (request.getFilename() == null) {
                    throw new FileNotFoundException();
                }
                File downloadFile = new File(request.getFilename());
                response.setFullFilename(downloadFile.getAbsolutePath());
                response.setReadOnly(!downloadFile.canWrite());
                response.setSize(downloadFile.length());
                inStream = new FileInputStream(downloadFile);
                response.setData(inStream);
            } catch (IOException e) {
                response.setException(e);
            } finally {
                if (inStream != null) {
                    try {
                        inStream.close();
                    } catch (Exception e) {
                        //nop
                    }
                }
            }
        }
        session.write(response);
    }

    /**Copies all data from one stream to another*/
    private void copyStreams(InputStream in, OutputStream out) throws IOException {
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
