//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/statistic/StatisticExportRequest.java,v 1.1 2012/04/18 14:10:39 heller Exp $
package de.mendelson.comm.as2.statistic;

import de.mendelson.comm.as2.partner.Partner;
import de.mendelson.util.clientserver.clients.datatransfer.DownloadRequest;
import java.io.Serializable;
/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Msg for the client server protocol
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class StatisticExportRequest extends DownloadRequest implements Serializable{

    private long startDate = 0;
    private long endDate = 0;
    private long timestep = 0;
    private Partner localStation = null;
    private Partner partner = null;


    @Override
    public String toString(){
        return( "Statistic export request" );
    }

    /**
     * @return the startDate
     */
    public long getStartDate() {
        return startDate;
    }

    /**
     * @param startDate the startDate to set
     */
    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    /**
     * @return the endDate
     */
    public long getEndDate() {
        return endDate;
    }

    /**
     * @param endDate the endDate to set
     */
    public void setEndDate(long endDate) {
        this.endDate = endDate;
    }

    /**
     * @return the timestep
     */
    public long getTimestep() {
        return timestep;
    }

    /**
     * @param timestep the timestep to set
     */
    public void setTimestep(long timestep) {
        this.timestep = timestep;
    }

    /**
     * @return the localStation
     */
    public Partner getLocalStation() {
        return localStation;
    }

    /**
     * @param localStation the localStation to set
     */
    public void setLocalStation(Partner localStation) {
        this.localStation = localStation;
    }

    /**
     * @return the partner
     */
    public Partner getPartner() {
        return partner;
    }

    /**
     * @param partner the partner to set
     */
    public void setPartner(Partner partner) {
        this.partner = partner;
    }

}
