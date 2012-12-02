//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/partner/PartnerCertificateInformationList.java,v 1.1 2012/04/18 14:10:32 heller Exp $
package de.mendelson.comm.as2.partner;

import de.mendelson.comm.as2.cem.CEMEntry;
import de.mendelson.util.security.cert.CertificateManager;
import de.mendelson.util.MecResourceBundle;
import java.io.Serializable;
import java.util.Collection;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Vector;
/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */

/**
 * Stores a certificate or key used by a partner. Every partner of a communication may use
 * several certificates with several priorities
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class PartnerCertificateInformationList implements Serializable {

    private PartnerCertificateInformation[] infoSSL = new PartnerCertificateInformation[]{null, null};
    private PartnerCertificateInformation[] infoCrypt = new PartnerCertificateInformation[]{null, null};
    private PartnerCertificateInformation[] infoSign = new PartnerCertificateInformation[]{null, null};
    private MecResourceBundle rb;

    public PartnerCertificateInformationList() {
        //load resource bundle
        try {
            this.rb = (MecResourceBundle) ResourceBundle.getBundle(
                    ResourceBundleCertificateInformation.class.getName());
        } catch (MissingResourceException e) {
            throw new RuntimeException("Oops..resource bundle " + e.getClassName() + " not found.");
        }
    }

    /**Returns the right info container for the passed category*/
    private PartnerCertificateInformation[] getContainerByCategory(int category) {
        if (category == CEMEntry.CATEGORY_CRYPT) {
            return (this.infoCrypt);
        } else if (category == CEMEntry.CATEGORY_SIGN) {
            return (this.infoSign);
        } else if (category == CEMEntry.CATEGORY_SSL) {
            return (this.infoSSL);
        } else {
            throw new IllegalArgumentException("PartnerCertificateInformationList.getContainerByCategory: Unsupported category " + category);
        }
    }

    /**Sets a single cert information to the partner, overwriting any existing with the same status, priority and type
     */
    public void setCertificateInformation(PartnerCertificateInformation information) {
        PartnerCertificateInformation[] container = this.getContainerByCategory(information.getCategory());
        container[information.getPriority() - 1] = information;
    }

    /**Returns the partner certificate with the passed category, status and priority. If
     * nothing is found, null is returned
     */
    public PartnerCertificateInformation getPartnerCertificate(int category, int priority) {
        PartnerCertificateInformation[] container = this.getContainerByCategory(category);
        return (container[priority - 1]);
    }

    /**Returns the partner certificate with the passed category. The status is accepted, the priority 1. If
     * nothing is found, null is returned
     */
    public PartnerCertificateInformation getPartnerCertificate(int category) {
        return (this.getPartnerCertificate(category, 1));
    }

    /**Inserts a new certificate into this partners list, shifting the existing certs of this category in their prio*/
    public PartnerCertificateInformation insertNewCertificate(String fingerprintSHA1, int category, int prio) {
        PartnerCertificateInformation[] container = this.getContainerByCategory(category);
        //dont shift if prio is 2
        if (prio == 2) {
            PartnerCertificateInformation information = new PartnerCertificateInformation(fingerprintSHA1, category);
            information.setPriority(prio);
            container[prio - 1] = information;
            return (information);
        } else if (prio == 1) {            
            //shift a value if a prio 1 value exists that is not of the same alias as the submitted
            PartnerCertificateInformation existingPrio1 = container[prio - 1];
            if (existingPrio1 != null && !existingPrio1.getFingerprintSHA1().equals(fingerprintSHA1)) {
                //shift value to prio2
                container[prio] = container[prio - 1];
                container[prio].setPriority(prio + 1);
            }
            //delete a prio 2 entry if it is the same as prio 1
            PartnerCertificateInformation existingPrio2 = container[prio];
            if (existingPrio2 != null && existingPrio2.getFingerprintSHA1().equals(fingerprintSHA1)) {
                //simply delete the prio2 entry
                container[prio] = null;
            }
            //now insert the new information to prio 1
            PartnerCertificateInformation information = new PartnerCertificateInformation(fingerprintSHA1, category);
            information.setPriority(prio);
            container[prio - 1] = information;
            return (information);
        }
        throw new IllegalArgumentException("PartnerCertificateInformationList.insertNewCertificate: Unsupported prio " + prio);
    }

    /**Returns a strinf that contains information about the actual certificate usage*/
    public String getCertificatePurposeDescription(CertificateManager manager, Partner partner, int category) {
        StringBuilder builder = new StringBuilder();
        PartnerCertificateInformation information1 = this.getPartnerCertificate(category, 1);        
        PartnerCertificateInformation information2 = this.getPartnerCertificate(category, 2);        
        if (information1 != null) {
            String alias1 = manager.getAliasByFingerprint(information1.getFingerprintSHA1());
            if (partner.isLocalStation()) {
                if (category == PartnerCertificateInformation.CATEGORY_CRYPT) {
                    builder.append(this.rb.getResourceString("localstation.decrypt.prio1",
                            new Object[]{partner.getName(), alias1}));
                    if (information2 != null) {
                        String alias2 = manager.getAliasByFingerprint(information2.getFingerprintSHA1());
                        builder.append(" ");                        
                        builder.append(this.rb.getResourceString("localstation.decrypt.prio2",
                                new Object[]{partner.getName(), alias2}));
                    }
                }
                if (category == PartnerCertificateInformation.CATEGORY_SIGN) {
                    builder.append(this.rb.getResourceString("localstation.sign.prio1",
                            new Object[]{partner.getName(), alias1}));
                }
            } else {
                if (category == PartnerCertificateInformation.CATEGORY_CRYPT) {
                    builder.append(this.rb.getResourceString("partner.encrypt.prio1",
                            new Object[]{partner.getName(), alias1}));
                }
                if (category == PartnerCertificateInformation.CATEGORY_SIGN) {
                    builder.append(this.rb.getResourceString("partner.sign.prio1",
                            new Object[]{partner.getName(), alias1}));
                    if (information2 != null) {
                        String alias2 = manager.getAliasByFingerprint(information2.getFingerprintSHA1());
                        builder.append(" ");
                        builder.append(this.rb.getResourceString("partner.sign.prio2",
                                new Object[]{partner.getName(), alias2}));
                    }
                }
            }
        }
        return (builder.toString());
    }

    /**Returns all available certificates as list*/
    public Collection<PartnerCertificateInformation> asList() {
        int[] categories = new int[]{CEMEntry.CATEGORY_CRYPT, CEMEntry.CATEGORY_SIGN, CEMEntry.CATEGORY_SSL};
        int[] prios = new int[]{1, 2};
        Vector<PartnerCertificateInformation> list = new Vector<PartnerCertificateInformation>();
        for (int category : categories) {
            PartnerCertificateInformation[] container = this.getContainerByCategory(category);
            for (int prio : prios) {
                if (container[prio - 1] != null) {
                    list.add(container[prio - 1]);
                }
            }
        }
        return (list);
    }
}
