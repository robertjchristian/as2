//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/partner/PartnerAccessDB.java,v 1.1 2012/04/18 14:10:32 heller Exp $
package de.mendelson.comm.as2.partner;

import de.mendelson.comm.as2.cert.CertificateAccessDB;
import de.mendelson.comm.as2.notification.Notification;
import de.mendelson.comm.as2.server.AS2Server;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software. Other product
 * and brand names are trademarks of their respective owners.
 */
/**
 * Implementation of a server log for the rosettanet server database
 *
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class PartnerAccessDB {

    /**
     * Logger to log inforamtion to
     */
    private Logger logger = Logger.getLogger(AS2Server.SERVER_LOGGER_NAME);
    /**
     * Connection to the database
     */
    private Connection configConnection;
    private Connection runtimeConnection;
    /**
     * Access the certificates
     */
    private CertificateAccessDB certificateAccess;

    /**
     * Creates new message I/O log and connects to localhost
     *
     * @param host host to connect to
     */
    public PartnerAccessDB(Connection configConnection, Connection runtimeConnection) {
        this.configConnection = configConnection;
        this.certificateAccess = new CertificateAccessDB(configConnection, runtimeConnection);
    }

    /**
     * Requires a query to select partners from the DB
     */
    private Partner[] getPartnerByQuery(String query) {
        List<Partner> partnerList = new ArrayList<Partner>();
        Statement statement = null;
        ResultSet result = null;
        try {
            statement = this.configConnection.createStatement();
            statement.setEscapeProcessing(true);
            result = statement.executeQuery(query);
            while (result.next()) {
                Partner partner = new Partner();
                partner.setAS2Identification(result.getString("as2ident"));
                partner.setName(result.getString("name"));
                partner.setDBId(result.getInt("id"));
                partner.setLocalStation(result.getInt("islocal") == 1);
                partner.setSignType(result.getInt("sign"));
                partner.setEncryptionType(result.getInt("encrypt"));
                partner.setEmail(result.getString("email"));
                partner.setURL(result.getString("url"));
                partner.setMdnURL(result.getString("mdnurl"));
                partner.setSubject(result.getString("subject"));
                partner.setContentType(result.getString("contenttype"));
                partner.setSyncMDN(result.getInt("syncmdn") == 1);
                partner.setPollIgnoreListString(result.getString("pollignorelist"));
                partner.setPollInterval(result.getInt("pollinterval"));
                partner.setCompressionType(result.getInt("compression"));
                partner.setSignedMDN(result.getInt("signedmdn") == 1);
                partner.setUseCommandOnReceipt(result.getInt("usecommandonreceipt") == 1);
                partner.setCommandOnReceipt(result.getString("commandonreceipt"));
                partner.setKeepOriginalFilenameOnReceipt(result.getInt("keeporiginalfilenameonreceipt") == 1);
                HTTPAuthentication authentication = partner.getAuthentication();
                authentication.setUser(result.getString("httpauthuser"));
                authentication.setPassword(result.getString("httpauthpass"));
                authentication.setEnabled(result.getInt("usehttpauth") == 1);
                HTTPAuthentication asyncAuthentication = partner.getAuthenticationAsyncMDN();
                asyncAuthentication.setUser(result.getString("httpauthuserasnymdn"));
                asyncAuthentication.setPassword(result.getString("httpauthpassasnymdn"));
                asyncAuthentication.setEnabled(result.getInt("usehttpauthasyncmdn") == 1);
                Object commentObj = result.getObject("partnercomment");
                if (!result.wasNull()) {
                    if (commentObj instanceof String) {
                        partner.setComment((String) commentObj);
                    } else if (commentObj instanceof byte[]) {
                        //compatibility issue - db update
                        partner.setComment(new String((byte[]) commentObj));
                    }
                } else {
                    partner.setComment(null);
                }
                partner.setNotifyReceive(result.getInt("notifyreceive"));
                partner.setNotifySend(result.getInt("notifysend"));
                partner.setNotifySendReceive(result.getInt("notifysendreceive"));
                partner.setNotifyReceiveEnabled(result.getInt("notifyreceiveenabled") == 1);
                partner.setNotifySendEnabled(result.getInt("notifysendenabled") == 1);
                partner.setNotifySendReceiveEnabled(result.getInt("notifysendreceiveenabled") == 1);
                partner.setUseCommandOnSendError(result.getInt("usecommandonsenderror") == 1);
                partner.setCommandOnSendError(result.getString("commandonsenderror"));
                partner.setUseCommandOnSendSuccess(result.getInt("usecommandonsendsuccess") == 1);
                partner.setCommandOnSendSuccess(result.getString("commandonsendsuccess"));
                partner.setContentTransferEncoding(result.getInt("contenttransferencoding"));
                partner.setHttpProtocolVersion(result.getString("httpversion"));
                partner.setMaxPollFiles(result.getInt("maxpollfiles"));
                //ensure to have a valid partner DB id before loading the releated data
                this.certificateAccess.loadPartnerCertificateInformation(partner);
                this.loadHttpHeader(partner);
                partnerList.add(partner);
            }
            Partner[] partnerArray = new Partner[partnerList.size()];
            partnerList.toArray(partnerArray);
            return (partnerArray);
        } catch (Exception e) {
            e.printStackTrace();
            this.logger.severe("PartnerAccessDB.getPartnerByQuery: " + e.getMessage());
            Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
            return (null);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (Exception e) {
                    //nop
                }
            }
            if (result != null) {
                try {
                    result.close();
                } catch (Exception e) {
                    //nop
                }
            }
        }
    }

    /**
     * Returns all partner stored in the DB, even the local station
     */
    public Partner[] getPartner() {
        return (this.getPartnerByQuery("SELECT * FROM partner"));
    }

    /**
     * Returns all local stations stored in the DB
     */
    public Partner[] getLocalStations() {
        return (this.getPartnerByQuery("SELECT * FROM partner WHERE islocal=1"));
    }

    /**
     * Returns all partner stored in the DB, even the local station
     */
    public Partner[] getNonLocalStations() {
        return (this.getPartnerByQuery("SELECT * FROM partner WHERE islocal<>1"));
    }

    /**
     * receives a partner configuration and updates the database with them
     */
    public void updatePartner(Partner[] newPartner) {
        //first delete all partners that are in the DB but not in the new list
        Partner[] existingPartner = this.getPartner();
        List<Partner> newPartnerList = new ArrayList(Arrays.asList(newPartner));
        for (int i = 0; i < existingPartner.length; i++) {
            if (!newPartnerList.contains(existingPartner[i])) {
                this.deletePartner(existingPartner[i]);
            }
        }
        //insert all NEW partners and update the existing
        for (int i = 0; i < newPartner.length; i++) {
            if (newPartner[i].getDBId() < 0) {
                this.insertPartner(newPartner[i]);
            } else {
                this.updatePartner(newPartner[i]);
            }
        }
    }

    /**
     * Updates a single partner in the db
     */
    /**
     * Inserts a new partner into the database
     */
    public void updatePartner(Partner partner) {
        PreparedStatement statement = null;
        try {
            statement = this.configConnection.prepareStatement(
                    "UPDATE partner SET as2ident=?,name=?,islocal=?,sign=?,encrypt=?,email=?,url=?,mdnurl=?,subject=?,contenttype=?,syncmdn=?,pollignorelist=?,pollinterval=?,compression=?,signedmdn=?,commandonreceipt=?,usecommandonreceipt=?,usehttpauth=?,httpauthuser=?,httpauthpass=?,usehttpauthasyncmdn=?,httpauthuserasnymdn=?,httpauthpassasnymdn=?,keeporiginalfilenameonreceipt=?,partnercomment=?,notifysend=?,notifyreceive=?,notifysendreceive=?,notifysendenabled=?,notifyreceiveenabled=?,notifysendreceiveenabled=?,commandonsenderror=?,usecommandonsenderror=?,commandonsendsuccess=?,usecommandonsendsuccess=?,contenttransferencoding=?,httpversion=?,maxpollfiles=? WHERE id=?");
            statement.setEscapeProcessing(true);
            statement.setString(1, partner.getAS2Identification());
            statement.setString(2, partner.getName());
            statement.setInt(3, partner.isLocalStation() ? 1 : 0);
            statement.setInt(4, partner.getSignType());
            statement.setInt(5, partner.getEncryptionType());
            statement.setString(6, partner.getEmail());
            statement.setString(7, partner.getURL());
            statement.setString(8, partner.getMdnURL());
            statement.setString(9, partner.getSubject());
            statement.setString(10, partner.getContentType());
            statement.setInt(11, partner.isSyncMDN() ? 1 : 0);
            statement.setString(12, partner.getPollIgnoreListAsString());
            statement.setInt(13, partner.getPollInterval());
            statement.setInt(14, partner.getCompressionType());
            statement.setInt(15, partner.isSignedMDN() ? 1 : 0);
            statement.setString(16, partner.getCommandOnReceipt());
            statement.setInt(17, partner.useCommandOnReceipt() ? 1 : 0);
            statement.setInt(18, partner.getAuthentication().isEnabled() ? 1 : 0);
            statement.setString(19, partner.getAuthentication().getUser());
            statement.setString(20, partner.getAuthentication().getPassword());
            statement.setInt(21, partner.getAuthenticationAsyncMDN().isEnabled() ? 1 : 0);
            statement.setString(22, partner.getAuthenticationAsyncMDN().getUser());
            statement.setString(23, partner.getAuthenticationAsyncMDN().getPassword());
            statement.setInt(24, partner.getKeepOriginalFilenameOnReceipt() ? 1 : 0);
            if (partner.getComment() == null) {
                statement.setNull(25, Types.JAVA_OBJECT);
            } else {
                statement.setObject(25, partner.getComment());
            }
            statement.setInt(26, partner.getNotifySend());
            statement.setInt(27, partner.getNotifyReceive());
            statement.setInt(28, partner.getNotifySendReceive());
            statement.setInt(29, partner.isNotifySendEnabled() ? 1 : 0);
            statement.setInt(30, partner.isNotifyReceiveEnabled() ? 1 : 0);
            statement.setInt(31, partner.isNotifySendReceiveEnabled() ? 1 : 0);
            statement.setString(32, partner.getCommandOnSendError());
            statement.setInt(33, partner.useCommandOnSendError() ? 1 : 0);
            statement.setString(34, partner.getCommandOnSendSuccess());
            statement.setInt(35, partner.useCommandOnSendSuccess() ? 1 : 0);
            statement.setInt(36, partner.getContentTransferEncoding());
            statement.setString(37, partner.getHttpProtocolVersion());
            statement.setInt(38, partner.getMaxPollFiles());
            //where statement
            statement.setInt(39, partner.getDBId());
            statement.execute();
        } catch (SQLException e) {
            this.logger.severe("updatePartner: " + e.getMessage());
            Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (Exception e) {
                    //nop
                }
            }
        }
        this.storeHttpHeader(partner);
        this.certificateAccess.storePartnerCertificateInformationList(partner);
    }

    /**
     * Deletes a single partner from the database
     */
    public void deletePartner(Partner partner) {
        this.deleteHttpHeader(partner);
        this.certificateAccess.deletePartnerCertificateInformationList(partner);
        PartnerSystemAccessDB partnerSystemAccess = new PartnerSystemAccessDB(this.configConnection, this.runtimeConnection);
        partnerSystemAccess.deletePartnerSystem(partner);
        PreparedStatement statement = null;
        try {
            statement = this.configConnection.prepareStatement("DELETE FROM partner WHERE id=?");
            statement.setEscapeProcessing(true);
            statement.setInt(1, partner.getDBId());
            statement.execute();
        } catch (SQLException e) {
            this.logger.severe("PartnerAccessDB.deletePartner: " + e.getMessage());
            Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (Exception e) {
                    this.logger.severe("PartnerAccessDB.deletePartner: " + e.getMessage());
                    Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
                }
            }
        }
    }

    /**
     * Inserts a new partner into the database
     */
    public void insertPartner(Partner partner) {
        PreparedStatement statement = null;
        try {
            statement = this.configConnection.prepareStatement(
                    "INSERT INTO partner(as2ident,name,islocal,sign,encrypt,email,url,mdnurl,subject,contenttype,syncmdn,pollignorelist,pollinterval,compression,signedmdn,commandonreceipt,usecommandonreceipt,usehttpauth,httpauthuser,httpauthpass,usehttpauthasyncmdn,httpauthuserasnymdn,httpauthpassasnymdn,keeporiginalfilenameonreceipt,partnercomment,notifysend,notifyreceive,notifysendreceive,notifysendenabled,notifyreceiveenabled,notifysendreceiveenabled,commandonsenderror,usecommandonsenderror,commandonsendsuccess,usecommandonsendsuccess,contenttransferencoding,httpversion,maxpollfiles)VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
            statement.setEscapeProcessing(true);
            statement.setString(1, partner.getAS2Identification());
            statement.setString(2, partner.getName());
            statement.setInt(3, partner.isLocalStation() ? 1 : 0);
            statement.setInt(4, partner.getSignType());
            statement.setInt(5, partner.getEncryptionType());
            statement.setString(6, partner.getEmail());
            statement.setString(7, partner.getURL());
            statement.setString(8, partner.getMdnURL());
            statement.setString(9, partner.getSubject());
            statement.setString(10, partner.getContentType());
            statement.setInt(11, partner.isSyncMDN() ? 1 : 0);
            statement.setString(12, partner.getPollIgnoreListAsString());
            statement.setInt(13, partner.getPollInterval());
            statement.setInt(14, partner.getCompressionType());
            statement.setInt(15, partner.isSignedMDN() ? 1 : 0);
            statement.setString(16, partner.getCommandOnReceipt());
            statement.setInt(17, partner.useCommandOnReceipt() ? 1 : 0);
            statement.setInt(18, partner.getAuthentication().isEnabled() ? 1 : 0);
            statement.setString(19, partner.getAuthentication().getUser());
            statement.setString(20, partner.getAuthentication().getPassword());
            statement.setInt(21, partner.getAuthenticationAsyncMDN().isEnabled() ? 1 : 0);
            statement.setString(22, partner.getAuthenticationAsyncMDN().getUser());
            statement.setString(23, partner.getAuthenticationAsyncMDN().getPassword());
            statement.setInt(24, partner.getKeepOriginalFilenameOnReceipt() ? 1 : 0);
            if (partner.getComment() == null) {
                statement.setNull(25, Types.JAVA_OBJECT);
            } else {
                statement.setObject(25, partner.getComment());
            }
            statement.setInt(26, partner.getNotifySend());
            statement.setInt(27, partner.getNotifyReceive());
            statement.setInt(28, partner.getNotifySendReceive());
            statement.setInt(29, partner.isNotifySendEnabled() ? 1 : 0);
            statement.setInt(30, partner.isNotifyReceiveEnabled() ? 1 : 0);
            statement.setInt(31, partner.isNotifySendReceiveEnabled() ? 1 : 0);
            statement.setString(32, partner.getCommandOnSendError());
            statement.setInt(33, partner.useCommandOnSendError() ? 1 : 0);
            statement.setString(34, partner.getCommandOnSendSuccess());
            statement.setInt(35, partner.useCommandOnSendSuccess() ? 1 : 0);
            statement.setInt(36, partner.getContentTransferEncoding());
            statement.setString(37, partner.getHttpProtocolVersion());
            statement.setInt(38, partner.getMaxPollFiles());
            statement.execute();
        } catch (SQLException e) {
            this.logger.severe("PartnerAccessDB.insertPartner: " + e.getMessage());
            Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (Exception e) {
                    this.logger.severe("PartnerAccessDB.insertPartner: " + e.getMessage());
                    Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
                }
            }
        }
        //get the assigned db id
        Partner storedPartner = this.getPartner(partner.getAS2Identification());
        if (storedPartner != null) {
            partner.setDBId(storedPartner.getDBId());
            this.storeHttpHeader(partner);
            this.certificateAccess.storePartnerCertificateInformationList(partner);
        }
    }

    /**
     * Loads a specified partner from the DB
     *
     * @return null if the partner does not exist
     */
    public Partner getPartner(String as2ident) {
        String query = "SELECT * FROM partner WHERE as2ident='" + as2ident + "'";
        Partner[] partner = this.getPartnerByQuery(query);
        if (partner == null || partner.length == 0) {
            return (null);
        }
        return (partner[0]);
    }

    /**
     * Loads a specified partner from the DB
     *
     * @return null if the partner does not exist
     */
    public Partner getPartner(int dbId) {
        String query = "SELECT * FROM partner WHERE id=" + dbId;
        Partner[] partner = this.getPartnerByQuery(query);
        if (partner == null || partner.length == 0) {
            return (null);
        }
        return (partner[0]);
    }

    /*
     * loads the partner specific http headers from the db and assigns it to the
     * passed partner
     */
    private void loadHttpHeader(Partner partner) {
        int partnerId = partner.getDBId();
        PreparedStatement statement = null;
        ResultSet result = null;
        try {
            statement = this.configConnection.prepareStatement("SELECT * FROM httpheader WHERE partnerid=?");
            statement.setInt(1, partnerId);
            statement.setEscapeProcessing(true);
            result = statement.executeQuery();
            while (result.next()) {
                PartnerHttpHeader header = new PartnerHttpHeader();
                header.setKey(result.getString("key"));
                header.setValue(result.getString("value"));
                partner.addHttpHeader(header);
            }
        } catch (Exception e) {
            this.logger.severe("PartnerAccessDB.loadHttpHeader: " + e.getMessage());
            Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (Exception e) {
                    this.logger.severe("PartnerAccessDB.loadHttpHeader: " + e.getMessage());
                    Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
                }
            }
            if (result != null) {
                try {
                    result.close();
                } catch (Exception e) {
                    this.logger.severe("PartnerAccessDB.loadHttpHeader: " + e.getMessage());
                    Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
                }
            }
        }
    }

    /**
     * Deletes a single partners http header from the database
     */
    private void deleteHttpHeader(Partner partner) {
        PreparedStatement statement = null;
        try {
            statement = this.configConnection.prepareStatement("DELETE FROM httpheader WHERE partnerid=?");
            statement.setEscapeProcessing(true);
            statement.setInt(1, partner.getDBId());
            statement.execute();
        } catch (Exception e) {
            this.logger.severe("PartnerAccessDB.deleteHttpHeader: " + e.getMessage());
            Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (Exception e) {
                    //nop
                }
            }
        }
    }

    /**
     * Updates a single partners http header in the db
     */
    private void storeHttpHeader(Partner partner) {
        this.deleteHttpHeader(partner);
        //clear unused headers in the partner object
        partner.deleteEmptyHttpHeader();
        List<PartnerHttpHeader> headerList = partner.getHttpHeader();
        for (PartnerHttpHeader header : headerList) {
            PreparedStatement statement = null;
            try {
                statement = this.configConnection.prepareStatement("INSERT INTO httpheader(partnerid,key,value)VALUES(?,?,?)");
                statement.setEscapeProcessing(true);
                statement.setInt(1, partner.getDBId());
                statement.setString(2, header.getKey());
                statement.setString(3, header.getValue());
                statement.execute();
            } catch (Exception e) {
                this.logger.severe("PartnerAccessDB.storeHttpHeader: " + e.getMessage());
                Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
            } finally {
                if (statement != null) {
                    try {
                        statement.close();
                    } catch (Exception e) {
                        //nop
                    }
                }
            }
        }
    }
}
