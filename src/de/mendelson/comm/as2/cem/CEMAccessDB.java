//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/cem/CEMAccessDB.java,v 1.1 2012/04/18 14:10:17 heller Exp $
package de.mendelson.comm.as2.cem;

import de.mendelson.comm.as2.cem.messages.EDIINTCertificateExchangeRequest;
import de.mendelson.comm.as2.cem.messages.EDIINTCertificateExchangeResponse;
import de.mendelson.comm.as2.cem.messages.TrustRequest;
import de.mendelson.comm.as2.cem.messages.TrustResponse;
import de.mendelson.comm.as2.message.AS2MessageInfo;
import de.mendelson.comm.as2.notification.Notification;
import de.mendelson.comm.as2.partner.Partner;
import de.mendelson.comm.as2.server.AS2Server;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Access the certificate lists in the database
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class CEMAccessDB {

    /**Logger to log inforamtion to*/
    private Logger logger = Logger.getLogger(AS2Server.SERVER_LOGGER_NAME);
    /**Connection to the database*/
    private Connection runtimeConnection;
    private Connection configConnection;

    /** Creates new message I/O log and connects to localhost
     *@param host host to connect to
     */
    public CEMAccessDB(Connection configConnection, Connection runtimeConnection) {
        this.configConnection = configConnection;
        this.runtimeConnection = runtimeConnection;
    }

    /**For debug purpose*/
    public static String convertCategory(int category) {
        if (category == CEMEntry.CATEGORY_CRYPT) {
            return ("encryption");
        } else if (category == CEMEntry.CATEGORY_SSL) {
            return ("SSL");
        } else if (category == CEMEntry.CATEGORY_SIGN) {
            return ("signature");
        } else {
            return ("unknown");
        }
    }

    /**Returns if a request with the passed request id exists in the system*/
    public boolean requestExists(String requestId) {
        PreparedStatement statement = null;
        ResultSet result = null;
        try {
            //get SSL and sign certificates
            String query = "SELECT COUNT(1) AS counter FROM cem WHERE requestid=?";
            statement = this.runtimeConnection.prepareStatement(query);
            statement.setEscapeProcessing(true);
            statement.setString(1, requestId);
            result = statement.executeQuery();
            if (result.next()) {
                return (result.getInt("counter") > 0);
            }
        } catch (Exception e) {
            this.logger.severe("CEMAccessDB.requestExists: " + e.getMessage());
            Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
        } finally {
            if (result != null) {
                try {
                    result.close();
                } catch (Exception e) {
                    this.logger.severe("CEMAccessDB.requestExists: " + e.getMessage());
                    Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (Exception e) {
                    this.logger.severe("CEMAccessDB.requestExists: " + e.getMessage());
                    Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
                }
            }
        }
        return (false);
    }

    /**Marks a request as processed, it will no longer be processed by the CertificateCEMController*/
    public void markAsProcessed(String requestId, int category) {
        PreparedStatement statement = null;
        try {
            //get SSL and sign certificates
            String query = "UPDATE cem SET processed=?,processdate=? WHERE requestid=? AND category=?";
            statement = this.runtimeConnection.prepareStatement(query);
            statement.setEscapeProcessing(true);
            statement.setInt(1, 1);
            statement.setLong(2, System.currentTimeMillis());
            statement.setString(3, requestId);
            statement.setInt(4, category);
            statement.executeUpdate();
        } catch (Exception e) {
            this.logger.severe("CEMAccessDB.markAsProcessed: " + e.getMessage());
            Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (Exception e) {
                    this.logger.severe("CEMAccessDB.markAsProcessed: " + e.getMessage());
                    Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
                }
            }
        }
    }

    /**Returns all available CEM entries that have the state pending
     */
    public List<CEMEntry> getCEMEntriesPending() {
        List<CEMEntry> entryList = new ArrayList<CEMEntry>();
        PreparedStatement statement = null;
        ResultSet result = null;
        try {
            //get SSL and sign certificates
            String query = "SELECT * FROM cem WHERE cemstate=? ORDER BY id";
            statement = this.runtimeConnection.prepareStatement(query);
            statement.setEscapeProcessing(true);
            statement.setInt(1, CEMEntry.STATUS_PENDING_INT);
            result = statement.executeQuery();
            while (result.next()) {
                CEMEntry cemEntry = new CEMEntry();
                cemEntry.setCategory(result.getInt("category"));
                cemEntry.setInitiatorAS2Id(result.getString("initiatoras2id"));
                cemEntry.setReceiverAS2Id(result.getString("receiveras2id"));
                long respondByDateValue = result.getLong("respondbydate");
                if (!result.wasNull()) {
                    cemEntry.setRespondByDate(respondByDateValue);
                }
                cemEntry.setSerialId(result.getString("serialid"));
                cemEntry.setRequestId(result.getString("requestid"));
                cemEntry.setCemState(result.getInt("cemstate"));
                cemEntry.setIssuername(result.getString("issuername"));
                cemEntry.setProcessed(result.getInt("processed") != 0);
                cemEntry.setRequestMessageid(result.getString("requestmessageid"));
                cemEntry.setResponseMessageid(result.getString("responsemessageid"));
                cemEntry.setRequestMessageOriginated(result.getLong("requestmessageoriginated"));
                cemEntry.setResponseMessageOriginated(result.getLong("responsemessageoriginated"));
                cemEntry.setProcessDate(result.getLong("processdate"));
                Object reasonForRejectionObj = result.getObject("reasonforrejection");
                if (!result.wasNull() && reasonForRejectionObj instanceof String) {
                    cemEntry.setReasonForRejection((String)reasonForRejectionObj);
                }
                entryList.add(cemEntry);
            }
        } catch (Exception e) {
            this.logger.severe("CEMAccessDB.getCEMEntriesPending: " + e.getMessage());
            Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
        } finally {
            if (result != null) {
                try {
                    result.close();
                } catch (Exception e) {
                    this.logger.severe("CEMAccessDB.getCEMEntriesPending: " + e.getMessage());
                    Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (Exception e) {
                    this.logger.severe("CEMAccessDB.getCEMEntriesPending: " + e.getMessage());
                    Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
                }
            }
            return (entryList);
        }
    }

    /**Returns all available CEM entries
     */
    public List<CEMEntry> getCEMEntries() {
        List<CEMEntry> entryList = new ArrayList<CEMEntry>();
        PreparedStatement statement = null;
        ResultSet result = null;
        try {
            //get SSL and sign certificates
            String query = "SELECT * FROM cem ORDER BY id";
            statement = this.runtimeConnection.prepareStatement(query);
            statement.setEscapeProcessing(true);
            result = statement.executeQuery();
            while (result.next()) {
                CEMEntry cemEntry = new CEMEntry();
                cemEntry.setCategory(result.getInt("category"));
                cemEntry.setInitiatorAS2Id(result.getString("initiatoras2id"));
                cemEntry.setReceiverAS2Id(result.getString("receiveras2id"));
                long respondByDateValue = result.getLong("respondbydate");
                if (!result.wasNull()) {
                    cemEntry.setRespondByDate(respondByDateValue);
                }
                cemEntry.setSerialId(result.getString("serialid"));
                cemEntry.setRequestId(result.getString("requestid"));
                cemEntry.setCemState(result.getInt("cemstate"));
                cemEntry.setIssuername(result.getString("issuername"));
                cemEntry.setProcessed(result.getInt("processed") != 0);
                cemEntry.setRequestMessageid(result.getString("requestmessageid"));
                cemEntry.setResponseMessageid(result.getString("responsemessageid"));
                cemEntry.setRequestMessageOriginated(result.getLong("requestmessageoriginated"));
                cemEntry.setResponseMessageOriginated(result.getLong("responsemessageoriginated"));
                cemEntry.setProcessDate(result.getLong("processdate"));
                Object reasonForRejectionObj = result.getObject("reasonforrejection");
                if (!result.wasNull() && reasonForRejectionObj instanceof String) {
                    cemEntry.setReasonForRejection((String)reasonForRejectionObj);
                }
                entryList.add(cemEntry);
            }
        } catch (Exception e) {
            this.logger.severe("CEMAccessDB.getCEMEntries: " + e.getMessage());
            Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
        } finally {
            if (result != null) {
                try {
                    result.close();
                } catch (Exception e) {
                    this.logger.severe("CEMAccessDB.getCEMEntries: " + e.getMessage());
                    Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (Exception e) {
                    this.logger.severe("CEMAccessDB.getCEMEntries: " + e.getMessage());
                    Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
                }
            }
            return (entryList);
        }
    }

    /**If a CEM is a SSL or signature request it contains a respondByDate entry. This is the date where this
     * entry should be set as sssl or signature cert
     * @return
     */
    public List<CEMEntry> getCertificatesToChange() {
        List<CEMEntry> responseList = new ArrayList<CEMEntry>();
        PreparedStatement statement = null;
        ResultSet resultSign = null;
        ResultSet resultCrypt = null;
        try {
            //get SSL and sign certificates where the date is set
            String query = "SELECT * FROM cem WHERE (category=? OR category=?) AND (cemstate=? OR cemstate=?) AND respondbydate IS NOT NULL AND respondbydate < ? AND processed=?";
            statement = this.runtimeConnection.prepareStatement(query);
            statement.setEscapeProcessing(true);
            statement.setInt(1, CEMEntry.CATEGORY_SIGN);
            statement.setInt(2, CEMEntry.CATEGORY_SSL);
            statement.setInt(3, CEMEntry.STATUS_ACCEPTED_INT);
            statement.setInt(4, CEMEntry.STATUS_PENDING_INT);
            statement.setLong(5, System.currentTimeMillis());
            statement.setInt(6, 0);
            resultSign = statement.executeQuery();
            while (resultSign.next()) {
                CEMEntry cemEntry = new CEMEntry();
                cemEntry.setCategory(resultSign.getInt("category"));
                cemEntry.setInitiatorAS2Id(resultSign.getString("initiatoras2id"));
                cemEntry.setReceiverAS2Id(resultSign.getString("receiveras2id"));
                long respondByDateValue = resultSign.getLong("respondbydate");
                if (!resultSign.wasNull()) {
                    cemEntry.setRespondByDate(respondByDateValue);
                }
                cemEntry.setSerialId(resultSign.getString("serialid"));
                cemEntry.setRequestId(resultSign.getString("requestid"));
                cemEntry.setIssuername(resultSign.getString("issuername"));
                responseList.add(cemEntry);
            }
            //get crypt certificates and sign/ssl certificates where respondbydate is not set and the partner has answered
            query = "SELECT * FROM cem WHERE (category=? OR ((category=? OR category=?) AND respondbydate IS NULL)) AND cemstate=? AND processed=?";
            statement = this.runtimeConnection.prepareStatement(query);
            statement.setEscapeProcessing(true);
            statement.setInt(1, CEMEntry.CATEGORY_CRYPT);
            statement.setInt(2, CEMEntry.CATEGORY_SIGN);
            statement.setInt(3, CEMEntry.CATEGORY_SSL);
            statement.setInt(4, CEMEntry.STATUS_ACCEPTED_INT);
            statement.setInt(5, 0);
            resultCrypt = statement.executeQuery();
            while (resultCrypt.next()) {
                CEMEntry cemEntry = new CEMEntry();
                cemEntry.setCategory(resultCrypt.getInt("category"));
                cemEntry.setInitiatorAS2Id(resultCrypt.getString("initiatoras2id"));
                cemEntry.setReceiverAS2Id(resultCrypt.getString("receiveras2id"));
                long respondByDateValue = resultCrypt.getLong("respondbydate");
                if (!resultCrypt.wasNull()) {
                    cemEntry.setRespondByDate(respondByDateValue);
                }
                cemEntry.setSerialId(resultCrypt.getString("serialid"));
                cemEntry.setRequestId(resultCrypt.getString("requestid"));
                cemEntry.setIssuername(resultCrypt.getString("issuername"));
                responseList.add(cemEntry);
            }
        } catch (Exception e) {
            this.logger.severe("CEMAccessDB.getCertificatesToChange: " + e.getMessage());
            Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
        } finally {
            if (resultSign != null) {
                try {
                    resultSign.close();
                } catch (Exception e) {
                    this.logger.severe("CEMAccessDB.getCertificatesToChange: " + e.getMessage());
                    Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
                }
            }
            if (resultCrypt != null) {
                try {
                    resultCrypt.close();
                } catch (Exception e) {
                    this.logger.severe("CEMAccessDB.getCertificatesToChange: " + e.getMessage());
                    Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (Exception e) {
                    this.logger.severe("CEMAccessDB.getCertificatesToChange: " + e.getMessage());
                    Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
                }
            }
            return (responseList);
        }
    }

    /**Inserts a new CEM response into the database*/
    public void insertResponse(AS2MessageInfo info, Partner initiator, Partner receiver, EDIINTCertificateExchangeResponse response) {
        List<TrustResponse> responseList = response.getTrustResponseList();
        for (TrustResponse trustResponse : responseList) {
            this.insertResponse(info, initiator, receiver, response, trustResponse);
        }
    }

    /**Inserts a new CEM response into the database*/
    public void insertResponse(AS2MessageInfo info, Partner initiator, Partner receiver, EDIINTCertificateExchangeResponse response,
            TrustResponse trustResponse) {
        PreparedStatement statement = null;
        try {
            String query = "UPDATE cem SET cemstate=?, responsemessageid=?,responsemessageoriginated=?,reasonforrejection=? WHERE "
                    + "requestid=? AND initiatoras2id=? AND receiveras2id=? AND serialid=?";
            statement = this.runtimeConnection.prepareStatement(query);
            statement.setEscapeProcessing(true);
            statement.setInt(1, trustResponse.getState());
            statement.setString(2, info.getMessageId());
            statement.setLong(3, response.getTradingPartnerInfo().getMessageOriginated().getTime());
            if (trustResponse.getReasonForRejection() == null) {
                statement.setNull(4, Types.JAVA_OBJECT);
            } else {
                statement.setObject(4, trustResponse.getReasonForRejection());
            }
            statement.setString(5, response.getRequestId());
            statement.setString(6, initiator.getAS2Identification());
            statement.setString(7, receiver.getAS2Identification());
            statement.setString(8, trustResponse.getCertificateReference().getSerialNumber());
            statement.execute();
        } catch (Exception e) {
            this.logger.severe("CEMAccessDB.insertResponse: " + e.getMessage());
            Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (Exception e) {
                    this.logger.severe("CEMAccessDB.insertResponse: " + e.getMessage());
                    Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
                }
            }
        }
    }

    /**Inserts a new request into the cem database*/
    public void insertRequest(AS2MessageInfo info, Partner initiator, Partner receiver, EDIINTCertificateExchangeRequest request) {
        List<TrustRequest> trustRequestList = request.getTrustRequestList();
        for (TrustRequest trustRequest : trustRequestList) {
            if (trustRequest.isCertUsageEncryption()) {
                this.insertTrustRequest(info, initiator, receiver, request, trustRequest, CEMEntry.CATEGORY_CRYPT);
            }
            if (trustRequest.isCertUsageSSL()) {
                this.insertTrustRequest(info, initiator, receiver, request, trustRequest, CEMEntry.CATEGORY_SSL);
            }
            if (trustRequest.isCertUsageSignature()) {
                this.insertTrustRequest(info, initiator, receiver, request, trustRequest, CEMEntry.CATEGORY_SIGN);
            }
        }
    }

    /**Inserts a new request into the cem database*/
    private void insertTrustRequest(AS2MessageInfo info, Partner initiator, Partner receiver, EDIINTCertificateExchangeRequest request,
            TrustRequest trustRequest, int category) {
        //cancel old entries with the same parameter
        this.setAllPendingRequestsToState(initiator.getAS2Identification(), receiver.getAS2Identification(), category, CEMEntry.STATUS_CANCELED_INT);
        PreparedStatement statement = null;
        try {
            String query = "INSERT INTO cem(initiatoras2id,receiveras2id,requestid,requestmessageid,respondbydate,requestmessageoriginated,category,cemstate,serialid,issuername)"
                    + "VALUES(?,?,?,?,?,?,?,?,?,?)";
            statement = this.runtimeConnection.prepareStatement(query);
            statement.setEscapeProcessing(true);
            statement.setString(1, initiator.getAS2Identification());
            statement.setString(2, receiver.getAS2Identification());
            statement.setString(3, request.getRequestId());
            statement.setString(4, info.getMessageId());
            if (trustRequest.getRespondByDate() == null) {
                statement.setNull(5, Types.BIGINT);
            } else {
                statement.setLong(5, trustRequest.getRespondByDate().getTime());
            }
            statement.setLong(6, request.getTradingPartnerInfo().getMessageOriginated().getTime());
            statement.setInt(7, category);
            //enter all requests as pending first. They are accepted by the response or the respondbydate
            statement.setInt(8, CEMEntry.STATUS_PENDING_INT);
            statement.setString(9, trustRequest.getEndEntity().getSerialNumber());
            statement.setString(10, trustRequest.getEndEntity().getIssuerName());
            statement.execute();
        } catch (Exception e) {
            this.logger.severe("CEMAccessDB.insertTrustRequest: " + e.getMessage());
            Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (Exception e) {
                    this.logger.severe("CEMAccessDB.insertTrustRequest: " + e.getMessage());
                    Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
                }
            }
        }
    }

    /**Updates a cem entry to a new state if its pending*/
    public void setAllPendingRequestsToState(String initiatorAS2Id, String receiverAS2Id, int category, int newState) {
        this.setPendingRequestsToState(initiatorAS2Id, receiverAS2Id, category, null, newState);
    }

    /**A new request for a chance came in. Update existing ones for this relationship and category to canceled if they exist*/
    public void setPendingRequestsToState(String initiatorAS2Id, String receiverAS2Id, int category, String requestId, int newState) {
        PreparedStatement statement = null;
        try {
            String query = "UPDATE cem SET cemstate=? WHERE initiatoras2id=? AND receiveras2id=? AND category=? AND (cemstate=? OR cemstate=?) AND processed=?";
            if (requestId != null) {
                query += " AND requestId=?";
            }
            statement = this.runtimeConnection.prepareStatement(query);
            statement.setEscapeProcessing(true);
            statement.setInt(1, newState);
            statement.setString(2, initiatorAS2Id);
            statement.setString(3, receiverAS2Id);
            statement.setInt(4, category);
            statement.setInt(5, CEMEntry.STATUS_PENDING_INT);
            statement.setInt(6, CEMEntry.STATUS_ACCEPTED_INT);
            statement.setInt(7, 0);
            if (requestId != null) {
                statement.setString(8, requestId);
            }
            statement.execute();
        } catch (Exception e) {
            this.logger.severe(e.getMessage());
            Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (Exception e) {
                    this.logger.severe(e.getMessage());
                    Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
                }
            }
        }
    }

    /**Remove an entry from the cem table*/
    public void removeEntry(String initiatorAS2Id, String receiverAS2Id, int category, String requestId) {
        PreparedStatement statement = null;
        try {
            String query = "DELETE FROM cem WHERE initiatoras2id=? AND receiveras2id=? AND category=? AND requestId=?";
            statement = this.runtimeConnection.prepareStatement(query);
            statement.setEscapeProcessing(true);
            statement.setString(1, initiatorAS2Id);
            statement.setString(2, receiverAS2Id);
            statement.setInt(3, category);
            statement.setString(4, requestId);
            statement.execute();
        } catch (Exception e) {
            this.logger.severe("CEMAccessDB.removeEntry: " + e.getMessage());
            Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (Exception e) {
                    this.logger.severe("CEMAccessDB.removeEntry: " + e.getMessage());
                    Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
                }
            }
        }
    }
}
