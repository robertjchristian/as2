//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/timing/CertificateExpireController.java,v 1.1 2012/04/18 14:10:39 heller Exp $
package de.mendelson.comm.as2.timing;

import de.mendelson.util.security.cert.CertificateManager;
import de.mendelson.util.security.cert.KeystoreCertificate;
import de.mendelson.comm.as2.notification.Notification;
import de.mendelson.comm.as2.server.AS2Server;
import de.mendelson.util.MecResourceBundle;
import java.sql.Connection;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Controlles the certificates and checks if they will expire soon
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class CertificateExpireController {

    private int[] daysToExpire = new int[]{10, 3, 1};
    /**Logger to log inforamtion to*/
    private Logger logger = Logger.getLogger(AS2Server.SERVER_LOGGER_NAME);
    private CertificateManager manager;
    private CertificationExpireThread expireThread;
    private MecResourceBundle rb = null;
    private Connection configConnection;
    private Connection runtimeConnection;

    public CertificateExpireController(CertificateManager manager, Connection configConnection, Connection runtimeConnection) {
        this.configConnection = configConnection;
        this.runtimeConnection = runtimeConnection;
        //Load default resourcebundle
        try {
            this.rb = (MecResourceBundle) ResourceBundle.getBundle(
                    ResourceBundleMessageDeleteController.class.getName());
        } //load up resourcebundle
        catch (MissingResourceException e) {
            throw new RuntimeException("Oops..resource bundle " + e.getClassName() + " not found.");
        }
        this.manager = manager;
    }

    /**Starts the embedded task that guards the log
     */
    public void startCertExpireControl() {
        this.expireThread = new CertificationExpireThread(this.configConnection, this.runtimeConnection);
        Executors.newSingleThreadExecutor().submit(this.expireThread);
    }

    public class CertificationExpireThread implements Runnable {

        private Connection configConnection;
        private Connection runtimeConnection;

        private boolean stopRequested = false;
        //wait this time between checks, once a day
        private final long WAIT_TIME = TimeUnit.DAYS.toMillis(1);

        public CertificationExpireThread(Connection configConnection, Connection runtimeConnection) {
            this.configConnection = configConnection;
            this.runtimeConnection = runtimeConnection;
        }

        @Override
        public void run() {
            Thread.currentThread().setName("Cert expire check");
            while (!stopRequested) {
                Date actualDate = new Date();
                List<KeystoreCertificate> list = manager.getKeyStoreCertificateList();
                for (KeystoreCertificate certificate : list) {
                    Date expireDate = certificate.getNotAfter();
                    int certificateExpireDuration = this.getDayDiff(actualDate, expireDate);
                    for (int expireDuration : daysToExpire) {
                        if (certificateExpireDuration == expireDuration) {
                            Notification notification = new Notification(this.configConnection, this.runtimeConnection);
                            try {
                                notification.sendCertificateWillExpire(certificate, certificateExpireDuration);
                            } catch (Exception e) {
                                logger.severe("CertificationExpireThread: " + e.getMessage());
                                Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
                            }
                        }
                    }
                    if (certificateExpireDuration <= 0) {
                        Notification notification = new Notification(this.configConnection, this.runtimeConnection);
                        try {
                            notification.sendCertificateWillExpire(certificate, certificateExpireDuration);
                        } catch (Exception e) {
                            logger.severe("CertificationExpireThread: " + e.getMessage());
                            Notification.systemFailure(this.configConnection, this.runtimeConnection, e);
                        }
                    }
                }
                try {
                    Thread.sleep(WAIT_TIME);
                } catch (InterruptedException e) {
                    //nop
                }
            }
        }

        /**Computes and returns the number of days between the two passed dates
         *@param date1 first date
         *@param date2 second date
         *@param dateFormat1 Format of the first date
         *@param dateFormat2 Format of the second date
         */
        public int getDayDiff(Date firstDate, Date secondDate) {
            GregorianCalendar calendar = (GregorianCalendar) Calendar.getInstance();
            calendar.setTime(firstDate);
            int dayOfYear1 = calendar.get(Calendar.DAY_OF_YEAR);
            int year1 = calendar.get(Calendar.YEAR);
            calendar.setTime(secondDate);
            int dayOfYear2 = calendar.get(Calendar.DAY_OF_YEAR);
            int year2 = calendar.get(Calendar.YEAR);
            return ((year2 - year1) * 365 + (dayOfYear2 - dayOfYear1));
        }
    }
}
