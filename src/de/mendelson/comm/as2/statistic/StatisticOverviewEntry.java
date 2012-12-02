//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/statistic/StatisticOverviewEntry.java,v 1.1 2012/04/18 14:10:39 heller Exp $
package de.mendelson.comm.as2.statistic;
import java.io.Serializable;
import java.util.Date;
/**
 * Stores a statistic overview entry
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class StatisticOverviewEntry implements Serializable{

    public StatisticOverviewEntry() {
    }

    public String getLocalStationId() {
        return "";
    }

    public void setLocalStationId(String localStationId) {        
    }

    public String getPartnerId() {
        return "";
    }

    public void setPartnerId(String partnerId) {
    }

    public int getSendMessageCount() {
        return 0;
    }

    public void setSendMessageCount(int sendMessageCount) {
    }

    public int getReceivedMessageCount() {
        return 0;
    }

    public void setReceivedMessageCount(int receivedMessageCount) {
    }

    public int getSendWithFailureCount() {
        return 0;
    }

    public void setSendWithFailureCount(int sendWithFailureCount) {
    }

    public int getReceivedWithFailureCount() {
        return(0);
    }

    public void setReceivedWithFailureCount(int receivedWithFailureCount) {
    }

    public Date getResetDate() {
        return new Date();
    }

    public void setResetDate(Date resetDate) {
    }
}
